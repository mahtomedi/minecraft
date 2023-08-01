package net.minecraft.world.effect;

import net.minecraft.world.entity.LivingEntity;

class AbsorptionMobEffect extends MobEffect {
    protected AbsorptionMobEffect(MobEffectCategory param0, int param1) {
        super(param0, param1);
    }

    @Override
    public void applyEffectTick(LivingEntity param0, int param1) {
        super.applyEffectTick(param0, param1);
        if (param0.getAbsorptionAmount() <= 0.0F) {
            param0.removeEffect(this);
        }

    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int param0, int param1) {
        return true;
    }

    @Override
    public void onEffectStarted(LivingEntity param0, int param1) {
        super.onEffectStarted(param0, param1);
        param0.setAbsorptionAmount(param0.getAbsorptionAmount() + (float)(4 * (1 + param1)));
    }
}
