package net.minecraft.data.recipes;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

public class SmithingTrimRecipeBuilder {
    private final RecipeCategory category;
    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    public SmithingTrimRecipeBuilder(RecipeCategory param0, Ingredient param1, Ingredient param2, Ingredient param3) {
        this.category = param0;
        this.template = param1;
        this.base = param2;
        this.addition = param3;
    }

    public static SmithingTrimRecipeBuilder smithingTrim(Ingredient param0, Ingredient param1, Ingredient param2, RecipeCategory param3) {
        return new SmithingTrimRecipeBuilder(param3, param0, param1, param2);
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
        SmithingTrimRecipe var1 = new SmithingTrimRecipe(this.template, this.base, this.addition);
        param0.accept(param1, var1, var0.build(param1.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private void ensureValid(ResourceLocation param0) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + param0);
        }
    }
}
