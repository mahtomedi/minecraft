package com.mojang.blaze3d.pipeline;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Objects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MainTarget extends RenderTarget {
    public static final int DEFAULT_WIDTH = 854;
    public static final int DEFAULT_HEIGHT = 480;
    static final MainTarget.Dimension DEFAULT_DIMENSIONS = new MainTarget.Dimension(854, 480);

    public MainTarget(int param0, int param1) {
        super(true);
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this.createFrameBuffer(param0, param1));
        } else {
            this.createFrameBuffer(param0, param1);
        }

    }

    private void createFrameBuffer(int param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        MainTarget.Dimension var0 = this.allocateAttachments(param0, param1);
        this.frameBufferId = GlStateManager.glGenFramebuffers();
        GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
        GlStateManager._bindTexture(this.colorTextureId);
        GlStateManager._texParameter(3553, 10241, 9728);
        GlStateManager._texParameter(3553, 10240, 9728);
        GlStateManager._texParameter(3553, 10242, 33071);
        GlStateManager._texParameter(3553, 10243, 33071);
        GlStateManager._glFramebufferTexture2D(36160, 36064, 3553, this.colorTextureId, 0);
        GlStateManager._bindTexture(this.depthBufferId);
        GlStateManager._texParameter(3553, 34892, 0);
        GlStateManager._texParameter(3553, 10241, 9728);
        GlStateManager._texParameter(3553, 10240, 9728);
        GlStateManager._texParameter(3553, 10242, 33071);
        GlStateManager._texParameter(3553, 10243, 33071);
        GlStateManager._glFramebufferTexture2D(36160, 36096, 3553, this.depthBufferId, 0);
        GlStateManager._bindTexture(0);
        this.viewWidth = var0.width;
        this.viewHeight = var0.height;
        this.width = var0.width;
        this.height = var0.height;
        this.checkStatus();
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    private MainTarget.Dimension allocateAttachments(int param0, int param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        this.colorTextureId = TextureUtil.generateTextureId();
        this.depthBufferId = TextureUtil.generateTextureId();
        MainTarget.AttachmentState var0 = MainTarget.AttachmentState.NONE;

        for(MainTarget.Dimension var1 : MainTarget.Dimension.listWithFallback(param0, param1)) {
            var0 = MainTarget.AttachmentState.NONE;
            if (this.allocateColorAttachment(var1)) {
                var0 = var0.with(MainTarget.AttachmentState.COLOR);
            }

            if (this.allocateDepthAttachment(var1)) {
                var0 = var0.with(MainTarget.AttachmentState.DEPTH);
            }

            if (var0 == MainTarget.AttachmentState.COLOR_DEPTH) {
                return var1;
            }
        }

        throw new RuntimeException("Unrecoverable GL_OUT_OF_MEMORY (allocated attachments = " + var0.name() + ")");
    }

    private boolean allocateColorAttachment(MainTarget.Dimension param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GlStateManager._getError();
        GlStateManager._bindTexture(this.colorTextureId);
        GlStateManager._texImage2D(3553, 0, 32856, param0.width, param0.height, 0, 6408, 5121, null);
        return GlStateManager._getError() != 1285;
    }

    private boolean allocateDepthAttachment(MainTarget.Dimension param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GlStateManager._getError();
        GlStateManager._bindTexture(this.depthBufferId);
        GlStateManager._texImage2D(3553, 0, 6402, param0.width, param0.height, 0, 6402, 5126, null);
        return GlStateManager._getError() != 1285;
    }

    @OnlyIn(Dist.CLIENT)
    static enum AttachmentState {
        NONE,
        COLOR,
        DEPTH,
        COLOR_DEPTH;

        private static final MainTarget.AttachmentState[] VALUES = values();

        MainTarget.AttachmentState with(MainTarget.AttachmentState param0) {
            return VALUES[this.ordinal() | param0.ordinal()];
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class Dimension {
        public final int width;
        public final int height;

        Dimension(int param0, int param1) {
            this.width = param0;
            this.height = param1;
        }

        static List<MainTarget.Dimension> listWithFallback(int param0, int param1) {
            RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
            int var0 = RenderSystem.maxSupportedTextureSize();
            return param0 > 0 && param0 <= var0 && param1 > 0 && param1 <= var0
                ? ImmutableList.of(new MainTarget.Dimension(param0, param1), MainTarget.DEFAULT_DIMENSIONS)
                : ImmutableList.of(MainTarget.DEFAULT_DIMENSIONS);
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                MainTarget.Dimension var0 = (MainTarget.Dimension)param0;
                return this.width == var0.width && this.height == var0.height;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.width, this.height);
        }

        @Override
        public String toString() {
            return this.width + "x" + this.height;
        }
    }
}
