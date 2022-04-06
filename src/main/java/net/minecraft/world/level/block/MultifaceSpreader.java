package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class MultifaceSpreader {
    public static final MultifaceSpreader.SpreadType[] DEFAULT_SPREAD_ORDER = new MultifaceSpreader.SpreadType[]{
        MultifaceSpreader.SpreadType.SAME_POSITION, MultifaceSpreader.SpreadType.SAME_PLANE, MultifaceSpreader.SpreadType.WRAP_AROUND
    };
    private final MultifaceSpreader.SpreadConfig config;

    public MultifaceSpreader(MultifaceBlock param0) {
        this(new MultifaceSpreader.DefaultSpreaderConfig(param0));
    }

    public MultifaceSpreader(MultifaceSpreader.SpreadConfig param0) {
        this.config = param0;
    }

    public boolean canSpreadInAnyDirection(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return Direction.stream()
            .anyMatch(param4 -> this.getSpreadFromFaceTowardDirection(param0, param1, param2, param3, param4, this.config::canSpreadInto).isPresent());
    }

    public Optional<MultifaceSpreader.SpreadPos> spreadFromRandomFaceTowardRandomDirection(
        BlockState param0, LevelAccessor param1, BlockPos param2, RandomSource param3
    ) {
        return Direction.allShuffled(param3)
            .stream()
            .filter(param1x -> this.config.canSpreadFrom(param0, param1x))
            .map(param4 -> this.spreadFromFaceTowardRandomDirection(param0, param1, param2, param4, param3, false))
            .filter(Optional::isPresent)
            .findFirst()
            .orElse(Optional.empty());
    }

    public long spreadAll(BlockState param0, LevelAccessor param1, BlockPos param2, boolean param3) {
        return Direction.stream()
            .filter(param1x -> this.config.canSpreadFrom(param0, param1x))
            .map(param4 -> this.spreadFromFaceTowardAllDirections(param0, param1, param2, param4, param3))
            .reduce(0L, Long::sum);
    }

    public Optional<MultifaceSpreader.SpreadPos> spreadFromFaceTowardRandomDirection(
        BlockState param0, LevelAccessor param1, BlockPos param2, Direction param3, RandomSource param4, boolean param5
    ) {
        return Direction.allShuffled(param4)
            .stream()
            .map(param5x -> this.spreadFromFaceTowardDirection(param0, param1, param2, param3, param5x, param5))
            .filter(Optional::isPresent)
            .findFirst()
            .orElse(Optional.empty());
    }

    private long spreadFromFaceTowardAllDirections(BlockState param0, LevelAccessor param1, BlockPos param2, Direction param3, boolean param4) {
        return Direction.stream()
            .map(param5 -> this.spreadFromFaceTowardDirection(param0, param1, param2, param3, param5, param4))
            .filter(Optional::isPresent)
            .count();
    }

    @VisibleForTesting
    public Optional<MultifaceSpreader.SpreadPos> spreadFromFaceTowardDirection(
        BlockState param0, LevelAccessor param1, BlockPos param2, Direction param3, Direction param4, boolean param5
    ) {
        return this.getSpreadFromFaceTowardDirection(param0, param1, param2, param3, param4, this.config::canSpreadInto)
            .flatMap(param2x -> this.spreadToFace(param1, param2x, param5));
    }

    public Optional<MultifaceSpreader.SpreadPos> getSpreadFromFaceTowardDirection(
        BlockState param0, BlockGetter param1, BlockPos param2, Direction param3, Direction param4, MultifaceSpreader.SpreadPredicate param5
    ) {
        if (param4.getAxis() == param3.getAxis()) {
            return Optional.empty();
        } else if (this.config.isOtherBlockValidAsSource(param0) || this.config.hasFace(param0, param3) && !this.config.hasFace(param0, param4)) {
            for(MultifaceSpreader.SpreadType var0 : this.config.getSpreadTypes()) {
                MultifaceSpreader.SpreadPos var1 = var0.getSpreadPos(param2, param4, param3);
                if (param5.test(param1, param2, var1)) {
                    return Optional.of(var1);
                }
            }

            return Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    public Optional<MultifaceSpreader.SpreadPos> spreadToFace(LevelAccessor param0, MultifaceSpreader.SpreadPos param1, boolean param2) {
        BlockState var0 = param0.getBlockState(param1.pos());
        return this.config.placeBlock(param0, param1, var0, param2) ? Optional.of(param1) : Optional.empty();
    }

    public static class DefaultSpreaderConfig implements MultifaceSpreader.SpreadConfig {
        protected MultifaceBlock block;

        public DefaultSpreaderConfig(MultifaceBlock param0) {
            this.block = param0;
        }

        @Nullable
        @Override
        public BlockState getStateForPlacement(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
            return this.block.getStateForPlacement(param0, param1, param2, param3);
        }

        protected boolean stateCanBeReplaced(BlockGetter param0, BlockPos param1, BlockPos param2, Direction param3, BlockState param4) {
            return param4.isAir() || param4.is(this.block) || param4.is(Blocks.WATER) && param4.getFluidState().isSource();
        }

        @Override
        public boolean canSpreadInto(BlockGetter param0, BlockPos param1, MultifaceSpreader.SpreadPos param2) {
            BlockState var0 = param0.getBlockState(param2.pos());
            return this.stateCanBeReplaced(param0, param1, param2.pos(), param2.face(), var0)
                && this.block.isValidStateForPlacement(param0, var0, param2.pos(), param2.face());
        }
    }

    public interface SpreadConfig {
        @Nullable
        BlockState getStateForPlacement(BlockState var1, BlockGetter var2, BlockPos var3, Direction var4);

        boolean canSpreadInto(BlockGetter var1, BlockPos var2, MultifaceSpreader.SpreadPos var3);

        default MultifaceSpreader.SpreadType[] getSpreadTypes() {
            return MultifaceSpreader.DEFAULT_SPREAD_ORDER;
        }

        default boolean hasFace(BlockState param0, Direction param1) {
            return MultifaceBlock.hasFace(param0, param1);
        }

        default boolean isOtherBlockValidAsSource(BlockState param0) {
            return false;
        }

        default boolean canSpreadFrom(BlockState param0, Direction param1) {
            return this.isOtherBlockValidAsSource(param0) || this.hasFace(param0, param1);
        }

        default boolean placeBlock(LevelAccessor param0, MultifaceSpreader.SpreadPos param1, BlockState param2, boolean param3) {
            BlockState var0 = this.getStateForPlacement(param2, param0, param1.pos(), param1.face());
            if (var0 != null) {
                if (param3) {
                    param0.getChunk(param1.pos()).markPosForPostprocessing(param1.pos());
                }

                return param0.setBlock(param1.pos(), var0, 2);
            } else {
                return false;
            }
        }
    }

    public static record SpreadPos(BlockPos pos, Direction face) {
    }

    @FunctionalInterface
    public interface SpreadPredicate {
        boolean test(BlockGetter var1, BlockPos var2, MultifaceSpreader.SpreadPos var3);
    }

    public static enum SpreadType {
        SAME_POSITION {
            @Override
            public MultifaceSpreader.SpreadPos getSpreadPos(BlockPos param0, Direction param1, Direction param2) {
                return new MultifaceSpreader.SpreadPos(param0, param1);
            }
        },
        SAME_PLANE {
            @Override
            public MultifaceSpreader.SpreadPos getSpreadPos(BlockPos param0, Direction param1, Direction param2) {
                return new MultifaceSpreader.SpreadPos(param0.relative(param1), param2);
            }
        },
        WRAP_AROUND {
            @Override
            public MultifaceSpreader.SpreadPos getSpreadPos(BlockPos param0, Direction param1, Direction param2) {
                return new MultifaceSpreader.SpreadPos(param0.relative(param1).relative(param2), param1.getOpposite());
            }
        };

        public abstract MultifaceSpreader.SpreadPos getSpreadPos(BlockPos var1, Direction var2, Direction var3);
    }
}
