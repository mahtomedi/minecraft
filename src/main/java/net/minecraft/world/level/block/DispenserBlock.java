package net.minecraft.world.level.block;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;

public class DispenserBlock extends BaseEntityBlock {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<DispenserBlock> CODEC = simpleCodec(DispenserBlock::new);
    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
    private static final Map<Item, DispenseItemBehavior> DISPENSER_REGISTRY = Util.make(
        new Object2ObjectOpenHashMap<>(), param0 -> param0.defaultReturnValue(new DefaultDispenseItemBehavior())
    );
    private static final int TRIGGER_DURATION = 4;

    @Override
    public MapCodec<? extends DispenserBlock> codec() {
        return CODEC;
    }

    public static void registerBehavior(ItemLike param0, DispenseItemBehavior param1) {
        DISPENSER_REGISTRY.put(param0.asItem(), param1);
    }

    protected DispenserBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TRIGGERED, Boolean.valueOf(false)));
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (param1.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            BlockEntity var0 = param1.getBlockEntity(param2);
            if (var0 instanceof DispenserBlockEntity) {
                param3.openMenu((DispenserBlockEntity)var0);
                if (var0 instanceof DropperBlockEntity) {
                    param3.awardStat(Stats.INSPECT_DROPPER);
                } else {
                    param3.awardStat(Stats.INSPECT_DISPENSER);
                }
            }

            return InteractionResult.CONSUME;
        }
    }

    protected void dispenseFrom(ServerLevel param0, BlockState param1, BlockPos param2) {
        DispenserBlockEntity var0 = param0.getBlockEntity(param2, BlockEntityType.DISPENSER).orElse(null);
        if (var0 == null) {
            LOGGER.warn("Ignoring dispensing attempt for Dispenser without matching block entity at {}", param2);
        } else {
            BlockSource var1 = new BlockSource(param0, param2, param1, var0);
            int var2 = var0.getRandomSlot(param0.random);
            if (var2 < 0) {
                param0.levelEvent(1001, param2, 0);
                param0.gameEvent(GameEvent.BLOCK_ACTIVATE, param2, GameEvent.Context.of(var0.getBlockState()));
            } else {
                ItemStack var3 = var0.getItem(var2);
                DispenseItemBehavior var4 = this.getDispenseMethod(var3);
                if (var4 != DispenseItemBehavior.NOOP) {
                    var0.setItem(var2, var4.dispense(var1, var3));
                }

            }
        }
    }

    protected DispenseItemBehavior getDispenseMethod(ItemStack param0) {
        return DISPENSER_REGISTRY.get(param0.getItem());
    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        boolean var0 = param1.hasNeighborSignal(param2) || param1.hasNeighborSignal(param2.above());
        boolean var1 = param0.getValue(TRIGGERED);
        if (var0 && !var1) {
            param1.scheduleTick(param2, this, 4);
            param1.setBlock(param2, param0.setValue(TRIGGERED, Boolean.valueOf(true)), 2);
        } else if (!var0 && var1) {
            param1.setBlock(param2, param0.setValue(TRIGGERED, Boolean.valueOf(false)), 2);
        }

    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        this.dispenseFrom(param1, param0, param2);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new DispenserBlockEntity(param0, param1);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState().setValue(FACING, param0.getNearestLookingDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, LivingEntity param3, ItemStack param4) {
        if (param4.hasCustomHoverName()) {
            BlockEntity var0 = param0.getBlockEntity(param1);
            if (var0 instanceof DispenserBlockEntity) {
                ((DispenserBlockEntity)var0).setCustomName(param4.getHoverName());
            }
        }

    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        Containers.dropContentsOnDestroy(param0, param3, param1, param2);
        super.onRemove(param0, param1, param2, param3, param4);
    }

    public static Position getDispensePosition(BlockSource param0) {
        Direction var0 = param0.state().getValue(FACING);
        return param0.center().add(0.7 * (double)var0.getStepX(), 0.7 * (double)var0.getStepY(), 0.7 * (double)var0.getStepZ());
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState param0) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(param1.getBlockEntity(param2));
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
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
        param0.add(FACING, TRIGGERED);
    }
}
