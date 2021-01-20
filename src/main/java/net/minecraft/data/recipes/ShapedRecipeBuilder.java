package net.minecraft.data.recipes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ShapedRecipeBuilder implements RecipeBuilder {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Item result;
    private final int count;
    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
    private final Advancement.Builder advancement = Advancement.Builder.advancement();
    private String group;

    public ShapedRecipeBuilder(ItemLike param0, int param1) {
        this.result = param0.asItem();
        this.count = param1;
    }

    public static ShapedRecipeBuilder shaped(ItemLike param0) {
        return shaped(param0, 1);
    }

    public static ShapedRecipeBuilder shaped(ItemLike param0, int param1) {
        return new ShapedRecipeBuilder(param0, param1);
    }

    public ShapedRecipeBuilder define(Character param0, Tag<Item> param1) {
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

    public ShapedRecipeBuilder unlockedBy(String param0, CriterionTriggerInstance param1) {
        this.advancement.addCriterion(param0, param1);
        return this;
    }

    public ShapedRecipeBuilder group(String param0) {
        this.group = param0;
        return this;
    }

    @Override
    public void save(Consumer<FinishedRecipe> param0) {
        this.save(param0, Registry.ITEM.getKey(this.result));
    }

    public void save(Consumer<FinishedRecipe> param0, String param1) {
        ResourceLocation var0 = Registry.ITEM.getKey(this.result);
        if (new ResourceLocation(param1).equals(var0)) {
            throw new IllegalStateException("Shaped Recipe " + param1 + " should remove its 'save' argument");
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
            new ShapedRecipeBuilder.Result(
                param1,
                this.result,
                this.count,
                this.group == null ? "" : this.group,
                this.rows,
                this.key,
                this.advancement,
                new ResourceLocation(param1.getNamespace(), "recipes/" + this.result.getItemCategory().getRecipeFolderName() + "/" + param1.getPath())
            )
        );
    }

    private void ensureValid(ResourceLocation param0) {
        if (this.rows.isEmpty()) {
            throw new IllegalStateException("No pattern is defined for shaped recipe " + param0 + "!");
        } else {
            Set<Character> var0 = Sets.newHashSet(this.key.keySet());
            var0.remove(' ');

            for(String var1 : this.rows) {
                for(int var2 = 0; var2 < var1.length(); ++var2) {
                    char var3 = var1.charAt(var2);
                    if (!this.key.containsKey(var3) && var3 != ' ') {
                        throw new IllegalStateException("Pattern in recipe " + param0 + " uses undefined symbol '" + var3 + "'");
                    }

                    var0.remove(var3);
                }
            }

            if (!var0.isEmpty()) {
                throw new IllegalStateException("Ingredients are defined but not used in pattern for recipe " + param0);
            } else if (this.rows.size() == 1 && this.rows.get(0).length() == 1) {
                throw new IllegalStateException("Shaped recipe " + param0 + " only takes in a single item - should it be a shapeless recipe instead?");
            } else if (this.advancement.getCriteria().isEmpty()) {
                throw new IllegalStateException("No way of obtaining recipe " + param0);
            }
        }
    }

    class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final Item result;
        private final int count;
        private final String group;
        private final List<String> pattern;
        private final Map<Character, Ingredient> key;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        public Result(
            ResourceLocation param0,
            Item param1,
            int param2,
            String param3,
            List<String> param4,
            Map<Character, Ingredient> param5,
            Advancement.Builder param6,
            ResourceLocation param7
        ) {
            this.id = param0;
            this.result = param1;
            this.count = param2;
            this.group = param3;
            this.pattern = param4;
            this.key = param5;
            this.advancement = param6;
            this.advancementId = param7;
        }

        @Override
        public void serializeRecipeData(JsonObject param0) {
            if (!this.group.isEmpty()) {
                param0.addProperty("group", this.group);
            }

            JsonArray var0 = new JsonArray();

            for(String var1 : this.pattern) {
                var0.add(var1);
            }

            param0.add("pattern", var0);
            JsonObject var2 = new JsonObject();

            for(Entry<Character, Ingredient> var3 : this.key.entrySet()) {
                var2.add(String.valueOf(var3.getKey()), var3.getValue().toJson());
            }

            param0.add("key", var2);
            JsonObject var4 = new JsonObject();
            var4.addProperty("item", Registry.ITEM.getKey(this.result).toString());
            if (this.count > 1) {
                var4.addProperty("count", this.count);
            }

            param0.add("result", var4);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return RecipeSerializer.SHAPED_RECIPE;
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
