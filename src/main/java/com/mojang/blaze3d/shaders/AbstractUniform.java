package com.mojang.blaze3d.shaders;

import com.mojang.math.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AbstractUniform {
    public void set(float param0) {
    }

    public void set(float param0, float param1) {
    }

    public void set(float param0, float param1, float param2) {
    }

    public void set(float param0, float param1, float param2, float param3) {
    }

    public void setSafe(float param0, float param1, float param2, float param3) {
    }

    public void setSafe(int param0, int param1, int param2, int param3) {
    }

    public void set(float[] param0) {
    }

    public void set(Matrix4f param0) {
    }
}
