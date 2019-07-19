package net.minecraft.world.item.crafting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FireworkRocketRecipe extends CustomRecipe {
    private static final Ingredient PAPER_INGREDIENT = Ingredient.of(Items.PAPER);
    private static final Ingredient GUNPOWDER_INGREDIENT = Ingredient.of(Items.GUNPOWDER);
    private static final Ingredient STAR_INGREDIENT = Ingredient.of(Items.FIREWORK_STAR);

    public FireworkRocketRecipe(ResourceLocation param0) {
        super(param0);
    }

    public boolean matches(CraftingContainer param0, Level param1) {
        boolean var0 = false;
        int var1 = 0;

        for(int var2 = 0; var2 < param0.getContainerSize(); ++var2) {
            ItemStack var3 = param0.getItem(var2);
            if (!var3.isEmpty()) {
                if (PAPER_INGREDIENT.test(var3)) {
                    if (var0) {
                        return false;
                    }

                    var0 = true;
                } else if (GUNPOWDER_INGREDIENT.test(var3)) {
                    if (++var1 > 3) {
                        return false;
                    }
                } else if (!STAR_INGREDIENT.test(var3)) {
                    return false;
                }
            }
        }

        return var0 && var1 >= 1;
    }

    public ItemStack assemble(CraftingContainer param0) {
        ItemStack var0 = new ItemStack(Items.FIREWORK_ROCKET, 3);
        CompoundTag var1 = var0.getOrCreateTagElement("Fireworks");
        ListTag var2 = new ListTag();
        int var3 = 0;

        for(int var4 = 0; var4 < param0.getContainerSize(); ++var4) {
            ItemStack var5 = param0.getItem(var4);
            if (!var5.isEmpty()) {
                if (GUNPOWDER_INGREDIENT.test(var5)) {
                    ++var3;
                } else if (STAR_INGREDIENT.test(var5)) {
                    CompoundTag var6 = var5.getTagElement("Explosion");
                    if (var6 != null) {
                        var2.add(var6);
                    }
                }
            }
        }

        var1.putByte("Flight", (byte)var3);
        if (!var2.isEmpty()) {
            var1.put("Explosions", var2);
        }

        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean canCraftInDimensions(int param0, int param1) {
        return param0 * param1 >= 2;
    }

    @Override
    public ItemStack getResultItem() {
        return new ItemStack(Items.FIREWORK_ROCKET);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.FIREWORK_ROCKET;
    }
}
