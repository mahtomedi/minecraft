package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface SignalGetter extends BlockGetter {
    Direction[] DIRECTIONS = Direction.values();

    default int getDirectSignal(BlockPos param0, Direction param1) {
        return this.getBlockState(param0).getDirectSignal(this, param0, param1);
    }

    default int getDirectSignalTo(BlockPos param0) {
        int var0 = 0;
        var0 = Math.max(var0, this.getDirectSignal(param0.below(), Direction.DOWN));
        if (var0 >= 15) {
            return var0;
        } else {
            var0 = Math.max(var0, this.getDirectSignal(param0.above(), Direction.UP));
            if (var0 >= 15) {
                return var0;
            } else {
                var0 = Math.max(var0, this.getDirectSignal(param0.north(), Direction.NORTH));
                if (var0 >= 15) {
                    return var0;
                } else {
                    var0 = Math.max(var0, this.getDirectSignal(param0.south(), Direction.SOUTH));
                    if (var0 >= 15) {
                        return var0;
                    } else {
                        var0 = Math.max(var0, this.getDirectSignal(param0.west(), Direction.WEST));
                        if (var0 >= 15) {
                            return var0;
                        } else {
                            var0 = Math.max(var0, this.getDirectSignal(param0.east(), Direction.EAST));
                            return var0 >= 15 ? var0 : var0;
                        }
                    }
                }
            }
        }
    }

    default int getControlInputSignal(BlockPos param0, Direction param1, boolean param2) {
        BlockState var0 = this.getBlockState(param0);
        if (param2) {
            return DiodeBlock.isDiode(var0) ? this.getDirectSignal(param0, param1) : 0;
        } else if (var0.is(Blocks.REDSTONE_BLOCK)) {
            return 15;
        } else if (var0.is(Blocks.REDSTONE_WIRE)) {
            return var0.getValue(RedStoneWireBlock.POWER);
        } else {
            return var0.isSignalSource() ? this.getDirectSignal(param0, param1) : 0;
        }
    }

    default boolean hasSignal(BlockPos param0, Direction param1) {
        return this.getSignal(param0, param1) > 0;
    }

    default int getSignal(BlockPos param0, Direction param1) {
        BlockState var0 = this.getBlockState(param0);
        int var1 = var0.getSignal(this, param0, param1);
        return var0.isRedstoneConductor(this, param0) ? Math.max(var1, this.getDirectSignalTo(param0)) : var1;
    }

    default boolean hasNeighborSignal(BlockPos param0) {
        if (this.getSignal(param0.below(), Direction.DOWN) > 0) {
            return true;
        } else if (this.getSignal(param0.above(), Direction.UP) > 0) {
            return true;
        } else if (this.getSignal(param0.north(), Direction.NORTH) > 0) {
            return true;
        } else if (this.getSignal(param0.south(), Direction.SOUTH) > 0) {
            return true;
        } else if (this.getSignal(param0.west(), Direction.WEST) > 0) {
            return true;
        } else {
            return this.getSignal(param0.east(), Direction.EAST) > 0;
        }
    }

    default int getBestNeighborSignal(BlockPos param0) {
        int var0 = 0;

        for(Direction var1 : DIRECTIONS) {
            int var2 = this.getSignal(param0.relative(var1), var1);
            if (var2 >= 15) {
                return 15;
            }

            if (var2 > var0) {
                var0 = var2;
            }
        }

        return var0;
    }
}
