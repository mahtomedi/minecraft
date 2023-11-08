package net.minecraft.data.recipes;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;

public class SmithingTransformRecipeBuilder {
    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;
    private final RecipeCategory category;
    private final Item result;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    public SmithingTransformRecipeBuilder(Ingredient param0, Ingredient param1, Ingredient param2, RecipeCategory param3, Item param4) {
        this.category = param3;
        this.template = param0;
        this.base = param1;
        this.addition = param2;
        this.result = param4;
    }

    public static SmithingTransformRecipeBuilder smithing(Ingredient param0, Ingredient param1, Ingredient param2, RecipeCategory param3, Item param4) {
        return new SmithingTransformRecipeBuilder(param0, param1, param2, param3, param4);
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
        SmithingTransformRecipe var1 = new SmithingTransformRecipe(this.template, this.base, this.addition, new ItemStack(this.result));
        param0.accept(param1, var1, var0.build(param1.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private void ensureValid(ResourceLocation param0) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + param0);
        }
    }
}
