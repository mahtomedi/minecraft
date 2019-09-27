package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
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
        float param9,
        float param10
    ) {
        if (param3.isCapeLoaded() && !param3.isInvisible() && param3.isModelPartShown(PlayerModelPart.CAPE) && param3.getCloakTextureLocation() != null) {
            ItemStack var0 = param3.getItemBySlot(EquipmentSlot.CHEST);
            if (var0.getItem() != Items.ELYTRA) {
                param0.pushPose();
                param0.translate(0.0, 0.0, 0.125);
                double var1 = Mth.lerp((double)param6, param3.xCloakO, param3.xCloak) - Mth.lerp((double)param6, param3.xo, param3.x);
                double var2 = Mth.lerp((double)param6, param3.yCloakO, param3.yCloak) - Mth.lerp((double)param6, param3.yo, param3.y);
                double var3 = Mth.lerp((double)param6, param3.zCloakO, param3.zCloak) - Mth.lerp((double)param6, param3.zo, param3.z);
                float var4 = param3.yBodyRotO + (param3.yBodyRot - param3.yBodyRotO);
                double var5 = (double)Mth.sin(var4 * (float) (Math.PI / 180.0));
                double var6 = (double)(-Mth.cos(var4 * (float) (Math.PI / 180.0)));
                float var7 = (float)var2 * 10.0F;
                var7 = Mth.clamp(var7, -6.0F, 32.0F);
                float var8 = (float)(var1 * var5 + var3 * var6) * 100.0F;
                var8 = Mth.clamp(var8, 0.0F, 150.0F);
                float var9 = (float)(var1 * var6 - var3 * var5) * 100.0F;
                var9 = Mth.clamp(var9, -20.0F, 20.0F);
                if (var8 < 0.0F) {
                    var8 = 0.0F;
                }

                float var10 = Mth.lerp(param6, param3.oBob, param3.bob);
                var7 += Mth.sin(Mth.lerp(param6, param3.walkDistO, param3.walkDist) * 6.0F) * 32.0F * var10;
                if (param3.isCrouching()) {
                    var7 += 25.0F;
                }

                param0.mulPose(Vector3f.XP.rotation(6.0F + var8 / 2.0F + var7, true));
                param0.mulPose(Vector3f.ZP.rotation(var9 / 2.0F, true));
                param0.mulPose(Vector3f.YP.rotation(180.0F - var9 / 2.0F, true));
                VertexConsumer var11 = param1.getBuffer(RenderType.NEW_ENTITY(param3.getCloakTextureLocation()));
                OverlayTexture.setDefault(var11);
                this.getParentModel().renderCloak(param0, var11, 0.0625F, param2);
                var11.unsetDefaultOverlayCoords();
                param0.popPose();
            }
        }
    }
}
