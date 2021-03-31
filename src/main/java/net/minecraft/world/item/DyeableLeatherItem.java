package net.minecraft.world.item;

import java.util.List;
import net.minecraft.nbt.CompoundTag;

public interface DyeableLeatherItem {
    String TAG_COLOR = "color";
    String TAG_DISPLAY = "display";
    int DEFAULT_LEATHER_COLOR = 10511680;

    default boolean hasCustomColor(ItemStack param0) {
        CompoundTag var0 = param0.getTagElement("display");
        return var0 != null && var0.contains("color", 99);
    }

    default int getColor(ItemStack param0) {
        CompoundTag var0 = param0.getTagElement("display");
        return var0 != null && var0.contains("color", 99) ? var0.getInt("color") : 10511680;
    }

    default void clearColor(ItemStack param0) {
        CompoundTag var0 = param0.getTagElement("display");
        if (var0 != null && var0.contains("color")) {
            var0.remove("color");
        }

    }

    default void setColor(ItemStack param0, int param1) {
        param0.getOrCreateTagElement("display").putInt("color", param1);
    }

    static ItemStack dyeArmor(ItemStack param0, List<DyeItem> param1) {
        ItemStack var0 = ItemStack.EMPTY;
        int[] var1 = new int[3];
        int var2 = 0;
        int var3 = 0;
        DyeableLeatherItem var4 = null;
        Item var5 = param0.getItem();
        if (var5 instanceof DyeableLeatherItem) {
            var4 = (DyeableLeatherItem)var5;
            var0 = param0.copy();
            var0.setCount(1);
            if (var4.hasCustomColor(param0)) {
                int var6 = var4.getColor(var0);
                float var7 = (float)(var6 >> 16 & 0xFF) / 255.0F;
                float var8 = (float)(var6 >> 8 & 0xFF) / 255.0F;
                float var9 = (float)(var6 & 0xFF) / 255.0F;
                var2 = (int)((float)var2 + Math.max(var7, Math.max(var8, var9)) * 255.0F);
                var1[0] = (int)((float)var1[0] + var7 * 255.0F);
                var1[1] = (int)((float)var1[1] + var8 * 255.0F);
                var1[2] = (int)((float)var1[2] + var9 * 255.0F);
                ++var3;
            }

            for(DyeItem var10 : param1) {
                float[] var11 = var10.getDyeColor().getTextureDiffuseColors();
                int var12 = (int)(var11[0] * 255.0F);
                int var13 = (int)(var11[1] * 255.0F);
                int var14 = (int)(var11[2] * 255.0F);
                var2 += Math.max(var12, Math.max(var13, var14));
                var1[0] += var12;
                var1[1] += var13;
                var1[2] += var14;
                ++var3;
            }
        }

        if (var4 == null) {
            return ItemStack.EMPTY;
        } else {
            int var15 = var1[0] / var3;
            int var16 = var1[1] / var3;
            int var17 = var1[2] / var3;
            float var18 = (float)var2 / (float)var3;
            float var19 = (float)Math.max(var15, Math.max(var16, var17));
            var15 = (int)((float)var15 * var18 / var19);
            var16 = (int)((float)var16 * var18 / var19);
            var17 = (int)((float)var17 * var18 / var19);
            int var26 = (var15 << 8) + var16;
            var26 = (var26 << 8) + var17;
            var4.setColor(var0, var26);
            return var0;
        }
    }
}
