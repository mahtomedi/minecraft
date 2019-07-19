package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class CoralWallFanBlock extends BaseCoralWallFanBlock {
    private final Block deadBlock;

    protected CoralWallFanBlock(Block param0, Block.Properties param1) {
        super(param1);
        this.deadBlock = param0;
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        this.tryScheduleDieTick(param0, param1, param2);
    }

    @Override
    public void tick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        if (!scanForWater(param0, param1, param2)) {
            param1.setBlock(
                param2, this.deadBlock.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, param0.getValue(FACING)), 2
            );
        }

    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param1.getOpposite() == param0.getValue(FACING) && !param0.canSurvive(param3, param4)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            if (param0.getValue(WATERLOGGED)) {
                param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
            }

            this.tryScheduleDieTick(param0, param3, param4);
            return super.updateShape(param0, param1, param2, param3, param4, param5);
        }
    }
}
