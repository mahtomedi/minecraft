package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class CoralBlock extends Block {
    private final Block deadBlock;

    public CoralBlock(Block param0, Block.Properties param1) {
        super(param1);
        this.deadBlock = param0;
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (!this.scanForWater(param1, param2)) {
            param1.setBlock(param2, this.deadBlock.defaultBlockState(), 2);
        }

    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (!this.scanForWater(param3, param4)) {
            param3.getBlockTicks().scheduleTick(param4, this, 60 + param3.getRandom().nextInt(40));
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    protected boolean scanForWater(BlockGetter param0, BlockPos param1) {
        for(Direction var0 : Direction.values()) {
            FluidState var1 = param0.getFluidState(param1.relative(var0));
            if (var1.is(FluidTags.WATER)) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        if (!this.scanForWater(param0.getLevel(), param0.getClickedPos())) {
            param0.getLevel().getBlockTicks().scheduleTick(param0.getClickedPos(), this, 60 + param0.getLevel().getRandom().nextInt(40));
        }

        return this.defaultBlockState();
    }
}
