package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SnowGolemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SnowGolemHeadLayer extends RenderLayer<SnowGolem, SnowGolemModel<SnowGolem>> {
    public SnowGolemHeadLayer(RenderLayerParent<SnowGolem, SnowGolemModel<SnowGolem>> param0) {
        super(param0);
    }

    public void render(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        SnowGolem param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9,
        float param10
    ) {
        if (!param3.isInvisible() && param3.hasPumpkin()) {
            param0.pushPose();
            this.getParentModel().getHead().translateAndRotate(param0, 0.0625F);
            float var0 = 0.625F;
            param0.translate(0.0, -0.34375, 0.0);
            param0.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            param0.scale(0.625F, -0.625F, -0.625F);
            ItemStack var1 = new ItemStack(Blocks.CARVED_PUMPKIN);
            Minecraft.getInstance()
                .getItemRenderer()
                .renderStatic(
                    param3,
                    var1,
                    ItemTransforms.TransformType.HEAD,
                    false,
                    param0,
                    param1,
                    param3.level,
                    param3.getLightColor(),
                    LivingEntityRenderer.getOverlayCoords(param3, 0.0F)
                );
            param0.popPose();
        }
    }
}
