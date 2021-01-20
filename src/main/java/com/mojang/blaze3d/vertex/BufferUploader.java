package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BufferUploader {
    private static int vertexBufferObject;
    private static int indexBufferObject;

    public static void end(BufferBuilder param0) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> {
                Pair<BufferBuilder.DrawState, ByteBuffer> var0x = param0.popNextBuffer();
                BufferBuilder.DrawState var1x = var0x.getFirst();
                _end(var0x.getSecond(), var1x.mode(), var1x.format(), var1x.vertexCount(), var1x.indexType(), var1x.indexCount(), var1x.sequentialIndex());
            });
        } else {
            Pair<BufferBuilder.DrawState, ByteBuffer> var0 = param0.popNextBuffer();
            BufferBuilder.DrawState var1 = var0.getFirst();
            _end(var0.getSecond(), var1.mode(), var1.format(), var1.vertexCount(), var1.indexType(), var1.indexCount(), var1.sequentialIndex());
        }

    }

    private static void _end(
        ByteBuffer param0, VertexFormat.Mode param1, VertexFormat param2, int param3, VertexFormat.IndexType param4, int param5, boolean param6
    ) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        ((Buffer)param0).clear();
        if (param3 > 0) {
            if (vertexBufferObject == 0) {
                vertexBufferObject = GlStateManager._glGenBuffers();
            }

            int var0 = param3 * param2.getVertexSize();
            GlStateManager._glBindBuffer(34962, vertexBufferObject);
            ((Buffer)param0).position(0);
            ((Buffer)param0).limit(var0);
            GlStateManager._glBufferData(34962, param0, 35044);
            int var2;
            if (param6) {
                RenderSystem.AutoStorageIndexBuffer var1 = RenderSystem.getSequentialBuffer(param1, param5);
                GlStateManager._glBindBuffer(34963, var1.name());
                var2 = var1.type().asGLType;
            } else {
                if (indexBufferObject == 0) {
                    indexBufferObject = GlStateManager._glGenBuffers();
                }

                GlStateManager._glBindBuffer(34963, indexBufferObject);
                ((Buffer)param0).position(var0);
                ((Buffer)param0).limit(var0 + param5 * param4.bytes);
                GlStateManager._glBufferData(34963, param0, 35044);
                var2 = param4.asGLType;
            }

            param2.setupBufferState(0L);
            GlStateManager._drawElements(param1.asGLMode, param5, var2, 0L);
            param2.clearBufferState();
            ((Buffer)param0).position(0);
            GlStateManager._glBindBuffer(34963, 0);
            GlStateManager._glBindBuffer(34962, 0);
        }
    }
}
