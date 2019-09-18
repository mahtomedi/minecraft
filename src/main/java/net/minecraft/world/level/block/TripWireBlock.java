package net.minecraft.world.level.block;

import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TripWireBlock extends Block {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    public static final BooleanProperty DISARMED = BlockStateProperties.DISARMED;
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = CrossCollisionBlock.PROPERTY_BY_DIRECTION;
    protected static final VoxelShape AABB = Block.box(0.0, 1.0, 0.0, 16.0, 2.5, 16.0);
    protected static final VoxelShape NOT_ATTACHED_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    private final TripWireHookBlock hook;

    public TripWireBlock(TripWireHookBlock param0, Block.Properties param1) {
        super(param1);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(POWERED, Boolean.valueOf(false))
                .setValue(ATTACHED, Boolean.valueOf(false))
                .setValue(DISARMED, Boolean.valueOf(false))
                .setValue(NORTH, Boolean.valueOf(false))
                .setValue(EAST, Boolean.valueOf(false))
                .setValue(SOUTH, Boolean.valueOf(false))
                .setValue(WEST, Boolean.valueOf(false))
        );
        this.hook = param0;
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return param0.getValue(ATTACHED) ? AABB : NOT_ATTACHED_AABB;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockGetter var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        return this.defaultBlockState()
            .setValue(NORTH, Boolean.valueOf(this.shouldConnectTo(var0.getBlockState(var1.north()), Direction.NORTH)))
            .setValue(EAST, Boolean.valueOf(this.shouldConnectTo(var0.getBlockState(var1.east()), Direction.EAST)))
            .setValue(SOUTH, Boolean.valueOf(this.shouldConnectTo(var0.getBlockState(var1.south()), Direction.SOUTH)))
            .setValue(WEST, Boolean.valueOf(this.shouldConnectTo(var0.getBlockState(var1.west()), Direction.WEST)));
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return param1.getAxis().isHorizontal()
            ? param0.setValue(PROPERTY_BY_DIRECTION.get(param1), Boolean.valueOf(this.shouldConnectTo(param2, param1)))
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (param3.getBlock() != param0.getBlock()) {
            this.updateSource(param1, param2, param0);
        }
    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param4 && param0.getBlock() != param3.getBlock()) {
            this.updateSource(param1, param2, param0.setValue(POWERED, Boolean.valueOf(true)));
        }
    }

    @Override
    public void playerWillDestroy(Level param0, BlockPos param1, BlockState param2, Player param3) {
        if (!param0.isClientSide && !param3.getMainHandItem().isEmpty() && param3.getMainHandItem().getItem() == Items.SHEARS) {
            param0.setBlock(param1, param2.setValue(DISARMED, Boolean.valueOf(true)), 4);
        }

        super.playerWillDestroy(param0, param1, param2, param3);
    }

    private void updateSource(Level param0, BlockPos param1, BlockState param2) {
        for(Direction var0 : new Direction[]{Direction.SOUTH, Direction.WEST}) {
            for(int var1 = 1; var1 < 42; ++var1) {
                BlockPos var2 = param1.relative(var0, var1);
                BlockState var3 = param0.getBlockState(var2);
                if (var3.getBlock() == this.hook) {
                    if (var3.getValue(TripWireHookBlock.FACING) == var0.getOpposite()) {
                        this.hook.calculateState(param0, var2, var3, false, true, var1, param2);
                    }
                    break;
                }

                if (var3.getBlock() != this) {
                    break;
                }
            }
        }

    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (!param1.isClientSide) {
            if (!param0.getValue(POWERED)) {
                this.checkPressed(param1, param2);
            }
        }
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (param1.getBlockState(param2).getValue(POWERED)) {
            this.checkPressed(param1, param2);
        }
    }

    private void checkPressed(Level param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1);
        boolean var1 = var0.getValue(POWERED);
        boolean var2 = false;
        List<? extends Entity> var3 = param0.getEntities(null, var0.getShape(param0, param1).bounds().move(param1));
        if (!var3.isEmpty()) {
            for(Entity var4 : var3) {
                if (!var4.isIgnoringBlockTriggers()) {
                    var2 = true;
                    break;
                }
            }
        }

        if (var2 != var1) {
            var0 = var0.setValue(POWERED, Boolean.valueOf(var2));
            param0.setBlock(param1, var0, 3);
            this.updateSource(param0, param1, var0);
        }

        if (var2) {
            param0.getBlockTicks().scheduleTick(new BlockPos(param1), this, this.getTickDelay(param0));
        }

    }

    public boolean shouldConnectTo(BlockState param0, Direction param1) {
        Block var0 = param0.getBlock();
        if (var0 == this.hook) {
            return param0.getValue(TripWireHookBlock.FACING) == param1.getOpposite();
        } else {
            return var0 == this;
        }
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

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(POWERED, ATTACHED, DISARMED, NORTH, EAST, WEST, SOUTH);
    }
}
