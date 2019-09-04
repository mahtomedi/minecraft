package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
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
    private final ElytraModel<T> elytraModel = new ElytraModel<>();

    public ElytraLayer(RenderLayerParent<T, M> param0) {
        super(param0);
    }

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        ItemStack var0 = param0.getItemBySlot(EquipmentSlot.CHEST);
        if (var0.getItem() == Items.ELYTRA) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            if (param0 instanceof AbstractClientPlayer) {
                AbstractClientPlayer var1 = (AbstractClientPlayer)param0;
                if (var1.isElytraLoaded() && var1.getElytraTextureLocation() != null) {
                    this.bindTexture(var1.getElytraTextureLocation());
                } else if (var1.isCapeLoaded() && var1.getCloakTextureLocation() != null && var1.isModelPartShown(PlayerModelPart.CAPE)) {
                    this.bindTexture(var1.getCloakTextureLocation());
                } else {
                    this.bindTexture(WINGS_LOCATION);
                }
            } else {
                this.bindTexture(WINGS_LOCATION);
            }

            RenderSystem.pushMatrix();
            RenderSystem.translatef(0.0F, 0.0F, 0.125F);
            this.elytraModel.setupAnim(param0, param1, param2, param4, param5, param6, param7);
            this.elytraModel.render(param0, param1, param2, param4, param5, param6, param7);
            if (var0.isEnchanted()) {
                AbstractArmorLayer.renderFoil(this::bindTexture, param0, this.elytraModel, param1, param2, param3, param4, param5, param6, param7);
            }

            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}
