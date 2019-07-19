package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PistonHeadRenderer extends BlockEntityRenderer<PistonMovingBlockEntity> {
    private final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

    public void render(PistonMovingBlockEntity param0, double param1, double param2, double param3, float param4, int param5) {
        BlockPos var0 = param0.getBlockPos().relative(param0.getMovementDirection().getOpposite());
        BlockState var1 = param0.getMovedState();
        if (!var1.isAir() && !(param0.getProgress(param4) >= 1.0F)) {
            Tesselator var2 = Tesselator.getInstance();
            BufferBuilder var3 = var2.getBuilder();
            this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
            Lighting.turnOff();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableBlend();
            GlStateManager.disableCull();
            if (Minecraft.useAmbientOcclusion()) {
                GlStateManager.shadeModel(7425);
            } else {
                GlStateManager.shadeModel(7424);
            }

            ModelBlockRenderer.enableCaching();
            var3.begin(7, DefaultVertexFormat.BLOCK);
            var3.offset(
                param1 - (double)var0.getX() + (double)param0.getXOff(param4),
                param2 - (double)var0.getY() + (double)param0.getYOff(param4),
                param3 - (double)var0.getZ() + (double)param0.getZOff(param4)
            );
            Level var4 = this.getLevel();
            if (var1.getBlock() == Blocks.PISTON_HEAD && param0.getProgress(param4) <= 4.0F) {
                var1 = var1.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(true));
                this.renderBlock(var0, var1, var3, var4, false);
            } else if (param0.isSourcePiston() && !param0.isExtending()) {
                PistonType var5 = var1.getBlock() == Blocks.STICKY_PISTON ? PistonType.STICKY : PistonType.DEFAULT;
                BlockState var6 = Blocks.PISTON_HEAD
                    .defaultBlockState()
                    .setValue(PistonHeadBlock.TYPE, var5)
                    .setValue(PistonHeadBlock.FACING, var1.getValue(PistonBaseBlock.FACING));
                var6 = var6.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(param0.getProgress(param4) >= 0.5F));
                this.renderBlock(var0, var6, var3, var4, false);
                BlockPos var7 = var0.relative(param0.getMovementDirection());
                var3.offset(param1 - (double)var7.getX(), param2 - (double)var7.getY(), param3 - (double)var7.getZ());
                var1 = var1.setValue(PistonBaseBlock.EXTENDED, Boolean.valueOf(true));
                this.renderBlock(var7, var1, var3, var4, true);
            } else {
                this.renderBlock(var0, var1, var3, var4, false);
            }

            var3.offset(0.0, 0.0, 0.0);
            var2.end();
            ModelBlockRenderer.clearCache();
            Lighting.turnOn();
        }
    }

    private boolean renderBlock(BlockPos param0, BlockState param1, BufferBuilder param2, Level param3, boolean param4) {
        return this.blockRenderer
            .getModelRenderer()
            .tesselateBlock(param3, this.blockRenderer.getBlockModel(param1), param1, param0, param2, param4, new Random(), param1.getSeed(param0));
    }
}
