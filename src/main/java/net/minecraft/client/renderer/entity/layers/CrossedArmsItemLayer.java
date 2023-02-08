package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CrossedArmsItemLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final ItemInHandRenderer itemInHandRenderer;

    public CrossedArmsItemLayer(RenderLayerParent<T, M> param0, ItemInHandRenderer param1) {
        super(param0);
        this.itemInHandRenderer = param1;
    }

    public void render(
        PoseStack param0, MultiBufferSource param1, int param2, T param3, float param4, float param5, float param6, float param7, float param8, float param9
    ) {
        param0.pushPose();
        param0.translate(0.0F, 0.4F, -0.4F);
        param0.mulPose(Axis.XP.rotationDegrees(180.0F));
        ItemStack var0 = param3.getItemBySlot(EquipmentSlot.MAINHAND);
        this.itemInHandRenderer.renderItem(param3, var0, ItemDisplayContext.GROUND, false, param0, param1, param2);
        param0.popPose();
    }
}
