package net.minecraft.client;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.core.Registry;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientRecipeBook extends RecipeBook {
    private static final Logger LOGGER = LogManager.getLogger();
    private Map<RecipeBookCategories, List<RecipeCollection>> collectionsByTab = ImmutableMap.of();
    private List<RecipeCollection> allCollections = ImmutableList.of();

    public void setupCollections(Iterable<Recipe<?>> param0) {
        Map<RecipeBookCategories, List<List<Recipe<?>>>> var0 = categorizeAndGroupRecipes(param0);
        Map<RecipeBookCategories, List<RecipeCollection>> var1 = Maps.newHashMap();
        Builder<RecipeCollection> var2 = ImmutableList.builder();
        var0.forEach((param2, param3) -> param3.stream().map(RecipeCollection::new).peek(var2::add));
        RecipeBookCategories.AGGREGATE_CATEGORIES
            .forEach((param1, param2) -> param2.stream().flatMap(param1x -> var1.getOrDefault(param1x, ImmutableList.of()).stream()));
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
        RecipeType<?> var0 = param0.getType();
        if (var0 == RecipeType.CRAFTING) {
            ItemStack var1 = param0.getResultItem();
            CreativeModeTab var2 = var1.getItem().getItemCategory();
            if (var2 == CreativeModeTab.TAB_BUILDING_BLOCKS) {
                return RecipeBookCategories.CRAFTING_BUILDING_BLOCKS;
            } else if (var2 == CreativeModeTab.TAB_TOOLS || var2 == CreativeModeTab.TAB_COMBAT) {
                return RecipeBookCategories.CRAFTING_EQUIPMENT;
            } else {
                return var2 == CreativeModeTab.TAB_REDSTONE ? RecipeBookCategories.CRAFTING_REDSTONE : RecipeBookCategories.CRAFTING_MISC;
            }
        } else if (var0 == RecipeType.SMELTING) {
            if (param0.getResultItem().getItem().isEdible()) {
                return RecipeBookCategories.FURNACE_FOOD;
            } else {
                return param0.getResultItem().getItem() instanceof BlockItem ? RecipeBookCategories.FURNACE_BLOCKS : RecipeBookCategories.FURNACE_MISC;
            }
        } else if (var0 == RecipeType.BLASTING) {
            return param0.getResultItem().getItem() instanceof BlockItem ? RecipeBookCategories.BLAST_FURNACE_BLOCKS : RecipeBookCategories.BLAST_FURNACE_MISC;
        } else if (var0 == RecipeType.SMOKING) {
            return RecipeBookCategories.SMOKER_FOOD;
        } else if (var0 == RecipeType.STONECUTTING) {
            return RecipeBookCategories.STONECUTTER;
        } else if (var0 == RecipeType.CAMPFIRE_COOKING) {
            return RecipeBookCategories.CAMPFIRE;
        } else if (var0 == RecipeType.SMITHING) {
            return RecipeBookCategories.SMITHING;
        } else {
            LOGGER.warn("Unknown recipe category: {}/{}", () -> Registry.RECIPE_TYPE.getKey(param0.getType()), param0::getId);
            return RecipeBookCategories.UNKNOWN;
        }
    }

    public List<RecipeCollection> getCollections() {
        return this.allCollections;
    }

    public List<RecipeCollection> getCollection(RecipeBookCategories param0) {
        return this.collectionsByTab.getOrDefault(param0, Collections.emptyList());
    }
}
