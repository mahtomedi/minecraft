package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class BucketItem extends Item implements DispensibleContainerItem {
    private final Fluid content;

    public BucketItem(Fluid param0, Item.Properties param1) {
        super(param1);
        this.content = param0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        BlockHitResult var1 = getPlayerPOVHitResult(param0, param1, this.content == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
        if (var1.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(var0);
        } else if (var1.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(var0);
        } else {
            BlockPos var2 = var1.getBlockPos();
            Direction var3 = var1.getDirection();
            BlockPos var4 = var2.relative(var3);
            if (!param0.mayInteract(param1, var2) || !param1.mayUseItemAt(var4, var3, var0)) {
                return InteractionResultHolder.fail(var0);
            } else if (this.content == Fluids.EMPTY) {
                BlockState var5 = param0.getBlockState(var2);
                Block var7 = var5.getBlock();
                if (var7 instanceof BucketPickup var6) {
                    ItemStack var7x = var6.pickupBlock(param1, param0, var2, var5);
                    if (!var7x.isEmpty()) {
                        param1.awardStat(Stats.ITEM_USED.get(this));
                        var6.getPickupSound().ifPresent(param1x -> param1.playSound(param1x, 1.0F, 1.0F));
                        param0.gameEvent(param1, GameEvent.FLUID_PICKUP, var2);
                        ItemStack var8 = ItemUtils.createFilledResult(var0, param1, var7x);
                        if (!param0.isClientSide) {
                            CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)param1, var7x);
                        }

                        return InteractionResultHolder.sidedSuccess(var8, param0.isClientSide());
                    }
                }

                return InteractionResultHolder.fail(var0);
            } else {
                BlockState var9 = param0.getBlockState(var2);
                BlockPos var10 = var9.getBlock() instanceof LiquidBlockContainer && this.content == Fluids.WATER ? var2 : var4;
                if (this.emptyContents(param1, param0, var10, var1)) {
                    this.checkExtraContent(param1, param0, var0, var10);
                    if (param1 instanceof ServerPlayer) {
                        CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)param1, var10, var0);
                    }

                    param1.awardStat(Stats.ITEM_USED.get(this));
                    return InteractionResultHolder.sidedSuccess(getEmptySuccessItem(var0, param1), param0.isClientSide());
                } else {
                    return InteractionResultHolder.fail(var0);
                }
            }
        }
    }

    public static ItemStack getEmptySuccessItem(ItemStack param0, Player param1) {
        return !param1.getAbilities().instabuild ? new ItemStack(Items.BUCKET) : param0;
    }

    @Override
    public void checkExtraContent(@Nullable Player param0, Level param1, ItemStack param2, BlockPos param3) {
    }

    @Override
    public boolean emptyContents(@Nullable Player param0, Level param1, BlockPos param2, @Nullable BlockHitResult param3) {
        Fluid var2 = this.content;
        if (!(var2 instanceof FlowingFluid)) {
            return false;
        } else {
            FlowingFluid var0;
            Block var3;
            boolean var4;
            boolean var10000;
            label82: {
                var0 = (FlowingFluid)var2;
                var2 = param1.getBlockState(param2);
                var3 = var2.getBlock();
                var4 = var2.canBeReplaced(this.content);
                label70:
                if (!var2.isAir() && !var4) {
                    if (var3 instanceof LiquidBlockContainer var5 && var5.canPlaceLiquid(param0, param1, param2, var2, this.content)) {
                        break label70;
                    }

                    var10000 = false;
                    break label82;
                }

                var10000 = true;
            }

            boolean var6 = var10000;
            if (!var6) {
                return param3 != null && this.emptyContents(param0, param1, param3.getBlockPos().relative(param3.getDirection()), null);
            } else if (param1.dimensionType().ultraWarm() && this.content.is(FluidTags.WATER)) {
                int var7 = param2.getX();
                int var8 = param2.getY();
                int var9 = param2.getZ();
                param1.playSound(
                    param0,
                    param2,
                    SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.BLOCKS,
                    0.5F,
                    2.6F + (param1.random.nextFloat() - param1.random.nextFloat()) * 0.8F
                );

                for(int var10 = 0; var10 < 8; ++var10) {
                    param1.addParticle(
                        ParticleTypes.LARGE_SMOKE, (double)var7 + Math.random(), (double)var8 + Math.random(), (double)var9 + Math.random(), 0.0, 0.0, 0.0
                    );
                }

                return true;
            } else {
                if (var3 instanceof LiquidBlockContainer var11 && this.content == Fluids.WATER) {
                    var11.placeLiquid(param1, param2, var2, var0.getSource(false));
                    this.playEmptySound(param0, param1, param2);
                    return true;
                }

                if (!param1.isClientSide && var4 && !var2.liquid()) {
                    param1.destroyBlock(param2, true);
                }

                if (!param1.setBlock(param2, this.content.defaultFluidState().createLegacyBlock(), 11) && !var2.getFluidState().isSource()) {
                    return false;
                } else {
                    this.playEmptySound(param0, param1, param2);
                    return true;
                }
            }
        }
    }

    protected void playEmptySound(@Nullable Player param0, LevelAccessor param1, BlockPos param2) {
        SoundEvent var0 = this.content.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        param1.playSound(param0, param2, var0, SoundSource.BLOCKS, 1.0F, 1.0F);
        param1.gameEvent(param0, GameEvent.FLUID_PLACE, param2);
    }
}
