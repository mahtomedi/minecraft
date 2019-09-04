package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
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

    public void render(AbstractClientPlayer param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        if (param0.isCapeLoaded() && !param0.isInvisible() && param0.isModelPartShown(PlayerModelPart.CAPE) && param0.getCloakTextureLocation() != null) {
            ItemStack var0 = param0.getItemBySlot(EquipmentSlot.CHEST);
            if (var0.getItem() != Items.ELYTRA) {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                this.bindTexture(param0.getCloakTextureLocation());
                RenderSystem.pushMatrix();
                RenderSystem.translatef(0.0F, 0.0F, 0.125F);
                double var1 = Mth.lerp((double)param3, param0.xCloakO, param0.xCloak) - Mth.lerp((double)param3, param0.xo, param0.x);
                double var2 = Mth.lerp((double)param3, param0.yCloakO, param0.yCloak) - Mth.lerp((double)param3, param0.yo, param0.y);
                double var3 = Mth.lerp((double)param3, param0.zCloakO, param0.zCloak) - Mth.lerp((double)param3, param0.zo, param0.z);
                float var4 = param0.yBodyRotO + (param0.yBodyRot - param0.yBodyRotO);
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

                float var10 = Mth.lerp(param3, param0.oBob, param0.bob);
                var7 += Mth.sin(Mth.lerp(param3, param0.walkDistO, param0.walkDist) * 6.0F) * 32.0F * var10;
                if (param0.isCrouching()) {
                    var7 += 25.0F;
                }

                RenderSystem.rotatef(6.0F + var8 / 2.0F + var7, 1.0F, 0.0F, 0.0F);
                RenderSystem.rotatef(var9 / 2.0F, 0.0F, 0.0F, 1.0F);
                RenderSystem.rotatef(-var9 / 2.0F, 0.0F, 1.0F, 0.0F);
                RenderSystem.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
                this.getParentModel().renderCloak(0.0625F);
                RenderSystem.popMatrix();
            }
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}
