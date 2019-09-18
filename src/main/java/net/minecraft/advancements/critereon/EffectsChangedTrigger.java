package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class EffectsChangedTrigger extends SimpleCriterionTrigger<EffectsChangedTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("effects_changed");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public EffectsChangedTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        MobEffectsPredicate var0 = MobEffectsPredicate.fromJson(param0.get("effects"));
        return new EffectsChangedTrigger.TriggerInstance(var0);
    }

    public void trigger(ServerPlayer param0) {
        this.trigger(param0.getAdvancements(), param1 -> param1.matches(param0));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MobEffectsPredicate effects;

        public TriggerInstance(MobEffectsPredicate param0) {
            super(EffectsChangedTrigger.ID);
            this.effects = param0;
        }

        public static EffectsChangedTrigger.TriggerInstance hasEffects(MobEffectsPredicate param0) {
            return new EffectsChangedTrigger.TriggerInstance(param0);
        }

        public boolean matches(ServerPlayer param0) {
            return this.effects.matches((LivingEntity)param0);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("effects", this.effects.serializeToJson());
            return var0;
        }
    }
}
