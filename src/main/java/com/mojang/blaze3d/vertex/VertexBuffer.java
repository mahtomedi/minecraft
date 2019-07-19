package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
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
        this.id = GLX.glGenBuffers();
    }

    public void bind() {
        GLX.glBindBuffer(GLX.GL_ARRAY_BUFFER, this.id);
    }

    public void upload(ByteBuffer param0) {
        this.bind();
        GLX.glBufferData(GLX.GL_ARRAY_BUFFER, param0, 35044);
        unbind();
        this.vertexCount = param0.limit() / this.format.getVertexSize();
    }

    public void draw(int param0) {
        GlStateManager.drawArrays(param0, 0, this.vertexCount);
    }

    public static void unbind() {
        GLX.glBindBuffer(GLX.GL_ARRAY_BUFFER, 0);
    }

    public void delete() {
        if (this.id >= 0) {
            GLX.glDeleteBuffers(this.id);
            this.id = -1;
        }

    }
}
