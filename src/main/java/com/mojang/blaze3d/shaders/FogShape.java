package com.mojang.blaze3d.shaders;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum FogShape {
    SPHERE(0),
    CYLINDER(1);

    private final int index;

    private FogShape(int param0) {
        this.index = param0;
    }

    public int getIndex() {
        return this.index;
    }
}
