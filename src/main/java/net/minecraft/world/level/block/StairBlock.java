package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StairBlock extends Block implements SimpleWaterloggedBlock {
    public static final MapCodec<StairBlock> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(BlockState.CODEC.fieldOf("base_state").forGetter(param0x -> param0x.baseState), propertiesCodec())
                .apply(param0, StairBlock::new)
    );
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape TOP_AABB = SlabBlock.TOP_AABB;
    protected static final VoxelShape BOTTOM_AABB = SlabBlock.BOTTOM_AABB;
    protected static final VoxelShape OCTET_NNN = Block.box(0.0, 0.0, 0.0, 8.0, 8.0, 8.0);
    protected static final VoxelShape OCTET_NNP = Block.box(0.0, 0.0, 8.0, 8.0, 8.0, 16.0);
    protected static final VoxelShape OCTET_NPN = Block.box(0.0, 8.0, 0.0, 8.0, 16.0, 8.0);
    protected static final VoxelShape OCTET_NPP = Block.box(0.0, 8.0, 8.0, 8.0, 16.0, 16.0);
    protected static final VoxelShape OCTET_PNN = Block.box(8.0, 0.0, 0.0, 16.0, 8.0, 8.0);
    protected static final VoxelShape OCTET_PNP = Block.box(8.0, 0.0, 8.0, 16.0, 8.0, 16.0);
    protected static final VoxelShape OCTET_PPN = Block.box(8.0, 8.0, 0.0, 16.0, 16.0, 8.0);
    protected static final VoxelShape OCTET_PPP = Block.box(8.0, 8.0, 8.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape[] TOP_SHAPES = makeShapes(TOP_AABB, OCTET_NNN, OCTET_PNN, OCTET_NNP, OCTET_PNP);
    protected static final VoxelShape[] BOTTOM_SHAPES = makeShapes(BOTTOM_AABB, OCTET_NPN, OCTET_PPN, OCTET_NPP, OCTET_PPP);
    private static final int[] SHAPE_BY_STATE = new int[]{12, 5, 3, 10, 14, 13, 7, 11, 13, 7, 11, 14, 8, 4, 1, 2, 4, 1, 2, 8};
    private final Block base;
    protected final BlockState baseState;

    @Override
    public MapCodec<? extends StairBlock> codec() {
        return CODEC;
    }

    private static VoxelShape[] makeShapes(VoxelShape param0, VoxelShape param1, VoxelShape param2, VoxelShape param3, VoxelShape param4) {
        return IntStream.range(0, 16)
            .mapToObj(param5 -> makeStairShape(param5, param0, param1, param2, param3, param4))
            .toArray(param0x -> new VoxelShape[param0x]);
    }

    private static VoxelShape makeStairShape(int param0, VoxelShape param1, VoxelShape param2, VoxelShape param3, VoxelShape param4, VoxelShape param5) {
        VoxelShape var0 = param1;
        if ((param0 & 1) != 0) {
            var0 = Shapes.or(param1, param2);
        }

        if ((param0 & 2) != 0) {
            var0 = Shapes.or(var0, param3);
        }

        if ((param0 & 4) != 0) {
            var0 = Shapes.or(var0, param4);
        }

        if ((param0 & 8) != 0) {
            var0 = Shapes.or(var0, param5);
        }

        return var0;
    }

    protected StairBlock(BlockState param0, BlockBehaviour.Properties param1) {
        super(param1);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, Half.BOTTOM)
                .setValue(SHAPE, StairsShape.STRAIGHT)
                .setValue(WATERLOGGED, Boolean.valueOf(false))
        );
        this.base = param0.getBlock();
        this.baseState = param0;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState param0) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return (param0.getValue(HALF) == Half.TOP ? TOP_SHAPES : BOTTOM_SHAPES)[SHAPE_BY_STATE[this.getShapeIndex(param0)]];
    }

    private int getShapeIndex(BlockState param0) {
        return param0.getValue(SHAPE).ordinal() * 4 + param0.getValue(FACING).get2DDataValue();
    }

    @Override
    public float getExplosionResistance() {
        return this.base.getExplosionResistance();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        Direction var0 = param0.getClickedFace();
        BlockPos var1 = param0.getClickedPos();
        FluidState var2 = param0.getLevel().getFluidState(var1);
        BlockState var3 = this.defaultBlockState()
            .setValue(FACING, param0.getHorizontalDirection())
            .setValue(
                HALF, var0 != Direction.DOWN && (var0 == Direction.UP || !(param0.getClickLocation().y - (double)var1.getY() > 0.5)) ? Half.BOTTOM : Half.TOP
            )
            .setValue(WATERLOGGED, Boolean.valueOf(var2.getType() == Fluids.WATER));
        return var3.setValue(SHAPE, getStairsShape(var3, param0.getLevel(), var1));
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return param1.getAxis().isHorizontal()
            ? param0.setValue(SHAPE, getStairsShape(param0, param3, param4))
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    private static StairsShape getStairsShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        Direction var0 = param0.getValue(FACING);
        BlockState var1 = param1.getBlockState(param2.relative(var0));
        if (isStairs(var1) && param0.getValue(HALF) == var1.getValue(HALF)) {
            Direction var2 = var1.getValue(FACING);
            if (var2.getAxis() != param0.getValue(FACING).getAxis() && canTakeShape(param0, param1, param2, var2.getOpposite())) {
                if (var2 == var0.getCounterClockWise()) {
                    return StairsShape.OUTER_LEFT;
                }

                return StairsShape.OUTER_RIGHT;
            }
        }

        BlockState var3 = param1.getBlockState(param2.relative(var0.getOpposite()));
        if (isStairs(var3) && param0.getValue(HALF) == var3.getValue(HALF)) {
            Direction var4 = var3.getValue(FACING);
            if (var4.getAxis() != param0.getValue(FACING).getAxis() && canTakeShape(param0, param1, param2, var4)) {
                if (var4 == var0.getCounterClockWise()) {
                    return StairsShape.INNER_LEFT;
                }

                return StairsShape.INNER_RIGHT;
            }
        }

        return StairsShape.STRAIGHT;
    }

    private static boolean canTakeShape(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        BlockState var0 = param1.getBlockState(param2.relative(param3));
        return !isStairs(var0) || var0.getValue(FACING) != param0.getValue(FACING) || var0.getValue(HALF) != param0.getValue(HALF);
    }

    public static boolean isStairs(BlockState param0) {
        return param0.getBlock() instanceof StairBlock;
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0.setValue(FACING, param1.rotate(param0.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        Direction var0 = param0.getValue(FACING);
        StairsShape var1 = param0.getValue(SHAPE);
        switch(param1) {
            case LEFT_RIGHT:
                if (var0.getAxis() == Direction.Axis.Z) {
                    switch(var1) {
                        case INNER_LEFT:
                            return param0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
                        case INNER_RIGHT:
                            return param0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
                        case OUTER_LEFT:
                            return param0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
                        case OUTER_RIGHT:
                            return param0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
                        default:
                            return param0.rotate(Rotation.CLOCKWISE_180);
                    }
                }
                break;
            case FRONT_BACK:
                if (var0.getAxis() == Direction.Axis.X) {
                    switch(var1) {
                        case INNER_LEFT:
                            return param0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
                        case INNER_RIGHT:
                            return param0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
                        case OUTER_LEFT:
                            return param0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
                        case OUTER_RIGHT:
                            return param0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
                        case STRAIGHT:
                            return param0.rotate(Rotation.CLOCKWISE_180);
                    }
                }
        }

        return super.mirror(param0, param1);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING, HALF, SHAPE, WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}
