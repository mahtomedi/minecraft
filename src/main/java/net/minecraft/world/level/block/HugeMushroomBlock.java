package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class HugeMushroomBlock extends Block {
    public static final MapCodec<HugeMushroomBlock> CODEC = simpleCodec(HugeMushroomBlock::new);
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    public static final BooleanProperty UP = PipeBlock.UP;
    public static final BooleanProperty DOWN = PipeBlock.DOWN;
    private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION;

    @Override
    public MapCodec<HugeMushroomBlock> codec() {
        return CODEC;
    }

    public HugeMushroomBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(NORTH, Boolean.valueOf(true))
                .setValue(EAST, Boolean.valueOf(true))
                .setValue(SOUTH, Boolean.valueOf(true))
                .setValue(WEST, Boolean.valueOf(true))
                .setValue(UP, Boolean.valueOf(true))
                .setValue(DOWN, Boolean.valueOf(true))
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockGetter var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        return this.defaultBlockState()
            .setValue(DOWN, Boolean.valueOf(!var0.getBlockState(var1.below()).is(this)))
            .setValue(UP, Boolean.valueOf(!var0.getBlockState(var1.above()).is(this)))
            .setValue(NORTH, Boolean.valueOf(!var0.getBlockState(var1.north()).is(this)))
            .setValue(EAST, Boolean.valueOf(!var0.getBlockState(var1.east()).is(this)))
            .setValue(SOUTH, Boolean.valueOf(!var0.getBlockState(var1.south()).is(this)))
            .setValue(WEST, Boolean.valueOf(!var0.getBlockState(var1.west()).is(this)));
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return param2.is(this)
            ? param0.setValue(PROPERTY_BY_DIRECTION.get(param1), Boolean.valueOf(false))
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0.setValue(PROPERTY_BY_DIRECTION.get(param1.rotate(Direction.NORTH)), param0.getValue(NORTH))
            .setValue(PROPERTY_BY_DIRECTION.get(param1.rotate(Direction.SOUTH)), param0.getValue(SOUTH))
            .setValue(PROPERTY_BY_DIRECTION.get(param1.rotate(Direction.EAST)), param0.getValue(EAST))
            .setValue(PROPERTY_BY_DIRECTION.get(param1.rotate(Direction.WEST)), param0.getValue(WEST))
            .setValue(PROPERTY_BY_DIRECTION.get(param1.rotate(Direction.UP)), param0.getValue(UP))
            .setValue(PROPERTY_BY_DIRECTION.get(param1.rotate(Direction.DOWN)), param0.getValue(DOWN));
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        return param0.setValue(PROPERTY_BY_DIRECTION.get(param1.mirror(Direction.NORTH)), param0.getValue(NORTH))
            .setValue(PROPERTY_BY_DIRECTION.get(param1.mirror(Direction.SOUTH)), param0.getValue(SOUTH))
            .setValue(PROPERTY_BY_DIRECTION.get(param1.mirror(Direction.EAST)), param0.getValue(EAST))
            .setValue(PROPERTY_BY_DIRECTION.get(param1.mirror(Direction.WEST)), param0.getValue(WEST))
            .setValue(PROPERTY_BY_DIRECTION.get(param1.mirror(Direction.UP)), param0.getValue(UP))
            .setValue(PROPERTY_BY_DIRECTION.get(param1.mirror(Direction.DOWN)), param0.getValue(DOWN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(UP, DOWN, NORTH, EAST, SOUTH, WEST);
    }
}
