package net.minecraft.world.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class AbsoptionMobEffect extends MobEffect {
    protected AbsoptionMobEffect(MobEffectCategory param0, int param1) {
        super(param0, param1);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity param0, AttributeMap param1, int param2) {
        param0.setAbsorptionAmount(param0.getAbsorptionAmount() - (float)(4 * (param2 + 1)));
        super.removeAttributeModifiers(param0, param1, param2);
    }

    @Override
    public void addAttributeModifiers(LivingEntity param0, AttributeMap param1, int param2) {
        param0.setAbsorptionAmount(param0.getAbsorptionAmount() + (float)(4 * (param2 + 1)));
        super.addAttributeModifiers(param0, param1, param2);
    }
}
