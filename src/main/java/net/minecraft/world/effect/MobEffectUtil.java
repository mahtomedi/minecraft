package net.minecraft.world.effect;

import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.LivingEntity;

public final class MobEffectUtil {
    public static String formatDuration(MobEffectInstance param0, float param1) {
        if (param0.isNoCounter()) {
            return "**:**";
        } else {
            int var0 = Mth.floor((float)param0.getDuration() * param1);
            return StringUtil.formatTickDuration(var0);
        }
    }

    public static boolean hasDigSpeed(LivingEntity param0) {
        return param0.hasEffect(MobEffects.DIG_SPEED) || param0.hasEffect(MobEffects.CONDUIT_POWER);
    }

    public static int getDigSpeedAmplification(LivingEntity param0) {
        int var0 = 0;
        int var1 = 0;
        if (param0.hasEffect(MobEffects.DIG_SPEED)) {
            var0 = param0.getEffect(MobEffects.DIG_SPEED).getAmplifier();
        }

        if (param0.hasEffect(MobEffects.CONDUIT_POWER)) {
            var1 = param0.getEffect(MobEffects.CONDUIT_POWER).getAmplifier();
        }

        return Math.max(var0, var1);
    }

    public static boolean hasWaterBreathing(LivingEntity param0) {
        return param0.hasEffect(MobEffects.WATER_BREATHING) || param0.hasEffect(MobEffects.CONDUIT_POWER);
    }
}
