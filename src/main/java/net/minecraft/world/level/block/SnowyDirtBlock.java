package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class SnowyDirtBlock extends Block {
    public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;

    protected SnowyDirtBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(SNOWY, Boolean.valueOf(false)));
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param1 != Direction.UP) {
            return super.updateShape(param0, param1, param2, param3, param4, param5);
        } else {
            Block var0 = param2.getBlock();
            return param0.setValue(SNOWY, Boolean.valueOf(var0 == Blocks.SNOW_BLOCK || var0 == Blocks.SNOW));
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        Block var0 = param0.getLevel().getBlockState(param0.getClickedPos().above()).getBlock();
        return this.defaultBlockState().setValue(SNOWY, Boolean.valueOf(var0 == Blocks.SNOW_BLOCK || var0 == Blocks.SNOW));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(SNOWY);
    }
}
