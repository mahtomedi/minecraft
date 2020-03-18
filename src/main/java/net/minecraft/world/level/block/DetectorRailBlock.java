package net.minecraft.world.level.block;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;

public class DetectorRailBlock extends BaseRailBlock {
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public DetectorRailBlock(BlockBehaviour.Properties param0) {
        super(true, param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, Boolean.valueOf(false)).setValue(SHAPE, RailShape.NORTH_SOUTH));
    }

    @Override
    public boolean isSignalSource(BlockState param0) {
        return true;
    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (!param1.isClientSide) {
            if (!param0.getValue(POWERED)) {
                this.checkPressed(param1, param2, param0);
            }
        }
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (param0.getValue(POWERED)) {
            this.checkPressed(param1, param2, param0);
        }
    }

    @Override
    public int getSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return param0.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        if (!param0.getValue(POWERED)) {
            return 0;
        } else {
            return param3 == Direction.UP ? 15 : 0;
        }
    }

    private void checkPressed(Level param0, BlockPos param1, BlockState param2) {
        boolean var0 = param2.getValue(POWERED);
        boolean var1 = false;
        List<AbstractMinecart> var2 = this.getInteractingMinecartOfType(param0, param1, AbstractMinecart.class, null);
        if (!var2.isEmpty()) {
            var1 = true;
        }

        if (var1 && !var0) {
            BlockState var3 = param2.setValue(POWERED, Boolean.valueOf(true));
            param0.setBlock(param1, var3, 3);
            this.updatePowerToConnected(param0, param1, var3, true);
            param0.updateNeighborsAt(param1, this);
            param0.updateNeighborsAt(param1.below(), this);
            param0.setBlocksDirty(param1, param2, var3);
        }

        if (!var1 && var0) {
            BlockState var4 = param2.setValue(POWERED, Boolean.valueOf(false));
            param0.setBlock(param1, var4, 3);
            this.updatePowerToConnected(param0, param1, var4, false);
            param0.updateNeighborsAt(param1, this);
            param0.updateNeighborsAt(param1.below(), this);
            param0.setBlocksDirty(param1, param2, var4);
        }

        if (var1) {
            param0.getBlockTicks().scheduleTick(param1, this, 20);
        }

        param0.updateNeighbourForOutputSignal(param1, this);
    }

    protected void updatePowerToConnected(Level param0, BlockPos param1, BlockState param2, boolean param3) {
        RailState var0 = new RailState(param0, param1, param2);

        for(BlockPos var2 : var0.getConnections()) {
            BlockState var3 = param0.getBlockState(var2);
            var3.neighborChanged(param0, var2, var3.getBlock(), param1, false);
        }

    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (param3.getBlock() != param0.getBlock()) {
            this.checkPressed(param1, param2, this.updateState(param0, param1, param2, param4));
        }
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState param0) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        if (param0.getValue(POWERED)) {
            List<MinecartCommandBlock> var0 = this.getInteractingMinecartOfType(param1, param2, MinecartCommandBlock.class, null);
            if (!var0.isEmpty()) {
                return var0.get(0).getCommandBlock().getSuccessCount();
            }

            List<AbstractMinecart> var1 = this.getInteractingMinecartOfType(param1, param2, AbstractMinecart.class, EntitySelector.CONTAINER_ENTITY_SELECTOR);
            if (!var1.isEmpty()) {
                return AbstractContainerMenu.getRedstoneSignalFromContainer((Container)var1.get(0));
            }
        }

        return 0;
    }

    protected <T extends AbstractMinecart> List<T> getInteractingMinecartOfType(
        Level param0, BlockPos param1, Class<T> param2, @Nullable Predicate<Entity> param3
    ) {
        return param0.getEntitiesOfClass(param2, this.getSearchBB(param1), param3);
    }

    private AABB getSearchBB(BlockPos param0) {
        float var0 = 0.2F;
        return new AABB(
            (double)((float)param0.getX() + 0.2F),
            (double)param0.getY(),
            (double)((float)param0.getZ() + 0.2F),
            (double)((float)(param0.getX() + 1) - 0.2F),
            (double)((float)(param0.getY() + 1) - 0.2F),
            (double)((float)(param0.getZ() + 1) - 0.2F)
        );
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
                    case NORTH_SOUTH:
                        return param0.setValue(SHAPE, RailShape.EAST_WEST);
                    case EAST_WEST:
                        return param0.setValue(SHAPE, RailShape.NORTH_SOUTH);
                }
            case CLOCKWISE_90:
                switch((RailShape)param0.getValue(SHAPE)) {
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
                    case NORTH_SOUTH:
                        return param0.setValue(SHAPE, RailShape.EAST_WEST);
                    case EAST_WEST:
                        return param0.setValue(SHAPE, RailShape.NORTH_SOUTH);
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
