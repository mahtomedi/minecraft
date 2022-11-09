package net.minecraft.client;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientRecipeBook extends RecipeBook {
    private static final Logger LOGGER = LogUtils.getLogger();
    private Map<RecipeBookCategories, List<RecipeCollection>> collectionsByTab = ImmutableMap.of();
    private List<RecipeCollection> allCollections = ImmutableList.of();

    public void setupCollections(Iterable<Recipe<?>> param0) {
        Map<RecipeBookCategories, List<List<Recipe<?>>>> var0 = categorizeAndGroupRecipes(param0);
        Map<RecipeBookCategories, List<RecipeCollection>> var1 = Maps.newHashMap();
        Builder<RecipeCollection> var2 = ImmutableList.builder();
        var0.forEach((param2, param3) -> var1.put(param2, param3.stream().map(RecipeCollection::new).peek(var2::add).collect(ImmutableList.toImmutableList())));
        RecipeBookCategories.AGGREGATE_CATEGORIES
            .forEach(
                (param1, param2) -> var1.put(
                        param1,
                        param2.stream().flatMap(param1x -> var1.getOrDefault(param1x, ImmutableList.of()).stream()).collect(ImmutableList.toImmutableList())
                    )
            );
        this.collectionsByTab = ImmutableMap.copyOf(var1);
        this.allCollections = var2.build();
    }

    private static Map<RecipeBookCategories, List<List<Recipe<?>>>> categorizeAndGroupRecipes(Iterable<Recipe<?>> param0) {
        Map<RecipeBookCategories, List<List<Recipe<?>>>> var0 = Maps.newHashMap();
        Table<RecipeBookCategories, String, List<Recipe<?>>> var1 = HashBasedTable.create();

        for(Recipe<?> var2 : param0) {
            if (!var2.isSpecial() && !var2.isIncomplete()) {
                RecipeBookCategories var3 = getCategory(var2);
                String var4 = var2.getGroup();
                if (var4.isEmpty()) {
                    var0.computeIfAbsent(var3, param0x -> Lists.newArrayList()).add(ImmutableList.of(var2));
                } else {
                    List<Recipe<?>> var5 = var1.get(var3, var4);
                    if (var5 == null) {
                        var5 = Lists.newArrayList();
                        var1.put(var3, var4, var5);
                        var0.computeIfAbsent(var3, param0x -> Lists.newArrayList()).add(var5);
                    }

                    var5.add(var2);
                }
            }
        }

        return var0;
    }

    private static RecipeBookCategories getCategory(Recipe<?> param0) {
        if (param0 instanceof CraftingRecipe var0) {
            return switch(var0.category()) {
                case BUILDING -> RecipeBookCategories.CRAFTING_BUILDING_BLOCKS;
                case EQUIPMENT -> RecipeBookCategories.CRAFTING_EQUIPMENT;
                case REDSTONE -> RecipeBookCategories.CRAFTING_REDSTONE;
                case MISC -> RecipeBookCategories.CRAFTING_MISC;
            };
        } else {
            RecipeType<?> var1 = param0.getType();
            if (param0 instanceof AbstractCookingRecipe var2) {
                CookingBookCategory var3 = var2.category();
                if (var1 == RecipeType.SMELTING) {
                    return switch(var3) {
                        case BLOCKS -> RecipeBookCategories.FURNACE_BLOCKS;
                        case FOOD -> RecipeBookCategories.FURNACE_FOOD;
                        case MISC -> RecipeBookCategories.FURNACE_MISC;
                    };
                }

                if (var1 == RecipeType.BLASTING) {
                    return var3 == CookingBookCategory.BLOCKS ? RecipeBookCategories.BLAST_FURNACE_BLOCKS : RecipeBookCategories.BLAST_FURNACE_MISC;
                }

                if (var1 == RecipeType.SMOKING) {
                    return RecipeBookCategories.SMOKER_FOOD;
                }

                if (var1 == RecipeType.CAMPFIRE_COOKING) {
                    return RecipeBookCategories.CAMPFIRE;
                }
            }

            if (var1 == RecipeType.STONECUTTING) {
                return RecipeBookCategories.STONECUTTER;
            } else if (var1 == RecipeType.SMITHING) {
                return RecipeBookCategories.SMITHING;
            } else {
                LOGGER.warn(
                    "Unknown recipe category: {}/{}",
                    LogUtils.defer(() -> BuiltInRegistries.RECIPE_TYPE.getKey(param0.getType())),
                    LogUtils.defer(param0::getId)
                );
                return RecipeBookCategories.UNKNOWN;
            }
        }
    }

    public List<RecipeCollection> getCollections() {
        return this.allCollections;
    }

    public List<RecipeCollection> getCollection(RecipeBookCategories param0) {
        return this.collectionsByTab.getOrDefault(param0, Collections.emptyList());
    }
}
