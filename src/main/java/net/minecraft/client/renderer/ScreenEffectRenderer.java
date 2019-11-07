package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
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

@OnlyIn(Dist.CLIENT)
public class ScreenEffectRenderer {
    private static final ResourceLocation UNDERWATER_LOCATION = new ResourceLocation("textures/misc/underwater.png");

    public static void renderScreenEffect(Minecraft param0, PoseStack param1) {
        RenderSystem.disableAlphaTest();
        if (param0.player.isInWall()) {
            BlockState var0 = param0.level.getBlockState(new BlockPos(param0.player));
            Player var1 = param0.player;

            for(int var2 = 0; var2 < 8; ++var2) {
                double var3 = var1.getX() + (double)(((float)((var2 >> 0) % 2) - 0.5F) * var1.getBbWidth() * 0.8F);
                double var4 = var1.getY() + (double)(((float)((var2 >> 1) % 2) - 0.5F) * 0.1F);
                double var5 = var1.getZ() + (double)(((float)((var2 >> 2) % 2) - 0.5F) * var1.getBbWidth() * 0.8F);
                BlockPos var6 = new BlockPos(var3, var4 + (double)var1.getEyeHeight(), var5);
                BlockState var7 = param0.level.getBlockState(var6);
                if (var7.isViewBlocking(param0.level, var6)) {
                    var0 = var7;
                }
            }

            if (var0.getRenderShape() != RenderShape.INVISIBLE) {
                renderTex(param0, param0.getBlockRenderer().getBlockModelShaper().getParticleIcon(var0), param1);
            }
        }

        if (!param0.player.isSpectator()) {
            if (param0.player.isUnderLiquid(FluidTags.WATER)) {
                renderWater(param0, param1);
            }

            if (param0.player.isOnFire()) {
                renderFire(param0, param1);
            }
        }

        RenderSystem.enableAlphaTest();
    }

    private static void renderTex(Minecraft param0, TextureAtlasSprite param1, PoseStack param2) {
        param0.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
        BufferBuilder var0 = Tesselator.getInstance().getBuilder();
        float var1 = 0.1F;
        float var2 = -1.0F;
        float var3 = 1.0F;
        float var4 = -1.0F;
        float var5 = 1.0F;
        float var6 = -0.5F;
        float var7 = param1.getU0();
        float var8 = param1.getU1();
        float var9 = param1.getV0();
        float var10 = param1.getV1();
        Matrix4f var11 = param2.last().pose();
        var0.begin(7, DefaultVertexFormat.POSITION_COLOR_TEX);
        var0.vertex(var11, -1.0F, -1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).uv(var8, var10).endVertex();
        var0.vertex(var11, 1.0F, -1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).uv(var7, var10).endVertex();
        var0.vertex(var11, 1.0F, 1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).uv(var7, var9).endVertex();
        var0.vertex(var11, -1.0F, 1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).uv(var8, var9).endVertex();
        var0.end();
        BufferUploader.end(var0);
    }

    private static void renderWater(Minecraft param0, PoseStack param1) {
        param0.getTextureManager().bind(UNDERWATER_LOCATION);
        BufferBuilder var0 = Tesselator.getInstance().getBuilder();
        float var1 = param0.player.getBrightness();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        float var2 = 4.0F;
        float var3 = -1.0F;
        float var4 = 1.0F;
        float var5 = -1.0F;
        float var6 = 1.0F;
        float var7 = -0.5F;
        float var8 = -param0.player.yRot / 64.0F;
        float var9 = param0.player.xRot / 64.0F;
        Matrix4f var10 = param1.last().pose();
        var0.begin(7, DefaultVertexFormat.POSITION_COLOR_TEX);
        var0.vertex(var10, -1.0F, -1.0F, -0.5F).color(var1, var1, var1, 0.1F).uv(4.0F + var8, 4.0F + var9).endVertex();
        var0.vertex(var10, 1.0F, -1.0F, -0.5F).color(var1, var1, var1, 0.1F).uv(0.0F + var8, 4.0F + var9).endVertex();
        var0.vertex(var10, 1.0F, 1.0F, -0.5F).color(var1, var1, var1, 0.1F).uv(0.0F + var8, 0.0F + var9).endVertex();
        var0.vertex(var10, -1.0F, 1.0F, -0.5F).color(var1, var1, var1, 0.1F).uv(4.0F + var8, 0.0F + var9).endVertex();
        var0.end();
        BufferUploader.end(var0);
        RenderSystem.disableBlend();
    }

    private static void renderFire(Minecraft param0, PoseStack param1) {
        BufferBuilder var0 = Tesselator.getInstance().getBuilder();
        RenderSystem.depthFunc(519);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        TextureAtlasSprite var1 = param0.getTextureAtlas().getSprite(ModelBakery.FIRE_1);
        param0.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
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
            param1.translate((double)((float)(-(var14 * 2 - 1)) * 0.24F), -0.3F, 0.0);
            param1.mulPose(Vector3f.YP.rotationDegrees((float)(var14 * 2 - 1) * 10.0F));
            Matrix4f var20 = param1.last().pose();
            var0.begin(7, DefaultVertexFormat.POSITION_COLOR_TEX);
            var0.vertex(var20, -0.5F, -0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(var10, var12).endVertex();
            var0.vertex(var20, 0.5F, -0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(var9, var12).endVertex();
            var0.vertex(var20, 0.5F, 0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(var9, var11).endVertex();
            var0.vertex(var20, -0.5F, 0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(var10, var11).endVertex();
            var0.end();
            BufferUploader.end(var0);
            param1.popPose();
        }

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(515);
    }
}
