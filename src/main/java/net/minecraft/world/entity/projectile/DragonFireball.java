package net.minecraft.world.entity.projectile;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DragonFireball extends AbstractHurtingProjectile {
    public DragonFireball(EntityType<? extends DragonFireball> param0, Level param1) {
        super(param0, param1);
    }

    @OnlyIn(Dist.CLIENT)
    public DragonFireball(Level param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        super(EntityType.DRAGON_FIREBALL, param1, param2, param3, param4, param5, param6, param0);
    }

    public DragonFireball(Level param0, LivingEntity param1, double param2, double param3, double param4) {
        super(EntityType.DRAGON_FIREBALL, param1, param2, param3, param4, param0);
    }

    @Override
    protected void onHit(HitResult param0) {
        super.onHit(param0);
        if (param0.getType() != HitResult.Type.ENTITY || !((EntityHitResult)param0).getEntity().is(this.owner)) {
            if (!this.level.isClientSide) {
                List<LivingEntity> var0 = this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0, 2.0, 4.0));
                AreaEffectCloud var1 = new AreaEffectCloud(this.level, this.getX(), this.getY(), this.getZ());
                var1.setOwner(this.owner);
                var1.setParticle(ParticleTypes.DRAGON_BREATH);
                var1.setRadius(3.0F);
                var1.setDuration(600);
                var1.setRadiusPerTick((7.0F - var1.getRadius()) / (float)var1.getDuration());
                var1.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 1));
                if (!var0.isEmpty()) {
                    for(LivingEntity var2 : var0) {
                        double var3 = this.distanceToSqr(var2);
                        if (var3 < 16.0) {
                            var1.setPos(var2.getX(), var2.getY(), var2.getZ());
                            break;
                        }
                    }
                }

                this.level.levelEvent(2006, new BlockPos(this), 0);
                this.level.addFreshEntity(var1);
                this.remove();
            }

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
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.DRAGON_BREATH;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }
}
