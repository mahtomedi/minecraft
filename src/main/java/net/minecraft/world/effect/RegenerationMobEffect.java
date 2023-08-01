package net.minecraft.world.effect;

import net.minecraft.world.entity.LivingEntity;

class RegenerationMobEffect extends MobEffect {
    protected RegenerationMobEffect(MobEffectCategory param0, int param1) {
        super(param0, param1);
    }

    @Override
    public void applyEffectTick(LivingEntity param0, int param1) {
        super.applyEffectTick(param0, param1);
        if (param0.getHealth() < param0.getMaxHealth()) {
            param0.heal(1.0F);
        }

    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int param0, int param1) {
        int var0 = 50 >> param1;
        if (var0 > 0) {
            return param0 % var0 == 0;
        } else {
            return true;
        }
    }
}
