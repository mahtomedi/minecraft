package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
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
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
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

    public SingleItemRecipeBuilder unlockedBy(String param0, Criterion<?> param1) {
        this.criteria.put(param0, param1);
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
    public void save(RecipeOutput param0, ResourceLocation param1) {
        this.ensureValid(param1);
        Advancement.Builder var0 = param0.advancement()
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(param1))
            .rewards(AdvancementRewards.Builder.recipe(param1))
            .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(var0::addCriterion);
        param0.accept(
            new SingleItemRecipeBuilder.Result(
                param1,
                this.type,
                this.group == null ? "" : this.group,
                this.ingredient,
                this.result,
                this.count,
                var0.build(param1.withPrefix("recipes/" + this.category.getFolderName() + "/"))
            )
        );
    }

    private void ensureValid(ResourceLocation param0) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + param0);
        }
    }

    public static record Result(
        ResourceLocation id, RecipeSerializer<?> type, String group, Ingredient ingredient, Item result, int count, AdvancementHolder advancement
    ) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject param0) {
            if (!this.group.isEmpty()) {
                param0.addProperty("group", this.group);
            }

            param0.add("ingredient", this.ingredient.toJson(false));
            param0.addProperty("result", BuiltInRegistries.ITEM.getKey(this.result).toString());
            param0.addProperty("count", this.count);
        }
    }
}
