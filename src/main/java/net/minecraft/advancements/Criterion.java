package net.minecraft.advancements;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public record Criterion<T extends CriterionTriggerInstance>(CriterionTrigger<T> trigger, T triggerInstance) {
    public static Criterion<?> criterionFromJson(JsonObject param0, DeserializationContext param1) {
        ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "trigger"));
        CriterionTrigger<?> var1 = CriteriaTriggers.getCriterion(var0);
        if (var1 == null) {
            throw new JsonSyntaxException("Invalid criterion trigger: " + var0);
        } else {
            return criterionFromJson(param0, param1, var1);
        }
    }

    private static <T extends CriterionTriggerInstance> Criterion<T> criterionFromJson(
        JsonObject param0, DeserializationContext param1, CriterionTrigger<T> param2
    ) {
        T var0 = param2.createInstance(GsonHelper.getAsJsonObject(param0, "conditions", new JsonObject()), param1);
        return new Criterion<>(param2, var0);
    }

    public static Map<String, Criterion<?>> criteriaFromJson(JsonObject param0, DeserializationContext param1) {
        Map<String, Criterion<?>> var0 = Maps.newHashMap();

        for(Entry<String, JsonElement> var1 : param0.entrySet()) {
            var0.put(var1.getKey(), criterionFromJson(GsonHelper.convertToJsonObject(var1.getValue(), "criterion"), param1));
        }

        return var0;
    }

    public JsonElement serializeToJson() {
        JsonObject var0 = new JsonObject();
        var0.addProperty("trigger", Objects.requireNonNull(CriteriaTriggers.getId(this.trigger), "Unregistered trigger").toString());
        JsonObject var1 = this.triggerInstance.serializeToJson();
        if (var1.size() != 0) {
            var0.add("conditions", var1);
        }

        return var0;
    }
}
