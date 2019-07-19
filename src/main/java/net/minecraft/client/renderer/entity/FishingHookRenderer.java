package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
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

    public void render(FishingHook param0, double param1, double param2, double param3, float param4, float param5) {
        Player var0 = param0.getOwner();
        if (var0 != null && !this.solidRender) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float)param1, (float)param2, (float)param3);
            GlStateManager.enableRescaleNormal();
            GlStateManager.scalef(0.5F, 0.5F, 0.5F);
            this.bindTexture(param0);
            Tesselator var1 = Tesselator.getInstance();
            BufferBuilder var2 = var1.getBuilder();
            float var3 = 1.0F;
            float var4 = 0.5F;
            float var5 = 0.5F;
            GlStateManager.rotatef(180.0F - this.entityRenderDispatcher.playerRotY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotatef(
                (float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * -this.entityRenderDispatcher.playerRotX, 1.0F, 0.0F, 0.0F
            );
            if (this.solidRender) {
                GlStateManager.enableColorMaterial();
                GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(param0));
            }

            var2.begin(7, DefaultVertexFormat.POSITION_TEX_NORMAL);
            var2.vertex(-0.5, -0.5, 0.0).uv(0.0, 1.0).normal(0.0F, 1.0F, 0.0F).endVertex();
            var2.vertex(0.5, -0.5, 0.0).uv(1.0, 1.0).normal(0.0F, 1.0F, 0.0F).endVertex();
            var2.vertex(0.5, 0.5, 0.0).uv(1.0, 0.0).normal(0.0F, 1.0F, 0.0F).endVertex();
            var2.vertex(-0.5, 0.5, 0.0).uv(0.0, 0.0).normal(0.0F, 1.0F, 0.0F).endVertex();
            var1.end();
            if (this.solidRender) {
                GlStateManager.tearDownSolidRenderingTextureCombine();
                GlStateManager.disableColorMaterial();
            }

            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
            int var6 = var0.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
            ItemStack var7 = var0.getMainHandItem();
            if (var7.getItem() != Items.FISHING_ROD) {
                var6 = -var6;
            }

            float var8 = var0.getAttackAnim(param5);
            float var9 = Mth.sin(Mth.sqrt(var8) * (float) Math.PI);
            float var10 = Mth.lerp(param5, var0.yBodyRotO, var0.yBodyRot) * (float) (Math.PI / 180.0);
            double var11 = (double)Mth.sin(var10);
            double var12 = (double)Mth.cos(var10);
            double var13 = (double)var6 * 0.35;
            double var14 = 0.8;
            double var21;
            double var22;
            double var23;
            double var24;
            if ((this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.thirdPersonView <= 0)
                && var0 == Minecraft.getInstance().player) {
                double var19 = this.entityRenderDispatcher.options.fov;
                var19 /= 100.0;
                Vec3 var20 = new Vec3((double)var6 * -0.36 * var19, -0.045 * var19, 0.4);
                var20 = var20.xRot(-Mth.lerp(param5, var0.xRotO, var0.xRot) * (float) (Math.PI / 180.0));
                var20 = var20.yRot(-Mth.lerp(param5, var0.yRotO, var0.yRot) * (float) (Math.PI / 180.0));
                var20 = var20.yRot(var9 * 0.5F);
                var20 = var20.xRot(-var9 * 0.7F);
                var21 = Mth.lerp((double)param5, var0.xo, var0.x) + var20.x;
                var22 = Mth.lerp((double)param5, var0.yo, var0.y) + var20.y;
                var23 = Mth.lerp((double)param5, var0.zo, var0.z) + var20.z;
                var24 = (double)var0.getEyeHeight();
            } else {
                var21 = Mth.lerp((double)param5, var0.xo, var0.x) - var12 * var13 - var11 * 0.8;
                var22 = var0.yo + (double)var0.getEyeHeight() + (var0.y - var0.yo) * (double)param5 - 0.45;
                var23 = Mth.lerp((double)param5, var0.zo, var0.z) - var11 * var13 + var12 * 0.8;
                var24 = var0.isVisuallySneaking() ? -0.1875 : 0.0;
            }

            double var25 = Mth.lerp((double)param5, param0.xo, param0.x);
            double var26 = Mth.lerp((double)param5, param0.yo, param0.y) + 0.25;
            double var27 = Mth.lerp((double)param5, param0.zo, param0.z);
            double var28 = (double)((float)(var21 - var25));
            double var29 = (double)((float)(var22 - var26)) + var24;
            double var30 = (double)((float)(var23 - var27));
            GlStateManager.disableTexture();
            GlStateManager.disableLighting();
            var2.begin(3, DefaultVertexFormat.POSITION_COLOR);
            int var31 = 16;

            for(int var32 = 0; var32 <= 16; ++var32) {
                float var33 = (float)var32 / 16.0F;
                var2.vertex(param1 + var28 * (double)var33, param2 + var29 * (double)(var33 * var33 + var33) * 0.5 + 0.25, param3 + var30 * (double)var33)
                    .color(0, 0, 0, 255)
                    .endVertex();
            }

            var1.end();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture();
            super.render(param0, param1, param2, param3, param4, param5);
        }
    }

    protected ResourceLocation getTextureLocation(FishingHook param0) {
        return TEXTURE_LOCATION;
    }
}
