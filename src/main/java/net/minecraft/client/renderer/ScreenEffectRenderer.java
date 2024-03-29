package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class ScreenEffectRenderer {
    private static final ResourceLocation UNDERWATER_LOCATION = new ResourceLocation("textures/misc/underwater.png");

    public static void renderScreenEffect(Minecraft param0, PoseStack param1) {
        Player var0 = param0.player;
        if (!var0.noPhysics) {
            BlockState var1 = getViewBlockingState(var0);
            if (var1 != null) {
                renderTex(param0.getBlockRenderer().getBlockModelShaper().getParticleIcon(var1), param1);
            }
        }

        if (!param0.player.isSpectator()) {
            if (param0.player.isEyeInFluid(FluidTags.WATER)) {
                renderWater(param0, param1);
            }

            if (param0.player.isOnFire()) {
                renderFire(param0, param1);
            }
        }

    }

    @Nullable
    private static BlockState getViewBlockingState(Player param0) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();

        for(int var1 = 0; var1 < 8; ++var1) {
            double var2 = param0.getX() + (double)(((float)((var1 >> 0) % 2) - 0.5F) * param0.getBbWidth() * 0.8F);
            double var3 = param0.getEyeY() + (double)(((float)((var1 >> 1) % 2) - 0.5F) * 0.1F);
            double var4 = param0.getZ() + (double)(((float)((var1 >> 2) % 2) - 0.5F) * param0.getBbWidth() * 0.8F);
            var0.set(var2, var3, var4);
            BlockState var5 = param0.level().getBlockState(var0);
            if (var5.getRenderShape() != RenderShape.INVISIBLE && var5.isViewBlocking(param0.level(), var0)) {
                return var5;
            }
        }

        return null;
    }

    private static void renderTex(TextureAtlasSprite param0, PoseStack param1) {
        RenderSystem.setShaderTexture(0, param0.atlasLocation());
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        BufferBuilder var0 = Tesselator.getInstance().getBuilder();
        float var1 = 0.1F;
        float var2 = -1.0F;
        float var3 = 1.0F;
        float var4 = -1.0F;
        float var5 = 1.0F;
        float var6 = -0.5F;
        float var7 = param0.getU0();
        float var8 = param0.getU1();
        float var9 = param0.getV0();
        float var10 = param0.getV1();
        Matrix4f var11 = param1.last().pose();
        var0.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        var0.vertex(var11, -1.0F, -1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).uv(var8, var10).endVertex();
        var0.vertex(var11, 1.0F, -1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).uv(var7, var10).endVertex();
        var0.vertex(var11, 1.0F, 1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).uv(var7, var9).endVertex();
        var0.vertex(var11, -1.0F, 1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).uv(var8, var9).endVertex();
        BufferUploader.drawWithShader(var0.end());
    }

    private static void renderWater(Minecraft param0, PoseStack param1) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, UNDERWATER_LOCATION);
        BufferBuilder var0 = Tesselator.getInstance().getBuilder();
        BlockPos var1 = BlockPos.containing(param0.player.getX(), param0.player.getEyeY(), param0.player.getZ());
        float var2 = LightTexture.getBrightness(param0.player.level().dimensionType(), param0.player.level().getMaxLocalRawBrightness(var1));
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(var2, var2, var2, 0.1F);
        float var3 = 4.0F;
        float var4 = -1.0F;
        float var5 = 1.0F;
        float var6 = -1.0F;
        float var7 = 1.0F;
        float var8 = -0.5F;
        float var9 = -param0.player.getYRot() / 64.0F;
        float var10 = param0.player.getXRot() / 64.0F;
        Matrix4f var11 = param1.last().pose();
        var0.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        var0.vertex(var11, -1.0F, -1.0F, -0.5F).uv(4.0F + var9, 4.0F + var10).endVertex();
        var0.vertex(var11, 1.0F, -1.0F, -0.5F).uv(0.0F + var9, 4.0F + var10).endVertex();
        var0.vertex(var11, 1.0F, 1.0F, -0.5F).uv(0.0F + var9, 0.0F + var10).endVertex();
        var0.vertex(var11, -1.0F, 1.0F, -0.5F).uv(4.0F + var9, 0.0F + var10).endVertex();
        BufferUploader.drawWithShader(var0.end());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private static void renderFire(Minecraft param0, PoseStack param1) {
        BufferBuilder var0 = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.depthFunc(519);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        TextureAtlasSprite var1 = ModelBakery.FIRE_1.sprite();
        RenderSystem.setShaderTexture(0, var1.atlasLocation());
        float var2 = var1.getU0();
        float var3 = var1.getU1();
        float var4 = (var2 + var3) / 2.0F;
        float var5 = var1.getV0();
        float var6 = var1.getV1();
        float var7 = (var5 + var6) / 2.0F;
        float var8 = var1.uvShrinkRatio();
        float var9 = Mth.lerp(var8, var2, var4);
        float var10 = Mth.lerp(var8, var3, var4);
        float var11 = Mth.lerp(var8, var5, var7);
        float var12 = Mth.lerp(var8, var6, var7);
        float var13 = 1.0F;

        for(int var14 = 0; var14 < 2; ++var14) {
            param1.pushPose();
            float var15 = -0.5F;
            float var16 = 0.5F;
            float var17 = -0.5F;
            float var18 = 0.5F;
            float var19 = -0.5F;
            param1.translate((float)(-(var14 * 2 - 1)) * 0.24F, -0.3F, 0.0F);
            param1.mulPose(Axis.YP.rotationDegrees((float)(var14 * 2 - 1) * 10.0F));
            Matrix4f var20 = param1.last().pose();
            var0.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
            var0.vertex(var20, -0.5F, -0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(var10, var12).endVertex();
            var0.vertex(var20, 0.5F, -0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(var9, var12).endVertex();
            var0.vertex(var20, 0.5F, 0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(var9, var11).endVertex();
            var0.vertex(var20, -0.5F, 0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(var10, var11).endVertex();
            BufferUploader.drawWithShader(var0.end());
            param1.popPose();
        }

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(515);
    }
}
