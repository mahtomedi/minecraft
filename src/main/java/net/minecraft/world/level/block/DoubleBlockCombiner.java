package net.minecraft.world.level.block;

import java.util.function.BiPredicate;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class DoubleBlockCombiner {
    public static <S extends BlockEntity> DoubleBlockCombiner.NeighborCombineResult<S> combineWithNeigbour(
        BlockEntityType<S> param0,
        Function<BlockState, DoubleBlockCombiner.BlockType> param1,
        Function<BlockState, Direction> param2,
        DirectionProperty param3,
        BlockState param4,
        LevelAccessor param5,
        BlockPos param6,
        BiPredicate<LevelAccessor, BlockPos> param7
    ) {
        S var0 = param0.getBlockEntity(param5, param6);
        if (var0 == null) {
            return DoubleBlockCombiner.Combiner::acceptNone;
        } else if (param7.test(param5, param6)) {
            return DoubleBlockCombiner.Combiner::acceptNone;
        } else {
            DoubleBlockCombiner.BlockType var1 = param1.apply(param4);
            boolean var2 = var1 == DoubleBlockCombiner.BlockType.SINGLE;
            boolean var3 = var1 == DoubleBlockCombiner.BlockType.FIRST;
            if (var2) {
                return new DoubleBlockCombiner.NeighborCombineResult.Single<>(var0);
            } else {
                BlockPos var4 = param6.relative(param2.apply(param4));
                BlockState var5 = param5.getBlockState(var4);
                if (var5.getBlock() == param4.getBlock()) {
                    DoubleBlockCombiner.BlockType var6 = param1.apply(var5);
                    if (var6 != DoubleBlockCombiner.BlockType.SINGLE && var1 != var6 && var5.getValue(param3) == param4.getValue(param3)) {
                        if (param7.test(param5, var4)) {
                            return DoubleBlockCombiner.Combiner::acceptNone;
                        }

                        S var7 = param0.getBlockEntity(param5, var4);
                        if (var7 != null) {
                            S var8 = var3 ? var0 : var7;
                            S var9 = var3 ? var7 : var0;
                            return new DoubleBlockCombiner.NeighborCombineResult.Double<>(var8, var9);
                        }
                    }
                }

                return new DoubleBlockCombiner.NeighborCombineResult.Single<>(var0);
            }
        }
    }

    public static enum BlockType {
        SINGLE,
        FIRST,
        SECOND;
    }

    public interface Combiner<S, T> {
        T acceptDouble(S var1, S var2);

        T acceptSingle(S var1);

        T acceptNone();
    }

    public interface NeighborCombineResult<S> {
        <T> T apply(DoubleBlockCombiner.Combiner<? super S, T> var1);

        public static final class Double<S> implements DoubleBlockCombiner.NeighborCombineResult<S> {
            private final S first;
            private final S second;

            public Double(S param0, S param1) {
                this.first = param0;
                this.second = param1;
            }

            @Override
            public <T> T apply(DoubleBlockCombiner.Combiner<? super S, T> param0) {
                return param0.acceptDouble(this.first, this.second);
            }
        }

        public static final class Single<S> implements DoubleBlockCombiner.NeighborCombineResult<S> {
            private final S single;

            public Single(S param0) {
                this.single = param0;
            }

            @Override
            public <T> T apply(DoubleBlockCombiner.Combiner<? super S, T> param0) {
                return param0.acceptSingle(this.single);
            }
        }
    }
}
