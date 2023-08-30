package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SmithingTransformRecipeBuilder {
    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;
    private final RecipeCategory category;
    private final Item result;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    private final RecipeSerializer<?> type;

    public SmithingTransformRecipeBuilder(
        RecipeSerializer<?> param0, Ingredient param1, Ingredient param2, Ingredient param3, RecipeCategory param4, Item param5
    ) {
        this.category = param4;
        this.type = param0;
        this.template = param1;
        this.base = param2;
        this.addition = param3;
        this.result = param5;
    }

    public static SmithingTransformRecipeBuilder smithing(Ingredient param0, Ingredient param1, Ingredient param2, RecipeCategory param3, Item param4) {
        return new SmithingTransformRecipeBuilder(RecipeSerializer.SMITHING_TRANSFORM, param0, param1, param2, param3, param4);
    }

    public SmithingTransformRecipeBuilder unlocks(String param0, Criterion<?> param1) {
        this.criteria.put(param0, param1);
        return this;
    }

    public void save(RecipeOutput param0, String param1) {
        this.save(param0, new ResourceLocation(param1));
    }

    public void save(RecipeOutput param0, ResourceLocation param1) {
        this.ensureValid(param1);
        Advancement.Builder var0 = param0.advancement()
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(param1))
            .rewards(AdvancementRewards.Builder.recipe(param1))
            .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(var0::addCriterion);
        param0.accept(
            new SmithingTransformRecipeBuilder.Result(
                param1,
                this.type,
                this.template,
                this.base,
                this.addition,
                this.result,
                var0.build(param1.withPrefix("recipes/" + this.category.getFolderName() + "/"))
            )
        );
    }

    private void ensureValid(ResourceLocation param0) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + param0);
        }
    }

    public static record Result(
        ResourceLocation id, RecipeSerializer<?> type, Ingredient template, Ingredient base, Ingredient addition, Item result, AdvancementHolder advancement
    ) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject param0) {
            param0.add("template", this.template.toJson(true));
            param0.add("base", this.base.toJson(true));
            param0.add("addition", this.addition.toJson(true));
            JsonObject var0 = new JsonObject();
            var0.addProperty("item", BuiltInRegistries.ITEM.getKey(this.result).toString());
            param0.add("result", var0);
        }
    }
}
