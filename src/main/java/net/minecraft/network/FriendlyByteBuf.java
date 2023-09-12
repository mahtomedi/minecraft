package net.minecraft.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FriendlyByteBuf extends ByteBuf {
    public static final int DEFAULT_NBT_QUOTA = 2097152;
    private final ByteBuf source;
    public static final short MAX_STRING_LENGTH = 32767;
    public static final int MAX_COMPONENT_STRING_LENGTH = 262144;
    private static final int PUBLIC_KEY_SIZE = 256;
    private static final int MAX_PUBLIC_KEY_HEADER_SIZE = 256;
    private static final int MAX_PUBLIC_KEY_LENGTH = 512;
    private static final Gson GSON = new Gson();

    public FriendlyByteBuf(ByteBuf param0) {
        this.source = param0;
    }

    @Deprecated
    public <T> T readWithCodecTrusted(DynamicOps<Tag> param0, Codec<T> param1) {
        return this.readWithCodec(param0, param1, NbtAccounter.unlimitedHeap());
    }

    @Deprecated
    public <T> T readWithCodec(DynamicOps<Tag> param0, Codec<T> param1, NbtAccounter param2) {
        Tag var0 = this.readNbt(param2);
        return Util.getOrThrow(param1.parse(param0, var0), param1x -> new DecoderException("Failed to decode: " + param1x + " " + var0));
    }

    @Deprecated
    public <T> FriendlyByteBuf writeWithCodec(DynamicOps<Tag> param0, Codec<T> param1, T param2) {
        Tag var0 = Util.getOrThrow(param1.encodeStart(param0, param2), param1x -> new EncoderException("Failed to encode: " + param1x + " " + param2));
        this.writeNbt(var0);
        return this;
    }

    public <T> T readJsonWithCodec(Codec<T> param0) {
        JsonElement var0 = GsonHelper.fromJson(GSON, this.readUtf(), JsonElement.class);
        DataResult<T> var1 = param0.parse(JsonOps.INSTANCE, var0);
        return Util.getOrThrow(var1, param0x -> new DecoderException("Failed to decode json: " + param0x));
    }

    public <T> void writeJsonWithCodec(Codec<T> param0, T param1) {
        DataResult<JsonElement> var0 = param0.encodeStart(JsonOps.INSTANCE, param1);
        this.writeUtf(GSON.toJson(Util.getOrThrow(var0, param1x -> new EncoderException("Failed to encode: " + param1x + " " + param1))));
    }

    public <T> void writeId(IdMap<T> param0, T param1) {
        int var0 = param0.getId(param1);
        if (var0 == -1) {
            throw new IllegalArgumentException("Can't find id for '" + param1 + "' in map " + param0);
        } else {
            this.writeVarInt(var0);
        }
    }

    public <T> void writeId(IdMap<Holder<T>> param0, Holder<T> param1, FriendlyByteBuf.Writer<T> param2) {
        switch(param1.kind()) {
            case REFERENCE:
                int var0 = param0.getId(param1);
                if (var0 == -1) {
                    throw new IllegalArgumentException("Can't find id for '" + param1.value() + "' in map " + param0);
                }

                this.writeVarInt(var0 + 1);
                break;
            case DIRECT:
                this.writeVarInt(0);
                param2.accept((T)this, param1.value());
        }

    }

    @Nullable
    public <T> T readById(IdMap<T> param0) {
        int var0 = this.readVarInt();
        return param0.byId(var0);
    }

    public <T> Holder<T> readById(IdMap<Holder<T>> param0, FriendlyByteBuf.Reader<T> param1) {
        int var0 = this.readVarInt();
        if (var0 == 0) {
            return Holder.direct(param1.apply((T)this));
        } else {
            Holder<T> var1 = param0.byId(var0 - 1);
            if (var1 == null) {
                throw new IllegalArgumentException("Can't find element with id " + var0);
            } else {
                return var1;
            }
        }
    }

    public static <T> IntFunction<T> limitValue(IntFunction<T> param0, int param1) {
        return param2 -> {
            if (param2 > param1) {
                throw new DecoderException("Value " + param2 + " is larger than limit " + param1);
            } else {
                return param0.apply(param2);
            }
        };
    }

    public <T, C extends Collection<T>> C readCollection(IntFunction<C> param0, FriendlyByteBuf.Reader<T> param1) {
        int var0 = this.readVarInt();
        C var1 = param0.apply(var0);

        for(int var2 = 0; var2 < var0; ++var2) {
            var1.add(param1.apply((T)this));
        }

        return var1;
    }

    public <T> void writeCollection(Collection<T> param0, FriendlyByteBuf.Writer<T> param1) {
        this.writeVarInt(param0.size());

        for(T var0 : param0) {
            param1.accept((T)this, var0);
        }

    }

    public <T> List<T> readList(FriendlyByteBuf.Reader<T> param0) {
        return this.readCollection(Lists::newArrayListWithCapacity, param0);
    }

    public IntList readIntIdList() {
        int var0 = this.readVarInt();
        IntList var1 = new IntArrayList();

        for(int var2 = 0; var2 < var0; ++var2) {
            var1.add(this.readVarInt());
        }

        return var1;
    }

    public void writeIntIdList(IntList param0) {
        this.writeVarInt(param0.size());
        param0.forEach(this::writeVarInt);
    }

    public <K, V, M extends Map<K, V>> M readMap(IntFunction<M> param0, FriendlyByteBuf.Reader<K> param1, FriendlyByteBuf.Reader<V> param2) {
        int var0 = this.readVarInt();
        M var1 = param0.apply(var0);

        for(int var2 = 0; var2 < var0; ++var2) {
            K var3 = param1.apply((K)this);
            V var4 = param2.apply((V)this);
            var1.put(var3, var4);
        }

        return var1;
    }

    public <K, V> Map<K, V> readMap(FriendlyByteBuf.Reader<K> param0, FriendlyByteBuf.Reader<V> param1) {
        return this.readMap(Maps::newHashMapWithExpectedSize, param0, param1);
    }

    public <K, V> void writeMap(Map<K, V> param0, FriendlyByteBuf.Writer<K> param1, FriendlyByteBuf.Writer<V> param2) {
        this.writeVarInt(param0.size());
        param0.forEach((param2x, param3) -> {
            param1.accept((K)this, param2x);
            param2.accept((V)this, param3);
        });
    }

    public void readWithCount(Consumer<FriendlyByteBuf> param0) {
        int var0 = this.readVarInt();

        for(int var1 = 0; var1 < var0; ++var1) {
            param0.accept(this);
        }

    }

    public <E extends Enum<E>> void writeEnumSet(EnumSet<E> param0, Class<E> param1) {
        E[] var0 = param1.getEnumConstants();
        BitSet var1 = new BitSet(var0.length);

        for(int var2 = 0; var2 < var0.length; ++var2) {
            var1.set(var2, param0.contains(var0[var2]));
        }

        this.writeFixedBitSet(var1, var0.length);
    }

    public <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> param0) {
        E[] var0 = param0.getEnumConstants();
        BitSet var1 = this.readFixedBitSet(var0.length);
        EnumSet<E> var2 = EnumSet.noneOf(param0);

        for(int var3 = 0; var3 < var0.length; ++var3) {
            if (var1.get(var3)) {
                var2.add(var0[var3]);
            }
        }

        return var2;
    }

    public <T> void writeOptional(Optional<T> param0, FriendlyByteBuf.Writer<T> param1) {
        if (param0.isPresent()) {
            this.writeBoolean(true);
            param1.accept((T)this, param0.get());
        } else {
            this.writeBoolean(false);
        }

    }

    public <T> Optional<T> readOptional(FriendlyByteBuf.Reader<T> param0) {
        return this.readBoolean() ? Optional.of(param0.apply((T)this)) : Optional.empty();
    }

    @Nullable
    public <T> T readNullable(FriendlyByteBuf.Reader<T> param0) {
        return this.readBoolean() ? param0.apply((T)this) : null;
    }

    public <T> void writeNullable(@Nullable T param0, FriendlyByteBuf.Writer<T> param1) {
        if (param0 != null) {
            this.writeBoolean(true);
            param1.accept((T)this, param0);
        } else {
            this.writeBoolean(false);
        }

    }

    public <L, R> void writeEither(Either<L, R> param0, FriendlyByteBuf.Writer<L> param1, FriendlyByteBuf.Writer<R> param2) {
        param0.ifLeft(param1x -> {
            this.writeBoolean(true);
            param1.accept((L)this, param1x);
        }).ifRight(param1x -> {
            this.writeBoolean(false);
            param2.accept((R)this, param1x);
        });
    }

    public <L, R> Either<L, R> readEither(FriendlyByteBuf.Reader<L> param0, FriendlyByteBuf.Reader<R> param1) {
        return this.readBoolean() ? Either.left(param0.apply((L)this)) : Either.right(param1.apply((R)this));
    }

    public byte[] readByteArray() {
        return this.readByteArray(this.readableBytes());
    }

    public FriendlyByteBuf writeByteArray(byte[] param0) {
        this.writeVarInt(param0.length);
        this.writeBytes(param0);
        return this;
    }

    public byte[] readByteArray(int param0) {
        int var0 = this.readVarInt();
        if (var0 > param0) {
            throw new DecoderException("ByteArray with size " + var0 + " is bigger than allowed " + param0);
        } else {
            byte[] var1 = new byte[var0];
            this.readBytes(var1);
            return var1;
        }
    }

    public FriendlyByteBuf writeVarIntArray(int[] param0) {
        this.writeVarInt(param0.length);

        for(int var0 : param0) {
            this.writeVarInt(var0);
        }

        return this;
    }

    public int[] readVarIntArray() {
        return this.readVarIntArray(this.readableBytes());
    }

    public int[] readVarIntArray(int param0) {
        int var0 = this.readVarInt();
        if (var0 > param0) {
            throw new DecoderException("VarIntArray with size " + var0 + " is bigger than allowed " + param0);
        } else {
            int[] var1 = new int[var0];

            for(int var2 = 0; var2 < var1.length; ++var2) {
                var1[var2] = this.readVarInt();
            }

            return var1;
        }
    }

    public FriendlyByteBuf writeLongArray(long[] param0) {
        this.writeVarInt(param0.length);

        for(long var0 : param0) {
            this.writeLong(var0);
        }

        return this;
    }

    public long[] readLongArray() {
        return this.readLongArray(null);
    }

    public long[] readLongArray(@Nullable long[] param0) {
        return this.readLongArray(param0, this.readableBytes() / 8);
    }

    public long[] readLongArray(@Nullable long[] param0, int param1) {
        int var0 = this.readVarInt();
        if (param0 == null || param0.length != var0) {
            if (var0 > param1) {
                throw new DecoderException("LongArray with size " + var0 + " is bigger than allowed " + param1);
            }

            param0 = new long[var0];
        }

        for(int var1 = 0; var1 < param0.length; ++var1) {
            param0[var1] = this.readLong();
        }

        return param0;
    }

    public BlockPos readBlockPos() {
        return BlockPos.of(this.readLong());
    }

    public FriendlyByteBuf writeBlockPos(BlockPos param0) {
        this.writeLong(param0.asLong());
        return this;
    }

    public ChunkPos readChunkPos() {
        return new ChunkPos(this.readLong());
    }

    public FriendlyByteBuf writeChunkPos(ChunkPos param0) {
        this.writeLong(param0.toLong());
        return this;
    }

    public SectionPos readSectionPos() {
        return SectionPos.of(this.readLong());
    }

    public FriendlyByteBuf writeSectionPos(SectionPos param0) {
        this.writeLong(param0.asLong());
        return this;
    }

    public GlobalPos readGlobalPos() {
        ResourceKey<Level> var0 = this.readResourceKey(Registries.DIMENSION);
        BlockPos var1 = this.readBlockPos();
        return GlobalPos.of(var0, var1);
    }

    public void writeGlobalPos(GlobalPos param0) {
        this.writeResourceKey(param0.dimension());
        this.writeBlockPos(param0.pos());
    }

    public Vector3f readVector3f() {
        return new Vector3f(this.readFloat(), this.readFloat(), this.readFloat());
    }

    public void writeVector3f(Vector3f param0) {
        this.writeFloat(param0.x());
        this.writeFloat(param0.y());
        this.writeFloat(param0.z());
    }

    public Quaternionf readQuaternion() {
        return new Quaternionf(this.readFloat(), this.readFloat(), this.readFloat(), this.readFloat());
    }

    public void writeQuaternion(Quaternionf param0) {
        this.writeFloat(param0.x);
        this.writeFloat(param0.y);
        this.writeFloat(param0.z);
        this.writeFloat(param0.w);
    }

    public Vec3 readVec3() {
        return new Vec3(this.readDouble(), this.readDouble(), this.readDouble());
    }

    public void writeVec3(Vec3 param0) {
        this.writeDouble(param0.x());
        this.writeDouble(param0.y());
        this.writeDouble(param0.z());
    }

    public Component readComponent() {
        Component var0 = Component.Serializer.fromJson(this.readUtf(262144));
        if (var0 == null) {
            throw new DecoderException("Received unexpected null component");
        } else {
            return var0;
        }
    }

    public FriendlyByteBuf writeComponent(Component param0) {
        return this.writeUtf(Component.Serializer.toJson(param0), 262144);
    }

    public <T extends Enum<T>> T readEnum(Class<T> param0) {
        return param0.getEnumConstants()[this.readVarInt()];
    }

    public FriendlyByteBuf writeEnum(Enum<?> param0) {
        return this.writeVarInt(param0.ordinal());
    }

    public <T> T readById(IntFunction<T> param0) {
        int var0 = this.readVarInt();
        return param0.apply(var0);
    }

    public <T> FriendlyByteBuf writeById(ToIntFunction<T> param0, T param1) {
        int var0 = param0.applyAsInt(param1);
        return this.writeVarInt(var0);
    }

    public int readVarInt() {
        return VarInt.read(this.source);
    }

    public long readVarLong() {
        return VarLong.read(this.source);
    }

    public FriendlyByteBuf writeUUID(UUID param0) {
        this.writeLong(param0.getMostSignificantBits());
        this.writeLong(param0.getLeastSignificantBits());
        return this;
    }

    public UUID readUUID() {
        return new UUID(this.readLong(), this.readLong());
    }

    public FriendlyByteBuf writeVarInt(int param0x) {
        VarInt.write(this.source, param0x);
        return this;
    }

    public FriendlyByteBuf writeVarLong(long param0) {
        VarLong.write(this.source, param0);
        return this;
    }

    public FriendlyByteBuf writeNbt(@Nullable Tag param0) {
        if (param0 == null) {
            param0 = EndTag.INSTANCE;
        }

        try {
            NbtIo.writeAnyTag(param0, new ByteBufOutputStream(this));
            return this;
        } catch (IOException var3) {
            throw new EncoderException(var3);
        }
    }

    @Nullable
    public CompoundTag readNbt() {
        Tag var0 = this.readNbt(NbtAccounter.create(2097152L));
        if (var0 != null && !(var0 instanceof CompoundTag)) {
            throw new DecoderException("Not a compound tag: " + var0);
        } else {
            return (CompoundTag)var0;
        }
    }

    @Nullable
    public Tag readNbt(NbtAccounter param0) {
        try {
            Tag var0 = NbtIo.readAnyTag(new ByteBufInputStream(this), param0);
            return var0.getId() == 0 ? null : var0;
        } catch (IOException var3) {
            throw new EncoderException(var3);
        }
    }

    public FriendlyByteBuf writeItem(ItemStack param0) {
        if (param0.isEmpty()) {
            this.writeBoolean(false);
        } else {
            this.writeBoolean(true);
            Item var0 = param0.getItem();
            this.writeId(BuiltInRegistries.ITEM, var0);
            this.writeByte(param0.getCount());
            CompoundTag var1 = null;
            if (var0.canBeDepleted() || var0.shouldOverrideMultiplayerNbt()) {
                var1 = param0.getTag();
            }

            this.writeNbt(var1);
        }

        return this;
    }

    public ItemStack readItem() {
        if (!this.readBoolean()) {
            return ItemStack.EMPTY;
        } else {
            Item var0 = this.readById(BuiltInRegistries.ITEM);
            int var1 = this.readByte();
            ItemStack var2 = new ItemStack(var0, var1);
            var2.setTag(this.readNbt());
            return var2;
        }
    }

    public String readUtf() {
        return this.readUtf(32767);
    }

    public String readUtf(int param0) {
        return Utf8String.read(this.source, param0);
    }

    public FriendlyByteBuf writeUtf(String param0) {
        return this.writeUtf(param0, 32767);
    }

    public FriendlyByteBuf writeUtf(String param0, int param1) {
        Utf8String.write(this.source, param0, param1);
        return this;
    }

    public ResourceLocation readResourceLocation() {
        return new ResourceLocation(this.readUtf(32767));
    }

    public FriendlyByteBuf writeResourceLocation(ResourceLocation param0) {
        this.writeUtf(param0.toString());
        return this;
    }

    public <T> ResourceKey<T> readResourceKey(ResourceKey<? extends Registry<T>> param0) {
        ResourceLocation var0 = this.readResourceLocation();
        return ResourceKey.create(param0, var0);
    }

    public void writeResourceKey(ResourceKey<?> param0) {
        this.writeResourceLocation(param0.location());
    }

    public <T> ResourceKey<? extends Registry<T>> readRegistryKey() {
        ResourceLocation var0 = this.readResourceLocation();
        return ResourceKey.createRegistryKey(var0);
    }

    public Date readDate() {
        return new Date(this.readLong());
    }

    public FriendlyByteBuf writeDate(Date param0) {
        this.writeLong(param0.getTime());
        return this;
    }

    public Instant readInstant() {
        return Instant.ofEpochMilli(this.readLong());
    }

    public void writeInstant(Instant param0) {
        this.writeLong(param0.toEpochMilli());
    }

    public PublicKey readPublicKey() {
        try {
            return Crypt.byteToPublicKey(this.readByteArray(512));
        } catch (CryptException var2) {
            throw new DecoderException("Malformed public key bytes", var2);
        }
    }

    public FriendlyByteBuf writePublicKey(PublicKey param0) {
        this.writeByteArray(param0.getEncoded());
        return this;
    }

    public BlockHitResult readBlockHitResult() {
        BlockPos var0 = this.readBlockPos();
        Direction var1 = this.readEnum(Direction.class);
        float var2 = this.readFloat();
        float var3 = this.readFloat();
        float var4 = this.readFloat();
        boolean var5 = this.readBoolean();
        return new BlockHitResult(
            new Vec3((double)var0.getX() + (double)var2, (double)var0.getY() + (double)var3, (double)var0.getZ() + (double)var4), var1, var0, var5
        );
    }

    public void writeBlockHitResult(BlockHitResult param0) {
        BlockPos var0 = param0.getBlockPos();
        this.writeBlockPos(var0);
        this.writeEnum(param0.getDirection());
        Vec3 var1 = param0.getLocation();
        this.writeFloat((float)(var1.x - (double)var0.getX()));
        this.writeFloat((float)(var1.y - (double)var0.getY()));
        this.writeFloat((float)(var1.z - (double)var0.getZ()));
        this.writeBoolean(param0.isInside());
    }

    public BitSet readBitSet() {
        return BitSet.valueOf(this.readLongArray());
    }

    public void writeBitSet(BitSet param0) {
        this.writeLongArray(param0.toLongArray());
    }

    public BitSet readFixedBitSet(int param0) {
        byte[] var0 = new byte[Mth.positiveCeilDiv(param0, 8)];
        this.readBytes(var0);
        return BitSet.valueOf(var0);
    }

    public void writeFixedBitSet(BitSet param0, int param1) {
        if (param0.length() > param1) {
            throw new EncoderException("BitSet is larger than expected size (" + param0.length() + ">" + param1 + ")");
        } else {
            byte[] var0 = param0.toByteArray();
            this.writeBytes(Arrays.copyOf(var0, Mth.positiveCeilDiv(param1, 8)));
        }
    }

    public GameProfile readGameProfile() {
        UUID var0 = this.readUUID();
        String var1 = this.readUtf(16);
        GameProfile var2 = new GameProfile(var0, var1);
        var2.getProperties().putAll(this.readGameProfileProperties());
        return var2;
    }

    public void writeGameProfile(GameProfile param0) {
        this.writeUUID(param0.getId());
        this.writeUtf(param0.getName());
        this.writeGameProfileProperties(param0.getProperties());
    }

    public PropertyMap readGameProfileProperties() {
        PropertyMap var0 = new PropertyMap();
        this.readWithCount(param1 -> {
            Property var0x = this.readProperty();
            var0.put(var0x.name(), var0x);
        });
        return var0;
    }

    public void writeGameProfileProperties(PropertyMap param0) {
        this.writeCollection(param0.values(), FriendlyByteBuf::writeProperty);
    }

    public Property readProperty() {
        String var0 = this.readUtf();
        String var1 = this.readUtf();
        String var2 = this.readNullable(FriendlyByteBuf::readUtf);
        return new Property(var0, var1, var2);
    }

    public void writeProperty(Property param0x) {
        this.writeUtf(param0x.name());
        this.writeUtf(param0x.value());
        this.writeNullable(param0x.signature(), FriendlyByteBuf::writeUtf);
    }

    @Override
    public boolean isContiguous() {
        return this.source.isContiguous();
    }

    @Override
    public int maxFastWritableBytes() {
        return this.source.maxFastWritableBytes();
    }

    @Override
    public int capacity() {
        return this.source.capacity();
    }

    public FriendlyByteBuf capacity(int param0) {
        this.source.capacity(param0);
        return this;
    }

    @Override
    public int maxCapacity() {
        return this.source.maxCapacity();
    }

    @Override
    public ByteBufAllocator alloc() {
        return this.source.alloc();
    }

    @Override
    public ByteOrder order() {
        return this.source.order();
    }

    @Override
    public ByteBuf order(ByteOrder param0) {
        return this.source.order(param0);
    }

    @Override
    public ByteBuf unwrap() {
        return this.source;
    }

    @Override
    public boolean isDirect() {
        return this.source.isDirect();
    }

    @Override
    public boolean isReadOnly() {
        return this.source.isReadOnly();
    }

    @Override
    public ByteBuf asReadOnly() {
        return this.source.asReadOnly();
    }

    @Override
    public int readerIndex() {
        return this.source.readerIndex();
    }

    public FriendlyByteBuf readerIndex(int param0) {
        this.source.readerIndex(param0);
        return this;
    }

    @Override
    public int writerIndex() {
        return this.source.writerIndex();
    }

    public FriendlyByteBuf writerIndex(int param0) {
        this.source.writerIndex(param0);
        return this;
    }

    public FriendlyByteBuf setIndex(int param0, int param1) {
        this.source.setIndex(param0, param1);
        return this;
    }

    @Override
    public int readableBytes() {
        return this.source.readableBytes();
    }

    @Override
    public int writableBytes() {
        return this.source.writableBytes();
    }

    @Override
    public int maxWritableBytes() {
        return this.source.maxWritableBytes();
    }

    @Override
    public boolean isReadable() {
        return this.source.isReadable();
    }

    @Override
    public boolean isReadable(int param0) {
        return this.source.isReadable(param0);
    }

    @Override
    public boolean isWritable() {
        return this.source.isWritable();
    }

    @Override
    public boolean isWritable(int param0) {
        return this.source.isWritable(param0);
    }

    public FriendlyByteBuf clear() {
        this.source.clear();
        return this;
    }

    public FriendlyByteBuf markReaderIndex() {
        this.source.markReaderIndex();
        return this;
    }

    public FriendlyByteBuf resetReaderIndex() {
        this.source.resetReaderIndex();
        return this;
    }

    public FriendlyByteBuf markWriterIndex() {
        this.source.markWriterIndex();
        return this;
    }

    public FriendlyByteBuf resetWriterIndex() {
        this.source.resetWriterIndex();
        return this;
    }

    public FriendlyByteBuf discardReadBytes() {
        this.source.discardReadBytes();
        return this;
    }

    public FriendlyByteBuf discardSomeReadBytes() {
        this.source.discardSomeReadBytes();
        return this;
    }

    public FriendlyByteBuf ensureWritable(int param0) {
        this.source.ensureWritable(param0);
        return this;
    }

    @Override
    public int ensureWritable(int param0, boolean param1) {
        return this.source.ensureWritable(param0, param1);
    }

    @Override
    public boolean getBoolean(int param0) {
        return this.source.getBoolean(param0);
    }

    @Override
    public byte getByte(int param0) {
        return this.source.getByte(param0);
    }

    @Override
    public short getUnsignedByte(int param0) {
        return this.source.getUnsignedByte(param0);
    }

    @Override
    public short getShort(int param0) {
        return this.source.getShort(param0);
    }

    @Override
    public short getShortLE(int param0) {
        return this.source.getShortLE(param0);
    }

    @Override
    public int getUnsignedShort(int param0) {
        return this.source.getUnsignedShort(param0);
    }

    @Override
    public int getUnsignedShortLE(int param0) {
        return this.source.getUnsignedShortLE(param0);
    }

    @Override
    public int getMedium(int param0) {
        return this.source.getMedium(param0);
    }

    @Override
    public int getMediumLE(int param0) {
        return this.source.getMediumLE(param0);
    }

    @Override
    public int getUnsignedMedium(int param0) {
        return this.source.getUnsignedMedium(param0);
    }

    @Override
    public int getUnsignedMediumLE(int param0) {
        return this.source.getUnsignedMediumLE(param0);
    }

    @Override
    public int getInt(int param0) {
        return this.source.getInt(param0);
    }

    @Override
    public int getIntLE(int param0) {
        return this.source.getIntLE(param0);
    }

    @Override
    public long getUnsignedInt(int param0) {
        return this.source.getUnsignedInt(param0);
    }

    @Override
    public long getUnsignedIntLE(int param0) {
        return this.source.getUnsignedIntLE(param0);
    }

    @Override
    public long getLong(int param0) {
        return this.source.getLong(param0);
    }

    @Override
    public long getLongLE(int param0) {
        return this.source.getLongLE(param0);
    }

    @Override
    public char getChar(int param0) {
        return this.source.getChar(param0);
    }

    @Override
    public float getFloat(int param0) {
        return this.source.getFloat(param0);
    }

    @Override
    public double getDouble(int param0) {
        return this.source.getDouble(param0);
    }

    public FriendlyByteBuf getBytes(int param0, ByteBuf param1) {
        this.source.getBytes(param0, param1);
        return this;
    }

    public FriendlyByteBuf getBytes(int param0, ByteBuf param1, int param2) {
        this.source.getBytes(param0, param1, param2);
        return this;
    }

    public FriendlyByteBuf getBytes(int param0, ByteBuf param1, int param2, int param3) {
        this.source.getBytes(param0, param1, param2, param3);
        return this;
    }

    public FriendlyByteBuf getBytes(int param0, byte[] param1) {
        this.source.getBytes(param0, param1);
        return this;
    }

    public FriendlyByteBuf getBytes(int param0, byte[] param1, int param2, int param3) {
        this.source.getBytes(param0, param1, param2, param3);
        return this;
    }

    public FriendlyByteBuf getBytes(int param0, ByteBuffer param1) {
        this.source.getBytes(param0, param1);
        return this;
    }

    public FriendlyByteBuf getBytes(int param0, OutputStream param1, int param2) throws IOException {
        this.source.getBytes(param0, param1, param2);
        return this;
    }

    @Override
    public int getBytes(int param0, GatheringByteChannel param1, int param2) throws IOException {
        return this.source.getBytes(param0, param1, param2);
    }

    @Override
    public int getBytes(int param0, FileChannel param1, long param2, int param3) throws IOException {
        return this.source.getBytes(param0, param1, param2, param3);
    }

    @Override
    public CharSequence getCharSequence(int param0, int param1, Charset param2) {
        return this.source.getCharSequence(param0, param1, param2);
    }

    public FriendlyByteBuf setBoolean(int param0, boolean param1) {
        this.source.setBoolean(param0, param1);
        return this;
    }

    public FriendlyByteBuf setByte(int param0, int param1) {
        this.source.setByte(param0, param1);
        return this;
    }

    public FriendlyByteBuf setShort(int param0, int param1) {
        this.source.setShort(param0, param1);
        return this;
    }

    public FriendlyByteBuf setShortLE(int param0, int param1) {
        this.source.setShortLE(param0, param1);
        return this;
    }

    public FriendlyByteBuf setMedium(int param0, int param1) {
        this.source.setMedium(param0, param1);
        return this;
    }

    public FriendlyByteBuf setMediumLE(int param0, int param1) {
        this.source.setMediumLE(param0, param1);
        return this;
    }

    public FriendlyByteBuf setInt(int param0, int param1) {
        this.source.setInt(param0, param1);
        return this;
    }

    public FriendlyByteBuf setIntLE(int param0, int param1) {
        this.source.setIntLE(param0, param1);
        return this;
    }

    public FriendlyByteBuf setLong(int param0, long param1) {
        this.source.setLong(param0, param1);
        return this;
    }

    public FriendlyByteBuf setLongLE(int param0, long param1) {
        this.source.setLongLE(param0, param1);
        return this;
    }

    public FriendlyByteBuf setChar(int param0, int param1) {
        this.source.setChar(param0, param1);
        return this;
    }

    public FriendlyByteBuf setFloat(int param0, float param1) {
        this.source.setFloat(param0, param1);
        return this;
    }

    public FriendlyByteBuf setDouble(int param0, double param1) {
        this.source.setDouble(param0, param1);
        return this;
    }

    public FriendlyByteBuf setBytes(int param0, ByteBuf param1) {
        this.source.setBytes(param0, param1);
        return this;
    }

    public FriendlyByteBuf setBytes(int param0, ByteBuf param1, int param2) {
        this.source.setBytes(param0, param1, param2);
        return this;
    }

    public FriendlyByteBuf setBytes(int param0, ByteBuf param1, int param2, int param3) {
        this.source.setBytes(param0, param1, param2, param3);
        return this;
    }

    public FriendlyByteBuf setBytes(int param0, byte[] param1) {
        this.source.setBytes(param0, param1);
        return this;
    }

    public FriendlyByteBuf setBytes(int param0, byte[] param1, int param2, int param3) {
        this.source.setBytes(param0, param1, param2, param3);
        return this;
    }

    public FriendlyByteBuf setBytes(int param0, ByteBuffer param1) {
        this.source.setBytes(param0, param1);
        return this;
    }

    @Override
    public int setBytes(int param0, InputStream param1, int param2) throws IOException {
        return this.source.setBytes(param0, param1, param2);
    }

    @Override
    public int setBytes(int param0, ScatteringByteChannel param1, int param2) throws IOException {
        return this.source.setBytes(param0, param1, param2);
    }

    @Override
    public int setBytes(int param0, FileChannel param1, long param2, int param3) throws IOException {
        return this.source.setBytes(param0, param1, param2, param3);
    }

    public FriendlyByteBuf setZero(int param0, int param1) {
        this.source.setZero(param0, param1);
        return this;
    }

    @Override
    public int setCharSequence(int param0, CharSequence param1, Charset param2) {
        return this.source.setCharSequence(param0, param1, param2);
    }

    @Override
    public boolean readBoolean() {
        return this.source.readBoolean();
    }

    @Override
    public byte readByte() {
        return this.source.readByte();
    }

    @Override
    public short readUnsignedByte() {
        return this.source.readUnsignedByte();
    }

    @Override
    public short readShort() {
        return this.source.readShort();
    }

    @Override
    public short readShortLE() {
        return this.source.readShortLE();
    }

    @Override
    public int readUnsignedShort() {
        return this.source.readUnsignedShort();
    }

    @Override
    public int readUnsignedShortLE() {
        return this.source.readUnsignedShortLE();
    }

    @Override
    public int readMedium() {
        return this.source.readMedium();
    }

    @Override
    public int readMediumLE() {
        return this.source.readMediumLE();
    }

    @Override
    public int readUnsignedMedium() {
        return this.source.readUnsignedMedium();
    }

    @Override
    public int readUnsignedMediumLE() {
        return this.source.readUnsignedMediumLE();
    }

    @Override
    public int readInt() {
        return this.source.readInt();
    }

    @Override
    public int readIntLE() {
        return this.source.readIntLE();
    }

    @Override
    public long readUnsignedInt() {
        return this.source.readUnsignedInt();
    }

    @Override
    public long readUnsignedIntLE() {
        return this.source.readUnsignedIntLE();
    }

    @Override
    public long readLong() {
        return this.source.readLong();
    }

    @Override
    public long readLongLE() {
        return this.source.readLongLE();
    }

    @Override
    public char readChar() {
        return this.source.readChar();
    }

    @Override
    public float readFloat() {
        return this.source.readFloat();
    }

    @Override
    public double readDouble() {
        return this.source.readDouble();
    }

    @Override
    public ByteBuf readBytes(int param0) {
        return this.source.readBytes(param0);
    }

    @Override
    public ByteBuf readSlice(int param0) {
        return this.source.readSlice(param0);
    }

    @Override
    public ByteBuf readRetainedSlice(int param0) {
        return this.source.readRetainedSlice(param0);
    }

    public FriendlyByteBuf readBytes(ByteBuf param0) {
        this.source.readBytes(param0);
        return this;
    }

    public FriendlyByteBuf readBytes(ByteBuf param0, int param1) {
        this.source.readBytes(param0, param1);
        return this;
    }

    public FriendlyByteBuf readBytes(ByteBuf param0, int param1, int param2) {
        this.source.readBytes(param0, param1, param2);
        return this;
    }

    public FriendlyByteBuf readBytes(byte[] param0) {
        this.source.readBytes(param0);
        return this;
    }

    public FriendlyByteBuf readBytes(byte[] param0, int param1, int param2) {
        this.source.readBytes(param0, param1, param2);
        return this;
    }

    public FriendlyByteBuf readBytes(ByteBuffer param0) {
        this.source.readBytes(param0);
        return this;
    }

    public FriendlyByteBuf readBytes(OutputStream param0, int param1) throws IOException {
        this.source.readBytes(param0, param1);
        return this;
    }

    @Override
    public int readBytes(GatheringByteChannel param0, int param1) throws IOException {
        return this.source.readBytes(param0, param1);
    }

    @Override
    public CharSequence readCharSequence(int param0, Charset param1) {
        return this.source.readCharSequence(param0, param1);
    }

    @Override
    public int readBytes(FileChannel param0, long param1, int param2) throws IOException {
        return this.source.readBytes(param0, param1, param2);
    }

    public FriendlyByteBuf skipBytes(int param0) {
        this.source.skipBytes(param0);
        return this;
    }

    public FriendlyByteBuf writeBoolean(boolean param0) {
        this.source.writeBoolean(param0);
        return this;
    }

    public FriendlyByteBuf writeByte(int param0) {
        this.source.writeByte(param0);
        return this;
    }

    public FriendlyByteBuf writeShort(int param0) {
        this.source.writeShort(param0);
        return this;
    }

    public FriendlyByteBuf writeShortLE(int param0) {
        this.source.writeShortLE(param0);
        return this;
    }

    public FriendlyByteBuf writeMedium(int param0) {
        this.source.writeMedium(param0);
        return this;
    }

    public FriendlyByteBuf writeMediumLE(int param0) {
        this.source.writeMediumLE(param0);
        return this;
    }

    public FriendlyByteBuf writeInt(int param0) {
        this.source.writeInt(param0);
        return this;
    }

    public FriendlyByteBuf writeIntLE(int param0) {
        this.source.writeIntLE(param0);
        return this;
    }

    public FriendlyByteBuf writeLong(long param0) {
        this.source.writeLong(param0);
        return this;
    }

    public FriendlyByteBuf writeLongLE(long param0) {
        this.source.writeLongLE(param0);
        return this;
    }

    public FriendlyByteBuf writeChar(int param0) {
        this.source.writeChar(param0);
        return this;
    }

    public FriendlyByteBuf writeFloat(float param0) {
        this.source.writeFloat(param0);
        return this;
    }

    public FriendlyByteBuf writeDouble(double param0) {
        this.source.writeDouble(param0);
        return this;
    }

    public FriendlyByteBuf writeBytes(ByteBuf param0) {
        this.source.writeBytes(param0);
        return this;
    }

    public FriendlyByteBuf writeBytes(ByteBuf param0, int param1) {
        this.source.writeBytes(param0, param1);
        return this;
    }

    public FriendlyByteBuf writeBytes(ByteBuf param0, int param1, int param2) {
        this.source.writeBytes(param0, param1, param2);
        return this;
    }

    public FriendlyByteBuf writeBytes(byte[] param0) {
        this.source.writeBytes(param0);
        return this;
    }

    public FriendlyByteBuf writeBytes(byte[] param0, int param1, int param2) {
        this.source.writeBytes(param0, param1, param2);
        return this;
    }

    public FriendlyByteBuf writeBytes(ByteBuffer param0) {
        this.source.writeBytes(param0);
        return this;
    }

    @Override
    public int writeBytes(InputStream param0, int param1) throws IOException {
        return this.source.writeBytes(param0, param1);
    }

    @Override
    public int writeBytes(ScatteringByteChannel param0, int param1) throws IOException {
        return this.source.writeBytes(param0, param1);
    }

    @Override
    public int writeBytes(FileChannel param0, long param1, int param2) throws IOException {
        return this.source.writeBytes(param0, param1, param2);
    }

    public FriendlyByteBuf writeZero(int param0) {
        this.source.writeZero(param0);
        return this;
    }

    @Override
    public int writeCharSequence(CharSequence param0, Charset param1) {
        return this.source.writeCharSequence(param0, param1);
    }

    @Override
    public int indexOf(int param0, int param1, byte param2) {
        return this.source.indexOf(param0, param1, param2);
    }

    @Override
    public int bytesBefore(byte param0) {
        return this.source.bytesBefore(param0);
    }

    @Override
    public int bytesBefore(int param0, byte param1) {
        return this.source.bytesBefore(param0, param1);
    }

    @Override
    public int bytesBefore(int param0, int param1, byte param2) {
        return this.source.bytesBefore(param0, param1, param2);
    }

    @Override
    public int forEachByte(ByteProcessor param0) {
        return this.source.forEachByte(param0);
    }

    @Override
    public int forEachByte(int param0, int param1, ByteProcessor param2) {
        return this.source.forEachByte(param0, param1, param2);
    }

    @Override
    public int forEachByteDesc(ByteProcessor param0) {
        return this.source.forEachByteDesc(param0);
    }

    @Override
    public int forEachByteDesc(int param0, int param1, ByteProcessor param2) {
        return this.source.forEachByteDesc(param0, param1, param2);
    }

    @Override
    public ByteBuf copy() {
        return this.source.copy();
    }

    @Override
    public ByteBuf copy(int param0, int param1) {
        return this.source.copy(param0, param1);
    }

    @Override
    public ByteBuf slice() {
        return this.source.slice();
    }

    @Override
    public ByteBuf retainedSlice() {
        return this.source.retainedSlice();
    }

    @Override
    public ByteBuf slice(int param0, int param1) {
        return this.source.slice(param0, param1);
    }

    @Override
    public ByteBuf retainedSlice(int param0, int param1) {
        return this.source.retainedSlice(param0, param1);
    }

    @Override
    public ByteBuf duplicate() {
        return this.source.duplicate();
    }

    @Override
    public ByteBuf retainedDuplicate() {
        return this.source.retainedDuplicate();
    }

    @Override
    public int nioBufferCount() {
        return this.source.nioBufferCount();
    }

    @Override
    public ByteBuffer nioBuffer() {
        return this.source.nioBuffer();
    }

    @Override
    public ByteBuffer nioBuffer(int param0, int param1) {
        return this.source.nioBuffer(param0, param1);
    }

    @Override
    public ByteBuffer internalNioBuffer(int param0, int param1) {
        return this.source.internalNioBuffer(param0, param1);
    }

    @Override
    public ByteBuffer[] nioBuffers() {
        return this.source.nioBuffers();
    }

    @Override
    public ByteBuffer[] nioBuffers(int param0, int param1) {
        return this.source.nioBuffers(param0, param1);
    }

    @Override
    public boolean hasArray() {
        return this.source.hasArray();
    }

    @Override
    public byte[] array() {
        return this.source.array();
    }

    @Override
    public int arrayOffset() {
        return this.source.arrayOffset();
    }

    @Override
    public boolean hasMemoryAddress() {
        return this.source.hasMemoryAddress();
    }

    @Override
    public long memoryAddress() {
        return this.source.memoryAddress();
    }

    @Override
    public String toString(Charset param0) {
        return this.source.toString(param0);
    }

    @Override
    public String toString(int param0, int param1, Charset param2) {
        return this.source.toString(param0, param1, param2);
    }

    @Override
    public int hashCode() {
        return this.source.hashCode();
    }

    @Override
    public boolean equals(Object param0) {
        return this.source.equals(param0);
    }

    @Override
    public int compareTo(ByteBuf param0) {
        return this.source.compareTo(param0);
    }

    @Override
    public String toString() {
        return this.source.toString();
    }

    public FriendlyByteBuf retain(int param0) {
        this.source.retain(param0);
        return this;
    }

    public FriendlyByteBuf retain() {
        this.source.retain();
        return this;
    }

    public FriendlyByteBuf touch() {
        this.source.touch();
        return this;
    }

    public FriendlyByteBuf touch(Object param0) {
        this.source.touch(param0);
        return this;
    }

    @Override
    public int refCnt() {
        return this.source.refCnt();
    }

    @Override
    public boolean release() {
        return this.source.release();
    }

    @Override
    public boolean release(int param0) {
        return this.source.release(param0);
    }

    @FunctionalInterface
    public interface Reader<T> extends Function<FriendlyByteBuf, T> {
        default FriendlyByteBuf.Reader<Optional<T>> asOptional() {
            return param0 -> param0.readOptional(this);
        }
    }

    @FunctionalInterface
    public interface Writer<T> extends BiConsumer<FriendlyByteBuf, T> {
        default FriendlyByteBuf.Writer<Optional<T>> asOptional() {
            return (param0, param1) -> param0.writeOptional(param1, this);
        }
    }
}
