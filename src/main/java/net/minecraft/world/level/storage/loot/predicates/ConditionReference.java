package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
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
        LootDataId<LootItemCondition> var0 = new LootDataId<>(LootDataType.PREDICATE, this.name);
        if (param0.hasVisitedElement(var0)) {
            param0.reportProblem("Condition " + this.name + " is recursively called");
        } else {
            LootItemCondition.super.validate(param0);
            param0.resolver()
                .getElementOptional(var0)
                .ifPresentOrElse(
                    param2 -> param2.validate(param0.enterElement(".{" + this.name + "}", var0)),
                    () -> param0.reportProblem("Unknown condition table called " + this.name)
                );
        }
    }

    public boolean test(LootContext param0) {
        LootItemCondition var0 = param0.getResolver().getElement(LootDataType.PREDICATE, this.name);
        if (var0 == null) {
            LOGGER.warn("Tried using unknown condition table called {}", this.name);
            return false;
        } else {
            LootContext.VisitedEntry<?> var1 = LootContext.createVisitedEntry(var0);
            if (param0.pushVisitedElement(var1)) {
                boolean var4;
                try {
                    var4 = var0.test(param0);
                } finally {
                    param0.popVisitedElement(var1);
                }

                return var4;
            } else {
                LOGGER.warn("Detected infinite loop in loot tables");
                return false;
            }
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
