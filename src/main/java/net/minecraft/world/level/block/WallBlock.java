package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WallBlock extends CrossCollisionBlock {
    public static final BooleanProperty UP = BlockStateProperties.UP;
    private final VoxelShape[] shapeWithPostByIndex;
    private final VoxelShape[] collisionShapeWithPostByIndex;

    public WallBlock(Block.Properties param0) {
        super(0.0F, 3.0F, 0.0F, 14.0F, 24.0F, param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(UP, Boolean.valueOf(true))
                .setValue(NORTH, Boolean.valueOf(false))
                .setValue(EAST, Boolean.valueOf(false))
                .setValue(SOUTH, Boolean.valueOf(false))
                .setValue(WEST, Boolean.valueOf(false))
                .setValue(WATERLOGGED, Boolean.valueOf(false))
        );
        this.shapeWithPostByIndex = this.makeShapes(4.0F, 3.0F, 16.0F, 0.0F, 14.0F);
        this.collisionShapeWithPostByIndex = this.makeShapes(4.0F, 3.0F, 24.0F, 0.0F, 24.0F);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return param0.getValue(UP) ? this.shapeWithPostByIndex[this.getAABBIndex(param0)] : super.getShape(param0, param1, param2, param3);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return param0.getValue(UP) ? this.collisionShapeWithPostByIndex[this.getAABBIndex(param0)] : super.getCollisionShape(param0, param1, param2, param3);
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }

    private boolean connectsTo(BlockState param0, boolean param1, Direction param2) {
        Block var0 = param0.getBlock();
        boolean var1 = var0.is(BlockTags.WALLS) || var0 instanceof FenceGateBlock && FenceGateBlock.connectsToDirection(param0, param2);
        return !isExceptionForConnection(var0) && param1 || var1;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        LevelReader var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        FluidState var2 = param0.getLevel().getFluidState(param0.getClickedPos());
        BlockPos var3 = var1.north();
        BlockPos var4 = var1.east();
        BlockPos var5 = var1.south();
        BlockPos var6 = var1.west();
        BlockState var7 = var0.getBlockState(var3);
        BlockState var8 = var0.getBlockState(var4);
        BlockState var9 = var0.getBlockState(var5);
        BlockState var10 = var0.getBlockState(var6);
        boolean var11 = this.connectsTo(var7, var7.isFaceSturdy(var0, var3, Direction.SOUTH), Direction.SOUTH);
        boolean var12 = this.connectsTo(var8, var8.isFaceSturdy(var0, var4, Direction.WEST), Direction.WEST);
        boolean var13 = this.connectsTo(var9, var9.isFaceSturdy(var0, var5, Direction.NORTH), Direction.NORTH);
        boolean var14 = this.connectsTo(var10, var10.isFaceSturdy(var0, var6, Direction.EAST), Direction.EAST);
        boolean var15 = (!var11 || var12 || !var13 || var14) && (var11 || !var12 || var13 || !var14);
        return this.defaultBlockState()
            .setValue(UP, Boolean.valueOf(var15 || !var0.isEmptyBlock(var1.above())))
            .setValue(NORTH, Boolean.valueOf(var11))
            .setValue(EAST, Boolean.valueOf(var12))
            .setValue(SOUTH, Boolean.valueOf(var13))
            .setValue(WEST, Boolean.valueOf(var14))
            .setValue(WATERLOGGED, Boolean.valueOf(var2.getType() == Fluids.WATER));
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        if (param1 == Direction.DOWN) {
            return super.updateShape(param0, param1, param2, param3, param4, param5);
        } else {
            Direction var0 = param1.getOpposite();
            boolean var1 = param1 == Direction.NORTH ? this.connectsTo(param2, param2.isFaceSturdy(param3, param5, var0), var0) : param0.getValue(NORTH);
            boolean var2 = param1 == Direction.EAST ? this.connectsTo(param2, param2.isFaceSturdy(param3, param5, var0), var0) : param0.getValue(EAST);
            boolean var3 = param1 == Direction.SOUTH ? this.connectsTo(param2, param2.isFaceSturdy(param3, param5, var0), var0) : param0.getValue(SOUTH);
            boolean var4 = param1 == Direction.WEST ? this.connectsTo(param2, param2.isFaceSturdy(param3, param5, var0), var0) : param0.getValue(WEST);
            boolean var5 = (!var1 || var2 || !var3 || var4) && (var1 || !var2 || var3 || !var4);
            return param0.setValue(UP, Boolean.valueOf(var5 || !param3.isEmptyBlock(param4.above())))
                .setValue(NORTH, Boolean.valueOf(var1))
                .setValue(EAST, Boolean.valueOf(var2))
                .setValue(SOUTH, Boolean.valueOf(var3))
                .setValue(WEST, Boolean.valueOf(var4));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(UP, NORTH, EAST, WEST, SOUTH, WATERLOGGED);
    }
}
