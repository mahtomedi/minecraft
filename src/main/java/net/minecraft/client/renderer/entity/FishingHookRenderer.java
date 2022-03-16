package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FishingHookRenderer extends EntityRenderer<FishingHook> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/fishing_hook.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE_LOCATION);
    private static final double VIEW_BOBBING_SCALE = 960.0;

    public FishingHookRenderer(EntityRendererProvider.Context param0) {
        super(param0);
    }

    public void render(FishingHook param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        Player var0 = param0.getPlayerOwner();
        if (var0 != null) {
            param3.pushPose();
            param3.pushPose();
            param3.scale(0.5F, 0.5F, 0.5F);
            param3.mulPose(this.entityRenderDispatcher.cameraOrientation());
            param3.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            PoseStack.Pose var1 = param3.last();
            Matrix4f var2 = var1.pose();
            Matrix3f var3 = var1.normal();
            VertexConsumer var4 = param4.getBuffer(RENDER_TYPE);
            vertex(var4, var2, var3, param5, 0.0F, 0, 0, 1);
            vertex(var4, var2, var3, param5, 1.0F, 0, 1, 1);
            vertex(var4, var2, var3, param5, 1.0F, 1, 1, 0);
            vertex(var4, var2, var3, param5, 0.0F, 1, 0, 0);
            param3.popPose();
            int var5 = var0.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
            ItemStack var6 = var0.getMainHandItem();
            if (!var6.is(Items.FISHING_ROD)) {
                var5 = -var5;
            }

            float var7 = var0.getAttackAnim(param2);
            float var8 = Mth.sin(Mth.sqrt(var7) * (float) Math.PI);
            float var9 = Mth.lerp(param2, var0.yBodyRotO, var0.yBodyRot) * (float) (Math.PI / 180.0);
            double var10 = (double)Mth.sin(var9);
            double var11 = (double)Mth.cos(var9);
            double var12 = (double)var5 * 0.35;
            double var13 = 0.8;
            double var20;
            double var21;
            double var22;
            float var23;
            if ((this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.getCameraType().isFirstPerson())
                && var0 == Minecraft.getInstance().player) {
                double var18 = 960.0 / (double)this.entityRenderDispatcher.options.fov().get().intValue();
                Vec3 var19 = this.entityRenderDispatcher.camera.getNearPlane().getPointOnPlane((float)var5 * 0.525F, -0.1F);
                var19 = var19.scale(var18);
                var19 = var19.yRot(var8 * 0.5F);
                var19 = var19.xRot(-var8 * 0.7F);
                var20 = Mth.lerp((double)param2, var0.xo, var0.getX()) + var19.x;
                var21 = Mth.lerp((double)param2, var0.yo, var0.getY()) + var19.y;
                var22 = Mth.lerp((double)param2, var0.zo, var0.getZ()) + var19.z;
                var23 = var0.getEyeHeight();
            } else {
                var20 = Mth.lerp((double)param2, var0.xo, var0.getX()) - var11 * var12 - var10 * 0.8;
                var21 = var0.yo + (double)var0.getEyeHeight() + (var0.getY() - var0.yo) * (double)param2 - 0.45;
                var22 = Mth.lerp((double)param2, var0.zo, var0.getZ()) - var10 * var12 + var11 * 0.8;
                var23 = var0.isCrouching() ? -0.1875F : 0.0F;
            }

            double var24 = Mth.lerp((double)param2, param0.xo, param0.getX());
            double var25 = Mth.lerp((double)param2, param0.yo, param0.getY()) + 0.25;
            double var26 = Mth.lerp((double)param2, param0.zo, param0.getZ());
            float var27 = (float)(var20 - var24);
            float var28 = (float)(var21 - var25) + var23;
            float var29 = (float)(var22 - var26);
            VertexConsumer var30 = param4.getBuffer(RenderType.lineStrip());
            PoseStack.Pose var31 = param3.last();
            int var32 = 16;

            for(int var33 = 0; var33 <= 16; ++var33) {
                stringVertex(var27, var28, var29, var30, var31, fraction(var33, 16), fraction(var33 + 1, 16));
            }

            param3.popPose();
            super.render(param0, param1, param2, param3, param4, param5);
        }
    }

    private static float fraction(int param0, int param1) {
        return (float)param0 / (float)param1;
    }

    private static void vertex(VertexConsumer param0, Matrix4f param1, Matrix3f param2, int param3, float param4, int param5, int param6, int param7) {
        param0.vertex(param1, param4 - 0.5F, (float)param5 - 0.5F, 0.0F)
            .color(255, 255, 255, 255)
            .uv((float)param6, (float)param7)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(param3)
            .normal(param2, 0.0F, 1.0F, 0.0F)
            .endVertex();
    }

    private static void stringVertex(float param0, float param1, float param2, VertexConsumer param3, PoseStack.Pose param4, float param5, float param6) {
        float var0 = param0 * param5;
        float var1 = param1 * (param5 * param5 + param5) * 0.5F + 0.25F;
        float var2 = param2 * param5;
        float var3 = param0 * param6 - var0;
        float var4 = param1 * (param6 * param6 + param6) * 0.5F + 0.25F - var1;
        float var5 = param2 * param6 - var2;
        float var6 = Mth.sqrt(var3 * var3 + var4 * var4 + var5 * var5);
        var3 /= var6;
        var4 /= var6;
        var5 /= var6;
        param3.vertex(param4.pose(), var0, var1, var2).color(0, 0, 0, 255).normal(param4.normal(), var3, var4, var5).endVertex();
    }

    public ResourceLocation getTextureLocation(FishingHook param0) {
        return TEXTURE_LOCATION;
    }
}
