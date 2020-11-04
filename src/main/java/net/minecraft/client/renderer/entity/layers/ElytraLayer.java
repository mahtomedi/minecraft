package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ElytraLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation WINGS_LOCATION = new ResourceLocation("textures/entity/elytra.png");
    private final ElytraModel<T> elytraModel;

    public ElytraLayer(RenderLayerParent<T, M> param0, EntityModelSet param1) {
        super(param0);
        this.elytraModel = new ElytraModel<>(param1.getLayer(ModelLayers.ELYTRA));
    }

    public void render(
        PoseStack param0, MultiBufferSource param1, int param2, T param3, float param4, float param5, float param6, float param7, float param8, float param9
    ) {
        ItemStack var0 = param3.getItemBySlot(EquipmentSlot.CHEST);
        if (var0.is(Items.ELYTRA)) {
            ResourceLocation var2;
            if (param3 instanceof AbstractClientPlayer) {
                AbstractClientPlayer var1 = (AbstractClientPlayer)param3;
                if (var1.isElytraLoaded() && var1.getElytraTextureLocation() != null) {
                    var2 = var1.getElytraTextureLocation();
                } else if (var1.isCapeLoaded() && var1.getCloakTextureLocation() != null && var1.isModelPartShown(PlayerModelPart.CAPE)) {
                    var2 = var1.getCloakTextureLocation();
                } else {
                    var2 = WINGS_LOCATION;
                }
            } else {
                var2 = WINGS_LOCATION;
            }

            param0.pushPose();
            param0.translate(0.0, 0.0, 0.125);
            this.getParentModel().copyPropertiesTo(this.elytraModel);
            this.elytraModel.setupAnim(param3, param4, param5, param7, param8, param9);
            VertexConsumer var6 = ItemRenderer.getArmorFoilBuffer(param1, RenderType.armorCutoutNoCull(var2), false, var0.hasFoil());
            this.elytraModel.renderToBuffer(param0, var6, param2, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            param0.popPose();
        }
    }
}
