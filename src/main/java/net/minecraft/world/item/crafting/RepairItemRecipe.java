package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RepairItemRecipe extends CustomRecipe {
    public RepairItemRecipe(ResourceLocation param0) {
        super(param0);
    }

    public boolean matches(CraftingContainer param0, Level param1) {
        List<ItemStack> var0 = Lists.newArrayList();

        for(int var1 = 0; var1 < param0.getContainerSize(); ++var1) {
            ItemStack var2 = param0.getItem(var1);
            if (!var2.isEmpty()) {
                var0.add(var2);
                if (var0.size() > 1) {
                    ItemStack var3 = var0.get(0);
                    if (var2.getItem() != var3.getItem() || var3.getCount() != 1 || var2.getCount() != 1 || !var3.getItem().canBeDepleted()) {
                        return false;
                    }
                }
            }
        }

        return var0.size() == 2;
    }

    public ItemStack assemble(CraftingContainer param0) {
        List<ItemStack> var0 = Lists.newArrayList();

        for(int var1 = 0; var1 < param0.getContainerSize(); ++var1) {
            ItemStack var2 = param0.getItem(var1);
            if (!var2.isEmpty()) {
                var0.add(var2);
                if (var0.size() > 1) {
                    ItemStack var3 = var0.get(0);
                    if (var2.getItem() != var3.getItem() || var3.getCount() != 1 || var2.getCount() != 1 || !var3.getItem().canBeDepleted()) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        if (var0.size() == 2) {
            ItemStack var4 = var0.get(0);
            ItemStack var5 = var0.get(1);
            if (var4.getItem() == var5.getItem() && var4.getCount() == 1 && var5.getCount() == 1 && var4.getItem().canBeDepleted()) {
                Item var6 = var4.getItem();
                int var7 = var6.getMaxDamage() - var4.getDamageValue();
                int var8 = var6.getMaxDamage() - var5.getDamageValue();
                int var9 = var7 + var8 + var6.getMaxDamage() * 5 / 100;
                int var10 = var6.getMaxDamage() - var9;
                if (var10 < 0) {
                    var10 = 0;
                }

                ItemStack var11 = new ItemStack(var4.getItem());
                var11.setDamageValue(var10);
                return var11;
            }
        }

        return ItemStack.EMPTY;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean canCraftInDimensions(int param0, int param1) {
        return param0 * param1 >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.REPAIR_ITEM;
    }
}
