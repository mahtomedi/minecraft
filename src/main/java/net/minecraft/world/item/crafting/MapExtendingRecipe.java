package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapExtendingRecipe extends ShapedRecipe {
    public MapExtendingRecipe(CraftingBookCategory param0) {
        super(
            "",
            param0,
            3,
            3,
            NonNullList.of(
                Ingredient.EMPTY,
                Ingredient.of(Items.PAPER),
                Ingredient.of(Items.PAPER),
                Ingredient.of(Items.PAPER),
                Ingredient.of(Items.PAPER),
                Ingredient.of(Items.FILLED_MAP),
                Ingredient.of(Items.PAPER),
                Ingredient.of(Items.PAPER),
                Ingredient.of(Items.PAPER),
                Ingredient.of(Items.PAPER)
            ),
            new ItemStack(Items.MAP)
        );
    }

    @Override
    public boolean matches(CraftingContainer param0, Level param1) {
        if (!super.matches(param0, param1)) {
            return false;
        } else {
            ItemStack var0 = findFilledMap(param0);
            if (var0.isEmpty()) {
                return false;
            } else {
                MapItemSavedData var1 = MapItem.getSavedData(var0, param1);
                if (var1 == null) {
                    return false;
                } else if (var1.isExplorationMap()) {
                    return false;
                } else {
                    return var1.scale < 4;
                }
            }
        }
    }

    @Override
    public ItemStack assemble(CraftingContainer param0, RegistryAccess param1) {
        ItemStack var0 = findFilledMap(param0).copyWithCount(1);
        var0.getOrCreateTag().putInt("map_scale_direction", 1);
        return var0;
    }

    private static ItemStack findFilledMap(CraftingContainer param0) {
        for(int var0 = 0; var0 < param0.getContainerSize(); ++var0) {
            ItemStack var1 = param0.getItem(var0);
            if (var1.is(Items.FILLED_MAP)) {
                return var1;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.MAP_EXTENDING;
    }
}
