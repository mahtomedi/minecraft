package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;

public class EffectsChangedTrigger extends SimpleCriterionTrigger<EffectsChangedTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("effects_changed");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public EffectsChangedTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        MobEffectsPredicate var0 = MobEffectsPredicate.fromJson(param0.get("effects"));
        EntityPredicate.Composite var1 = EntityPredicate.Composite.fromJson(param0, "source", param2);
        return new EffectsChangedTrigger.TriggerInstance(param1, var0, var1);
    }

    public void trigger(ServerPlayer param0, @Nullable Entity param1) {
        LootContext var0 = param1 != null ? EntityPredicate.createContext(param0, param1) : null;
        this.trigger(param0, param2 -> param2.matches(param0, var0));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MobEffectsPredicate effects;
        private final EntityPredicate.Composite source;

        public TriggerInstance(EntityPredicate.Composite param0, MobEffectsPredicate param1, EntityPredicate.Composite param2) {
            super(EffectsChangedTrigger.ID, param0);
            this.effects = param1;
            this.source = param2;
        }

        public static EffectsChangedTrigger.TriggerInstance hasEffects(MobEffectsPredicate param0) {
            return new EffectsChangedTrigger.TriggerInstance(EntityPredicate.Composite.ANY, param0, EntityPredicate.Composite.ANY);
        }

        public static EffectsChangedTrigger.TriggerInstance gotEffectsFrom(EntityPredicate param0) {
            return new EffectsChangedTrigger.TriggerInstance(EntityPredicate.Composite.ANY, MobEffectsPredicate.ANY, EntityPredicate.Composite.wrap(param0));
        }

        public boolean matches(ServerPlayer param0, @Nullable LootContext param1) {
            if (!this.effects.matches((LivingEntity)param0)) {
                return false;
            } else {
                return this.source == EntityPredicate.Composite.ANY || param1 != null && this.source.matches(param1);
            }
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.add("effects", this.effects.serializeToJson());
            var0.add("source", this.source.toJson(param0));
            return var0;
        }
    }
}
