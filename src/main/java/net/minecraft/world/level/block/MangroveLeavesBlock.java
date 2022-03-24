package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MangroveLeavesBlock extends LeavesBlock implements BonemealableBlock {
    public static final int GROWTH_CHANCE = 5;

    public MangroveLeavesBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public boolean isRandomlyTicking(BlockState param0) {
        return true;
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        super.randomTick(param0, param1, param2, param3);
        if (param3.nextInt(5) == 0 && !param0.getValue(PERSISTENT) && !this.decaying(param0)) {
            BlockPos var0 = param2.below();
            if (param1.getBlockState(var0).isAir() && param1.getBlockState(var0.below()).isAir() && !isTooCloseToAnotherPropagule(param1, var0)) {
                param1.setBlockAndUpdate(var0, MangrovePropaguleBlock.createNewHangingPropagule());
            }

        }
    }

    private static boolean isTooCloseToAnotherPropagule(LevelAccessor param0, BlockPos param1) {
        for(BlockPos var1 : BlockPos.betweenClosed(param1.above().north().east(), param1.below().south().west())) {
            if (param0.getBlockState(var1).is(Blocks.MANGROVE_PROPAGULE)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter param0, BlockPos param1, BlockState param2, boolean param3) {
        return param0.getBlockState(param1.below()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level param0, Random param1, BlockPos param2, BlockState param3) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel param0, Random param1, BlockPos param2, BlockState param3) {
        param0.setBlock(param2.below(), MangrovePropaguleBlock.createNewHangingPropagule(), 2);
    }
}
