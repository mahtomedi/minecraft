package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LecternBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty HAS_BOOK = BlockStateProperties.HAS_BOOK;
    public static final VoxelShape SHAPE_BASE = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
    public static final VoxelShape SHAPE_POST = Block.box(4.0, 2.0, 4.0, 12.0, 14.0, 12.0);
    public static final VoxelShape SHAPE_COMMON = Shapes.or(SHAPE_BASE, SHAPE_POST);
    public static final VoxelShape SHAPE_TOP_PLATE = Block.box(0.0, 15.0, 0.0, 16.0, 15.0, 16.0);
    public static final VoxelShape SHAPE_COLLISION = Shapes.or(SHAPE_COMMON, SHAPE_TOP_PLATE);
    public static final VoxelShape SHAPE_WEST = Shapes.or(
        Block.box(1.0, 10.0, 0.0, 5.333333, 14.0, 16.0),
        Block.box(5.333333, 12.0, 0.0, 9.666667, 16.0, 16.0),
        Block.box(9.666667, 14.0, 0.0, 14.0, 18.0, 16.0),
        SHAPE_COMMON
    );
    public static final VoxelShape SHAPE_NORTH = Shapes.or(
        Block.box(0.0, 10.0, 1.0, 16.0, 14.0, 5.333333),
        Block.box(0.0, 12.0, 5.333333, 16.0, 16.0, 9.666667),
        Block.box(0.0, 14.0, 9.666667, 16.0, 18.0, 14.0),
        SHAPE_COMMON
    );
    public static final VoxelShape SHAPE_EAST = Shapes.or(
        Block.box(10.666667, 10.0, 0.0, 15.0, 14.0, 16.0),
        Block.box(6.333333, 12.0, 0.0, 10.666667, 16.0, 16.0),
        Block.box(2.0, 14.0, 0.0, 6.333333, 18.0, 16.0),
        SHAPE_COMMON
    );
    public static final VoxelShape SHAPE_SOUTH = Shapes.or(
        Block.box(0.0, 10.0, 10.666667, 16.0, 14.0, 15.0),
        Block.box(0.0, 12.0, 6.333333, 16.0, 16.0, 10.666667),
        Block.box(0.0, 14.0, 2.0, 16.0, 18.0, 6.333333),
        SHAPE_COMMON
    );

    protected LecternBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(HAS_BOOK, Boolean.valueOf(false))
        );
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        return SHAPE_COMMON;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState param0) {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        Level var0 = param0.getLevel();
        ItemStack var1 = param0.getItemInHand();
        CompoundTag var2 = var1.getTag();
        Player var3 = param0.getPlayer();
        boolean var4 = false;
        if (!var0.isClientSide && var3 != null && var2 != null && var3.canUseGameMasterBlocks() && var2.contains("BlockEntityTag")) {
            CompoundTag var5 = var2.getCompound("BlockEntityTag");
            if (var5.contains("Book")) {
                var4 = true;
            }
        }

        return this.defaultBlockState().setValue(FACING, param0.getHorizontalDirection().getOpposite()).setValue(HAS_BOOK, Boolean.valueOf(var4));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE_COLLISION;
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        switch((Direction)param0.getValue(FACING)) {
            case NORTH:
                return SHAPE_NORTH;
            case SOUTH:
                return SHAPE_SOUTH;
            case EAST:
                return SHAPE_EAST;
            case WEST:
                return SHAPE_WEST;
            default:
                return SHAPE_COMMON;
        }
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0.setValue(FACING, param1.rotate(param0.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        return param0.rotate(param1.getRotation(param0.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING, POWERED, HAS_BOOK);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new LecternBlockEntity(param0, param1);
    }

    public static boolean tryPlaceBook(@Nullable Player param0, Level param1, BlockPos param2, BlockState param3, ItemStack param4) {
        if (!param3.getValue(HAS_BOOK)) {
            if (!param1.isClientSide) {
                placeBook(param0, param1, param2, param3, param4);
            }

            return true;
        } else {
            return false;
        }
    }

    private static void placeBook(@Nullable Player param0, Level param1, BlockPos param2, BlockState param3, ItemStack param4) {
        BlockEntity var0 = param1.getBlockEntity(param2);
        if (var0 instanceof LecternBlockEntity) {
            LecternBlockEntity var1 = (LecternBlockEntity)var0;
            var1.setBook(param4.split(1));
            resetBookState(param1, param2, param3, true);
            param1.playSound(null, param2, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1.0F, 1.0F);
            param1.gameEvent(param0, GameEvent.BLOCK_CHANGE, param2);
        }

    }

    public static void resetBookState(Level param0, BlockPos param1, BlockState param2, boolean param3) {
        param0.setBlock(param1, param2.setValue(POWERED, Boolean.valueOf(false)).setValue(HAS_BOOK, Boolean.valueOf(param3)), 3);
        updateBelow(param0, param1, param2);
    }

    public static void signalPageChange(Level param0, BlockPos param1, BlockState param2) {
        changePowered(param0, param1, param2, true);
        param0.getBlockTicks().scheduleTick(param1, param2.getBlock(), 2);
        param0.levelEvent(1043, param1, 0);
    }

    private static void changePowered(Level param0, BlockPos param1, BlockState param2, boolean param3) {
        param0.setBlock(param1, param2.setValue(POWERED, Boolean.valueOf(param3)), 3);
        updateBelow(param0, param1, param2);
    }

    private static void updateBelow(Level param0, BlockPos param1, BlockState param2) {
        param0.updateNeighborsAt(param1.below(), param2.getBlock());
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        changePowered(param1, param2, param0, false);
    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param0.is(param3.getBlock())) {
            if (param0.getValue(HAS_BOOK)) {
                this.popBook(param0, param1, param2);
            }

            if (param0.getValue(POWERED)) {
                param1.updateNeighborsAt(param2.below(), this);
            }

            super.onRemove(param0, param1, param2, param3, param4);
        }
    }

    private void popBook(BlockState param0, Level param1, BlockPos param2) {
        BlockEntity var0 = param1.getBlockEntity(param2);
        if (var0 instanceof LecternBlockEntity) {
            LecternBlockEntity var1 = (LecternBlockEntity)var0;
            Direction var2 = param0.getValue(FACING);
            ItemStack var3 = var1.getBook().copy();
            float var4 = 0.25F * (float)var2.getStepX();
            float var5 = 0.25F * (float)var2.getStepZ();
            ItemEntity var6 = new ItemEntity(
                param1, (double)param2.getX() + 0.5 + (double)var4, (double)(param2.getY() + 1), (double)param2.getZ() + 0.5 + (double)var5, var3
            );
            var6.setDefaultPickUpDelay();
            param1.addFreshEntity(var6);
            var1.clearContent();
        }

    }

    @Override
    public boolean isSignalSource(BlockState param0) {
        return true;
    }

    @Override
    public int getSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return param0.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return param3 == Direction.UP && param0.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState param0) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        if (param0.getValue(HAS_BOOK)) {
            BlockEntity var0 = param1.getBlockEntity(param2);
            if (var0 instanceof LecternBlockEntity) {
                return ((LecternBlockEntity)var0).getRedstoneSignal();
            }
        }

        return 0;
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (param0.getValue(HAS_BOOK)) {
            if (!param1.isClientSide) {
                this.openScreen(param1, param2, param3);
            }

            return InteractionResult.sidedSuccess(param1.isClientSide);
        } else {
            ItemStack var0 = param3.getItemInHand(param4);
            return !var0.isEmpty() && !var0.is(ItemTags.LECTERN_BOOKS) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState param0, Level param1, BlockPos param2) {
        return !param0.getValue(HAS_BOOK) ? null : super.getMenuProvider(param0, param1, param2);
    }

    private void openScreen(Level param0, BlockPos param1, Player param2) {
        BlockEntity var0 = param0.getBlockEntity(param1);
        if (var0 instanceof LecternBlockEntity) {
            param2.openMenu((LecternBlockEntity)var0);
            param2.awardStat(Stats.INTERACT_WITH_LECTERN);
        }

    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}
