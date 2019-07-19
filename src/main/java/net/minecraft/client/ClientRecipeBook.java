package net.minecraft.client;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientRecipeBook extends RecipeBook {
    private final RecipeManager recipes;
    private final Map<RecipeBookCategories, List<RecipeCollection>> collectionsByTab = Maps.newHashMap();
    private final List<RecipeCollection> collections = Lists.newArrayList();

    public ClientRecipeBook(RecipeManager param0) {
        this.recipes = param0;
    }

    public void setupCollections() {
        this.collections.clear();
        this.collectionsByTab.clear();
        Table<RecipeBookCategories, String, RecipeCollection> var0 = HashBasedTable.create();

        for(Recipe<?> var1 : this.recipes.getRecipes()) {
            if (!var1.isSpecial()) {
                RecipeBookCategories var2 = getCategory(var1);
                String var3 = var1.getGroup();
                RecipeCollection var4;
                if (var3.isEmpty()) {
                    var4 = this.createCollection(var2);
                } else {
                    var4 = var0.get(var2, var3);
                    if (var4 == null) {
                        var4 = this.createCollection(var2);
                        var0.put(var2, var3, var4);
                    }
                }

                var4.add(var1);
            }
        }

    }

    private RecipeCollection createCollection(RecipeBookCategories param0) {
        RecipeCollection var0 = new RecipeCollection();
        this.collections.add(var0);
        this.collectionsByTab.computeIfAbsent(param0, param0x -> Lists.newArrayList()).add(var0);
        if (param0 == RecipeBookCategories.FURNACE_BLOCKS || param0 == RecipeBookCategories.FURNACE_FOOD || param0 == RecipeBookCategories.FURNACE_MISC) {
            this.addToCollection(RecipeBookCategories.FURNACE_SEARCH, var0);
        } else if (param0 == RecipeBookCategories.BLAST_FURNACE_BLOCKS || param0 == RecipeBookCategories.BLAST_FURNACE_MISC) {
            this.addToCollection(RecipeBookCategories.BLAST_FURNACE_SEARCH, var0);
        } else if (param0 == RecipeBookCategories.SMOKER_FOOD) {
            this.addToCollection(RecipeBookCategories.SMOKER_SEARCH, var0);
        } else if (param0 == RecipeBookCategories.STONECUTTER) {
            this.addToCollection(RecipeBookCategories.STONECUTTER, var0);
        } else if (param0 == RecipeBookCategories.CAMPFIRE) {
            this.addToCollection(RecipeBookCategories.CAMPFIRE, var0);
        } else {
            this.addToCollection(RecipeBookCategories.SEARCH, var0);
        }

        return var0;
    }

    private void addToCollection(RecipeBookCategories param0, RecipeCollection param1) {
        this.collectionsByTab.computeIfAbsent(param0, param0x -> Lists.newArrayList()).add(param1);
    }

    private static RecipeBookCategories getCategory(Recipe<?> param0) {
        RecipeType<?> var0 = param0.getType();
        if (var0 == RecipeType.SMELTING) {
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
        } else {
            ItemStack var1 = param0.getResultItem();
            CreativeModeTab var2 = var1.getItem().getItemCategory();
            if (var2 == CreativeModeTab.TAB_BUILDING_BLOCKS) {
                return RecipeBookCategories.BUILDING_BLOCKS;
            } else if (var2 == CreativeModeTab.TAB_TOOLS || var2 == CreativeModeTab.TAB_COMBAT) {
                return RecipeBookCategories.EQUIPMENT;
            } else {
                return var2 == CreativeModeTab.TAB_REDSTONE ? RecipeBookCategories.REDSTONE : RecipeBookCategories.MISC;
            }
        }
    }

    public static List<RecipeBookCategories> getCategories(RecipeBookMenu<?> param0) {
        if (param0 instanceof CraftingMenu || param0 instanceof InventoryMenu) {
            return Lists.newArrayList(
                RecipeBookCategories.SEARCH,
                RecipeBookCategories.EQUIPMENT,
                RecipeBookCategories.BUILDING_BLOCKS,
                RecipeBookCategories.MISC,
                RecipeBookCategories.REDSTONE
            );
        } else if (param0 instanceof FurnaceMenu) {
            return Lists.newArrayList(
                RecipeBookCategories.FURNACE_SEARCH, RecipeBookCategories.FURNACE_FOOD, RecipeBookCategories.FURNACE_BLOCKS, RecipeBookCategories.FURNACE_MISC
            );
        } else if (param0 instanceof BlastFurnaceMenu) {
            return Lists.newArrayList(
                RecipeBookCategories.BLAST_FURNACE_SEARCH, RecipeBookCategories.BLAST_FURNACE_BLOCKS, RecipeBookCategories.BLAST_FURNACE_MISC
            );
        } else {
            return param0 instanceof SmokerMenu
                ? Lists.newArrayList(RecipeBookCategories.SMOKER_SEARCH, RecipeBookCategories.SMOKER_FOOD)
                : Lists.newArrayList();
        }
    }

    public List<RecipeCollection> getCollections() {
        return this.collections;
    }

    public List<RecipeCollection> getCollection(RecipeBookCategories param0) {
        return this.collectionsByTab.getOrDefault(param0, Collections.emptyList());
    }
}
