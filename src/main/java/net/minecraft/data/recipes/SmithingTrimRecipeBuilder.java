package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SmithingTrimRecipeBuilder {
    private final RecipeCategory category;
    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
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

    public SmithingTrimRecipeBuilder unlocks(String param0, CriterionTriggerInstance param1) {
        this.advancement.addCriterion(param0, param1);
        return this;
    }

    public void save(Consumer<FinishedRecipe> param0, ResourceLocation param1) {
        this.ensureValid(param1);
        this.advancement
            .parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(param1))
            .rewards(AdvancementRewards.Builder.recipe(param1))
            .requirements(RequirementsStrategy.OR);
        param0.accept(
            new SmithingTrimRecipeBuilder.Result(
                param1,
                this.type,
                this.template,
                this.base,
                this.addition,
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

    public static record Result(
        ResourceLocation id,
        RecipeSerializer<?> type,
        Ingredient template,
        Ingredient base,
        Ingredient addition,
        Advancement.Builder advancement,
        ResourceLocation advancementId
    ) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject param0) {
            param0.add("template", this.template.toJson());
            param0.add("base", this.base.toJson());
            param0.add("addition", this.addition.toJson());
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
