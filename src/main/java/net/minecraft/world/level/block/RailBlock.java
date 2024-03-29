package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;

public class RailBlock extends BaseRailBlock {
    public static final MapCodec<RailBlock> CODEC = simpleCodec(RailBlock::new);
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE;

    @Override
    public MapCodec<RailBlock> codec() {
        return CODEC;
    }

    protected RailBlock(BlockBehaviour.Properties param0) {
        super(false, param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    @Override
    protected void updateState(BlockState param0, Level param1, BlockPos param2, Block param3) {
        if (param3.defaultBlockState().isSignalSource() && new RailState(param1, param2, param0).countPotentialConnections() == 3) {
            this.updateDir(param1, param2, param0, false);
        }

    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        RailShape var0 = param0.getValue(SHAPE);

        return param0.setValue(SHAPE, switch(param1) {
            case CLOCKWISE_180 -> {
                switch(var0) {
                    case NORTH_SOUTH:
                        yield RailShape.NORTH_SOUTH;
                    case EAST_WEST:
                        yield RailShape.EAST_WEST;
                    case ASCENDING_EAST:
                        yield RailShape.ASCENDING_WEST;
                    case ASCENDING_WEST:
                        yield RailShape.ASCENDING_EAST;
                    case ASCENDING_NORTH:
                        yield RailShape.ASCENDING_SOUTH;
                    case ASCENDING_SOUTH:
                        yield RailShape.ASCENDING_NORTH;
                    case SOUTH_EAST:
                        yield RailShape.NORTH_WEST;
                    case SOUTH_WEST:
                        yield RailShape.NORTH_EAST;
                    case NORTH_WEST:
                        yield RailShape.SOUTH_EAST;
                    case NORTH_EAST:
                        yield RailShape.SOUTH_WEST;
                    default:
                        throw new IncompatibleClassChangeError();
                }
            }
            case COUNTERCLOCKWISE_90 -> {
                switch(var0) {
                    case NORTH_SOUTH:
                        yield RailShape.EAST_WEST;
                    case EAST_WEST:
                        yield RailShape.NORTH_SOUTH;
                    case ASCENDING_EAST:
                        yield RailShape.ASCENDING_NORTH;
                    case ASCENDING_WEST:
                        yield RailShape.ASCENDING_SOUTH;
                    case ASCENDING_NORTH:
                        yield RailShape.ASCENDING_WEST;
                    case ASCENDING_SOUTH:
                        yield RailShape.ASCENDING_EAST;
                    case SOUTH_EAST:
                        yield RailShape.NORTH_EAST;
                    case SOUTH_WEST:
                        yield RailShape.SOUTH_EAST;
                    case NORTH_WEST:
                        yield RailShape.SOUTH_WEST;
                    case NORTH_EAST:
                        yield RailShape.NORTH_WEST;
                    default:
                        throw new IncompatibleClassChangeError();
                }
            }
            case CLOCKWISE_90 -> {
                switch(var0) {
                    case NORTH_SOUTH:
                        yield RailShape.EAST_WEST;
                    case EAST_WEST:
                        yield RailShape.NORTH_SOUTH;
                    case ASCENDING_EAST:
                        yield RailShape.ASCENDING_SOUTH;
                    case ASCENDING_WEST:
                        yield RailShape.ASCENDING_NORTH;
                    case ASCENDING_NORTH:
                        yield RailShape.ASCENDING_EAST;
                    case ASCENDING_SOUTH:
                        yield RailShape.ASCENDING_WEST;
                    case SOUTH_EAST:
                        yield RailShape.SOUTH_WEST;
                    case SOUTH_WEST:
                        yield RailShape.NORTH_WEST;
                    case NORTH_WEST:
                        yield RailShape.NORTH_EAST;
                    case NORTH_EAST:
                        yield RailShape.SOUTH_EAST;
                    default:
                        throw new IncompatibleClassChangeError();
                }
            }
            default -> var0;
        });
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        RailShape var0 = param0.getValue(SHAPE);
        switch(param1) {
            case LEFT_RIGHT:
                switch(var0) {
                    case ASCENDING_NORTH:
                        return param0.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
                    case ASCENDING_SOUTH:
                        return param0.setValue(SHAPE, RailShape.ASCENDING_NORTH);
                    case SOUTH_EAST:
                        return param0.setValue(SHAPE, RailShape.NORTH_EAST);
                    case SOUTH_WEST:
                        return param0.setValue(SHAPE, RailShape.NORTH_WEST);
                    case NORTH_WEST:
                        return param0.setValue(SHAPE, RailShape.SOUTH_WEST);
                    case NORTH_EAST:
                        return param0.setValue(SHAPE, RailShape.SOUTH_EAST);
                    default:
                        return super.mirror(param0, param1);
                }
            case FRONT_BACK:
                switch(var0) {
                    case ASCENDING_EAST:
                        return param0.setValue(SHAPE, RailShape.ASCENDING_WEST);
                    case ASCENDING_WEST:
                        return param0.setValue(SHAPE, RailShape.ASCENDING_EAST);
                    case ASCENDING_NORTH:
                    case ASCENDING_SOUTH:
                    default:
                        break;
                    case SOUTH_EAST:
                        return param0.setValue(SHAPE, RailShape.SOUTH_WEST);
                    case SOUTH_WEST:
                        return param0.setValue(SHAPE, RailShape.SOUTH_EAST);
                    case NORTH_WEST:
                        return param0.setValue(SHAPE, RailShape.NORTH_EAST);
                    case NORTH_EAST:
                        return param0.setValue(SHAPE, RailShape.NORTH_WEST);
                }
        }

        return super.mirror(param0, param1);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(SHAPE, WATERLOGGED);
    }
}
