package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTableProblemCollector;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public class InvertedLootItemCondition implements LootItemCondition {
    private final LootItemCondition term;

    private InvertedLootItemCondition(LootItemCondition param0) {
        this.term = param0;
    }

    public final boolean test(LootContext param0) {
        return !this.term.test(param0);
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.term.getReferencedContextParams();
    }

    @Override
    public void validate(
        LootTableProblemCollector param0, Function<ResourceLocation, LootTable> param1, Set<ResourceLocation> param2, LootContextParamSet param3
    ) {
        LootItemCondition.super.validate(param0, param1, param2, param3);
        this.term.validate(param0, param1, param2, param3);
    }

    public static LootItemCondition.Builder invert(LootItemCondition.Builder param0) {
        InvertedLootItemCondition var0 = new InvertedLootItemCondition(param0.build());
        return () -> var0;
    }

    public static class Serializer extends LootItemCondition.Serializer<InvertedLootItemCondition> {
        public Serializer() {
            super(new ResourceLocation("inverted"), InvertedLootItemCondition.class);
        }

        public void serialize(JsonObject param0, InvertedLootItemCondition param1, JsonSerializationContext param2) {
            param0.add("term", param2.serialize(param1.term));
        }

        public InvertedLootItemCondition deserialize(JsonObject param0, JsonDeserializationContext param1) {
            LootItemCondition var0 = GsonHelper.getAsObject(param0, "term", param1, LootItemCondition.class);
            return new InvertedLootItemCondition(var0);
        }
    }
}
