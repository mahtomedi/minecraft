package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
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

    public EffectsChangedTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        Optional<MobEffectsPredicate> var0 = MobEffectsPredicate.fromJson(param0.get("effects"));
        Optional<ContextAwarePredicate> var1 = EntityPredicate.fromJson(param0, "source", param2);
        return new EffectsChangedTrigger.TriggerInstance(param1, var0, var1);
    }

    public void trigger(ServerPlayer param0, @Nullable Entity param1) {
        LootContext var0 = param1 != null ? EntityPredicate.createContext(param0, param1) : null;
        this.trigger(param0, param2 -> param2.matches(param0, var0));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<MobEffectsPredicate> effects;
        private final Optional<ContextAwarePredicate> source;

        public TriggerInstance(Optional<ContextAwarePredicate> param0, Optional<MobEffectsPredicate> param1, Optional<ContextAwarePredicate> param2) {
            super(EffectsChangedTrigger.ID, param0);
            this.effects = param1;
            this.source = param2;
        }

        public static EffectsChangedTrigger.TriggerInstance hasEffects(MobEffectsPredicate.Builder param0) {
            return new EffectsChangedTrigger.TriggerInstance(Optional.empty(), param0.build(), Optional.empty());
        }

        public static EffectsChangedTrigger.TriggerInstance gotEffectsFrom(Optional<EntityPredicate> param0) {
            return new EffectsChangedTrigger.TriggerInstance(Optional.empty(), Optional.empty(), EntityPredicate.wrap(param0));
        }

        public boolean matches(ServerPlayer param0, @Nullable LootContext param1) {
            if (this.effects.isPresent() && !this.effects.get().matches((LivingEntity)param0)) {
                return false;
            } else {
                return !this.source.isPresent() || param1 != null && this.source.get().matches(param1);
            }
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            this.effects.ifPresent(param1 -> var0.add("effects", param1.serializeToJson()));
            this.source.ifPresent(param1 -> var0.add("source", param1.toJson()));
            return var0;
        }
    }
}
