package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.model.GuardianModel;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuardianRenderer extends MobRenderer<Guardian, GuardianModel> {
    private static final ResourceLocation GUARDIAN_LOCATION = new ResourceLocation("textures/entity/guardian.png");
    private static final ResourceLocation GUARDIAN_BEAM_LOCATION = new ResourceLocation("textures/entity/guardian_beam.png");

    public GuardianRenderer(EntityRenderDispatcher param0) {
        this(param0, 0.5F);
    }

    protected GuardianRenderer(EntityRenderDispatcher param0, float param1) {
        super(param0, new GuardianModel(), param1);
    }

    public boolean shouldRender(Guardian param0, Culler param1, double param2, double param3, double param4) {
        if (super.shouldRender(param0, param1, param2, param3, param4)) {
            return true;
        } else {
            if (param0.hasActiveAttackTarget()) {
                LivingEntity var0 = param0.getActiveAttackTarget();
                if (var0 != null) {
                    Vec3 var1 = this.getPosition(var0, (double)var0.getBbHeight() * 0.5, 1.0F);
                    Vec3 var2 = this.getPosition(param0, (double)param0.getEyeHeight(), 1.0F);
                    if (param1.isVisible(new AABB(var2.x, var2.y, var2.z, var1.x, var1.y, var1.z))) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    private Vec3 getPosition(LivingEntity param0, double param1, float param2) {
        double var0 = Mth.lerp((double)param2, param0.xOld, param0.x);
        double var1 = Mth.lerp((double)param2, param0.yOld, param0.y) + param1;
        double var2 = Mth.lerp((double)param2, param0.zOld, param0.z);
        return new Vec3(var0, var1, var2);
    }

    public void render(Guardian param0, double param1, double param2, double param3, float param4, float param5) {
        super.render(param0, param1, param2, param3, param4, param5);
        LivingEntity var0 = param0.getActiveAttackTarget();
        if (var0 != null) {
            float var1 = param0.getAttackAnimationScale(param5);
            Tesselator var2 = Tesselator.getInstance();
            BufferBuilder var3 = var2.getBuilder();
            this.bindTexture(GUARDIAN_BEAM_LOCATION);
            RenderSystem.texParameter(3553, 10242, 10497);
            RenderSystem.texParameter(3553, 10243, 10497);
            RenderSystem.disableLighting();
            RenderSystem.disableCull();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            float var4 = 240.0F;
            RenderSystem.glMultiTexCoord2f(33985, 240.0F, 240.0F);
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
            );
            float var5 = (float)param0.level.getGameTime() + param5;
            float var6 = var5 * 0.5F % 1.0F;
            float var7 = param0.getEyeHeight();
            RenderSystem.pushMatrix();
            RenderSystem.translatef((float)param1, (float)param2 + var7, (float)param3);
            Vec3 var8 = this.getPosition(var0, (double)var0.getBbHeight() * 0.5, param5);
            Vec3 var9 = this.getPosition(param0, (double)var7, param5);
            Vec3 var10 = var8.subtract(var9);
            double var11 = var10.length() + 1.0;
            var10 = var10.normalize();
            float var12 = (float)Math.acos(var10.y);
            float var13 = (float)Math.atan2(var10.z, var10.x);
            RenderSystem.rotatef(((float) (Math.PI / 2) - var13) * (180.0F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
            RenderSystem.rotatef(var12 * (180.0F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
            int var14 = 1;
            double var15 = (double)var5 * 0.05 * -1.5;
            var3.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
            float var16 = var1 * var1;
            int var17 = 64 + (int)(var16 * 191.0F);
            int var18 = 32 + (int)(var16 * 191.0F);
            int var19 = 128 - (int)(var16 * 64.0F);
            double var20 = 0.2;
            double var21 = 0.282;
            double var22 = 0.0 + Math.cos(var15 + (Math.PI * 3.0 / 4.0)) * 0.282;
            double var23 = 0.0 + Math.sin(var15 + (Math.PI * 3.0 / 4.0)) * 0.282;
            double var24 = 0.0 + Math.cos(var15 + (Math.PI / 4)) * 0.282;
            double var25 = 0.0 + Math.sin(var15 + (Math.PI / 4)) * 0.282;
            double var26 = 0.0 + Math.cos(var15 + (Math.PI * 5.0 / 4.0)) * 0.282;
            double var27 = 0.0 + Math.sin(var15 + (Math.PI * 5.0 / 4.0)) * 0.282;
            double var28 = 0.0 + Math.cos(var15 + (Math.PI * 7.0 / 4.0)) * 0.282;
            double var29 = 0.0 + Math.sin(var15 + (Math.PI * 7.0 / 4.0)) * 0.282;
            double var30 = 0.0 + Math.cos(var15 + Math.PI) * 0.2;
            double var31 = 0.0 + Math.sin(var15 + Math.PI) * 0.2;
            double var32 = 0.0 + Math.cos(var15 + 0.0) * 0.2;
            double var33 = 0.0 + Math.sin(var15 + 0.0) * 0.2;
            double var34 = 0.0 + Math.cos(var15 + (Math.PI / 2)) * 0.2;
            double var35 = 0.0 + Math.sin(var15 + (Math.PI / 2)) * 0.2;
            double var36 = 0.0 + Math.cos(var15 + (Math.PI * 3.0 / 2.0)) * 0.2;
            double var37 = 0.0 + Math.sin(var15 + (Math.PI * 3.0 / 2.0)) * 0.2;
            double var39 = 0.0;
            double var40 = 0.4999;
            double var41 = (double)(-1.0F + var6);
            double var42 = var11 * 2.5 + var41;
            var3.vertex(var30, var11, var31).uv(0.4999, var42).color(var17, var18, var19, 255).endVertex();
            var3.vertex(var30, 0.0, var31).uv(0.4999, var41).color(var17, var18, var19, 255).endVertex();
            var3.vertex(var32, 0.0, var33).uv(0.0, var41).color(var17, var18, var19, 255).endVertex();
            var3.vertex(var32, var11, var33).uv(0.0, var42).color(var17, var18, var19, 255).endVertex();
            var3.vertex(var34, var11, var35).uv(0.4999, var42).color(var17, var18, var19, 255).endVertex();
            var3.vertex(var34, 0.0, var35).uv(0.4999, var41).color(var17, var18, var19, 255).endVertex();
            var3.vertex(var36, 0.0, var37).uv(0.0, var41).color(var17, var18, var19, 255).endVertex();
            var3.vertex(var36, var11, var37).uv(0.0, var42).color(var17, var18, var19, 255).endVertex();
            double var43 = 0.0;
            if (param0.tickCount % 2 == 0) {
                var43 = 0.5;
            }

            var3.vertex(var22, var11, var23).uv(0.5, var43 + 0.5).color(var17, var18, var19, 255).endVertex();
            var3.vertex(var24, var11, var25).uv(1.0, var43 + 0.5).color(var17, var18, var19, 255).endVertex();
            var3.vertex(var28, var11, var29).uv(1.0, var43).color(var17, var18, var19, 255).endVertex();
            var3.vertex(var26, var11, var27).uv(0.5, var43).color(var17, var18, var19, 255).endVertex();
            var2.end();
            RenderSystem.popMatrix();
        }

    }

    protected ResourceLocation getTextureLocation(Guardian param0) {
        return GUARDIAN_LOCATION;
    }
}
