package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class ContextAwarePredicate {
    private final List<LootItemCondition> conditions;
    private final Predicate<LootContext> compositePredicates;

    ContextAwarePredicate(List<LootItemCondition> param0) {
        if (param0.isEmpty()) {
            throw new IllegalArgumentException("ContextAwarePredicate must have at least one condition");
        } else {
            this.conditions = param0;
            this.compositePredicates = LootItemConditions.andConditions(param0);
        }
    }

    public static ContextAwarePredicate create(LootItemCondition... param0) {
        return new ContextAwarePredicate(List.of(param0));
    }

    public static Optional<Optional<ContextAwarePredicate>> fromElement(
        String param0, DeserializationContext param1, @Nullable JsonElement param2, LootContextParamSet param3
    ) {
        if (param2 != null && param2.isJsonArray()) {
            List<LootItemCondition> var0 = param1.deserializeConditions(param2.getAsJsonArray(), param1.getAdvancementId() + "/" + param0, param3);
            return var0.isEmpty() ? Optional.of(Optional.empty()) : Optional.of(Optional.of(new ContextAwarePredicate(var0)));
        } else {
            return Optional.empty();
        }
    }

    public boolean matches(LootContext param0) {
        return this.compositePredicates.test(param0);
    }

    public JsonElement toJson() {
        return Util.getOrThrow(LootItemConditions.CODEC.listOf().encodeStart(JsonOps.INSTANCE, this.conditions), IllegalStateException::new);
    }

    public static JsonElement toJson(List<ContextAwarePredicate> param0) {
        if (param0.isEmpty()) {
            return JsonNull.INSTANCE;
        } else {
            JsonArray var0 = new JsonArray();

            for(ContextAwarePredicate var1 : param0) {
                var0.add(var1.toJson());
            }

            return var0;
        }
    }
}
