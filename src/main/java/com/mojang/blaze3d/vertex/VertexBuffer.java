package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VertexBuffer implements AutoCloseable {
    private int id;
    private int indexBufferId;
    private VertexFormat.IndexType indexType;
    private int indexCount;
    private VertexFormat.Mode mode;
    private boolean sequentialIndices;

    public VertexBuffer() {
        RenderSystem.glGenBuffers(param0 -> this.id = param0);
        RenderSystem.glGenBuffers(param0 -> this.indexBufferId = param0);
    }

    public void bind() {
        RenderSystem.glBindBuffer(34962, () -> this.id);
        if (this.sequentialIndices) {
            RenderSystem.glBindBuffer(34963, () -> {
                RenderSystem.AutoStorageIndexBuffer var0 = RenderSystem.getSequentialBuffer(this.mode, this.indexCount);
                this.indexType = var0.type();
                return var0.name();
            });
        } else {
            RenderSystem.glBindBuffer(34963, () -> this.indexBufferId);
        }

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
        if (this.id != -1) {
            BufferBuilder.DrawState var1 = var0.getFirst();
            ByteBuffer var2 = var0.getSecond();
            int var3 = var1.vertexBufferSize();
            this.indexCount = var1.indexCount();
            this.indexType = var1.indexType();
            this.mode = var1.mode();
            this.sequentialIndices = var1.sequentialIndex();
            this.bind();
            if (!var1.indexOnly()) {
                ((Buffer)var2).limit(var3);
                RenderSystem.glBufferData(34962, var2, 35044);
                ((Buffer)var2).position(var3);
            }

            if (!this.sequentialIndices) {
                ((Buffer)var2).limit(var1.bufferSize());
                RenderSystem.glBufferData(34963, var2, 35044);
                ((Buffer)var2).position(0);
            } else {
                ((Buffer)var2).limit(var1.bufferSize());
                ((Buffer)var2).position(0);
            }

            unbind();
        }
    }

    public void draw(Matrix4f param0) {
        if (this.indexCount != 0) {
            RenderSystem.pushMatrix();
            RenderSystem.loadIdentity();
            RenderSystem.multMatrix(param0);
            RenderSystem.drawElements(this.mode.asGLMode, this.indexCount, this.indexType.asGLType);
            RenderSystem.popMatrix();
        }
    }

    public static void unbind() {
        RenderSystem.glBindBuffer(34962, () -> 0);
        RenderSystem.glBindBuffer(34963, () -> 0);
    }

    @Override
    public void close() {
        if (this.id >= 0) {
            RenderSystem.glDeleteBuffers(this.id);
            this.id = -1;
        }

        if (this.indexBufferId >= 0) {
            RenderSystem.glDeleteBuffers(this.indexBufferId);
            this.indexBufferId = -1;
        }

    }
}
