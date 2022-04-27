package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Tesselator {
    private static final int MAX_MEMORY_USE = 8388608;
    private static final int MAX_FLOATS = 2097152;
    private final BufferBuilder builder;
    private static final Tesselator INSTANCE = new Tesselator();

    public static Tesselator getInstance() {
        RenderSystem.assertOnGameThreadOrInit();
        return INSTANCE;
    }

    public Tesselator(int param0) {
        this.builder = new BufferBuilder(param0);
    }

    public Tesselator() {
        this(2097152);
    }

    public void end() {
        BufferUploader.drawWithShader(this.builder.end());
    }

    public BufferBuilder getBuilder() {
        return this.builder;
    }
}
