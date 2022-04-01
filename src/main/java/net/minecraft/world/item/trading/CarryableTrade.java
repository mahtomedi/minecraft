package net.minecraft.world.item.trading;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;

public interface CarryableTrade {
    Codec<CarryableTrade> CODEC = new Codec<CarryableTrade>() {
        @Override
        public <T> DataResult<Pair<CarryableTrade, T>> decode(DynamicOps<T> param0, T param1) {
            DataResult<Pair<CarryableTrade, T>> var0 = CarryableTrade.Block.CODEC.decode(param0, param1).map(param0x -> param0x.mapFirst(Function.identity()));
            DataResult<Pair<CarryableTrade, T>> var1 = CarryableTrade.Entity.CODEC.decode(param0, param1).map(param0x -> param0x.mapFirst(Function.identity()));
            return var0.result().isPresent() ? var0 : var1;
        }

        public <T> DataResult<T> encode(CarryableTrade param0, DynamicOps<T> param1, T param2) {
            return CarryableTrade.encode(param0, param1);
        }
    };

    static CarryableTrade.Block block(net.minecraft.world.level.block.Block param0) {
        return new CarryableTrade.Block(param0);
    }

    static CarryableTrade.Entity entity(EntityType<?> param0) {
        return new CarryableTrade.Entity(param0);
    }

    void giveToPlayer(ServerPlayer var1);

    boolean matches(CarryableTrade var1);

    default ItemStack asItemStack() {
        return ItemStack.EMPTY;
    }

    Codec<? extends CarryableTrade> getCodec();

    static <T, C extends CarryableTrade> DataResult<T> encode(C param0, DynamicOps<T> param1) {
        Codec<C> var0 = param0.getCodec();
        return var0.encodeStart(param1, param0);
    }

    public static record Block(net.minecraft.world.level.block.Block block) implements CarryableTrade {
        public static final Codec<CarryableTrade.Block> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(Registry.BLOCK.byNameCodec().fieldOf("block").forGetter(CarryableTrade.Block::block))
                    .apply(param0, CarryableTrade.Block::new)
        );

        @Override
        public void giveToPlayer(ServerPlayer param0) {
            param0.setCarriedBlock(this.block.defaultBlockState());
        }

        @Override
        public boolean matches(CarryableTrade param0) {
            if (param0 instanceof CarryableTrade.Block var0) {
                return var0.block() == this.block;
            } else {
                return false;
            }
        }

        @Override
        public ItemStack asItemStack() {
            return new ItemStack(this.block);
        }

        @Override
        public Codec<? extends CarryableTrade> getCodec() {
            return CODEC;
        }
    }

    public static record Entity(EntityType<?> entity) implements CarryableTrade {
        public static final Codec<CarryableTrade.Entity> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(Registry.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(CarryableTrade.Entity::entity))
                    .apply(param0, CarryableTrade.Entity::new)
        );

        @Override
        public void giveToPlayer(ServerPlayer param0) {
            ServerLevel var0 = param0.getLevel();
            net.minecraft.world.entity.Entity var1 = this.entity.spawn(var0, null, param0, param0.blockPosition(), MobSpawnType.SPAWNER, false, false);
            if (var1 != null) {
                param0.startCarryingEntity(var1);
            }

        }

        @Override
        public boolean matches(CarryableTrade param0) {
            if (param0 instanceof CarryableTrade.Entity var0) {
                return var0.entity() == this.entity;
            } else {
                return false;
            }
        }

        @Override
        public Codec<? extends CarryableTrade> getCodec() {
            return CODEC;
        }
    }
}
