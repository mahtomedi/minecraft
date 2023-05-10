package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class SimpleCookingRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final CookingBookCategory bookCategory;
    private final Item result;
    private final Ingredient ingredient;
    private final float experience;
    private final int cookingTime;
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
    @Nullable
    private String group;
    private final RecipeSerializer<? extends AbstractCookingRecipe> serializer;

    private SimpleCookingRecipeBuilder(
        RecipeCategory param0,
        CookingBookCategory param1,
        ItemLike param2,
        Ingredient param3,
        float param4,
        int param5,
        RecipeSerializer<? extends AbstractCookingRecipe> param6
    ) {
        this.category = param0;
        this.bookCategory = param1;
        this.result = param2.asItem();
        this.ingredient = param3;
        this.experience = param4;
        this.cookingTime = param5;
        this.serializer = param6;
    }

    public static SimpleCookingRecipeBuilder generic(
        Ingredient param0, RecipeCategory param1, ItemLike param2, float param3, int param4, RecipeSerializer<? extends AbstractCookingRecipe> param5
    ) {
        return new SimpleCookingRecipeBuilder(param1, determineRecipeCategory(param5, param2), param2, param0, param3, param4, param5);
    }

    public static SimpleCookingRecipeBuilder campfireCooking(Ingredient param0, RecipeCategory param1, ItemLike param2, float param3, int param4) {
        return new SimpleCookingRecipeBuilder(param1, CookingBookCategory.FOOD, param2, param0, param3, param4, RecipeSerializer.CAMPFIRE_COOKING_RECIPE);
    }

    public static SimpleCookingRecipeBuilder blasting(Ingredient param0, RecipeCategory param1, ItemLike param2, float param3, int param4) {
        return new SimpleCookingRecipeBuilder(param1, determineBlastingRecipeCategory(param2), param2, param0, param3, param4, RecipeSerializer.BLASTING_RECIPE);
    }

    public static SimpleCookingRecipeBuilder smelting(Ingredient param0, RecipeCategory param1, ItemLike param2, float param3, int param4) {
        return new SimpleCookingRecipeBuilder(param1, determineSmeltingRecipeCategory(param2), param2, param0, param3, param4, RecipeSerializer.SMELTING_RECIPE);
    }

    public static SimpleCookingRecipeBuilder smoking(Ingredient param0, RecipeCategory param1, ItemLike param2, float param3, int param4) {
        return new SimpleCookingRecipeBuilder(param1, CookingBookCategory.FOOD, param2, param0, param3, param4, RecipeSerializer.SMOKING_RECIPE);
    }

    public SimpleCookingRecipeBuilder unlockedBy(String param0, CriterionTriggerInstance param1) {
        this.advancement.addCriterion(param0, param1);
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
    public void save(Consumer<FinishedRecipe> param0, ResourceLocation param1) {
        this.ensureValid(param1);
        this.advancement
            .parent(ROOT_RECIPE_ADVANCEMENT)
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(param1))
            .rewards(AdvancementRewards.Builder.recipe(param1))
            .requirements(RequirementsStrategy.OR);
        param0.accept(
            new SimpleCookingRecipeBuilder.Result(
                param1,
                this.group == null ? "" : this.group,
                this.bookCategory,
                this.ingredient,
                this.result,
                this.experience,
                this.cookingTime,
                this.advancement,
                param1.withPrefix("recipes/" + this.category.getFolderName() + "/"),
                this.serializer
            )
        );
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
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + param0);
        }
    }

    static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final String group;
        private final CookingBookCategory category;
        private final Ingredient ingredient;
        private final Item result;
        private final float experience;
        private final int cookingTime;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;
        private final RecipeSerializer<? extends AbstractCookingRecipe> serializer;

        public Result(
            ResourceLocation param0,
            String param1,
            CookingBookCategory param2,
            Ingredient param3,
            Item param4,
            float param5,
            int param6,
            Advancement.Builder param7,
            ResourceLocation param8,
            RecipeSerializer<? extends AbstractCookingRecipe> param9
        ) {
            this.id = param0;
            this.group = param1;
            this.category = param2;
            this.ingredient = param3;
            this.result = param4;
            this.experience = param5;
            this.cookingTime = param6;
            this.advancement = param7;
            this.advancementId = param8;
            this.serializer = param9;
        }

        @Override
        public void serializeRecipeData(JsonObject param0) {
            if (!this.group.isEmpty()) {
                param0.addProperty("group", this.group);
            }

            param0.addProperty("category", this.category.getSerializedName());
            param0.add("ingredient", this.ingredient.toJson());
            param0.addProperty("result", BuiltInRegistries.ITEM.getKey(this.result).toString());
            param0.addProperty("experience", this.experience);
            param0.addProperty("cookingtime", this.cookingTime);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return this.serializer;
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return this.advancement.serializeToJson();
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return this.advancementId;
        }
    }
}
