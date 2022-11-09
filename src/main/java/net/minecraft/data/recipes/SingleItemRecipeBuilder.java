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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class SingleItemRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final Ingredient ingredient;
    private final int count;
    private final Advancement.Builder advancement = Advancement.Builder.advancement();
    @Nullable
    private String group;
    private final RecipeSerializer<?> type;

    public SingleItemRecipeBuilder(RecipeCategory param0, RecipeSerializer<?> param1, Ingredient param2, ItemLike param3, int param4) {
        this.category = param0;
        this.type = param1;
        this.result = param3.asItem();
        this.ingredient = param2;
        this.count = param4;
    }

    public static SingleItemRecipeBuilder stonecutting(Ingredient param0, RecipeCategory param1, ItemLike param2) {
        return new SingleItemRecipeBuilder(param1, RecipeSerializer.STONECUTTER, param0, param2, 1);
    }

    public static SingleItemRecipeBuilder stonecutting(Ingredient param0, RecipeCategory param1, ItemLike param2, int param3) {
        return new SingleItemRecipeBuilder(param1, RecipeSerializer.STONECUTTER, param0, param2, param3);
    }

    public SingleItemRecipeBuilder unlockedBy(String param0, CriterionTriggerInstance param1) {
        this.advancement.addCriterion(param0, param1);
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
    public void save(Consumer<FinishedRecipe> param0, ResourceLocation param1) {
        this.ensureValid(param1);
        this.advancement
            .parent(ROOT_RECIPE_ADVANCEMENT)
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(param1))
            .rewards(AdvancementRewards.Builder.recipe(param1))
            .requirements(RequirementsStrategy.OR);
        param0.accept(
            new SingleItemRecipeBuilder.Result(
                param1,
                this.type,
                this.group == null ? "" : this.group,
                this.ingredient,
                this.result,
                this.count,
                this.advancement,
                param1.withPrefix("recipes/" + this.category.getFolderName() + "/")
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
        private final int count;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;
        private final RecipeSerializer<?> type;

        public Result(
            ResourceLocation param0,
            RecipeSerializer<?> param1,
            String param2,
            Ingredient param3,
            Item param4,
            int param5,
            Advancement.Builder param6,
            ResourceLocation param7
        ) {
            this.id = param0;
            this.type = param1;
            this.group = param2;
            this.ingredient = param3;
            this.result = param4;
            this.count = param5;
            this.advancement = param6;
            this.advancementId = param7;
        }

        @Override
        public void serializeRecipeData(JsonObject param0) {
            if (!this.group.isEmpty()) {
                param0.addProperty("group", this.group);
            }

            param0.add("ingredient", this.ingredient.toJson());
            param0.addProperty("result", BuiltInRegistries.ITEM.getKey(this.result).toString());
            param0.addProperty("count", this.count);
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return this.type;
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
