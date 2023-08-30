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
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientRecipeBook extends RecipeBook {
    private static final Logger LOGGER = LogUtils.getLogger();
    private Map<RecipeBookCategories, List<RecipeCollection>> collectionsByTab = ImmutableMap.of();
    private List<RecipeCollection> allCollections = ImmutableList.of();

    public void setupCollections(Iterable<RecipeHolder<?>> param0, RegistryAccess param1) {
        Map<RecipeBookCategories, List<List<RecipeHolder<?>>>> var0 = categorizeAndGroupRecipes(param0);
        Map<RecipeBookCategories, List<RecipeCollection>> var1 = Maps.newHashMap();
        Builder<RecipeCollection> var2 = ImmutableList.builder();
        var0.forEach(
            (param3, param4) -> var1.put(
                    param3, param4.stream().map(param1x -> new RecipeCollection(param1, param1x)).peek(var2::add).collect(ImmutableList.toImmutableList())
                )
        );
        RecipeBookCategories.AGGREGATE_CATEGORIES
            .forEach(
                (param1x, param2) -> var1.put(
                        param1x,
                        param2.stream().flatMap(param1xx -> var1.getOrDefault(param1xx, ImmutableList.of()).stream()).collect(ImmutableList.toImmutableList())
                    )
            );
        this.collectionsByTab = ImmutableMap.copyOf(var1);
        this.allCollections = var2.build();
    }

    private static Map<RecipeBookCategories, List<List<RecipeHolder<?>>>> categorizeAndGroupRecipes(Iterable<RecipeHolder<?>> param0) {
        Map<RecipeBookCategories, List<List<RecipeHolder<?>>>> var0 = Maps.newHashMap();
        Table<RecipeBookCategories, String, List<RecipeHolder<?>>> var1 = HashBasedTable.create();

        for(RecipeHolder<?> var2 : param0) {
            Recipe<?> var3 = var2.value();
            if (!var3.isSpecial() && !var3.isIncomplete()) {
                RecipeBookCategories var4 = getCategory(var2);
                String var5 = var3.getGroup();
                if (var5.isEmpty()) {
                    var0.computeIfAbsent(var4, param0x -> Lists.newArrayList()).add(ImmutableList.of(var2));
                } else {
                    List<RecipeHolder<?>> var6 = var1.get(var4, var5);
                    if (var6 == null) {
                        var6 = Lists.newArrayList();
                        var1.put(var4, var5, var6);
                        var0.computeIfAbsent(var4, param0x -> Lists.newArrayList()).add(var6);
                    }

                    var6.add(var2);
                }
            }
        }

        return var0;
    }

    private static RecipeBookCategories getCategory(RecipeHolder<?> param0) {
        Recipe<?> var0 = param0.value();
        if (var0 instanceof CraftingRecipe var1) {
            return switch(var1.category()) {
                case BUILDING -> RecipeBookCategories.CRAFTING_BUILDING_BLOCKS;
                case EQUIPMENT -> RecipeBookCategories.CRAFTING_EQUIPMENT;
                case REDSTONE -> RecipeBookCategories.CRAFTING_REDSTONE;
                case MISC -> RecipeBookCategories.CRAFTING_MISC;
            };
        } else {
            RecipeType<?> var2 = var0.getType();
            if (var0 instanceof AbstractCookingRecipe var3) {
                CookingBookCategory var4 = var3.category();
                if (var2 == RecipeType.SMELTING) {
                    return switch(var4) {
                        case BLOCKS -> RecipeBookCategories.FURNACE_BLOCKS;
                        case FOOD -> RecipeBookCategories.FURNACE_FOOD;
                        case MISC -> RecipeBookCategories.FURNACE_MISC;
                    };
                }

                if (var2 == RecipeType.BLASTING) {
                    return var4 == CookingBookCategory.BLOCKS ? RecipeBookCategories.BLAST_FURNACE_BLOCKS : RecipeBookCategories.BLAST_FURNACE_MISC;
                }

                if (var2 == RecipeType.SMOKING) {
                    return RecipeBookCategories.SMOKER_FOOD;
                }

                if (var2 == RecipeType.CAMPFIRE_COOKING) {
                    return RecipeBookCategories.CAMPFIRE;
                }
            }

            if (var2 == RecipeType.STONECUTTING) {
                return RecipeBookCategories.STONECUTTER;
            } else if (var2 == RecipeType.SMITHING) {
                return RecipeBookCategories.SMITHING;
            } else {
                LOGGER.warn(
                    "Unknown recipe category: {}/{}", LogUtils.defer(() -> BuiltInRegistries.RECIPE_TYPE.getKey(var0.getType())), LogUtils.defer(param0::id)
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
