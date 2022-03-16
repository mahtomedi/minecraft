package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class MultifaceBlock extends Block {
    private static final float AABB_OFFSET = 1.0F;
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
    protected static final Direction[] DIRECTIONS = Direction.values();
    private final ImmutableMap<BlockState, VoxelShape> shapesCache;
    private final boolean canRotate;
    private final boolean canMirrorX;
    private final boolean canMirrorZ;

    public MultifaceBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(getDefaultMultifaceState(this.stateDefinition));
        this.shapesCache = this.getShapeForEachState(MultifaceBlock::calculateMultifaceShape);
        this.canRotate = Direction.Plane.HORIZONTAL.stream().allMatch(this::isFaceSupported);
        this.canMirrorX = Direction.Plane.HORIZONTAL.stream().filter(Direction.Axis.X).filter(this::isFaceSupported).count() % 2L == 0L;
        this.canMirrorZ = Direction.Plane.HORIZONTAL.stream().filter(Direction.Axis.Z).filter(this::isFaceSupported).count() % 2L == 0L;
    }

    public static Set<Direction> availableFaces(BlockState param0) {
        if (!(param0.getBlock() instanceof MultifaceBlock)) {
            return Set.of();
        } else {
            Set<Direction> var0 = EnumSet.noneOf(Direction.class);

            for(Direction var1 : Direction.values()) {
                if (hasFace(param0, var1)) {
                    var0.add(var1);
                }
            }

            return var0;
        }
    }

    @Nullable
    public static Set<Direction> unpack(byte param0) {
        if (param0 == -1) {
            return null;
        } else {
            Set<Direction> var0 = EnumSet.noneOf(Direction.class);

            for(Direction var1 : Direction.values()) {
                if ((param0 & (byte)(1 << var1.ordinal())) > 0) {
                    var0.add(var1);
                }
            }

            return var0;
        }
    }

    public static byte pack(@Nullable Collection<Direction> param0) {
        if (param0 == null) {
            return -1;
        } else {
            byte var0 = 0;

            for(Direction var1 : param0) {
                var0 = (byte)(var0 | 1 << var1.ordinal());
            }

            return var0;
        }
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
        if (!hasAnyFace(param0)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            return hasFace(param0, param1) && !canAttachTo(param3, param1, param5, param2) ? removeFace(param0, getFaceProperty(param1)) : param0;
        }
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

    public boolean isValidStateForPlacement(BlockGetter param0, BlockState param1, BlockPos param2, Direction param3) {
        if (this.isFaceSupported(param3) && (!param1.is(this) || !hasFace(param1, param3))) {
            BlockPos var0 = param2.relative(param3);
            return canAttachTo(param0, param3, var0, param0.getBlockState(var0));
        } else {
            return false;
        }
    }

    @Nullable
    public BlockState getStateForPlacement(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        if (!this.isValidStateForPlacement(param1, param0, param2, param3)) {
            return null;
        } else {
            BlockState var0;
            if (param0.is(this)) {
                var0 = param0;
            } else if (this.isWaterloggable() && param0.getFluidState().isSourceOfType(Fluids.WATER)) {
                var0 = this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true));
            } else {
                var0 = this.defaultBlockState();
            }

            return var0.setValue(getFaceProperty(param3), Boolean.valueOf(true));
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

    public static boolean hasFace(BlockState param0, Direction param1) {
        BooleanProperty var0 = getFaceProperty(param1);
        return param0.hasProperty(var0) && param0.getValue(var0);
    }

    protected static boolean canAttachTo(BlockGetter param0, Direction param1, BlockPos param2, BlockState param3) {
        return Block.isFaceFull(param3.getCollisionShape(param0, param2), param1.getOpposite());
    }

    private boolean isWaterloggable() {
        return this.stateDefinition.getProperties().contains(BlockStateProperties.WATERLOGGED);
    }

    private static BlockState removeFace(BlockState param0, BooleanProperty param1) {
        BlockState var0 = param0.setValue(param1, Boolean.valueOf(false));
        return hasAnyFace(var0) ? var0 : Blocks.AIR.defaultBlockState();
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

    private static VoxelShape calculateMultifaceShape(BlockState param0x) {
        VoxelShape var0 = Shapes.empty();

        for(Direction var1 : DIRECTIONS) {
            if (hasFace(param0x, var1)) {
                var0 = Shapes.or(var0, SHAPE_BY_DIRECTION.get(var1));
            }
        }

        return var0.isEmpty() ? Shapes.block() : var0;
    }

    protected static boolean hasAnyFace(BlockState param0) {
        return Arrays.stream(DIRECTIONS).anyMatch(param1 -> hasFace(param0, param1));
    }

    private static boolean hasAnyVacantFace(BlockState param0) {
        return Arrays.stream(DIRECTIONS).anyMatch(param1 -> !hasFace(param0, param1));
    }

    public abstract MultifaceSpreader getSpreader();
}
