package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitchItemLayer<T extends LivingEntity> extends CrossedArmsItemLayer<T, WitchModel<T>> {
    public WitchItemLayer(RenderLayerParent<T, WitchModel<T>> param0, ItemInHandRenderer param1) {
        super(param0, param1);
    }

    @Override
    public void render(
        PoseStack param0, MultiBufferSource param1, int param2, T param3, float param4, float param5, float param6, float param7, float param8, float param9
    ) {
        ItemStack var0 = param3.getMainHandItem();
        param0.pushPose();
        if (var0.is(Items.POTION)) {
            this.getParentModel().getHead().translateAndRotate(param0);
            this.getParentModel().getNose().translateAndRotate(param0);
            param0.translate(0.0625F, 0.25F, 0.0F);
            param0.mulPose(Axis.ZP.rotationDegrees(180.0F));
            param0.mulPose(Axis.XP.rotationDegrees(140.0F));
            param0.mulPose(Axis.ZP.rotationDegrees(10.0F));
            param0.translate(0.0F, -0.4F, 0.4F);
        }

        super.render(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
        param0.popPose();
    }
}
