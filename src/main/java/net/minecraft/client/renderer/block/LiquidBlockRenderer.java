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
                float var72 = (float)var1[0].getWidth() / (var1[0].getU1() - var1[0].getU0());
                float var73 = (float)var1[0].getHeight() / (var1[0].getV1() - var1[0].getV0());
                float var74 = 4.0F / Math.max(var73, var72);
                var49 = Mth.lerp(var74, var49, var70);
                var51 = Mth.lerp(var74, var51, var70);
                var53 = Mth.lerp(var74, var53, var70);
                var55 = Mth.lerp(var74, var55, var70);
                var50 = Mth.lerp(var74, var50, var71);
                var52 = Mth.lerp(var74, var52, var71);
                var54 = Mth.lerp(var74, var54, var71);
                var56 = Mth.lerp(var74, var56, var71);
                int var75 = this.getLightColor(param0, param1);
                float var76 = var25 * var3;
                float var77 = var25 * var4;
                float var78 = var25 * var5;
                this.vertex(param2, var42 + 0.0, var43 + (double)var31, var44 + 0.0, var76, var77, var78, var49, var50, var75);
                this.vertex(param2, var42 + 0.0, var43 + (double)var33, var44 + 1.0, var76, var77, var78, var51, var52, var75);
                this.vertex(param2, var42 + 1.0, var43 + (double)var32, var44 + 1.0, var76, var77, var78, var53, var54, var75);
                this.vertex(param2, var42 + 1.0, var43 + (double)var30, var44 + 0.0, var76, var77, var78, var55, var56, var75);
                if (param4.shouldRenderBackwardUpFace(param0, param1.above())) {
                    this.vertex(param2, var42 + 0.0, var43 + (double)var31, var44 + 0.0, var76, var77, var78, var49, var50, var75);
                    this.vertex(param2, var42 + 1.0, var43 + (double)var30, var44 + 0.0, var76, var77, var78, var55, var56, var75);
                    this.vertex(param2, var42 + 1.0, var43 + (double)var32, var44 + 1.0, var76, var77, var78, var53, var54, var75);
                    this.vertex(param2, var42 + 0.0, var43 + (double)var33, var44 + 1.0, var76, var77, var78, var51, var52, var75);
                }
            }

            if (var19) {
                float var79 = var1[0].getU0();
                float var80 = var1[0].getU1();
                float var81 = var1[0].getV0();
                float var82 = var1[0].getV1();
                int var83 = this.getLightColor(param0, param1.below());
                float var84 = var24 * var3;
                float var85 = var24 * var4;
                float var86 = var24 * var5;
                this.vertex(param2, var42, var43 + (double)var46, var44 + 1.0, var84, var85, var86, var79, var82, var83);
                this.vertex(param2, var42, var43 + (double)var46, var44, var84, var85, var86, var79, var81, var83);
                this.vertex(param2, var42 + 1.0, var43 + (double)var46, var44, var84, var85, var86, var80, var81, var83);
                this.vertex(param2, var42 + 1.0, var43 + (double)var46, var44 + 1.0, var84, var85, var86, var80, var82, var83);
            }

            int var87 = this.getLightColor(param0, param1);

            for(Direction var88 : Direction.Plane.HORIZONTAL) {
                float var89;
                float var90;
                double var91;
                double var93;
                double var92;
                double var94;
                boolean var95;
                switch(var88) {
                    case NORTH:
                        var89 = var31;
                        var90 = var30;
                        var91 = var42;
                        var92 = var42 + 1.0;
                        var93 = var44 + 0.001F;
                        var94 = var44 + 0.001F;
                        var95 = var20;
                        break;
                    case SOUTH:
                        var89 = var32;
                        var90 = var33;
                        var91 = var42 + 1.0;
                        var92 = var42;
                        var93 = var44 + 1.0 - 0.001F;
                        var94 = var44 + 1.0 - 0.001F;
                        var95 = var21;
                        break;
                    case WEST:
                        var89 = var33;
                        var90 = var31;
                        var91 = var42 + 0.001F;
                        var92 = var42 + 0.001F;
                        var93 = var44 + 1.0;
                        var94 = var44;
                        var95 = var22;
                        break;
                    default:
                        var89 = var30;
                        var90 = var32;
                        var91 = var42 + 1.0 - 0.001F;
                        var92 = var42 + 1.0 - 0.001F;
                        var93 = var44;
                        var94 = var44 + 1.0;
                        var95 = var23;
                }

                if (var95 && !isFaceOccludedByNeighbor(param0, param1, var88, Math.max(var89, var90), param0.getBlockState(param1.relative(var88)))) {
                    BlockPos var117 = param1.relative(var88);
                    TextureAtlasSprite var118 = var1[1];
                    if (!var0) {
                        Block var119 = param0.getBlockState(var117).getBlock();
                        if (var119 instanceof HalfTransparentBlock || var119 instanceof LeavesBlock) {
                            var118 = this.waterOverlay;
                        }
                    }

                    float var120 = var118.getU(0.0);
                    float var121 = var118.getU(8.0);
                    float var122 = var118.getV((double)((1.0F - var89) * 16.0F * 0.5F));
                    float var123 = var118.getV((double)((1.0F - var90) * 16.0F * 0.5F));
                    float var124 = var118.getV(8.0);
                    float var125 = var88.getAxis() == Direction.Axis.Z ? var26 : var27;
                    float var126 = var25 * var125 * var3;
                    float var127 = var25 * var125 * var4;
                    float var128 = var25 * var125 * var5;
                    this.vertex(param2, var91, var43 + (double)var89, var93, var126, var127, var128, var120, var122, var87);
                    this.vertex(param2, var92, var43 + (double)var90, var94, var126, var127, var128, var121, var123, var87);
                    this.vertex(param2, var92, var43 + (double)var46, var94, var126, var127, var128, var121, var124, var87);
                    this.vertex(param2, var91, var43 + (double)var46, var93, var126, var127, var128, var120, var124, var87);
                    if (var118 != this.waterOverlay) {
                        this.vertex(param2, var91, var43 + (double)var46, var93, var126, var127, var128, var120, var124, var87);
                        this.vertex(param2, var92, var43 + (double)var46, var94, var126, var127, var128, var121, var124, var87);
                        this.vertex(param2, var92, var43 + (double)var90, var94, var126, var127, var128, var121, var123, var87);
                        this.vertex(param2, var91, var43 + (double)var89, var93, var126, var127, var128, var120, var122, var87);
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
            return !param3.getMaterial().isSolid() ? 0.0F : -1.0F;
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
