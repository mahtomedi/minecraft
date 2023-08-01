package net.minecraft.world.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

class HungerMobEffect extends MobEffect {
    protected HungerMobEffect(MobEffectCategory param0, int param1) {
        super(param0, param1);
    }

    @Override
    public void applyEffectTick(LivingEntity param0, int param1) {
        super.applyEffectTick(param0, param1);
        if (param0 instanceof Player var0) {
            var0.causeFoodExhaustion(0.005F * (float)(param1 + 1));
        }

    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int param0, int param1) {
        return true;
    }
}
