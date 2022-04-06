package net.minecraft.world.level.block;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
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

public class ChestBlock extends AbstractChestBlock<ChestBlockEntity> implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<ChestType> TYPE = BlockStateProperties.CHEST_TYPE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final int EVENT_SET_OPEN_COUNT = 1;
    protected static final int AABB_OFFSET = 1;
    protected static final int AABB_HEIGHT = 14;
    protected static final VoxelShape NORTH_AABB = Block.box(1.0, 0.0, 0.0, 15.0, 14.0, 15.0);
    protected static final VoxelShape SOUTH_AABB = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 16.0);
    protected static final VoxelShape WEST_AABB = Block.box(0.0, 0.0, 1.0, 15.0, 14.0, 15.0);
    protected static final VoxelShape EAST_AABB = Block.box(1.0, 0.0, 1.0, 16.0, 14.0, 15.0);
    protected static final VoxelShape AABB = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);
    private static final DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<Container>> CHEST_COMBINER = new DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<Container>>(
        
    ) {
        public Optional<Container> acceptDouble(ChestBlockEntity param0, ChestBlockEntity param1) {
            return Optional.of(new CompoundContainer(param0, param1));
        }

        public Optional<Container> acceptSingle(ChestBlockEntity param0) {
            return Optional.of(param0);
        }

        public Optional<Container> acceptNone() {
            return Optional.empty();
        }
    };
    private static final DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<MenuProvider>> MENU_PROVIDER_COMBINER = new DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<MenuProvider>>(
        
    ) {
        public Optional<MenuProvider> acceptDouble(final ChestBlockEntity param0, final ChestBlockEntity param1) {
            final Container var0 = new CompoundContainer(param0, param1);
            return Optional.of(new MenuProvider() {
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
            });
        }

        public Optional<MenuProvider> acceptSingle(ChestBlockEntity param0) {
            return Optional.of(param0);
        }

        public Optional<MenuProvider> acceptNone() {
            return Optional.empty();
        }
    };

    protected ChestBlock(BlockBehaviour.Properties param0, Supplier<BlockEntityType<? extends ChestBlockEntity>> param1) {
        super(param0, param1);
        this.registerDefaultState(
            this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, ChestType.SINGLE).setValue(WATERLOGGED, Boolean.valueOf(false))
        );
    }

    public static DoubleBlockCombiner.BlockType getBlockType(BlockState param0) {
        ChestType var0 = param0.getValue(TYPE);
        if (var0 == ChestType.SINGLE) {
            return DoubleBlockCombiner.BlockType.SINGLE;
        } else {
            return var0 == ChestType.RIGHT ? DoubleBlockCombiner.BlockType.FIRST : DoubleBlockCombiner.BlockType.SECOND;
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        if (param2.is(this) && param1.getAxis().isHorizontal()) {
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
        return var0.is(this) && var0.getValue(TYPE) == ChestType.SINGLE ? var0.getValue(FACING) : null;
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
        if (!param0.is(param3.getBlock())) {
            BlockEntity var0 = param1.getBlockEntity(param2);
            if (var0 instanceof Container) {
                Containers.dropContents(param1, param2, (Container)var0);
                param1.updateNeighbourForOutputSignal(param2, this);
            }

            super.onRemove(param0, param1, param2, param3, param4);
        }
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (param1.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            MenuProvider var0 = this.getMenuProvider(param0, param1, param2);
            if (var0 != null) {
                param3.openMenu(var0);
                param3.awardStat(this.getOpenChestStat());
                PiglinAi.angerNearbyPiglins(param3, true);
            }

            return InteractionResult.CONSUME;
        }
    }

    protected Stat<ResourceLocation> getOpenChestStat() {
        return Stats.CUSTOM.get(Stats.OPEN_CHEST);
    }

    public BlockEntityType<? extends ChestBlockEntity> blockEntityType() {
        return this.blockEntityType.get();
    }

    @Nullable
    public static Container getContainer(ChestBlock param0, BlockState param1, Level param2, BlockPos param3, boolean param4) {
        return param0.combine(param1, param2, param3, param4).<Optional<Container>>apply(CHEST_COMBINER).orElse(null);
    }

    @Override
    public DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(BlockState param0, Level param1, BlockPos param2, boolean param3) {
        BiPredicate<LevelAccessor, BlockPos> var0;
        if (param3) {
            var0 = (param0x, param1x) -> false;
        } else {
            var0 = ChestBlock::isChestBlockedAt;
        }

        return DoubleBlockCombiner.combineWithNeigbour(
            this.blockEntityType.get(), ChestBlock::getBlockType, ChestBlock::getConnectedDirection, FACING, param0, param1, param2, var0
        );
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState param0, Level param1, BlockPos param2) {
        return this.combine(param0, param1, param2, false).<Optional<MenuProvider>>apply(MENU_PROVIDER_COMBINER).orElse(null);
    }

    public static DoubleBlockCombiner.Combiner<ChestBlockEntity, Float2FloatFunction> opennessCombiner(final LidBlockEntity param0) {
        return new DoubleBlockCombiner.Combiner<ChestBlockEntity, Float2FloatFunction>() {
            public Float2FloatFunction acceptDouble(ChestBlockEntity param0x, ChestBlockEntity param1) {
                return param2 -> Math.max(param0.getOpenNess(param2), param1.getOpenNess(param2));
            }

            public Float2FloatFunction acceptSingle(ChestBlockEntity param0x) {
                return param0::getOpenNess;
            }

            public Float2FloatFunction acceptNone() {
                return param0::getOpenNess;
            }
        };
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new ChestBlockEntity(param0, param1);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level param0, BlockState param1, BlockEntityType<T> param2) {
        return param0.isClientSide ? createTickerHelper(param2, this.blockEntityType(), ChestBlockEntity::lidAnimateTick) : null;
    }

    public static boolean isChestBlockedAt(LevelAccessor param0x, BlockPos param1x) {
        return isBlockedChestByBlock(param0x, param1x) || isCatSittingOnChest(param0x, param1x);
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
                if (var1.isInSittingPose()) {
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
        return AbstractContainerMenu.getRedstoneSignalFromContainer(getContainer(this, param0, param1, param2, false));
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

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        BlockEntity var0 = param1.getBlockEntity(param2);
        if (var0 instanceof ChestBlockEntity) {
            ((ChestBlockEntity)var0).recheckOpen();
        }

    }
}
