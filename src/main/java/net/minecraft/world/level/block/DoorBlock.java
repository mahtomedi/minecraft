package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DoorBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
    protected static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape WEST_AABB = Block.box(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape EAST_AABB = Block.box(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);

    protected DoorBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, Boolean.valueOf(false))
                .setValue(HINGE, DoorHingeSide.LEFT)
                .setValue(POWERED, Boolean.valueOf(false))
                .setValue(HALF, DoubleBlockHalf.LOWER)
        );
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        Direction var0 = param0.getValue(FACING);
        boolean var1 = !param0.getValue(OPEN);
        boolean var2 = param0.getValue(HINGE) == DoorHingeSide.RIGHT;
        switch(var0) {
            case EAST:
            default:
                return var1 ? EAST_AABB : (var2 ? NORTH_AABB : SOUTH_AABB);
            case SOUTH:
                return var1 ? SOUTH_AABB : (var2 ? EAST_AABB : WEST_AABB);
            case WEST:
                return var1 ? WEST_AABB : (var2 ? SOUTH_AABB : NORTH_AABB);
            case NORTH:
                return var1 ? NORTH_AABB : (var2 ? WEST_AABB : EAST_AABB);
        }
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        DoubleBlockHalf var0 = param0.getValue(HALF);
        if (param1.getAxis() != Direction.Axis.Y || var0 == DoubleBlockHalf.LOWER != (param1 == Direction.UP)) {
            return var0 == DoubleBlockHalf.LOWER && param1 == Direction.DOWN && !param0.canSurvive(param3, param4)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(param0, param1, param2, param3, param4, param5);
        } else {
            return param2.is(this) && param2.getValue(HALF) != var0
                ? param0.setValue(FACING, param2.getValue(FACING))
                    .setValue(OPEN, param2.getValue(OPEN))
                    .setValue(HINGE, param2.getValue(HINGE))
                    .setValue(POWERED, param2.getValue(POWERED))
                : Blocks.AIR.defaultBlockState();
        }
    }

    @Override
    public void playerDestroy(Level param0, Player param1, BlockPos param2, BlockState param3, @Nullable BlockEntity param4, ItemStack param5) {
        super.playerDestroy(param0, param1, param2, Blocks.AIR.defaultBlockState(), param4, param5);
    }

    @Override
    public void playerWillDestroy(Level param0, BlockPos param1, BlockState param2, Player param3) {
        DoubleBlockHalf var0 = param2.getValue(HALF);
        BlockPos var1 = var0 == DoubleBlockHalf.LOWER ? param1.above() : param1.below();
        BlockState var2 = param0.getBlockState(var1);
        if (var2.is(this) && var2.getValue(HALF) != var0) {
            param0.setBlock(var1, Blocks.AIR.defaultBlockState(), 35);
            param0.levelEvent(param3, 2001, var1, Block.getId(var2));
            ItemStack var3 = param3.getMainHandItem();
            if (!param0.isClientSide && !param3.isCreative() && param3.canDestroy(var2)) {
                Block.dropResources(param2, param0, param1, null, param3, var3);
                Block.dropResources(var2, param0, var1, null, param3, var3);
            }
        }

        super.playerWillDestroy(param0, param1, param2, param3);
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        switch(param3) {
            case LAND:
                return param0.getValue(OPEN);
            case WATER:
                return false;
            case AIR:
                return param0.getValue(OPEN);
            default:
                return false;
        }
    }

    private int getCloseSound() {
        return this.material == Material.METAL ? 1011 : 1012;
    }

    private int getOpenSound() {
        return this.material == Material.METAL ? 1005 : 1006;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockPos var0 = param0.getClickedPos();
        if (var0.getY() < 255 && param0.getLevel().getBlockState(var0.above()).canBeReplaced(param0)) {
            Level var1 = param0.getLevel();
            boolean var2 = var1.hasNeighborSignal(var0) || var1.hasNeighborSignal(var0.above());
            return this.defaultBlockState()
                .setValue(FACING, param0.getHorizontalDirection())
                .setValue(HINGE, this.getHinge(param0))
                .setValue(POWERED, Boolean.valueOf(var2))
                .setValue(OPEN, Boolean.valueOf(var2))
                .setValue(HALF, DoubleBlockHalf.LOWER);
        } else {
            return null;
        }
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, LivingEntity param3, ItemStack param4) {
        param0.setBlock(param1.above(), param2.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    private DoorHingeSide getHinge(BlockPlaceContext param0) {
        BlockGetter var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        Direction var2 = param0.getHorizontalDirection();
        BlockPos var3 = var1.above();
        Direction var4 = var2.getCounterClockWise();
        BlockPos var5 = var1.relative(var4);
        BlockState var6 = var0.getBlockState(var5);
        BlockPos var7 = var3.relative(var4);
        BlockState var8 = var0.getBlockState(var7);
        Direction var9 = var2.getClockWise();
        BlockPos var10 = var1.relative(var9);
        BlockState var11 = var0.getBlockState(var10);
        BlockPos var12 = var3.relative(var9);
        BlockState var13 = var0.getBlockState(var12);
        int var14 = (var6.isCollisionShapeFullBlock(var0, var5) ? -1 : 0)
            + (var8.isCollisionShapeFullBlock(var0, var7) ? -1 : 0)
            + (var11.isCollisionShapeFullBlock(var0, var10) ? 1 : 0)
            + (var13.isCollisionShapeFullBlock(var0, var12) ? 1 : 0);
        boolean var15 = var6.is(this) && var6.getValue(HALF) == DoubleBlockHalf.LOWER;
        boolean var16 = var11.is(this) && var11.getValue(HALF) == DoubleBlockHalf.LOWER;
        if ((!var15 || var16) && var14 <= 0) {
            if ((!var16 || var15) && var14 >= 0) {
                int var17 = var2.getStepX();
                int var18 = var2.getStepZ();
                Vec3 var19 = param0.getClickLocation();
                double var20 = var19.x - (double)var1.getX();
                double var21 = var19.z - (double)var1.getZ();
                return (var17 >= 0 || !(var21 < 0.5)) && (var17 <= 0 || !(var21 > 0.5)) && (var18 >= 0 || !(var20 > 0.5)) && (var18 <= 0 || !(var20 < 0.5))
                    ? DoorHingeSide.LEFT
                    : DoorHingeSide.RIGHT;
            } else {
                return DoorHingeSide.LEFT;
            }
        } else {
            return DoorHingeSide.RIGHT;
        }
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (this.material == Material.METAL) {
            return InteractionResult.PASS;
        } else {
            param0 = param0.cycle(OPEN);
            param1.setBlock(param2, param0, 10);
            param1.levelEvent(param3, param0.getValue(OPEN) ? this.getOpenSound() : this.getCloseSound(), param2, 0);
            return InteractionResult.SUCCESS;
        }
    }

    public void setOpen(Level param0, BlockPos param1, boolean param2) {
        BlockState var0 = param0.getBlockState(param1);
        if (var0.is(this) && var0.getValue(OPEN) != param2) {
            param0.setBlock(param1, var0.setValue(OPEN, Boolean.valueOf(param2)), 10);
            this.playSound(param0, param1, param2);
        }
    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        boolean var0 = param1.hasNeighborSignal(param2)
            || param1.hasNeighborSignal(param2.relative(param0.getValue(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN));
        if (param3 != this && var0 != param0.getValue(POWERED)) {
            if (var0 != param0.getValue(OPEN)) {
                this.playSound(param1, param2, var0);
            }

            param1.setBlock(param2, param0.setValue(POWERED, Boolean.valueOf(var0)).setValue(OPEN, Boolean.valueOf(var0)), 2);
        }

    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockPos var0 = param2.below();
        BlockState var1 = param1.getBlockState(var0);
        return param0.getValue(HALF) == DoubleBlockHalf.LOWER ? var1.isFaceSturdy(param1, var0, Direction.UP) : var1.is(this);
    }

    private void playSound(Level param0, BlockPos param1, boolean param2) {
        param0.levelEvent(null, param2 ? this.getOpenSound() : this.getCloseSound(), param1, 0);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState param0) {
        return PushReaction.DESTROY;
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0.setValue(FACING, param1.rotate(param0.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        return param1 == Mirror.NONE ? param0 : param0.rotate(param1.getRotation(param0.getValue(FACING))).cycle(HINGE);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public long getSeed(BlockState param0, BlockPos param1) {
        return Mth.getSeed(param1.getX(), param1.below(param0.getValue(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), param1.getZ());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(HALF, FACING, OPEN, HINGE, POWERED);
    }

    public static boolean isWoodenDoor(Level param0, BlockPos param1) {
        return isWoodenDoor(param0.getBlockState(param1));
    }

    public static boolean isWoodenDoor(BlockState param0) {
        return param0.getBlock() instanceof DoorBlock && (param0.getMaterial() == Material.WOOD || param0.getMaterial() == Material.NETHER_WOOD);
    }
}
