package net.minecraft.world.level.block;

import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VineBlock extends Block {
    public static final BooleanProperty UP = PipeBlock.UP;
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION
        .entrySet()
        .stream()
        .filter(param0 -> param0.getKey() != Direction.DOWN)
        .collect(Util.toMap());
    protected static final VoxelShape UP_AABB = Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape EAST_AABB = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
    protected static final VoxelShape WEST_AABB = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
    protected static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);

    public VineBlock(Block.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(UP, Boolean.valueOf(false))
                .setValue(NORTH, Boolean.valueOf(false))
                .setValue(EAST, Boolean.valueOf(false))
                .setValue(SOUTH, Boolean.valueOf(false))
                .setValue(WEST, Boolean.valueOf(false))
        );
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        VoxelShape var0 = Shapes.empty();
        if (param0.getValue(UP)) {
            var0 = Shapes.or(var0, UP_AABB);
        }

        if (param0.getValue(NORTH)) {
            var0 = Shapes.or(var0, SOUTH_AABB);
        }

        if (param0.getValue(EAST)) {
            var0 = Shapes.or(var0, WEST_AABB);
        }

        if (param0.getValue(SOUTH)) {
            var0 = Shapes.or(var0, NORTH_AABB);
        }

        if (param0.getValue(WEST)) {
            var0 = Shapes.or(var0, EAST_AABB);
        }

        return var0;
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        return this.hasFaces(this.getUpdatedState(param0, param1, param2));
    }

    private boolean hasFaces(BlockState param0) {
        return this.countFaces(param0) > 0;
    }

    private int countFaces(BlockState param0) {
        int var0 = 0;

        for(BooleanProperty var1 : PROPERTY_BY_DIRECTION.values()) {
            if (param0.getValue(var1)) {
                ++var0;
            }
        }

        return var0;
    }

    private boolean canSupportAtFace(BlockGetter param0, BlockPos param1, Direction param2) {
        if (param2 == Direction.DOWN) {
            return false;
        } else {
            BlockPos var0 = param1.relative(param2);
            if (isAcceptableNeighbour(param0, var0, param2)) {
                return true;
            } else if (param2.getAxis() == Direction.Axis.Y) {
                return false;
            } else {
                BooleanProperty var1 = PROPERTY_BY_DIRECTION.get(param2);
                BlockState var2 = param0.getBlockState(param1.above());
                return var2.getBlock() == this && var2.getValue(var1);
            }
        }
    }

    public static boolean isAcceptableNeighbour(BlockGetter param0, BlockPos param1, Direction param2) {
        BlockState var0 = param0.getBlockState(param1);
        return Block.isFaceFull(var0.getCollisionShape(param0, param1), param2.getOpposite());
    }

    private BlockState getUpdatedState(BlockState param0, BlockGetter param1, BlockPos param2) {
        BlockPos var0 = param2.above();
        if (param0.getValue(UP)) {
            param0 = param0.setValue(UP, Boolean.valueOf(isAcceptableNeighbour(param1, var0, Direction.DOWN)));
        }

        BlockState var1 = null;

        for(Direction var2 : Direction.Plane.HORIZONTAL) {
            BooleanProperty var3 = getPropertyForFace(var2);
            if (param0.getValue(var3)) {
                boolean var4 = this.canSupportAtFace(param1, param2, var2);
                if (!var4) {
                    if (var1 == null) {
                        var1 = param1.getBlockState(var0);
                    }

                    var4 = var1.getBlock() == this && var1.getValue(var3);
                }

                param0 = param0.setValue(var3, Boolean.valueOf(var4));
            }
        }

        return param0;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param1 == Direction.DOWN) {
            return super.updateShape(param0, param1, param2, param3, param4, param5);
        } else {
            BlockState var0 = this.getUpdatedState(param0, param3, param4);
            return !this.hasFaces(var0) ? Blocks.AIR.defaultBlockState() : var0;
        }
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        BlockState var0 = this.getUpdatedState(param0, param1, param2);
        if (var0 != param0) {
            if (this.hasFaces(var0)) {
                param1.setBlock(param2, var0, 2);
            } else {
                dropResources(param0, param1, param2);
                param1.removeBlock(param2, false);
            }

        } else if (param1.random.nextInt(4) == 0) {
            Direction var1 = Direction.getRandomFace(param3);
            BlockPos var2 = param2.above();
            if (var1.getAxis().isHorizontal() && !param0.getValue(getPropertyForFace(var1))) {
                if (this.canSpread(param1, param2)) {
                    BlockPos var3 = param2.relative(var1);
                    BlockState var4 = param1.getBlockState(var3);
                    if (var4.isAir()) {
                        Direction var5 = var1.getClockWise();
                        Direction var6 = var1.getCounterClockWise();
                        boolean var7 = param0.getValue(getPropertyForFace(var5));
                        boolean var8 = param0.getValue(getPropertyForFace(var6));
                        BlockPos var9 = var3.relative(var5);
                        BlockPos var10 = var3.relative(var6);
                        if (var7 && isAcceptableNeighbour(param1, var9, var5)) {
                            param1.setBlock(var3, this.defaultBlockState().setValue(getPropertyForFace(var5), Boolean.valueOf(true)), 2);
                        } else if (var8 && isAcceptableNeighbour(param1, var10, var6)) {
                            param1.setBlock(var3, this.defaultBlockState().setValue(getPropertyForFace(var6), Boolean.valueOf(true)), 2);
                        } else {
                            Direction var11 = var1.getOpposite();
                            if (var7 && param1.isEmptyBlock(var9) && isAcceptableNeighbour(param1, param2.relative(var5), var11)) {
                                param1.setBlock(var9, this.defaultBlockState().setValue(getPropertyForFace(var11), Boolean.valueOf(true)), 2);
                            } else if (var8 && param1.isEmptyBlock(var10) && isAcceptableNeighbour(param1, param2.relative(var6), var11)) {
                                param1.setBlock(var10, this.defaultBlockState().setValue(getPropertyForFace(var11), Boolean.valueOf(true)), 2);
                            } else if ((double)param1.random.nextFloat() < 0.05 && isAcceptableNeighbour(param1, var3.above(), Direction.UP)) {
                                param1.setBlock(var3, this.defaultBlockState().setValue(UP, Boolean.valueOf(true)), 2);
                            }
                        }
                    } else if (isAcceptableNeighbour(param1, var3, var1)) {
                        param1.setBlock(param2, param0.setValue(getPropertyForFace(var1), Boolean.valueOf(true)), 2);
                    }

                }
            } else {
                if (var1 == Direction.UP && param2.getY() < 255) {
                    if (this.canSupportAtFace(param1, param2, var1)) {
                        param1.setBlock(param2, param0.setValue(UP, Boolean.valueOf(true)), 2);
                        return;
                    }

                    if (param1.isEmptyBlock(var2)) {
                        if (!this.canSpread(param1, param2)) {
                            return;
                        }

                        BlockState var12 = param0;

                        for(Direction var13 : Direction.Plane.HORIZONTAL) {
                            if (param3.nextBoolean() || !isAcceptableNeighbour(param1, var2.relative(var13), Direction.UP)) {
                                var12 = var12.setValue(getPropertyForFace(var13), Boolean.valueOf(false));
                            }
                        }

                        if (this.hasHorizontalConnection(var12)) {
                            param1.setBlock(var2, var12, 2);
                        }

                        return;
                    }
                }

                if (param2.getY() > 0) {
                    BlockPos var14 = param2.below();
                    BlockState var15 = param1.getBlockState(var14);
                    if (var15.isAir() || var15.getBlock() == this) {
                        BlockState var16 = var15.isAir() ? this.defaultBlockState() : var15;
                        BlockState var17 = this.copyRandomFaces(param0, var16, param3);
                        if (var16 != var17 && this.hasHorizontalConnection(var17)) {
                            param1.setBlock(var14, var17, 2);
                        }
                    }
                }

            }
        }
    }

    private BlockState copyRandomFaces(BlockState param0, BlockState param1, Random param2) {
        for(Direction var0 : Direction.Plane.HORIZONTAL) {
            if (param2.nextBoolean()) {
                BooleanProperty var1 = getPropertyForFace(var0);
                if (param0.getValue(var1)) {
                    param1 = param1.setValue(var1, Boolean.valueOf(true));
                }
            }
        }

        return param1;
    }

    private boolean hasHorizontalConnection(BlockState param0) {
        return param0.getValue(NORTH) || param0.getValue(EAST) || param0.getValue(SOUTH) || param0.getValue(WEST);
    }

    private boolean canSpread(BlockGetter param0, BlockPos param1) {
        int var0 = 4;
        Iterable<BlockPos> var1 = BlockPos.betweenClosed(
            param1.getX() - 4, param1.getY() - 1, param1.getZ() - 4, param1.getX() + 4, param1.getY() + 1, param1.getZ() + 4
        );
        int var2 = 5;

        for(BlockPos var3 : var1) {
            if (param0.getBlockState(var3).getBlock() == this) {
                if (--var2 <= 0) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean canBeReplaced(BlockState param0, BlockPlaceContext param1) {
        BlockState var0 = param1.getLevel().getBlockState(param1.getClickedPos());
        if (var0.getBlock() == this) {
            return this.countFaces(var0) < PROPERTY_BY_DIRECTION.size();
        } else {
            return super.canBeReplaced(param0, param1);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockState var0 = param0.getLevel().getBlockState(param0.getClickedPos());
        boolean var1 = var0.getBlock() == this;
        BlockState var2 = var1 ? var0 : this.defaultBlockState();

        for(Direction var3 : param0.getNearestLookingDirections()) {
            if (var3 != Direction.DOWN) {
                BooleanProperty var4 = getPropertyForFace(var3);
                boolean var5 = var1 && var0.getValue(var4);
                if (!var5 && this.canSupportAtFace(param0.getLevel(), param0.getClickedPos(), var3)) {
                    return var2.setValue(var4, Boolean.valueOf(true));
                }
            }
        }

        return var1 ? var2 : null;
    }

    @Override
    public BlockLayer getRenderLayer() {
        return BlockLayer.CUTOUT;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(UP, NORTH, EAST, SOUTH, WEST);
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        switch(param1) {
            case CLOCKWISE_180:
                return param0.setValue(NORTH, param0.getValue(SOUTH))
                    .setValue(EAST, param0.getValue(WEST))
                    .setValue(SOUTH, param0.getValue(NORTH))
                    .setValue(WEST, param0.getValue(EAST));
            case COUNTERCLOCKWISE_90:
                return param0.setValue(NORTH, param0.getValue(EAST))
                    .setValue(EAST, param0.getValue(SOUTH))
                    .setValue(SOUTH, param0.getValue(WEST))
                    .setValue(WEST, param0.getValue(NORTH));
            case CLOCKWISE_90:
                return param0.setValue(NORTH, param0.getValue(WEST))
                    .setValue(EAST, param0.getValue(NORTH))
                    .setValue(SOUTH, param0.getValue(EAST))
                    .setValue(WEST, param0.getValue(SOUTH));
            default:
                return param0;
        }
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        switch(param1) {
            case LEFT_RIGHT:
                return param0.setValue(NORTH, param0.getValue(SOUTH)).setValue(SOUTH, param0.getValue(NORTH));
            case FRONT_BACK:
                return param0.setValue(EAST, param0.getValue(WEST)).setValue(WEST, param0.getValue(EAST));
            default:
                return super.mirror(param0, param1);
        }
    }

    public static BooleanProperty getPropertyForFace(Direction param0) {
        return PROPERTY_BY_DIRECTION.get(param0);
    }
}
