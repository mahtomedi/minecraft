package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CubeMap {
    private final ResourceLocation[] images = new ResourceLocation[6];

    public CubeMap(ResourceLocation param0) {
        for(int var0 = 0; var0 < 6; ++var0) {
            this.images[var0] = new ResourceLocation(param0.getNamespace(), param0.getPath() + '_' + var0 + ".png");
        }

    }

    public void render(Minecraft param0, float param1, float param2, float param3) {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        RenderSystem.matrixMode(5889);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.multMatrix(Matrix4f.perspective(85.0, (float)param0.getWindow().getWidth() / (float)param0.getWindow().getHeight(), 0.05F, 10.0F));
        RenderSystem.matrixMode(5888);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        int var2 = 2;

        for(int var3 = 0; var3 < 4; ++var3) {
            RenderSystem.pushMatrix();
            float var4 = ((float)(var3 % 2) / 2.0F - 0.5F) / 256.0F;
            float var5 = ((float)(var3 / 2) / 2.0F - 0.5F) / 256.0F;
            float var6 = 0.0F;
            RenderSystem.translatef(var4, var5, 0.0F);
            RenderSystem.rotatef(param1, 1.0F, 0.0F, 0.0F);
            RenderSystem.rotatef(param2, 0.0F, 1.0F, 0.0F);

            for(int var7 = 0; var7 < 6; ++var7) {
                param0.getTextureManager().bind(this.images[var7]);
                var1.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
                int var8 = Math.round(255.0F * param3) / (var3 + 1);
                if (var7 == 0) {
                    var1.vertex(-1.0, -1.0, 1.0).uv(0.0F, 0.0F).color(255, 255, 255, var8).endVertex();
                    var1.vertex(-1.0, 1.0, 1.0).uv(0.0F, 1.0F).color(255, 255, 255, var8).endVertex();
                    var1.vertex(1.0, 1.0, 1.0).uv(1.0F, 1.0F).color(255, 255, 255, var8).endVertex();
                    var1.vertex(1.0, -1.0, 1.0).uv(1.0F, 0.0F).color(255, 255, 255, var8).endVertex();
                }

                if (var7 == 1) {
                    var1.vertex(1.0, -1.0, 1.0).uv(0.0F, 0.0F).color(255, 255, 255, var8).endVertex();
                    var1.vertex(1.0, 1.0, 1.0).uv(0.0F, 1.0F).color(255, 255, 255, var8).endVertex();
                    var1.vertex(1.0, 1.0, -1.0).uv(1.0F, 1.0F).color(255, 255, 255, var8).endVertex();
                    var1.vertex(1.0, -1.0, -1.0).uv(1.0F, 0.0F).color(255, 255, 255, var8).endVertex();
                }

                if (var7 == 2) {
                    var1.vertex(1.0, -1.0, -1.0).uv(0.0F, 0.0F).color(255, 255, 255, var8).endVertex();
                    var1.vertex(1.0, 1.0, -1.0).uv(0.0F, 1.0F).color(255, 255, 255, var8).endVertex();
                    var1.vertex(-1.0, 1.0, -1.0).uv(1.0F, 1.0F).color(255, 255, 255, var8).endVertex();
                    var1.vertex(-1.0, -1.0, -1.0).uv(1.0F, 0.0F).color(255, 255, 255, var8).endVertex();
                }

                if (var7 == 3) {
                    var1.vertex(-1.0, -1.0, -1.0).uv(0.0F, 0.0F).color(255, 255, 255, var8).endVertex();
                    var1.vertex(-1.0, 1.0, -1.0).uv(0.0F, 1.0F).color(255, 255, 255, var8).endVertex();
                    var1.vertex(-1.0, 1.0, 1.0).uv(1.0F, 1.0F).color(255, 255, 255, var8).endVertex();
                    var1.vertex(-1.0, -1.0, 1.0).uv(1.0F, 0.0F).color(255, 255, 255, var8).endVertex();
                }

                if (var7 == 4) {
                    var1.vertex(-1.0, -1.0, -1.0).uv(0.0F, 0.0F).color(255, 255, 255, var8).endVertex();
                    var1.vertex(-1.0, -1.0, 1.0).uv(0.0F, 1.0F).color(255, 255, 255, var8).endVertex();
                    var1.vertex(1.0, -1.0, 1.0).uv(1.0F, 1.0F).color(255, 255, 255, var8).endVertex();
                    var1.vertex(1.0, -1.0, -1.0).uv(1.0F, 0.0F).color(255, 255, 255, var8).endVertex();
                }

                if (var7 == 5) {
                    var1.vertex(-1.0, 1.0, 1.0).uv(0.0F, 0.0F).color(255, 255, 255, var8).endVertex();
                    var1.vertex(-1.0, 1.0, -1.0).uv(0.0F, 1.0F).color(255, 255, 255, var8).endVertex();
                    var1.vertex(1.0, 1.0, -1.0).uv(1.0F, 1.0F).color(255, 255, 255, var8).endVertex();
                    var1.vertex(1.0, 1.0, 1.0).uv(1.0F, 0.0F).color(255, 255, 255, var8).endVertex();
                }

                var0.end();
            }

            RenderSystem.popMatrix();
            RenderSystem.colorMask(true, true, true, false);
        }

        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.matrixMode(5889);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(5888);
        RenderSystem.popMatrix();
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
