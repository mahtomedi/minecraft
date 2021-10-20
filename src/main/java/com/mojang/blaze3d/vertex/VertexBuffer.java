package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VertexBuffer implements AutoCloseable {
    private int vertextBufferId;
    private int indexBufferId;
    private VertexFormat.IndexType indexType;
    private int arrayObjectId;
    private int indexCount;
    private VertexFormat.Mode mode;
    private boolean sequentialIndices;
    private VertexFormat format;

    public VertexBuffer() {
        RenderSystem.glGenBuffers(param0 -> this.vertextBufferId = param0);
        RenderSystem.glGenVertexArrays(param0 -> this.arrayObjectId = param0);
        RenderSystem.glGenBuffers(param0 -> this.indexBufferId = param0);
    }

    public void bind() {
        RenderSystem.glBindBuffer(34962, () -> this.vertextBufferId);
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
        if (this.vertextBufferId != 0) {
            BufferUploader.reset();
            BufferBuilder.DrawState var1 = var0.getFirst();
            ByteBuffer var2 = var0.getSecond();
            int var3 = var1.vertexBufferSize();
            this.indexCount = var1.indexCount();
            this.indexType = var1.indexType();
            this.format = var1.format();
            this.mode = var1.mode();
            this.sequentialIndices = var1.sequentialIndex();
            this.bindVertexArray();
            this.bind();
            if (!var1.indexOnly()) {
                var2.limit(var3);
                RenderSystem.glBufferData(34962, var2, 35044);
                var2.position(var3);
            }

            if (!this.sequentialIndices) {
                var2.limit(var1.bufferSize());
                RenderSystem.glBufferData(34963, var2, 35044);
                var2.position(0);
            } else {
                var2.limit(var1.bufferSize());
                var2.position(0);
            }

            unbind();
            unbindVertexArray();
        }
    }

    private void bindVertexArray() {
        RenderSystem.glBindVertexArray(() -> this.arrayObjectId);
    }

    public static void unbindVertexArray() {
        RenderSystem.glBindVertexArray(() -> 0);
    }

    public void draw() {
        if (this.indexCount != 0) {
            RenderSystem.drawElements(this.mode.asGLMode, this.indexCount, this.indexType.asGLType);
        }
    }

    public void drawWithShader(Matrix4f param0, Matrix4f param1, ShaderInstance param2) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this._drawWithShader(param0.copy(), param1.copy(), param2));
        } else {
            this._drawWithShader(param0, param1, param2);
        }

    }

    public void _drawWithShader(Matrix4f param0, Matrix4f param1, ShaderInstance param2) {
        if (this.indexCount != 0) {
            RenderSystem.assertOnRenderThread();
            BufferUploader.reset();

            for(int var0 = 0; var0 < 12; ++var0) {
                int var1 = RenderSystem.getShaderTexture(var0);
                param2.setSampler("Sampler" + var0, var1);
            }

            if (param2.MODEL_VIEW_MATRIX != null) {
                param2.MODEL_VIEW_MATRIX.set(param0);
            }

            if (param2.PROJECTION_MATRIX != null) {
                param2.PROJECTION_MATRIX.set(param1);
            }

            if (param2.COLOR_MODULATOR != null) {
                param2.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
            }

            if (param2.FOG_START != null) {
                param2.FOG_START.set(RenderSystem.getShaderFogStart());
            }

            if (param2.FOG_END != null) {
                param2.FOG_END.set(RenderSystem.getShaderFogEnd());
            }

            if (param2.FOG_COLOR != null) {
                param2.FOG_COLOR.set(RenderSystem.getShaderFogColor());
            }

            if (param2.TEXTURE_MATRIX != null) {
                param2.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
            }

            if (param2.GAME_TIME != null) {
                param2.GAME_TIME.set(RenderSystem.getShaderGameTime());
            }

            if (param2.SCREEN_SIZE != null) {
                Window var2 = Minecraft.getInstance().getWindow();
                param2.SCREEN_SIZE.set((float)var2.getWidth(), (float)var2.getHeight());
            }

            if (param2.LINE_WIDTH != null && (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP)) {
                param2.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
            }

            RenderSystem.setupShaderLights(param2);
            this.bindVertexArray();
            this.bind();
            this.getFormat().setupBufferState();
            param2.apply();
            RenderSystem.drawElements(this.mode.asGLMode, this.indexCount, this.indexType.asGLType);
            param2.clear();
            this.getFormat().clearBufferState();
            unbind();
            unbindVertexArray();
        }
    }

    public void drawChunkLayer() {
        if (this.indexCount != 0) {
            RenderSystem.assertOnRenderThread();
            this.bindVertexArray();
            this.bind();
            this.format.setupBufferState();
            RenderSystem.drawElements(this.mode.asGLMode, this.indexCount, this.indexType.asGLType);
        }
    }

    public static void unbind() {
        RenderSystem.glBindBuffer(34962, () -> 0);
        RenderSystem.glBindBuffer(34963, () -> 0);
    }

    @Override
    public void close() {
        if (this.indexBufferId >= 0) {
            RenderSystem.glDeleteBuffers(this.indexBufferId);
            this.indexBufferId = -1;
        }

        if (this.vertextBufferId > 0) {
            RenderSystem.glDeleteBuffers(this.vertextBufferId);
            this.vertextBufferId = 0;
        }

        if (this.arrayObjectId > 0) {
            RenderSystem.glDeleteVertexArrays(this.arrayObjectId);
            this.arrayObjectId = 0;
        }

    }

    public VertexFormat getFormat() {
        return this.format;
    }
}
