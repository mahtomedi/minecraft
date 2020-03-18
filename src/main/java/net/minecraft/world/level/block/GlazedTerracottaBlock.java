package net.minecraft.world.level.block;

import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.PushReaction;

public class GlazedTerracottaBlock extends HorizontalDirectionalBlock {
    public GlazedTerracottaBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState().setValue(FACING, param0.getHorizontalDirection().getOpposite());
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState param0) {
        return PushReaction.PUSH_ONLY;
    }
}
