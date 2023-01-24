package net.minecraft.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;

public class ShulkerBoxColoring extends CustomRecipe {
    public ShulkerBoxColoring(ResourceLocation param0, CraftingBookCategory param1) {
        super(param0, param1);
    }

    public boolean matches(CraftingContainer param0, Level param1) {
        int var0 = 0;
        int var1 = 0;

        for(int var2 = 0; var2 < param0.getContainerSize(); ++var2) {
            ItemStack var3 = param0.getItem(var2);
            if (!var3.isEmpty()) {
                if (Block.byItem(var3.getItem()) instanceof ShulkerBoxBlock) {
                    ++var0;
                } else {
                    if (!(var3.getItem() instanceof DyeItem)) {
                        return false;
                    }

                    ++var1;
                }

                if (var1 > 1 || var0 > 1) {
                    return false;
                }
            }
        }

        return var0 == 1 && var1 == 1;
    }

    public ItemStack assemble(CraftingContainer param0, RegistryAccess param1) {
        ItemStack var0 = ItemStack.EMPTY;
        DyeItem var1 = (DyeItem)Items.WHITE_DYE;

        for(int var2 = 0; var2 < param0.getContainerSize(); ++var2) {
            ItemStack var3 = param0.getItem(var2);
            if (!var3.isEmpty()) {
                Item var4 = var3.getItem();
                if (Block.byItem(var4) instanceof ShulkerBoxBlock) {
                    var0 = var3;
                } else if (var4 instanceof DyeItem) {
                    var1 = (DyeItem)var4;
                }
            }
        }

        ItemStack var5 = ShulkerBoxBlock.getColoredItemStack(var1.getDyeColor());
        if (var0.hasTag()) {
            var5.setTag(var0.getTag().copy());
        }

        return var5;
    }

    @Override
    public boolean canCraftInDimensions(int param0, int param1) {
        return param0 * param1 >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHULKER_BOX_COLORING;
    }
}
