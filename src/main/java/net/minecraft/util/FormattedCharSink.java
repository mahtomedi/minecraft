package net.minecraft.util;

import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@FunctionalInterface
public interface FormattedCharSink {
    @OnlyIn(Dist.CLIENT)
    boolean accept(int var1, Style var2, int var3);
}
