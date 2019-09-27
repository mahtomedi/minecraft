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
            param6.mulPose(Vector3f.YP.rotation(180.0F - this.entityRenderDispatcher.playerRotY, true));
            param6.mulPose(
                Vector3f.XP
                    .rotation((float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * -this.entityRenderDispatcher.playerRotX, true)
            );
            Matrix4f var4 = param6.getPose();
            VertexConsumer var5 = param7.getBuffer(RenderType.NEW_ENTITY(TEXTURE_LOCATION));
            OverlayTexture.setDefault(var5);
            int var6 = param0.getLightColor();
            var5.vertex(var4, -0.5F, -0.5F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(var6).normal(0.0F, 1.0F, 0.0F).endVertex();
            var5.vertex(var4, 0.5F, -0.5F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(var6).normal(0.0F, 1.0F, 0.0F).endVertex();
            var5.vertex(var4, 0.5F, 0.5F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(var6).normal(0.0F, 1.0F, 0.0F).endVertex();
            var5.vertex(var4, -0.5F, 0.5F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(var6).normal(0.0F, 1.0F, 0.0F).endVertex();
            param6.popPose();
            var5.unsetDefaultOverlayCoords();
            int var7 = var0.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
            ItemStack var8 = var0.getMainHandItem();
            if (var8.getItem() != Items.FISHING_ROD) {
                var7 = -var7;
            }

            float var9 = var0.getAttackAnim(param5);
            float var10 = Mth.sin(Mth.sqrt(var9) * (float) Math.PI);
            float var11 = Mth.lerp(param5, var0.yBodyRotO, var0.yBodyRot) * (float) (Math.PI / 180.0);
            double var12 = (double)Mth.sin(var11);
            double var13 = (double)Mth.cos(var11);
            double var14 = (double)var7 * 0.35;
            double var15 = 0.8;
            double var22;
            double var23;
            double var24;
            float var25;
            if ((this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.thirdPersonView <= 0)
                && var0 == Minecraft.getInstance().player) {
                double var20 = this.entityRenderDispatcher.options.fov;
                var20 /= 100.0;
                Vec3 var21 = new Vec3((double)var7 * -0.36 * var20, -0.045 * var20, 0.4);
                var21 = var21.xRot(-Mth.lerp(param5, var0.xRotO, var0.xRot) * (float) (Math.PI / 180.0));
                var21 = var21.yRot(-Mth.lerp(param5, var0.yRotO, var0.yRot) * (float) (Math.PI / 180.0));
                var21 = var21.yRot(var10 * 0.5F);
                var21 = var21.xRot(-var10 * 0.7F);
                var22 = Mth.lerp((double)param5, var0.xo, var0.x) + var21.x;
                var23 = Mth.lerp((double)param5, var0.yo, var0.y) + var21.y;
                var24 = Mth.lerp((double)param5, var0.zo, var0.z) + var21.z;
                var25 = var0.getEyeHeight();
            } else {
                var22 = Mth.lerp((double)param5, var0.xo, var0.x) - var13 * var14 - var12 * 0.8;
                var23 = var0.yo + (double)var0.getEyeHeight() + (var0.y - var0.yo) * (double)param5 - 0.45;
                var24 = Mth.lerp((double)param5, var0.zo, var0.z) - var12 * var14 + var13 * 0.8;
                var25 = var0.isCrouching() ? -0.1875F : 0.0F;
            }

            double var26 = Mth.lerp((double)param5, param0.xo, param0.x);
            double var27 = Mth.lerp((double)param5, param0.yo, param0.y) + 0.25;
            double var28 = Mth.lerp((double)param5, param0.zo, param0.z);
            float var29 = (float)(var22 - var26);
            float var30 = (float)(var23 - var27) + var25;
            float var31 = (float)(var24 - var28);
            VertexConsumer var32 = param7.getBuffer(RenderType.LINES);
            Matrix4f var33 = param6.getPose();
            int var34 = 16;

            for(int var35 = 0; var35 < 16; ++var35) {
                stringVertex(var29, var30, var31, var32, var33, (float)(var35 / 16));
                stringVertex(var29, var30, var31, var32, var33, (float)((var35 + 1) / 16));
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
