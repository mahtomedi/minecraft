package net.minecraft.data.recipes;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class ShapelessRecipeBuilder implements RecipeBuilder {
    private final Item result;
    private final int count;
    private final List<Ingredient> ingredients = Lists.newArrayList();
    private final Advancement.Builder advancement = Advancement.Builder.advancement();
    @Nullable
    private String group;

    public ShapelessRecipeBuilder(ItemLike param0, int param1) {
        this.result = param0.asItem();
        this.count = param1;
    }

    public static ShapelessRecipeBuilder shapeless(ItemLike param0) {
        return new ShapelessRecipeBuilder(param0, 1);
    }

    public static ShapelessRecipeBuilder shapeless(ItemLike param0, int param1) {
        return new ShapelessRecipeBuilder(param0, param1);
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

    public ShapelessRecipeBuilder unlockedBy(String param0, CriterionTriggerInstance param1) {
        this.advancement.addCriterion(param0, param1);
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
    public void save(Consumer<FinishedRecipe> param0, ResourceLocation param1) {
        this.ensureValid(param1);
        this.advancement
            .parent(new ResourceLocation("recipes/root"))
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(param1))
            .rewards(AdvancementRewards.Builder.recipe(param1))
            .requirements(RequirementsStrategy.OR);
        param0.accept(
            new ShapelessRecipeBuilder.Result(
                param1,
                this.result,
                this.count,
                this.group == null ? "" : this.group,
                this.ingredients,
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
        private final Item result;
        private final int count;
        private final String group;
        private final List<Ingredient> ingredients;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        public Result(
            ResourceLocation param0, Item param1, int param2, String param3, List<Ingredient> param4, Advancement.Builder param5, ResourceLocation param6
        ) {
            this.id = param0;
            this.result = param1;
            this.count = param2;
            this.group = param3;
            this.ingredients = param4;
            this.advancement = param5;
            this.advancementId = param6;
        }

        @Override
        public void serializeRecipeData(JsonObject param0) {
            if (!this.group.isEmpty()) {
                param0.addProperty("group", this.group);
            }

            JsonArray var0 = new JsonArray();

            for(Ingredient var1 : this.ingredients) {
                var0.add(var1.toJson());
            }

            param0.add("ingredients", var0);
            JsonObject var2 = new JsonObject();
            var2.addProperty("item", Registry.ITEM.getKey(this.result).toString());
            if (this.count > 1) {
                var2.addProperty("count", this.count);
            }

            param0.add("result", var2);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return RecipeSerializer.SHAPELESS_RECIPE;
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
