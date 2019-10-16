package net.minecraft.world.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LargeFireball extends Fireball {
    public int explosionPower = 1;

    public LargeFireball(EntityType<? extends LargeFireball> param0, Level param1) {
        super(param0, param1);
    }

    @OnlyIn(Dist.CLIENT)
    public LargeFireball(Level param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        super(EntityType.FIREBALL, param1, param2, param3, param4, param5, param6, param0);
    }

    public LargeFireball(Level param0, LivingEntity param1, double param2, double param3, double param4) {
        super(EntityType.FIREBALL, param1, param2, param3, param4, param0);
    }

    @Override
    protected void onHit(HitResult param0) {
        super.onHit(param0);
        if (!this.level.isClientSide) {
            if (param0.getType() == HitResult.Type.ENTITY) {
                Entity var0 = ((EntityHitResult)param0).getEntity();
                var0.hurt(DamageSource.fireball(this, this.owner), 6.0F);
                this.doEnchantDamageEffects(this.owner, var0);
            }

            boolean var1 = this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            this.level
                .explode(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    (float)this.explosionPower,
                    var1,
                    var1 ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.NONE
                );
            this.remove();
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("ExplosionPower", this.explosionPower);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("ExplosionPower", 99)) {
            this.explosionPower = param0.getInt("ExplosionPower");
        }

    }
}
