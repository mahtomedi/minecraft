package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

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
        Entity var0 = this.minecraft.gameRenderer.getMainCamera().getEntity();
        float var1 = (float)((double)this.minecraft.level.getMinBuildHeight() - param3);
        float var2 = (float)((double)this.minecraft.level.getMaxBuildHeight() - param3);
        ChunkPos var3 = var0.chunkPosition();
        float var4 = (float)((double)var3.getMinBlockX() - param2);
        float var5 = (float)((double)var3.getMinBlockZ() - param4);
        VertexConsumer var6 = param1.getBuffer(RenderType.debugLineStrip(1.0));
        Matrix4f var7 = param0.last().pose();

        for(int var8 = -16; var8 <= 32; var8 += 16) {
            for(int var9 = -16; var9 <= 32; var9 += 16) {
                var6.vertex(var7, var4 + (float)var8, var1, var5 + (float)var9).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
                var6.vertex(var7, var4 + (float)var8, var1, var5 + (float)var9).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                var6.vertex(var7, var4 + (float)var8, var2, var5 + (float)var9).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                var6.vertex(var7, var4 + (float)var8, var2, var5 + (float)var9).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
            }
        }

        for(int var10 = 2; var10 < 16; var10 += 2) {
            int var11 = var10 % 4 == 0 ? CELL_BORDER : YELLOW;
            var6.vertex(var7, var4 + (float)var10, var1, var5).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var6.vertex(var7, var4 + (float)var10, var1, var5).color(var11).endVertex();
            var6.vertex(var7, var4 + (float)var10, var2, var5).color(var11).endVertex();
            var6.vertex(var7, var4 + (float)var10, var2, var5).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var6.vertex(var7, var4 + (float)var10, var1, var5 + 16.0F).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var6.vertex(var7, var4 + (float)var10, var1, var5 + 16.0F).color(var11).endVertex();
            var6.vertex(var7, var4 + (float)var10, var2, var5 + 16.0F).color(var11).endVertex();
            var6.vertex(var7, var4 + (float)var10, var2, var5 + 16.0F).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
        }

        for(int var12 = 2; var12 < 16; var12 += 2) {
            int var13 = var12 % 4 == 0 ? CELL_BORDER : YELLOW;
            var6.vertex(var7, var4, var1, var5 + (float)var12).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var6.vertex(var7, var4, var1, var5 + (float)var12).color(var13).endVertex();
            var6.vertex(var7, var4, var2, var5 + (float)var12).color(var13).endVertex();
            var6.vertex(var7, var4, var2, var5 + (float)var12).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var6.vertex(var7, var4 + 16.0F, var1, var5 + (float)var12).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var6.vertex(var7, var4 + 16.0F, var1, var5 + (float)var12).color(var13).endVertex();
            var6.vertex(var7, var4 + 16.0F, var2, var5 + (float)var12).color(var13).endVertex();
            var6.vertex(var7, var4 + 16.0F, var2, var5 + (float)var12).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
        }

        for(int var14 = this.minecraft.level.getMinBuildHeight(); var14 <= this.minecraft.level.getMaxBuildHeight(); var14 += 2) {
            float var15 = (float)((double)var14 - param3);
            int var16 = var14 % 8 == 0 ? CELL_BORDER : YELLOW;
            var6.vertex(var7, var4, var15, var5).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            var6.vertex(var7, var4, var15, var5).color(var16).endVertex();
            var6.vertex(var7, var4, var15, var5 + 16.0F).color(var16).endVertex();
            var6.vertex(var7, var4 + 16.0F, var15, var5 + 16.0F).color(var16).endVertex();
            var6.vertex(var7, var4 + 16.0F, var15, var5).color(var16).endVertex();
            var6.vertex(var7, var4, var15, var5).color(var16).endVertex();
            var6.vertex(var7, var4, var15, var5).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
        }

        var6 = param1.getBuffer(RenderType.debugLineStrip(2.0));

        for(int var17 = 0; var17 <= 16; var17 += 16) {
            for(int var18 = 0; var18 <= 16; var18 += 16) {
                var6.vertex(var7, var4 + (float)var17, var1, var5 + (float)var18).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
                var6.vertex(var7, var4 + (float)var17, var1, var5 + (float)var18).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
                var6.vertex(var7, var4 + (float)var17, var2, var5 + (float)var18).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
                var6.vertex(var7, var4 + (float)var17, var2, var5 + (float)var18).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
            }
        }

        for(int var19 = this.minecraft.level.getMinBuildHeight(); var19 <= this.minecraft.level.getMaxBuildHeight(); var19 += 16) {
            float var20 = (float)((double)var19 - param3);
            var6.vertex(var7, var4, var20, var5).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
            var6.vertex(var7, var4, var20, var5).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var6.vertex(var7, var4, var20, var5 + 16.0F).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var6.vertex(var7, var4 + 16.0F, var20, var5 + 16.0F).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var6.vertex(var7, var4 + 16.0F, var20, var5).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var6.vertex(var7, var4, var20, var5).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var6.vertex(var7, var4, var20, var5).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
        }

    }
}
