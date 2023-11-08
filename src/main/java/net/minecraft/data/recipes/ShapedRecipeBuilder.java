package net.minecraft.data.recipes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;

public class ShapedRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;
    private boolean showNotification = true;

    public ShapedRecipeBuilder(RecipeCategory param0, ItemLike param1, int param2) {
        this.category = param0;
        this.result = param1.asItem();
        this.count = param2;
    }

    public static ShapedRecipeBuilder shaped(RecipeCategory param0, ItemLike param1) {
        return shaped(param0, param1, 1);
    }

    public static ShapedRecipeBuilder shaped(RecipeCategory param0, ItemLike param1, int param2) {
        return new ShapedRecipeBuilder(param0, param1, param2);
    }

    public ShapedRecipeBuilder define(Character param0, TagKey<Item> param1) {
        return this.define(param0, Ingredient.of(param1));
    }

    public ShapedRecipeBuilder define(Character param0, ItemLike param1) {
        return this.define(param0, Ingredient.of(param1));
    }

    public ShapedRecipeBuilder define(Character param0, Ingredient param1) {
        if (this.key.containsKey(param0)) {
            throw new IllegalArgumentException("Symbol '" + param0 + "' is already defined!");
        } else if (param0 == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        } else {
            this.key.put(param0, param1);
            return this;
        }
    }

    public ShapedRecipeBuilder pattern(String param0) {
        if (!this.rows.isEmpty() && param0.length() != this.rows.get(0).length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        } else {
            this.rows.add(param0);
            return this;
        }
    }

    public ShapedRecipeBuilder unlockedBy(String param0, Criterion<?> param1) {
        this.criteria.put(param0, param1);
        return this;
    }

    public ShapedRecipeBuilder group(@Nullable String param0) {
        this.group = param0;
        return this;
    }

    public ShapedRecipeBuilder showNotification(boolean param0) {
        this.showNotification = param0;
        return this;
    }

    @Override
    public Item getResult() {
        return this.result;
    }

    @Override
    public void save(RecipeOutput param0, ResourceLocation param1) {
        ShapedRecipePattern var0 = this.ensureValid(param1);
        Advancement.Builder var1 = param0.advancement()
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(param1))
            .rewards(AdvancementRewards.Builder.recipe(param1))
            .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(var1::addCriterion);
        ShapedRecipe var2 = new ShapedRecipe(
            Objects.requireNonNullElse(this.group, ""),
            RecipeBuilder.determineBookCategory(this.category),
            var0,
            new ItemStack(this.result, this.count),
            this.showNotification
        );
        param0.accept(param1, var2, var1.build(param1.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private ShapedRecipePattern ensureValid(ResourceLocation param0) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + param0);
        } else {
            return ShapedRecipePattern.of(this.key, this.rows);
        }
    }
}
