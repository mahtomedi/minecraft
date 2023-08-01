package net.minecraft.world.effect;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

class HealOrHarmMobEffect extends InstantenousMobEffect {
    private final boolean isHarm;

    public HealOrHarmMobEffect(MobEffectCategory param0, int param1, boolean param2) {
        super(param0, param1);
        this.isHarm = param2;
    }

    @Override
    public void applyEffectTick(LivingEntity param0, int param1) {
        super.applyEffectTick(param0, param1);
        if (this.isHarm == param0.isInvertedHealAndHarm()) {
            param0.heal((float)Math.max(4 << param1, 0));
        } else {
            param0.hurt(param0.damageSources().magic(), (float)(6 << param1));
        }

    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity param0, @Nullable Entity param1, LivingEntity param2, int param3, double param4) {
        if (this.isHarm == param2.isInvertedHealAndHarm()) {
            int var0 = (int)(param4 * (double)(4 << param3) + 0.5);
            param2.heal((float)var0);
        } else {
            int var1 = (int)(param4 * (double)(6 << param3) + 0.5);
            if (param0 == null) {
                param2.hurt(param2.damageSources().magic(), (float)var1);
            } else {
                param2.hurt(param2.damageSources().indirectMagic(param0, param1), (float)var1);
            }
        }

    }
}
