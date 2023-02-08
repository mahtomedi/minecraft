package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PandaHoldsItemLayer extends RenderLayer<Panda, PandaModel<Panda>> {
    private final ItemInHandRenderer itemInHandRenderer;

    public PandaHoldsItemLayer(RenderLayerParent<Panda, PandaModel<Panda>> param0, ItemInHandRenderer param1) {
        super(param0);
        this.itemInHandRenderer = param1;
    }

    public void render(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        Panda param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9
    ) {
        ItemStack var0 = param3.getItemBySlot(EquipmentSlot.MAINHAND);
        if (param3.isSitting() && !param3.isScared()) {
            float var1 = -0.6F;
            float var2 = 1.4F;
            if (param3.isEating()) {
                var1 -= 0.2F * Mth.sin(param7 * 0.6F) + 0.2F;
                var2 -= 0.09F * Mth.sin(param7 * 0.6F);
            }

            param0.pushPose();
            param0.translate(0.1F, var2, var1);
            this.itemInHandRenderer.renderItem(param3, var0, ItemDisplayContext.GROUND, false, param0, param1, param2);
            param0.popPose();
        }
    }
}
