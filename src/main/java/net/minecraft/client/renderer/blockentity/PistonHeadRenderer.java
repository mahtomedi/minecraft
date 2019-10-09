package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
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
public class PistonHeadRenderer extends BlockEntityRenderer<PistonMovingBlockEntity> {
    private final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

    public PistonHeadRenderer(BlockEntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(
        PistonMovingBlockEntity param0,
        double param1,
        double param2,
        double param3,
        float param4,
        PoseStack param5,
        MultiBufferSource param6,
        int param7,
        int param8
    ) {
        Level var0 = param0.getLevel();
        if (var0 != null) {
            BlockPos var1 = param0.getBlockPos().relative(param0.getMovementDirection().getOpposite());
            BlockState var2 = param0.getMovedState();
            if (!var2.isAir() && !(param0.getProgress(param4) >= 1.0F)) {
                ModelBlockRenderer.enableCaching();
                param5.pushPose();
                param5.translate(
                    (double)((float)(-(var1.getX() & 15)) + param0.getXOff(param4)),
                    (double)((float)(-(var1.getY() & 15)) + param0.getYOff(param4)),
                    (double)((float)(-(var1.getZ() & 15)) + param0.getZOff(param4))
                );
                if (var2.getBlock() == Blocks.PISTON_HEAD && param0.getProgress(param4) <= 4.0F) {
                    var2 = var2.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(true));
                    this.renderBlock(var1, var2, param5, param6, var0, false, param8);
                } else if (param0.isSourcePiston() && !param0.isExtending()) {
                    PistonType var3 = var2.getBlock() == Blocks.STICKY_PISTON ? PistonType.STICKY : PistonType.DEFAULT;
                    BlockState var4 = Blocks.PISTON_HEAD
                        .defaultBlockState()
                        .setValue(PistonHeadBlock.TYPE, var3)
                        .setValue(PistonHeadBlock.FACING, var2.getValue(PistonBaseBlock.FACING));
                    var4 = var4.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(param0.getProgress(param4) >= 0.5F));
                    this.renderBlock(var1, var4, param5, param6, var0, false, param8);
                    BlockPos var5 = var1.relative(param0.getMovementDirection());
                    param5.popPose();
                    param5.translate((double)(-(var5.getX() & 15)), (double)(-(var5.getY() & 15)), (double)(-(var5.getZ() & 15)));
                    var2 = var2.setValue(PistonBaseBlock.EXTENDED, Boolean.valueOf(true));
                    this.renderBlock(var5, var2, param5, param6, var0, true, param8);
                    param5.pushPose();
                } else {
                    this.renderBlock(var1, var2, param5, param6, var0, false, param8);
                }

                param5.popPose();
                ModelBlockRenderer.clearCache();
            }
        }
    }

    private void renderBlock(BlockPos param0, BlockState param1, PoseStack param2, MultiBufferSource param3, Level param4, boolean param5, int param6) {
        RenderType var0 = RenderType.getChunkRenderType(param1);
        VertexConsumer var1 = param3.getBuffer(var0);
        this.blockRenderer
            .getModelRenderer()
            .tesselateBlock(
                param4, this.blockRenderer.getBlockModel(param1), param1, param0, param2, var1, param5, new Random(), param1.getSeed(param0), param6
            );
    }
}
