package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class AntBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public AntBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        BlockState var0 = param1.getBlockState(param2.below());
        if (var0.getBlock() == Blocks.WHITE_CONCRETE) {
            this.move(param0, param1, param2, AntBlock.Step.CW);
        } else if (var0.getBlock() == Blocks.BLACK_CONCRETE) {
            this.move(param0, param1, param2, AntBlock.Step.CCW);
        }

    }

    private void move(BlockState param0, ServerLevel param1, BlockPos param2, AntBlock.Step param3) {
        Direction var0 = param0.getValue(FACING);
        Direction var1 = param3 == AntBlock.Step.CW ? var0.getClockWise() : var0.getCounterClockWise();
        BlockPos var2 = param2.relative(var1);
        if (param1.isLoaded(var2)) {
            switch(param3) {
                case CW:
                    param1.setBlock(param2.below(), Blocks.BLACK_CONCRETE.defaultBlockState(), 19);
                    param1.setBlock(param2, Blocks.AIR.defaultBlockState(), 3);
                    param1.setBlock(var2, param0.setValue(FACING, var1), 3);
                    break;
                case CCW:
                    param1.setBlock(param2.below(), Blocks.WHITE_CONCRETE.defaultBlockState(), 19);
                    param1.setBlock(param2, Blocks.AIR.defaultBlockState(), 3);
                    param1.setBlock(var2, param0.setValue(FACING, var1), 3);
            }
        }

    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        param1.getBlockTicks().scheduleTick(param2, this, 1);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING);
    }

    static enum Step {
        CW,
        CCW;
    }
}
