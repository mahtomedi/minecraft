package net.minecraft.world.damagesource;

import net.minecraft.util.Mth;

public class CombatRules {
    public static final float MAX_ARMOR = 20.0F;
    public static final float ARMOR_PROTECTION_DIVIDER = 25.0F;
    public static final float BASE_ARMOR_TOUGHNESS = 2.0F;
    public static final float MIN_ARMOR_RATIO = 0.2F;
    private static final int NUM_ARMOR_ITEMS = 4;

    public static float getDamageAfterAbsorb(float param0, float param1, float param2) {
        float var0 = 2.0F + param2 / 4.0F;
        float var1 = Mth.clamp(param1 - param0 / var0, param1 * 0.2F, 20.0F);
        return param0 * (1.0F - var1 / 25.0F);
    }

    public static float getDamageAfterMagicAbsorb(float param0, float param1) {
        float var0 = Mth.clamp(param1, 0.0F, 20.0F);
        return param0 * (1.0F - var0 / 25.0F);
    }
}
