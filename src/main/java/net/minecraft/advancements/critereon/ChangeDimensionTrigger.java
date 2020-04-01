package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
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

    public ChangeDimensionTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        EntityPredicate var0 = EntityPredicate.fromJson(param0.get("player"));
        DimensionType var1 = param0.has("from") ? DimensionType.getByName(new ResourceLocation(GsonHelper.getAsString(param0, "from"))) : null;
        DimensionType var2 = param0.has("to") ? DimensionType.getByName(new ResourceLocation(GsonHelper.getAsString(param0, "to"))) : null;
        return new ChangeDimensionTrigger.TriggerInstance(var1, var2, var0);
    }

    public void trigger(ServerPlayer param0, DimensionType param1, DimensionType param2) {
        this.trigger(param0.getAdvancements(), param3 -> param3.matches(param0, param1, param2));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        @Nullable
        private final DimensionType from;
        @Nullable
        private final DimensionType to;
        private final EntityPredicate entity;

        public TriggerInstance(@Nullable DimensionType param0, @Nullable DimensionType param1, EntityPredicate param2) {
            super(ChangeDimensionTrigger.ID);
            this.from = param0;
            this.to = param1;
            this.entity = param2;
        }

        public static ChangeDimensionTrigger.TriggerInstance changedDimension(EntityPredicate param0) {
            return new ChangeDimensionTrigger.TriggerInstance(null, null, param0);
        }

        public static ChangeDimensionTrigger.TriggerInstance changedDimensionTo(DimensionType param0) {
            return new ChangeDimensionTrigger.TriggerInstance(null, param0, EntityPredicate.ANY);
        }

        public boolean matches(ServerPlayer param0, DimensionType param1, DimensionType param2) {
            if (!this.entity.matches(param0, param0)) {
                return false;
            } else if (this.from != null && this.from != param1) {
                return false;
            } else {
                return this.to == null || this.to == param2;
            }
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            if (this.from != null) {
                var0.addProperty("from", DimensionType.getName(this.from).toString());
            }

            if (this.to != null) {
                var0.addProperty("to", DimensionType.getName(this.to).toString());
            }

            var0.add("player", this.entity.serializeToJson());
            return var0;
        }
    }
}
