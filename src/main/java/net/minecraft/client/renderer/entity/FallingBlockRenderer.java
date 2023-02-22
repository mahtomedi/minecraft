package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FallingBlockRenderer extends EntityRenderer<FallingBlockEntity> {
    private final BlockRenderDispatcher dispatcher;

    public FallingBlockRenderer(EntityRendererProvider.Context param0) {
        super(param0);
        this.shadowRadius = 0.5F;
        this.dispatcher = param0.getBlockRenderDispatcher();
    }

    public void render(FallingBlockEntity param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        BlockState var0 = param0.getBlockState();
        if (var0.getRenderShape() == RenderShape.MODEL) {
            Level var1 = param0.getLevel();
            if (var0 != var1.getBlockState(param0.blockPosition()) && var0.getRenderShape() != RenderShape.INVISIBLE) {
                param3.pushPose();
                BlockPos var2 = BlockPos.containing(param0.getX(), param0.getBoundingBox().maxY, param0.getZ());
                param3.translate(-0.5, 0.0, -0.5);
                this.dispatcher
                    .getModelRenderer()
                    .tesselateBlock(
                        var1,
                        this.dispatcher.getBlockModel(var0),
                        var0,
                        var2,
                        param3,
                        param4.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(var0)),
                        false,
                        RandomSource.create(),
                        var0.getSeed(param0.getStartPos()),
                        OverlayTexture.NO_OVERLAY
                    );
                param3.popPose();
                super.render(param0, param1, param2, param3, param4, param5);
            }
        }
    }

    public ResourceLocation getTextureLocation(FallingBlockEntity param0) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
