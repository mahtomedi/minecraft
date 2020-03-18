package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SnowLayerBlock extends Block {
    public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;
    protected static final VoxelShape[] SHAPE_BY_LAYER = new VoxelShape[]{
        Shapes.empty(),
        Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
    };

    protected SnowLayerBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, Integer.valueOf(1)));
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        switch(param3) {
            case LAND:
                return param0.getValue(LAYERS) < 5;
            case WATER:
                return false;
            case AIR:
                return false;
            default:
                return false;
        }
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE_BY_LAYER[param0.getValue(LAYERS)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE_BY_LAYER[param0.getValue(LAYERS) - 1];
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        return SHAPE_BY_LAYER[param0.getValue(LAYERS)];
    }

    @Override
    public VoxelShape getVisualShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE_BY_LAYER[param0.getValue(LAYERS)];
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState param0) {
        return true;
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockState var0 = param1.getBlockState(param2.below());
        Block var1 = var0.getBlock();
        if (var1 == Blocks.ICE || var1 == Blocks.PACKED_ICE || var1 == Blocks.BARRIER) {
            return false;
        } else if (var1 != Blocks.HONEY_BLOCK && var1 != Blocks.SOUL_SAND) {
            return Block.isFaceFull(var0.getCollisionShape(param1, param2.below()), Direction.UP) || var1 == this && var0.getValue(LAYERS) == 8;
        } else {
            return true;
        }
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return !param0.canSurvive(param3, param4) ? Blocks.AIR.defaultBlockState() : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (param1.getBrightness(LightLayer.BLOCK, param2) > 11) {
            dropResources(param0, param1, param2);
            param1.removeBlock(param2, false);
        }

    }

    @Override
    public boolean canBeReplaced(BlockState param0, BlockPlaceContext param1) {
        int var0 = param0.getValue(LAYERS);
        if (param1.getItemInHand().getItem() != this.asItem() || var0 >= 8) {
            return var0 == 1;
        } else if (param1.replacingClickedOnBlock()) {
            return param1.getClickedFace() == Direction.UP;
        } else {
            return true;
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockState var0 = param0.getLevel().getBlockState(param0.getClickedPos());
        if (var0.getBlock() == this) {
            int var1 = var0.getValue(LAYERS);
            return var0.setValue(LAYERS, Integer.valueOf(Math.min(8, var1 + 1)));
        } else {
            return super.getStateForPlacement(param0);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(LAYERS);
    }
}
