package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.GuardianModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class GuardianRenderer extends MobRenderer<Guardian, GuardianModel> {
    private static final ResourceLocation GUARDIAN_LOCATION = new ResourceLocation("textures/entity/guardian.png");
    private static final ResourceLocation GUARDIAN_BEAM_LOCATION = new ResourceLocation("textures/entity/guardian_beam.png");
    private static final RenderType BEAM_RENDER_TYPE = RenderType.entityCutoutNoCull(GUARDIAN_BEAM_LOCATION);

    public GuardianRenderer(EntityRendererProvider.Context param0) {
        this(param0, 0.5F, ModelLayers.GUARDIAN);
    }

    protected GuardianRenderer(EntityRendererProvider.Context param0, float param1, ModelLayerLocation param2) {
        super(param0, new GuardianModel(param0.bakeLayer(param2)), param1);
    }

    public boolean shouldRender(Guardian param0, Frustum param1, double param2, double param3, double param4) {
        if (super.shouldRender(param0, param1, param2, param3, param4)) {
            return true;
        } else {
            if (param0.hasActiveAttackTarget()) {
                LivingEntity var0 = param0.getActiveAttackTarget();
                if (var0 != null) {
                    Vec3 var1 = this.getPosition(var0, (double)var0.getBbHeight() * 0.5, 1.0F);
                    Vec3 var2 = this.getPosition(param0, (double)param0.getEyeHeight(), 1.0F);
                    return param1.isVisible(new AABB(var2.x, var2.y, var2.z, var1.x, var1.y, var1.z));
                }
            }

            return false;
        }
    }

    private Vec3 getPosition(LivingEntity param0, double param1, float param2) {
        double var0 = Mth.lerp((double)param2, param0.xOld, param0.getX());
        double var1 = Mth.lerp((double)param2, param0.yOld, param0.getY()) + param1;
        double var2 = Mth.lerp((double)param2, param0.zOld, param0.getZ());
        return new Vec3(var0, var1, var2);
    }

    public void render(Guardian param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        super.render(param0, param1, param2, param3, param4, param5);
        LivingEntity var0 = param0.getActiveAttackTarget();
        if (var0 != null) {
            float var1 = param0.getAttackAnimationScale(param2);
            float var2 = param0.getClientSideAttackTime() + param2;
            float var3 = var2 * 0.5F % 1.0F;
            float var4 = param0.getEyeHeight();
            param3.pushPose();
            param3.translate(0.0F, var4, 0.0F);
            Vec3 var5 = this.getPosition(var0, (double)var0.getBbHeight() * 0.5, param2);
            Vec3 var6 = this.getPosition(param0, (double)var4, param2);
            Vec3 var7 = var5.subtract(var6);
            float var8 = (float)(var7.length() + 1.0);
            var7 = var7.normalize();
            float var9 = (float)Math.acos(var7.y);
            float var10 = (float)Math.atan2(var7.z, var7.x);
            param3.mulPose(Axis.YP.rotationDegrees(((float) (Math.PI / 2) - var10) * (180.0F / (float)Math.PI)));
            param3.mulPose(Axis.XP.rotationDegrees(var9 * (180.0F / (float)Math.PI)));
            int var11 = 1;
            float var12 = var2 * 0.05F * -1.5F;
            float var13 = var1 * var1;
            int var14 = 64 + (int)(var13 * 191.0F);
            int var15 = 32 + (int)(var13 * 191.0F);
            int var16 = 128 - (int)(var13 * 64.0F);
            float var17 = 0.2F;
            float var18 = 0.282F;
            float var19 = Mth.cos(var12 + (float) (Math.PI * 3.0 / 4.0)) * 0.282F;
            float var20 = Mth.sin(var12 + (float) (Math.PI * 3.0 / 4.0)) * 0.282F;
            float var21 = Mth.cos(var12 + (float) (Math.PI / 4)) * 0.282F;
            float var22 = Mth.sin(var12 + (float) (Math.PI / 4)) * 0.282F;
            float var23 = Mth.cos(var12 + ((float) Math.PI * 5.0F / 4.0F)) * 0.282F;
            float var24 = Mth.sin(var12 + ((float) Math.PI * 5.0F / 4.0F)) * 0.282F;
            float var25 = Mth.cos(var12 + ((float) Math.PI * 7.0F / 4.0F)) * 0.282F;
            float var26 = Mth.sin(var12 + ((float) Math.PI * 7.0F / 4.0F)) * 0.282F;
            float var27 = Mth.cos(var12 + (float) Math.PI) * 0.2F;
            float var28 = Mth.sin(var12 + (float) Math.PI) * 0.2F;
            float var29 = Mth.cos(var12 + 0.0F) * 0.2F;
            float var30 = Mth.sin(var12 + 0.0F) * 0.2F;
            float var31 = Mth.cos(var12 + (float) (Math.PI / 2)) * 0.2F;
            float var32 = Mth.sin(var12 + (float) (Math.PI / 2)) * 0.2F;
            float var33 = Mth.cos(var12 + (float) (Math.PI * 3.0 / 2.0)) * 0.2F;
            float var34 = Mth.sin(var12 + (float) (Math.PI * 3.0 / 2.0)) * 0.2F;
            float var36 = 0.0F;
            float var37 = 0.4999F;
            float var38 = -1.0F + var3;
            float var39 = var8 * 2.5F + var38;
            VertexConsumer var40 = param4.getBuffer(BEAM_RENDER_TYPE);
            PoseStack.Pose var41 = param3.last();
            Matrix4f var42 = var41.pose();
            Matrix3f var43 = var41.normal();
            vertex(var40, var42, var43, var27, var8, var28, var14, var15, var16, 0.4999F, var39);
            vertex(var40, var42, var43, var27, 0.0F, var28, var14, var15, var16, 0.4999F, var38);
            vertex(var40, var42, var43, var29, 0.0F, var30, var14, var15, var16, 0.0F, var38);
            vertex(var40, var42, var43, var29, var8, var30, var14, var15, var16, 0.0F, var39);
            vertex(var40, var42, var43, var31, var8, var32, var14, var15, var16, 0.4999F, var39);
            vertex(var40, var42, var43, var31, 0.0F, var32, var14, var15, var16, 0.4999F, var38);
            vertex(var40, var42, var43, var33, 0.0F, var34, var14, var15, var16, 0.0F, var38);
            vertex(var40, var42, var43, var33, var8, var34, var14, var15, var16, 0.0F, var39);
            float var44 = 0.0F;
            if (param0.tickCount % 2 == 0) {
                var44 = 0.5F;
            }

            vertex(var40, var42, var43, var19, var8, var20, var14, var15, var16, 0.5F, var44 + 0.5F);
            vertex(var40, var42, var43, var21, var8, var22, var14, var15, var16, 1.0F, var44 + 0.5F);
            vertex(var40, var42, var43, var25, var8, var26, var14, var15, var16, 1.0F, var44);
            vertex(var40, var42, var43, var23, var8, var24, var14, var15, var16, 0.5F, var44);
            param3.popPose();
        }

    }

    private static void vertex(
        VertexConsumer param0,
        Matrix4f param1,
        Matrix3f param2,
        float param3,
        float param4,
        float param5,
        int param6,
        int param7,
        int param8,
        float param9,
        float param10
    ) {
        param0.vertex(param1, param3, param4, param5)
            .color(param6, param7, param8, 255)
            .uv(param9, param10)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(15728880)
            .normal(param2, 0.0F, 1.0F, 0.0F)
            .endVertex();
    }

    public ResourceLocation getTextureLocation(Guardian param0) {
        return GUARDIAN_LOCATION;
    }
}
