package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import org.slf4j.Logger;

public class ConditionReference implements LootItemCondition {
    private static final Logger LOGGER = LogUtils.getLogger();
    final ResourceLocation name;

    ConditionReference(ResourceLocation param0) {
        this.name = param0;
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.REFERENCE;
    }

    @Override
    public void validate(ValidationContext param0) {
        if (param0.hasVisitedCondition(this.name)) {
            param0.reportProblem("Condition " + this.name + " is recursively called");
        } else {
            LootItemCondition.super.validate(param0);
            LootItemCondition var0 = param0.resolveCondition(this.name);
            if (var0 == null) {
                param0.reportProblem("Unknown condition table called " + this.name);
            } else {
                var0.validate(param0.enterTable(".{" + this.name + "}", this.name));
            }

        }
    }

    public boolean test(LootContext param0) {
        LootItemCondition var0 = param0.getCondition(this.name);
        if (param0.addVisitedCondition(var0)) {
            boolean var3;
            try {
                var3 = var0.test(param0);
            } finally {
                param0.removeVisitedCondition(var0);
            }

            return var3;
        } else {
            LOGGER.warn("Detected infinite loop in loot tables");
            return false;
        }
    }

    public static LootItemCondition.Builder conditionReference(ResourceLocation param0) {
        return () -> new ConditionReference(param0);
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ConditionReference> {
        public void serialize(JsonObject param0, ConditionReference param1, JsonSerializationContext param2) {
            param0.addProperty("name", param1.name.toString());
        }

        public ConditionReference deserialize(JsonObject param0, JsonDeserializationContext param1) {
            ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "name"));
            return new ConditionReference(var0);
        }
    }
}
