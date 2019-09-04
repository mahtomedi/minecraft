package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VertexBuffer {
    private int id;
    private final VertexFormat format;
    private int vertexCount;

    public VertexBuffer(VertexFormat param0) {
        this.format = param0;
        this.id = GlStateManager.glGenBuffers();
    }

    public void bind() {
        GlStateManager.glBindBuffer(34962, this.id);
    }

    public void upload(ByteBuffer param0) {
        this.bind();
        GlStateManager.glBufferData(34962, param0, 35044);
        unbind();
        this.vertexCount = param0.limit() / this.format.getVertexSize();
    }

    public void draw(int param0) {
        RenderSystem.drawArrays(param0, 0, this.vertexCount);
    }

    public static void unbind() {
        GlStateManager.glBindBuffer(34962, 0);
    }

    public void delete() {
        if (this.id >= 0) {
            GlStateManager.glDeleteBuffers(this.id);
            this.id = -1;
        }

    }
}
