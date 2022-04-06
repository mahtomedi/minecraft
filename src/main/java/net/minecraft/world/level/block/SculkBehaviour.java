package net.minecraft.world.level.block;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public interface SculkBehaviour {
    SculkBehaviour DEFAULT = new SculkBehaviour() {
        @Override
        public boolean attemptSpreadVein(LevelAccessor param0, BlockPos param1, BlockState param2, @Nullable Collection<Direction> param3, boolean param4) {
            if (param3 == null) {
                return ((SculkVeinBlock)Blocks.SCULK_VEIN).getSameSpaceSpreader().spreadAll(param0.getBlockState(param1), param0, param1, param4) > 0L;
            } else if (!param3.isEmpty()) {
                return !param2.isAir() && !param2.getFluidState().is(Fluids.WATER) ? false : SculkVeinBlock.regrow(param0, param1, param2, param3);
            } else {
                return SculkBehaviour.super.attemptSpreadVein(param0, param1, param2, param3, param4);
            }
        }

        @Override
        public int attemptUseCharge(
            SculkSpreader.ChargeCursor param0, LevelAccessor param1, BlockPos param2, RandomSource param3, SculkSpreader param4, boolean param5
        ) {
            return param0.getDecayDelay() > 0 ? param0.getCharge() : 0;
        }

        @Override
        public int updateDecayDelay(int param0) {
            return Math.max(param0 - 1, 0);
        }
    };

    default byte getSculkSpreadDelay() {
        return 1;
    }

    default void onDischarged(LevelAccessor param0, BlockState param1, BlockPos param2, RandomSource param3) {
    }

    default boolean depositCharge(LevelAccessor param0, BlockPos param1, RandomSource param2) {
        return false;
    }

    default boolean attemptSpreadVein(LevelAccessor param0, BlockPos param1, BlockState param2, @Nullable Collection<Direction> param3, boolean param4) {
        return ((MultifaceBlock)Blocks.SCULK_VEIN).getSpreader().spreadAll(param2, param0, param1, param4) > 0L;
    }

    default boolean canChangeBlockStateOnSpread() {
        return true;
    }

    default int updateDecayDelay(int param0) {
        return 1;
    }

    int attemptUseCharge(SculkSpreader.ChargeCursor var1, LevelAccessor var2, BlockPos var3, RandomSource var4, SculkSpreader var5, boolean var6);
}
