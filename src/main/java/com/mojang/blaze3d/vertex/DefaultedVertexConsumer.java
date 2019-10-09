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

    public void defaultColor(int param0, int param1, int param2, int param3) {
        this.defaultR = param0;
        this.defaultG = param1;
        this.defaultB = param2;
        this.defaultA = param3;
        this.defaultColorSet = true;
    }
}
