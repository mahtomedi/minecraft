package net.minecraft.advancements;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class Criterion {
    private final CriterionTriggerInstance trigger;

    public Criterion(CriterionTriggerInstance param0) {
        this.trigger = param0;
    }

    public Criterion() {
        this.trigger = null;
    }

    public void serializeToNetwork(FriendlyByteBuf param0) {
    }

    public static Criterion criterionFromJson(JsonObject param0, JsonDeserializationContext param1) {
        ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "trigger"));
        CriterionTrigger<?> var1 = CriteriaTriggers.getCriterion(var0);
        if (var1 == null) {
            throw new JsonSyntaxException("Invalid criterion trigger: " + var0);
        } else {
            CriterionTriggerInstance var2 = var1.createInstance(GsonHelper.getAsJsonObject(param0, "conditions", new JsonObject()), param1);
            return new Criterion(var2);
        }
    }

    public static Criterion criterionFromNetwork(FriendlyByteBuf param0) {
        return new Criterion();
    }

    public static Map<String, Criterion> criteriaFromJson(JsonObject param0, JsonDeserializationContext param1) {
        Map<String, Criterion> var0 = Maps.newHashMap();

        for(Entry<String, JsonElement> var1 : param0.entrySet()) {
            var0.put(var1.getKey(), criterionFromJson(GsonHelper.convertToJsonObject(var1.getValue(), "criterion"), param1));
        }

        return var0;
    }

    public static Map<String, Criterion> criteriaFromNetwork(FriendlyByteBuf param0) {
        Map<String, Criterion> var0 = Maps.newHashMap();
        int var1 = param0.readVarInt();

        for(int var2 = 0; var2 < var1; ++var2) {
            var0.put(param0.readUtf(32767), criterionFromNetwork(param0));
        }

        return var0;
    }

    public static void serializeToNetwork(Map<String, Criterion> param0, FriendlyByteBuf param1) {
        param1.writeVarInt(param0.size());

        for(Entry<String, Criterion> var0 : param0.entrySet()) {
            param1.writeUtf(var0.getKey());
            var0.getValue().serializeToNetwork(param1);
        }

    }

    @Nullable
    public CriterionTriggerInstance getTrigger() {
        return this.trigger;
    }

    public JsonElement serializeToJson() {
        JsonObject var0 = new JsonObject();
        var0.addProperty("trigger", this.trigger.getCriterion().toString());
        var0.add("conditions", this.trigger.serializeToJson());
        return var0;
    }
}
