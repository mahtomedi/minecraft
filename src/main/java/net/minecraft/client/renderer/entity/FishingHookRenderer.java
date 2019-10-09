package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.fishing.FishingHook;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FishingHookRenderer extends EntityRenderer<FishingHook> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/fishing_hook.png");

    public FishingHookRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(FishingHook param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7) {
        Player var0 = param0.getOwner();
        if (var0 != null) {
            param6.pushPose();
            param6.pushPose();
            param6.scale(0.5F, 0.5F, 0.5F);
            float var1 = 1.0F;
            float var2 = 0.5F;
            float var3 = 0.5F;
            param6.mulPose(Vector3f.YP.rotationDegrees(180.0F - this.entityRenderDispatcher.playerRotY));
            float var4 = (float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * -this.entityRenderDispatcher.playerRotX;
            param6.mulPose(Vector3f.XP.rotationDegrees(var4));
            Matrix4f var5 = param6.getPose();
            VertexConsumer var6 = param7.getBuffer(RenderType.entityCutout(TEXTURE_LOCATION));
            int var7 = param0.getLightColor();
            var6.vertex(var5, -0.5F, -0.5F, 0.0F)
                .color(255, 255, 255, 255)
                .uv(0.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(var7)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
            var6.vertex(var5, 0.5F, -0.5F, 0.0F)
                .color(255, 255, 255, 255)
                .uv(1.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(var7)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
            var6.vertex(var5, 0.5F, 0.5F, 0.0F)
                .color(255, 255, 255, 255)
                .uv(1.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(var7)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
            var6.vertex(var5, -0.5F, 0.5F, 0.0F)
                .color(255, 255, 255, 255)
                .uv(0.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(var7)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
            param6.popPose();
            int var8 = var0.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
            ItemStack var9 = var0.getMainHandItem();
            if (var9.getItem() != Items.FISHING_ROD) {
                var8 = -var8;
            }

            float var10 = var0.getAttackAnim(param5);
            float var11 = Mth.sin(Mth.sqrt(var10) * (float) Math.PI);
            float var12 = Mth.lerp(param5, var0.yBodyRotO, var0.yBodyRot) * (float) (Math.PI / 180.0);
            double var13 = (double)Mth.sin(var12);
            double var14 = (double)Mth.cos(var12);
            double var15 = (double)var8 * 0.35;
            double var16 = 0.8;
            double var23;
            double var24;
            double var25;
            float var26;
            if ((this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.thirdPersonView <= 0)
                && var0 == Minecraft.getInstance().player) {
                double var21 = this.entityRenderDispatcher.options.fov;
                var21 /= 100.0;
                Vec3 var22 = new Vec3((double)var8 * -0.36 * var21, -0.045 * var21, 0.4);
                var22 = var22.xRot(-Mth.lerp(param5, var0.xRotO, var0.xRot) * (float) (Math.PI / 180.0));
                var22 = var22.yRot(-Mth.lerp(param5, var0.yRotO, var0.yRot) * (float) (Math.PI / 180.0));
                var22 = var22.yRot(var11 * 0.5F);
                var22 = var22.xRot(-var11 * 0.7F);
                var23 = Mth.lerp((double)param5, var0.xo, var0.getX()) + var22.x;
                var24 = Mth.lerp((double)param5, var0.yo, var0.getY()) + var22.y;
                var25 = Mth.lerp((double)param5, var0.zo, var0.getZ()) + var22.z;
                var26 = var0.getEyeHeight();
            } else {
                var23 = Mth.lerp((double)param5, var0.xo, var0.getX()) - var14 * var15 - var13 * 0.8;
                var24 = var0.yo + (double)var0.getEyeHeight() + (var0.getY() - var0.yo) * (double)param5 - 0.45;
                var25 = Mth.lerp((double)param5, var0.zo, var0.getZ()) - var13 * var15 + var14 * 0.8;
                var26 = var0.isCrouching() ? -0.1875F : 0.0F;
            }

            double var27 = Mth.lerp((double)param5, param0.xo, param0.getX());
            double var28 = Mth.lerp((double)param5, param0.yo, param0.getY()) + 0.25;
            double var29 = Mth.lerp((double)param5, param0.zo, param0.getZ());
            float var30 = (float)(var23 - var27);
            float var31 = (float)(var24 - var28) + var26;
            float var32 = (float)(var25 - var29);
            VertexConsumer var33 = param7.getBuffer(RenderType.lines());
            Matrix4f var34 = param6.getPose();
            int var35 = 16;

            for(int var36 = 0; var36 < 16; ++var36) {
                stringVertex(var30, var31, var32, var33, var34, (float)(var36 / 16));
                stringVertex(var30, var31, var32, var33, var34, (float)((var36 + 1) / 16));
            }

            param6.popPose();
            super.render(param0, param1, param2, param3, param4, param5, param6, param7);
        }
    }

    private static void stringVertex(float param0, float param1, float param2, VertexConsumer param3, Matrix4f param4, float param5) {
        param3.vertex(param4, param0 * param5, param1 * (param5 * param5 + param5) * 0.5F + 0.25F, param2 * param5).color(0, 0, 0, 255).endVertex();
    }

    public ResourceLocation getTextureLocation(FishingHook param0) {
        return TEXTURE_LOCATION;
    }
}
