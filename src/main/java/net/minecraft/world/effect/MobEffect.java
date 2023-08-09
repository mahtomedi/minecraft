package net.minecraft.world.effect;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class MobEffect {
    private final Map<Attribute, AttributeModifierTemplate> attributeModifiers = Maps.newHashMap();
    private final MobEffectCategory category;
    private final int color;
    @Nullable
    private String descriptionId;
    private Supplier<MobEffectInstance.FactorData> factorDataFactory = () -> null;
    private final Holder.Reference<MobEffect> builtInRegistryHolder = BuiltInRegistries.MOB_EFFECT.createIntrusiveHolder(this);

    protected MobEffect(MobEffectCategory param0, int param1) {
        this.category = param0;
        this.color = param1;
    }

    public Optional<MobEffectInstance.FactorData> createFactorData() {
        return Optional.ofNullable(this.factorDataFactory.get());
    }

    public void applyEffectTick(LivingEntity param0, int param1) {
    }

    public void applyInstantenousEffect(@Nullable Entity param0, @Nullable Entity param1, LivingEntity param2, int param3, double param4) {
        this.applyEffectTick(param2, param3);
    }

    public boolean shouldApplyEffectTickThisTick(int param0, int param1) {
        return false;
    }

    public void onEffectStarted(LivingEntity param0, int param1) {
    }

    public boolean isInstantenous() {
        return false;
    }

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("effect", BuiltInRegistries.MOB_EFFECT.getKey(this));
        }

        return this.descriptionId;
    }

    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    public Component getDisplayName() {
        return Component.translatable(this.getDescriptionId());
    }

    public MobEffectCategory getCategory() {
        return this.category;
    }

    public int getColor() {
        return this.color;
    }

    public MobEffect addAttributeModifier(Attribute param0, String param1, double param2, AttributeModifier.Operation param3) {
        this.attributeModifiers.put(param0, new MobEffect.MobEffectAttributeModifierTemplate(UUID.fromString(param1), param2, param3));
        return this;
    }

    public MobEffect setFactorDataFactory(Supplier<MobEffectInstance.FactorData> param0) {
        this.factorDataFactory = param0;
        return this;
    }

    public Map<Attribute, AttributeModifierTemplate> getAttributeModifiers() {
        return this.attributeModifiers;
    }

    public void removeAttributeModifiers(AttributeMap param0) {
        for(Entry<Attribute, AttributeModifierTemplate> var0 : this.attributeModifiers.entrySet()) {
            AttributeInstance var1 = param0.getInstance(var0.getKey());
            if (var1 != null) {
                var1.removeModifier(var0.getValue().getAttributeModifierId());
            }
        }

    }

    public void addAttributeModifiers(AttributeMap param0, int param1) {
        for(Entry<Attribute, AttributeModifierTemplate> var0 : this.attributeModifiers.entrySet()) {
            AttributeInstance var1 = param0.getInstance(var0.getKey());
            if (var1 != null) {
                var1.removeModifier(var0.getValue().getAttributeModifierId());
                var1.addPermanentModifier(var0.getValue().create(param1));
            }
        }

    }

    public boolean isBeneficial() {
        return this.category == MobEffectCategory.BENEFICIAL;
    }

    @Deprecated
    public Holder.Reference<MobEffect> builtInRegistryHolder() {
        return this.builtInRegistryHolder;
    }

    class MobEffectAttributeModifierTemplate implements AttributeModifierTemplate {
        private final UUID id;
        private final double amount;
        private final AttributeModifier.Operation operation;

        public MobEffectAttributeModifierTemplate(UUID param0, double param1, AttributeModifier.Operation param2) {
            this.id = param0;
            this.amount = param1;
            this.operation = param2;
        }

        @Override
        public UUID getAttributeModifierId() {
            return this.id;
        }

        @Override
        public AttributeModifier create(int param0) {
            return new AttributeModifier(this.id, MobEffect.this.getDescriptionId() + " " + param0, this.amount * (double)(param0 + 1), this.operation);
        }
    }
}
