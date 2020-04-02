package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;

public class ChorusPlantBlock extends PipeBlock {
    protected ChorusPlantBlock(BlockBehaviour.Properties param0) {
        super(0.3125F, param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(NORTH, Boolean.valueOf(false))
                .setValue(EAST, Boolean.valueOf(false))
                .setValue(SOUTH, Boolean.valueOf(false))
                .setValue(WEST, Boolean.valueOf(false))
                .setValue(UP, Boolean.valueOf(false))
                .setValue(DOWN, Boolean.valueOf(false))
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.getStateForPlacement(param0.getLevel(), param0.getClickedPos());
    }

    public BlockState getStateForPlacement(BlockGetter param0, BlockPos param1) {
        Block var0 = param0.getBlockState(param1.below()).getBlock();
        Block var1 = param0.getBlockState(param1.above()).getBlock();
        Block var2 = param0.getBlockState(param1.north()).getBlock();
        Block var3 = param0.getBlockState(param1.east()).getBlock();
        Block var4 = param0.getBlockState(param1.south()).getBlock();
        Block var5 = param0.getBlockState(param1.west()).getBlock();
        return this.defaultBlockState()
            .setValue(DOWN, Boolean.valueOf(var0 == this || var0 == Blocks.CHORUS_FLOWER || var0 == Blocks.END_STONE))
            .setValue(UP, Boolean.valueOf(var1 == this || var1 == Blocks.CHORUS_FLOWER))
            .setValue(NORTH, Boolean.valueOf(var2 == this || var2 == Blocks.CHORUS_FLOWER))
            .setValue(EAST, Boolean.valueOf(var3 == this || var3 == Blocks.CHORUS_FLOWER))
            .setValue(SOUTH, Boolean.valueOf(var4 == this || var4 == Blocks.CHORUS_FLOWER))
            .setValue(WEST, Boolean.valueOf(var5 == this || var5 == Blocks.CHORUS_FLOWER));
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (!param0.canSurvive(param3, param4)) {
            param3.getBlockTicks().scheduleTick(param4, this, 1);
            return super.updateShape(param0, param1, param2, param3, param4, param5);
        } else {
            Block var0 = param2.getBlock();
            boolean var1 = var0 == this || var0 == Blocks.CHORUS_FLOWER || param1 == Direction.DOWN && var0 == Blocks.END_STONE;
            return param0.setValue(PROPERTY_BY_DIRECTION.get(param1), Boolean.valueOf(var1));
        }
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (!param0.canSurvive(param1, param2)) {
            param1.destroyBlock(param2, true);
        }

    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockState var0 = param1.getBlockState(param2.below());
        boolean var1 = !param1.getBlockState(param2.above()).isAir() && !var0.isAir();

        for(Direction var2 : Direction.Plane.HORIZONTAL) {
            BlockPos var3 = param2.relative(var2);
            Block var4 = param1.getBlockState(var3).getBlock();
            if (var4 == this) {
                if (var1) {
                    return false;
                }

                Block var5 = param1.getBlockState(var3.below()).getBlock();
                if (var5 == this || var5 == Blocks.END_STONE) {
                    return true;
                }
            }
        }

        Block var6 = var0.getBlock();
        return var6 == this || var6 == Blocks.END_STONE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}
