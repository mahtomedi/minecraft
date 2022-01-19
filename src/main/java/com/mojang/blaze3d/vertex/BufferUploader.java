package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BufferUploader {
    private static int lastVertexArrayObject;
    private static int lastVertexBufferObject;
    private static int lastIndexBufferObject;
    @Nullable
    private static VertexFormat lastFormat;

    public static void reset() {
        if (lastFormat != null) {
            lastFormat.clearBufferState();
            lastFormat = null;
        }

        GlStateManager._glBindBuffer(34963, 0);
        lastIndexBufferObject = 0;
        GlStateManager._glBindBuffer(34962, 0);
        lastVertexBufferObject = 0;
        GlStateManager._glBindVertexArray(0);
        lastVertexArrayObject = 0;
    }

    public static void invalidateElementArrayBufferBinding() {
        GlStateManager._glBindBuffer(34963, 0);
        lastIndexBufferObject = 0;
    }

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
        RenderSystem.assertOnRenderThread();
        param0.clear();
        if (param3 > 0) {
            int var0 = param3 * param2.getVertexSize();
            updateVertexSetup(param2);
            param0.position(0);
            param0.limit(var0);
            GlStateManager._glBufferData(34962, param0, 35048);
            int var3;
            if (param6) {
                RenderSystem.AutoStorageIndexBuffer var1 = RenderSystem.getSequentialBuffer(param1, param5);
                int var2 = var1.name();
                if (var2 != lastIndexBufferObject) {
                    GlStateManager._glBindBuffer(34963, var2);
                    lastIndexBufferObject = var2;
                }

                var3 = var1.type().asGLType;
            } else {
                int var4 = param2.getOrCreateIndexBufferObject();
                if (var4 != lastIndexBufferObject) {
                    GlStateManager._glBindBuffer(34963, var4);
                    lastIndexBufferObject = var4;
                }

                param0.position(var0);
                param0.limit(var0 + param5 * param4.bytes);
                GlStateManager._glBufferData(34963, param0, 35048);
                var3 = param4.asGLType;
            }

            ShaderInstance var6 = RenderSystem.getShader();

            for(int var7 = 0; var7 < 8; ++var7) {
                int var8 = RenderSystem.getShaderTexture(var7);
                var6.setSampler("Sampler" + var7, var8);
            }

            if (var6.MODEL_VIEW_MATRIX != null) {
                var6.MODEL_VIEW_MATRIX.set(RenderSystem.getModelViewMatrix());
            }

            if (var6.PROJECTION_MATRIX != null) {
                var6.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
            }

            if (var6.INVERSE_VIEW_ROTATION_MATRIX != null) {
                var6.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
            }

            if (var6.COLOR_MODULATOR != null) {
                var6.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
            }

            if (var6.FOG_START != null) {
                var6.FOG_START.set(RenderSystem.getShaderFogStart());
            }

            if (var6.FOG_END != null) {
                var6.FOG_END.set(RenderSystem.getShaderFogEnd());
            }

            if (var6.FOG_COLOR != null) {
                var6.FOG_COLOR.set(RenderSystem.getShaderFogColor());
            }

            if (var6.FOG_SHAPE != null) {
                var6.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
            }

            if (var6.TEXTURE_MATRIX != null) {
                var6.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
            }

            if (var6.GAME_TIME != null) {
                var6.GAME_TIME.set(RenderSystem.getShaderGameTime());
            }

            if (var6.SCREEN_SIZE != null) {
                Window var9 = Minecraft.getInstance().getWindow();
                var6.SCREEN_SIZE.set((float)var9.getWidth(), (float)var9.getHeight());
            }

            if (var6.LINE_WIDTH != null && (param1 == VertexFormat.Mode.LINES || param1 == VertexFormat.Mode.LINE_STRIP)) {
                var6.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
            }

            RenderSystem.setupShaderLights(var6);
            var6.apply();
            GlStateManager._drawElements(param1.asGLMode, param5, var3, 0L);
            var6.clear();
            param0.position(0);
        }
    }

    public static void _endInternal(BufferBuilder param0) {
        RenderSystem.assertOnRenderThread();
        Pair<BufferBuilder.DrawState, ByteBuffer> var0 = param0.popNextBuffer();
        BufferBuilder.DrawState var1 = var0.getFirst();
        ByteBuffer var2 = var0.getSecond();
        VertexFormat var3 = var1.format();
        int var4 = var1.vertexCount();
        var2.clear();
        if (var4 > 0) {
            int var5 = var4 * var3.getVertexSize();
            updateVertexSetup(var3);
            var2.position(0);
            var2.limit(var5);
            GlStateManager._glBufferData(34962, var2, 35048);
            RenderSystem.AutoStorageIndexBuffer var6 = RenderSystem.getSequentialBuffer(var1.mode(), var1.indexCount());
            int var7 = var6.name();
            if (var7 != lastIndexBufferObject) {
                GlStateManager._glBindBuffer(34963, var7);
                lastIndexBufferObject = var7;
            }

            int var8 = var6.type().asGLType;
            GlStateManager._drawElements(var1.mode().asGLMode, var1.indexCount(), var8, 0L);
            var2.position(0);
        }
    }

    private static void updateVertexSetup(VertexFormat param0) {
        int var0 = param0.getOrCreateVertexArrayObject();
        int var1 = param0.getOrCreateVertexBufferObject();
        boolean var2 = param0 != lastFormat;
        if (var2) {
            reset();
        }

        if (var0 != lastVertexArrayObject) {
            GlStateManager._glBindVertexArray(var0);
            lastVertexArrayObject = var0;
        }

        if (var1 != lastVertexBufferObject) {
            GlStateManager._glBindBuffer(34962, var1);
            lastVertexBufferObject = var1;
        }

        if (var2) {
            param0.setupBufferState();
            lastFormat = param0;
        }

    }
}
