package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StructureBlockRenderer extends BlockEntityRenderer<StructureBlockEntity> {
    public void render(StructureBlockEntity param0, double param1, double param2, double param3, float param4, int param5) {
        if (Minecraft.getInstance().player.canUseGameMasterBlocks() || Minecraft.getInstance().player.isSpectator()) {
            super.render(param0, param1, param2, param3, param4, param5);
            BlockPos var0 = param0.getStructurePos();
            BlockPos var1 = param0.getStructureSize();
            if (var1.getX() >= 1 && var1.getY() >= 1 && var1.getZ() >= 1) {
                if (param0.getMode() == StructureMode.SAVE || param0.getMode() == StructureMode.LOAD) {
                    double var2 = 0.01;
                    double var3 = (double)var0.getX();
                    double var4 = (double)var0.getZ();
                    double var5 = param2 + (double)var0.getY() - 0.01;
                    double var6 = var5 + (double)var1.getY() + 0.02;
                    double var7;
                    double var8;
                    switch(param0.getMirror()) {
                        case LEFT_RIGHT:
                            var7 = (double)var1.getX() + 0.02;
                            var8 = -((double)var1.getZ() + 0.02);
                            break;
                        case FRONT_BACK:
                            var7 = -((double)var1.getX() + 0.02);
                            var8 = (double)var1.getZ() + 0.02;
                            break;
                        default:
                            var7 = (double)var1.getX() + 0.02;
                            var8 = (double)var1.getZ() + 0.02;
                    }

                    double var25;
                    double var26;
                    double var27;
                    double var28;
                    switch(param0.getRotation()) {
                        case CLOCKWISE_90:
                            var25 = param1 + (var8 < 0.0 ? var3 - 0.01 : var3 + 1.0 + 0.01);
                            var26 = param3 + (var7 < 0.0 ? var4 + 1.0 + 0.01 : var4 - 0.01);
                            var27 = var25 - var8;
                            var28 = var26 + var7;
                            break;
                        case CLOCKWISE_180:
                            var25 = param1 + (var7 < 0.0 ? var3 - 0.01 : var3 + 1.0 + 0.01);
                            var26 = param3 + (var8 < 0.0 ? var4 - 0.01 : var4 + 1.0 + 0.01);
                            var27 = var25 - var7;
                            var28 = var26 - var8;
                            break;
                        case COUNTERCLOCKWISE_90:
                            var25 = param1 + (var8 < 0.0 ? var3 + 1.0 + 0.01 : var3 - 0.01);
                            var26 = param3 + (var7 < 0.0 ? var4 - 0.01 : var4 + 1.0 + 0.01);
                            var27 = var25 + var8;
                            var28 = var26 - var7;
                            break;
                        default:
                            var25 = param1 + (var7 < 0.0 ? var3 + 1.0 + 0.01 : var3 - 0.01);
                            var26 = param3 + (var8 < 0.0 ? var4 + 1.0 + 0.01 : var4 - 0.01);
                            var27 = var25 + var7;
                            var28 = var26 + var8;
                    }

                    int var29 = 255;
                    int var30 = 223;
                    int var31 = 127;
                    Tesselator var32 = Tesselator.getInstance();
                    BufferBuilder var33 = var32.getBuilder();
                    RenderSystem.disableFog();
                    RenderSystem.disableLighting();
                    RenderSystem.disableTexture();
                    RenderSystem.enableBlend();
                    RenderSystem.blendFuncSeparate(
                        GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                        GlStateManager.SourceFactor.ONE,
                        GlStateManager.DestFactor.ZERO
                    );
                    this.setOverlayRenderState(true);
                    if (param0.getMode() == StructureMode.SAVE || param0.getShowBoundingBox()) {
                        this.renderBox(var32, var33, var25, var5, var26, var27, var6, var28, 255, 223, 127);
                    }

                    if (param0.getMode() == StructureMode.SAVE && param0.getShowAir()) {
                        this.renderInvisibleBlocks(param0, param1, param2, param3, var0, var32, var33, true);
                        this.renderInvisibleBlocks(param0, param1, param2, param3, var0, var32, var33, false);
                    }

                    this.setOverlayRenderState(false);
                    RenderSystem.lineWidth(1.0F);
                    RenderSystem.enableLighting();
                    RenderSystem.enableTexture();
                    RenderSystem.enableDepthTest();
                    RenderSystem.depthMask(true);
                    RenderSystem.enableFog();
                }
            }
        }
    }

    private void renderInvisibleBlocks(
        StructureBlockEntity param0, double param1, double param2, double param3, BlockPos param4, Tesselator param5, BufferBuilder param6, boolean param7
    ) {
        RenderSystem.lineWidth(param7 ? 3.0F : 1.0F);
        param6.begin(3, DefaultVertexFormat.POSITION_COLOR);
        BlockGetter var0 = param0.getLevel();
        BlockPos var1 = param0.getBlockPos();
        BlockPos var2 = var1.offset(param4);

        for(BlockPos var3 : BlockPos.betweenClosed(var2, var2.offset(param0.getStructureSize()).offset(-1, -1, -1))) {
            BlockState var4 = var0.getBlockState(var3);
            boolean var5 = var4.isAir();
            boolean var6 = var4.getBlock() == Blocks.STRUCTURE_VOID;
            if (var5 || var6) {
                float var7 = var5 ? 0.05F : 0.0F;
                double var8 = (double)((float)(var3.getX() - var1.getX()) + 0.45F) + param1 - (double)var7;
                double var9 = (double)((float)(var3.getY() - var1.getY()) + 0.45F) + param2 - (double)var7;
                double var10 = (double)((float)(var3.getZ() - var1.getZ()) + 0.45F) + param3 - (double)var7;
                double var11 = (double)((float)(var3.getX() - var1.getX()) + 0.55F) + param1 + (double)var7;
                double var12 = (double)((float)(var3.getY() - var1.getY()) + 0.55F) + param2 + (double)var7;
                double var13 = (double)((float)(var3.getZ() - var1.getZ()) + 0.55F) + param3 + (double)var7;
                if (param7) {
                    LevelRenderer.addChainedLineBoxVertices(param6, var8, var9, var10, var11, var12, var13, 0.0F, 0.0F, 0.0F, 1.0F);
                } else if (var5) {
                    LevelRenderer.addChainedLineBoxVertices(param6, var8, var9, var10, var11, var12, var13, 0.5F, 0.5F, 1.0F, 1.0F);
                } else {
                    LevelRenderer.addChainedLineBoxVertices(param6, var8, var9, var10, var11, var12, var13, 1.0F, 0.25F, 0.25F, 1.0F);
                }
            }
        }

        param5.end();
    }

    private void renderBox(
        Tesselator param0,
        BufferBuilder param1,
        double param2,
        double param3,
        double param4,
        double param5,
        double param6,
        double param7,
        int param8,
        int param9,
        int param10
    ) {
        RenderSystem.lineWidth(2.0F);
        param1.begin(3, DefaultVertexFormat.POSITION_COLOR);
        param1.vertex(param2, param3, param4).color((float)param9, (float)param9, (float)param9, 0.0F).endVertex();
        param1.vertex(param2, param3, param4).color(param9, param9, param9, param8).endVertex();
        param1.vertex(param5, param3, param4).color(param9, param10, param10, param8).endVertex();
        param1.vertex(param5, param3, param7).color(param9, param9, param9, param8).endVertex();
        param1.vertex(param2, param3, param7).color(param9, param9, param9, param8).endVertex();
        param1.vertex(param2, param3, param4).color(param10, param10, param9, param8).endVertex();
        param1.vertex(param2, param6, param4).color(param10, param9, param10, param8).endVertex();
        param1.vertex(param5, param6, param4).color(param9, param9, param9, param8).endVertex();
        param1.vertex(param5, param6, param7).color(param9, param9, param9, param8).endVertex();
        param1.vertex(param2, param6, param7).color(param9, param9, param9, param8).endVertex();
        param1.vertex(param2, param6, param4).color(param9, param9, param9, param8).endVertex();
        param1.vertex(param2, param6, param7).color(param9, param9, param9, param8).endVertex();
        param1.vertex(param2, param3, param7).color(param9, param9, param9, param8).endVertex();
        param1.vertex(param5, param3, param7).color(param9, param9, param9, param8).endVertex();
        param1.vertex(param5, param6, param7).color(param9, param9, param9, param8).endVertex();
        param1.vertex(param5, param6, param4).color(param9, param9, param9, param8).endVertex();
        param1.vertex(param5, param3, param4).color(param9, param9, param9, param8).endVertex();
        param1.vertex(param5, param3, param4).color((float)param9, (float)param9, (float)param9, 0.0F).endVertex();
        param0.end();
        RenderSystem.lineWidth(1.0F);
    }

    public boolean shouldRenderOffScreen(StructureBlockEntity param0) {
        return true;
    }
}
