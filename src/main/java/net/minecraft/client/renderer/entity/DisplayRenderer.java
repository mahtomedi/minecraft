package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.List;
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
public abstract class DisplayRenderer<T extends Display> extends EntityRenderer<T> {
    private static final float MAX_SHADOW_RADIUS = 64.0F;
    private final EntityRenderDispatcher entityRenderDispatcher;

    protected DisplayRenderer(EntityRendererProvider.Context param0) {
        super(param0);
        this.entityRenderDispatcher = param0.getEntityRenderDispatcher();
    }

    public ResourceLocation getTextureLocation(T param0) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    public void render(T param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        float var0 = param0.calculateInterpolationProgress(param2);
        this.shadowRadius = Math.min(param0.getShadowRadius(var0), 64.0F);
        this.shadowStrength = param0.getShadowStrength(var0);
        int var1 = param0.getPackedBrightnessOverride();
        int var2 = var1 != -1 ? var1 : param5;
        super.render(param0, param1, param2, param3, param4, var2);
        param3.pushPose();
        param3.mulPose(this.calculateOrientation(param0));
        Transformation var3 = param0.transformation(var0);
        param3.mulPoseMatrix(var3.getMatrix());
        param3.last().normal().rotate(var3.getLeftRotation()).rotate(var3.getRightRotation());
        this.renderInner(param0, param3, param4, var2, var0);
        param3.popPose();
    }

    private Quaternionf calculateOrientation(T param0) {
        Camera var0 = this.entityRenderDispatcher.camera;

        return switch(param0.getBillboardConstraints()) {
            case FIXED -> param0.orientation();
            case HORIZONTAL -> new Quaternionf().rotationYXZ((float) (-Math.PI / 180.0) * param0.getYRot(), (float) (-Math.PI / 180.0) * var0.getXRot(), 0.0F);
            case VERTICAL -> new Quaternionf()
            .rotationYXZ((float) Math.PI - (float) (Math.PI / 180.0) * var0.getYRot(), (float) (Math.PI / 180.0) * param0.getXRot(), 0.0F);
            case CENTER -> new Quaternionf()
            .rotationYXZ((float) Math.PI - (float) (Math.PI / 180.0) * var0.getYRot(), (float) (-Math.PI / 180.0) * var0.getXRot(), 0.0F);
        };
    }

    protected abstract void renderInner(T var1, PoseStack var2, MultiBufferSource var3, int var4, float var5);

    @OnlyIn(Dist.CLIENT)
    public static class BlockDisplayRenderer extends DisplayRenderer<Display.BlockDisplay> {
        private final BlockRenderDispatcher blockRenderer;

        protected BlockDisplayRenderer(EntityRendererProvider.Context param0) {
            super(param0);
            this.blockRenderer = param0.getBlockRenderDispatcher();
        }

        public void renderInner(Display.BlockDisplay param0, PoseStack param1, MultiBufferSource param2, int param3, float param4) {
            this.blockRenderer.renderSingleBlock(param0.getBlockState(), param1, param2, param3, OverlayTexture.NO_OVERLAY);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ItemDisplayRenderer extends DisplayRenderer<Display.ItemDisplay> {
        private final ItemRenderer itemRenderer;

        protected ItemDisplayRenderer(EntityRendererProvider.Context param0) {
            super(param0);
            this.itemRenderer = param0.getItemRenderer();
        }

        public void renderInner(Display.ItemDisplay param0, PoseStack param1, MultiBufferSource param2, int param3, float param4) {
            this.itemRenderer
                .renderStatic(
                    param0.getItemStack(), param0.getItemTransform(), param3, OverlayTexture.NO_OVERLAY, param1, param2, param0.getLevel(), param0.getId()
                );
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class TextDisplayRenderer extends DisplayRenderer<Display.TextDisplay> {
        private final Font font;

        protected TextDisplayRenderer(EntityRendererProvider.Context param0) {
            super(param0);
            this.font = param0.getFont();
        }

        private Display.TextDisplay.CachedInfo splitLines(Component param0, int param1) {
            List<FormattedCharSequence> var0 = this.font.split(param0, param1);
            List<Display.TextDisplay.CachedLine> var1 = new ArrayList(var0.size());
            int var2 = 0;

            for(FormattedCharSequence var3 : var0) {
                int var4 = this.font.width(var3);
                var2 = Math.max(var2, var4);
                var1.add(new Display.TextDisplay.CachedLine(var3, var4));
            }

            return new Display.TextDisplay.CachedInfo(var1, var2);
        }

        public void renderInner(Display.TextDisplay param0, PoseStack param1, MultiBufferSource param2, int param3, float param4) {
            byte var0 = param0.getFlags();
            boolean var1 = (var0 & 2) != 0;
            boolean var2 = (var0 & 4) != 0;
            boolean var3 = (var0 & 1) != 0;
            Display.TextDisplay.Align var4 = Display.TextDisplay.getAlign(var0);
            byte var5 = param0.getTextOpacity(param4);
            int var7;
            if (var2) {
                float var6 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
                var7 = (int)(var6 * 255.0F) << 24;
            } else {
                var7 = param0.getBackgroundColor(param4);
            }

            float var9 = 0.0F;
            Matrix4f var10 = param1.last().pose();
            var10.rotate((float) Math.PI, 0.0F, 1.0F, 0.0F);
            var10.scale(-0.025F, -0.025F, -0.025F);
            Display.TextDisplay.CachedInfo var11 = param0.cacheDisplay(this::splitLines);
            int var12 = 9 + 1;
            int var13 = var11.width();
            int var14 = var11.lines().size() * var12;
            var10.translate(1.0F - (float)var13 / 2.0F, (float)(-var14), 0.0F);
            if (var7 != 0) {
                VertexConsumer var15 = param2.getBuffer(var1 ? RenderType.textBackgroundSeeThrough() : RenderType.textBackground());
                var15.vertex(var10, -1.0F, -1.0F, 0.0F).color(var7).uv2(param3).endVertex();
                var15.vertex(var10, -1.0F, (float)var14, 0.0F).color(var7).uv2(param3).endVertex();
                var15.vertex(var10, (float)var13, (float)var14, 0.0F).color(var7).uv2(param3).endVertex();
                var15.vertex(var10, (float)var13, -1.0F, 0.0F).color(var7).uv2(param3).endVertex();
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
                        param2,
                        var1 ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET,
                        0,
                        param3
                    );
                var9 += (float)var12;
            }

        }
    }
}
