package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
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
        if (!GLX.isUsingFBOs()) {
            this.viewWidth = param0;
            this.viewHeight = param1;
        } else {
            GlStateManager.enableDepthTest();
            if (this.frameBufferId >= 0) {
                this.destroyBuffers();
            }

            this.createBuffers(param0, param1, param2);
            GLX.glBindFramebuffer(GLX.GL_FRAMEBUFFER, 0);
        }
    }

    public void destroyBuffers() {
        if (GLX.isUsingFBOs()) {
            this.unbindRead();
            this.unbindWrite();
            if (this.depthBufferId > -1) {
                GLX.glDeleteRenderbuffers(this.depthBufferId);
                this.depthBufferId = -1;
            }

            if (this.colorTextureId > -1) {
                TextureUtil.releaseTextureId(this.colorTextureId);
                this.colorTextureId = -1;
            }

            if (this.frameBufferId > -1) {
                GLX.glBindFramebuffer(GLX.GL_FRAMEBUFFER, 0);
                GLX.glDeleteFramebuffers(this.frameBufferId);
                this.frameBufferId = -1;
            }

        }
    }

    public void createBuffers(int param0, int param1, boolean param2) {
        this.viewWidth = param0;
        this.viewHeight = param1;
        this.width = param0;
        this.height = param1;
        if (!GLX.isUsingFBOs()) {
            this.clear(param2);
        } else {
            this.frameBufferId = GLX.glGenFramebuffers();
            this.colorTextureId = TextureUtil.generateTextureId();
            if (this.useDepth) {
                this.depthBufferId = GLX.glGenRenderbuffers();
            }

            this.setFilterMode(9728);
            GlStateManager.bindTexture(this.colorTextureId);
            GlStateManager.texImage2D(3553, 0, 32856, this.width, this.height, 0, 6408, 5121, null);
            GLX.glBindFramebuffer(GLX.GL_FRAMEBUFFER, this.frameBufferId);
            GLX.glFramebufferTexture2D(GLX.GL_FRAMEBUFFER, GLX.GL_COLOR_ATTACHMENT0, 3553, this.colorTextureId, 0);
            if (this.useDepth) {
                GLX.glBindRenderbuffer(GLX.GL_RENDERBUFFER, this.depthBufferId);
                GLX.glRenderbufferStorage(GLX.GL_RENDERBUFFER, 33190, this.width, this.height);
                GLX.glFramebufferRenderbuffer(GLX.GL_FRAMEBUFFER, GLX.GL_DEPTH_ATTACHMENT, GLX.GL_RENDERBUFFER, this.depthBufferId);
            }

            this.checkStatus();
            this.clear(param2);
            this.unbindRead();
        }
    }

    public void setFilterMode(int param0) {
        if (GLX.isUsingFBOs()) {
            this.filterMode = param0;
            GlStateManager.bindTexture(this.colorTextureId);
            GlStateManager.texParameter(3553, 10241, param0);
            GlStateManager.texParameter(3553, 10240, param0);
            GlStateManager.texParameter(3553, 10242, 10496);
            GlStateManager.texParameter(3553, 10243, 10496);
            GlStateManager.bindTexture(0);
        }

    }

    public void checkStatus() {
        int var0 = GLX.glCheckFramebufferStatus(GLX.GL_FRAMEBUFFER);
        if (var0 != GLX.GL_FRAMEBUFFER_COMPLETE) {
            if (var0 == GLX.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
            } else if (var0 == GLX.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
            } else if (var0 == GLX.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
            } else if (var0 == GLX.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
            } else {
                throw new RuntimeException("glCheckFramebufferStatus returned unknown status:" + var0);
            }
        }
    }

    public void bindRead() {
        if (GLX.isUsingFBOs()) {
            GlStateManager.bindTexture(this.colorTextureId);
        }

    }

    public void unbindRead() {
        if (GLX.isUsingFBOs()) {
            GlStateManager.bindTexture(0);
        }

    }

    public void bindWrite(boolean param0) {
        if (GLX.isUsingFBOs()) {
            GLX.glBindFramebuffer(GLX.GL_FRAMEBUFFER, this.frameBufferId);
            if (param0) {
                GlStateManager.viewport(0, 0, this.viewWidth, this.viewHeight);
            }
        }

    }

    public void unbindWrite() {
        if (GLX.isUsingFBOs()) {
            GLX.glBindFramebuffer(GLX.GL_FRAMEBUFFER, 0);
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
        if (GLX.isUsingFBOs()) {
            GlStateManager.colorMask(true, true, true, false);
            GlStateManager.disableDepthTest();
            GlStateManager.depthMask(false);
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0, (double)param0, (double)param1, 0.0, 1000.0, 3000.0);
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();
            GlStateManager.translatef(0.0F, 0.0F, -2000.0F);
            GlStateManager.viewport(0, 0, param0, param1);
            GlStateManager.enableTexture();
            GlStateManager.disableLighting();
            GlStateManager.disableAlphaTest();
            if (param2) {
                GlStateManager.disableBlend();
                GlStateManager.enableColorMaterial();
            }

            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
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
            GlStateManager.depthMask(true);
            GlStateManager.colorMask(true, true, true, true);
        }
    }

    public void clear(boolean param0) {
        this.bindWrite(true);
        GlStateManager.clearColor(this.clearChannels[0], this.clearChannels[1], this.clearChannels[2], this.clearChannels[3]);
        int var0 = 16384;
        if (this.useDepth) {
            GlStateManager.clearDepth(1.0);
            var0 |= 256;
        }

        GlStateManager.clear(var0, param0);
        this.unbindWrite();
    }
}
