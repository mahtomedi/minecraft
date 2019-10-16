package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.Random;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.EntityBlockRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndBiomeGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockRenderDispatcher implements ResourceManagerReloadListener {
    private final BlockModelShaper blockModelShaper;
    private final ModelBlockRenderer modelRenderer;
    private final LiquidBlockRenderer liquidBlockRenderer;
    private final Random random = new Random();
    private final BlockColors blockColors;

    public BlockRenderDispatcher(BlockModelShaper param0, BlockColors param1) {
        this.blockModelShaper = param0;
        this.blockColors = param1;
        this.modelRenderer = new ModelBlockRenderer(this.blockColors);
        this.liquidBlockRenderer = new LiquidBlockRenderer();
    }

    public BlockModelShaper getBlockModelShaper() {
        return this.blockModelShaper;
    }

    public void renderBreakingTexture(BlockState param0, BlockPos param1, BlockAndBiomeGetter param2, PoseStack param3, VertexConsumer param4) {
        if (param0.getRenderShape() == RenderShape.MODEL) {
            BakedModel var0 = this.blockModelShaper.getBlockModel(param0);
            long var1 = param0.getSeed(param1);
            this.modelRenderer.tesselateBlock(param2, var0, param0, param1, param3, param4, true, this.random, var1, OverlayTexture.NO_OVERLAY);
        }
    }

    public boolean renderBatched(
        BlockState param0, BlockPos param1, BlockAndBiomeGetter param2, PoseStack param3, VertexConsumer param4, boolean param5, Random param6
    ) {
        try {
            RenderShape var0 = param0.getRenderShape();
            return var0 != RenderShape.MODEL
                ? false
                : this.modelRenderer
                    .tesselateBlock(
                        param2, this.getBlockModel(param0), param0, param1, param3, param4, param5, param6, param0.getSeed(param1), OverlayTexture.NO_OVERLAY
                    );
        } catch (Throwable var11) {
            CrashReport var2 = CrashReport.forThrowable(var11, "Tesselating block in world");
            CrashReportCategory var3 = var2.addCategory("Block being tesselated");
            CrashReportCategory.populateBlockDetails(var3, param1, param0);
            throw new ReportedException(var2);
        }
    }

    public boolean renderLiquid(BlockPos param0, BlockAndBiomeGetter param1, VertexConsumer param2, FluidState param3) {
        try {
            return this.liquidBlockRenderer.tesselate(param1, param0, param2, param3);
        } catch (Throwable var8) {
            CrashReport var1 = CrashReport.forThrowable(var8, "Tesselating liquid in world");
            CrashReportCategory var2 = var1.addCategory("Block being tesselated");
            CrashReportCategory.populateBlockDetails(var2, param0, null);
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
                    param1.pushPose();
                    param1.mulPose(Vector3f.YP.rotationDegrees(90.0F));
                    int var2 = this.blockColors.getColor(param0, null, null, 0);
                    float var3 = (float)(var2 >> 16 & 0xFF) / 255.0F;
                    float var4 = (float)(var2 >> 8 & 0xFF) / 255.0F;
                    float var5 = (float)(var2 & 0xFF) / 255.0F;
                    this.modelRenderer
                        .renderModel(
                            param1.getPose(),
                            param1.getNormal(),
                            param2.getBuffer(ItemBlockRenderTypes.getRenderType(param0)),
                            param0,
                            var1,
                            var3,
                            var4,
                            var5,
                            param3,
                            param4
                        );
                    param1.popPose();
                    break;
                case ENTITYBLOCK_ANIMATED:
                    param1.pushPose();
                    param1.mulPose(Vector3f.YP.rotationDegrees(90.0F));
                    EntityBlockRenderer.instance.renderByItem(new ItemStack(param0.getBlock()), param1, param2, param3, param4);
                    param1.popPose();
            }

        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager param0) {
        this.liquidBlockRenderer.setupSprites();
    }
}
