package com.mojang.blaze3d.platform;

import java.util.OptionalInt;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DisplayData {
    public final int width;
    public final int height;
    public final OptionalInt fullscreenWidth;
    public final OptionalInt fullscreenHeight;
    public final boolean isFullscreen;

    public DisplayData(int param0, int param1, OptionalInt param2, OptionalInt param3, boolean param4) {
        this.width = param0;
        this.height = param1;
        this.fullscreenWidth = param2;
        this.fullscreenHeight = param3;
        this.isFullscreen = param4;
    }
}
