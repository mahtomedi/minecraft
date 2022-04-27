package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BufferUploader {
    @Nullable
    private static VertexBuffer lastImmediateBuffer;

    public static void reset() {
        if (lastImmediateBuffer != null) {
            invalidate();
            VertexBuffer.unbind();
        }

    }

    public static void invalidate() {
        lastImmediateBuffer = null;
    }

    public static void drawWithShader(BufferBuilder.RenderedBuffer param0) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> _drawWithShader(param0));
        } else {
            _drawWithShader(param0);
        }

    }

    private static void _drawWithShader(BufferBuilder.RenderedBuffer param0) {
        VertexBuffer var0 = upload(param0);
        if (var0 != null) {
            var0.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
        }

    }

    public static void draw(BufferBuilder.RenderedBuffer param0) {
        VertexBuffer var0 = upload(param0);
        if (var0 != null) {
            var0.draw();
        }

    }

    @Nullable
    private static VertexBuffer upload(BufferBuilder.RenderedBuffer param0) {
        RenderSystem.assertOnRenderThread();
        if (param0.isEmpty()) {
            param0.release();
            return null;
        } else {
            VertexBuffer var0 = bindImmediateBuffer(param0.drawState().format());
            var0.upload(param0);
            return var0;
        }
    }

    private static VertexBuffer bindImmediateBuffer(VertexFormat param0) {
        VertexBuffer var0 = param0.getImmediateDrawVertexBuffer();
        bindImmediateBuffer(var0);
        return var0;
    }

    private static void bindImmediateBuffer(VertexBuffer param0) {
        if (param0 != lastImmediateBuffer) {
            param0.bind();
            lastImmediateBuffer = param0;
        }

    }
}
