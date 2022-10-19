package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CubeMap {
    private static final int SIDES = 6;
    private final ResourceLocation[] images = new ResourceLocation[6];

    public CubeMap(ResourceLocation param0) {
        for(int var0 = 0; var0 < 6; ++var0) {
            this.images[var0] = param0.withPath(param0.getPath() + "_" + var0 + ".png");
        }

    }

    public void render(Minecraft param0, float param1, float param2, float param3) {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        Matrix4f var2 = Matrix4f.perspective(85.0, (float)param0.getWindow().getWidth() / (float)param0.getWindow().getHeight(), 0.05F, 10.0F);
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(var2);
        PoseStack var3 = RenderSystem.getModelViewStack();
        var3.pushPose();
        var3.setIdentity();
        var3.mulPose(Vector3f.XP.rotationDegrees(180.0F));
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        int var4 = 2;

        for(int var5 = 0; var5 < 4; ++var5) {
            var3.pushPose();
            float var6 = ((float)(var5 % 2) / 2.0F - 0.5F) / 256.0F;
            float var7 = ((float)(var5 / 2) / 2.0F - 0.5F) / 256.0F;
            float var8 = 0.0F;
            var3.translate((double)var6, (double)var7, 0.0);
            var3.mulPose(Vector3f.XP.rotationDegrees(param1));
            var3.mulPose(Vector3f.YP.rotationDegrees(param2));
            RenderSystem.applyModelViewMatrix();

            for(int var9 = 0; var9 < 6; ++var9) {
                RenderSystem.setShaderTexture(0, this.images[var9]);
                var1.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                int var10 = Math.round(255.0F * param3) / (var5 + 1);
                if (var9 == 0) {
                    var1.vertex(-1.0, -1.0, 1.0).uv(0.0F, 0.0F).color(255, 255, 255, var10).endVertex();
                    var1.vertex(-1.0, 1.0, 1.0).uv(0.0F, 1.0F).color(255, 255, 255, var10).endVertex();
                    var1.vertex(1.0, 1.0, 1.0).uv(1.0F, 1.0F).color(255, 255, 255, var10).endVertex();
                    var1.vertex(1.0, -1.0, 1.0).uv(1.0F, 0.0F).color(255, 255, 255, var10).endVertex();
                }

                if (var9 == 1) {
                    var1.vertex(1.0, -1.0, 1.0).uv(0.0F, 0.0F).color(255, 255, 255, var10).endVertex();
                    var1.vertex(1.0, 1.0, 1.0).uv(0.0F, 1.0F).color(255, 255, 255, var10).endVertex();
                    var1.vertex(1.0, 1.0, -1.0).uv(1.0F, 1.0F).color(255, 255, 255, var10).endVertex();
                    var1.vertex(1.0, -1.0, -1.0).uv(1.0F, 0.0F).color(255, 255, 255, var10).endVertex();
                }

                if (var9 == 2) {
                    var1.vertex(1.0, -1.0, -1.0).uv(0.0F, 0.0F).color(255, 255, 255, var10).endVertex();
                    var1.vertex(1.0, 1.0, -1.0).uv(0.0F, 1.0F).color(255, 255, 255, var10).endVertex();
                    var1.vertex(-1.0, 1.0, -1.0).uv(1.0F, 1.0F).color(255, 255, 255, var10).endVertex();
                    var1.vertex(-1.0, -1.0, -1.0).uv(1.0F, 0.0F).color(255, 255, 255, var10).endVertex();
                }

                if (var9 == 3) {
                    var1.vertex(-1.0, -1.0, -1.0).uv(0.0F, 0.0F).color(255, 255, 255, var10).endVertex();
                    var1.vertex(-1.0, 1.0, -1.0).uv(0.0F, 1.0F).color(255, 255, 255, var10).endVertex();
                    var1.vertex(-1.0, 1.0, 1.0).uv(1.0F, 1.0F).color(255, 255, 255, var10).endVertex();
                    var1.vertex(-1.0, -1.0, 1.0).uv(1.0F, 0.0F).color(255, 255, 255, var10).endVertex();
                }

                if (var9 == 4) {
                    var1.vertex(-1.0, -1.0, -1.0).uv(0.0F, 0.0F).color(255, 255, 255, var10).endVertex();
                    var1.vertex(-1.0, -1.0, 1.0).uv(0.0F, 1.0F).color(255, 255, 255, var10).endVertex();
                    var1.vertex(1.0, -1.0, 1.0).uv(1.0F, 1.0F).color(255, 255, 255, var10).endVertex();
                    var1.vertex(1.0, -1.0, -1.0).uv(1.0F, 0.0F).color(255, 255, 255, var10).endVertex();
                }

                if (var9 == 5) {
                    var1.vertex(-1.0, 1.0, 1.0).uv(0.0F, 0.0F).color(255, 255, 255, var10).endVertex();
                    var1.vertex(-1.0, 1.0, -1.0).uv(0.0F, 1.0F).color(255, 255, 255, var10).endVertex();
                    var1.vertex(1.0, 1.0, -1.0).uv(1.0F, 1.0F).color(255, 255, 255, var10).endVertex();
                    var1.vertex(1.0, 1.0, 1.0).uv(1.0F, 0.0F).color(255, 255, 255, var10).endVertex();
                }

                var0.end();
            }

            var3.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.colorMask(true, true, true, false);
        }

        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.restoreProjectionMatrix();
        var3.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    public CompletableFuture<Void> preload(TextureManager param0, Executor param1) {
        CompletableFuture<?>[] var0 = new CompletableFuture[6];

        for(int var1 = 0; var1 < var0.length; ++var1) {
            var0[var1] = param0.preload(this.images[var1], param1);
        }

        return CompletableFuture.allOf(var0);
    }
}
