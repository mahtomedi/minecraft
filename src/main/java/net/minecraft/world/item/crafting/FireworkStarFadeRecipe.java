package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class FireworkStarFadeRecipe extends CustomRecipe {
    private static final Ingredient STAR_INGREDIENT = Ingredient.of(Items.FIREWORK_STAR);

    public FireworkStarFadeRecipe(ResourceLocation param0, CraftingBookCategory param1) {
        super(param0, param1);
    }

    public boolean matches(CraftingContainer param0, Level param1) {
        boolean var0 = false;
        boolean var1 = false;

        for(int var2 = 0; var2 < param0.getContainerSize(); ++var2) {
            ItemStack var3 = param0.getItem(var2);
            if (!var3.isEmpty()) {
                if (var3.getItem() instanceof DyeItem) {
                    var0 = true;
                } else {
                    if (!STAR_INGREDIENT.test(var3)) {
                        return false;
                    }

                    if (var1) {
                        return false;
                    }

                    var1 = true;
                }
            }
        }

        return var1 && var0;
    }

    public ItemStack assemble(CraftingContainer param0, RegistryAccess param1) {
        List<Integer> var0 = Lists.newArrayList();
        ItemStack var1 = null;

        for(int var2 = 0; var2 < param0.getContainerSize(); ++var2) {
            ItemStack var3 = param0.getItem(var2);
            Item var4 = var3.getItem();
            if (var4 instanceof DyeItem) {
                var0.add(((DyeItem)var4).getDyeColor().getFireworkColor());
            } else if (STAR_INGREDIENT.test(var3)) {
                var1 = var3.copy();
                var1.setCount(1);
            }
        }

        if (var1 != null && !var0.isEmpty()) {
            var1.getOrCreateTagElement("Explosion").putIntArray("FadeColors", var0);
            return var1;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean canCraftInDimensions(int param0, int param1) {
        return param0 * param1 >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.FIREWORK_STAR_FADE;
    }
}
