package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ExperienceOrbRenderer extends EntityRenderer<ExperienceOrb> {
    private static final ResourceLocation EXPERIENCE_ORB_LOCATION = new ResourceLocation("textures/entity/experience_orb.png");

    public ExperienceOrbRenderer(EntityRenderDispatcher param0) {
        super(param0);
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    public void render(ExperienceOrb param0, double param1, double param2, double param3, float param4, float param5) {
        if (!this.solidRender && Minecraft.getInstance().getEntityRenderDispatcher().options != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float)param1, (float)param2, (float)param3);
            this.bindTexture(param0);
            Lighting.turnOn();
            int var0 = param0.getIcon();
            float var1 = (float)(var0 % 4 * 16 + 0) / 64.0F;
            float var2 = (float)(var0 % 4 * 16 + 16) / 64.0F;
            float var3 = (float)(var0 / 4 * 16 + 0) / 64.0F;
            float var4 = (float)(var0 / 4 * 16 + 16) / 64.0F;
            float var5 = 1.0F;
            float var6 = 0.5F;
            float var7 = 0.25F;
            int var8 = param0.getLightColor();
            int var9 = var8 % 65536;
            int var10 = var8 / 65536;
            GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float)var9, (float)var10);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            float var11 = 255.0F;
            float var12 = ((float)param0.tickCount + param5) / 2.0F;
            int var13 = (int)((Mth.sin(var12 + 0.0F) + 1.0F) * 0.5F * 255.0F);
            int var14 = 255;
            int var15 = (int)((Mth.sin(var12 + ((float) (Math.PI * 4.0 / 3.0))) + 1.0F) * 0.1F * 255.0F);
            GlStateManager.translatef(0.0F, 0.1F, 0.0F);
            GlStateManager.rotatef(180.0F - this.entityRenderDispatcher.playerRotY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotatef(
                (float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * -this.entityRenderDispatcher.playerRotX, 1.0F, 0.0F, 0.0F
            );
            float var16 = 0.3F;
            GlStateManager.scalef(0.3F, 0.3F, 0.3F);
            Tesselator var17 = Tesselator.getInstance();
            BufferBuilder var18 = var17.getBuilder();
            var18.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
            var18.vertex(-0.5, -0.25, 0.0).uv((double)var1, (double)var4).color(var13, 255, var15, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
            var18.vertex(0.5, -0.25, 0.0).uv((double)var2, (double)var4).color(var13, 255, var15, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
            var18.vertex(0.5, 0.75, 0.0).uv((double)var2, (double)var3).color(var13, 255, var15, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
            var18.vertex(-0.5, 0.75, 0.0).uv((double)var1, (double)var3).color(var13, 255, var15, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
            var17.end();
            GlStateManager.disableBlend();
            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
            super.render(param0, param1, param2, param3, param4, param5);
        }
    }

    protected ResourceLocation getTextureLocation(ExperienceOrb param0) {
        return EXPERIENCE_ORB_LOCATION;
    }
}
