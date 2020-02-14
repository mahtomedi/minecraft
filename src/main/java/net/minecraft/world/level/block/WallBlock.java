package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WallBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final EnumProperty<WallSide> EAST_WALL = BlockStateProperties.EAST_WALL;
    public static final EnumProperty<WallSide> NORTH_WALL = BlockStateProperties.NORTH_WALL;
    public static final EnumProperty<WallSide> SOUTH_WALL = BlockStateProperties.SOUTH_WALL;
    public static final EnumProperty<WallSide> WEST_WALL = BlockStateProperties.WEST_WALL;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private final Map<BlockState, VoxelShape> shapeByIndex;
    private final Map<BlockState, VoxelShape> collisionShapeByIndex;
    private static final VoxelShape POST_TEST = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
    private static final VoxelShape NORTH_TEST = Block.box(5.0, 0.0, 0.0, 11.0, 16.0, 11.0);
    private static final VoxelShape SOUTH_TEST = Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 16.0);
    private static final VoxelShape WEST_TEST = Block.box(0.0, 0.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape EAST_TEST = Block.box(5.0, 0.0, 5.0, 16.0, 16.0, 11.0);

    public WallBlock(Block.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(UP, Boolean.valueOf(true))
                .setValue(NORTH_WALL, WallSide.NONE)
                .setValue(EAST_WALL, WallSide.NONE)
                .setValue(SOUTH_WALL, WallSide.NONE)
                .setValue(WEST_WALL, WallSide.NONE)
                .setValue(WATERLOGGED, Boolean.valueOf(false))
        );
        this.shapeByIndex = this.makeShapes(4.0F, 3.0F, 16.0F, 0.0F, 14.0F, 16.0F);
        this.collisionShapeByIndex = this.makeShapes(4.0F, 3.0F, 24.0F, 0.0F, 24.0F, 24.0F);
    }

    private static VoxelShape applyWallShape(VoxelShape param0, WallSide param1, VoxelShape param2, VoxelShape param3) {
        if (param1 == WallSide.TALL) {
            return Shapes.or(param0, param3);
        } else {
            return param1 == WallSide.LOW ? Shapes.or(param0, param2) : param0;
        }
    }

    private Map<BlockState, VoxelShape> makeShapes(float param0, float param1, float param2, float param3, float param4, float param5) {
        float var0 = 8.0F - param0;
        float var1 = 8.0F + param0;
        float var2 = 8.0F - param1;
        float var3 = 8.0F + param1;
        VoxelShape var4 = Block.box((double)var0, 0.0, (double)var0, (double)var1, (double)param2, (double)var1);
        VoxelShape var5 = Block.box((double)var2, (double)param3, 0.0, (double)var3, (double)param4, (double)var3);
        VoxelShape var6 = Block.box((double)var2, (double)param3, (double)var2, (double)var3, (double)param4, 16.0);
        VoxelShape var7 = Block.box(0.0, (double)param3, (double)var2, (double)var3, (double)param4, (double)var3);
        VoxelShape var8 = Block.box((double)var2, (double)param3, (double)var2, 16.0, (double)param4, (double)var3);
        VoxelShape var9 = Block.box((double)var2, (double)param3, 0.0, (double)var3, (double)param5, (double)var3);
        VoxelShape var10 = Block.box((double)var2, (double)param3, (double)var2, (double)var3, (double)param5, 16.0);
        VoxelShape var11 = Block.box(0.0, (double)param3, (double)var2, (double)var3, (double)param5, (double)var3);
        VoxelShape var12 = Block.box((double)var2, (double)param3, (double)var2, 16.0, (double)param5, (double)var3);
        Builder<BlockState, VoxelShape> var13 = ImmutableMap.builder();

        for(Boolean var14 : UP.getPossibleValues()) {
            for(WallSide var15 : EAST_WALL.getPossibleValues()) {
                for(WallSide var16 : NORTH_WALL.getPossibleValues()) {
                    for(WallSide var17 : WEST_WALL.getPossibleValues()) {
                        for(WallSide var18 : SOUTH_WALL.getPossibleValues()) {
                            VoxelShape var19 = Shapes.empty();
                            var19 = applyWallShape(var19, var15, var8, var12);
                            var19 = applyWallShape(var19, var17, var7, var11);
                            var19 = applyWallShape(var19, var16, var5, var9);
                            var19 = applyWallShape(var19, var18, var6, var10);
                            if (var14) {
                                var19 = Shapes.or(var19, var4);
                            }

                            BlockState var20 = this.defaultBlockState()
                                .setValue(UP, var14)
                                .setValue(EAST_WALL, var15)
                                .setValue(WEST_WALL, var17)
                                .setValue(NORTH_WALL, var16)
                                .setValue(SOUTH_WALL, var18);
                            var13.put(var20.setValue(WATERLOGGED, Boolean.valueOf(false)), var19);
                            var13.put(var20.setValue(WATERLOGGED, Boolean.valueOf(true)), var19);
                        }
                    }
                }
            }
        }

        return var13.build();
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return this.shapeByIndex.get(param0);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return this.collisionShapeByIndex.get(param0);
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
        BlockPos var7 = var1.above();
        BlockState var8 = var0.getBlockState(var3);
        BlockState var9 = var0.getBlockState(var4);
        BlockState var10 = var0.getBlockState(var5);
        BlockState var11 = var0.getBlockState(var6);
        BlockState var12 = var0.getBlockState(var7);
        boolean var13 = this.connectsTo(var8, var8.isFaceSturdy(var0, var3, Direction.SOUTH), Direction.SOUTH);
        boolean var14 = this.connectsTo(var9, var9.isFaceSturdy(var0, var4, Direction.WEST), Direction.WEST);
        boolean var15 = this.connectsTo(var10, var10.isFaceSturdy(var0, var5, Direction.NORTH), Direction.NORTH);
        boolean var16 = this.connectsTo(var11, var11.isFaceSturdy(var0, var6, Direction.EAST), Direction.EAST);
        BlockState var17 = this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(var2.getType() == Fluids.WATER));
        return this.updateShape(var0, var17, var7, var12, var13, var14, var15, var16);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        if (param1 == Direction.DOWN) {
            return super.updateShape(param0, param1, param2, param3, param4, param5);
        } else {
            return param1 == Direction.UP ? this.topUpdate(param3, param0, param5, param2) : this.sideUpdate(param3, param4, param0, param5, param2, param1);
        }
    }

    private static boolean isConnected(BlockState param0, Property<WallSide> param1) {
        return param0.getValue(param1) != WallSide.NONE;
    }

    private static boolean isCovered(VoxelShape param0, VoxelShape param1) {
        return !Shapes.joinIsNotEmpty(param1, param0, BooleanOp.ONLY_FIRST);
    }

    private BlockState topUpdate(LevelReader param0, BlockState param1, BlockPos param2, BlockState param3) {
        boolean var0 = isConnected(param1, NORTH_WALL);
        boolean var1 = isConnected(param1, EAST_WALL);
        boolean var2 = isConnected(param1, SOUTH_WALL);
        boolean var3 = isConnected(param1, WEST_WALL);
        return this.updateShape(param0, param1, param2, param3, var0, var1, var2, var3);
    }

    private BlockState sideUpdate(LevelReader param0, BlockPos param1, BlockState param2, BlockPos param3, BlockState param4, Direction param5) {
        Direction var0 = param5.getOpposite();
        boolean var1 = param5 == Direction.NORTH ? this.connectsTo(param4, param4.isFaceSturdy(param0, param3, var0), var0) : isConnected(param2, NORTH_WALL);
        boolean var2 = param5 == Direction.EAST ? this.connectsTo(param4, param4.isFaceSturdy(param0, param3, var0), var0) : isConnected(param2, EAST_WALL);
        boolean var3 = param5 == Direction.SOUTH ? this.connectsTo(param4, param4.isFaceSturdy(param0, param3, var0), var0) : isConnected(param2, SOUTH_WALL);
        boolean var4 = param5 == Direction.WEST ? this.connectsTo(param4, param4.isFaceSturdy(param0, param3, var0), var0) : isConnected(param2, WEST_WALL);
        BlockPos var5 = param1.above();
        BlockState var6 = param0.getBlockState(var5);
        return this.updateShape(param0, param2, var5, var6, var1, var2, var3, var4);
    }

    private BlockState updateShape(
        LevelReader param0, BlockState param1, BlockPos param2, BlockState param3, boolean param4, boolean param5, boolean param6, boolean param7
    ) {
        VoxelShape var0 = param3.getCollisionShape(param0, param2).getFaceShape(Direction.DOWN);
        boolean var1 = (!param4 || param5 || !param6 || param7) && (param4 || !param5 || param6 || !param7);
        boolean var2 = var1 || param3.getBlock().is(BlockTags.WALL_POST_OVERRIDE) || isCovered(var0, POST_TEST);
        return this.updateSides(param1.setValue(UP, Boolean.valueOf(var2)), param4, param5, param6, param7, var0);
    }

    private BlockState updateSides(BlockState param0, boolean param1, boolean param2, boolean param3, boolean param4, VoxelShape param5) {
        return param0.setValue(NORTH_WALL, this.makeWallState(param1, param5, NORTH_TEST))
            .setValue(EAST_WALL, this.makeWallState(param2, param5, EAST_TEST))
            .setValue(SOUTH_WALL, this.makeWallState(param3, param5, SOUTH_TEST))
            .setValue(WEST_WALL, this.makeWallState(param4, param5, WEST_TEST));
    }

    private WallSide makeWallState(boolean param0, VoxelShape param1, VoxelShape param2) {
        if (param0) {
            return isCovered(param1, param2) ? WallSide.TALL : WallSide.LOW;
        } else {
            return WallSide.NONE;
        }
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState param0, BlockGetter param1, BlockPos param2) {
        return !param0.getValue(WATERLOGGED);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(UP, NORTH_WALL, EAST_WALL, WEST_WALL, SOUTH_WALL, WATERLOGGED);
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        switch(param1) {
            case CLOCKWISE_180:
                return param0.setValue(NORTH_WALL, param0.getValue(SOUTH_WALL))
                    .setValue(EAST_WALL, param0.getValue(WEST_WALL))
                    .setValue(SOUTH_WALL, param0.getValue(NORTH_WALL))
                    .setValue(WEST_WALL, param0.getValue(EAST_WALL));
            case COUNTERCLOCKWISE_90:
                return param0.setValue(NORTH_WALL, param0.getValue(EAST_WALL))
                    .setValue(EAST_WALL, param0.getValue(SOUTH_WALL))
                    .setValue(SOUTH_WALL, param0.getValue(WEST_WALL))
                    .setValue(WEST_WALL, param0.getValue(NORTH_WALL));
            case CLOCKWISE_90:
                return param0.setValue(NORTH_WALL, param0.getValue(WEST_WALL))
                    .setValue(EAST_WALL, param0.getValue(NORTH_WALL))
                    .setValue(SOUTH_WALL, param0.getValue(EAST_WALL))
                    .setValue(WEST_WALL, param0.getValue(SOUTH_WALL));
            default:
                return param0;
        }
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        switch(param1) {
            case LEFT_RIGHT:
                return param0.setValue(NORTH_WALL, param0.getValue(SOUTH_WALL)).setValue(SOUTH_WALL, param0.getValue(NORTH_WALL));
            case FRONT_BACK:
                return param0.setValue(EAST_WALL, param0.getValue(WEST_WALL)).setValue(WEST_WALL, param0.getValue(EAST_WALL));
            default:
                return super.mirror(param0, param1);
        }
    }
}
