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

public class SmithingTransformRecipeBuilder {
    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;
    private final RecipeCategory category;
    private final Item result;
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
    private final RecipeSerializer<?> type;

    public SmithingTransformRecipeBuilder(
        RecipeSerializer<?> param0, Ingredient param1, Ingredient param2, Ingredient param3, RecipeCategory param4, Item param5
    ) {
        this.category = param4;
        this.type = param0;
        this.template = param1;
        this.base = param2;
        this.addition = param3;
        this.result = param5;
    }

    public static SmithingTransformRecipeBuilder smithing(Ingredient param0, Ingredient param1, Ingredient param2, RecipeCategory param3, Item param4) {
        return new SmithingTransformRecipeBuilder(RecipeSerializer.SMITHING_TRANSFORM, param0, param1, param2, param3, param4);
    }

    public SmithingTransformRecipeBuilder unlocks(String param0, CriterionTriggerInstance param1) {
        this.advancement.addCriterion(param0, param1);
        return this;
    }

    public void save(Consumer<FinishedRecipe> param0, String param1) {
        this.save(param0, new ResourceLocation(param1));
    }

    public void save(Consumer<FinishedRecipe> param0, ResourceLocation param1) {
        this.ensureValid(param1);
        this.advancement
            .parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(param1))
            .rewards(AdvancementRewards.Builder.recipe(param1))
            .requirements(RequirementsStrategy.OR);
        param0.accept(
            new SmithingTransformRecipeBuilder.Result(
                param1,
                this.type,
                this.template,
                this.base,
                this.addition,
                this.result,
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
        Item result,
        Advancement.Builder advancement,
        ResourceLocation advancementId
    ) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject param0) {
            param0.add("template", this.template.toJson());
            param0.add("base", this.base.toJson());
            param0.add("addition", this.addition.toJson());
            JsonObject var0 = new JsonObject();
            var0.addProperty("item", BuiltInRegistries.ITEM.getKey(this.result).toString());
            param0.add("result", var0);
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
