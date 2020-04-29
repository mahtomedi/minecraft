package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.level.ItemLike;

public class SimpleCookingRecipeBuilder {
    private final Item result;
    private final Ingredient ingredient;
    private final float experience;
    private final int cookingTime;
    private final Advancement.Builder advancement = Advancement.Builder.advancement();
    private String group;
    private final SimpleCookingSerializer<?> serializer;

    private SimpleCookingRecipeBuilder(ItemLike param0, Ingredient param1, float param2, int param3, SimpleCookingSerializer<?> param4) {
        this.result = param0.asItem();
        this.ingredient = param1;
        this.experience = param2;
        this.cookingTime = param3;
        this.serializer = param4;
    }

    public static SimpleCookingRecipeBuilder cooking(Ingredient param0, ItemLike param1, float param2, int param3, SimpleCookingSerializer<?> param4) {
        return new SimpleCookingRecipeBuilder(param1, param0, param2, param3, param4);
    }

    public static SimpleCookingRecipeBuilder blasting(Ingredient param0, ItemLike param1, float param2, int param3) {
        return cooking(param0, param1, param2, param3, RecipeSerializer.BLASTING_RECIPE);
    }

    public static SimpleCookingRecipeBuilder smelting(Ingredient param0, ItemLike param1, float param2, int param3) {
        return cooking(param0, param1, param2, param3, RecipeSerializer.SMELTING_RECIPE);
    }

    public SimpleCookingRecipeBuilder unlockedBy(String param0, CriterionTriggerInstance param1) {
        this.advancement.addCriterion(param0, param1);
        return this;
    }

    public void save(Consumer<FinishedRecipe> param0) {
        this.save(param0, Registry.ITEM.getKey(this.result));
    }

    public void save(Consumer<FinishedRecipe> param0, String param1) {
        ResourceLocation var0 = Registry.ITEM.getKey(this.result);
        ResourceLocation var1 = new ResourceLocation(param1);
        if (var1.equals(var0)) {
            throw new IllegalStateException("Recipe " + var1 + " should remove its 'save' argument");
        } else {
            this.save(param0, var1);
        }
    }

    public void save(Consumer<FinishedRecipe> param0, ResourceLocation param1) {
        this.ensureValid(param1);
        this.advancement
            .parent(new ResourceLocation("recipes/root"))
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(param1))
            .rewards(AdvancementRewards.Builder.recipe(param1))
            .requirements(RequirementsStrategy.OR);
        param0.accept(
            new SimpleCookingRecipeBuilder.Result(
                param1,
                this.group == null ? "" : this.group,
                this.ingredient,
                this.result,
                this.experience,
                this.cookingTime,
                this.advancement,
                new ResourceLocation(param1.getNamespace(), "recipes/" + this.result.getItemCategory().getRecipeFolderName() + "/" + param1.getPath()),
                this.serializer
            )
        );
    }

    private void ensureValid(ResourceLocation param0) {
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + param0);
        }
    }

    public static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final String group;
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
            Ingredient param2,
            Item param3,
            float param4,
            int param5,
            Advancement.Builder param6,
            ResourceLocation param7,
            RecipeSerializer<? extends AbstractCookingRecipe> param8
        ) {
            this.id = param0;
            this.group = param1;
            this.ingredient = param2;
            this.result = param3;
            this.experience = param4;
            this.cookingTime = param5;
            this.advancement = param6;
            this.advancementId = param7;
            this.serializer = param8;
        }

        @Override
        public void serializeRecipeData(JsonObject param0) {
            if (!this.group.isEmpty()) {
                param0.addProperty("group", this.group);
            }

            param0.add("ingredient", this.ingredient.toJson());
            param0.addProperty("result", Registry.ITEM.getKey(this.result).toString());
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
