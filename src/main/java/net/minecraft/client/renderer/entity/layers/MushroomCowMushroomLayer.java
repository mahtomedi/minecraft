package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MushroomCowMushroomLayer<T extends MushroomCow> extends RenderLayer<T, CowModel<T>> {
    private final BlockRenderDispatcher blockRenderer;

    public MushroomCowMushroomLayer(RenderLayerParent<T, CowModel<T>> param0, BlockRenderDispatcher param1) {
        super(param0);
        this.blockRenderer = param1;
    }

    public void render(
        PoseStack param0, MultiBufferSource param1, int param2, T param3, float param4, float param5, float param6, float param7, float param8, float param9
    ) {
        if (!param3.isBaby()) {
            Minecraft var0 = Minecraft.getInstance();
            boolean var1 = var0.shouldEntityAppearGlowing(param3) && param3.isInvisible();
            if (!param3.isInvisible() || var1) {
                BlockState var2 = param3.getMushroomType().getBlockState();
                int var3 = LivingEntityRenderer.getOverlayCoords(param3, 0.0F);
                BakedModel var4 = this.blockRenderer.getBlockModel(var2);
                param0.pushPose();
                param0.translate(0.2F, -0.35F, 0.5F);
                param0.mulPose(Axis.YP.rotationDegrees(-48.0F));
                param0.scale(-1.0F, -1.0F, 1.0F);
                param0.translate(-0.5F, -0.5F, -0.5F);
                this.renderMushroomBlock(param0, param1, param2, var1, var2, var3, var4);
                param0.popPose();
                param0.pushPose();
                param0.translate(0.2F, -0.35F, 0.5F);
                param0.mulPose(Axis.YP.rotationDegrees(42.0F));
                param0.translate(0.1F, 0.0F, -0.6F);
                param0.mulPose(Axis.YP.rotationDegrees(-48.0F));
                param0.scale(-1.0F, -1.0F, 1.0F);
                param0.translate(-0.5F, -0.5F, -0.5F);
                this.renderMushroomBlock(param0, param1, param2, var1, var2, var3, var4);
                param0.popPose();
                param0.pushPose();
                this.getParentModel().getHead().translateAndRotate(param0);
                param0.translate(0.0F, -0.7F, -0.2F);
                param0.mulPose(Axis.YP.rotationDegrees(-78.0F));
                param0.scale(-1.0F, -1.0F, 1.0F);
                param0.translate(-0.5F, -0.5F, -0.5F);
                this.renderMushroomBlock(param0, param1, param2, var1, var2, var3, var4);
                param0.popPose();
            }
        }
    }

    private void renderMushroomBlock(PoseStack param0, MultiBufferSource param1, int param2, boolean param3, BlockState param4, int param5, BakedModel param6) {
        if (param3) {
            this.blockRenderer
                .getModelRenderer()
                .renderModel(
                    param0.last(), param1.getBuffer(RenderType.outline(TextureAtlas.LOCATION_BLOCKS)), param4, param6, 0.0F, 0.0F, 0.0F, param2, param5
                );
        } else {
            this.blockRenderer.renderSingleBlock(param4, param0, param1, param2, param5);
        }

    }
}
