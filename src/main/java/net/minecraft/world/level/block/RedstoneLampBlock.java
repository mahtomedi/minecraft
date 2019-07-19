package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class RedstoneLampBlock extends Block {
    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

    public RedstoneLampBlock(Block.Properties param0) {
        super(param0);
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, Boolean.valueOf(false)));
    }

    @Override
    public int getLightEmission(BlockState param0) {
        return param0.getValue(LIT) ? super.getLightEmission(param0) : 0;
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        super.onPlace(param0, param1, param2, param3, param4);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState().setValue(LIT, Boolean.valueOf(param0.getLevel().hasNeighborSignal(param0.getClickedPos())));
    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        if (!param1.isClientSide) {
            boolean var0 = param0.getValue(LIT);
            if (var0 != param1.hasNeighborSignal(param2)) {
                if (var0) {
                    param1.getBlockTicks().scheduleTick(param2, this, 4);
                } else {
                    param1.setBlock(param2, param0.cycle(LIT), 2);
                }
            }

        }
    }

    @Override
    public void tick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        if (!param1.isClientSide) {
            if (param0.getValue(LIT) && !param1.hasNeighborSignal(param2)) {
                param1.setBlock(param2, param0.cycle(LIT), 2);
            }

        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(LIT);
    }
}
