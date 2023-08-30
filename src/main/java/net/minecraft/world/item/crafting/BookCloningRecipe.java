package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.Level;

public class BookCloningRecipe extends CustomRecipe {
    public BookCloningRecipe(CraftingBookCategory param0) {
        super(param0);
    }

    public boolean matches(CraftingContainer param0, Level param1) {
        int var0 = 0;
        ItemStack var1 = ItemStack.EMPTY;

        for(int var2 = 0; var2 < param0.getContainerSize(); ++var2) {
            ItemStack var3 = param0.getItem(var2);
            if (!var3.isEmpty()) {
                if (var3.is(Items.WRITTEN_BOOK)) {
                    if (!var1.isEmpty()) {
                        return false;
                    }

                    var1 = var3;
                } else {
                    if (!var3.is(Items.WRITABLE_BOOK)) {
                        return false;
                    }

                    ++var0;
                }
            }
        }

        return !var1.isEmpty() && var1.hasTag() && var0 > 0;
    }

    public ItemStack assemble(CraftingContainer param0, RegistryAccess param1) {
        int var0 = 0;
        ItemStack var1 = ItemStack.EMPTY;

        for(int var2 = 0; var2 < param0.getContainerSize(); ++var2) {
            ItemStack var3 = param0.getItem(var2);
            if (!var3.isEmpty()) {
                if (var3.is(Items.WRITTEN_BOOK)) {
                    if (!var1.isEmpty()) {
                        return ItemStack.EMPTY;
                    }

                    var1 = var3;
                } else {
                    if (!var3.is(Items.WRITABLE_BOOK)) {
                        return ItemStack.EMPTY;
                    }

                    ++var0;
                }
            }
        }

        if (!var1.isEmpty() && var1.hasTag() && var0 >= 1 && WrittenBookItem.getGeneration(var1) < 2) {
            ItemStack var4 = new ItemStack(Items.WRITTEN_BOOK, var0);
            CompoundTag var5 = var1.getTag().copy();
            var5.putInt("generation", WrittenBookItem.getGeneration(var1) + 1);
            var4.setTag(var5);
            return var4;
        } else {
            return ItemStack.EMPTY;
        }
    }

    public NonNullList<ItemStack> getRemainingItems(CraftingContainer param0) {
        NonNullList<ItemStack> var0 = NonNullList.withSize(param0.getContainerSize(), ItemStack.EMPTY);

        for(int var1 = 0; var1 < var0.size(); ++var1) {
            ItemStack var2 = param0.getItem(var1);
            if (var2.getItem().hasCraftingRemainingItem()) {
                var0.set(var1, new ItemStack(var2.getItem().getCraftingRemainingItem()));
            } else if (var2.getItem() instanceof WrittenBookItem) {
                var0.set(var1, var2.copyWithCount(1));
                break;
            }
        }

        return var0;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.BOOK_CLONING;
    }

    @Override
    public boolean canCraftInDimensions(int param0, int param1) {
        return param0 >= 3 && param1 >= 3;
    }
}
