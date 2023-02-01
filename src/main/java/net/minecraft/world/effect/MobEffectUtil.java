package net.minecraft.world.effect;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class MobEffectUtil {
    public static Component formatDuration(MobEffectInstance param0, float param1) {
        if (param0.isInfiniteDuration()) {
            return Component.translatable("effect.duration.infinite");
        } else {
            int var0 = Mth.floor((float)param0.getDuration() * param1);
            return Component.literal(StringUtil.formatTickDuration(var0));
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

    public static List<ServerPlayer> addEffectToPlayersAround(
        ServerLevel param0, @Nullable Entity param1, Vec3 param2, double param3, MobEffectInstance param4, int param5
    ) {
        MobEffect var0 = param4.getEffect();
        List<ServerPlayer> var1 = param0.getPlayers(
            param6 -> param6.gameMode.isSurvival()
                    && (param1 == null || !param1.isAlliedTo(param6))
                    && param2.closerThan(param6.position(), param3)
                    && (
                        !param6.hasEffect(var0)
                            || param6.getEffect(var0).getAmplifier() < param4.getAmplifier()
                            || param6.getEffect(var0).endsWithin(param5 - 1)
                    )
        );
        var1.forEach(param2x -> param2x.addEffect(new MobEffectInstance(param4), param1));
        return var1;
    }
}
