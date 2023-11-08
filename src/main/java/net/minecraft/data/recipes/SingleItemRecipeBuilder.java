package net.minecraft.data.recipes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.ItemLike;

public class SingleItemRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final Ingredient ingredient;
    private final int count;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;
    private final SingleItemRecipe.Factory<?> factory;

    public SingleItemRecipeBuilder(RecipeCategory param0, SingleItemRecipe.Factory<?> param1, Ingredient param2, ItemLike param3, int param4) {
        this.category = param0;
        this.factory = param1;
        this.result = param3.asItem();
        this.ingredient = param2;
        this.count = param4;
    }

    public static SingleItemRecipeBuilder stonecutting(Ingredient param0, RecipeCategory param1, ItemLike param2) {
        return new SingleItemRecipeBuilder(param1, StonecutterRecipe::new, param0, param2, 1);
    }

    public static SingleItemRecipeBuilder stonecutting(Ingredient param0, RecipeCategory param1, ItemLike param2, int param3) {
        return new SingleItemRecipeBuilder(param1, StonecutterRecipe::new, param0, param2, param3);
    }

    public SingleItemRecipeBuilder unlockedBy(String param0, Criterion<?> param1) {
        this.criteria.put(param0, param1);
        return this;
    }

    public SingleItemRecipeBuilder group(@Nullable String param0) {
        this.group = param0;
        return this;
    }

    @Override
    public Item getResult() {
        return this.result;
    }

    @Override
    public void save(RecipeOutput param0, ResourceLocation param1) {
        this.ensureValid(param1);
        Advancement.Builder var0 = param0.advancement()
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(param1))
            .rewards(AdvancementRewards.Builder.recipe(param1))
            .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(var0::addCriterion);
        SingleItemRecipe var1 = this.factory.create(Objects.requireNonNullElse(this.group, ""), this.ingredient, new ItemStack(this.result, this.count));
        param0.accept(param1, var1, var0.build(param1.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private void ensureValid(ResourceLocation param0) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + param0);
        }
    }
}
