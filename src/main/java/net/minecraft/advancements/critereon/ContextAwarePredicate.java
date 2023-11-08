package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class ContextAwarePredicate {
    public static final Codec<ContextAwarePredicate> CODEC = LootItemConditions.CODEC.listOf().xmap(ContextAwarePredicate::new, param0 -> param0.conditions);
    private final List<LootItemCondition> conditions;
    private final Predicate<LootContext> compositePredicates;

    ContextAwarePredicate(List<LootItemCondition> param0) {
        this.conditions = param0;
        this.compositePredicates = LootItemConditions.andConditions(param0);
    }

    public static ContextAwarePredicate create(LootItemCondition... param0) {
        return new ContextAwarePredicate(List.of(param0));
    }

    public boolean matches(LootContext param0) {
        return this.compositePredicates.test(param0);
    }

    public void validate(ValidationContext param0) {
        for(int var0 = 0; var0 < this.conditions.size(); ++var0) {
            LootItemCondition var1 = this.conditions.get(var0);
            var1.validate(param0.forChild("[" + var0 + "]"));
        }

    }
}
