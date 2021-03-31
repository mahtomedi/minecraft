package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderTarget {
    private static final int RED_CHANNEL = 0;
    private static final int GREEN_CHANNEL = 1;
    private static final int BLUE_CHANNEL = 2;
    private static final int ALPHA_CHANNEL = 3;
    public int width;
    public int height;
    public int viewWidth;
    public int viewHeight;
    public final boolean useDepth;
    public int frameBufferId;
    private int colorTextureId;
    private int depthBufferId;
    public final float[] clearChannels;
    public int filterMode;

    public RenderTarget(int param0, int param1, boolean param2, boolean param3) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        this.useDepth = param2;
        this.frameBufferId = -1;
        this.colorTextureId = -1;
        this.depthBufferId = -1;
        this.clearChannels = new float[4];
        this.clearChannels[0] = 1.0F;
        this.clearChannels[1] = 1.0F;
        this.clearChannels[2] = 1.0F;
        this.clearChannels[3] = 0.0F;
        this.resize(param0, param1, param3);
    }

    public void resize(int param0, int param1, boolean param2) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this._resize(param0, param1, param2));
        } else {
            this._resize(param0, param1, param2);
        }

    }

    private void _resize(int param0, int param1, boolean param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GlStateManager._enableDepthTest();
        if (this.frameBufferId >= 0) {
            this.destroyBuffers();
        }

        this.createBuffers(param0, param1, param2);
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    public void destroyBuffers() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        this.unbindRead();
        this.unbindWrite();
        if (this.depthBufferId > -1) {
            TextureUtil.releaseTextureId(this.depthBufferId);
            this.depthBufferId = -1;
        }

        if (this.colorTextureId > -1) {
            TextureUtil.releaseTextureId(this.colorTextureId);
            this.colorTextureId = -1;
        }

        if (this.frameBufferId > -1) {
            GlStateManager._glBindFramebuffer(36160, 0);
            GlStateManager._glDeleteFramebuffers(this.frameBufferId);
            this.frameBufferId = -1;
        }

    }

    public void copyDepthFrom(RenderTarget param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GlStateManager._glBindFramebuffer(36008, param0.frameBufferId);
        GlStateManager._glBindFramebuffer(36009, this.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, param0.width, param0.height, 0, 0, this.width, this.height, 256, 9728);
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    public void createBuffers(int param0, int param1, boolean param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        this.viewWidth = param0;
        this.viewHeight = param1;
        this.width = param0;
        this.height = param1;
        this.frameBufferId = GlStateManager.glGenFramebuffers();
        this.colorTextureId = TextureUtil.generateTextureId();
        if (this.useDepth) {
            this.depthBufferId = TextureUtil.generateTextureId();
            GlStateManager._bindTexture(this.depthBufferId);
            GlStateManager._texParameter(3553, 10241, 9728);
            GlStateManager._texParameter(3553, 10240, 9728);
            GlStateManager._texParameter(3553, 34892, 0);
            GlStateManager._texParameter(3553, 10242, 33071);
            GlStateManager._texParameter(3553, 10243, 33071);
            GlStateManager._texImage2D(3553, 0, 6402, this.width, this.height, 0, 6402, 5126, null);
        }

        this.setFilterMode(9728);
        GlStateManager._bindTexture(this.colorTextureId);
        GlStateManager._texParameter(3553, 10242, 33071);
        GlStateManager._texParameter(3553, 10243, 33071);
        GlStateManager._texImage2D(3553, 0, 32856, this.width, this.height, 0, 6408, 5121, null);
        GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
        GlStateManager._glFramebufferTexture2D(36160, 36064, 3553, this.colorTextureId, 0);
        if (this.useDepth) {
            GlStateManager._glFramebufferTexture2D(36160, 36096, 3553, this.depthBufferId, 0);
        }

        this.checkStatus();
        this.clear(param2);
        this.unbindRead();
    }

    public void setFilterMode(int param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        this.filterMode = param0;
        GlStateManager._bindTexture(this.colorTextureId);
        GlStateManager._texParameter(3553, 10241, param0);
        GlStateManager._texParameter(3553, 10240, param0);
        GlStateManager._bindTexture(0);
    }

    public void checkStatus() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        int var0 = GlStateManager.glCheckFramebufferStatus(36160);
        if (var0 != 36053) {
            if (var0 == 36054) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
            } else if (var0 == 36055) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
            } else if (var0 == 36059) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
            } else if (var0 == 36060) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
            } else {
                throw new RuntimeException("glCheckFramebufferStatus returned unknown status:" + var0);
            }
        }
    }

    public void bindRead() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager._bindTexture(this.colorTextureId);
    }

    public void unbindRead() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GlStateManager._bindTexture(0);
    }

    public void bindWrite(boolean param0) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this._bindWrite(param0));
        } else {
            this._bindWrite(param0);
        }

    }

    private void _bindWrite(boolean param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
        if (param0) {
            GlStateManager._viewport(0, 0, this.viewWidth, this.viewHeight);
        }

    }

    public void unbindWrite() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> GlStateManager._glBindFramebuffer(36160, 0));
        } else {
            GlStateManager._glBindFramebuffer(36160, 0);
        }

    }

    public void setClearColor(float param0, float param1, float param2, float param3) {
        this.clearChannels[0] = param0;
        this.clearChannels[1] = param1;
        this.clearChannels[2] = param2;
        this.clearChannels[3] = param3;
    }

    public void blitToScreen(int param0, int param1) {
        this.blitToScreen(param0, param1, true);
    }

    public void blitToScreen(int param0, int param1, boolean param2) {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        if (!RenderSystem.isInInitPhase()) {
            RenderSystem.recordRenderCall(() -> this._blitToScreen(param0, param1, param2));
        } else {
            this._blitToScreen(param0, param1, param2);
        }

    }

    private void _blitToScreen(int param0, int param1, boolean param2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager._colorMask(true, true, true, false);
        GlStateManager._disableDepthTest();
        GlStateManager._depthMask(false);
        GlStateManager._viewport(0, 0, param0, param1);
        if (param2) {
            GlStateManager._disableBlend();
        }

        Minecraft var0 = Minecraft.getInstance();
        ShaderInstance var1 = var0.gameRenderer.blitShader;
        var1.setSampler("DiffuseSampler", this.colorTextureId);
        Matrix4f var2 = Matrix4f.orthographic((float)param0, (float)(-param1), 1000.0F, 3000.0F);
        RenderSystem.setProjectionMatrix(var2);
        if (var1.MODEL_VIEW_MATRIX != null) {
            var1.MODEL_VIEW_MATRIX.set(Matrix4f.createTranslateMatrix(0.0F, 0.0F, -2000.0F));
        }

        if (var1.PROJECTION_MATRIX != null) {
            var1.PROJECTION_MATRIX.set(var2);
        }

        var1.apply();
        float var3 = (float)param0;
        float var4 = (float)param1;
        float var5 = (float)this.viewWidth / (float)this.width;
        float var6 = (float)this.viewHeight / (float)this.height;
        Tesselator var7 = RenderSystem.renderThreadTesselator();
        BufferBuilder var8 = var7.getBuilder();
        var8.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        var8.vertex(0.0, (double)var4, 0.0).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
        var8.vertex((double)var3, (double)var4, 0.0).uv(var5, 0.0F).color(255, 255, 255, 255).endVertex();
        var8.vertex((double)var3, 0.0, 0.0).uv(var5, var6).color(255, 255, 255, 255).endVertex();
        var8.vertex(0.0, 0.0, 0.0).uv(0.0F, var6).color(255, 255, 255, 255).endVertex();
        var8.end();
        BufferUploader._endInternal(var8);
        var1.clear();
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(true, true, true, true);
    }

    public void clear(boolean param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        this.bindWrite(true);
        GlStateManager._clearColor(this.clearChannels[0], this.clearChannels[1], this.clearChannels[2], this.clearChannels[3]);
        int var0 = 16384;
        if (this.useDepth) {
            GlStateManager._clearDepth(1.0);
            var0 |= 256;
        }

        GlStateManager._clear(var0, param0);
        this.unbindWrite();
    }

    public int getColorTextureId() {
        return this.colorTextureId;
    }

    public int getDepthTextureId() {
        return this.depthBufferId;
    }
}
