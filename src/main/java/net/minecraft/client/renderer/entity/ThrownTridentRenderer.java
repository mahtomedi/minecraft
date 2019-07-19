package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.model.TridentModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThrownTridentRenderer extends EntityRenderer<ThrownTrident> {
    public static final ResourceLocation TRIDENT_LOCATION = new ResourceLocation("textures/entity/trident.png");
    private final TridentModel model = new TridentModel();

    public ThrownTridentRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(ThrownTrident param0, double param1, double param2, double param3, float param4, float param5) {
        this.bindTexture(param0);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.translatef((float)param1, (float)param2, (float)param3);
        GlStateManager.rotatef(Mth.lerp(param5, param0.yRotO, param0.yRot) - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(Mth.lerp(param5, param0.xRotO, param0.xRot) + 90.0F, 0.0F, 0.0F, 1.0F);
        this.model.render();
        GlStateManager.popMatrix();
        this.renderLeash(param0, param1, param2, param3, param4, param5);
        super.render(param0, param1, param2, param3, param4, param5);
        GlStateManager.enableLighting();
    }

    protected ResourceLocation getTextureLocation(ThrownTrident param0) {
        return TRIDENT_LOCATION;
    }

    protected void renderLeash(ThrownTrident param0, double param1, double param2, double param3, float param4, float param5) {
        Entity var0 = param0.getOwner();
        if (var0 != null && param0.isNoPhysics()) {
            Tesselator var1 = Tesselator.getInstance();
            BufferBuilder var2 = var1.getBuilder();
            double var3 = (double)(Mth.lerp(param5 * 0.5F, var0.yRot, var0.yRotO) * (float) (Math.PI / 180.0));
            double var4 = Math.cos(var3);
            double var5 = Math.sin(var3);
            double var6 = Mth.lerp((double)param5, var0.xo, var0.x);
            double var7 = Mth.lerp((double)param5, var0.yo + (double)var0.getEyeHeight() * 0.8, var0.y + (double)var0.getEyeHeight() * 0.8);
            double var8 = Mth.lerp((double)param5, var0.zo, var0.z);
            double var9 = var4 - var5;
            double var10 = var5 + var4;
            double var11 = Mth.lerp((double)param5, param0.xo, param0.x);
            double var12 = Mth.lerp((double)param5, param0.yo, param0.y);
            double var13 = Mth.lerp((double)param5, param0.zo, param0.z);
            double var14 = (double)((float)(var6 - var11));
            double var15 = (double)((float)(var7 - var12));
            double var16 = (double)((float)(var8 - var13));
            double var17 = Math.sqrt(var14 * var14 + var15 * var15 + var16 * var16);
            int var18 = param0.getId() + param0.tickCount;
            double var19 = (double)((float)var18 + param5) * -0.1;
            double var20 = Math.min(0.5, var17 / 30.0);
            GlStateManager.disableTexture();
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 255.0F, 255.0F);
            var2.begin(5, DefaultVertexFormat.POSITION_COLOR);
            int var21 = 37;
            int var22 = 7 - var18 % 7;
            double var23 = 0.1;

            for(int var24 = 0; var24 <= 37; ++var24) {
                double var25 = (double)var24 / 37.0;
                float var26 = 1.0F - (float)((var24 + var22) % 7) / 7.0F;
                double var27 = var25 * 2.0 - 1.0;
                var27 = (1.0 - var27 * var27) * var20;
                double var28 = param1 + var14 * var25 + Math.sin(var25 * Math.PI * 8.0 + var19) * var9 * var27;
                double var29 = param2 + var15 * var25 + Math.cos(var25 * Math.PI * 8.0 + var19) * 0.02 + (0.1 + var27) * 1.0;
                double var30 = param3 + var16 * var25 + Math.sin(var25 * Math.PI * 8.0 + var19) * var10 * var27;
                float var31 = 0.87F * var26 + 0.3F * (1.0F - var26);
                float var32 = 0.91F * var26 + 0.6F * (1.0F - var26);
                float var33 = 0.85F * var26 + 0.5F * (1.0F - var26);
                var2.vertex(var28, var29, var30).color(var31, var32, var33, 1.0F).endVertex();
                var2.vertex(var28 + 0.1 * var27, var29 + 0.1 * var27, var30).color(var31, var32, var33, 1.0F).endVertex();
                if (var24 > param0.clientSideReturnTridentTickCount * 2) {
                    break;
                }
            }

            var1.end();
            var2.begin(5, DefaultVertexFormat.POSITION_COLOR);

            for(int var34 = 0; var34 <= 37; ++var34) {
                double var35 = (double)var34 / 37.0;
                float var36 = 1.0F - (float)((var34 + var22) % 7) / 7.0F;
                double var37 = var35 * 2.0 - 1.0;
                var37 = (1.0 - var37 * var37) * var20;
                double var38 = param1 + var14 * var35 + Math.sin(var35 * Math.PI * 8.0 + var19) * var9 * var37;
                double var39 = param2 + var15 * var35 + Math.cos(var35 * Math.PI * 8.0 + var19) * 0.01 + (0.1 + var37) * 1.0;
                double var40 = param3 + var16 * var35 + Math.sin(var35 * Math.PI * 8.0 + var19) * var10 * var37;
                float var41 = 0.87F * var36 + 0.3F * (1.0F - var36);
                float var42 = 0.91F * var36 + 0.6F * (1.0F - var36);
                float var43 = 0.85F * var36 + 0.5F * (1.0F - var36);
                var2.vertex(var38, var39, var40).color(var41, var42, var43, 1.0F).endVertex();
                var2.vertex(var38 + 0.1 * var37, var39, var40 + 0.1 * var37).color(var41, var42, var43, 1.0F).endVertex();
                if (var34 > param0.clientSideReturnTridentTickCount * 2) {
                    break;
                }
            }

            var1.end();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture();
            GlStateManager.enableCull();
        }
    }
}
