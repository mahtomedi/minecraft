package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChunkBorderRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private static final int CELL_BORDER = FastColor.ARGB32.color(255, 0, 155, 155);
    private static final int YELLOW = FastColor.ARGB32.color(255, 255, 255, 0);

    public ChunkBorderRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
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
        var2.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for(int var8 = -16; var8 <= 32; var8 += 16) {
            for(int var9 = -16; var9 <= 32; var9 += 16) {
                var2.vertex(var6 + (double)var8, var3, var7 + (double)var9).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
                var2.vertex(var6 + (double)var8, var3, var7 + (double)var9).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                var2.vertex(var6 + (double)var8, var4, var7 + (double)var9).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                var2.vertex(var6 + (double)var8, var4, var7 + (double)var9).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
            }
        }

        for(int var10 = 2; var10 < 16; var10 += 2) {
            int var11 = var10 % 4 == 0 ? CELL_BORDER : YELLOW;
            var2.vertex(var6 + (double)var10, var3, var7).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var6 + (double)var10, var3, var7).color(var11).endVertex();
            var2.vertex(var6 + (double)var10, var4, var7).color(var11).endVertex();
            var2.vertex(var6 + (double)var10, var4, var7).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var6 + (double)var10, var3, var7 + 16.0).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var6 + (double)var10, var3, var7 + 16.0).color(var11).endVertex();
            var2.vertex(var6 + (double)var10, var4, var7 + 16.0).color(var11).endVertex();
            var2.vertex(var6 + (double)var10, var4, var7 + 16.0).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
        }

        for(int var12 = 2; var12 < 16; var12 += 2) {
            int var13 = var12 % 4 == 0 ? CELL_BORDER : YELLOW;
            var2.vertex(var6, var3, var7 + (double)var12).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var6, var3, var7 + (double)var12).color(var13).endVertex();
            var2.vertex(var6, var4, var7 + (double)var12).color(var13).endVertex();
            var2.vertex(var6, var4, var7 + (double)var12).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var6 + 16.0, var3, var7 + (double)var12).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var6 + 16.0, var3, var7 + (double)var12).color(var13).endVertex();
            var2.vertex(var6 + 16.0, var4, var7 + (double)var12).color(var13).endVertex();
            var2.vertex(var6 + 16.0, var4, var7 + (double)var12).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
        }

        for(int var14 = this.minecraft.level.getMinBuildHeight(); var14 <= this.minecraft.level.getMaxBuildHeight(); var14 += 2) {
            double var15 = (double)var14 - param3;
            int var16 = var14 % 8 == 0 ? CELL_BORDER : YELLOW;
            var2.vertex(var6, var15, var7).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var2.vertex(var6, var15, var7).color(var16).endVertex();
            var2.vertex(var6, var15, var7 + 16.0).color(var16).endVertex();
            var2.vertex(var6 + 16.0, var15, var7 + 16.0).color(var16).endVertex();
            var2.vertex(var6 + 16.0, var15, var7).color(var16).endVertex();
            var2.vertex(var6, var15, var7).color(var16).endVertex();
            var2.vertex(var6, var15, var7).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
        }

        var1.end();
        RenderSystem.lineWidth(2.0F);
        var2.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for(int var17 = 0; var17 <= 16; var17 += 16) {
            for(int var18 = 0; var18 <= 16; var18 += 16) {
                var2.vertex(var6 + (double)var17, var3, var7 + (double)var18).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
                var2.vertex(var6 + (double)var17, var3, var7 + (double)var18).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
                var2.vertex(var6 + (double)var17, var4, var7 + (double)var18).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
                var2.vertex(var6 + (double)var17, var4, var7 + (double)var18).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
            }
        }

        for(int var19 = this.minecraft.level.getMinBuildHeight(); var19 <= this.minecraft.level.getMaxBuildHeight(); var19 += 16) {
            double var20 = (double)var19 - param3;
            var2.vertex(var6, var20, var7).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
            var2.vertex(var6, var20, var7).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var2.vertex(var6, var20, var7 + 16.0).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var2.vertex(var6 + 16.0, var20, var7 + 16.0).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var2.vertex(var6 + 16.0, var20, var7).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var2.vertex(var6, var20, var7).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var2.vertex(var6, var20, var7).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
        }

        var1.end();
        RenderSystem.lineWidth(1.0F);
        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
    }
}
