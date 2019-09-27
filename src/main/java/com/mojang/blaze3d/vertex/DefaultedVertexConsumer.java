package com.mojang.blaze3d.vertex;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class DefaultedVertexConsumer implements VertexConsumer {
    protected boolean defaultColorSet = false;
    protected int defaultR = 255;
    protected int defaultG = 255;
    protected int defaultB = 255;
    protected int defaultA = 255;
    protected boolean defaultOverlayCoordsSet = false;
    protected int defaultOverlayU = 0;
    protected int defaultOverlayV = 10;

    public void defaultColor(int param0, int param1, int param2, int param3) {
        this.defaultR = param0;
        this.defaultG = param1;
        this.defaultB = param2;
        this.defaultA = param3;
        this.defaultColorSet = true;
    }

    @Override
    public void defaultOverlayCoords(int param0, int param1) {
        this.defaultOverlayU = param0;
        this.defaultOverlayV = param1;
        this.defaultOverlayCoordsSet = true;
    }

    @Override
    public void unsetDefaultOverlayCoords() {
        this.defaultOverlayCoordsSet = false;
    }
}
