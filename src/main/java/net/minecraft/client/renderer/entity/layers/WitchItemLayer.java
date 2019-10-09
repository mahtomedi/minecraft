package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
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
        if (!var0.isEmpty()) {
            param0.pushPose();
            if (this.getParentModel().young) {
                param0.translate(0.0, 0.625, 0.0);
                param0.mulPose(Vector3f.XP.rotationDegrees(20.0F));
                float var1 = 0.5F;
                param0.scale(0.5F, 0.5F, 0.5F);
            }

            this.getParentModel().getNose().translateAndRotate(param0, 0.0625F);
            param0.translate(-0.0625, 0.53125, 0.21875);
            Item var2 = var0.getItem();
            if (Block.byItem(var2).defaultBlockState().getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED) {
                param0.translate(0.0, 0.0625, -0.25);
                param0.mulPose(Vector3f.XP.rotationDegrees(30.0F));
                param0.mulPose(Vector3f.YP.rotationDegrees(-5.0F));
                float var3 = 0.375F;
                param0.scale(0.375F, -0.375F, 0.375F);
            } else if (var2 == Items.BOW) {
                param0.translate(0.0, 0.125, -0.125);
                param0.mulPose(Vector3f.YP.rotationDegrees(-45.0F));
                float var4 = 0.625F;
                param0.scale(0.625F, -0.625F, 0.625F);
                param0.mulPose(Vector3f.XP.rotationDegrees(-100.0F));
                param0.mulPose(Vector3f.YP.rotationDegrees(-20.0F));
            } else {
                param0.translate(0.1875, 0.1875, 0.0);
                float var5 = 0.875F;
                param0.scale(0.875F, 0.875F, 0.875F);
                param0.mulPose(Vector3f.ZP.rotationDegrees(-20.0F));
                param0.mulPose(Vector3f.XP.rotationDegrees(-60.0F));
                param0.mulPose(Vector3f.ZP.rotationDegrees(-30.0F));
            }

            param0.mulPose(Vector3f.XP.rotationDegrees(-15.0F));
            param0.mulPose(Vector3f.ZP.rotationDegrees(40.0F));
            Minecraft.getInstance()
                .getItemInHandRenderer()
                .renderItem(param3, var0, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, false, param0, param1);
            param0.popPose();
        }
    }
}
