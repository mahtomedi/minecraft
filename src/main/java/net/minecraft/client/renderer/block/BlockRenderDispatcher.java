package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Random;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
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
    private final AnimatedEntityBlockRenderer entityBlockRenderer = new AnimatedEntityBlockRenderer();
    private final LiquidBlockRenderer liquidBlockRenderer;
    private final Random random = new Random();

    public BlockRenderDispatcher(BlockModelShaper param0, BlockColors param1) {
        this.blockModelShaper = param0;
        this.modelRenderer = new ModelBlockRenderer(param1);
        this.liquidBlockRenderer = new LiquidBlockRenderer();
    }

    public BlockModelShaper getBlockModelShaper() {
        return this.blockModelShaper;
    }

    public void renderBreakingTexture(BlockState param0, BlockPos param1, TextureAtlasSprite param2, BlockAndBiomeGetter param3) {
        if (param0.getRenderShape() == RenderShape.MODEL) {
            BakedModel var0 = this.blockModelShaper.getBlockModel(param0);
            long var1 = param0.getSeed(param1);
            BakedModel var2 = new SimpleBakedModel.Builder(param0, var0, param2, this.random, var1).build();
            this.modelRenderer.tesselateBlock(param3, var2, param0, param1, Tesselator.getInstance().getBuilder(), true, this.random, var1);
        }
    }

    public boolean renderBatched(BlockState param0, BlockPos param1, BlockAndBiomeGetter param2, BufferBuilder param3, Random param4) {
        try {
            RenderShape var0 = param0.getRenderShape();
            if (var0 == RenderShape.INVISIBLE) {
                return false;
            } else {
                switch(var0) {
                    case MODEL:
                        return this.modelRenderer
                            .tesselateBlock(param2, this.getBlockModel(param0), param0, param1, param3, true, param4, param0.getSeed(param1));
                    case ENTITYBLOCK_ANIMATED:
                        return false;
                    default:
                        return false;
                }
            }
        } catch (Throwable var9) {
            CrashReport var2 = CrashReport.forThrowable(var9, "Tesselating block in world");
            CrashReportCategory var3 = var2.addCategory("Block being tesselated");
            CrashReportCategory.populateBlockDetails(var3, param1, param0);
            throw new ReportedException(var2);
        }
    }

    public boolean renderLiquid(BlockPos param0, BlockAndBiomeGetter param1, BufferBuilder param2, FluidState param3) {
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

    public void renderSingleBlock(BlockState param0, float param1) {
        RenderShape var0 = param0.getRenderShape();
        if (var0 != RenderShape.INVISIBLE) {
            switch(var0) {
                case MODEL:
                    BakedModel var1 = this.getBlockModel(param0);
                    this.modelRenderer.renderSingleBlock(var1, param0, param1, true);
                    break;
                case ENTITYBLOCK_ANIMATED:
                    this.entityBlockRenderer.renderSingleBlock(param0.getBlock(), param1);
            }

        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager param0) {
        this.liquidBlockRenderer.setupSprites();
    }
}
