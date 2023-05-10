package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class ContextAwarePredicate {
    public static final ContextAwarePredicate ANY = new ContextAwarePredicate(new LootItemCondition[0]);
    private final LootItemCondition[] conditions;
    private final Predicate<LootContext> compositePredicates;

    ContextAwarePredicate(LootItemCondition[] param0) {
        this.conditions = param0;
        this.compositePredicates = LootItemConditions.andConditions(param0);
    }

    public static ContextAwarePredicate create(LootItemCondition... param0) {
        return new ContextAwarePredicate(param0);
    }

    @Nullable
    public static ContextAwarePredicate fromElement(String param0, DeserializationContext param1, @Nullable JsonElement param2, LootContextParamSet param3) {
        if (param2 != null && param2.isJsonArray()) {
            LootItemCondition[] var0 = param1.deserializeConditions(param2.getAsJsonArray(), param1.getAdvancementId() + "/" + param0, param3);
            return new ContextAwarePredicate(var0);
        } else {
            return null;
        }
    }

    public boolean matches(LootContext param0) {
        return this.compositePredicates.test(param0);
    }

    public JsonElement toJson(SerializationContext param0) {
        return (JsonElement)(this.conditions.length == 0 ? JsonNull.INSTANCE : param0.serializeConditions(this.conditions));
    }

    public static JsonElement toJson(ContextAwarePredicate[] param0, SerializationContext param1) {
        if (param0.length == 0) {
            return JsonNull.INSTANCE;
        } else {
            JsonArray var0 = new JsonArray();

            for(ContextAwarePredicate var1 : param0) {
                var0.add(var1.toJson(param1));
            }

            return var0;
        }
    }
}
