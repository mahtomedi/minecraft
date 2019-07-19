package net.minecraft.world.effect;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttackDamageMobEffect extends MobEffect {
    protected final double multiplier;

    protected AttackDamageMobEffect(MobEffectCategory param0, int param1, double param2) {
        super(param0, param1);
        this.multiplier = param2;
    }

    @Override
    public double getAttributeModifierValue(int param0, AttributeModifier param1) {
        return this.multiplier * (double)(param0 + 1);
    }
}
