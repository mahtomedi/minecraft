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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SmithingTrimRecipeBuilder {
    private final RecipeCategory category;
    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    private final RecipeSerializer<?> type;

    public SmithingTrimRecipeBuilder(RecipeSerializer<?> param0, RecipeCategory param1, Ingredient param2, Ingredient param3, Ingredient param4) {
        this.category = param1;
        this.type = param0;
        this.template = param2;
        this.base = param3;
        this.addition = param4;
    }

    public static SmithingTrimRecipeBuilder smithingTrim(Ingredient param0, Ingredient param1, Ingredient param2, RecipeCategory param3) {
        return new SmithingTrimRecipeBuilder(RecipeSerializer.SMITHING_TRIM, param3, param0, param1, param2);
    }

    public SmithingTrimRecipeBuilder unlocks(String param0, Criterion<?> param1) {
        this.criteria.put(param0, param1);
        return this;
    }

    public void save(RecipeOutput param0, ResourceLocation param1) {
        this.ensureValid(param1);
        Advancement.Builder var0 = param0.advancement()
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(param1))
            .rewards(AdvancementRewards.Builder.recipe(param1))
            .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(var0::addCriterion);
        param0.accept(
            new SmithingTrimRecipeBuilder.Result(
                param1, this.type, this.template, this.base, this.addition, var0.build(param1.withPrefix("recipes/" + this.category.getFolderName() + "/"))
            )
        );
    }

    private void ensureValid(ResourceLocation param0) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + param0);
        }
    }

    public static record Result(
        ResourceLocation id, RecipeSerializer<?> type, Ingredient template, Ingredient base, Ingredient addition, AdvancementHolder advancement
    ) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject param0) {
            param0.add("template", this.template.toJson(true));
            param0.add("base", this.base.toJson(true));
            param0.add("addition", this.addition.toJson(true));
        }
    }
}
