package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MushroomCowMushroomLayer<T extends MushroomCow> extends RenderLayer<T, CowModel<T>> {
    public MushroomCowMushroomLayer(RenderLayerParent<T, CowModel<T>> param0) {
        super(param0);
    }

    public void render(
        PoseStack param0, MultiBufferSource param1, int param2, T param3, float param4, float param5, float param6, float param7, float param8, float param9
    ) {
        if (!param3.isBaby() && !param3.isInvisible()) {
            BlockRenderDispatcher var0 = Minecraft.getInstance().getBlockRenderer();
            BlockState var1 = param3.getMushroomType().getBlockState();
            int var2 = LivingEntityRenderer.getOverlayCoords(param3, 0.0F);
            param0.pushPose();
            param0.translate(0.2F, -0.35F, 0.5);
            param0.mulPose(Vector3f.YP.rotationDegrees(-48.0F));
            param0.scale(-1.0F, -1.0F, 1.0F);
            param0.translate(-0.5, -0.5, -0.5);
            var0.renderSingleBlock(var1, param0, param1, param2, var2);
            param0.popPose();
            param0.pushPose();
            param0.translate(0.2F, -0.35F, 0.5);
            param0.mulPose(Vector3f.YP.rotationDegrees(42.0F));
            param0.translate(0.1F, 0.0, -0.6F);
            param0.mulPose(Vector3f.YP.rotationDegrees(-48.0F));
            param0.scale(-1.0F, -1.0F, 1.0F);
            param0.translate(-0.5, -0.5, -0.5);
            var0.renderSingleBlock(var1, param0, param1, param2, var2);
            param0.popPose();
            param0.pushPose();
            this.getParentModel().getHead().translateAndRotate(param0);
            param0.translate(0.0, -0.7F, -0.2F);
            param0.mulPose(Vector3f.YP.rotationDegrees(-78.0F));
            param0.scale(-1.0F, -1.0F, 1.0F);
            param0.translate(-0.5, -0.5, -0.5);
            var0.renderSingleBlock(var1, param0, param1, param2, var2);
            param0.popPose();
        }
    }
}
