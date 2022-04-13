package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CarriedBlockLayer extends RenderLayer<EnderMan, EndermanModel<EnderMan>> {
    private final BlockRenderDispatcher blockRenderer;

    public CarriedBlockLayer(RenderLayerParent<EnderMan, EndermanModel<EnderMan>> param0, BlockRenderDispatcher param1) {
        super(param0);
        this.blockRenderer = param1;
    }

    public void render(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        EnderMan param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9
    ) {
        BlockState var0 = param3.getCarriedBlock();
        if (var0 != null) {
            param0.pushPose();
            param0.translate(0.0, 0.6875, -0.75);
            param0.mulPose(Vector3f.XP.rotationDegrees(20.0F));
            param0.mulPose(Vector3f.YP.rotationDegrees(45.0F));
            param0.translate(0.25, 0.1875, 0.25);
            float var1 = 0.5F;
            param0.scale(-0.5F, -0.5F, 0.5F);
            param0.mulPose(Vector3f.YP.rotationDegrees(90.0F));
            this.blockRenderer.renderSingleBlock(var0, param0, param1, param2, OverlayTexture.NO_OVERLAY);
            param0.popPose();
        }
    }
}
