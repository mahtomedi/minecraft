package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitchItemLayer<T extends LivingEntity> extends RenderLayer<T, WitchModel<T>> {
    public WitchItemLayer(RenderLayerParent<T, WitchModel<T>> param0) {
        super(param0);
    }

    public void render(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        T param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9,
        float param10
    ) {
        ItemStack var0 = param3.getMainHandItem();
        if (var0.getItem() == Items.POTION) {
            param0.pushPose();
            this.getParentModel().getHead().translateAndRotate(param0, 0.0625F);
            this.getParentModel().getNose().translateAndRotate(param0, 0.0625F);
            param0.translate(0.0, 0.375, -0.03125);
            param0.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
            param0.mulPose(Vector3f.XP.rotationDegrees(-35.0F));
            Minecraft.getInstance()
                .getItemInHandRenderer()
                .renderItem(param3, var0, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, false, param0, param1);
            param0.popPose();
        }
    }
}
