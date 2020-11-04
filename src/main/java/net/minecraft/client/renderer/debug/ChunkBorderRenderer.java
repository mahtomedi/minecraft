package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
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
        double var3 = (double)this.minecraft.level.getMinBuildHeight() - param3;
        double var4 = (double)this.minecraft.level.getMaxBuildHeight() - param3;
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();
        ChunkPos var5 = var0.chunkPosition();
        double var6 = (double)var5.getMinBlockX() - param2;
        double var7 = (double)var5.getMinBlockZ() - param4;
        RenderSystem.lineWidth(1.0F);
        var2.begin(VertexFormat.Mode.LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for(int var8 = -16; var8 <= 32; var8 += 16) {
            for(int var9 = -16; var9 <= 32; var9 += 16) {
                var2.vertex(var6 + (double)var8, var3, var7 + (double)var9).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
                var2.vertex(var6 + (double)var8, var3, var7 + (double)var9).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                var2.vertex(var6 + (double)var8, var4, var7 + (double)var9).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                var2.vertex(var6 + (double)var8, var4, var7 + (double)var9).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
            }
        }

        for(int var10 = 2; var10 < 16; var10 += 2) {
            var2.vertex(var6 + (double)var10, var3, var7).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var6 + (double)var10, var3, var7).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var6 + (double)var10, var4, var7).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var6 + (double)var10, var4, var7).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var6 + (double)var10, var3, var7 + 16.0).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var6 + (double)var10, var3, var7 + 16.0).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var6 + (double)var10, var4, var7 + 16.0).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var6 + (double)var10, var4, var7 + 16.0).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
        }

        for(int var11 = 2; var11 < 16; var11 += 2) {
            var2.vertex(var6, var3, var7 + (double)var11).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var6, var3, var7 + (double)var11).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var6, var4, var7 + (double)var11).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var6, var4, var7 + (double)var11).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var6 + 16.0, var3, var7 + (double)var11).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var6 + 16.0, var3, var7 + (double)var11).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var6 + 16.0, var4, var7 + (double)var11).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var6 + 16.0, var4, var7 + (double)var11).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
        }

        for(int var12 = this.minecraft.level.getMinBuildHeight(); var12 <= this.minecraft.level.getMaxBuildHeight(); var12 += 2) {
            double var13 = (double)var12 - param3;
            var2.vertex(var6, var13, var7).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var6, var13, var7).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var6, var13, var7 + 16.0).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var6 + 16.0, var13, var7 + 16.0).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var6 + 16.0, var13, var7).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var6, var13, var7).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            var2.vertex(var6, var13, var7).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
        }

        var1.end();
        RenderSystem.lineWidth(2.0F);
        var2.begin(VertexFormat.Mode.LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for(int var14 = 0; var14 <= 16; var14 += 16) {
            for(int var15 = 0; var15 <= 16; var15 += 16) {
                var2.vertex(var6 + (double)var14, var3, var7 + (double)var15).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
                var2.vertex(var6 + (double)var14, var3, var7 + (double)var15).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
                var2.vertex(var6 + (double)var14, var4, var7 + (double)var15).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
                var2.vertex(var6 + (double)var14, var4, var7 + (double)var15).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
            }
        }

        for(int var16 = this.minecraft.level.getMinBuildHeight(); var16 <= this.minecraft.level.getMaxBuildHeight(); var16 += 16) {
            double var17 = (double)var16 - param3;
            var2.vertex(var6, var17, var7).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
            var2.vertex(var6, var17, var7).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var2.vertex(var6, var17, var7 + 16.0).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var2.vertex(var6 + 16.0, var17, var7 + 16.0).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var2.vertex(var6 + 16.0, var17, var7).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var2.vertex(var6, var17, var7).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var2.vertex(var6, var17, var7).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
        }

        var1.end();
        RenderSystem.lineWidth(1.0F);
        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
    }
}
