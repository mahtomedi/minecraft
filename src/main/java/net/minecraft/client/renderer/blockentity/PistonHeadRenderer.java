package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
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
public class PistonHeadRenderer extends BatchedBlockEntityRenderer<PistonMovingBlockEntity> {
    private final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

    protected void renderToBuffer(
        PistonMovingBlockEntity param0,
        double param1,
        double param2,
        double param3,
        float param4,
        int param5,
        RenderType param6,
        BufferBuilder param7,
        int param8,
        int param9
    ) {
        BlockPos var0 = param0.getBlockPos().relative(param0.getMovementDirection().getOpposite());
        BlockState var1 = param0.getMovedState();
        if (!var1.isAir() && !(param0.getProgress(param4) >= 1.0F)) {
            ModelBlockRenderer.enableCaching();
            param7.offset(
                param1 - (double)var0.getX() + (double)param0.getXOff(param4),
                param2 - (double)var0.getY() + (double)param0.getYOff(param4),
                param3 - (double)var0.getZ() + (double)param0.getZOff(param4)
            );
            Level var2 = this.getLevel();
            if (var1.getBlock() == Blocks.PISTON_HEAD && param0.getProgress(param4) <= 4.0F) {
                var1 = var1.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(true));
                this.renderBlock(var0, var1, param7, var2, false);
            } else if (param0.isSourcePiston() && !param0.isExtending()) {
                PistonType var3 = var1.getBlock() == Blocks.STICKY_PISTON ? PistonType.STICKY : PistonType.DEFAULT;
                BlockState var4 = Blocks.PISTON_HEAD
                    .defaultBlockState()
                    .setValue(PistonHeadBlock.TYPE, var3)
                    .setValue(PistonHeadBlock.FACING, var1.getValue(PistonBaseBlock.FACING));
                var4 = var4.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(param0.getProgress(param4) >= 0.5F));
                this.renderBlock(var0, var4, param7, var2, false);
                BlockPos var5 = var0.relative(param0.getMovementDirection());
                param7.offset(param1 - (double)var5.getX(), param2 - (double)var5.getY(), param3 - (double)var5.getZ());
                var1 = var1.setValue(PistonBaseBlock.EXTENDED, Boolean.valueOf(true));
                this.renderBlock(var5, var1, param7, var2, true);
            } else {
                this.renderBlock(var0, var1, param7, var2, false);
            }

            ModelBlockRenderer.clearCache();
        }
    }

    private boolean renderBlock(BlockPos param0, BlockState param1, BufferBuilder param2, Level param3, boolean param4) {
        return this.blockRenderer
            .getModelRenderer()
            .tesselateBlock(param3, this.blockRenderer.getBlockModel(param1), param1, param0, param2, param4, new Random(), param1.getSeed(param0));
    }
}
