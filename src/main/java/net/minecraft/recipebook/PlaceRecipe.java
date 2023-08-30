package net.minecraft.recipebook;

import java.util.Iterator;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;

public interface PlaceRecipe<T> {
    default void placeRecipe(int param0, int param1, int param2, RecipeHolder<?> param3, Iterator<T> param4, int param5) {
        int var0 = param0;
        int var1 = param1;
        Recipe<?> var2 = param3.value();
        if (var2 instanceof ShapedRecipe var3) {
            var0 = var3.getWidth();
            var1 = var3.getHeight();
        }

        int var4 = 0;

        for(int var5 = 0; var5 < param1; ++var5) {
            if (var4 == param2) {
                ++var4;
            }

            boolean var6 = (float)var1 < (float)param1 / 2.0F;
            int var7 = Mth.floor((float)param1 / 2.0F - (float)var1 / 2.0F);
            if (var6 && var7 > var5) {
                var4 += param0;
                ++var5;
            }

            for(int var8 = 0; var8 < param0; ++var8) {
                if (!param4.hasNext()) {
                    return;
                }

                var6 = (float)var0 < (float)param0 / 2.0F;
                var7 = Mth.floor((float)param0 / 2.0F - (float)var0 / 2.0F);
                int var9 = var0;
                boolean var10 = var8 < var0;
                if (var6) {
                    var9 = var7 + var0;
                    var10 = var7 <= var8 && var8 < var7 + var0;
                }

                if (var10) {
                    this.addItemToSlot(param4, var4, param5, var5, var8);
                } else if (var9 == var8) {
                    var4 += param0 - var8;
                    break;
                }

                ++var4;
            }
        }

    }

    void addItemToSlot(Iterator<T> var1, int var2, int var3, int var4, int var5);
}
