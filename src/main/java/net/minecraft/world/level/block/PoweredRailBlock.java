package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;

public class PoweredRailBlock extends BaseRailBlock {
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    protected PoweredRailBlock(BlockBehaviour.Properties param0) {
        super(true, param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(POWERED, Boolean.valueOf(false)));
    }

    protected boolean findPoweredRailSignal(Level param0, BlockPos param1, BlockState param2, boolean param3, int param4) {
        if (param4 >= 8) {
            return false;
        } else {
            int var0 = param1.getX();
            int var1 = param1.getY();
            int var2 = param1.getZ();
            boolean var3 = true;
            RailShape var4 = param2.getValue(SHAPE);
            switch(var4) {
                case NORTH_SOUTH:
                    if (param3) {
                        ++var2;
                    } else {
                        --var2;
                    }
                    break;
                case EAST_WEST:
                    if (param3) {
                        --var0;
                    } else {
                        ++var0;
                    }
                    break;
                case ASCENDING_EAST:
                    if (param3) {
                        --var0;
                    } else {
                        ++var0;
                        ++var1;
                        var3 = false;
                    }

                    var4 = RailShape.EAST_WEST;
                    break;
                case ASCENDING_WEST:
                    if (param3) {
                        --var0;
                        ++var1;
                        var3 = false;
                    } else {
                        ++var0;
                    }

                    var4 = RailShape.EAST_WEST;
                    break;
                case ASCENDING_NORTH:
                    if (param3) {
                        ++var2;
                    } else {
                        --var2;
                        ++var1;
                        var3 = false;
                    }

                    var4 = RailShape.NORTH_SOUTH;
                    break;
                case ASCENDING_SOUTH:
                    if (param3) {
                        ++var2;
                        ++var1;
                        var3 = false;
                    } else {
                        --var2;
                    }

                    var4 = RailShape.NORTH_SOUTH;
            }

            if (this.isSameRailWithPower(param0, new BlockPos(var0, var1, var2), param3, param4, var4)) {
                return true;
            } else {
                return var3 && this.isSameRailWithPower(param0, new BlockPos(var0, var1 - 1, var2), param3, param4, var4);
            }
        }
    }

    protected boolean isSameRailWithPower(Level param0, BlockPos param1, boolean param2, int param3, RailShape param4) {
        BlockState var0 = param0.getBlockState(param1);
        if (var0.getBlock() != this) {
            return false;
        } else {
            RailShape var1 = var0.getValue(SHAPE);
            if (param4 != RailShape.EAST_WEST || var1 != RailShape.NORTH_SOUTH && var1 != RailShape.ASCENDING_NORTH && var1 != RailShape.ASCENDING_SOUTH) {
                if (param4 != RailShape.NORTH_SOUTH || var1 != RailShape.EAST_WEST && var1 != RailShape.ASCENDING_EAST && var1 != RailShape.ASCENDING_WEST) {
                    if (!var0.getValue(POWERED)) {
                        return false;
                    } else {
                        return param0.hasNeighborSignal(param1) ? true : this.findPoweredRailSignal(param0, param1, var0, param2, param3 + 1);
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    @Override
    protected void updateState(BlockState param0, Level param1, BlockPos param2, Block param3) {
        boolean var0 = param0.getValue(POWERED);
        boolean var1 = param1.hasNeighborSignal(param2)
            || this.findPoweredRailSignal(param1, param2, param0, true, 0)
            || this.findPoweredRailSignal(param1, param2, param0, false, 0);
        if (var1 != var0) {
            param1.setBlock(param2, param0.setValue(POWERED, Boolean.valueOf(var1)), 3);
            param1.updateNeighborsAt(param2.below(), this);
            if (param0.getValue(SHAPE).isAscending()) {
                param1.updateNeighborsAt(param2.above(), this);
            }
        }

    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        switch(param1) {
            case CLOCKWISE_180:
                switch((RailShape)param0.getValue(SHAPE)) {
                    case ASCENDING_EAST:
                        return param0.setValue(SHAPE, RailShape.ASCENDING_WEST);
                    case ASCENDING_WEST:
                        return param0.setValue(SHAPE, RailShape.ASCENDING_EAST);
                    case ASCENDING_NORTH:
                        return param0.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
                    case ASCENDING_SOUTH:
                        return param0.setValue(SHAPE, RailShape.ASCENDING_NORTH);
                    case SOUTH_EAST:
                        return param0.setValue(SHAPE, RailShape.NORTH_WEST);
                    case SOUTH_WEST:
                        return param0.setValue(SHAPE, RailShape.NORTH_EAST);
                    case NORTH_WEST:
                        return param0.setValue(SHAPE, RailShape.SOUTH_EAST);
                    case NORTH_EAST:
                        return param0.setValue(SHAPE, RailShape.SOUTH_WEST);
                }
            case COUNTERCLOCKWISE_90:
                switch((RailShape)param0.getValue(SHAPE)) {
                    case NORTH_SOUTH:
                        return param0.setValue(SHAPE, RailShape.EAST_WEST);
                    case EAST_WEST:
                        return param0.setValue(SHAPE, RailShape.NORTH_SOUTH);
                    case ASCENDING_EAST:
                        return param0.setValue(SHAPE, RailShape.ASCENDING_NORTH);
                    case ASCENDING_WEST:
                        return param0.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
                    case ASCENDING_NORTH:
                        return param0.setValue(SHAPE, RailShape.ASCENDING_WEST);
                    case ASCENDING_SOUTH:
                        return param0.setValue(SHAPE, RailShape.ASCENDING_EAST);
                    case SOUTH_EAST:
                        return param0.setValue(SHAPE, RailShape.NORTH_EAST);
                    case SOUTH_WEST:
                        return param0.setValue(SHAPE, RailShape.SOUTH_EAST);
                    case NORTH_WEST:
                        return param0.setValue(SHAPE, RailShape.SOUTH_WEST);
                    case NORTH_EAST:
                        return param0.setValue(SHAPE, RailShape.NORTH_WEST);
                }
            case CLOCKWISE_90:
                switch((RailShape)param0.getValue(SHAPE)) {
                    case NORTH_SOUTH:
                        return param0.setValue(SHAPE, RailShape.EAST_WEST);
                    case EAST_WEST:
                        return param0.setValue(SHAPE, RailShape.NORTH_SOUTH);
                    case ASCENDING_EAST:
                        return param0.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
                    case ASCENDING_WEST:
                        return param0.setValue(SHAPE, RailShape.ASCENDING_NORTH);
                    case ASCENDING_NORTH:
                        return param0.setValue(SHAPE, RailShape.ASCENDING_EAST);
                    case ASCENDING_SOUTH:
                        return param0.setValue(SHAPE, RailShape.ASCENDING_WEST);
                    case SOUTH_EAST:
                        return param0.setValue(SHAPE, RailShape.SOUTH_WEST);
                    case SOUTH_WEST:
                        return param0.setValue(SHAPE, RailShape.NORTH_WEST);
                    case NORTH_WEST:
                        return param0.setValue(SHAPE, RailShape.NORTH_EAST);
                    case NORTH_EAST:
                        return param0.setValue(SHAPE, RailShape.SOUTH_EAST);
                }
            default:
                return param0;
        }
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
        param0.add(SHAPE, POWERED);
    }
}
