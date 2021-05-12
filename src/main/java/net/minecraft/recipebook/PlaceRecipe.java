package net.minecraft.recipebook;

import java.util.Iterator;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

public interface PlaceRecipe<T> {
    default void placeRecipe(int param0, int param1, int param2, Recipe<?> param3, Iterator<T> param4, int param5) {
        int var0 = param0;
        int var1 = param1;
        if (param3 instanceof ShapedRecipe var2) {
            var0 = var2.getWidth();
            var1 = var2.getHeight();
        }

        int var3 = 0;

        for(int var4 = 0; var4 < param1; ++var4) {
            if (var3 == param2) {
                ++var3;
            }

            boolean var5 = (float)var1 < (float)param1 / 2.0F;
            int var6 = Mth.floor((float)param1 / 2.0F - (float)var1 / 2.0F);
            if (var5 && var6 > var4) {
                var3 += param0;
                ++var4;
            }

            for(int var7 = 0; var7 < param0; ++var7) {
                if (!param4.hasNext()) {
                    return;
                }

                var5 = (float)var0 < (float)param0 / 2.0F;
                var6 = Mth.floor((float)param0 / 2.0F - (float)var0 / 2.0F);
                int var8 = var0;
                boolean var9 = var7 < var0;
                if (var5) {
                    var8 = var6 + var0;
                    var9 = var6 <= var7 && var7 < var6 + var0;
                }

                if (var9) {
                    this.addItemToSlot(param4, var3, param5, var4, var7);
                } else if (var8 == var7) {
                    var3 += param0 - var7;
                    break;
                }

                ++var3;
            }
        }

    }

    void addItemToSlot(Iterator<T> var1, int var2, int var3, int var4, int var5);
}
