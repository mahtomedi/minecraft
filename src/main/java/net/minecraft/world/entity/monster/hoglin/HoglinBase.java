package net.minecraft.world.entity.monster.hoglin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public interface HoglinBase {
    int ATTACK_ANIMATION_DURATION = 10;

    int getAttackAnimationRemainingTicks();

    static boolean hurtAndThrowTarget(LivingEntity param0, LivingEntity param1) {
        float var0 = (float)param0.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float var1;
        if (!param0.isBaby() && (int)var0 > 0) {
            var1 = var0 / 2.0F + (float)param0.level.random.nextInt((int)var0);
        } else {
            var1 = var0;
        }

        boolean var3 = param1.hurt(DamageSource.mobAttack(param0), var1);
        if (var3) {
            param0.doEnchantDamageEffects(param0, param1);
            if (!param0.isBaby()) {
                throwTarget(param0, param1);
            }
        }

        return var3;
    }

    static void throwTarget(LivingEntity param0, LivingEntity param1) {
        double var0 = param0.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        double var1 = param1.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        double var2 = var0 - var1;
        if (!(var2 <= 0.0)) {
            double var3 = param1.getX() - param0.getX();
            double var4 = param1.getZ() - param0.getZ();
            float var5 = (float)(param0.level.random.nextInt(21) - 10);
            double var6 = var2 * (double)(param0.level.random.nextFloat() * 0.5F + 0.2F);
            Vec3 var7 = new Vec3(var3, 0.0, var4).normalize().scale(var6).yRot(var5);
            double var8 = var2 * (double)param0.level.random.nextFloat() * 0.5;
            param1.push(var7.x, var8, var7.z);
            param1.hurtMarked = true;
        }
    }
}
