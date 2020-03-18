package net.minecraft.world.level.block;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CrossCollisionBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION
        .entrySet()
        .stream()
        .filter(param0 -> param0.getKey().getAxis().isHorizontal())
        .collect(Util.toMap());
    protected final VoxelShape[] collisionShapeByIndex;
    protected final VoxelShape[] shapeByIndex;
    private final Object2IntMap<BlockState> stateToIndex = new Object2IntOpenHashMap<>();

    protected CrossCollisionBlock(float param0, float param1, float param2, float param3, float param4, BlockBehaviour.Properties param5) {
        super(param5);
        this.collisionShapeByIndex = this.makeShapes(param0, param1, param4, 0.0F, param4);
        this.shapeByIndex = this.makeShapes(param0, param1, param2, 0.0F, param3);
    }

    protected VoxelShape[] makeShapes(float param0, float param1, float param2, float param3, float param4) {
        float var0 = 8.0F - param0;
        float var1 = 8.0F + param0;
        float var2 = 8.0F - param1;
        float var3 = 8.0F + param1;
        VoxelShape var4 = Block.box((double)var0, 0.0, (double)var0, (double)var1, (double)param2, (double)var1);
        VoxelShape var5 = Block.box((double)var2, (double)param3, 0.0, (double)var3, (double)param4, (double)var3);
        VoxelShape var6 = Block.box((double)var2, (double)param3, (double)var2, (double)var3, (double)param4, 16.0);
        VoxelShape var7 = Block.box(0.0, (double)param3, (double)var2, (double)var3, (double)param4, (double)var3);
        VoxelShape var8 = Block.box((double)var2, (double)param3, (double)var2, 16.0, (double)param4, (double)var3);
        VoxelShape var9 = Shapes.or(var5, var8);
        VoxelShape var10 = Shapes.or(var6, var7);
        VoxelShape[] var11 = new VoxelShape[]{
            Shapes.empty(),
            var6,
            var7,
            var10,
            var5,
            Shapes.or(var6, var5),
            Shapes.or(var7, var5),
            Shapes.or(var10, var5),
            var8,
            Shapes.or(var6, var8),
            Shapes.or(var7, var8),
            Shapes.or(var10, var8),
            var9,
            Shapes.or(var6, var9),
            Shapes.or(var7, var9),
            Shapes.or(var10, var9)
        };

        for(int var12 = 0; var12 < 16; ++var12) {
            var11[var12] = Shapes.or(var4, var11[var12]);
        }

        return var11;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState param0, BlockGetter param1, BlockPos param2) {
        return !param0.getValue(WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return this.shapeByIndex[this.getAABBIndex(param0)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return this.collisionShapeByIndex[this.getAABBIndex(param0)];
    }

    private static int indexFor(Direction param0) {
        return 1 << param0.get2DDataValue();
    }

    protected int getAABBIndex(BlockState param0) {
        return this.stateToIndex.computeIntIfAbsent(param0, param0x -> {
            int var0 = 0;
            if (param0x.getValue(NORTH)) {
                var0 |= indexFor(Direction.NORTH);
            }

            if (param0x.getValue(EAST)) {
                var0 |= indexFor(Direction.EAST);
            }

            if (param0x.getValue(SOUTH)) {
                var0 |= indexFor(Direction.SOUTH);
            }

            if (param0x.getValue(WEST)) {
                var0 |= indexFor(Direction.WEST);
            }

            return var0;
        });
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
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
}
