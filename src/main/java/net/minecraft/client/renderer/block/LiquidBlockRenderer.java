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

    public boolean tesselate(BlockAndTintGetter param0, BlockPos param1, VertexConsumer param2, BlockState param3, FluidState param4) {
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
        if (!var18 && !var19 && !var23 && !var22 && !var20 && !var21) {
            return false;
        } else {
            boolean var24 = false;
            float var25 = param0.getShade(Direction.DOWN, true);
            float var26 = param0.getShade(Direction.UP, true);
            float var27 = param0.getShade(Direction.NORTH, true);
            float var28 = param0.getShade(Direction.WEST, true);
            Fluid var29 = param4.getType();
            float var30 = this.getHeight(param0, var29, param1, param3, param4);
            float var31;
            float var32;
            float var33;
            float var34;
            if (var30 >= 1.0F) {
                var31 = 1.0F;
                var32 = 1.0F;
                var33 = 1.0F;
                var34 = 1.0F;
            } else {
                float var35 = this.getHeight(param0, var29, param1.north(), var10, var11);
                float var36 = this.getHeight(param0, var29, param1.south(), var12, var13);
                float var37 = this.getHeight(param0, var29, param1.east(), var16, var17);
                float var38 = this.getHeight(param0, var29, param1.west(), var14, var15);
                var31 = this.calculateAverageHeight(param0, var29, var30, var35, var37, param1.relative(Direction.NORTH).relative(Direction.EAST));
                var32 = this.calculateAverageHeight(param0, var29, var30, var35, var38, param1.relative(Direction.NORTH).relative(Direction.WEST));
                var33 = this.calculateAverageHeight(param0, var29, var30, var36, var37, param1.relative(Direction.SOUTH).relative(Direction.EAST));
                var34 = this.calculateAverageHeight(param0, var29, var30, var36, var38, param1.relative(Direction.SOUTH).relative(Direction.WEST));
            }

            double var43 = (double)(param1.getX() & 15);
            double var44 = (double)(param1.getY() & 15);
            double var45 = (double)(param1.getZ() & 15);
            float var46 = 0.001F;
            float var47 = var19 ? 0.001F : 0.0F;
            if (var18 && !isFaceOccludedByNeighbor(param0, param1, Direction.UP, Math.min(Math.min(var32, var34), Math.min(var33, var31)), var8)) {
                var24 = true;
                var32 -= 0.001F;
                var34 -= 0.001F;
                var33 -= 0.001F;
                var31 -= 0.001F;
                Vec3 var48 = param4.getFlow(param0, param1);
                float var50;
                float var52;
                float var54;
                float var56;
                float var51;
                float var53;
                float var55;
                float var57;
                if (var48.x == 0.0 && var48.z == 0.0) {
                    TextureAtlasSprite var49 = var1[0];
                    var50 = var49.getU(0.0);
                    var51 = var49.getV(0.0);
                    var52 = var50;
                    var53 = var49.getV(16.0);
                    var54 = var49.getU(16.0);
                    var55 = var53;
                    var56 = var54;
                    var57 = var51;
                } else {
                    TextureAtlasSprite var58 = var1[1];
                    float var59 = (float)Mth.atan2(var48.z, var48.x) - (float) (Math.PI / 2);
                    float var60 = Mth.sin(var59) * 0.25F;
                    float var61 = Mth.cos(var59) * 0.25F;
                    float var62 = 8.0F;
                    var50 = var58.getU((double)(8.0F + (-var61 - var60) * 16.0F));
                    var51 = var58.getV((double)(8.0F + (-var61 + var60) * 16.0F));
                    var52 = var58.getU((double)(8.0F + (-var61 + var60) * 16.0F));
                    var53 = var58.getV((double)(8.0F + (var61 + var60) * 16.0F));
                    var54 = var58.getU((double)(8.0F + (var61 + var60) * 16.0F));
                    var55 = var58.getV((double)(8.0F + (var61 - var60) * 16.0F));
                    var56 = var58.getU((double)(8.0F + (var61 - var60) * 16.0F));
                    var57 = var58.getV((double)(8.0F + (-var61 - var60) * 16.0F));
                }

                float var71 = (var50 + var52 + var54 + var56) / 4.0F;
                float var72 = (var51 + var53 + var55 + var57) / 4.0F;
                float var73 = (float)var1[0].getWidth() / (var1[0].getU1() - var1[0].getU0());
                float var74 = (float)var1[0].getHeight() / (var1[0].getV1() - var1[0].getV0());
                float var75 = 4.0F / Math.max(var74, var73);
                var50 = Mth.lerp(var75, var50, var71);
                var52 = Mth.lerp(var75, var52, var71);
                var54 = Mth.lerp(var75, var54, var71);
                var56 = Mth.lerp(var75, var56, var71);
                var51 = Mth.lerp(var75, var51, var72);
                var53 = Mth.lerp(var75, var53, var72);
                var55 = Mth.lerp(var75, var55, var72);
                var57 = Mth.lerp(var75, var57, var72);
                int var76 = this.getLightColor(param0, param1);
                float var77 = var26 * var3;
                float var78 = var26 * var4;
                float var79 = var26 * var5;
                this.vertex(param2, var43 + 0.0, var44 + (double)var32, var45 + 0.0, var77, var78, var79, var50, var51, var76);
                this.vertex(param2, var43 + 0.0, var44 + (double)var34, var45 + 1.0, var77, var78, var79, var52, var53, var76);
                this.vertex(param2, var43 + 1.0, var44 + (double)var33, var45 + 1.0, var77, var78, var79, var54, var55, var76);
                this.vertex(param2, var43 + 1.0, var44 + (double)var31, var45 + 0.0, var77, var78, var79, var56, var57, var76);
                if (param4.shouldRenderBackwardUpFace(param0, param1.above())) {
                    this.vertex(param2, var43 + 0.0, var44 + (double)var32, var45 + 0.0, var77, var78, var79, var50, var51, var76);
                    this.vertex(param2, var43 + 1.0, var44 + (double)var31, var45 + 0.0, var77, var78, var79, var56, var57, var76);
                    this.vertex(param2, var43 + 1.0, var44 + (double)var33, var45 + 1.0, var77, var78, var79, var54, var55, var76);
                    this.vertex(param2, var43 + 0.0, var44 + (double)var34, var45 + 1.0, var77, var78, var79, var52, var53, var76);
                }
            }

            if (var19) {
                float var80 = var1[0].getU0();
                float var81 = var1[0].getU1();
                float var82 = var1[0].getV0();
                float var83 = var1[0].getV1();
                int var84 = this.getLightColor(param0, param1.below());
                float var85 = var25 * var3;
                float var86 = var25 * var4;
                float var87 = var25 * var5;
                this.vertex(param2, var43, var44 + (double)var47, var45 + 1.0, var85, var86, var87, var80, var83, var84);
                this.vertex(param2, var43, var44 + (double)var47, var45, var85, var86, var87, var80, var82, var84);
                this.vertex(param2, var43 + 1.0, var44 + (double)var47, var45, var85, var86, var87, var81, var82, var84);
                this.vertex(param2, var43 + 1.0, var44 + (double)var47, var45 + 1.0, var85, var86, var87, var81, var83, var84);
                var24 = true;
            }

            int var88 = this.getLightColor(param0, param1);

            for(Direction var89 : Direction.Plane.HORIZONTAL) {
                float var90;
                float var91;
                double var92;
                double var94;
                double var93;
                double var95;
                boolean var96;
                switch(var89) {
                    case NORTH:
                        var90 = var32;
                        var91 = var31;
                        var92 = var43;
                        var93 = var43 + 1.0;
                        var94 = var45 + 0.001F;
                        var95 = var45 + 0.001F;
                        var96 = var20;
                        break;
                    case SOUTH:
                        var90 = var33;
                        var91 = var34;
                        var92 = var43 + 1.0;
                        var93 = var43;
                        var94 = var45 + 1.0 - 0.001F;
                        var95 = var45 + 1.0 - 0.001F;
                        var96 = var21;
                        break;
                    case WEST:
                        var90 = var34;
                        var91 = var32;
                        var92 = var43 + 0.001F;
                        var93 = var43 + 0.001F;
                        var94 = var45 + 1.0;
                        var95 = var45;
                        var96 = var22;
                        break;
                    default:
                        var90 = var31;
                        var91 = var33;
                        var92 = var43 + 1.0 - 0.001F;
                        var93 = var43 + 1.0 - 0.001F;
                        var94 = var45;
                        var95 = var45 + 1.0;
                        var96 = var23;
                }

                if (var96 && !isFaceOccludedByNeighbor(param0, param1, var89, Math.max(var90, var91), param0.getBlockState(param1.relative(var89)))) {
                    var24 = true;
                    BlockPos var118 = param1.relative(var89);
                    TextureAtlasSprite var119 = var1[1];
                    if (!var0) {
                        Block var120 = param0.getBlockState(var118).getBlock();
                        if (var120 instanceof HalfTransparentBlock || var120 instanceof LeavesBlock) {
                            var119 = this.waterOverlay;
                        }
                    }

                    float var121 = var119.getU(0.0);
                    float var122 = var119.getU(8.0);
                    float var123 = var119.getV((double)((1.0F - var90) * 16.0F * 0.5F));
                    float var124 = var119.getV((double)((1.0F - var91) * 16.0F * 0.5F));
                    float var125 = var119.getV(8.0);
                    float var126 = var89.getAxis() == Direction.Axis.Z ? var27 : var28;
                    float var127 = var26 * var126 * var3;
                    float var128 = var26 * var126 * var4;
                    float var129 = var26 * var126 * var5;
                    this.vertex(param2, var92, var44 + (double)var90, var94, var127, var128, var129, var121, var123, var88);
                    this.vertex(param2, var93, var44 + (double)var91, var95, var127, var128, var129, var122, var124, var88);
                    this.vertex(param2, var93, var44 + (double)var47, var95, var127, var128, var129, var122, var125, var88);
                    this.vertex(param2, var92, var44 + (double)var47, var94, var127, var128, var129, var121, var125, var88);
                    if (var119 != this.waterOverlay) {
                        this.vertex(param2, var92, var44 + (double)var47, var94, var127, var128, var129, var121, var125, var88);
                        this.vertex(param2, var93, var44 + (double)var47, var95, var127, var128, var129, var122, var125, var88);
                        this.vertex(param2, var93, var44 + (double)var91, var95, var127, var128, var129, var122, var124, var88);
                        this.vertex(param2, var92, var44 + (double)var90, var94, var127, var128, var129, var121, var123, var88);
                    }
                }
            }

            return var24;
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
