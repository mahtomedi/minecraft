package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.nio.ByteBuffer;
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

    public static void drawWithShader(BufferBuilder param0) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> _drawWithShader(param0));
        } else {
            _drawWithShader(param0);
        }

    }

    private static void _drawWithShader(BufferBuilder param0) {
        VertexBuffer var0 = upload(param0);
        if (var0 != null) {
            var0.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
        }

    }

    public static void draw(BufferBuilder param0) {
        VertexBuffer var0 = upload(param0);
        if (var0 != null) {
            var0.draw();
        }

    }

    @Nullable
    private static VertexBuffer upload(BufferBuilder param0) {
        RenderSystem.assertOnRenderThread();
        Pair<BufferBuilder.DrawState, ByteBuffer> var0 = param0.popNextBuffer();
        BufferBuilder.DrawState var1 = var0.getFirst();
        ByteBuffer var2 = var0.getSecond();
        var2.clear();
        if (var1.vertexCount() <= 0) {
            return null;
        } else {
            VertexBuffer var3 = bindImmediateBuffer(var1.format());
            var3.upload(var1, var2);
            return var3;
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
