package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FoxHeldItemLayer extends RenderLayer<Fox, FoxModel<Fox>> {
    public FoxHeldItemLayer(RenderLayerParent<Fox, FoxModel<Fox>> param0) {
        super(param0);
    }

    public void render(Fox param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        ItemStack var0 = param0.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!var0.isEmpty()) {
            boolean var1 = param0.isSleeping();
            boolean var2 = param0.isBaby();
            GlStateManager.pushMatrix();
            if (var2) {
                float var3 = 0.75F;
                GlStateManager.scalef(0.75F, 0.75F, 0.75F);
                GlStateManager.translatef(0.0F, 8.0F * param7, 3.35F * param7);
            }

            GlStateManager.translatef(this.getParentModel().head.x / 16.0F, this.getParentModel().head.y / 16.0F, this.getParentModel().head.z / 16.0F);
            float var4 = param0.getHeadRollAngle(param3) * (180.0F / (float)Math.PI);
            GlStateManager.rotatef(var4, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotatef(param5, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotatef(param6, 1.0F, 0.0F, 0.0F);
            if (param0.isBaby()) {
                if (var1) {
                    GlStateManager.translatef(0.4F, 0.26F, 0.15F);
                } else {
                    GlStateManager.translatef(0.06F, 0.26F, -0.5F);
                }
            } else if (var1) {
                GlStateManager.translatef(0.46F, 0.26F, 0.22F);
            } else {
                GlStateManager.translatef(0.06F, 0.27F, -0.5F);
            }

            GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
            if (var1) {
                GlStateManager.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
            }

            Minecraft.getInstance().getItemRenderer().renderWithMobState(var0, param0, ItemTransforms.TransformType.GROUND, false);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}
