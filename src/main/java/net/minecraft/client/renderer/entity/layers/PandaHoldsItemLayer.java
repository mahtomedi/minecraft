package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PandaHoldsItemLayer extends RenderLayer<Panda, PandaModel<Panda>> {
    public PandaHoldsItemLayer(RenderLayerParent<Panda, PandaModel<Panda>> param0) {
        super(param0);
    }

    public void render(Panda param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        ItemStack var0 = param0.getItemBySlot(EquipmentSlot.MAINHAND);
        if (param0.isSitting() && !var0.isEmpty() && !param0.isScared()) {
            float var1 = -0.6F;
            float var2 = 1.4F;
            if (param0.isEating()) {
                var1 -= 0.2F * Mth.sin(param4 * 0.6F) + 0.2F;
                var2 -= 0.09F * Mth.sin(param4 * 0.6F);
            }

            GlStateManager.pushMatrix();
            GlStateManager.translatef(0.1F, var2, var1);
            Minecraft.getInstance().getItemRenderer().renderWithMobState(var0, param0, ItemTransforms.TransformType.GROUND, false);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}
