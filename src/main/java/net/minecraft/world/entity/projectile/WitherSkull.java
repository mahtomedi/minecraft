package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class WitherSkull extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Boolean> DATA_DANGEROUS = SynchedEntityData.defineId(WitherSkull.class, EntityDataSerializers.BOOLEAN);

    public WitherSkull(EntityType<? extends WitherSkull> param0, Level param1) {
        super(param0, param1);
    }

    public WitherSkull(Level param0, LivingEntity param1, double param2, double param3, double param4) {
        super(EntityType.WITHER_SKULL, param1, param2, param3, param4, param0);
    }

    @Override
    protected float getInertia() {
        return this.isDangerous() ? 0.73F : super.getInertia();
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public float getBlockExplosionResistance(Explosion param0, BlockGetter param1, BlockPos param2, BlockState param3, FluidState param4, float param5) {
        return this.isDangerous() && WitherBoss.canDestroy(param3) ? Math.min(0.8F, param5) : param5;
    }

    @Override
    protected void onHitEntity(EntityHitResult param0) {
        super.onHitEntity(param0);
        if (!this.level().isClientSide) {
            Entity var0 = param0.getEntity();
            Entity var1 = this.getOwner();
            boolean var3;
            if (var1 instanceof LivingEntity var2) {
                var3 = var0.hurt(this.damageSources().witherSkull(this, var2), 8.0F);
                if (var3) {
                    if (var0.isAlive()) {
                        this.doEnchantDamageEffects(var2, var0);
                    } else {
                        var2.heal(5.0F);
                    }
                }
            } else {
                var3 = var0.hurt(this.damageSources().magic(), 5.0F);
            }

            if (var3 && var0 instanceof LivingEntity var5) {
                int var6 = 0;
                if (this.level().getDifficulty() == Difficulty.NORMAL) {
                    var6 = 10;
                } else if (this.level().getDifficulty() == Difficulty.HARD) {
                    var6 = 40;
                }

                if (var6 > 0) {
                    var5.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * var6, 1), this.getEffectSource());
                }
            }

        }
    }

    @Override
    protected void onHit(HitResult param0) {
        super.onHit(param0);
        if (!this.level().isClientSide) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 1.0F, false, Level.ExplosionInteraction.MOB);
            this.discard();
        }

    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_DANGEROUS, false);
    }

    public boolean isDangerous() {
        return this.entityData.get(DATA_DANGEROUS);
    }

    public void setDangerous(boolean param0) {
        this.entityData.set(DATA_DANGEROUS, param0);
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }
}
