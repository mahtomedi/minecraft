package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class BucketItem extends Item {
    private final Fluid content;

    public BucketItem(Fluid param0, Item.Properties param1) {
        super(param1);
        this.content = param0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        HitResult var1 = getPlayerPOVHitResult(param0, param1, this.content == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
        if (var1.getType() == HitResult.Type.MISS) {
            return new InteractionResultHolder<>(InteractionResult.PASS, var0);
        } else if (var1.getType() != HitResult.Type.BLOCK) {
            return new InteractionResultHolder<>(InteractionResult.PASS, var0);
        } else {
            BlockHitResult var2 = (BlockHitResult)var1;
            BlockPos var3 = var2.getBlockPos();
            if (!param0.mayInteract(param1, var3) || !param1.mayUseItemAt(var3, var2.getDirection(), var0)) {
                return new InteractionResultHolder<>(InteractionResult.FAIL, var0);
            } else if (this.content == Fluids.EMPTY) {
                BlockState var4 = param0.getBlockState(var3);
                if (var4.getBlock() instanceof BucketPickup) {
                    Fluid var5 = ((BucketPickup)var4.getBlock()).takeLiquid(param0, var3, var4);
                    if (var5 != Fluids.EMPTY) {
                        param1.awardStat(Stats.ITEM_USED.get(this));
                        param1.playSound(var5.is(FluidTags.LAVA) ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL, 1.0F, 1.0F);
                        ItemStack var6 = this.createResultItem(var0, param1, var5.getBucket());
                        if (!param0.isClientSide) {
                            CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)param1, new ItemStack(var5.getBucket()));
                        }

                        return new InteractionResultHolder<>(InteractionResult.SUCCESS, var6);
                    }
                }

                return new InteractionResultHolder<>(InteractionResult.FAIL, var0);
            } else {
                BlockState var7 = param0.getBlockState(var3);
                BlockPos var8 = var7.getBlock() instanceof LiquidBlockContainer && this.content == Fluids.WATER
                    ? var3
                    : var2.getBlockPos().relative(var2.getDirection());
                if (this.emptyBucket(param1, param0, var8, var2)) {
                    this.checkExtraContent(param0, var0, var8);
                    if (param1 instanceof ServerPlayer) {
                        CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)param1, var8, var0);
                    }

                    param1.awardStat(Stats.ITEM_USED.get(this));
                    return new InteractionResultHolder<>(InteractionResult.SUCCESS, this.getEmptySuccessItem(var0, param1));
                } else {
                    return new InteractionResultHolder<>(InteractionResult.FAIL, var0);
                }
            }
        }
    }

    protected ItemStack getEmptySuccessItem(ItemStack param0, Player param1) {
        return !param1.abilities.instabuild ? new ItemStack(Items.BUCKET) : param0;
    }

    public void checkExtraContent(Level param0, ItemStack param1, BlockPos param2) {
    }

    private ItemStack createResultItem(ItemStack param0, Player param1, Item param2) {
        if (param1.abilities.instabuild) {
            return param0;
        } else {
            param0.shrink(1);
            if (param0.isEmpty()) {
                return new ItemStack(param2);
            } else {
                if (!param1.inventory.add(new ItemStack(param2))) {
                    param1.drop(new ItemStack(param2), false);
                }

                return param0;
            }
        }
    }

    public boolean emptyBucket(@Nullable Player param0, Level param1, BlockPos param2, @Nullable BlockHitResult param3) {
        if (!(this.content instanceof FlowingFluid)) {
            return false;
        } else {
            BlockState var0 = param1.getBlockState(param2);
            Material var1 = var0.getMaterial();
            boolean var2 = !var1.isSolid();
            boolean var3 = var1.isReplaceable();
            if (param1.isEmptyBlock(param2)
                || var2
                || var3
                || var0.getBlock() instanceof LiquidBlockContainer
                    && ((LiquidBlockContainer)var0.getBlock()).canPlaceLiquid(param1, param2, var0, this.content)) {
                if (param1.dimension.isUltraWarm() && this.content.is(FluidTags.WATER)) {
                    int var4 = param2.getX();
                    int var5 = param2.getY();
                    int var6 = param2.getZ();
                    param1.playSound(
                        param0,
                        param2,
                        SoundEvents.FIRE_EXTINGUISH,
                        SoundSource.BLOCKS,
                        0.5F,
                        2.6F + (param1.random.nextFloat() - param1.random.nextFloat()) * 0.8F
                    );

                    for(int var7 = 0; var7 < 8; ++var7) {
                        param1.addParticle(
                            ParticleTypes.LARGE_SMOKE, (double)var4 + Math.random(), (double)var5 + Math.random(), (double)var6 + Math.random(), 0.0, 0.0, 0.0
                        );
                    }
                } else if (var0.getBlock() instanceof LiquidBlockContainer && this.content == Fluids.WATER) {
                    if (((LiquidBlockContainer)var0.getBlock()).placeLiquid(param1, param2, var0, ((FlowingFluid)this.content).getSource(false))) {
                        this.playEmptySound(param0, param1, param2);
                    }
                } else {
                    if (!param1.isClientSide && (var2 || var3) && !var1.isLiquid()) {
                        param1.destroyBlock(param2, true);
                    }

                    this.playEmptySound(param0, param1, param2);
                    param1.setBlock(param2, this.content.defaultFluidState().createLegacyBlock(), 11);
                }

                return true;
            } else {
                return param3 == null ? false : this.emptyBucket(param0, param1, param3.getBlockPos().relative(param3.getDirection()), null);
            }
        }
    }

    protected void playEmptySound(@Nullable Player param0, LevelAccessor param1, BlockPos param2) {
        SoundEvent var0 = this.content.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        param1.playSound(param0, param2, var0, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
