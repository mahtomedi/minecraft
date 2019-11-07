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

    public void render(FishingHook param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        Player var0 = param0.getOwner();
        if (var0 != null) {
            param3.pushPose();
            param3.pushPose();
            param3.scale(0.5F, 0.5F, 0.5F);
            param3.mulPose(Vector3f.YP.rotationDegrees(180.0F - this.entityRenderDispatcher.playerRotY));
            float var1 = (float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * -this.entityRenderDispatcher.playerRotX;
            param3.mulPose(Vector3f.XP.rotationDegrees(var1));
            PoseStack.Pose var2 = param3.last();
            Matrix4f var3 = var2.pose();
            Matrix3f var4 = var2.normal();
            VertexConsumer var5 = param4.getBuffer(RenderType.entityCutout(TEXTURE_LOCATION));
            vertex(var5, var3, var4, param5, 0.0F, 0, 0, 1);
            vertex(var5, var3, var4, param5, 1.0F, 0, 1, 1);
            vertex(var5, var3, var4, param5, 1.0F, 1, 1, 0);
            vertex(var5, var3, var4, param5, 0.0F, 1, 0, 0);
            param3.popPose();
            int var6 = var0.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
            ItemStack var7 = var0.getMainHandItem();
            if (var7.getItem() != Items.FISHING_ROD) {
                var6 = -var6;
            }

            float var8 = var0.getAttackAnim(param2);
            float var9 = Mth.sin(Mth.sqrt(var8) * (float) Math.PI);
            float var10 = Mth.lerp(param2, var0.yBodyRotO, var0.yBodyRot) * (float) (Math.PI / 180.0);
            double var11 = (double)Mth.sin(var10);
            double var12 = (double)Mth.cos(var10);
            double var13 = (double)var6 * 0.35;
            double var14 = 0.8;
            double var21;
            double var22;
            double var23;
            float var24;
            if ((this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.thirdPersonView <= 0)
                && var0 == Minecraft.getInstance().player) {
                double var19 = this.entityRenderDispatcher.options.fov;
                var19 /= 100.0;
                Vec3 var20 = new Vec3((double)var6 * -0.36 * var19, -0.045 * var19, 0.4);
                var20 = var20.xRot(-Mth.lerp(param2, var0.xRotO, var0.xRot) * (float) (Math.PI / 180.0));
                var20 = var20.yRot(-Mth.lerp(param2, var0.yRotO, var0.yRot) * (float) (Math.PI / 180.0));
                var20 = var20.yRot(var9 * 0.5F);
                var20 = var20.xRot(-var9 * 0.7F);
                var21 = Mth.lerp((double)param2, var0.xo, var0.getX()) + var20.x;
                var22 = Mth.lerp((double)param2, var0.yo, var0.getY()) + var20.y;
                var23 = Mth.lerp((double)param2, var0.zo, var0.getZ()) + var20.z;
                var24 = var0.getEyeHeight();
            } else {
                var21 = Mth.lerp((double)param2, var0.xo, var0.getX()) - var12 * var13 - var11 * 0.8;
                var22 = var0.yo + (double)var0.getEyeHeight() + (var0.getY() - var0.yo) * (double)param2 - 0.45;
                var23 = Mth.lerp((double)param2, var0.zo, var0.getZ()) - var11 * var13 + var12 * 0.8;
                var24 = var0.isCrouching() ? -0.1875F : 0.0F;
            }

            double var25 = Mth.lerp((double)param2, param0.xo, param0.getX());
            double var26 = Mth.lerp((double)param2, param0.yo, param0.getY()) + 0.25;
            double var27 = Mth.lerp((double)param2, param0.zo, param0.getZ());
            float var28 = (float)(var21 - var25);
            float var29 = (float)(var22 - var26) + var24;
            float var30 = (float)(var23 - var27);
            VertexConsumer var31 = param4.getBuffer(RenderType.lines());
            Matrix4f var32 = param3.last().pose();
            int var33 = 16;

            for(int var34 = 0; var34 < 16; ++var34) {
                stringVertex(var28, var29, var30, var31, var32, (float)(var34 / 16));
                stringVertex(var28, var29, var30, var31, var32, (float)((var34 + 1) / 16));
            }

            param3.popPose();
            super.render(param0, param1, param2, param3, param4, param5);
        }
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

    private static void stringVertex(float param0, float param1, float param2, VertexConsumer param3, Matrix4f param4, float param5) {
        param3.vertex(param4, param0 * param5, param1 * (param5 * param5 + param5) * 0.5F + 0.25F, param2 * param5).color(0, 0, 0, 255).endVertex();
    }

    public ResourceLocation getTextureLocation(FishingHook param0) {
        return TEXTURE_LOCATION;
    }
}
