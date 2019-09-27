package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VertexBuffer {
    private int id;
    private final VertexFormat format;
    private int vertexCount;

    public VertexBuffer(VertexFormat param0) {
        this.format = param0;
        RenderSystem.glGenBuffers(param0x -> this.id = param0x);
    }

    public void bind() {
        RenderSystem.glBindBuffer(34962, () -> this.id);
    }

    public void upload(BufferBuilder param0) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this.upload_(param0));
        } else {
            this.upload_(param0);
        }

    }

    public CompletableFuture<Void> uploadLater(BufferBuilder param0) {
        if (!RenderSystem.isOnRenderThread()) {
            return CompletableFuture.runAsync(() -> this.upload_(param0), param0x -> RenderSystem.recordRenderCall(param0x::run));
        } else {
            this.upload_(param0);
            return CompletableFuture.completedFuture(null);
        }
    }

    private void upload_(BufferBuilder param0) {
        Pair<BufferBuilder.DrawState, ByteBuffer> var0 = param0.popNextBuffer();
        ByteBuffer var1 = var0.getSecond();
        this.vertexCount = var1.remaining() / this.format.getVertexSize();
        this.bind();
        RenderSystem.glBufferData(34962, var1, 35044);
        unbind();
    }

    public void draw(Matrix4f param0, int param1) {
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.multMatrix(param0);
        RenderSystem.drawArrays(param1, 0, this.vertexCount);
        RenderSystem.popMatrix();
    }

    public static void unbind() {
        RenderSystem.glBindBuffer(34962, () -> 0);
    }

    public void delete() {
        if (this.id >= 0) {
            RenderSystem.glDeleteBuffers(this.id);
            this.id = -1;
        }

    }
}
