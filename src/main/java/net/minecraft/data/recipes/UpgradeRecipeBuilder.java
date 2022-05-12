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

public class UpgradeRecipeBuilder {
    private final Ingredient base;
    private final Ingredient addition;
    private final Item result;
    private final Advancement.Builder advancement = Advancement.Builder.advancement();
    private final RecipeSerializer<?> type;

    public UpgradeRecipeBuilder(RecipeSerializer<?> param0, Ingredient param1, Ingredient param2, Item param3) {
        this.type = param0;
        this.base = param1;
        this.addition = param2;
        this.result = param3;
    }

    public static UpgradeRecipeBuilder smithing(Ingredient param0, Ingredient param1, Item param2) {
        return new UpgradeRecipeBuilder(RecipeSerializer.SMITHING, param0, param1, param2);
    }

    public UpgradeRecipeBuilder unlocks(String param0, CriterionTriggerInstance param1) {
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
            new UpgradeRecipeBuilder.Result(
                param1,
                this.type,
                this.base,
                this.addition,
                this.result,
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
        private final Ingredient base;
        private final Ingredient addition;
        private final Item result;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;
        private final RecipeSerializer<?> type;

        public Result(
            ResourceLocation param0,
            RecipeSerializer<?> param1,
            Ingredient param2,
            Ingredient param3,
            Item param4,
            Advancement.Builder param5,
            ResourceLocation param6
        ) {
            this.id = param0;
            this.type = param1;
            this.base = param2;
            this.addition = param3;
            this.result = param4;
            this.advancement = param5;
            this.advancementId = param6;
        }

        @Override
        public void serializeRecipeData(JsonObject param0) {
            param0.add("base", this.base.toJson());
            param0.add("addition", this.addition.toJson());
            JsonObject var0 = new JsonObject();
            var0.addProperty("item", Registry.ITEM.getKey(this.result).toString());
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
