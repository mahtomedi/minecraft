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
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;

public class ShapelessRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final NonNullList<Ingredient> ingredients = NonNullList.create();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;

    public ShapelessRecipeBuilder(RecipeCategory param0, ItemLike param1, int param2) {
        this.category = param0;
        this.result = param1.asItem();
        this.count = param2;
    }

    public static ShapelessRecipeBuilder shapeless(RecipeCategory param0, ItemLike param1) {
        return new ShapelessRecipeBuilder(param0, param1, 1);
    }

    public static ShapelessRecipeBuilder shapeless(RecipeCategory param0, ItemLike param1, int param2) {
        return new ShapelessRecipeBuilder(param0, param1, param2);
    }

    public ShapelessRecipeBuilder requires(TagKey<Item> param0) {
        return this.requires(Ingredient.of(param0));
    }

    public ShapelessRecipeBuilder requires(ItemLike param0) {
        return this.requires(param0, 1);
    }

    public ShapelessRecipeBuilder requires(ItemLike param0, int param1) {
        for(int var0 = 0; var0 < param1; ++var0) {
            this.requires(Ingredient.of(param0));
        }

        return this;
    }

    public ShapelessRecipeBuilder requires(Ingredient param0) {
        return this.requires(param0, 1);
    }

    public ShapelessRecipeBuilder requires(Ingredient param0, int param1) {
        for(int var0 = 0; var0 < param1; ++var0) {
            this.ingredients.add(param0);
        }

        return this;
    }

    public ShapelessRecipeBuilder unlockedBy(String param0, Criterion<?> param1) {
        this.criteria.put(param0, param1);
        return this;
    }

    public ShapelessRecipeBuilder group(@Nullable String param0) {
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
        ShapelessRecipe var1 = new ShapelessRecipe(
            Objects.requireNonNullElse(this.group, ""),
            RecipeBuilder.determineBookCategory(this.category),
            new ItemStack(this.result, this.count),
            this.ingredients
        );
        param0.accept(param1, var1, var0.build(param1.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private void ensureValid(ResourceLocation param0) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + param0);
        }
    }
}
