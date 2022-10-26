package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IronGolemFlowerLayer extends RenderLayer<IronGolem, IronGolemModel<IronGolem>> {
    private final BlockRenderDispatcher blockRenderer;

    public IronGolemFlowerLayer(RenderLayerParent<IronGolem, IronGolemModel<IronGolem>> param0, BlockRenderDispatcher param1) {
        super(param0);
        this.blockRenderer = param1;
    }

    public void render(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        IronGolem param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9
    ) {
        if (param3.getOfferFlowerTick() != 0) {
            param0.pushPose();
            ModelPart var0 = this.getParentModel().getFlowerHoldingArm();
            var0.translateAndRotate(param0);
            param0.translate(-1.1875F, 1.0625F, -0.9375F);
            param0.translate(0.5F, 0.5F, 0.5F);
            float var1 = 0.5F;
            param0.scale(0.5F, 0.5F, 0.5F);
            param0.mulPose(Axis.XP.rotationDegrees(-90.0F));
            param0.translate(-0.5F, -0.5F, -0.5F);
            this.blockRenderer.renderSingleBlock(Blocks.POPPY.defaultBlockState(), param0, param1, param2, OverlayTexture.NO_OVERLAY);
            param0.popPose();
        }
    }
}
