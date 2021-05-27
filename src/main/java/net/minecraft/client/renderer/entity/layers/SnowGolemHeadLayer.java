package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SnowGolemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
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
        float param9
    ) {
        if (param3.hasPumpkin()) {
            Minecraft var0 = Minecraft.getInstance();
            boolean var1 = var0.shouldEntityAppearGlowing(param3) && param3.isInvisible();
            if (!param3.isInvisible() || var1) {
                param0.pushPose();
                this.getParentModel().getHead().translateAndRotate(param0);
                float var2 = 0.625F;
                param0.translate(0.0, -0.34375, 0.0);
                param0.mulPose(Vector3f.YP.rotationDegrees(180.0F));
                param0.scale(0.625F, -0.625F, -0.625F);
                ItemStack var3 = new ItemStack(Blocks.CARVED_PUMPKIN);
                if (var1) {
                    BlockState var4 = Blocks.CARVED_PUMPKIN.defaultBlockState();
                    BlockRenderDispatcher var5 = var0.getBlockRenderer();
                    BakedModel var6 = var5.getBlockModel(var4);
                    int var7 = LivingEntityRenderer.getOverlayCoords(param3, 0.0F);
                    param0.translate(-0.5, -0.5, -0.5);
                    var5.getModelRenderer()
                        .renderModel(
                            param0.last(), param1.getBuffer(RenderType.outline(TextureAtlas.LOCATION_BLOCKS)), var4, var6, 0.0F, 0.0F, 0.0F, param2, var7
                        );
                } else {
                    var0.getItemRenderer()
                        .renderStatic(
                            param3,
                            var3,
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
