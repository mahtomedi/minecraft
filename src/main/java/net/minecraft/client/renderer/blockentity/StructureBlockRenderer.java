package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StructureBlockRenderer implements BlockEntityRenderer<StructureBlockEntity> {
    public StructureBlockRenderer(BlockEntityRendererProvider.Context param0) {
    }

    public void render(StructureBlockEntity param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        if (Minecraft.getInstance().player.canUseGameMasterBlocks() || Minecraft.getInstance().player.isSpectator()) {
            BlockPos var0 = param0.getStructurePos();
            Vec3i var1 = param0.getStructureSize();
            if (var1.getX() >= 1 && var1.getY() >= 1 && var1.getZ() >= 1) {
                if (param0.getMode() == StructureMode.SAVE || param0.getMode() == StructureMode.LOAD) {
                    double var2 = (double)var0.getX();
                    double var3 = (double)var0.getZ();
                    double var4 = (double)var0.getY();
                    double var5 = var4 + (double)var1.getY();
                    double var6;
                    double var7;
                    switch(param0.getMirror()) {
                        case LEFT_RIGHT:
                            var6 = (double)var1.getX();
                            var7 = (double)(-var1.getZ());
                            break;
                        case FRONT_BACK:
                            var6 = (double)(-var1.getX());
                            var7 = (double)var1.getZ();
                            break;
                        default:
                            var6 = (double)var1.getX();
                            var7 = (double)var1.getZ();
                    }

                    double var24;
                    double var25;
                    double var26;
                    double var27;
                    switch(param0.getRotation()) {
                        case CLOCKWISE_90:
                            var24 = var7 < 0.0 ? var2 : var2 + 1.0;
                            var25 = var6 < 0.0 ? var3 + 1.0 : var3;
                            var26 = var24 - var7;
                            var27 = var25 + var6;
                            break;
                        case CLOCKWISE_180:
                            var24 = var6 < 0.0 ? var2 : var2 + 1.0;
                            var25 = var7 < 0.0 ? var3 : var3 + 1.0;
                            var26 = var24 - var6;
                            var27 = var25 - var7;
                            break;
                        case COUNTERCLOCKWISE_90:
                            var24 = var7 < 0.0 ? var2 + 1.0 : var2;
                            var25 = var6 < 0.0 ? var3 : var3 + 1.0;
                            var26 = var24 + var7;
                            var27 = var25 - var6;
                            break;
                        default:
                            var24 = var6 < 0.0 ? var2 + 1.0 : var2;
                            var25 = var7 < 0.0 ? var3 + 1.0 : var3;
                            var26 = var24 + var6;
                            var27 = var25 + var7;
                    }

                    float var28 = 1.0F;
                    float var29 = 0.9F;
                    float var30 = 0.5F;
                    VertexConsumer var31 = param3.getBuffer(RenderType.lines());
                    if (param0.getMode() == StructureMode.SAVE || param0.getShowBoundingBox()) {
                        LevelRenderer.renderLineBox(param2, var31, var24, var4, var25, var26, var5, var27, 0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F);
                    }

                    if (param0.getMode() == StructureMode.SAVE && param0.getShowAir()) {
                        this.renderInvisibleBlocks(param0, var31, var0, param2);
                    }

                }
            }
        }
    }

    private void renderInvisibleBlocks(StructureBlockEntity param0, VertexConsumer param1, BlockPos param2, PoseStack param3) {
        BlockGetter var0 = param0.getLevel();
        BlockPos var1 = param0.getBlockPos();
        BlockPos var2 = var1.offset(param2);

        for(BlockPos var3 : BlockPos.betweenClosed(var2, var2.offset(param0.getStructureSize()).offset(-1, -1, -1))) {
            BlockState var4 = var0.getBlockState(var3);
            boolean var5 = var4.isAir();
            boolean var6 = var4.is(Blocks.STRUCTURE_VOID);
            boolean var7 = var4.is(Blocks.BARRIER);
            boolean var8 = var4.is(Blocks.LIGHT);
            boolean var9 = var6 || var7 || var8;
            if (var5 || var9) {
                float var10 = var5 ? 0.05F : 0.0F;
                double var11 = (double)((float)(var3.getX() - var1.getX()) + 0.45F - var10);
                double var12 = (double)((float)(var3.getY() - var1.getY()) + 0.45F - var10);
                double var13 = (double)((float)(var3.getZ() - var1.getZ()) + 0.45F - var10);
                double var14 = (double)((float)(var3.getX() - var1.getX()) + 0.55F + var10);
                double var15 = (double)((float)(var3.getY() - var1.getY()) + 0.55F + var10);
                double var16 = (double)((float)(var3.getZ() - var1.getZ()) + 0.55F + var10);
                if (var5) {
                    LevelRenderer.renderLineBox(param3, param1, var11, var12, var13, var14, var15, var16, 0.5F, 0.5F, 1.0F, 1.0F, 0.5F, 0.5F, 1.0F);
                } else if (var6) {
                    LevelRenderer.renderLineBox(param3, param1, var11, var12, var13, var14, var15, var16, 1.0F, 0.75F, 0.75F, 1.0F, 1.0F, 0.75F, 0.75F);
                } else if (var7) {
                    LevelRenderer.renderLineBox(param3, param1, var11, var12, var13, var14, var15, var16, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F);
                } else if (var8) {
                    LevelRenderer.renderLineBox(param3, param1, var11, var12, var13, var14, var15, var16, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F);
                }
            }
        }

    }

    public boolean shouldRenderOffScreen(StructureBlockEntity param0) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}
