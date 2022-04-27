package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockRenderDispatcher implements ResourceManagerReloadListener {
    private final BlockModelShaper blockModelShaper;
    private final ModelBlockRenderer modelRenderer;
    private final BlockEntityWithoutLevelRenderer blockEntityRenderer;
    private final LiquidBlockRenderer liquidBlockRenderer;
    private final RandomSource random = RandomSource.create();
    private final BlockColors blockColors;

    public BlockRenderDispatcher(BlockModelShaper param0, BlockEntityWithoutLevelRenderer param1, BlockColors param2) {
        this.blockModelShaper = param0;
        this.blockEntityRenderer = param1;
        this.blockColors = param2;
        this.modelRenderer = new ModelBlockRenderer(this.blockColors);
        this.liquidBlockRenderer = new LiquidBlockRenderer();
    }

    public BlockModelShaper getBlockModelShaper() {
        return this.blockModelShaper;
    }

    public void renderBreakingTexture(BlockState param0, BlockPos param1, BlockAndTintGetter param2, PoseStack param3, VertexConsumer param4) {
        if (param0.getRenderShape() == RenderShape.MODEL) {
            BakedModel var0 = this.blockModelShaper.getBlockModel(param0);
            long var1 = param0.getSeed(param1);
            this.modelRenderer.tesselateBlock(param2, var0, param0, param1, param3, param4, true, this.random, var1, OverlayTexture.NO_OVERLAY);
        }
    }

    public void renderBatched(
        BlockState param0, BlockPos param1, BlockAndTintGetter param2, PoseStack param3, VertexConsumer param4, boolean param5, RandomSource param6
    ) {
        try {
            RenderShape var0 = param0.getRenderShape();
            if (var0 == RenderShape.MODEL) {
                this.modelRenderer
                    .tesselateBlock(
                        param2, this.getBlockModel(param0), param0, param1, param3, param4, param5, param6, param0.getSeed(param1), OverlayTexture.NO_OVERLAY
                    );
            }

        } catch (Throwable var11) {
            CrashReport var2 = CrashReport.forThrowable(var11, "Tesselating block in world");
            CrashReportCategory var3 = var2.addCategory("Block being tesselated");
            CrashReportCategory.populateBlockDetails(var3, param2, param1, param0);
            throw new ReportedException(var2);
        }
    }

    public void renderLiquid(BlockPos param0, BlockAndTintGetter param1, VertexConsumer param2, BlockState param3, FluidState param4) {
        try {
            this.liquidBlockRenderer.tesselate(param1, param0, param2, param3, param4);
        } catch (Throwable var9) {
            CrashReport var1 = CrashReport.forThrowable(var9, "Tesselating liquid in world");
            CrashReportCategory var2 = var1.addCategory("Block being tesselated");
            CrashReportCategory.populateBlockDetails(var2, param1, param0, null);
            throw new ReportedException(var1);
        }
    }

    public ModelBlockRenderer getModelRenderer() {
        return this.modelRenderer;
    }

    public BakedModel getBlockModel(BlockState param0) {
        return this.blockModelShaper.getBlockModel(param0);
    }

    public void renderSingleBlock(BlockState param0, PoseStack param1, MultiBufferSource param2, int param3, int param4) {
        RenderShape var0 = param0.getRenderShape();
        if (var0 != RenderShape.INVISIBLE) {
            switch(var0) {
                case MODEL:
                    BakedModel var1 = this.getBlockModel(param0);
                    int var2 = this.blockColors.getColor(param0, null, null, 0);
                    float var3 = (float)(var2 >> 16 & 0xFF) / 255.0F;
                    float var4 = (float)(var2 >> 8 & 0xFF) / 255.0F;
                    float var5 = (float)(var2 & 0xFF) / 255.0F;
                    this.modelRenderer
                        .renderModel(
                            param1.last(), param2.getBuffer(ItemBlockRenderTypes.getRenderType(param0, false)), param0, var1, var3, var4, var5, param3, param4
                        );
                    break;
                case ENTITYBLOCK_ANIMATED:
                    this.blockEntityRenderer.renderByItem(new ItemStack(param0.getBlock()), ItemTransforms.TransformType.NONE, param1, param2, param3, param4);
            }

        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager param0) {
        this.liquidBlockRenderer.setupSprites();
    }
}
