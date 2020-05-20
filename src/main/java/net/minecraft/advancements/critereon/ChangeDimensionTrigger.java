package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.dimension.DimensionType;

public class ChangeDimensionTrigger extends SimpleCriterionTrigger<ChangeDimensionTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("changed_dimension");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public ChangeDimensionTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        ResourceKey<DimensionType> var0 = param0.has("from")
            ? ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation(GsonHelper.getAsString(param0, "from")))
            : null;
        ResourceKey<DimensionType> var1 = param0.has("to")
            ? ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation(GsonHelper.getAsString(param0, "to")))
            : null;
        return new ChangeDimensionTrigger.TriggerInstance(param1, var0, var1);
    }

    public void trigger(ServerPlayer param0, ResourceKey<DimensionType> param1, ResourceKey<DimensionType> param2) {
        this.trigger(param0, param2x -> param2x.matches(param1, param2));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        @Nullable
        private final ResourceKey<DimensionType> from;
        @Nullable
        private final ResourceKey<DimensionType> to;

        public TriggerInstance(EntityPredicate.Composite param0, @Nullable ResourceKey<DimensionType> param1, @Nullable ResourceKey<DimensionType> param2) {
            super(ChangeDimensionTrigger.ID, param0);
            this.from = param1;
            this.to = param2;
        }

        public static ChangeDimensionTrigger.TriggerInstance changedDimensionTo(ResourceKey<DimensionType> param0) {
            return new ChangeDimensionTrigger.TriggerInstance(EntityPredicate.Composite.ANY, null, param0);
        }

        public boolean matches(ResourceKey<DimensionType> param0, ResourceKey<DimensionType> param1) {
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
