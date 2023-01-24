package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ArmorDyeRecipe extends CustomRecipe {
    public ArmorDyeRecipe(ResourceLocation param0, CraftingBookCategory param1) {
        super(param0, param1);
    }

    public boolean matches(CraftingContainer param0, Level param1) {
        ItemStack var0 = ItemStack.EMPTY;
        List<ItemStack> var1 = Lists.newArrayList();

        for(int var2 = 0; var2 < param0.getContainerSize(); ++var2) {
            ItemStack var3 = param0.getItem(var2);
            if (!var3.isEmpty()) {
                if (var3.getItem() instanceof DyeableLeatherItem) {
                    if (!var0.isEmpty()) {
                        return false;
                    }

                    var0 = var3;
                } else {
                    if (!(var3.getItem() instanceof DyeItem)) {
                        return false;
                    }

                    var1.add(var3);
                }
            }
        }

        return !var0.isEmpty() && !var1.isEmpty();
    }

    public ItemStack assemble(CraftingContainer param0, RegistryAccess param1) {
        List<DyeItem> var0 = Lists.newArrayList();
        ItemStack var1 = ItemStack.EMPTY;

        for(int var2 = 0; var2 < param0.getContainerSize(); ++var2) {
            ItemStack var3 = param0.getItem(var2);
            if (!var3.isEmpty()) {
                Item var4 = var3.getItem();
                if (var4 instanceof DyeableLeatherItem) {
                    if (!var1.isEmpty()) {
                        return ItemStack.EMPTY;
                    }

                    var1 = var3.copy();
                } else {
                    if (!(var4 instanceof DyeItem)) {
                        return ItemStack.EMPTY;
                    }

                    var0.add((DyeItem)var4);
                }
            }
        }

        return !var1.isEmpty() && !var0.isEmpty() ? DyeableLeatherItem.dyeArmor(var1, var0) : ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int param0, int param1) {
        return param0 * param1 >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.ARMOR_DYE;
    }
}
