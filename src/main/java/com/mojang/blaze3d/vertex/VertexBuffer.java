package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class VertexBuffer implements AutoCloseable {
    private int vertexBufferId;
    private int indexBufferId;
    private int arrayObjectId;
    @Nullable
    private VertexFormat format;
    @Nullable
    private RenderSystem.AutoStorageIndexBuffer sequentialIndices;
    private VertexFormat.IndexType indexType;
    private int indexCount;
    private VertexFormat.Mode mode;

    public VertexBuffer() {
        RenderSystem.assertOnRenderThread();
        this.vertexBufferId = GlStateManager._glGenBuffers();
        this.indexBufferId = GlStateManager._glGenBuffers();
        this.arrayObjectId = GlStateManager._glGenVertexArrays();
    }

    public void upload(BufferBuilder.RenderedBuffer param0) {
        if (!this.isInvalid()) {
            RenderSystem.assertOnRenderThread();

            try {
                BufferBuilder.DrawState var0 = param0.drawState();
                this.format = this.uploadVertexBuffer(var0, param0.vertexBuffer());
                this.sequentialIndices = this.uploadIndexBuffer(var0, param0.indexBuffer());
                this.indexCount = var0.indexCount();
                this.indexType = var0.indexType();
                this.mode = var0.mode();
            } finally {
                param0.release();
            }

        }
    }

    private VertexFormat uploadVertexBuffer(BufferBuilder.DrawState param0, ByteBuffer param1) {
        boolean var0 = false;
        if (!param0.format().equals(this.format)) {
            if (this.format != null) {
                this.format.clearBufferState();
            }

            GlStateManager._glBindBuffer(34962, this.vertexBufferId);
            param0.format().setupBufferState();
            var0 = true;
        }

        if (!param0.indexOnly()) {
            if (!var0) {
                GlStateManager._glBindBuffer(34962, this.vertexBufferId);
            }

            RenderSystem.glBufferData(34962, param1, 35044);
        }

        return param0.format();
    }

    @Nullable
    private RenderSystem.AutoStorageIndexBuffer uploadIndexBuffer(BufferBuilder.DrawState param0, ByteBuffer param1) {
        if (!param0.sequentialIndex()) {
            GlStateManager._glBindBuffer(34963, this.indexBufferId);
            RenderSystem.glBufferData(34963, param1, 35044);
            return null;
        } else {
            RenderSystem.AutoStorageIndexBuffer var0 = RenderSystem.getSequentialBuffer(param0.mode());
            if (var0 != this.sequentialIndices || !var0.hasStorage(param0.indexCount())) {
                var0.bind(param0.indexCount());
            }

            return var0;
        }
    }

    public void bind() {
        BufferUploader.invalidate();
        GlStateManager._glBindVertexArray(this.arrayObjectId);
    }

    public static void unbind() {
        BufferUploader.invalidate();
        GlStateManager._glBindVertexArray(0);
    }

    public void draw() {
        RenderSystem.drawElements(this.mode.asGLMode, this.indexCount, this.getIndexType().asGLType);
    }

    private VertexFormat.IndexType getIndexType() {
        RenderSystem.AutoStorageIndexBuffer var0 = this.sequentialIndices;
        return var0 != null ? var0.type() : this.indexType;
    }

    public void drawWithShader(Matrix4f param0, Matrix4f param1, ShaderInstance param2) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this._drawWithShader(new Matrix4f(param0), new Matrix4f(param1), param2));
        } else {
            this._drawWithShader(param0, param1, param2);
        }

    }

    private void _drawWithShader(Matrix4f param0, Matrix4f param1, ShaderInstance param2) {
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

        if (param2.INVERSE_VIEW_ROTATION_MATRIX != null) {
            param2.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
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

        if (param2.FOG_SHAPE != null) {
            param2.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
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
        param2.apply();
        this.draw();
        param2.clear();
    }

    @Override
    public void close() {
        if (this.vertexBufferId >= 0) {
            RenderSystem.glDeleteBuffers(this.vertexBufferId);
            this.vertexBufferId = -1;
        }

        if (this.indexBufferId >= 0) {
            RenderSystem.glDeleteBuffers(this.indexBufferId);
            this.indexBufferId = -1;
        }

        if (this.arrayObjectId >= 0) {
            RenderSystem.glDeleteVertexArrays(this.arrayObjectId);
            this.arrayObjectId = -1;
        }

    }

    public VertexFormat getFormat() {
        return this.format;
    }

    public boolean isInvalid() {
        return this.arrayObjectId == -1;
    }
}
