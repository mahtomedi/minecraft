package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CapeLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public CapeLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> param0) {
        super(param0);
    }

    public void render(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        AbstractClientPlayer param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9
    ) {
        if (!param3.isInvisible() && param3.isModelPartShown(PlayerModelPart.CAPE)) {
            PlayerSkin var0 = param3.getSkin();
            if (var0.capeTexture() != null) {
                ItemStack var1 = param3.getItemBySlot(EquipmentSlot.CHEST);
                if (!var1.is(Items.ELYTRA)) {
                    param0.pushPose();
                    param0.translate(0.0F, 0.0F, 0.125F);
                    double var2 = Mth.lerp((double)param6, param3.xCloakO, param3.xCloak) - Mth.lerp((double)param6, param3.xo, param3.getX());
                    double var3 = Mth.lerp((double)param6, param3.yCloakO, param3.yCloak) - Mth.lerp((double)param6, param3.yo, param3.getY());
                    double var4 = Mth.lerp((double)param6, param3.zCloakO, param3.zCloak) - Mth.lerp((double)param6, param3.zo, param3.getZ());
                    float var5 = Mth.rotLerp(param6, param3.yBodyRotO, param3.yBodyRot);
                    double var6 = (double)Mth.sin(var5 * (float) (Math.PI / 180.0));
                    double var7 = (double)(-Mth.cos(var5 * (float) (Math.PI / 180.0)));
                    float var8 = (float)var3 * 10.0F;
                    var8 = Mth.clamp(var8, -6.0F, 32.0F);
                    float var9 = (float)(var2 * var6 + var4 * var7) * 100.0F;
                    var9 = Mth.clamp(var9, 0.0F, 150.0F);
                    float var10 = (float)(var2 * var7 - var4 * var6) * 100.0F;
                    var10 = Mth.clamp(var10, -20.0F, 20.0F);
                    if (var9 < 0.0F) {
                        var9 = 0.0F;
                    }

                    float var11 = Mth.lerp(param6, param3.oBob, param3.bob);
                    var8 += Mth.sin(Mth.lerp(param6, param3.walkDistO, param3.walkDist) * 6.0F) * 32.0F * var11;
                    if (param3.isCrouching()) {
                        var8 += 25.0F;
                    }

                    param0.mulPose(Axis.XP.rotationDegrees(6.0F + var9 / 2.0F + var8));
                    param0.mulPose(Axis.ZP.rotationDegrees(var10 / 2.0F));
                    param0.mulPose(Axis.YP.rotationDegrees(180.0F - var10 / 2.0F));
                    VertexConsumer var12 = param1.getBuffer(RenderType.entitySolid(var0.capeTexture()));
                    this.getParentModel().renderCloak(param0, var12, param2, OverlayTexture.NO_OVERLAY);
                    param0.popPose();
                }
            }
        }
    }
}
