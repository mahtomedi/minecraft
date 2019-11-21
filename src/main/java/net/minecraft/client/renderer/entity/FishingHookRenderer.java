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
    private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE_LOCATION);

    public FishingHookRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(FishingHook param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        Player var0 = param0.getOwner();
        if (var0 != null) {
            param3.pushPose();
            param3.pushPose();
            param3.scale(0.5F, 0.5F, 0.5F);
            param3.mulPose(this.entityRenderDispatcher.camera.rotation());
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
            if (var6.getItem() != Items.FISHING_ROD) {
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
            if ((this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.thirdPersonView <= 0)
                && var0 == Minecraft.getInstance().player) {
                double var18 = this.entityRenderDispatcher.options.fov;
                var18 /= 100.0;
                Vec3 var19 = new Vec3((double)var5 * -0.36 * var18, -0.045 * var18, 0.4);
                var19 = var19.xRot(-Mth.lerp(param2, var0.xRotO, var0.xRot) * (float) (Math.PI / 180.0));
                var19 = var19.yRot(-Mth.lerp(param2, var0.yRotO, var0.yRot) * (float) (Math.PI / 180.0));
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
            VertexConsumer var30 = param4.getBuffer(RenderType.lines());
            Matrix4f var31 = param3.last().pose();
            int var32 = 16;

            for(int var33 = 0; var33 < 16; ++var33) {
                stringVertex(var27, var28, var29, var30, var31, fraction(var33, 16));
                stringVertex(var27, var28, var29, var30, var31, fraction(var33 + 1, 16));
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

    private static void stringVertex(float param0, float param1, float param2, VertexConsumer param3, Matrix4f param4, float param5) {
        param3.vertex(param4, param0 * param5, param1 * (param5 * param5 + param5) * 0.5F + 0.25F, param2 * param5).color(0, 0, 0, 255).endVertex();
    }

    public ResourceLocation getTextureLocation(FishingHook param0) {
        return TEXTURE_LOCATION;
    }
}
