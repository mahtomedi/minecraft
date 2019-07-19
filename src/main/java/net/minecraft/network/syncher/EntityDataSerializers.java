package net.minecraft.network.syncher;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class EntityDataSerializers {
    private static final CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> SERIALIZERS = new CrudeIncrementalIntIdentityHashBiMap<>(16);
    public static final EntityDataSerializer<Byte> BYTE = new EntityDataSerializer<Byte>() {
        public void write(FriendlyByteBuf param0, Byte param1) {
            param0.writeByte(param1);
        }

        public Byte read(FriendlyByteBuf param0) {
            return param0.readByte();
        }

        public Byte copy(Byte param0) {
            return param0;
        }
    };
    public static final EntityDataSerializer<Integer> INT = new EntityDataSerializer<Integer>() {
        public void write(FriendlyByteBuf param0, Integer param1) {
            param0.writeVarInt(param1);
        }

        public Integer read(FriendlyByteBuf param0) {
            return param0.readVarInt();
        }

        public Integer copy(Integer param0) {
            return param0;
        }
    };
    public static final EntityDataSerializer<Float> FLOAT = new EntityDataSerializer<Float>() {
        public void write(FriendlyByteBuf param0, Float param1) {
            param0.writeFloat(param1);
        }

        public Float read(FriendlyByteBuf param0) {
            return param0.readFloat();
        }

        public Float copy(Float param0) {
            return param0;
        }
    };
    public static final EntityDataSerializer<String> STRING = new EntityDataSerializer<String>() {
        public void write(FriendlyByteBuf param0, String param1) {
            param0.writeUtf(param1);
        }

        public String read(FriendlyByteBuf param0) {
            return param0.readUtf(32767);
        }

        public String copy(String param0) {
            return param0;
        }
    };
    public static final EntityDataSerializer<Component> COMPONENT = new EntityDataSerializer<Component>() {
        public void write(FriendlyByteBuf param0, Component param1) {
            param0.writeComponent(param1);
        }

        public Component read(FriendlyByteBuf param0) {
            return param0.readComponent();
        }

        public Component copy(Component param0) {
            return param0.deepCopy();
        }
    };
    public static final EntityDataSerializer<Optional<Component>> OPTIONAL_COMPONENT = new EntityDataSerializer<Optional<Component>>() {
        public void write(FriendlyByteBuf param0, Optional<Component> param1) {
            if (param1.isPresent()) {
                param0.writeBoolean(true);
                param0.writeComponent(param1.get());
            } else {
                param0.writeBoolean(false);
            }

        }

        public Optional<Component> read(FriendlyByteBuf param0) {
            return param0.readBoolean() ? Optional.of(param0.readComponent()) : Optional.empty();
        }

        public Optional<Component> copy(Optional<Component> param0) {
            return param0.isPresent() ? Optional.of(param0.get().deepCopy()) : Optional.empty();
        }
    };
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
    public static final EntityDataSerializer<Optional<BlockState>> BLOCK_STATE = new EntityDataSerializer<Optional<BlockState>>() {
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

        public Optional<BlockState> copy(Optional<BlockState> param0) {
            return param0;
        }
    };
    public static final EntityDataSerializer<Boolean> BOOLEAN = new EntityDataSerializer<Boolean>() {
        public void write(FriendlyByteBuf param0, Boolean param1) {
            param0.writeBoolean(param1);
        }

        public Boolean read(FriendlyByteBuf param0) {
            return param0.readBoolean();
        }

        public Boolean copy(Boolean param0) {
            return param0;
        }
    };
    public static final EntityDataSerializer<ParticleOptions> PARTICLE = new EntityDataSerializer<ParticleOptions>() {
        public void write(FriendlyByteBuf param0, ParticleOptions param1) {
            param0.writeVarInt(Registry.PARTICLE_TYPE.getId(param1.getType()));
            param1.writeToNetwork(param0);
        }

        public ParticleOptions read(FriendlyByteBuf param0) {
            return this.readParticle(param0, Registry.PARTICLE_TYPE.byId(param0.readVarInt()));
        }

        private <T extends ParticleOptions> T readParticle(FriendlyByteBuf param0, ParticleType<T> param1) {
            return param1.getDeserializer().fromNetwork(param1, param0);
        }

        public ParticleOptions copy(ParticleOptions param0) {
            return param0;
        }
    };
    public static final EntityDataSerializer<Rotations> ROTATIONS = new EntityDataSerializer<Rotations>() {
        public void write(FriendlyByteBuf param0, Rotations param1) {
            param0.writeFloat(param1.getX());
            param0.writeFloat(param1.getY());
            param0.writeFloat(param1.getZ());
        }

        public Rotations read(FriendlyByteBuf param0) {
            return new Rotations(param0.readFloat(), param0.readFloat(), param0.readFloat());
        }

        public Rotations copy(Rotations param0) {
            return param0;
        }
    };
    public static final EntityDataSerializer<BlockPos> BLOCK_POS = new EntityDataSerializer<BlockPos>() {
        public void write(FriendlyByteBuf param0, BlockPos param1) {
            param0.writeBlockPos(param1);
        }

        public BlockPos read(FriendlyByteBuf param0) {
            return param0.readBlockPos();
        }

        public BlockPos copy(BlockPos param0) {
            return param0;
        }
    };
    public static final EntityDataSerializer<Optional<BlockPos>> OPTIONAL_BLOCK_POS = new EntityDataSerializer<Optional<BlockPos>>() {
        public void write(FriendlyByteBuf param0, Optional<BlockPos> param1) {
            param0.writeBoolean(param1.isPresent());
            if (param1.isPresent()) {
                param0.writeBlockPos(param1.get());
            }

        }

        public Optional<BlockPos> read(FriendlyByteBuf param0) {
            return !param0.readBoolean() ? Optional.empty() : Optional.of(param0.readBlockPos());
        }

        public Optional<BlockPos> copy(Optional<BlockPos> param0) {
            return param0;
        }
    };
    public static final EntityDataSerializer<Direction> DIRECTION = new EntityDataSerializer<Direction>() {
        public void write(FriendlyByteBuf param0, Direction param1) {
            param0.writeEnum(param1);
        }

        public Direction read(FriendlyByteBuf param0) {
            return param0.readEnum(Direction.class);
        }

        public Direction copy(Direction param0) {
            return param0;
        }
    };
    public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID = new EntityDataSerializer<Optional<UUID>>() {
        public void write(FriendlyByteBuf param0, Optional<UUID> param1) {
            param0.writeBoolean(param1.isPresent());
            if (param1.isPresent()) {
                param0.writeUUID(param1.get());
            }

        }

        public Optional<UUID> read(FriendlyByteBuf param0) {
            return !param0.readBoolean() ? Optional.empty() : Optional.of(param0.readUUID());
        }

        public Optional<UUID> copy(Optional<UUID> param0) {
            return param0;
        }
    };
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
    public static final EntityDataSerializer<VillagerData> VILLAGER_DATA = new EntityDataSerializer<VillagerData>() {
        public void write(FriendlyByteBuf param0, VillagerData param1) {
            param0.writeVarInt(Registry.VILLAGER_TYPE.getId(param1.getType()));
            param0.writeVarInt(Registry.VILLAGER_PROFESSION.getId(param1.getProfession()));
            param0.writeVarInt(param1.getLevel());
        }

        public VillagerData read(FriendlyByteBuf param0) {
            return new VillagerData(
                Registry.VILLAGER_TYPE.byId(param0.readVarInt()), Registry.VILLAGER_PROFESSION.byId(param0.readVarInt()), param0.readVarInt()
            );
        }

        public VillagerData copy(VillagerData param0) {
            return param0;
        }
    };
    public static final EntityDataSerializer<OptionalInt> OPTIONAL_UNSIGNED_INT = new EntityDataSerializer<OptionalInt>() {
        public void write(FriendlyByteBuf param0, OptionalInt param1) {
            param0.writeVarInt(param1.orElse(-1) + 1);
        }

        public OptionalInt read(FriendlyByteBuf param0) {
            int var0 = param0.readVarInt();
            return var0 == 0 ? OptionalInt.empty() : OptionalInt.of(var0 - 1);
        }

        public OptionalInt copy(OptionalInt param0) {
            return param0;
        }
    };
    public static final EntityDataSerializer<Pose> POSE = new EntityDataSerializer<Pose>() {
        public void write(FriendlyByteBuf param0, Pose param1) {
            param0.writeEnum(param1);
        }

        public Pose read(FriendlyByteBuf param0) {
            return param0.readEnum(Pose.class);
        }

        public Pose copy(Pose param0) {
            return param0;
        }
    };

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

    static {
        registerSerializer(BYTE);
        registerSerializer(INT);
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
    }
}
