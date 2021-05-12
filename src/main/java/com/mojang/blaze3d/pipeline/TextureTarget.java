package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextureTarget extends RenderTarget {
    public TextureTarget(int param0, int param1, boolean param2, boolean param3) {
        super(param2);
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        this.resize(param0, param1, param3);
    }
}
