package net.minecraft.world.item.crafting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ShieldDecorationRecipe extends CustomRecipe {
    public ShieldDecorationRecipe(ResourceLocation param0, CraftingBookCategory param1) {
        super(param0, param1);
    }

    public boolean matches(CraftingContainer param0, Level param1) {
        ItemStack var0 = ItemStack.EMPTY;
        ItemStack var1 = ItemStack.EMPTY;

        for(int var2 = 0; var2 < param0.getContainerSize(); ++var2) {
            ItemStack var3 = param0.getItem(var2);
            if (!var3.isEmpty()) {
                if (var3.getItem() instanceof BannerItem) {
                    if (!var1.isEmpty()) {
                        return false;
                    }

                    var1 = var3;
                } else {
                    if (!var3.is(Items.SHIELD)) {
                        return false;
                    }

                    if (!var0.isEmpty()) {
                        return false;
                    }

                    if (BlockItem.getBlockEntityData(var3) != null) {
                        return false;
                    }

                    var0 = var3;
                }
            }
        }

        return !var0.isEmpty() && !var1.isEmpty();
    }

    public ItemStack assemble(CraftingContainer param0) {
        ItemStack var0 = ItemStack.EMPTY;
        ItemStack var1 = ItemStack.EMPTY;

        for(int var2 = 0; var2 < param0.getContainerSize(); ++var2) {
            ItemStack var3 = param0.getItem(var2);
            if (!var3.isEmpty()) {
                if (var3.getItem() instanceof BannerItem) {
                    var0 = var3;
                } else if (var3.is(Items.SHIELD)) {
                    var1 = var3.copy();
                }
            }
        }

        if (var1.isEmpty()) {
            return var1;
        } else {
            CompoundTag var4 = BlockItem.getBlockEntityData(var0);
            CompoundTag var5 = var4 == null ? new CompoundTag() : var4.copy();
            var5.putInt("Base", ((BannerItem)var0.getItem()).getColor().getId());
            BlockItem.setBlockEntityData(var1, BlockEntityType.BANNER, var5);
            return var1;
        }
    }

    @Override
    public boolean canCraftInDimensions(int param0, int param1) {
        return param0 * param1 >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHIELD_DECORATION;
    }
}
