package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ChangeOverTimeSlabBlock extends SlabBlock implements ChangeOverTimeBlock {
    private final Block changeTo;

    public ChangeOverTimeSlabBlock(BlockBehaviour.Properties param0, Block param1) {
        super(param0);
        this.changeTo = param1;
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        super.onPlace(param0, param1, param2, param3, param4);
        this.scheduleChange(param1, this, param2);
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        this.change(param1, param0, param2);
    }

    @Override
    public BlockState getChangeTo(BlockState param0) {
        return this.changeTo.defaultBlockState().setValue(TYPE, param0.getValue(TYPE)).setValue(WATERLOGGED, param0.getValue(WATERLOGGED));
    }
}