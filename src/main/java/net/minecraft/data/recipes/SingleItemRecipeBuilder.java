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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class SingleItemRecipeBuilder {
    private final Item result;
    private final Ingredient ingredient;
    private final int count;
    private final Advancement.Builder advancement = Advancement.Builder.advancement();
    private String group;
    private final RecipeSerializer<?> type;

    public SingleItemRecipeBuilder(RecipeSerializer<?> param0, Ingredient param1, ItemLike param2, int param3) {
        this.type = param0;
        this.result = param2.asItem();
        this.ingredient = param1;
        this.count = param3;
    }

    public static SingleItemRecipeBuilder stonecutting(Ingredient param0, ItemLike param1) {
        return new SingleItemRecipeBuilder(RecipeSerializer.STONECUTTER, param0, param1, 1);
    }

    public static SingleItemRecipeBuilder stonecutting(Ingredient param0, ItemLike param1, int param2) {
        return new SingleItemRecipeBuilder(RecipeSerializer.STONECUTTER, param0, param1, param2);
    }

    public SingleItemRecipeBuilder unlocks(String param0, CriterionTriggerInstance param1) {
        this.advancement.addCriterion(param0, param1);
        return this;
    }

    public SingleItemRecipeBuilder group(String param0) {
        this.group = param0;
        return this;
    }

    public void save(Consumer<FinishedRecipe> param0, String param1) {
        ResourceLocation var0 = Registry.ITEM.getKey(this.result);
        if (new ResourceLocation(param1).equals(var0)) {
            throw new IllegalStateException("Single Item Recipe " + param1 + " should remove its 'save' argument");
        } else {
            this.save(param0, new ResourceLocation(param1));
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
            new SingleItemRecipeBuilder.Result(
                param1,
                this.type,
                this.group == null ? "" : this.group,
                this.ingredient,
                this.result,
                this.count,
                this.advancement,
                new ResourceLocation(param1.getNamespace(), "recipes/" + this.result.getItemCategory().getRecipeFolderName() + "/" + param1.getPath())
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
            param0.addProperty("result", Registry.ITEM.getKey(this.result).toString());
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
