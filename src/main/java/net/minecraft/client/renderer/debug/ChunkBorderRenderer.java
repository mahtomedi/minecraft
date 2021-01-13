package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChunkBorderRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;

    public ChunkBorderRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        RenderSystem.enableDepthTest();
        RenderSystem.shadeModel(7425);
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        Entity var0 = this.minecraft.gameRenderer.getMainCamera().getEntity();
        Tesselator var1 = Tesselator.getInstance();
        BufferBuilder var2 = var1.getBuilder();
        double var3 = 0.0 - param3;
        double var4 = 256.0 - param3;
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();
        double var5 = (double)(var0.xChunk << 4) - param2;
        double var6 = (double)(var0.zChunk << 4) - param4;
        RenderSystem.lineWidth(1.0F);
        var2.begin(3, DefaultVertexFormat.POSITION_COLOR);

        for(int var7 = -16; var7 <= 32; var7 += 16) {
            for(int var8 = -16; var8 <= 32; var8 += 16) {
                var2.vertex(var5 + (double)var7, var3, var6 + (double)var8).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
                var2.vertex(var5 + (double)var7, var3, var6 + (double)var8).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                var2.vertex(var5 + (double)var7, var4, var6 + (double)var8).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                var2.vertex(var5 + (double)var7, var4, var6 + (double)var8).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
            }
        }

        for(int var9 = 2; var9 < 16; var9 += 2) {
            var2.vertex(var5 + (double)var9, var3, var6).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var5 + (double)var9, var3, var6).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var5 + (double)var9, var4, var6).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var5 + (double)var9, var4, var6).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var5 + (double)var9, var3, var6 + 16.0).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var5 + (double)var9, var3, var6 + 16.0).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var5 + (double)var9, var4, var6 + 16.0).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var5 + (double)var9, var4, var6 + 16.0).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
        }

        for(int var10 = 2; var10 < 16; var10 += 2) {
            var2.vertex(var5, var3, var6 + (double)var10).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var5, var3, var6 + (double)var10).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var5, var4, var6 + (double)var10).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var5, var4, var6 + (double)var10).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var5 + 16.0, var3, var6 + (double)var10).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var5 + 16.0, var3, var6 + (double)var10).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var5 + 16.0, var4, var6 + (double)var10).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var5 + 16.0, var4, var6 + (double)var10).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
        }

        for(int var11 = 0; var11 <= 256; var11 += 2) {
            double var12 = (double)var11 - param3;
            var2.vertex(var5, var12, var6).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var5, var12, var6).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var5, var12, var6 + 16.0).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var5 + 16.0, var12, var6 + 16.0).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var5 + 16.0, var12, var6).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var5, var12, var6).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var5, var12, var6).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
        }

        var1.end();
        RenderSystem.lineWidth(2.0F);
        var2.begin(3, DefaultVertexFormat.POSITION_COLOR);

        for(int var13 = 0; var13 <= 16; var13 += 16) {
            for(int var14 = 0; var14 <= 16; var14 += 16) {
                var2.vertex(var5 + (double)var13, var3, var6 + (double)var14).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
                var2.vertex(var5 + (double)var13, var3, var6 + (double)var14).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
                var2.vertex(var5 + (double)var13, var4, var6 + (double)var14).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
                var2.vertex(var5 + (double)var13, var4, var6 + (double)var14).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
            }
        }

        for(int var15 = 0; var15 <= 256; var15 += 16) {
            double var16 = (double)var15 - param3;
            var2.vertex(var5, var16, var6).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
            var2.vertex(var5, var16, var6).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var2.vertex(var5, var16, var6 + 16.0).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var2.vertex(var5 + 16.0, var16, var6 + 16.0).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var2.vertex(var5 + 16.0, var16, var6).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var2.vertex(var5, var16, var6).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var2.vertex(var5, var16, var6).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
        }

        var1.end();
        RenderSystem.lineWidth(1.0F);
        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
    }
}
