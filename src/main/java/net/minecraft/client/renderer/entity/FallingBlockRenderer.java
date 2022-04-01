package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GenericItemBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FallingBlockRenderer extends EntityRenderer<FallingBlockEntity> {
    private final ItemRenderer itemRenderer;

    public FallingBlockRenderer(EntityRendererProvider.Context param0) {
        super(param0);
        this.itemRenderer = param0.getItemRenderer();
        this.shadowRadius = 0.5F;
    }

    public void render(FallingBlockEntity param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        BlockState var0 = param0.getBlockState();
        if (var0.is(Blocks.LAVA)) {
            var0 = GenericItemBlock.genericBlockFromItem(Items.LAVA_BUCKET);
        } else if (var0.is(Blocks.WATER)) {
            var0 = GenericItemBlock.genericBlockFromItem(Items.WATER_BUCKET);
        }

        if (var0.getRenderShape() == RenderShape.MODEL) {
            Level var1 = param0.getLevel();
            if ((var0.is(Blocks.GENERIC_ITEM_BLOCK) || var0 != var1.getBlockState(param0.blockPosition())) && var0.getRenderShape() != RenderShape.INVISIBLE) {
                param3.pushPose();
                BlockPos var2 = new BlockPos(param0.getX(), param0.getBoundingBox().maxY, param0.getZ());
                Item var3 = GenericItemBlock.itemFromGenericBlock(var0);
                if (var3 != null) {
                    ItemStack var4 = new ItemStack(var3);
                    BakedModel var5 = this.itemRenderer.getModel(var4, param0.level, null, param0.getId());
                    float var6 = param0.getSpin(param2);
                    param3.pushPose();
                    param3.mulPose(Vector3f.XP.rotationDegrees(90.0F));
                    param3.mulPose(Vector3f.ZP.rotationDegrees(var6));
                    this.itemRenderer.render(var4, ItemTransforms.TransformType.FIXED, false, param3, param4, param5, OverlayTexture.NO_OVERLAY, var5);
                    param3.popPose();
                } else {
                    param3.translate(-0.5, 0.0, -0.5);
                    BlockRenderDispatcher var7 = Minecraft.getInstance().getBlockRenderer();
                    var7.getModelRenderer()
                        .tesselateBlock(
                            var1,
                            var7.getBlockModel(var0),
                            var0,
                            var2,
                            param3,
                            param4.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(var0)),
                            false,
                            new Random(),
                            var0.getSeed(param0.getStartPos()),
                            OverlayTexture.NO_OVERLAY
                        );
                }

                param3.popPose();
                super.render(param0, param1, param2, param3, param4, param5);
            }
        }
    }

    public ResourceLocation getTextureLocation(FallingBlockEntity param0) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
