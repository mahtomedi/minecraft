package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DolphinCarryingItemLayer extends RenderLayer<Dolphin, DolphinModel<Dolphin>> {
    private final ItemInHandRenderer itemInHandRenderer;

    public DolphinCarryingItemLayer(RenderLayerParent<Dolphin, DolphinModel<Dolphin>> param0, ItemInHandRenderer param1) {
        super(param0);
        this.itemInHandRenderer = param1;
    }

    public void render(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        Dolphin param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9
    ) {
        boolean var0 = param3.getMainArm() == HumanoidArm.RIGHT;
        param0.pushPose();
        float var1 = 1.0F;
        float var2 = -1.0F;
        float var3 = Mth.abs(param3.getXRot()) / 60.0F;
        if (param3.getXRot() < 0.0F) {
            param0.translate(0.0, (double)(1.0F - var3 * 0.5F), (double)(-1.0F + var3 * 0.5F));
        } else {
            param0.translate(0.0, (double)(1.0F + var3 * 0.8F), (double)(-1.0F + var3 * 0.2F));
        }

        ItemStack var4 = var0 ? param3.getMainHandItem() : param3.getOffhandItem();
        this.itemInHandRenderer.renderItem(param3, var4, ItemTransforms.TransformType.GROUND, false, param0, param1, param2);
        param0.popPose();
    }
}
