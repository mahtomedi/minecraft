package com.mojang.blaze3d.shaders;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface Effect {
    int getId();

    void markDirty();

    Program getVertexProgram();

    Program getFragmentProgram();
}
