package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PaintingRenderer extends EntityRenderer<Painting> {
    public PaintingRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(Painting param0, double param1, double param2, double param3, float param4, float param5) {
        RenderSystem.pushMatrix();
        RenderSystem.translated(param1, param2, param3);
        RenderSystem.rotatef(180.0F - param4, 0.0F, 1.0F, 0.0F);
        RenderSystem.enableRescaleNormal();
        this.bindTexture(param0);
        Motive var0 = param0.motive;
        float var1 = 0.0625F;
        RenderSystem.scalef(0.0625F, 0.0625F, 0.0625F);
        if (this.solidRender) {
            RenderSystem.enableColorMaterial();
            RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(param0));
        }

        PaintingTextureManager var2 = Minecraft.getInstance().getPaintingTextures();
        this.renderPainting(param0, var0.getWidth(), var0.getHeight(), var2.get(var0), var2.getBackSprite());
        if (this.solidRender) {
            RenderSystem.tearDownSolidRenderingTextureCombine();
            RenderSystem.disableColorMaterial();
        }

        RenderSystem.disableRescaleNormal();
        RenderSystem.popMatrix();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    protected ResourceLocation getTextureLocation(Painting param0) {
        return TextureAtlas.LOCATION_PAINTINGS;
    }

    private void renderPainting(Painting param0, int param1, int param2, TextureAtlasSprite param3, TextureAtlasSprite param4) {
        float var0 = (float)(-param1) / 2.0F;
        float var1 = (float)(-param2) / 2.0F;
        float var2 = 0.5F;
        float var3 = param4.getU0();
        float var4 = param4.getU1();
        float var5 = param4.getV0();
        float var6 = param4.getV1();
        float var7 = param4.getU0();
        float var8 = param4.getU1();
        float var9 = param4.getV0();
        float var10 = param4.getV(1.0);
        float var11 = param4.getU0();
        float var12 = param4.getU(1.0);
        float var13 = param4.getV0();
        float var14 = param4.getV1();
        int var15 = param1 / 16;
        int var16 = param2 / 16;
        double var17 = 16.0 / (double)var15;
        double var18 = 16.0 / (double)var16;

        for(int var19 = 0; var19 < var15; ++var19) {
            for(int var20 = 0; var20 < var16; ++var20) {
                float var21 = var0 + (float)((var19 + 1) * 16);
                float var22 = var0 + (float)(var19 * 16);
                float var23 = var1 + (float)((var20 + 1) * 16);
                float var24 = var1 + (float)(var20 * 16);
                this.setBrightness(param0, (var21 + var22) / 2.0F, (var23 + var24) / 2.0F);
                float var25 = param3.getU(var17 * (double)(var15 - var19));
                float var26 = param3.getU(var17 * (double)(var15 - (var19 + 1)));
                float var27 = param3.getV(var18 * (double)(var16 - var20));
                float var28 = param3.getV(var18 * (double)(var16 - (var20 + 1)));
                Tesselator var29 = Tesselator.getInstance();
                BufferBuilder var30 = var29.getBuilder();
                var30.begin(7, DefaultVertexFormat.POSITION_TEX_NORMAL);
                var30.vertex((double)var21, (double)var24, -0.5).uv((double)var26, (double)var27).normal(0.0F, 0.0F, -1.0F).endVertex();
                var30.vertex((double)var22, (double)var24, -0.5).uv((double)var25, (double)var27).normal(0.0F, 0.0F, -1.0F).endVertex();
                var30.vertex((double)var22, (double)var23, -0.5).uv((double)var25, (double)var28).normal(0.0F, 0.0F, -1.0F).endVertex();
                var30.vertex((double)var21, (double)var23, -0.5).uv((double)var26, (double)var28).normal(0.0F, 0.0F, -1.0F).endVertex();
                var30.vertex((double)var21, (double)var23, 0.5).uv((double)var3, (double)var5).normal(0.0F, 0.0F, 1.0F).endVertex();
                var30.vertex((double)var22, (double)var23, 0.5).uv((double)var4, (double)var5).normal(0.0F, 0.0F, 1.0F).endVertex();
                var30.vertex((double)var22, (double)var24, 0.5).uv((double)var4, (double)var6).normal(0.0F, 0.0F, 1.0F).endVertex();
                var30.vertex((double)var21, (double)var24, 0.5).uv((double)var3, (double)var6).normal(0.0F, 0.0F, 1.0F).endVertex();
                var30.vertex((double)var21, (double)var23, -0.5).uv((double)var7, (double)var9).normal(0.0F, 1.0F, 0.0F).endVertex();
                var30.vertex((double)var22, (double)var23, -0.5).uv((double)var8, (double)var9).normal(0.0F, 1.0F, 0.0F).endVertex();
                var30.vertex((double)var22, (double)var23, 0.5).uv((double)var8, (double)var10).normal(0.0F, 1.0F, 0.0F).endVertex();
                var30.vertex((double)var21, (double)var23, 0.5).uv((double)var7, (double)var10).normal(0.0F, 1.0F, 0.0F).endVertex();
                var30.vertex((double)var21, (double)var24, 0.5).uv((double)var7, (double)var9).normal(0.0F, -1.0F, 0.0F).endVertex();
                var30.vertex((double)var22, (double)var24, 0.5).uv((double)var8, (double)var9).normal(0.0F, -1.0F, 0.0F).endVertex();
                var30.vertex((double)var22, (double)var24, -0.5).uv((double)var8, (double)var10).normal(0.0F, -1.0F, 0.0F).endVertex();
                var30.vertex((double)var21, (double)var24, -0.5).uv((double)var7, (double)var10).normal(0.0F, -1.0F, 0.0F).endVertex();
                var30.vertex((double)var21, (double)var23, 0.5).uv((double)var12, (double)var13).normal(-1.0F, 0.0F, 0.0F).endVertex();
                var30.vertex((double)var21, (double)var24, 0.5).uv((double)var12, (double)var14).normal(-1.0F, 0.0F, 0.0F).endVertex();
                var30.vertex((double)var21, (double)var24, -0.5).uv((double)var11, (double)var14).normal(-1.0F, 0.0F, 0.0F).endVertex();
                var30.vertex((double)var21, (double)var23, -0.5).uv((double)var11, (double)var13).normal(-1.0F, 0.0F, 0.0F).endVertex();
                var30.vertex((double)var22, (double)var23, -0.5).uv((double)var12, (double)var13).normal(1.0F, 0.0F, 0.0F).endVertex();
                var30.vertex((double)var22, (double)var24, -0.5).uv((double)var12, (double)var14).normal(1.0F, 0.0F, 0.0F).endVertex();
                var30.vertex((double)var22, (double)var24, 0.5).uv((double)var11, (double)var14).normal(1.0F, 0.0F, 0.0F).endVertex();
                var30.vertex((double)var22, (double)var23, 0.5).uv((double)var11, (double)var13).normal(1.0F, 0.0F, 0.0F).endVertex();
                var29.end();
            }
        }

    }

    private void setBrightness(Painting param0, float param1, float param2) {
        int var0 = Mth.floor(param0.x);
        int var1 = Mth.floor(param0.y + (double)(param2 / 16.0F));
        int var2 = Mth.floor(param0.z);
        Direction var3 = param0.getDirection();
        if (var3 == Direction.NORTH) {
            var0 = Mth.floor(param0.x + (double)(param1 / 16.0F));
        }

        if (var3 == Direction.WEST) {
            var2 = Mth.floor(param0.z - (double)(param1 / 16.0F));
        }

        if (var3 == Direction.SOUTH) {
            var0 = Mth.floor(param0.x - (double)(param1 / 16.0F));
        }

        if (var3 == Direction.EAST) {
            var2 = Mth.floor(param0.z + (double)(param1 / 16.0F));
        }

        int var4 = this.entityRenderDispatcher.level.getLightColor(new BlockPos(var0, var1, var2));
        int var5 = var4 % 65536;
        int var6 = var4 / 65536;
        RenderSystem.glMultiTexCoord2f(33985, (float)var5, (float)var6);
        RenderSystem.color3f(1.0F, 1.0F, 1.0F);
    }
}
