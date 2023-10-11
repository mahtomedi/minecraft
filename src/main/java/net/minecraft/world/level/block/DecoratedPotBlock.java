package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DecoratedPotBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<DecoratedPotBlock> CODEC = simpleCodec(DecoratedPotBlock::new);
    public static final ResourceLocation SHERDS_DYNAMIC_DROP_ID = new ResourceLocation("sherds");
    private static final VoxelShape BOUNDING_BOX = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
    private static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty CRACKED = BlockStateProperties.CRACKED;
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    @Override
    public MapCodec<DecoratedPotBlock> codec() {
        return CODEC;
    }

    protected DecoratedPotBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(HORIZONTAL_FACING, Direction.NORTH)
                .setValue(WATERLOGGED, Boolean.valueOf(false))
                .setValue(CRACKED, Boolean.valueOf(false))
        );
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        FluidState var0 = param0.getLevel().getFluidState(param0.getClickedPos());
        return this.defaultBlockState()
            .setValue(HORIZONTAL_FACING, param0.getHorizontalDirection())
            .setValue(WATERLOGGED, Boolean.valueOf(var0.getType() == Fluids.WATER))
            .setValue(CRACKED, Boolean.valueOf(false));
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        BlockEntity var2 = param1.getBlockEntity(param2);
        if (!(var2 instanceof DecoratedPotBlockEntity)) {
            return InteractionResult.PASS;
        } else {
            DecoratedPotBlockEntity var0 = (DecoratedPotBlockEntity)var2;
            ItemStack var13 = param3.getItemInHand(param4);
            ItemStack var3 = var0.getTheItem();
            if (!var13.isEmpty() && (var3.isEmpty() || ItemStack.isSameItemSameTags(var3, var13) && var3.getCount() < var3.getMaxStackSize())) {
                var0.wobble(DecoratedPotBlockEntity.WobbleStyle.POSITIVE);
                param3.awardStat(Stats.ITEM_USED.get(var13.getItem()));
                ItemStack var4 = param3.isCreative() ? var13.copyWithCount(1) : var13.split(1);
                float var5;
                if (var0.isEmpty()) {
                    var0.setTheItem(var4);
                    var5 = (float)var4.getCount() / (float)var4.getMaxStackSize();
                } else {
                    var3.grow(1);
                    var5 = (float)var3.getCount() / (float)var3.getMaxStackSize();
                }

                param1.playSound(null, param2, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS, 1.0F, 0.7F + 0.5F * var5);
                if (param1 instanceof ServerLevel var7) {
                    var7.sendParticles(
                        ParticleTypes.DUST_PLUME, (double)param2.getX() + 0.5, (double)param2.getY() + 1.2, (double)param2.getZ() + 0.5, 7, 0.0, 0.0, 0.0, 0.0
                    );
                }

                param1.updateNeighbourForOutputSignal(param2, this);
            } else {
                param1.playSound(null, param2, SoundEvents.DECORATED_POT_INSERT_FAIL, SoundSource.BLOCKS, 1.0F, 1.0F);
                var0.wobble(DecoratedPotBlockEntity.WobbleStyle.NEGATIVE);
            }

            param1.gameEvent(param3, GameEvent.BLOCK_CHANGE, param2);
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, @Nullable LivingEntity param3, ItemStack param4) {
        if (param0.isClientSide) {
            param0.getBlockEntity(param1, BlockEntityType.DECORATED_POT).ifPresent(param1x -> param1x.setFromItem(param4));
        }

    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return BOUNDING_BOX;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(HORIZONTAL_FACING, WATERLOGGED, CRACKED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new DecoratedPotBlockEntity(param0, param1);
    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        Containers.dropContentsOnDestroy(param0, param3, param1, param2);
        super.onRemove(param0, param1, param2, param3, param4);
    }

    @Override
    public List<ItemStack> getDrops(BlockState param0, LootParams.Builder param1) {
        BlockEntity var0 = param1.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (var0 instanceof DecoratedPotBlockEntity var1) {
            param1.withDynamicDrop(SHERDS_DYNAMIC_DROP_ID, param1x -> var1.getDecorations().sorted().map(Item::getDefaultInstance).forEach(param1x));
        }

        return super.getDrops(param0, param1);
    }

    @Override
    public BlockState playerWillDestroy(Level param0, BlockPos param1, BlockState param2, Player param3) {
        ItemStack var0 = param3.getMainHandItem();
        BlockState var1 = param2;
        if (var0.is(ItemTags.BREAKS_DECORATED_POTS) && !EnchantmentHelper.hasSilkTouch(var0)) {
            var1 = param2.setValue(CRACKED, Boolean.valueOf(true));
            param0.setBlock(param1, var1, 4);
        }

        return super.playerWillDestroy(param0, param1, var1, param3);
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    public SoundType getSoundType(BlockState param0) {
        return param0.getValue(CRACKED) ? SoundType.DECORATED_POT_CRACKED : SoundType.DECORATED_POT;
    }

    @Override
    public void appendHoverText(ItemStack param0, @Nullable BlockGetter param1, List<Component> param2, TooltipFlag param3) {
        super.appendHoverText(param0, param1, param2, param3);
        DecoratedPotBlockEntity.Decorations var0 = DecoratedPotBlockEntity.Decorations.load(BlockItem.getBlockEntityData(param0));
        if (!var0.equals(DecoratedPotBlockEntity.Decorations.EMPTY)) {
            param2.add(CommonComponents.EMPTY);
            Stream.of(var0.front(), var0.left(), var0.right(), var0.back())
                .forEach(param1x -> param2.add(new ItemStack(param1x, 1).getHoverName().plainCopy().withStyle(ChatFormatting.GRAY)));
        }
    }

    @Override
    public void onProjectileHit(Level param0, BlockState param1, BlockHitResult param2, Projectile param3) {
        BlockPos var0 = param2.getBlockPos();
        if (!param0.isClientSide && param3.mayInteract(param0, var0) && param3.getType().is(EntityTypeTags.IMPACT_PROJECTILES)) {
            param0.setBlock(var0, param1.setValue(CRACKED, Boolean.valueOf(true)), 4);
            param0.destroyBlock(var0, true, param3);
        }

    }

    @Override
    public ItemStack getCloneItemStack(LevelReader param0, BlockPos param1, BlockState param2) {
        BlockEntity var5 = param0.getBlockEntity(param1);
        return var5 instanceof DecoratedPotBlockEntity var0 ? var0.getPotAsItem() : super.getCloneItemStack(param0, param1, param2);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState param0) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(param1.getBlockEntity(param2));
    }
}
