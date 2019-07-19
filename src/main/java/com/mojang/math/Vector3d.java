package com.mojang.math;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Vector3d {
    public double x;
    public double y;
    public double z;

    public Vector3d(double param0, double param1, double param2) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
    }
}
