package net.minecraft.network.syncher;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class EntityDataSerializers {
    private static final CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> SERIALIZERS = CrudeIncrementalIntIdentityHashBiMap.create(16);
    public static final EntityDataSerializer<Byte> BYTE = EntityDataSerializer.simple((param0, param1) -> param0.writeByte(param1), FriendlyByteBuf::readByte);
    public static final EntityDataSerializer<Integer> INT = EntityDataSerializer.simple(FriendlyByteBuf::writeVarInt, FriendlyByteBuf::readVarInt);
    public static final EntityDataSerializer<Long> LONG = EntityDataSerializer.simple(FriendlyByteBuf::writeVarLong, FriendlyByteBuf::readVarLong);
    public static final EntityDataSerializer<Float> FLOAT = EntityDataSerializer.simple(FriendlyByteBuf::writeFloat, FriendlyByteBuf::readFloat);
    public static final EntityDataSerializer<String> STRING = EntityDataSerializer.simple(FriendlyByteBuf::writeUtf, FriendlyByteBuf::readUtf);
    public static final EntityDataSerializer<Component> COMPONENT = EntityDataSerializer.simple(FriendlyByteBuf::writeComponent, FriendlyByteBuf::readComponent);
    public static final EntityDataSerializer<Optional<Component>> OPTIONAL_COMPONENT = EntityDataSerializer.optional(
        FriendlyByteBuf::writeComponent, FriendlyByteBuf::readComponent
    );
    public static final EntityDataSerializer<ItemStack> ITEM_STACK = new EntityDataSerializer<ItemStack>() {
        public void write(FriendlyByteBuf param0, ItemStack param1) {
            param0.writeItem(param1);
        }

        public ItemStack read(FriendlyByteBuf param0) {
            return param0.readItem();
        }

        public ItemStack copy(ItemStack param0) {
            return param0.copy();
        }
    };
    public static final EntityDataSerializer<Optional<BlockState>> BLOCK_STATE = new EntityDataSerializer.ForValueType<Optional<BlockState>>() {
        public void write(FriendlyByteBuf param0, Optional<BlockState> param1) {
            if (param1.isPresent()) {
                param0.writeVarInt(Block.getId(param1.get()));
            } else {
                param0.writeVarInt(0);
            }

        }

        public Optional<BlockState> read(FriendlyByteBuf param0) {
            int var0 = param0.readVarInt();
            return var0 == 0 ? Optional.empty() : Optional.of(Block.stateById(var0));
        }
    };
    public static final EntityDataSerializer<Boolean> BOOLEAN = EntityDataSerializer.simple(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean);
    public static final EntityDataSerializer<ParticleOptions> PARTICLE = new EntityDataSerializer.ForValueType<ParticleOptions>() {
        public void write(FriendlyByteBuf param0, ParticleOptions param1) {
            param0.writeId(BuiltInRegistries.PARTICLE_TYPE, param1.getType());
            param1.writeToNetwork(param0);
        }

        public ParticleOptions read(FriendlyByteBuf param0) {
            return this.readParticle(param0, param0.readById(BuiltInRegistries.PARTICLE_TYPE));
        }

        private <T extends ParticleOptions> T readParticle(FriendlyByteBuf param0, ParticleType<T> param1) {
            return param1.getDeserializer().fromNetwork(param1, param0);
        }
    };
    public static final EntityDataSerializer<Rotations> ROTATIONS = new EntityDataSerializer.ForValueType<Rotations>() {
        public void write(FriendlyByteBuf param0, Rotations param1) {
            param0.writeFloat(param1.getX());
            param0.writeFloat(param1.getY());
            param0.writeFloat(param1.getZ());
        }

        public Rotations read(FriendlyByteBuf param0) {
            return new Rotations(param0.readFloat(), param0.readFloat(), param0.readFloat());
        }
    };
    public static final EntityDataSerializer<BlockPos> BLOCK_POS = EntityDataSerializer.simple(FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::readBlockPos);
    public static final EntityDataSerializer<Optional<BlockPos>> OPTIONAL_BLOCK_POS = EntityDataSerializer.optional(
        FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::readBlockPos
    );
    public static final EntityDataSerializer<Direction> DIRECTION = EntityDataSerializer.simpleEnum(Direction.class);
    public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID = EntityDataSerializer.optional(
        FriendlyByteBuf::writeUUID, FriendlyByteBuf::readUUID
    );
    public static final EntityDataSerializer<Optional<GlobalPos>> OPTIONAL_GLOBAL_POS = EntityDataSerializer.optional(
        FriendlyByteBuf::writeGlobalPos, FriendlyByteBuf::readGlobalPos
    );
    public static final EntityDataSerializer<CompoundTag> COMPOUND_TAG = new EntityDataSerializer<CompoundTag>() {
        public void write(FriendlyByteBuf param0, CompoundTag param1) {
            param0.writeNbt(param1);
        }

        public CompoundTag read(FriendlyByteBuf param0) {
            return param0.readNbt();
        }

        public CompoundTag copy(CompoundTag param0) {
            return param0.copy();
        }
    };
    public static final EntityDataSerializer<VillagerData> VILLAGER_DATA = new EntityDataSerializer.ForValueType<VillagerData>() {
        public void write(FriendlyByteBuf param0, VillagerData param1) {
            param0.writeId(BuiltInRegistries.VILLAGER_TYPE, param1.getType());
            param0.writeId(BuiltInRegistries.VILLAGER_PROFESSION, param1.getProfession());
            param0.writeVarInt(param1.getLevel());
        }

        public VillagerData read(FriendlyByteBuf param0) {
            return new VillagerData(
                param0.readById(BuiltInRegistries.VILLAGER_TYPE), param0.readById(BuiltInRegistries.VILLAGER_PROFESSION), param0.readVarInt()
            );
        }
    };
    public static final EntityDataSerializer<OptionalInt> OPTIONAL_UNSIGNED_INT = new EntityDataSerializer.ForValueType<OptionalInt>() {
        public void write(FriendlyByteBuf param0, OptionalInt param1) {
            param0.writeVarInt(param1.orElse(-1) + 1);
        }

        public OptionalInt read(FriendlyByteBuf param0) {
            int var0 = param0.readVarInt();
            return var0 == 0 ? OptionalInt.empty() : OptionalInt.of(var0 - 1);
        }
    };
    public static final EntityDataSerializer<Pose> POSE = EntityDataSerializer.simpleEnum(Pose.class);
    public static final EntityDataSerializer<CatVariant> CAT_VARIANT = EntityDataSerializer.simpleId(BuiltInRegistries.CAT_VARIANT);
    public static final EntityDataSerializer<FrogVariant> FROG_VARIANT = EntityDataSerializer.simpleId(BuiltInRegistries.FROG_VARIANT);
    public static final EntityDataSerializer<Holder<PaintingVariant>> PAINTING_VARIANT = EntityDataSerializer.simpleId(
        BuiltInRegistries.PAINTING_VARIANT.asHolderIdMap()
    );

    public static void registerSerializer(EntityDataSerializer<?> param0) {
        SERIALIZERS.add(param0);
    }

    @Nullable
    public static EntityDataSerializer<?> getSerializer(int param0) {
        return SERIALIZERS.byId(param0);
    }

    public static int getSerializedId(EntityDataSerializer<?> param0) {
        return SERIALIZERS.getId(param0);
    }

    private EntityDataSerializers() {
    }

    static {
        registerSerializer(BYTE);
        registerSerializer(INT);
        registerSerializer(LONG);
        registerSerializer(FLOAT);
        registerSerializer(STRING);
        registerSerializer(COMPONENT);
        registerSerializer(OPTIONAL_COMPONENT);
        registerSerializer(ITEM_STACK);
        registerSerializer(BOOLEAN);
        registerSerializer(ROTATIONS);
        registerSerializer(BLOCK_POS);
        registerSerializer(OPTIONAL_BLOCK_POS);
        registerSerializer(DIRECTION);
        registerSerializer(OPTIONAL_UUID);
        registerSerializer(BLOCK_STATE);
        registerSerializer(COMPOUND_TAG);
        registerSerializer(PARTICLE);
        registerSerializer(VILLAGER_DATA);
        registerSerializer(OPTIONAL_UNSIGNED_INT);
        registerSerializer(POSE);
        registerSerializer(CAT_VARIANT);
        registerSerializer(FROG_VARIANT);
        registerSerializer(OPTIONAL_GLOBAL_POS);
        registerSerializer(PAINTING_VARIANT);
    }
}
