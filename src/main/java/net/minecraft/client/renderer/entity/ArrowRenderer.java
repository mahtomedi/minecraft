package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ArrowRenderer<T extends AbstractArrow> extends EntityRenderer<T> {
    public ArrowRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(T param0, double param1, double param2, double param3, float param4, float param5) {
        this.bindTexture(param0);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.translatef((float)param1, (float)param2, (float)param3);
        GlStateManager.rotatef(Mth.lerp(param5, param0.yRotO, param0.yRot) - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(Mth.lerp(param5, param0.xRotO, param0.xRot), 0.0F, 0.0F, 1.0F);
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        int var2 = 0;
        float var3 = 0.0F;
        float var4 = 0.5F;
        float var5 = 0.0F;
        float var6 = 0.15625F;
        float var7 = 0.0F;
        float var8 = 0.15625F;
        float var9 = 0.15625F;
        float var10 = 0.3125F;
        float var11 = 0.05625F;
        GlStateManager.enableRescaleNormal();
        float var12 = (float)param0.shakeTime - param5;
        if (var12 > 0.0F) {
            float var13 = -Mth.sin(var12 * 3.0F) * var12;
            GlStateManager.rotatef(var13, 0.0F, 0.0F, 1.0F);
        }

        GlStateManager.rotatef(45.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scalef(0.05625F, 0.05625F, 0.05625F);
        GlStateManager.translatef(-4.0F, 0.0F, 0.0F);
        if (this.solidRender) {
            GlStateManager.enableColorMaterial();
            GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(param0));
        }

        GlStateManager.normal3f(0.05625F, 0.0F, 0.0F);
        var1.begin(7, DefaultVertexFormat.POSITION_TEX);
        var1.vertex(-7.0, -2.0, -2.0).uv(0.0, 0.15625).endVertex();
        var1.vertex(-7.0, -2.0, 2.0).uv(0.15625, 0.15625).endVertex();
        var1.vertex(-7.0, 2.0, 2.0).uv(0.15625, 0.3125).endVertex();
        var1.vertex(-7.0, 2.0, -2.0).uv(0.0, 0.3125).endVertex();
        var0.end();
        GlStateManager.normal3f(-0.05625F, 0.0F, 0.0F);
        var1.begin(7, DefaultVertexFormat.POSITION_TEX);
        var1.vertex(-7.0, 2.0, -2.0).uv(0.0, 0.15625).endVertex();
        var1.vertex(-7.0, 2.0, 2.0).uv(0.15625, 0.15625).endVertex();
        var1.vertex(-7.0, -2.0, 2.0).uv(0.15625, 0.3125).endVertex();
        var1.vertex(-7.0, -2.0, -2.0).uv(0.0, 0.3125).endVertex();
        var0.end();

        for(int var14 = 0; var14 < 4; ++var14) {
            GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.normal3f(0.0F, 0.0F, 0.05625F);
            var1.begin(7, DefaultVertexFormat.POSITION_TEX);
            var1.vertex(-8.0, -2.0, 0.0).uv(0.0, 0.0).endVertex();
            var1.vertex(8.0, -2.0, 0.0).uv(0.5, 0.0).endVertex();
            var1.vertex(8.0, 2.0, 0.0).uv(0.5, 0.15625).endVertex();
            var1.vertex(-8.0, 2.0, 0.0).uv(0.0, 0.15625).endVertex();
            var0.end();
        }

        if (this.solidRender) {
            GlStateManager.tearDownSolidRenderingTextureCombine();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.disableRescaleNormal();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        super.render(param0, param1, param2, param3, param4, param5);
    }
}
