package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapExtendingRecipe extends ShapedRecipe {
    public MapExtendingRecipe(ResourceLocation param0) {
        super(
            param0,
            "",
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
            ItemStack var0 = ItemStack.EMPTY;

            for(int var1 = 0; var1 < param0.getContainerSize() && var0.isEmpty(); ++var1) {
                ItemStack var2 = param0.getItem(var1);
                if (var2.getItem() == Items.FILLED_MAP) {
                    var0 = var2;
                }
            }

            if (var0.isEmpty()) {
                return false;
            } else {
                MapItemSavedData var3 = MapItem.getOrCreateSavedData(var0, param1);
                if (var3 == null) {
                    return false;
                } else if (this.isExplorationMap(var3)) {
                    return false;
                } else {
                    return var3.scale < 4;
                }
            }
        }
    }

    private boolean isExplorationMap(MapItemSavedData param0) {
        if (param0.decorations != null) {
            for(MapDecoration var0 : param0.decorations.values()) {
                if (var0.getType() == MapDecoration.Type.MANSION || var0.getType() == MapDecoration.Type.MONUMENT) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public ItemStack assemble(CraftingContainer param0) {
        ItemStack var0 = ItemStack.EMPTY;

        for(int var1 = 0; var1 < param0.getContainerSize() && var0.isEmpty(); ++var1) {
            ItemStack var2 = param0.getItem(var1);
            if (var2.getItem() == Items.FILLED_MAP) {
                var0 = var2;
            }
        }

        var0 = var0.copy();
        var0.setCount(1);
        var0.getOrCreateTag().putInt("map_scale_direction", 1);
        return var0;
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
