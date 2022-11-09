package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;

public class ChangeDimensionTrigger extends SimpleCriterionTrigger<ChangeDimensionTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("changed_dimension");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public ChangeDimensionTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
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

        public TriggerInstance(EntityPredicate.Composite param0, @Nullable ResourceKey<Level> param1, @Nullable ResourceKey<Level> param2) {
            super(ChangeDimensionTrigger.ID, param0);
            this.from = param1;
            this.to = param2;
        }

        public static ChangeDimensionTrigger.TriggerInstance changedDimension() {
            return new ChangeDimensionTrigger.TriggerInstance(EntityPredicate.Composite.ANY, null, null);
        }

        public static ChangeDimensionTrigger.TriggerInstance changedDimension(ResourceKey<Level> param0, ResourceKey<Level> param1) {
            return new ChangeDimensionTrigger.TriggerInstance(EntityPredicate.Composite.ANY, param0, param1);
        }

        public static ChangeDimensionTrigger.TriggerInstance changedDimensionTo(ResourceKey<Level> param0) {
            return new ChangeDimensionTrigger.TriggerInstance(EntityPredicate.Composite.ANY, null, param0);
        }

        public static ChangeDimensionTrigger.TriggerInstance changedDimensionFrom(ResourceKey<Level> param0) {
            return new ChangeDimensionTrigger.TriggerInstance(EntityPredicate.Composite.ANY, param0, null);
        }

        public boolean matches(ResourceKey<Level> param0, ResourceKey<Level> param1) {
            if (this.from != null && this.from != param0) {
                return false;
            } else {
                return this.to == null || this.to == param1;
            }
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
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
