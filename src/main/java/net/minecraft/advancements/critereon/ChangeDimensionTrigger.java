package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;

public class ChangeDimensionTrigger extends SimpleCriterionTrigger<ChangeDimensionTrigger.TriggerInstance> {
    public ChangeDimensionTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        ResourceKey<Level> var0 = param0.has("from")
            ? ResourceKey.create(Registries.DIMENSION, new ResourceLocation(GsonHelper.getAsString(param0, "from")))
            : null;
        ResourceKey<Level> var1 = param0.has("to")
            ? ResourceKey.create(Registries.DIMENSION, new ResourceLocation(GsonHelper.getAsString(param0, "to")))
            : null;
        return new ChangeDimensionTrigger.TriggerInstance(param1, var0, var1);
    }

    public void trigger(ServerPlayer param0, ResourceKey<Level> param1, ResourceKey<Level> param2) {
        this.trigger(param0, param2x -> param2x.matches(param1, param2));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        @Nullable
        private final ResourceKey<Level> from;
        @Nullable
        private final ResourceKey<Level> to;

        public TriggerInstance(Optional<ContextAwarePredicate> param0, @Nullable ResourceKey<Level> param1, @Nullable ResourceKey<Level> param2) {
            super(param0);
            this.from = param1;
            this.to = param2;
        }

        public static Criterion<ChangeDimensionTrigger.TriggerInstance> changedDimension() {
            return CriteriaTriggers.CHANGED_DIMENSION.createCriterion(new ChangeDimensionTrigger.TriggerInstance(Optional.empty(), null, null));
        }

        public static Criterion<ChangeDimensionTrigger.TriggerInstance> changedDimension(ResourceKey<Level> param0, ResourceKey<Level> param1) {
            return CriteriaTriggers.CHANGED_DIMENSION.createCriterion(new ChangeDimensionTrigger.TriggerInstance(Optional.empty(), param0, param1));
        }

        public static Criterion<ChangeDimensionTrigger.TriggerInstance> changedDimensionTo(ResourceKey<Level> param0) {
            return CriteriaTriggers.CHANGED_DIMENSION.createCriterion(new ChangeDimensionTrigger.TriggerInstance(Optional.empty(), null, param0));
        }

        public static Criterion<ChangeDimensionTrigger.TriggerInstance> changedDimensionFrom(ResourceKey<Level> param0) {
            return CriteriaTriggers.CHANGED_DIMENSION.createCriterion(new ChangeDimensionTrigger.TriggerInstance(Optional.empty(), param0, null));
        }

        public boolean matches(ResourceKey<Level> param0, ResourceKey<Level> param1) {
            if (this.from != null && this.from != param0) {
                return false;
            } else {
                return this.to == null || this.to == param1;
            }
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            if (this.from != null) {
                var0.addProperty("from", this.from.location().toString());
            }

            if (this.to != null) {
                var0.addProperty("to", this.to.location().toString());
            }

            return var0;
        }
    }
}
