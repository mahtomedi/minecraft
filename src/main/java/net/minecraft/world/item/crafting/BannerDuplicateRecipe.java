package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;

public class BannerDuplicateRecipe extends CustomRecipe {
    public BannerDuplicateRecipe(ResourceLocation param0, CraftingBookCategory param1) {
        super(param0, param1);
    }

    public boolean matches(CraftingContainer param0, Level param1) {
        DyeColor var0 = null;
        ItemStack var1 = null;
        ItemStack var2 = null;

        for(int var3 = 0; var3 < param0.getContainerSize(); ++var3) {
            ItemStack var4 = param0.getItem(var3);
            if (!var4.isEmpty()) {
                Item var5 = var4.getItem();
                if (!(var5 instanceof BannerItem)) {
                    return false;
                }

                BannerItem var6 = (BannerItem)var5;
                if (var0 == null) {
                    var0 = var6.getColor();
                } else if (var0 != var6.getColor()) {
                    return false;
                }

                int var7 = BannerBlockEntity.getPatternCount(var4);
                if (var7 > 6) {
                    return false;
                }

                if (var7 > 0) {
                    if (var1 != null) {
                        return false;
                    }

                    var1 = var4;
                } else {
                    if (var2 != null) {
                        return false;
                    }

                    var2 = var4;
                }
            }
        }

        return var1 != null && var2 != null;
    }

    public ItemStack assemble(CraftingContainer param0, RegistryAccess param1) {
        for(int var0 = 0; var0 < param0.getContainerSize(); ++var0) {
            ItemStack var1 = param0.getItem(var0);
            if (!var1.isEmpty()) {
                int var2 = BannerBlockEntity.getPatternCount(var1);
                if (var2 > 0 && var2 <= 6) {
                    return var1.copyWithCount(1);
                }
            }
        }

        return ItemStack.EMPTY;
    }

    public NonNullList<ItemStack> getRemainingItems(CraftingContainer param0) {
        NonNullList<ItemStack> var0 = NonNullList.withSize(param0.getContainerSize(), ItemStack.EMPTY);

        for(int var1 = 0; var1 < var0.size(); ++var1) {
            ItemStack var2 = param0.getItem(var1);
            if (!var2.isEmpty()) {
                if (var2.getItem().hasCraftingRemainingItem()) {
                    var0.set(var1, new ItemStack(var2.getItem().getCraftingRemainingItem()));
                } else if (var2.hasTag() && BannerBlockEntity.getPatternCount(var2) > 0) {
                    var0.set(var1, var2.copyWithCount(1));
                }
            }
        }

        return var0;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.BANNER_DUPLICATE;
    }

    @Override
    public boolean canCraftInDimensions(int param0, int param1) {
        return param0 * param1 >= 2;
    }
}
