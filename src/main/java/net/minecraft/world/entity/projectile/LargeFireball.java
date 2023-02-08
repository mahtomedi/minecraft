package net.minecraft.world.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class LargeFireball extends Fireball {
    private int explosionPower = 1;

    public LargeFireball(EntityType<? extends LargeFireball> param0, Level param1) {
        super(param0, param1);
    }

    public LargeFireball(Level param0, LivingEntity param1, double param2, double param3, double param4, int param5) {
        super(EntityType.FIREBALL, param1, param2, param3, param4, param0);
        this.explosionPower = param5;
    }

    @Override
    protected void onHit(HitResult param0) {
        super.onHit(param0);
        if (!this.level.isClientSide) {
            boolean var0 = this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            this.level.explode(this, this.getX(), this.getY(), this.getZ(), (float)this.explosionPower, var0, Level.ExplosionInteraction.MOB);
            this.discard();
        }

    }

    @Override
    protected void onHitEntity(EntityHitResult param0) {
        super.onHitEntity(param0);
        if (!this.level.isClientSide) {
            Entity var0 = param0.getEntity();
            Entity var1 = this.getOwner();
            var0.hurt(this.damageSources().fireball(this, var1), 6.0F);
            if (var1 instanceof LivingEntity) {
                this.doEnchantDamageEffects((LivingEntity)var1, var0);
            }

        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putByte("ExplosionPower", (byte)this.explosionPower);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("ExplosionPower", 99)) {
            this.explosionPower = param0.getByte("ExplosionPower");
        }

    }
}
