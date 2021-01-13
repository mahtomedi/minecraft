package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public class BufferUploader {
    public static void end(BufferBuilder param0) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                Pair<BufferBuilder.DrawState, ByteBuffer> var0x = param0.popNextBuffer();
                BufferBuilder.DrawState var1x = var0x.getFirst();
                _end(var0x.getSecond(), var1x.mode(), var1x.format(), var1x.vertexCount());
            });
        } else {
            Pair<BufferBuilder.DrawState, ByteBuffer> var0 = param0.popNextBuffer();
            BufferBuilder.DrawState var1 = var0.getFirst();
            _end(var0.getSecond(), var1.mode(), var1.format(), var1.vertexCount());
        }

    }

    private static void _end(ByteBuffer param0, int param1, VertexFormat param2, int param3) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        ((Buffer)param0).clear();
        if (param3 > 0) {
            param2.setupBufferState(MemoryUtil.memAddress(param0));
            GlStateManager._drawArrays(param1, 0, param3);
            param2.clearBufferState();
        }
    }
}
