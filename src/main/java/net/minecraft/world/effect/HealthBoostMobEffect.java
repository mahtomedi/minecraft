package net.minecraft.world.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class HealthBoostMobEffect extends MobEffect {
    public HealthBoostMobEffect(MobEffectCategory param0, int param1) {
        super(param0, param1);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity param0, AttributeMap param1, int param2) {
        super.removeAttributeModifiers(param0, param1, param2);
        if (param0.getHealth() > param0.getMaxHealth()) {
            param0.setHealth(param0.getMaxHealth());
        }

    }
}
