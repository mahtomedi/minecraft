package net.minecraft.world.entity.projectile;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownPotion extends ThrowableItemProjectile implements ItemSupplier {
    public static final double SPLASH_RANGE = 4.0;
    private static final double SPLASH_RANGE_SQ = 16.0;
    public static final Predicate<LivingEntity> WATER_SENSITIVE = LivingEntity::isSensitiveToWater;

    public ThrownPotion(EntityType<? extends ThrownPotion> param0, Level param1) {
        super(param0, param1);
    }

    public ThrownPotion(Level param0, LivingEntity param1) {
        super(EntityType.POTION, param1, param0);
    }

    public ThrownPotion(Level param0, double param1, double param2, double param3) {
        super(EntityType.POTION, param1, param2, param3, param0);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SPLASH_POTION;
    }

    @Override
    protected float getGravity() {
        return 0.05F;
    }

    @Override
    protected void onHitBlock(BlockHitResult param0) {
        super.onHitBlock(param0);
        if (!this.level.isClientSide) {
            ItemStack var0 = this.getItem();
            Potion var1 = PotionUtils.getPotion(var0);
            List<MobEffectInstance> var2 = PotionUtils.getMobEffects(var0);
            boolean var3 = var1 == Potions.WATER && var2.isEmpty();
            Direction var4 = param0.getDirection();
            BlockPos var5 = param0.getBlockPos();
            BlockPos var6 = var5.relative(var4);
            if (var3) {
                this.dowseFire(var6);
                this.dowseFire(var6.relative(var4.getOpposite()));

                for(Direction var7 : Direction.Plane.HORIZONTAL) {
                    this.dowseFire(var6.relative(var7));
                }
            }

        }
    }

    @Override
    protected void onHit(HitResult param0) {
        super.onHit(param0);
        if (!this.level.isClientSide) {
            ItemStack var0 = this.getItem();
            Potion var1 = PotionUtils.getPotion(var0);
            List<MobEffectInstance> var2 = PotionUtils.getMobEffects(var0);
            boolean var3 = var1 == Potions.WATER && var2.isEmpty();
            if (var3) {
                this.applyWater();
            } else if (!var2.isEmpty()) {
                if (this.isLingering()) {
                    this.makeAreaOfEffectCloud(var0, var1);
                } else {
                    this.applySplash(var2, param0.getType() == HitResult.Type.ENTITY ? ((EntityHitResult)param0).getEntity() : null);
                }
            }

            int var4 = var1.hasInstantEffects() ? 2007 : 2002;
            this.level.levelEvent(var4, this.blockPosition(), PotionUtils.getColor(var0));
            this.discard();
        }
    }

    private void applyWater() {
        AABB var0 = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
        List<LivingEntity> var1 = this.level.getEntitiesOfClass(LivingEntity.class, var0, WATER_SENSITIVE);
        if (!var1.isEmpty()) {
            for(LivingEntity var2 : var1) {
                double var3 = this.distanceToSqr(var2);
                if (var3 < 16.0 && var2.isSensitiveToWater()) {
                    var2.hurt(DamageSource.indirectMagic(var2, this.getOwner()), 1.0F);
                }
            }
        }

    }

    private void applySplash(List<MobEffectInstance> param0, @Nullable Entity param1) {
        AABB var0 = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
        List<LivingEntity> var1 = this.level.getEntitiesOfClass(LivingEntity.class, var0);
        if (!var1.isEmpty()) {
            for(LivingEntity var2 : var1) {
                if (var2.isAffectedByPotions()) {
                    double var3 = this.distanceToSqr(var2);
                    if (var3 < 16.0) {
                        double var4 = 1.0 - Math.sqrt(var3) / 4.0;
                        if (var2 == param1) {
                            var4 = 1.0;
                        }

                        for(MobEffectInstance var5 : param0) {
                            MobEffect var6 = var5.getEffect();
                            if (var6.isInstantenous()) {
                                var6.applyInstantenousEffect(this, this.getOwner(), var2, var5.getAmplifier(), var4);
                            } else {
                                int var7 = (int)(var4 * (double)var5.getDuration() + 0.5);
                                if (var7 > 20) {
                                    var2.addEffect(new MobEffectInstance(var6, var7, var5.getAmplifier(), var5.isAmbient(), var5.isVisible()));
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private void makeAreaOfEffectCloud(ItemStack param0, Potion param1) {
        AreaEffectCloud var0 = new AreaEffectCloud(this.level, this.getX(), this.getY(), this.getZ());
        Entity var1 = this.getOwner();
        if (var1 instanceof LivingEntity) {
            var0.setOwner((LivingEntity)var1);
        }

        var0.setRadius(3.0F);
        var0.setRadiusOnUse(-0.5F);
        var0.setWaitTime(10);
        var0.setRadiusPerTick(-var0.getRadius() / (float)var0.getDuration());
        var0.setPotion(param1);

        for(MobEffectInstance var2 : PotionUtils.getCustomEffects(param0)) {
            var0.addEffect(new MobEffectInstance(var2));
        }

        CompoundTag var3 = param0.getTag();
        if (var3 != null && var3.contains("CustomPotionColor", 99)) {
            var0.setFixedColor(var3.getInt("CustomPotionColor"));
        }

        this.level.addFreshEntity(var0);
    }

    private boolean isLingering() {
        return this.getItem().is(Items.LINGERING_POTION);
    }

    private void dowseFire(BlockPos param0) {
        BlockState var0 = this.level.getBlockState(param0);
        if (var0.is(BlockTags.FIRE)) {
            this.level.removeBlock(param0, false);
        } else if (AbstractCandleBlock.isLit(var0)) {
            AbstractCandleBlock.extinguish(null, var0, this.level, param0);
        } else if (CampfireBlock.isLitCampfire(var0)) {
            this.level.levelEvent(null, 1009, param0, 0);
            CampfireBlock.dowse(this.getOwner(), this.level, param0, var0);
            this.level.setBlockAndUpdate(param0, var0.setValue(CampfireBlock.LIT, Boolean.valueOf(false)));
        }

    }
}
