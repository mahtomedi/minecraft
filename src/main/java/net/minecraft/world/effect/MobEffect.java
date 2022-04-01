package net.minecraft.world.effect;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

public class MobEffect {
    private final Map<Attribute, AttributeModifier> attributeModifiers = Maps.newHashMap();
    private final MobEffectCategory category;
    private final int color;
    @Nullable
    private String descriptionId;

    @Nullable
    public static MobEffect byId(int param0) {
        return Registry.MOB_EFFECT.byId(param0);
    }

    public static int getId(MobEffect param0) {
        return Registry.MOB_EFFECT.getId(param0);
    }

    protected MobEffect(MobEffectCategory param0, int param1) {
        this.category = param0;
        this.color = param1;
    }

    public void applyEffectTick(LivingEntity param0, int param1) {
        if (this == MobEffects.REGENERATION) {
            if (param0.getHealth() < param0.getMaxHealth()) {
                param0.heal(1.0F);
            }
        } else if (this == MobEffects.POISON) {
            if (param0.getHealth() > 1.0F) {
                param0.hurt(DamageSource.MAGIC, 1.0F);
            }
        } else if (this == MobEffects.WITHER) {
            param0.hurt(DamageSource.WITHER, 1.0F);
        } else if (this == MobEffects.HUNGER && param0 instanceof Player) {
            ((Player)param0).causeFoodExhaustion(0.005F * (float)(param1 + 1));
        } else if (this == MobEffects.SATURATION && param0 instanceof Player) {
            if (!param0.level.isClientSide) {
                ((Player)param0).getFoodData().eat(param1 + 1, 1.0F);
            }
        } else if ((this != MobEffects.HEAL || param0.isInvertedHealAndHarm()) && (this != MobEffects.HARM || !param0.isInvertedHealAndHarm())) {
            if (this == MobEffects.HARM && !param0.isInvertedHealAndHarm() || this == MobEffects.HEAL && param0.isInvertedHealAndHarm()) {
                param0.hurt(DamageSource.MAGIC, (float)(6 << param1));
            }
        } else {
            param0.heal((float)Math.max(4 << param1, 0));
        }

    }

    public void applyInstantenousEffect(@Nullable Entity param0, @Nullable Entity param1, LivingEntity param2, int param3, double param4) {
        if ((this != MobEffects.HEAL || param2.isInvertedHealAndHarm()) && (this != MobEffects.HARM || !param2.isInvertedHealAndHarm())) {
            if (this == MobEffects.HARM && !param2.isInvertedHealAndHarm() || this == MobEffects.HEAL && param2.isInvertedHealAndHarm()) {
                int var1 = (int)(param4 * (double)(6 << param3) + 0.5);
                if (param0 == null) {
                    param2.hurt(DamageSource.MAGIC, (float)var1);
                } else {
                    param2.hurt(DamageSource.indirectMagic(param0, param1), (float)var1);
                }
            } else {
                this.applyEffectTick(param2, param3);
            }
        } else {
            int var0 = (int)(param4 * (double)(4 << param3) + 0.5);
            param2.heal((float)var0);
        }

    }

    public boolean isDurationEffectTick(int param0, int param1) {
        if (this == MobEffects.REGENERATION) {
            int var0 = 50 >> param1;
            if (var0 > 0) {
                return param0 % var0 == 0;
            } else {
                return true;
            }
        } else if (this == MobEffects.POISON) {
            int var1 = 25 >> param1;
            if (var1 > 0) {
                return param0 % var1 == 0;
            } else {
                return true;
            }
        } else if (this == MobEffects.WITHER) {
            int var2 = 40 >> param1;
            if (var2 > 0) {
                return param0 % var2 == 0;
            } else {
                return true;
            }
        } else {
            return this == MobEffects.HUNGER;
        }
    }

    public boolean isInstantenous() {
        return false;
    }

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("effect", Registry.MOB_EFFECT.getKey(this));
        }

        return this.descriptionId;
    }

    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    public Component getDisplayName() {
        return new TranslatableComponent(this.getDescriptionId());
    }

    public MobEffectCategory getCategory() {
        return this.category;
    }

    public int getColor() {
        return this.color;
    }

    public MobEffect addAttributeModifier(Attribute param0, String param1, double param2, AttributeModifier.Operation param3) {
        AttributeModifier var0 = new AttributeModifier(UUID.fromString(param1), this::getDescriptionId, param2, param3);
        this.attributeModifiers.put(param0, var0);
        return this;
    }

    public Map<Attribute, AttributeModifier> getAttributeModifiers() {
        return this.attributeModifiers;
    }

    public void removeAttributeModifiers(LivingEntity param0, AttributeMap param1, int param2) {
        for(Entry<Attribute, AttributeModifier> var0 : this.attributeModifiers.entrySet()) {
            AttributeInstance var1 = param1.getInstance(var0.getKey());
            if (var1 != null) {
                var1.removeModifier(var0.getValue());
            }
        }

    }

    public void addAttributeModifiers(LivingEntity param0, AttributeMap param1, int param2) {
        for(Entry<Attribute, AttributeModifier> var0 : this.attributeModifiers.entrySet()) {
            AttributeInstance var1 = param1.getInstance(var0.getKey());
            if (var1 != null) {
                AttributeModifier var2 = var0.getValue();
                var1.removeModifier(var2);
                var1.addPermanentModifier(
                    new AttributeModifier(
                        var2.getId(), this.getDescriptionId() + " " + param2, this.getAttributeModifierValue(param2, var2), var2.getOperation()
                    )
                );
            }
        }

    }

    public double getAttributeModifierValue(int param0, AttributeModifier param1) {
        return param1.getAmount() * (double)(param0 + 1);
    }

    public boolean isBeneficial() {
        return this.category == MobEffectCategory.BENEFICIAL;
    }
}
