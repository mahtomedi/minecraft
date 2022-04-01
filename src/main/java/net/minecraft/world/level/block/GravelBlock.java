package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class GravelBlock extends FallingBlock {
    public GravelBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public int getDustColor(BlockState param0, BlockGetter param1, BlockPos param2) {
        return -8356741;
    }

    @Override
    public void onLand(Level param0, BlockPos param1, BlockState param2, BlockState param3, FallingBlockEntity param4) {
        super.onLand(param0, param1, param2, param3, param4);

        for(Direction var0 : Direction.Plane.HORIZONTAL) {
            this.trySetFire(param0, param1.relative(var0));
        }

    }

    private void trySetFire(Level param0, BlockPos param1) {
        FireBlock var0 = (FireBlock)Blocks.FIRE;
        int var1 = var0.getFlameOdds(param0.getBlockState(param1));
        if (var1 > 0 && param0.random.nextInt(var1) > 5) {
            param0.setBlock(param1, var0.getStateForPlacement(param0, param1), 3);
        }

    }
}
