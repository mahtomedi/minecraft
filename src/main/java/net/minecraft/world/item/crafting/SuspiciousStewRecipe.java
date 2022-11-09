package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SuspiciousEffectHolder;

public class SuspiciousStewRecipe extends CustomRecipe {
    public SuspiciousStewRecipe(ResourceLocation param0, CraftingBookCategory param1) {
        super(param0, param1);
    }

    public boolean matches(CraftingContainer param0, Level param1) {
        boolean var0 = false;
        boolean var1 = false;
        boolean var2 = false;
        boolean var3 = false;

        for(int var4 = 0; var4 < param0.getContainerSize(); ++var4) {
            ItemStack var5 = param0.getItem(var4);
            if (!var5.isEmpty()) {
                if (var5.is(Blocks.BROWN_MUSHROOM.asItem()) && !var2) {
                    var2 = true;
                } else if (var5.is(Blocks.RED_MUSHROOM.asItem()) && !var1) {
                    var1 = true;
                } else if (var5.is(ItemTags.SMALL_FLOWERS) && !var0) {
                    var0 = true;
                } else {
                    if (!var5.is(Items.BOWL) || var3) {
                        return false;
                    }

                    var3 = true;
                }
            }
        }

        return var0 && var2 && var1 && var3;
    }

    public ItemStack assemble(CraftingContainer param0) {
        ItemStack var0 = new ItemStack(Items.SUSPICIOUS_STEW, 1);

        for(int var1 = 0; var1 < param0.getContainerSize(); ++var1) {
            ItemStack var2 = param0.getItem(var1);
            if (!var2.isEmpty()) {
                SuspiciousEffectHolder var3 = SuspiciousEffectHolder.tryGet(var2.getItem());
                if (var3 != null) {
                    SuspiciousStewItem.saveMobEffect(var0, var3.getSuspiciousEffect(), var3.getEffectDuration());
                    break;
                }
            }
        }

        return var0;
    }

    @Override
    public boolean canCraftInDimensions(int param0, int param1) {
        return param0 >= 2 && param1 >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SUSPICIOUS_STEW;
    }
}
