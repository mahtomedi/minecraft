package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Display;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public abstract class DisplayRenderer<T extends Display, S> extends EntityRenderer<T> {
    private final EntityRenderDispatcher entityRenderDispatcher;

    protected DisplayRenderer(EntityRendererProvider.Context param0) {
        super(param0);
        this.entityRenderDispatcher = param0.getEntityRenderDispatcher();
    }

    public ResourceLocation getTextureLocation(T param0) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    public void render(T param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        Display.RenderState var0 = param0.renderState();
        if (var0 != null) {
            S var1 = this.getSubState(param0);
            if (var1 != null) {
                float var2 = param0.calculateInterpolationProgress(param2);
                this.shadowRadius = var0.shadowRadius().get(var2);
                this.shadowStrength = var0.shadowStrength().get(var2);
                int var3 = var0.brightnessOverride();
                int var4 = var3 != -1 ? var3 : param5;
                super.render(param0, param1, param2, param3, param4, var4);
                param3.pushPose();
                param3.mulPose(this.calculateOrientation(var0, param0));
                Transformation var5 = var0.transformation().get(var2);
                param3.mulPoseMatrix(var5.getMatrix());
                param3.last().normal().rotate(var5.getLeftRotation()).rotate(var5.getRightRotation());
                this.renderInner(param0, var1, param3, param4, var4, var2);
                param3.popPose();
            }
        }
    }

    private Quaternionf calculateOrientation(Display.RenderState param0, T param1) {
        Camera var0 = this.entityRenderDispatcher.camera;

        return switch(param0.billboardConstraints()) {
            case FIXED -> param1.orientation();
            case HORIZONTAL -> new Quaternionf().rotationYXZ((float) (-Math.PI / 180.0) * param1.getYRot(), (float) (-Math.PI / 180.0) * var0.getXRot(), 0.0F);
            case VERTICAL -> new Quaternionf()
            .rotationYXZ((float) Math.PI - (float) (Math.PI / 180.0) * var0.getYRot(), (float) (Math.PI / 180.0) * param1.getXRot(), 0.0F);
            case CENTER -> new Quaternionf()
            .rotationYXZ((float) Math.PI - (float) (Math.PI / 180.0) * var0.getYRot(), (float) (-Math.PI / 180.0) * var0.getXRot(), 0.0F);
        };
    }

    @Nullable
    protected abstract S getSubState(T var1);

    protected abstract void renderInner(T var1, S var2, PoseStack var3, MultiBufferSource var4, int var5, float var6);

    @OnlyIn(Dist.CLIENT)
    public static class BlockDisplayRenderer extends DisplayRenderer<Display.BlockDisplay, Display.BlockDisplay.BlockRenderState> {
        private final BlockRenderDispatcher blockRenderer;

        protected BlockDisplayRenderer(EntityRendererProvider.Context param0) {
            super(param0);
            this.blockRenderer = param0.getBlockRenderDispatcher();
        }

        @Nullable
        protected Display.BlockDisplay.BlockRenderState getSubState(Display.BlockDisplay param0) {
            return param0.blockRenderState();
        }

        public void renderInner(
            Display.BlockDisplay param0, Display.BlockDisplay.BlockRenderState param1, PoseStack param2, MultiBufferSource param3, int param4, float param5
        ) {
            this.blockRenderer.renderSingleBlock(param1.blockState(), param2, param3, param4, OverlayTexture.NO_OVERLAY);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ItemDisplayRenderer extends DisplayRenderer<Display.ItemDisplay, Display.ItemDisplay.ItemRenderState> {
        private final ItemRenderer itemRenderer;

        protected ItemDisplayRenderer(EntityRendererProvider.Context param0) {
            super(param0);
            this.itemRenderer = param0.getItemRenderer();
        }

        @Nullable
        protected Display.ItemDisplay.ItemRenderState getSubState(Display.ItemDisplay param0) {
            return param0.itemRenderState();
        }

        public void renderInner(
            Display.ItemDisplay param0, Display.ItemDisplay.ItemRenderState param1, PoseStack param2, MultiBufferSource param3, int param4, float param5
        ) {
            this.itemRenderer
                .renderStatic(param1.itemStack(), param1.itemTransform(), param4, OverlayTexture.NO_OVERLAY, param2, param3, param0.getLevel(), param0.getId());
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class TextDisplayRenderer extends DisplayRenderer<Display.TextDisplay, Display.TextDisplay.TextRenderState> {
        private final Font font;

        protected TextDisplayRenderer(EntityRendererProvider.Context param0) {
            super(param0);
            this.font = param0.getFont();
        }

        private Display.TextDisplay.CachedInfo splitLines(Component param0, int param1) {
            List<FormattedCharSequence> var0 = this.font.split(param0, param1);
            List<Display.TextDisplay.CachedLine> var1 = new ArrayList<>(var0.size());
            int var2 = 0;

            for(FormattedCharSequence var3 : var0) {
                int var4 = this.font.width(var3);
                var2 = Math.max(var2, var4);
                var1.add(new Display.TextDisplay.CachedLine(var3, var4));
            }

            return new Display.TextDisplay.CachedInfo(var1, var2);
        }

        @Nullable
        protected Display.TextDisplay.TextRenderState getSubState(Display.TextDisplay param0) {
            return param0.textRenderState();
        }

        public void renderInner(
            Display.TextDisplay param0, Display.TextDisplay.TextRenderState param1, PoseStack param2, MultiBufferSource param3, int param4, float param5
        ) {
            byte var0 = param1.flags();
            boolean var1 = (var0 & 2) != 0;
            boolean var2 = (var0 & 4) != 0;
            boolean var3 = (var0 & 1) != 0;
            Display.TextDisplay.Align var4 = Display.TextDisplay.getAlign(var0);
            byte var5 = (byte)param1.textOpacity().get(param5);
            int var7;
            if (var2) {
                float var6 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
                var7 = (int)(var6 * 255.0F) << 24;
            } else {
                var7 = param1.backgroundColor().get(param5);
            }

            float var9 = 0.0F;
            Matrix4f var10 = param2.last().pose();
            var10.rotate((float) Math.PI, 0.0F, 1.0F, 0.0F);
            var10.scale(-0.025F, -0.025F, -0.025F);
            Display.TextDisplay.CachedInfo var11 = param0.cacheDisplay(this::splitLines);
            int var12 = 9 + 1;
            int var13 = var11.width();
            int var14 = var11.lines().size() * var12;
            var10.translate(1.0F - (float)var13 / 2.0F, (float)(-var14), 0.0F);
            if (var7 != 0) {
                VertexConsumer var15 = param3.getBuffer(var1 ? RenderType.textBackgroundSeeThrough() : RenderType.textBackground());
                var15.vertex(var10, -1.0F, -1.0F, 0.0F).color(var7).uv2(param4).endVertex();
                var15.vertex(var10, -1.0F, (float)var14, 0.0F).color(var7).uv2(param4).endVertex();
                var15.vertex(var10, (float)var13, (float)var14, 0.0F).color(var7).uv2(param4).endVertex();
                var15.vertex(var10, (float)var13, -1.0F, 0.0F).color(var7).uv2(param4).endVertex();
            }

            for(Display.TextDisplay.CachedLine var16 : var11.lines()) {
                float var17 = switch(var4) {
                    case LEFT -> 0.0F;
                    case RIGHT -> (float)(var13 - var16.width());
                    case CENTER -> (float)var13 / 2.0F - (float)var16.width() / 2.0F;
                };
                this.font
                    .drawInBatch(
                        var16.contents(),
                        var17,
                        var9,
                        var5 << 24 | 16777215,
                        var3,
                        var10,
                        param3,
                        var1 ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET,
                        0,
                        param4
                    );
                var9 += (float)var12;
            }

        }
    }
}
