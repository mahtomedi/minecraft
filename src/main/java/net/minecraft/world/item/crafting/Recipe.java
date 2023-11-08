package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public interface Recipe<C extends Container> {
    Codec<Recipe<?>> CODEC = BuiltInRegistries.RECIPE_SERIALIZER.byNameCodec().dispatch(Recipe::getSerializer, RecipeSerializer::codec);

    boolean matches(C var1, Level var2);

    ItemStack assemble(C var1, RegistryAccess var2);

    boolean canCraftInDimensions(int var1, int var2);

    ItemStack getResultItem(RegistryAccess var1);

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

    default boolean showNotification() {
        return true;
    }

    default String getGroup() {
        return "";
    }

    default ItemStack getToastSymbol() {
        return new ItemStack(Blocks.CRAFTING_TABLE);
    }

    RecipeSerializer<?> getSerializer();

    RecipeType<?> getType();

    default boolean isIncomplete() {
        NonNullList<Ingredient> var0 = this.getIngredients();
        return var0.isEmpty() || var0.stream().anyMatch(param0 -> param0.getItems().length == 0);
    }
}
