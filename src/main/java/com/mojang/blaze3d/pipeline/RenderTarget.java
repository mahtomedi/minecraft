package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderTarget {
    public int width;
    public int height;
    public int viewWidth;
    public int viewHeight;
    public final boolean useDepth;
    public int frameBufferId;
    public int colorTextureId;
    public int depthBufferId;
    public final float[] clearChannels;
    public int filterMode;

    public RenderTarget(int param0, int param1, boolean param2, boolean param3) {
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
        RenderSystem.enableDepthTest();
        if (this.frameBufferId >= 0) {
            this.destroyBuffers();
        }

        this.createBuffers(param0, param1, param2);
        GlStateManager.glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);
    }

    public void destroyBuffers() {
        this.unbindRead();
        this.unbindWrite();
        if (this.depthBufferId > -1) {
            GlStateManager.glDeleteRenderbuffers(this.depthBufferId);
            this.depthBufferId = -1;
        }

        if (this.colorTextureId > -1) {
            TextureUtil.releaseTextureId(this.colorTextureId);
            this.colorTextureId = -1;
        }

        if (this.frameBufferId > -1) {
            GlStateManager.glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);
            GlStateManager.glDeleteFramebuffers(this.frameBufferId);
            this.frameBufferId = -1;
        }

    }

    public void createBuffers(int param0, int param1, boolean param2) {
        this.viewWidth = param0;
        this.viewHeight = param1;
        this.width = param0;
        this.height = param1;
        this.frameBufferId = GlStateManager.glGenFramebuffers();
        this.colorTextureId = TextureUtil.generateTextureId();
        if (this.useDepth) {
            this.depthBufferId = GlStateManager.glGenRenderbuffers();
        }

        this.setFilterMode(9728);
        RenderSystem.bindTexture(this.colorTextureId);
        RenderSystem.texImage2D(3553, 0, 32856, this.width, this.height, 0, 6408, 5121, null);
        GlStateManager.glBindFramebuffer(GlConst.GL_FRAMEBUFFER, this.frameBufferId);
        GlStateManager.glFramebufferTexture2D(GlConst.GL_FRAMEBUFFER, GlConst.GL_COLOR_ATTACHMENT0, 3553, this.colorTextureId, 0);
        if (this.useDepth) {
            GlStateManager.glBindRenderbuffer(GlConst.GL_RENDERBUFFER, this.depthBufferId);
            GlStateManager.glRenderbufferStorage(GlConst.GL_RENDERBUFFER, 33190, this.width, this.height);
            GlStateManager.glFramebufferRenderbuffer(GlConst.GL_FRAMEBUFFER, GlConst.GL_DEPTH_ATTACHMENT, GlConst.GL_RENDERBUFFER, this.depthBufferId);
        }

        this.checkStatus();
        this.clear(param2);
        this.unbindRead();
    }

    public void setFilterMode(int param0) {
        this.filterMode = param0;
        RenderSystem.bindTexture(this.colorTextureId);
        RenderSystem.texParameter(3553, 10241, param0);
        RenderSystem.texParameter(3553, 10240, param0);
        RenderSystem.texParameter(3553, 10242, 10496);
        RenderSystem.texParameter(3553, 10243, 10496);
        RenderSystem.bindTexture(0);
    }

    public void checkStatus() {
        int var0 = GlStateManager.glCheckFramebufferStatus(GlConst.GL_FRAMEBUFFER);
        if (var0 != GlConst.GL_FRAMEBUFFER_COMPLETE) {
            if (var0 == GlConst.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
            } else if (var0 == GlConst.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
            } else if (var0 == GlConst.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
            } else if (var0 == GlConst.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
            } else {
                throw new RuntimeException("glCheckFramebufferStatus returned unknown status:" + var0);
            }
        }
    }

    public void bindRead() {
        RenderSystem.bindTexture(this.colorTextureId);
    }

    public void unbindRead() {
        RenderSystem.bindTexture(0);
    }

    public void bindWrite(boolean param0) {
        GlStateManager.glBindFramebuffer(GlConst.GL_FRAMEBUFFER, this.frameBufferId);
        if (param0) {
            RenderSystem.viewport(0, 0, this.viewWidth, this.viewHeight);
        }

    }

    public void unbindWrite() {
        GlStateManager.glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);
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
        RenderSystem.colorMask(true, true, true, false);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.matrixMode(5889);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0.0, (double)param0, (double)param1, 0.0, 1000.0, 3000.0);
        RenderSystem.matrixMode(5888);
        RenderSystem.loadIdentity();
        RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
        RenderSystem.viewport(0, 0, param0, param1);
        RenderSystem.enableTexture();
        RenderSystem.disableLighting();
        RenderSystem.disableAlphaTest();
        if (param2) {
            RenderSystem.disableBlend();
            RenderSystem.enableColorMaterial();
        }

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.bindRead();
        float var0 = (float)param0;
        float var1 = (float)param1;
        float var2 = (float)this.viewWidth / (float)this.width;
        float var3 = (float)this.viewHeight / (float)this.height;
        Tesselator var4 = Tesselator.getInstance();
        BufferBuilder var5 = var4.getBuilder();
        var5.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
        var5.vertex(0.0, (double)var1, 0.0).uv(0.0, 0.0).color(255, 255, 255, 255).endVertex();
        var5.vertex((double)var0, (double)var1, 0.0).uv((double)var2, 0.0).color(255, 255, 255, 255).endVertex();
        var5.vertex((double)var0, 0.0, 0.0).uv((double)var2, (double)var3).color(255, 255, 255, 255).endVertex();
        var5.vertex(0.0, 0.0, 0.0).uv(0.0, (double)var3).color(255, 255, 255, 255).endVertex();
        var4.end();
        this.unbindRead();
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);
    }

    public void clear(boolean param0) {
        this.bindWrite(true);
        RenderSystem.clearColor(this.clearChannels[0], this.clearChannels[1], this.clearChannels[2], this.clearChannels[3]);
        int var0 = 16384;
        if (this.useDepth) {
            RenderSystem.clearDepth(1.0);
            var0 |= 256;
        }

        RenderSystem.clear(var0, param0);
        this.unbindWrite();
    }
}
