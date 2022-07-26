package net.minecraft.network;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
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
import java.nio.charset.StandardCharsets;
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
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class FriendlyByteBuf extends ByteBuf {
    private static final int MAX_VARINT_SIZE = 5;
    private static final int MAX_VARLONG_SIZE = 10;
    public static final int DEFAULT_NBT_QUOTA = 2097152;
    private final ByteBuf source;
    public static final short MAX_STRING_LENGTH = 32767;
    public static final int MAX_COMPONENT_STRING_LENGTH = 262144;
    private static final int PUBLIC_KEY_SIZE = 256;
    private static final int MAX_PUBLIC_KEY_HEADER_SIZE = 256;
    private static final int MAX_PUBLIC_KEY_LENGTH = 512;

    public FriendlyByteBuf(ByteBuf param0) {
        this.source = param0;
    }

    public static int getVarIntSize(int param0) {
        for(int var0 = 1; var0 < 5; ++var0) {
            if ((param0 & -1 << var0 * 7) == 0) {
                return var0;
            }
        }

        return 5;
    }

    public static int getVarLongSize(long param0) {
        for(int var0 = 1; var0 < 10; ++var0) {
            if ((param0 & -1L << var0 * 7) == 0L) {
                return var0;
            }
        }

        return 10;
    }

    @Deprecated
    public <T> T readWithCodec(Codec<T> param0) {
        CompoundTag var0 = this.readAnySizeNbt();
        return Util.getOrThrow(param0.parse(NbtOps.INSTANCE, var0), param1 -> new DecoderException("Failed to decode: " + param1 + " " + var0));
    }

    @Deprecated
    public <T> void writeWithCodec(Codec<T> param0, T param1) {
        Tag var0 = Util.getOrThrow(param0.encodeStart(NbtOps.INSTANCE, param1), param1x -> new EncoderException("Failed to encode: " + param1x + " " + param1));
        this.writeNbt((CompoundTag)var0);
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

    @VisibleForTesting
    public byte[] accessByteBufWithCorrectSize() {
        int var0 = this.writerIndex();
        byte[] var1 = new byte[var0];
        this.getBytes(0, var1);
        return var1;
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

    public int readVarInt() {
        int var0 = 0;
        int var1 = 0;

        byte var2;
        do {
            var2 = this.readByte();
            var0 |= (var2 & 127) << var1++ * 7;
            if (var1 > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while((var2 & 128) == 128);

        return var0;
    }

    public long readVarLong() {
        long var0 = 0L;
        int var1 = 0;

        byte var2;
        do {
            var2 = this.readByte();
            var0 |= (long)(var2 & 127) << var1++ * 7;
            if (var1 > 10) {
                throw new RuntimeException("VarLong too big");
            }
        } while((var2 & 128) == 128);

        return var0;
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
        while((param0x & -128) != 0) {
            this.writeByte(param0x & 127 | 128);
            param0x >>>= 7;
        }

        this.writeByte(param0x);
        return this;
    }

    public FriendlyByteBuf writeVarLong(long param0) {
        while((param0 & -128L) != 0L) {
            this.writeByte((int)(param0 & 127L) | 128);
            param0 >>>= 7;
        }

        this.writeByte((int)param0);
        return this;
    }

    public FriendlyByteBuf writeNbt(@Nullable CompoundTag param0) {
        if (param0 == null) {
            this.writeByte(0);
        } else {
            try {
                NbtIo.write(param0, new ByteBufOutputStream(this));
            } catch (IOException var3) {
                throw new EncoderException(var3);
            }
        }

        return this;
    }

    @Nullable
    public CompoundTag readNbt() {
        return this.readNbt(new NbtAccounter(2097152L));
    }

    @Nullable
    public CompoundTag readAnySizeNbt() {
        return this.readNbt(NbtAccounter.UNLIMITED);
    }

    @Nullable
    public CompoundTag readNbt(NbtAccounter param0) {
        int var0 = this.readerIndex();
        byte var1 = this.readByte();
        if (var1 == 0) {
            return null;
        } else {
            this.readerIndex(var0);

            try {
                return NbtIo.read(new ByteBufInputStream(this), param0);
            } catch (IOException var5) {
                throw new EncoderException(var5);
            }
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
        int var0 = getMaxEncodedUtfLength(param0);
        int var1 = this.readVarInt();
        if (var1 > var0) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + var1 + " > " + var0 + ")");
        } else if (var1 < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            String var2 = this.toString(this.readerIndex(), var1, StandardCharsets.UTF_8);
            this.readerIndex(this.readerIndex() + var1);
            if (var2.length() > param0) {
                throw new DecoderException("The received string length is longer than maximum allowed (" + var2.length() + " > " + param0 + ")");
            } else {
                return var2;
            }
        }
    }

    public FriendlyByteBuf writeUtf(String param0) {
        return this.writeUtf(param0, 32767);
    }

    public FriendlyByteBuf writeUtf(String param0, int param1) {
        if (param0.length() > param1) {
            throw new EncoderException("String too big (was " + param0.length() + " characters, max " + param1 + ")");
        } else {
            byte[] var0 = param0.getBytes(StandardCharsets.UTF_8);
            int var1 = getMaxEncodedUtfLength(param1);
            if (var0.length > var1) {
                throw new EncoderException("String too big (was " + var0.length + " bytes encoded, max " + var1 + ")");
            } else {
                this.writeVarInt(var0.length);
                this.writeBytes(var0);
                return this;
            }
        }
    }

    private static int getMaxEncodedUtfLength(int param0) {
        return param0 * 3;
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
            var0.put(var0x.getName(), var0x);
        });
        return var0;
    }

    public void writeGameProfileProperties(PropertyMap param0) {
        this.writeCollection(param0.values(), FriendlyByteBuf::writeProperty);
    }

    public Property readProperty() {
        String var0 = this.readUtf();
        String var1 = this.readUtf();
        if (this.readBoolean()) {
            String var2 = this.readUtf();
            return new Property(var0, var1, var2);
        } else {
            return new Property(var0, var1);
        }
    }

    public void writeProperty(Property param0x) {
        this.writeUtf(param0x.getName());
        this.writeUtf(param0x.getValue());
        if (param0x.hasSignature()) {
            this.writeBoolean(true);
            this.writeUtf(param0x.getSignature());
        } else {
            this.writeBoolean(false);
        }

    }

    @Override
    public int capacity() {
        return this.source.capacity();
    }

    @Override
    public ByteBuf capacity(int param0) {
        return this.source.capacity(param0);
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
        return this.source.unwrap();
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

    @Override
    public ByteBuf readerIndex(int param0) {
        return this.source.readerIndex(param0);
    }

    @Override
    public int writerIndex() {
        return this.source.writerIndex();
    }

    @Override
    public ByteBuf writerIndex(int param0) {
        return this.source.writerIndex(param0);
    }

    @Override
    public ByteBuf setIndex(int param0, int param1) {
        return this.source.setIndex(param0, param1);
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

    @Override
    public ByteBuf clear() {
        return this.source.clear();
    }

    @Override
    public ByteBuf markReaderIndex() {
        return this.source.markReaderIndex();
    }

    @Override
    public ByteBuf resetReaderIndex() {
        return this.source.resetReaderIndex();
    }

    @Override
    public ByteBuf markWriterIndex() {
        return this.source.markWriterIndex();
    }

    @Override
    public ByteBuf resetWriterIndex() {
        return this.source.resetWriterIndex();
    }

    @Override
    public ByteBuf discardReadBytes() {
        return this.source.discardReadBytes();
    }

    @Override
    public ByteBuf discardSomeReadBytes() {
        return this.source.discardSomeReadBytes();
    }

    @Override
    public ByteBuf ensureWritable(int param0) {
        return this.source.ensureWritable(param0);
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

    @Override
    public ByteBuf getBytes(int param0, ByteBuf param1) {
        return this.source.getBytes(param0, param1);
    }

    @Override
    public ByteBuf getBytes(int param0, ByteBuf param1, int param2) {
        return this.source.getBytes(param0, param1, param2);
    }

    @Override
    public ByteBuf getBytes(int param0, ByteBuf param1, int param2, int param3) {
        return this.source.getBytes(param0, param1, param2, param3);
    }

    @Override
    public ByteBuf getBytes(int param0, byte[] param1) {
        return this.source.getBytes(param0, param1);
    }

    @Override
    public ByteBuf getBytes(int param0, byte[] param1, int param2, int param3) {
        return this.source.getBytes(param0, param1, param2, param3);
    }

    @Override
    public ByteBuf getBytes(int param0, ByteBuffer param1) {
        return this.source.getBytes(param0, param1);
    }

    @Override
    public ByteBuf getBytes(int param0, OutputStream param1, int param2) throws IOException {
        return this.source.getBytes(param0, param1, param2);
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

    @Override
    public ByteBuf setBoolean(int param0, boolean param1) {
        return this.source.setBoolean(param0, param1);
    }

    @Override
    public ByteBuf setByte(int param0, int param1) {
        return this.source.setByte(param0, param1);
    }

    @Override
    public ByteBuf setShort(int param0, int param1) {
        return this.source.setShort(param0, param1);
    }

    @Override
    public ByteBuf setShortLE(int param0, int param1) {
        return this.source.setShortLE(param0, param1);
    }

    @Override
    public ByteBuf setMedium(int param0, int param1) {
        return this.source.setMedium(param0, param1);
    }

    @Override
    public ByteBuf setMediumLE(int param0, int param1) {
        return this.source.setMediumLE(param0, param1);
    }

    @Override
    public ByteBuf setInt(int param0, int param1) {
        return this.source.setInt(param0, param1);
    }

    @Override
    public ByteBuf setIntLE(int param0, int param1) {
        return this.source.setIntLE(param0, param1);
    }

    @Override
    public ByteBuf setLong(int param0, long param1) {
        return this.source.setLong(param0, param1);
    }

    @Override
    public ByteBuf setLongLE(int param0, long param1) {
        return this.source.setLongLE(param0, param1);
    }

    @Override
    public ByteBuf setChar(int param0, int param1) {
        return this.source.setChar(param0, param1);
    }

    @Override
    public ByteBuf setFloat(int param0, float param1) {
        return this.source.setFloat(param0, param1);
    }

    @Override
    public ByteBuf setDouble(int param0, double param1) {
        return this.source.setDouble(param0, param1);
    }

    @Override
    public ByteBuf setBytes(int param0, ByteBuf param1) {
        return this.source.setBytes(param0, param1);
    }

    @Override
    public ByteBuf setBytes(int param0, ByteBuf param1, int param2) {
        return this.source.setBytes(param0, param1, param2);
    }

    @Override
    public ByteBuf setBytes(int param0, ByteBuf param1, int param2, int param3) {
        return this.source.setBytes(param0, param1, param2, param3);
    }

    @Override
    public ByteBuf setBytes(int param0, byte[] param1) {
        return this.source.setBytes(param0, param1);
    }

    @Override
    public ByteBuf setBytes(int param0, byte[] param1, int param2, int param3) {
        return this.source.setBytes(param0, param1, param2, param3);
    }

    @Override
    public ByteBuf setBytes(int param0, ByteBuffer param1) {
        return this.source.setBytes(param0, param1);
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

    @Override
    public ByteBuf setZero(int param0, int param1) {
        return this.source.setZero(param0, param1);
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

    @Override
    public ByteBuf readBytes(ByteBuf param0) {
        return this.source.readBytes(param0);
    }

    @Override
    public ByteBuf readBytes(ByteBuf param0, int param1) {
        return this.source.readBytes(param0, param1);
    }

    @Override
    public ByteBuf readBytes(ByteBuf param0, int param1, int param2) {
        return this.source.readBytes(param0, param1, param2);
    }

    @Override
    public ByteBuf readBytes(byte[] param0) {
        return this.source.readBytes(param0);
    }

    @Override
    public ByteBuf readBytes(byte[] param0, int param1, int param2) {
        return this.source.readBytes(param0, param1, param2);
    }

    @Override
    public ByteBuf readBytes(ByteBuffer param0) {
        return this.source.readBytes(param0);
    }

    @Override
    public ByteBuf readBytes(OutputStream param0, int param1) throws IOException {
        return this.source.readBytes(param0, param1);
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

    @Override
    public ByteBuf skipBytes(int param0) {
        return this.source.skipBytes(param0);
    }

    @Override
    public ByteBuf writeBoolean(boolean param0) {
        return this.source.writeBoolean(param0);
    }

    @Override
    public ByteBuf writeByte(int param0) {
        return this.source.writeByte(param0);
    }

    @Override
    public ByteBuf writeShort(int param0) {
        return this.source.writeShort(param0);
    }

    @Override
    public ByteBuf writeShortLE(int param0) {
        return this.source.writeShortLE(param0);
    }

    @Override
    public ByteBuf writeMedium(int param0) {
        return this.source.writeMedium(param0);
    }

    @Override
    public ByteBuf writeMediumLE(int param0) {
        return this.source.writeMediumLE(param0);
    }

    @Override
    public ByteBuf writeInt(int param0) {
        return this.source.writeInt(param0);
    }

    @Override
    public ByteBuf writeIntLE(int param0) {
        return this.source.writeIntLE(param0);
    }

    @Override
    public ByteBuf writeLong(long param0) {
        return this.source.writeLong(param0);
    }

    @Override
    public ByteBuf writeLongLE(long param0) {
        return this.source.writeLongLE(param0);
    }

    @Override
    public ByteBuf writeChar(int param0) {
        return this.source.writeChar(param0);
    }

    @Override
    public ByteBuf writeFloat(float param0) {
        return this.source.writeFloat(param0);
    }

    @Override
    public ByteBuf writeDouble(double param0) {
        return this.source.writeDouble(param0);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf param0) {
        return this.source.writeBytes(param0);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf param0, int param1) {
        return this.source.writeBytes(param0, param1);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf param0, int param1, int param2) {
        return this.source.writeBytes(param0, param1, param2);
    }

    @Override
    public ByteBuf writeBytes(byte[] param0) {
        return this.source.writeBytes(param0);
    }

    @Override
    public ByteBuf writeBytes(byte[] param0, int param1, int param2) {
        return this.source.writeBytes(param0, param1, param2);
    }

    @Override
    public ByteBuf writeBytes(ByteBuffer param0) {
        return this.source.writeBytes(param0);
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

    @Override
    public ByteBuf writeZero(int param0) {
        return this.source.writeZero(param0);
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

    @Override
    public ByteBuf retain(int param0) {
        return this.source.retain(param0);
    }

    @Override
    public ByteBuf retain() {
        return this.source.retain();
    }

    @Override
    public ByteBuf touch() {
        return this.source.touch();
    }

    @Override
    public ByteBuf touch(Object param0) {
        return this.source.touch(param0);
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
