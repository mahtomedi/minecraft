package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TippedArrowRecipe extends CustomRecipe {
    public TippedArrowRecipe(ResourceLocation param0) {
        super(param0);
    }

    public boolean matches(CraftingContainer param0, Level param1) {
        if (param0.getWidth() == 3 && param0.getHeight() == 3) {
            for(int var0 = 0; var0 < param0.getWidth(); ++var0) {
                for(int var1 = 0; var1 < param0.getHeight(); ++var1) {
                    ItemStack var2 = param0.getItem(var0 + var1 * param0.getWidth());
                    if (var2.isEmpty()) {
                        return false;
                    }

                    Item var3 = var2.getItem();
                    if (var0 == 1 && var1 == 1) {
                        if (var3 != Items.LINGERING_POTION) {
                            return false;
                        }
                    } else if (var3 != Items.ARROW) {
                        return false;
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public ItemStack assemble(CraftingContainer param0) {
        ItemStack var0 = param0.getItem(1 + param0.getWidth());
        if (var0.getItem() != Items.LINGERING_POTION) {
            return ItemStack.EMPTY;
        } else {
            ItemStack var1 = new ItemStack(Items.TIPPED_ARROW, 8);
            PotionUtils.setPotion(var1, PotionUtils.getPotion(var0));
            PotionUtils.setCustomEffects(var1, PotionUtils.getCustomEffects(var0));
            return var1;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean canCraftInDimensions(int param0, int param1) {
        return param0 >= 2 && param1 >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.TIPPED_ARROW;
    }
}
