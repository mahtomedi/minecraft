package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MultifaceBlock extends Block {
    private static final VoxelShape UP_AABB = Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape DOWN_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private static final VoxelShape WEST_AABB = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
    private static final VoxelShape EAST_AABB = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
    private static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
    private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION;
    private static final Map<Direction, VoxelShape> SHAPE_BY_DIRECTION = Util.make(Maps.newEnumMap(Direction.class), param0 -> {
        param0.put(Direction.NORTH, NORTH_AABB);
        param0.put(Direction.EAST, EAST_AABB);
        param0.put(Direction.SOUTH, SOUTH_AABB);
        param0.put(Direction.WEST, WEST_AABB);
        param0.put(Direction.UP, UP_AABB);
        param0.put(Direction.DOWN, DOWN_AABB);
    });
    private static final Direction[] DIRECTIONS = Direction.values();
    private final ImmutableMap<BlockState, VoxelShape> shapesCache;
    private final boolean canRotate;
    private final boolean canMirrorX;
    private final boolean canMirrorZ;

    public MultifaceBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(getDefaultMultifaceState(this.stateDefinition));
        this.shapesCache = getShapes(this.stateDefinition);
        this.canRotate = Direction.Plane.HORIZONTAL.stream().allMatch(this::isFaceSupported);
        this.canMirrorX = Direction.Plane.HORIZONTAL.stream().filter(Direction.Axis.X).filter(this::isFaceSupported).count() % 2L == 0L;
        this.canMirrorZ = Direction.Plane.HORIZONTAL.stream().filter(Direction.Axis.Z).filter(this::isFaceSupported).count() % 2L == 0L;
    }

    protected boolean isFaceSupported(Direction param0x) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        for(Direction var0 : DIRECTIONS) {
            if (this.isFaceSupported(var0)) {
                param0.add(getFaceProperty(var0));
            }
        }

    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return hasFace(param0, param1) && !canAttachTo(param3, param1, param5, param2) ? removeFace(param0, getFaceProperty(param1)) : param0;
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return this.shapesCache.get(param0);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        boolean var0 = false;

        for(Direction var1 : DIRECTIONS) {
            if (hasFace(param0, var1)) {
                BlockPos var2 = param2.relative(var1);
                if (!canAttachTo(param1, var1, var2, param1.getBlockState(var2))) {
                    return false;
                }

                var0 = true;
            }
        }

        return var0;
    }

    @Override
    public boolean canBeReplaced(BlockState param0, BlockPlaceContext param1) {
        return hasAnyVacantFace(param0);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        Level var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        BlockState var2 = var0.getBlockState(var1);
        return Arrays.stream(param0.getNearestLookingDirections())
            .map(param3 -> this.getStateForPlacement(var2, var0, var1, param3))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockState param0, LevelAccessor param1, BlockPos param2, Direction param3) {
        if (!this.isFaceSupported(param3)) {
            return null;
        } else {
            BlockState var0;
            if (param0.is(this)) {
                if (hasFace(param0, param3)) {
                    return null;
                }

                var0 = param0;
            } else if (this.isWaterloggable() && param0.getFluidState().isSource()) {
                var0 = this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true));
            } else {
                var0 = this.defaultBlockState();
            }

            BlockPos var3 = param2.relative(param3);
            return canAttachTo(param1, param3, var3, param1.getBlockState(var3)) ? var0.setValue(getFaceProperty(param3), Boolean.valueOf(true)) : null;
        }
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return !this.canRotate ? param0 : this.mapDirections(param0, param1::rotate);
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        if (param1 == Mirror.FRONT_BACK && !this.canMirrorX) {
            return param0;
        } else {
            return param1 == Mirror.LEFT_RIGHT && !this.canMirrorZ ? param0 : this.mapDirections(param0, param1::mirror);
        }
    }

    private BlockState mapDirections(BlockState param0, Function<Direction, Direction> param1) {
        BlockState var0 = param0;

        for(Direction var1 : DIRECTIONS) {
            if (this.isFaceSupported(var1)) {
                var0 = var0.setValue(getFaceProperty(param1.apply(var1)), param0.getValue(getFaceProperty(var1)));
            }
        }

        return var0;
    }

    public boolean spreadFromRandomFaceTowardRandomDirection(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        List<Direction> var0 = Lists.newArrayList(DIRECTIONS);
        Collections.shuffle(var0);
        return var0.stream()
            .filter(param1x -> hasFace(param0, param1x))
            .anyMatch(param4 -> this.spreadFromFaceTowardRandomDirection(param0, param1, param2, param4, param3));
    }

    public boolean spreadFromFaceTowardRandomDirection(BlockState param0, LevelAccessor param1, BlockPos param2, Direction param3, Random param4) {
        List<Direction> var0 = Arrays.asList(DIRECTIONS);
        Collections.shuffle(var0, param4);
        return var0.stream().anyMatch(param4x -> this.spreadFromFaceTowardDirection(param0, param1, param2, param3, param4x));
    }

    public boolean spreadFromFaceTowardDirection(BlockState param0, LevelAccessor param1, BlockPos param2, Direction param3, Direction param4) {
        if (param4.getAxis() == param3.getAxis() || !hasFace(param0, param3) || hasFace(param0, param4)) {
            return false;
        } else if (this.spreadToFace(param1, param2, param4)) {
            return true;
        } else {
            return this.spreadToFace(param1, param2.relative(param4), param3)
                ? true
                : this.spreadToFace(param1, param2.relative(param4).relative(param3), param4.getOpposite());
        }
    }

    private boolean spreadToFace(LevelAccessor param0, BlockPos param1, Direction param2) {
        BlockState var0 = param0.getBlockState(param1);
        if (!this.canSpreadInto(var0)) {
            return false;
        } else {
            BlockState var1 = this.getStateForPlacement(var0, param0, param1, param2);
            return var1 != null ? param0.setBlock(param1, var1, 2) : false;
        }
    }

    private boolean canSpreadInto(BlockState param0) {
        return param0.isAir() || param0.is(this) || param0.is(Blocks.WATER) && param0.getFluidState().isSource();
    }

    private static boolean hasFace(BlockState param0, Direction param1) {
        BooleanProperty var0 = getFaceProperty(param1);
        return param0.hasProperty(var0) && param0.getValue(var0);
    }

    private static boolean canAttachTo(BlockGetter param0, Direction param1, BlockPos param2, BlockState param3) {
        return Block.isFaceFull(param3.getCollisionShape(param0, param2), param1.getOpposite());
    }

    private boolean isWaterloggable() {
        return this.stateDefinition.getProperties().contains(BlockStateProperties.WATERLOGGED);
    }

    private static BlockState removeFace(BlockState param0, BooleanProperty param1) {
        BlockState var0 = param0.setValue(param1, Boolean.valueOf(false));
        if (hasAnyFace(var0)) {
            return var0;
        } else {
            return param0.hasProperty(BlockStateProperties.WATERLOGGED) && param0.getValue(BlockStateProperties.WATERLOGGED)
                ? Blocks.WATER.defaultBlockState()
                : Blocks.AIR.defaultBlockState();
        }
    }

    public static BooleanProperty getFaceProperty(Direction param0) {
        return PROPERTY_BY_DIRECTION.get(param0);
    }

    private static BlockState getDefaultMultifaceState(StateDefinition<Block, BlockState> param0) {
        BlockState var0 = param0.any();

        for(BooleanProperty var1 : PROPERTY_BY_DIRECTION.values()) {
            if (var0.hasProperty(var1)) {
                var0 = var0.setValue(var1, Boolean.valueOf(false));
            }
        }

        return var0;
    }

    private static ImmutableMap<BlockState, VoxelShape> getShapes(StateDefinition<Block, BlockState> param0) {
        Map<BlockState, VoxelShape> var0 = param0.getPossibleStates()
            .stream()
            .collect(Collectors.toMap(Function.identity(), MultifaceBlock::calculateMultifaceShape));
        return ImmutableMap.copyOf(var0);
    }

    private static VoxelShape calculateMultifaceShape(BlockState param0x) {
        VoxelShape var0x = Shapes.empty();

        for(Direction var1 : DIRECTIONS) {
            if (hasFace(param0x, var1)) {
                var0x = Shapes.or(var0x, SHAPE_BY_DIRECTION.get(var1));
            }
        }

        return var0x;
    }

    private static boolean hasAnyFace(BlockState param0) {
        return Arrays.stream(DIRECTIONS).anyMatch(param1 -> hasFace(param0, param1));
    }

    private static boolean hasAnyVacantFace(BlockState param0) {
        return Arrays.stream(DIRECTIONS).anyMatch(param1 -> !hasFace(param0, param1));
    }
}
