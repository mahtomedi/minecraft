package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeCache;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CrafterBlock extends BaseEntityBlock {
    public static final MapCodec<CrafterBlock> CODEC = simpleCodec(CrafterBlock::new);
    public static final BooleanProperty CRAFTING = BlockStateProperties.CRAFTING;
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
    private static final EnumProperty<FrontAndTop> ORIENTATION = BlockStateProperties.ORIENTATION;
    private static final int MAX_CRAFTING_TICKS = 6;
    private static final RecipeCache RECIPE_CACHE = new RecipeCache(10);

    public CrafterBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(ORIENTATION, FrontAndTop.NORTH_UP)
                .setValue(TRIGGERED, Boolean.valueOf(false))
                .setValue(CRAFTING, Boolean.valueOf(false))
        );
    }

    @Override
    protected MapCodec<CrafterBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState param0) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        BlockEntity var0 = param1.getBlockEntity(param2);
        return var0 instanceof CrafterBlockEntity var1 ? var1.getRedstoneSignal() : 0;
    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        boolean var0 = param1.hasNeighborSignal(param2);
        boolean var1 = param0.getValue(TRIGGERED);
        BlockEntity var2 = param1.getBlockEntity(param2);
        if (var0 && !var1) {
            param1.scheduleTick(param2, this, 1);
            param1.setBlock(param2, param0.setValue(TRIGGERED, Boolean.valueOf(true)), 2);
            this.setBlockEntityTriggered(var2, true);
        } else if (!var0 && var1) {
            param1.setBlock(param2, param0.setValue(TRIGGERED, Boolean.valueOf(false)).setValue(CRAFTING, Boolean.valueOf(false)), 2);
            this.setBlockEntityTriggered(var2, false);
        }

    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        this.dispenseFrom(param0, param1, param2);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level param0, BlockState param1, BlockEntityType<T> param2) {
        return param0.isClientSide ? null : createTickerHelper(param2, BlockEntityType.CRAFTER, CrafterBlockEntity::serverTick);
    }

    private void setBlockEntityTriggered(@Nullable BlockEntity param0, boolean param1) {
        if (param0 instanceof CrafterBlockEntity var0) {
            var0.setTriggered(param1);
        }

    }

    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        CrafterBlockEntity var0 = new CrafterBlockEntity(param0, param1);
        var0.setTriggered(param1.hasProperty(TRIGGERED) && param1.getValue(TRIGGERED));
        return var0;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        Direction var0 = param0.getNearestLookingDirection().getOpposite();

        Direction var1 = switch(var0) {
            case DOWN -> param0.getHorizontalDirection().getOpposite();
            case UP -> param0.getHorizontalDirection();
            case NORTH, SOUTH, WEST, EAST -> Direction.UP;
        };
        return this.defaultBlockState()
            .setValue(ORIENTATION, FrontAndTop.fromFrontAndTop(var0, var1))
            .setValue(TRIGGERED, Boolean.valueOf(param0.getLevel().hasNeighborSignal(param0.getClickedPos())));
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, LivingEntity param3, ItemStack param4) {
        if (param4.hasCustomHoverName()) {
            BlockEntity var7 = param0.getBlockEntity(param1);
            if (var7 instanceof CrafterBlockEntity var0) {
                var0.setCustomName(param4.getHoverName());
            }
        }

        if (param2.getValue(TRIGGERED)) {
            param0.scheduleTick(param1, this, 1);
        }

    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        Containers.dropContentsOnDestroy(param0, param3, param1, param2);
        super.onRemove(param0, param1, param2, param3, param4);
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (param1.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            BlockEntity var0 = param1.getBlockEntity(param2);
            if (var0 instanceof CrafterBlockEntity) {
                param3.openMenu((CrafterBlockEntity)var0);
            }

            return InteractionResult.CONSUME;
        }
    }

    protected void dispenseFrom(BlockState param0, ServerLevel param1, BlockPos param2) {
        BlockEntity var2 = param1.getBlockEntity(param2);
        if (var2 instanceof CrafterBlockEntity var0) {
            Optional<CraftingRecipe> var2x = getPotentialResults(param1, var0);
            if (var2x.isEmpty()) {
                param1.levelEvent(1050, param2, 0);
            } else {
                var0.setCraftingTicksRemaining(6);
                param1.setBlock(param2, param0.setValue(CRAFTING, Boolean.valueOf(true)), 2);
                CraftingRecipe var3 = var2x.get();
                ItemStack var4 = var3.assemble(var0, param1.registryAccess());
                var4.onCraftedBySystem(param1);
                this.dispenseItem(param1, param2, var0, var4, param0);
                var3.getRemainingItems(var0).forEach(param4 -> this.dispenseItem(param1, param2, var0, param4, param0));
                var0.getItems().forEach(param0x -> {
                    if (!param0x.isEmpty()) {
                        param0x.shrink(1);
                    }
                });
            }
        }
    }

    public static Optional<CraftingRecipe> getPotentialResults(Level param0, CraftingContainer param1) {
        return RECIPE_CACHE.get(param0, param1);
    }

    private void dispenseItem(Level param0, BlockPos param1, CrafterBlockEntity param2, ItemStack param3, BlockState param4) {
        Direction var0 = param4.getValue(ORIENTATION).front();
        Container var1 = HopperBlockEntity.getContainerAt(param0, param1.relative(var0));
        ItemStack var2 = param3.copy();
        if (var1 instanceof CrafterBlockEntity) {
            while(!var2.isEmpty()) {
                ItemStack var3 = var2.copyWithCount(1);
                ItemStack var4 = HopperBlockEntity.addItem(param2, var1, var3, var0.getOpposite());
                if (!var4.isEmpty()) {
                    break;
                }

                var2.shrink(1);
            }
        } else if (var1 != null) {
            while(!var2.isEmpty()) {
                int var5 = var2.getCount();
                var2 = HopperBlockEntity.addItem(param2, var1, var2, var0.getOpposite());
                if (var5 == var2.getCount()) {
                    break;
                }
            }
        }

        if (!var2.isEmpty()) {
            Vec3 var6 = Vec3.atCenterOf(param1).relative(var0, 0.7);
            DefaultDispenseItemBehavior.spawnItem(param0, var2, 6, var0, var6);
            param0.levelEvent(1049, param1, 0);
            param0.levelEvent(2010, param1, var0.get3DDataValue());
        }

    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0.setValue(ORIENTATION, param1.rotation().rotate(param0.getValue(ORIENTATION)));
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        return param0.setValue(ORIENTATION, param1.rotation().rotate(param0.getValue(ORIENTATION)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(ORIENTATION, TRIGGERED, CRAFTING);
    }
}
