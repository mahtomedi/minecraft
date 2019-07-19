package com.mojang.blaze3d.vertex;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Tesselator {
    private final BufferBuilder builder;
    private final BufferUploader uploader = new BufferUploader();
    private static final Tesselator INSTANCE = new Tesselator(2097152);

    public static Tesselator getInstance() {
        return INSTANCE;
    }

    public Tesselator(int param0) {
        this.builder = new BufferBuilder(param0);
    }

    public void end() {
        this.builder.end();
        this.uploader.end(this.builder);
    }

    public BufferBuilder getBuilder() {
        return this.builder;
    }
}
