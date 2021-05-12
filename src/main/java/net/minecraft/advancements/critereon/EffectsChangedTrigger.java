package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class EffectsChangedTrigger extends SimpleCriterionTrigger<EffectsChangedTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("effects_changed");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public EffectsChangedTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        MobEffectsPredicate var0 = MobEffectsPredicate.fromJson(param0.get("effects"));
        return new EffectsChangedTrigger.TriggerInstance(param1, var0);
    }

    public void trigger(ServerPlayer param0) {
        this.trigger(param0, param1 -> param1.matches(param0));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MobEffectsPredicate effects;

        public TriggerInstance(EntityPredicate.Composite param0, MobEffectsPredicate param1) {
            super(EffectsChangedTrigger.ID, param0);
            this.effects = param1;
        }

        public static EffectsChangedTrigger.TriggerInstance hasEffects(MobEffectsPredicate param0) {
            return new EffectsChangedTrigger.TriggerInstance(EntityPredicate.Composite.ANY, param0);
        }

        public boolean matches(ServerPlayer param0) {
            return this.effects.matches((LivingEntity)param0);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.add("effects", this.effects.serializeToJson());
            return var0;
        }
    }
}
