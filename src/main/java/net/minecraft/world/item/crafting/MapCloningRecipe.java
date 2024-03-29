package net.minecraft.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class MapCloningRecipe extends CustomRecipe {
    public MapCloningRecipe(CraftingBookCategory param0) {
        super(param0);
    }

    public boolean matches(CraftingContainer param0, Level param1) {
        int var0 = 0;
        ItemStack var1 = ItemStack.EMPTY;

        for(int var2 = 0; var2 < param0.getContainerSize(); ++var2) {
            ItemStack var3 = param0.getItem(var2);
            if (!var3.isEmpty()) {
                if (var3.is(Items.FILLED_MAP)) {
                    if (!var1.isEmpty()) {
                        return false;
                    }

                    var1 = var3;
                } else {
                    if (!var3.is(Items.MAP)) {
                        return false;
                    }

                    ++var0;
                }
            }
        }

        return !var1.isEmpty() && var0 > 0;
    }

    public ItemStack assemble(CraftingContainer param0, RegistryAccess param1) {
        int var0 = 0;
        ItemStack var1 = ItemStack.EMPTY;

        for(int var2 = 0; var2 < param0.getContainerSize(); ++var2) {
            ItemStack var3 = param0.getItem(var2);
            if (!var3.isEmpty()) {
                if (var3.is(Items.FILLED_MAP)) {
                    if (!var1.isEmpty()) {
                        return ItemStack.EMPTY;
                    }

                    var1 = var3;
                } else {
                    if (!var3.is(Items.MAP)) {
                        return ItemStack.EMPTY;
                    }

                    ++var0;
                }
            }
        }

        return !var1.isEmpty() && var0 >= 1 ? var1.copyWithCount(var0 + 1) : ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int param0, int param1) {
        return param0 >= 3 && param1 >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.MAP_CLONING;
    }
}
