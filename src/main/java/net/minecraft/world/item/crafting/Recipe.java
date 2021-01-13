package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface Recipe<C extends Container> {
    boolean matches(C var1, Level var2);

    ItemStack assemble(C var1);

    @OnlyIn(Dist.CLIENT)
    boolean canCraftInDimensions(int var1, int var2);

    ItemStack getResultItem();

    default NonNullList<ItemStack> getRemainingItems(C param0) {
        NonNullList<ItemStack> var0 = NonNullList.withSize(param0.getContainerSize(), ItemStack.EMPTY);

        for(int var1 = 0; var1 < var0.size(); ++var1) {
            Item var2 = param0.getItem(var1).getItem();
            if (var2.hasCraftingRemainingItem()) {
                var0.set(var1, new ItemStack(var2.getCraftingRemainingItem()));
            }
        }

        return var0;
    }

    default NonNullList<Ingredient> getIngredients() {
        return NonNullList.create();
    }

    default boolean isSpecial() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    default String getGroup() {
        return "";
    }

    @OnlyIn(Dist.CLIENT)
    default ItemStack getToastSymbol() {
        return new ItemStack(Blocks.CRAFTING_TABLE);
    }

    ResourceLocation getId();

    RecipeSerializer<?> getSerializer();

    RecipeType<?> getType();
}
