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
import net.minecraft.world.level.block.StainedGlassBlock;
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

    private static boolean isNeighborSameFluid(BlockGetter param0, BlockPos param1, Direction param2, FluidState param3) {
        BlockPos var0 = param1.relative(param2);
        FluidState var1 = param0.getFluidState(var0);
        return var1.getType().isSame(param3.getType());
    }

    private static boolean isFaceOccluded(BlockGetter param0, BlockPos param1, Direction param2, float param3) {
        BlockPos var0 = param1.relative(param2);
        BlockState var1 = param0.getBlockState(var0);
        if (var1.canOcclude()) {
            VoxelShape var2 = Shapes.box(0.0, 0.0, 0.0, 1.0, (double)param3, 1.0);
            VoxelShape var3 = var1.getOcclusionShape(param0, var0);
            return Shapes.blockOccudes(var2, var3, param2);
        } else {
            return false;
        }
    }

    public boolean tesselate(BlockAndTintGetter param0, BlockPos param1, VertexConsumer param2, FluidState param3) {
        boolean var0 = param3.is(FluidTags.LAVA);
        TextureAtlasSprite[] var1 = var0 ? this.lavaIcons : this.waterIcons;
        int var2 = var0 ? 16777215 : BiomeColors.getAverageWaterColor(param0, param1);
        float var3 = (float)(var2 >> 16 & 0xFF) / 255.0F;
        float var4 = (float)(var2 >> 8 & 0xFF) / 255.0F;
        float var5 = (float)(var2 & 0xFF) / 255.0F;
        boolean var6 = !isNeighborSameFluid(param0, param1, Direction.UP, param3);
        boolean var7 = !isNeighborSameFluid(param0, param1, Direction.DOWN, param3) && !isFaceOccluded(param0, param1, Direction.DOWN, 0.8888889F);
        boolean var8 = !isNeighborSameFluid(param0, param1, Direction.NORTH, param3);
        boolean var9 = !isNeighborSameFluid(param0, param1, Direction.SOUTH, param3);
        boolean var10 = !isNeighborSameFluid(param0, param1, Direction.WEST, param3);
        boolean var11 = !isNeighborSameFluid(param0, param1, Direction.EAST, param3);
        if (!var6 && !var7 && !var11 && !var10 && !var8 && !var9) {
            return false;
        } else {
            boolean var12 = false;
            float var13 = 0.5F;
            float var14 = 1.0F;
            float var15 = 0.8F;
            float var16 = 0.6F;
            float var17 = this.getWaterHeight(param0, param1, param3.getType());
            float var18 = this.getWaterHeight(param0, param1.south(), param3.getType());
            float var19 = this.getWaterHeight(param0, param1.east().south(), param3.getType());
            float var20 = this.getWaterHeight(param0, param1.east(), param3.getType());
            double var21 = (double)(param1.getX() & 15);
            double var22 = (double)(param1.getY() & 15);
            double var23 = (double)(param1.getZ() & 15);
            float var24 = 0.001F;
            float var25 = var7 ? 0.001F : 0.0F;
            if (var6 && !isFaceOccluded(param0, param1, Direction.UP, Math.min(Math.min(var17, var18), Math.min(var19, var20)))) {
                var12 = true;
                var17 -= 0.001F;
                var18 -= 0.001F;
                var19 -= 0.001F;
                var20 -= 0.001F;
                Vec3 var26 = param3.getFlow(param0, param1);
                float var28;
                float var30;
                float var32;
                float var34;
                float var29;
                float var31;
                float var33;
                float var35;
                if (var26.x == 0.0 && var26.z == 0.0) {
                    TextureAtlasSprite var27 = var1[0];
                    var28 = var27.getU(0.0);
                    var29 = var27.getV(0.0);
                    var30 = var28;
                    var31 = var27.getV(16.0);
                    var32 = var27.getU(16.0);
                    var33 = var31;
                    var34 = var32;
                    var35 = var29;
                } else {
                    TextureAtlasSprite var36 = var1[1];
                    float var37 = (float)Mth.atan2(var26.z, var26.x) - (float) (Math.PI / 2);
                    float var38 = Mth.sin(var37) * 0.25F;
                    float var39 = Mth.cos(var37) * 0.25F;
                    float var40 = 8.0F;
                    var28 = var36.getU((double)(8.0F + (-var39 - var38) * 16.0F));
                    var29 = var36.getV((double)(8.0F + (-var39 + var38) * 16.0F));
                    var30 = var36.getU((double)(8.0F + (-var39 + var38) * 16.0F));
                    var31 = var36.getV((double)(8.0F + (var39 + var38) * 16.0F));
                    var32 = var36.getU((double)(8.0F + (var39 + var38) * 16.0F));
                    var33 = var36.getV((double)(8.0F + (var39 - var38) * 16.0F));
                    var34 = var36.getU((double)(8.0F + (var39 - var38) * 16.0F));
                    var35 = var36.getV((double)(8.0F + (-var39 - var38) * 16.0F));
                }

                float var49 = (var28 + var30 + var32 + var34) / 4.0F;
                float var50 = (var29 + var31 + var33 + var35) / 4.0F;
                float var51 = (float)var1[0].getWidth() / (var1[0].getU1() - var1[0].getU0());
                float var52 = (float)var1[0].getHeight() / (var1[0].getV1() - var1[0].getV0());
                float var53 = 4.0F / Math.max(var52, var51);
                var28 = Mth.lerp(var53, var28, var49);
                var30 = Mth.lerp(var53, var30, var49);
                var32 = Mth.lerp(var53, var32, var49);
                var34 = Mth.lerp(var53, var34, var49);
                var29 = Mth.lerp(var53, var29, var50);
                var31 = Mth.lerp(var53, var31, var50);
                var33 = Mth.lerp(var53, var33, var50);
                var35 = Mth.lerp(var53, var35, var50);
                int var54 = this.getLightColor(param0, param1);
                float var55 = 1.0F * var3;
                float var56 = 1.0F * var4;
                float var57 = 1.0F * var5;
                this.vertex(param2, var21 + 0.0, var22 + (double)var17, var23 + 0.0, var55, var56, var57, var28, var29, var54);
                this.vertex(param2, var21 + 0.0, var22 + (double)var18, var23 + 1.0, var55, var56, var57, var30, var31, var54);
                this.vertex(param2, var21 + 1.0, var22 + (double)var19, var23 + 1.0, var55, var56, var57, var32, var33, var54);
                this.vertex(param2, var21 + 1.0, var22 + (double)var20, var23 + 0.0, var55, var56, var57, var34, var35, var54);
                if (param3.shouldRenderBackwardUpFace(param0, param1.above())) {
                    this.vertex(param2, var21 + 0.0, var22 + (double)var17, var23 + 0.0, var55, var56, var57, var28, var29, var54);
                    this.vertex(param2, var21 + 1.0, var22 + (double)var20, var23 + 0.0, var55, var56, var57, var34, var35, var54);
                    this.vertex(param2, var21 + 1.0, var22 + (double)var19, var23 + 1.0, var55, var56, var57, var32, var33, var54);
                    this.vertex(param2, var21 + 0.0, var22 + (double)var18, var23 + 1.0, var55, var56, var57, var30, var31, var54);
                }
            }

            if (var7) {
                float var58 = var1[0].getU0();
                float var59 = var1[0].getU1();
                float var60 = var1[0].getV0();
                float var61 = var1[0].getV1();
                int var62 = this.getLightColor(param0, param1.below());
                float var63 = 0.5F * var3;
                float var64 = 0.5F * var4;
                float var65 = 0.5F * var5;
                this.vertex(param2, var21, var22 + (double)var25, var23 + 1.0, var63, var64, var65, var58, var61, var62);
                this.vertex(param2, var21, var22 + (double)var25, var23, var63, var64, var65, var58, var60, var62);
                this.vertex(param2, var21 + 1.0, var22 + (double)var25, var23, var63, var64, var65, var59, var60, var62);
                this.vertex(param2, var21 + 1.0, var22 + (double)var25, var23 + 1.0, var63, var64, var65, var59, var61, var62);
                var12 = true;
            }

            for(int var66 = 0; var66 < 4; ++var66) {
                float var67;
                float var68;
                double var69;
                double var71;
                double var70;
                double var72;
                Direction var73;
                boolean var74;
                if (var66 == 0) {
                    var67 = var17;
                    var68 = var20;
                    var69 = var21;
                    var70 = var21 + 1.0;
                    var71 = var23 + 0.001F;
                    var72 = var23 + 0.001F;
                    var73 = Direction.NORTH;
                    var74 = var8;
                } else if (var66 == 1) {
                    var67 = var19;
                    var68 = var18;
                    var69 = var21 + 1.0;
                    var70 = var21;
                    var71 = var23 + 1.0 - 0.001F;
                    var72 = var23 + 1.0 - 0.001F;
                    var73 = Direction.SOUTH;
                    var74 = var9;
                } else if (var66 == 2) {
                    var67 = var18;
                    var68 = var17;
                    var69 = var21 + 0.001F;
                    var70 = var21 + 0.001F;
                    var71 = var23 + 1.0;
                    var72 = var23;
                    var73 = Direction.WEST;
                    var74 = var10;
                } else {
                    var67 = var20;
                    var68 = var19;
                    var69 = var21 + 1.0 - 0.001F;
                    var70 = var21 + 1.0 - 0.001F;
                    var71 = var23;
                    var72 = var23 + 1.0;
                    var73 = Direction.EAST;
                    var74 = var11;
                }

                if (var74 && !isFaceOccluded(param0, param1, var73, Math.max(var67, var68))) {
                    var12 = true;
                    BlockPos var99 = param1.relative(var73);
                    TextureAtlasSprite var100 = var1[1];
                    if (!var0) {
                        Block var101 = param0.getBlockState(var99).getBlock();
                        if (var101 == Blocks.GLASS || var101 instanceof StainedGlassBlock) {
                            var100 = this.waterOverlay;
                        }
                    }

                    float var102 = var100.getU(0.0);
                    float var103 = var100.getU(8.0);
                    float var104 = var100.getV((double)((1.0F - var67) * 16.0F * 0.5F));
                    float var105 = var100.getV((double)((1.0F - var68) * 16.0F * 0.5F));
                    float var106 = var100.getV(8.0);
                    int var107 = this.getLightColor(param0, var99);
                    float var108 = var66 < 2 ? 0.8F : 0.6F;
                    float var109 = 1.0F * var108 * var3;
                    float var110 = 1.0F * var108 * var4;
                    float var111 = 1.0F * var108 * var5;
                    this.vertex(param2, var69, var22 + (double)var67, var71, var109, var110, var111, var102, var104, var107);
                    this.vertex(param2, var70, var22 + (double)var68, var72, var109, var110, var111, var103, var105, var107);
                    this.vertex(param2, var70, var22 + (double)var25, var72, var109, var110, var111, var103, var106, var107);
                    this.vertex(param2, var69, var22 + (double)var25, var71, var109, var110, var111, var102, var106, var107);
                    if (var100 != this.waterOverlay) {
                        this.vertex(param2, var69, var22 + (double)var25, var71, var109, var110, var111, var102, var106, var107);
                        this.vertex(param2, var70, var22 + (double)var25, var72, var109, var110, var111, var103, var106, var107);
                        this.vertex(param2, var70, var22 + (double)var68, var72, var109, var110, var111, var103, var105, var107);
                        this.vertex(param2, var69, var22 + (double)var67, var71, var109, var110, var111, var102, var104, var107);
                    }
                }
            }

            return var12;
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

    private float getWaterHeight(BlockGetter param0, BlockPos param1, Fluid param2) {
        int var0 = 0;
        float var1 = 0.0F;

        for(int var2 = 0; var2 < 4; ++var2) {
            BlockPos var3 = param1.offset(-(var2 & 1), 0, -(var2 >> 1 & 1));
            if (param0.getFluidState(var3.above()).getType().isSame(param2)) {
                return 1.0F;
            }

            FluidState var4 = param0.getFluidState(var3);
            if (var4.getType().isSame(param2)) {
                float var5 = var4.getHeight(param0, var3);
                if (var5 >= 0.8F) {
                    var1 += var5 * 10.0F;
                    var0 += 10;
                } else {
                    var1 += var5;
                    ++var0;
                }
            } else if (!param0.getBlockState(var3).getMaterial().isSolid()) {
                ++var0;
            }
        }

        return var1 / (float)var0;
    }
}
