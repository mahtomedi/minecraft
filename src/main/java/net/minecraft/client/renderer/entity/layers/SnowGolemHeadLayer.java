package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SnowGolemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SnowGolemHeadLayer extends RenderLayer<SnowGolem, SnowGolemModel<SnowGolem>> {
    private final BlockRenderDispatcher blockRenderer;
    private final ItemRenderer itemRenderer;

    public SnowGolemHeadLayer(RenderLayerParent<SnowGolem, SnowGolemModel<SnowGolem>> param0, BlockRenderDispatcher param1, ItemRenderer param2) {
        super(param0);
        this.blockRenderer = param1;
        this.itemRenderer = param2;
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
        float param9
    ) {
        if (param3.hasPumpkin()) {
            boolean var0 = Minecraft.getInstance().shouldEntityAppearGlowing(param3) && param3.isInvisible();
            if (!param3.isInvisible() || var0) {
                param0.pushPose();
                this.getParentModel().getHead().translateAndRotate(param0);
                float var1 = 0.625F;
                param0.translate(0.0, -0.34375, 0.0);
                param0.mulPose(Vector3f.YP.rotationDegrees(180.0F));
                param0.scale(0.625F, -0.625F, -0.625F);
                ItemStack var2 = new ItemStack(Blocks.CARVED_PUMPKIN);
                if (var0) {
                    BlockState var3 = Blocks.CARVED_PUMPKIN.defaultBlockState();
                    BakedModel var4 = this.blockRenderer.getBlockModel(var3);
                    int var5 = LivingEntityRenderer.getOverlayCoords(param3, 0.0F);
                    param0.translate(-0.5, -0.5, -0.5);
                    this.blockRenderer
                        .getModelRenderer()
                        .renderModel(
                            param0.last(), param1.getBuffer(RenderType.outline(TextureAtlas.LOCATION_BLOCKS)), var3, var4, 0.0F, 0.0F, 0.0F, param2, var5
                        );
                } else {
                    this.itemRenderer
                        .renderStatic(
                            param3,
                            var2,
                            ItemTransforms.TransformType.HEAD,
                            false,
                            param0,
                            param1,
                            param3.level,
                            param2,
                            LivingEntityRenderer.getOverlayCoords(param3, 0.0F),
                            param3.getId()
                        );
                }

                param0.popPose();
            }
        }
    }
}
