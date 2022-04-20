package net.minecraft.client.gui.screens;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LoadingDotsText {
    private static final String[] FRAMES = new String[]{"O o o", "o O o", "o o O", "o O o"};
    private static final long INTERVAL_MS = 300L;

    public static String get(long param0) {
        int var0 = (int)(param0 / 300L % (long)FRAMES.length);
        return FRAMES[var0];
    }
}
