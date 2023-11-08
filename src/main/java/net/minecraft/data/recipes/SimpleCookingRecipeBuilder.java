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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.ItemLike;

public class SimpleCookingRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final CookingBookCategory bookCategory;
    private final Item result;
    private final Ingredient ingredient;
    private final float experience;
    private final int cookingTime;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;
    private final AbstractCookingRecipe.Factory<?> factory;

    private SimpleCookingRecipeBuilder(
        RecipeCategory param0,
        CookingBookCategory param1,
        ItemLike param2,
        Ingredient param3,
        float param4,
        int param5,
        AbstractCookingRecipe.Factory<?> param6
    ) {
        this.category = param0;
        this.bookCategory = param1;
        this.result = param2.asItem();
        this.ingredient = param3;
        this.experience = param4;
        this.cookingTime = param5;
        this.factory = param6;
    }

    public static <T extends AbstractCookingRecipe> SimpleCookingRecipeBuilder generic(
        Ingredient param0,
        RecipeCategory param1,
        ItemLike param2,
        float param3,
        int param4,
        RecipeSerializer<T> param5,
        AbstractCookingRecipe.Factory<T> param6
    ) {
        return new SimpleCookingRecipeBuilder(param1, determineRecipeCategory(param5, param2), param2, param0, param3, param4, param6);
    }

    public static SimpleCookingRecipeBuilder campfireCooking(Ingredient param0, RecipeCategory param1, ItemLike param2, float param3, int param4) {
        return new SimpleCookingRecipeBuilder(param1, CookingBookCategory.FOOD, param2, param0, param3, param4, CampfireCookingRecipe::new);
    }

    public static SimpleCookingRecipeBuilder blasting(Ingredient param0, RecipeCategory param1, ItemLike param2, float param3, int param4) {
        return new SimpleCookingRecipeBuilder(param1, determineBlastingRecipeCategory(param2), param2, param0, param3, param4, BlastingRecipe::new);
    }

    public static SimpleCookingRecipeBuilder smelting(Ingredient param0, RecipeCategory param1, ItemLike param2, float param3, int param4) {
        return new SimpleCookingRecipeBuilder(param1, determineSmeltingRecipeCategory(param2), param2, param0, param3, param4, SmeltingRecipe::new);
    }

    public static SimpleCookingRecipeBuilder smoking(Ingredient param0, RecipeCategory param1, ItemLike param2, float param3, int param4) {
        return new SimpleCookingRecipeBuilder(param1, CookingBookCategory.FOOD, param2, param0, param3, param4, SmokingRecipe::new);
    }

    public SimpleCookingRecipeBuilder unlockedBy(String param0, Criterion<?> param1) {
        this.criteria.put(param0, param1);
        return this;
    }

    public SimpleCookingRecipeBuilder group(@Nullable String param0) {
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
        AbstractCookingRecipe var1 = this.factory
            .create(
                Objects.requireNonNullElse(this.group, ""), this.bookCategory, this.ingredient, new ItemStack(this.result), this.experience, this.cookingTime
            );
        param0.accept(param1, var1, var0.build(param1.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private static CookingBookCategory determineSmeltingRecipeCategory(ItemLike param0) {
        if (param0.asItem().isEdible()) {
            return CookingBookCategory.FOOD;
        } else {
            return param0.asItem() instanceof BlockItem ? CookingBookCategory.BLOCKS : CookingBookCategory.MISC;
        }
    }

    private static CookingBookCategory determineBlastingRecipeCategory(ItemLike param0) {
        return param0.asItem() instanceof BlockItem ? CookingBookCategory.BLOCKS : CookingBookCategory.MISC;
    }

    private static CookingBookCategory determineRecipeCategory(RecipeSerializer<? extends AbstractCookingRecipe> param0, ItemLike param1) {
        if (param0 == RecipeSerializer.SMELTING_RECIPE) {
            return determineSmeltingRecipeCategory(param1);
        } else if (param0 == RecipeSerializer.BLASTING_RECIPE) {
            return determineBlastingRecipeCategory(param1);
        } else if (param0 != RecipeSerializer.SMOKING_RECIPE && param0 != RecipeSerializer.CAMPFIRE_COOKING_RECIPE) {
            throw new IllegalStateException("Unknown cooking recipe type");
        } else {
            return CookingBookCategory.FOOD;
        }
    }

    private void ensureValid(ResourceLocation param0) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + param0);
        }
    }
}
