package net.minecraft.world.level.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChestBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<ChestType> TYPE = BlockStateProperties.CHEST_TYPE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape NORTH_AABB = Block.box(1.0, 0.0, 0.0, 15.0, 14.0, 15.0);
    protected static final VoxelShape SOUTH_AABB = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 16.0);
    protected static final VoxelShape WEST_AABB = Block.box(0.0, 0.0, 1.0, 15.0, 14.0, 15.0);
    protected static final VoxelShape EAST_AABB = Block.box(1.0, 0.0, 1.0, 16.0, 14.0, 15.0);
    protected static final VoxelShape AABB = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);
    private static final ChestBlock.ChestSearchCallback<Container> CHEST_COMBINER = new ChestBlock.ChestSearchCallback<Container>() {
        public Container acceptDouble(ChestBlockEntity param0, ChestBlockEntity param1) {
            return new CompoundContainer(param0, param1);
        }

        public Container acceptSingle(ChestBlockEntity param0) {
            return param0;
        }
    };
    private static final ChestBlock.ChestSearchCallback<MenuProvider> MENU_PROVIDER_COMBINER = new ChestBlock.ChestSearchCallback<MenuProvider>() {
        public MenuProvider acceptDouble(final ChestBlockEntity param0, final ChestBlockEntity param1) {
            final Container var0 = new CompoundContainer(param0, param1);
            return new MenuProvider() {
                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int param0x, Inventory param1x, Player param2) {
                    if (param0.canOpen(param2) && param1.canOpen(param2)) {
                        param0.unpackLootTable(param1.player);
                        param1.unpackLootTable(param1.player);
                        return ChestMenu.sixRows(param0, param1, var0);
                    } else {
                        return null;
                    }
                }

                @Override
                public Component getDisplayName() {
                    if (param0.hasCustomName()) {
                        return param0.getDisplayName();
                    } else {
                        return (Component)(param1.hasCustomName() ? param1.getDisplayName() : new TranslatableComponent("container.chestDouble"));
                    }
                }
            };
        }

        public MenuProvider acceptSingle(ChestBlockEntity param0) {
            return param0;
        }
    };

    protected ChestBlock(Block.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, ChestType.SINGLE).setValue(WATERLOGGED, Boolean.valueOf(false))
        );
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        if (param2.getBlock() == this && param1.getAxis().isHorizontal()) {
            ChestType var0 = param2.getValue(TYPE);
            if (param0.getValue(TYPE) == ChestType.SINGLE
                && var0 != ChestType.SINGLE
                && param0.getValue(FACING) == param2.getValue(FACING)
                && getConnectedDirection(param2) == param1.getOpposite()) {
                return param0.setValue(TYPE, var0.getOpposite());
            }
        } else if (getConnectedDirection(param0) == param1) {
            return param0.setValue(TYPE, ChestType.SINGLE);
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        if (param0.getValue(TYPE) == ChestType.SINGLE) {
            return AABB;
        } else {
            switch(getConnectedDirection(param0)) {
                case NORTH:
                default:
                    return NORTH_AABB;
                case SOUTH:
                    return SOUTH_AABB;
                case WEST:
                    return WEST_AABB;
                case EAST:
                    return EAST_AABB;
            }
        }
    }

    public static Direction getConnectedDirection(BlockState param0) {
        Direction var0 = param0.getValue(FACING);
        return param0.getValue(TYPE) == ChestType.LEFT ? var0.getClockWise() : var0.getCounterClockWise();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        ChestType var0 = ChestType.SINGLE;
        Direction var1 = param0.getHorizontalDirection().getOpposite();
        FluidState var2 = param0.getLevel().getFluidState(param0.getClickedPos());
        boolean var3 = param0.isSecondaryUseActive();
        Direction var4 = param0.getClickedFace();
        if (var4.getAxis().isHorizontal() && var3) {
            Direction var5 = this.candidatePartnerFacing(param0, var4.getOpposite());
            if (var5 != null && var5.getAxis() != var4.getAxis()) {
                var1 = var5;
                var0 = var5.getCounterClockWise() == var4.getOpposite() ? ChestType.RIGHT : ChestType.LEFT;
            }
        }

        if (var0 == ChestType.SINGLE && !var3) {
            if (var1 == this.candidatePartnerFacing(param0, var1.getClockWise())) {
                var0 = ChestType.LEFT;
            } else if (var1 == this.candidatePartnerFacing(param0, var1.getCounterClockWise())) {
                var0 = ChestType.RIGHT;
            }
        }

        return this.defaultBlockState().setValue(FACING, var1).setValue(TYPE, var0).setValue(WATERLOGGED, Boolean.valueOf(var2.getType() == Fluids.WATER));
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Nullable
    private Direction candidatePartnerFacing(BlockPlaceContext param0, Direction param1) {
        BlockState var0 = param0.getLevel().getBlockState(param0.getClickedPos().relative(param1));
        return var0.getBlock() == this && var0.getValue(TYPE) == ChestType.SINGLE ? var0.getValue(FACING) : null;
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, LivingEntity param3, ItemStack param4) {
        if (param4.hasCustomHoverName()) {
            BlockEntity var0 = param0.getBlockEntity(param1);
            if (var0 instanceof ChestBlockEntity) {
                ((ChestBlockEntity)var0).setCustomName(param4.getHoverName());
            }
        }

    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (param0.getBlock() != param3.getBlock()) {
            BlockEntity var0 = param1.getBlockEntity(param2);
            if (var0 instanceof Container) {
                Containers.dropContents(param1, param2, (Container)var0);
                param1.updateNeighbourForOutputSignal(param2, this);
            }

            super.onRemove(param0, param1, param2, param3, param4);
        }
    }

    @Override
    public boolean use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (param1.isClientSide) {
            return true;
        } else {
            MenuProvider var0 = this.getMenuProvider(param0, param1, param2);
            if (var0 != null) {
                param3.openMenu(var0);
                param3.awardStat(this.getOpenChestStat());
            }

            return true;
        }
    }

    protected Stat<ResourceLocation> getOpenChestStat() {
        return Stats.CUSTOM.get(Stats.OPEN_CHEST);
    }

    @Nullable
    public static <T> T combineWithNeigbour(BlockState param0, LevelAccessor param1, BlockPos param2, boolean param3, ChestBlock.ChestSearchCallback<T> param4) {
        BlockEntity var0 = param1.getBlockEntity(param2);
        if (!(var0 instanceof ChestBlockEntity)) {
            return null;
        } else if (!param3 && isChestBlockedAt(param1, param2)) {
            return null;
        } else {
            ChestBlockEntity var1 = (ChestBlockEntity)var0;
            ChestType var2 = param0.getValue(TYPE);
            if (var2 == ChestType.SINGLE) {
                return param4.acceptSingle(var1);
            } else {
                BlockPos var3 = param2.relative(getConnectedDirection(param0));
                BlockState var4 = param1.getBlockState(var3);
                if (var4.getBlock() == param0.getBlock()) {
                    ChestType var5 = var4.getValue(TYPE);
                    if (var5 != ChestType.SINGLE && var2 != var5 && var4.getValue(FACING) == param0.getValue(FACING)) {
                        if (!param3 && isChestBlockedAt(param1, var3)) {
                            return null;
                        }

                        BlockEntity var6 = param1.getBlockEntity(var3);
                        if (var6 instanceof ChestBlockEntity) {
                            ChestBlockEntity var7 = var2 == ChestType.RIGHT ? var1 : (ChestBlockEntity)var6;
                            ChestBlockEntity var8 = var2 == ChestType.RIGHT ? (ChestBlockEntity)var6 : var1;
                            return param4.acceptDouble(var7, var8);
                        }
                    }
                }

                return param4.acceptSingle(var1);
            }
        }
    }

    @Nullable
    public static Container getContainer(BlockState param0, Level param1, BlockPos param2, boolean param3) {
        return combineWithNeigbour(param0, param1, param2, param3, CHEST_COMBINER);
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState param0, Level param1, BlockPos param2) {
        return combineWithNeigbour(param0, param1, param2, false, MENU_PROVIDER_COMBINER);
    }

    @Override
    public BlockEntity newBlockEntity(BlockGetter param0) {
        return new ChestBlockEntity();
    }

    private static boolean isChestBlockedAt(LevelAccessor param0, BlockPos param1) {
        return isBlockedChestByBlock(param0, param1) || isCatSittingOnChest(param0, param1);
    }

    private static boolean isBlockedChestByBlock(BlockGetter param0, BlockPos param1) {
        BlockPos var0 = param1.above();
        return param0.getBlockState(var0).isRedstoneConductor(param0, var0);
    }

    private static boolean isCatSittingOnChest(LevelAccessor param0, BlockPos param1) {
        List<Cat> var0 = param0.getEntitiesOfClass(
            Cat.class,
            new AABB(
                (double)param1.getX(),
                (double)(param1.getY() + 1),
                (double)param1.getZ(),
                (double)(param1.getX() + 1),
                (double)(param1.getY() + 2),
                (double)(param1.getZ() + 1)
            )
        );
        if (!var0.isEmpty()) {
            for(Cat var1 : var0) {
                if (var1.isSitting()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState param0) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        return AbstractContainerMenu.getRedstoneSignalFromContainer(getContainer(param0, param1, param2, false));
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
        param0.add(FACING, TYPE, WATERLOGGED);
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }

    interface ChestSearchCallback<T> {
        T acceptDouble(ChestBlockEntity var1, ChestBlockEntity var2);

        T acceptSingle(ChestBlockEntity var1);
    }
}
