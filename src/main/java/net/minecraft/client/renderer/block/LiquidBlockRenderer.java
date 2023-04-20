package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LiquidBlockRenderer {
    private static final float MAX_FLUID_HEIGHT = 0.8888889F;
    private final TextureAtlasSprite[] lavaIcons = new TextureAtlasSprite[2];
    private final TextureAtlasSprite[] waterIcons = new TextureAtlasSprite[2];
    private TextureAtlasSprite waterOverlay;

    protected void setupSprites() {
        this.lavaIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.LAVA.defaultBlockState()).getParticleIcon();
        this.lavaIcons[1] = ModelBakery.LAVA_FLOW.sprite();
        this.waterIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).getParticleIcon();
        this.waterIcons[1] = ModelBakery.WATER_FLOW.sprite();
        this.waterOverlay = ModelBakery.WATER_OVERLAY.sprite();
    }

    private static boolean isNeighborSameFluid(FluidState param0, FluidState param1) {
        return param1.getType().isSame(param0.getType());
    }

    private static boolean isFaceOccludedByState(BlockGetter param0, Direction param1, float param2, BlockPos param3, BlockState param4) {
        if (param4.canOcclude()) {
            VoxelShape var0 = Shapes.box(0.0, 0.0, 0.0, 1.0, (double)param2, 1.0);
            VoxelShape var1 = param4.getOcclusionShape(param0, param3);
            return Shapes.blockOccudes(var0, var1, param1);
        } else {
            return false;
        }
    }

    private static boolean isFaceOccludedByNeighbor(BlockGetter param0, BlockPos param1, Direction param2, float param3, BlockState param4) {
        return isFaceOccludedByState(param0, param2, param3, param1.relative(param2), param4);
    }

    private static boolean isFaceOccludedBySelf(BlockGetter param0, BlockPos param1, BlockState param2, Direction param3) {
        return isFaceOccludedByState(param0, param3.getOpposite(), 1.0F, param1, param2);
    }

    public static boolean shouldRenderFace(
        BlockAndTintGetter param0, BlockPos param1, FluidState param2, BlockState param3, Direction param4, FluidState param5
    ) {
        return !isFaceOccludedBySelf(param0, param1, param3, param4) && !isNeighborSameFluid(param2, param5);
    }

    public void tesselate(BlockAndTintGetter param0, BlockPos param1, VertexConsumer param2, BlockState param3, FluidState param4) {
        boolean var0 = param4.is(FluidTags.LAVA);
        TextureAtlasSprite[] var1 = var0 ? this.lavaIcons : this.waterIcons;
        int var2 = var0 ? 16777215 : BiomeColors.getAverageWaterColor(param0, param1);
        float var3 = (float)(var2 >> 16 & 0xFF) / 255.0F;
        float var4 = (float)(var2 >> 8 & 0xFF) / 255.0F;
        float var5 = (float)(var2 & 0xFF) / 255.0F;
        BlockState var6 = param0.getBlockState(param1.relative(Direction.DOWN));
        FluidState var7 = var6.getFluidState();
        BlockState var8 = param0.getBlockState(param1.relative(Direction.UP));
        FluidState var9 = var8.getFluidState();
        BlockState var10 = param0.getBlockState(param1.relative(Direction.NORTH));
        FluidState var11 = var10.getFluidState();
        BlockState var12 = param0.getBlockState(param1.relative(Direction.SOUTH));
        FluidState var13 = var12.getFluidState();
        BlockState var14 = param0.getBlockState(param1.relative(Direction.WEST));
        FluidState var15 = var14.getFluidState();
        BlockState var16 = param0.getBlockState(param1.relative(Direction.EAST));
        FluidState var17 = var16.getFluidState();
        boolean var18 = !isNeighborSameFluid(param4, var9);
        boolean var19 = shouldRenderFace(param0, param1, param4, param3, Direction.DOWN, var7)
            && !isFaceOccludedByNeighbor(param0, param1, Direction.DOWN, 0.8888889F, var6);
        boolean var20 = shouldRenderFace(param0, param1, param4, param3, Direction.NORTH, var11);
        boolean var21 = shouldRenderFace(param0, param1, param4, param3, Direction.SOUTH, var13);
        boolean var22 = shouldRenderFace(param0, param1, param4, param3, Direction.WEST, var15);
        boolean var23 = shouldRenderFace(param0, param1, param4, param3, Direction.EAST, var17);
        if (var18 || var19 || var23 || var22 || var20 || var21) {
            float var24 = param0.getShade(Direction.DOWN, true);
            float var25 = param0.getShade(Direction.UP, true);
            float var26 = param0.getShade(Direction.NORTH, true);
            float var27 = param0.getShade(Direction.WEST, true);
            Fluid var28 = param4.getType();
            float var29 = this.getHeight(param0, var28, param1, param3, param4);
            float var30;
            float var31;
            float var32;
            float var33;
            if (var29 >= 1.0F) {
                var30 = 1.0F;
                var31 = 1.0F;
                var32 = 1.0F;
                var33 = 1.0F;
            } else {
                float var34 = this.getHeight(param0, var28, param1.north(), var10, var11);
                float var35 = this.getHeight(param0, var28, param1.south(), var12, var13);
                float var36 = this.getHeight(param0, var28, param1.east(), var16, var17);
                float var37 = this.getHeight(param0, var28, param1.west(), var14, var15);
                var30 = this.calculateAverageHeight(param0, var28, var29, var34, var36, param1.relative(Direction.NORTH).relative(Direction.EAST));
                var31 = this.calculateAverageHeight(param0, var28, var29, var34, var37, param1.relative(Direction.NORTH).relative(Direction.WEST));
                var32 = this.calculateAverageHeight(param0, var28, var29, var35, var36, param1.relative(Direction.SOUTH).relative(Direction.EAST));
                var33 = this.calculateAverageHeight(param0, var28, var29, var35, var37, param1.relative(Direction.SOUTH).relative(Direction.WEST));
            }

            double var42 = (double)(param1.getX() & 15);
            double var43 = (double)(param1.getY() & 15);
            double var44 = (double)(param1.getZ() & 15);
            float var45 = 0.001F;
            float var46 = var19 ? 0.001F : 0.0F;
            if (var18 && !isFaceOccludedByNeighbor(param0, param1, Direction.UP, Math.min(Math.min(var31, var33), Math.min(var32, var30)), var8)) {
                var31 -= 0.001F;
                var33 -= 0.001F;
                var32 -= 0.001F;
                var30 -= 0.001F;
                Vec3 var47 = param4.getFlow(param0, param1);
                float var49;
                float var51;
                float var53;
                float var55;
                float var50;
                float var52;
                float var54;
                float var56;
                if (var47.x == 0.0 && var47.z == 0.0) {
                    TextureAtlasSprite var48 = var1[0];
                    var49 = var48.getU(0.0);
                    var50 = var48.getV(0.0);
                    var51 = var49;
                    var52 = var48.getV(16.0);
                    var53 = var48.getU(16.0);
                    var54 = var52;
                    var55 = var53;
                    var56 = var50;
                } else {
                    TextureAtlasSprite var57 = var1[1];
                    float var58 = (float)Mth.atan2(var47.z, var47.x) - (float) (Math.PI / 2);
                    float var59 = Mth.sin(var58) * 0.25F;
                    float var60 = Mth.cos(var58) * 0.25F;
                    float var61 = 8.0F;
                    var49 = var57.getU((double)(8.0F + (-var60 - var59) * 16.0F));
                    var50 = var57.getV((double)(8.0F + (-var60 + var59) * 16.0F));
                    var51 = var57.getU((double)(8.0F + (-var60 + var59) * 16.0F));
                    var52 = var57.getV((double)(8.0F + (var60 + var59) * 16.0F));
                    var53 = var57.getU((double)(8.0F + (var60 + var59) * 16.0F));
                    var54 = var57.getV((double)(8.0F + (var60 - var59) * 16.0F));
                    var55 = var57.getU((double)(8.0F + (var60 - var59) * 16.0F));
                    var56 = var57.getV((double)(8.0F + (-var60 - var59) * 16.0F));
                }

                float var70 = (var49 + var51 + var53 + var55) / 4.0F;
                float var71 = (var50 + var52 + var54 + var56) / 4.0F;
                float var72 = var1[0].uvShrinkRatio();
                var49 = Mth.lerp(var72, var49, var70);
                var51 = Mth.lerp(var72, var51, var70);
                var53 = Mth.lerp(var72, var53, var70);
                var55 = Mth.lerp(var72, var55, var70);
                var50 = Mth.lerp(var72, var50, var71);
                var52 = Mth.lerp(var72, var52, var71);
                var54 = Mth.lerp(var72, var54, var71);
                var56 = Mth.lerp(var72, var56, var71);
                int var73 = this.getLightColor(param0, param1);
                float var74 = var25 * var3;
                float var75 = var25 * var4;
                float var76 = var25 * var5;
                this.vertex(param2, var42 + 0.0, var43 + (double)var31, var44 + 0.0, var74, var75, var76, var49, var50, var73);
                this.vertex(param2, var42 + 0.0, var43 + (double)var33, var44 + 1.0, var74, var75, var76, var51, var52, var73);
                this.vertex(param2, var42 + 1.0, var43 + (double)var32, var44 + 1.0, var74, var75, var76, var53, var54, var73);
                this.vertex(param2, var42 + 1.0, var43 + (double)var30, var44 + 0.0, var74, var75, var76, var55, var56, var73);
                if (param4.shouldRenderBackwardUpFace(param0, param1.above())) {
                    this.vertex(param2, var42 + 0.0, var43 + (double)var31, var44 + 0.0, var74, var75, var76, var49, var50, var73);
                    this.vertex(param2, var42 + 1.0, var43 + (double)var30, var44 + 0.0, var74, var75, var76, var55, var56, var73);
                    this.vertex(param2, var42 + 1.0, var43 + (double)var32, var44 + 1.0, var74, var75, var76, var53, var54, var73);
                    this.vertex(param2, var42 + 0.0, var43 + (double)var33, var44 + 1.0, var74, var75, var76, var51, var52, var73);
                }
            }

            if (var19) {
                float var77 = var1[0].getU0();
                float var78 = var1[0].getU1();
                float var79 = var1[0].getV0();
                float var80 = var1[0].getV1();
                int var81 = this.getLightColor(param0, param1.below());
                float var82 = var24 * var3;
                float var83 = var24 * var4;
                float var84 = var24 * var5;
                this.vertex(param2, var42, var43 + (double)var46, var44 + 1.0, var82, var83, var84, var77, var80, var81);
                this.vertex(param2, var42, var43 + (double)var46, var44, var82, var83, var84, var77, var79, var81);
                this.vertex(param2, var42 + 1.0, var43 + (double)var46, var44, var82, var83, var84, var78, var79, var81);
                this.vertex(param2, var42 + 1.0, var43 + (double)var46, var44 + 1.0, var82, var83, var84, var78, var80, var81);
            }

            int var85 = this.getLightColor(param0, param1);

            for(Direction var86 : Direction.Plane.HORIZONTAL) {
                float var87;
                float var88;
                double var89;
                double var91;
                double var90;
                double var92;
                boolean var93;
                switch(var86) {
                    case NORTH:
                        var87 = var31;
                        var88 = var30;
                        var89 = var42;
                        var90 = var42 + 1.0;
                        var91 = var44 + 0.001F;
                        var92 = var44 + 0.001F;
                        var93 = var20;
                        break;
                    case SOUTH:
                        var87 = var32;
                        var88 = var33;
                        var89 = var42 + 1.0;
                        var90 = var42;
                        var91 = var44 + 1.0 - 0.001F;
                        var92 = var44 + 1.0 - 0.001F;
                        var93 = var21;
                        break;
                    case WEST:
                        var87 = var33;
                        var88 = var31;
                        var89 = var42 + 0.001F;
                        var90 = var42 + 0.001F;
                        var91 = var44 + 1.0;
                        var92 = var44;
                        var93 = var22;
                        break;
                    default:
                        var87 = var30;
                        var88 = var32;
                        var89 = var42 + 1.0 - 0.001F;
                        var90 = var42 + 1.0 - 0.001F;
                        var91 = var44;
                        var92 = var44 + 1.0;
                        var93 = var23;
                }

                if (var93 && !isFaceOccludedByNeighbor(param0, param1, var86, Math.max(var87, var88), param0.getBlockState(param1.relative(var86)))) {
                    BlockPos var115 = param1.relative(var86);
                    TextureAtlasSprite var116 = var1[1];
                    if (!var0) {
                        Block var117 = param0.getBlockState(var115).getBlock();
                        if (var117 instanceof HalfTransparentBlock || var117 instanceof LeavesBlock) {
                            var116 = this.waterOverlay;
                        }
                    }

                    float var118 = var116.getU(0.0);
                    float var119 = var116.getU(8.0);
                    float var120 = var116.getV((double)((1.0F - var87) * 16.0F * 0.5F));
                    float var121 = var116.getV((double)((1.0F - var88) * 16.0F * 0.5F));
                    float var122 = var116.getV(8.0);
                    float var123 = var86.getAxis() == Direction.Axis.Z ? var26 : var27;
                    float var124 = var25 * var123 * var3;
                    float var125 = var25 * var123 * var4;
                    float var126 = var25 * var123 * var5;
                    this.vertex(param2, var89, var43 + (double)var87, var91, var124, var125, var126, var118, var120, var85);
                    this.vertex(param2, var90, var43 + (double)var88, var92, var124, var125, var126, var119, var121, var85);
                    this.vertex(param2, var90, var43 + (double)var46, var92, var124, var125, var126, var119, var122, var85);
                    this.vertex(param2, var89, var43 + (double)var46, var91, var124, var125, var126, var118, var122, var85);
                    if (var116 != this.waterOverlay) {
                        this.vertex(param2, var89, var43 + (double)var46, var91, var124, var125, var126, var118, var122, var85);
                        this.vertex(param2, var90, var43 + (double)var46, var92, var124, var125, var126, var119, var122, var85);
                        this.vertex(param2, var90, var43 + (double)var88, var92, var124, var125, var126, var119, var121, var85);
                        this.vertex(param2, var89, var43 + (double)var87, var91, var124, var125, var126, var118, var120, var85);
                    }
                }
            }

        }
    }

    private float calculateAverageHeight(BlockAndTintGetter param0, Fluid param1, float param2, float param3, float param4, BlockPos param5) {
        if (!(param4 >= 1.0F) && !(param3 >= 1.0F)) {
            float[] var0 = new float[2];
            if (param4 > 0.0F || param3 > 0.0F) {
                float var1 = this.getHeight(param0, param1, param5);
                if (var1 >= 1.0F) {
                    return 1.0F;
                }

                this.addWeightedHeight(var0, var1);
            }

            this.addWeightedHeight(var0, param2);
            this.addWeightedHeight(var0, param4);
            this.addWeightedHeight(var0, param3);
            return var0[0] / var0[1];
        } else {
            return 1.0F;
        }
    }

    private void addWeightedHeight(float[] param0, float param1) {
        if (param1 >= 0.8F) {
            param0[0] += param1 * 10.0F;
            param0[1] += 10.0F;
        } else if (param1 >= 0.0F) {
            param0[0] += param1;
            param0[1]++;
        }

    }

    private float getHeight(BlockAndTintGetter param0, Fluid param1, BlockPos param2) {
        BlockState var0 = param0.getBlockState(param2);
        return this.getHeight(param0, param1, param2, var0, var0.getFluidState());
    }

    private float getHeight(BlockAndTintGetter param0, Fluid param1, BlockPos param2, BlockState param3, FluidState param4) {
        if (param1.isSame(param4.getType())) {
            BlockState var0 = param0.getBlockState(param2.above());
            return param1.isSame(var0.getFluidState().getType()) ? 1.0F : param4.getOwnHeight();
        } else {
            return !param3.isSolid() ? 0.0F : -1.0F;
        }
    }

    private void vertex(
        VertexConsumer param0, double param1, double param2, double param3, float param4, float param5, float param6, float param7, float param8, int param9
    ) {
        param0.vertex(param1, param2, param3).color(param4, param5, param6, 1.0F).uv(param7, param8).uv2(param9).normal(0.0F, 1.0F, 0.0F).endVertex();
    }

    private int getLightColor(BlockAndTintGetter param0, BlockPos param1) {
        int var0 = LevelRenderer.getLightColor(param0, param1);
        int var1 = LevelRenderer.getLightColor(param0, param1.above());
        int var2 = var0 & 0xFF;
        int var3 = var1 & 0xFF;
        int var4 = var0 >> 16 & 0xFF;
        int var5 = var1 >> 16 & 0xFF;
        return (var2 > var3 ? var2 : var3) | (var4 > var5 ? var4 : var5) << 16;
    }
}
