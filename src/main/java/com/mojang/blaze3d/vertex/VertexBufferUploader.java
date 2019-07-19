package com.mojang.blaze3d.vertex;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VertexBufferUploader extends BufferUploader {
    private VertexBuffer buffer;

    @Override
    public void end(BufferBuilder param0) {
        param0.clear();
        this.buffer.upload(param0.getBuffer());
    }

    public void setBuffer(VertexBuffer param0) {
        this.buffer = param0;
    }
}
