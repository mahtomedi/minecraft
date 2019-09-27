package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChunkBorderRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;

    public ChunkBorderRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void render(long param0) {
        RenderSystem.shadeModel(7425);
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        Camera var0 = this.minecraft.gameRenderer.getMainCamera();
        Tesselator var1 = Tesselator.getInstance();
        BufferBuilder var2 = var1.getBuilder();
        double var3 = var0.getPosition().x;
        double var4 = var0.getPosition().y;
        double var5 = var0.getPosition().z;
        double var6 = 0.0 - var4;
        double var7 = 256.0 - var4;
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();
        double var8 = (double)(var0.getEntity().xChunk << 4) - var3;
        double var9 = (double)(var0.getEntity().zChunk << 4) - var5;
        RenderSystem.lineWidth(1.0F);
        var2.begin(3, DefaultVertexFormat.POSITION_COLOR);

        for(int var10 = -16; var10 <= 32; var10 += 16) {
            for(int var11 = -16; var11 <= 32; var11 += 16) {
                var2.vertex(var8 + (double)var10, var6, var9 + (double)var11).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
                var2.vertex(var8 + (double)var10, var6, var9 + (double)var11).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                var2.vertex(var8 + (double)var10, var7, var9 + (double)var11).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                var2.vertex(var8 + (double)var10, var7, var9 + (double)var11).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
            }
        }

        for(int var12 = 2; var12 < 16; var12 += 2) {
            var2.vertex(var8 + (double)var12, var6, var9).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var8 + (double)var12, var6, var9).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var8 + (double)var12, var7, var9).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var8 + (double)var12, var7, var9).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var8 + (double)var12, var6, var9 + 16.0).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var8 + (double)var12, var6, var9 + 16.0).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var8 + (double)var12, var7, var9 + 16.0).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var8 + (double)var12, var7, var9 + 16.0).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
        }

        for(int var13 = 2; var13 < 16; var13 += 2) {
            var2.vertex(var8, var6, var9 + (double)var13).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var8, var6, var9 + (double)var13).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var8, var7, var9 + (double)var13).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var8, var7, var9 + (double)var13).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var8 + 16.0, var6, var9 + (double)var13).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var8 + 16.0, var6, var9 + (double)var13).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var8 + 16.0, var7, var9 + (double)var13).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var8 + 16.0, var7, var9 + (double)var13).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
        }

        for(int var14 = 0; var14 <= 256; var14 += 2) {
            double var15 = (double)var14 - var4;
            var2.vertex(var8, var15, var9).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var8, var15, var9).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var8, var15, var9 + 16.0).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var8 + 16.0, var15, var9 + 16.0).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var8 + 16.0, var15, var9).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var8, var15, var9).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var8, var15, var9).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
        }

        var1.end();
        RenderSystem.lineWidth(2.0F);
        var2.begin(3, DefaultVertexFormat.POSITION_COLOR);

        for(int var16 = 0; var16 <= 16; var16 += 16) {
            for(int var17 = 0; var17 <= 16; var17 += 16) {
                var2.vertex(var8 + (double)var16, var6, var9 + (double)var17).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
                var2.vertex(var8 + (double)var16, var6, var9 + (double)var17).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
                var2.vertex(var8 + (double)var16, var7, var9 + (double)var17).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
                var2.vertex(var8 + (double)var16, var7, var9 + (double)var17).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
            }
        }

        for(int var18 = 0; var18 <= 256; var18 += 16) {
            double var19 = (double)var18 - var4;
            var2.vertex(var8, var19, var9).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
            var2.vertex(var8, var19, var9).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var2.vertex(var8, var19, var9 + 16.0).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var2.vertex(var8 + 16.0, var19, var9 + 16.0).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var2.vertex(var8 + 16.0, var19, var9).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var2.vertex(var8, var19, var9).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var2.vertex(var8, var19, var9).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
        }

        var1.end();
        RenderSystem.lineWidth(1.0F);
        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
    }
}
