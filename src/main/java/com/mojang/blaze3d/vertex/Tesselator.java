package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Tesselator {
    private final BufferBuilder builder;
    private static final Tesselator INSTANCE = new Tesselator();

    public static Tesselator getInstance() {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        return INSTANCE;
    }

    public Tesselator(int param0) {
        this.builder = new BufferBuilder(param0);
    }

    public Tesselator() {
        this(2097152);
    }

    public void end() {
        this.builder.end();
        BufferUploader.end(this.builder);
    }

    public BufferBuilder getBuilder() {
        return this.builder;
    }
}
