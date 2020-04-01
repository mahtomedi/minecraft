package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
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
        Vector3f var2 = param0.getExtraTint(Blocks.WATER.defaultBlockState(), param1);
        int var3 = var0 ? 16777215 : BiomeColors.getAverageWaterColor(param0, param1);
        float var4 = var2.x() * (float)(var3 >> 16 & 0xFF) / 255.0F;
        float var5 = var2.x() * (float)(var3 >> 8 & 0xFF) / 255.0F;
        float var6 = var2.x() * (float)(var3 & 0xFF) / 255.0F;
        boolean var7 = !isNeighborSameFluid(param0, param1, Direction.UP, param3);
        boolean var8 = !isNeighborSameFluid(param0, param1, Direction.DOWN, param3) && !isFaceOccluded(param0, param1, Direction.DOWN, 0.8888889F);
        boolean var9 = !isNeighborSameFluid(param0, param1, Direction.NORTH, param3);
        boolean var10 = !isNeighborSameFluid(param0, param1, Direction.SOUTH, param3);
        boolean var11 = !isNeighborSameFluid(param0, param1, Direction.WEST, param3);
        boolean var12 = !isNeighborSameFluid(param0, param1, Direction.EAST, param3);
        if (!var7 && !var8 && !var12 && !var11 && !var9 && !var10) {
            return false;
        } else {
            boolean var13 = false;
            float var14 = 0.5F;
            float var15 = 1.0F;
            float var16 = 0.8F;
            float var17 = 0.6F;
            float var18 = this.getWaterHeight(param0, param1, param3.getType());
            float var19 = this.getWaterHeight(param0, param1.south(), param3.getType());
            float var20 = this.getWaterHeight(param0, param1.east().south(), param3.getType());
            float var21 = this.getWaterHeight(param0, param1.east(), param3.getType());
            double var22 = (double)(param1.getX() & 15);
            double var23 = (double)(param1.getY() & 15);
            double var24 = (double)(param1.getZ() & 15);
            float var25 = 0.001F;
            float var26 = var8 ? 0.001F : 0.0F;
            if (var7 && !isFaceOccluded(param0, param1, Direction.UP, Math.min(Math.min(var18, var19), Math.min(var20, var21)))) {
                var13 = true;
                var18 -= 0.001F;
                var19 -= 0.001F;
                var20 -= 0.001F;
                var21 -= 0.001F;
                Vec3 var27 = param3.getFlow(param0, param1);
                float var29;
                float var31;
                float var33;
                float var35;
                float var30;
                float var32;
                float var34;
                float var36;
                if (var27.x == 0.0 && var27.z == 0.0) {
                    TextureAtlasSprite var28 = var1[0];
                    var29 = var28.getU(0.0);
                    var30 = var28.getV(0.0);
                    var31 = var29;
                    var32 = var28.getV(16.0);
                    var33 = var28.getU(16.0);
                    var34 = var32;
                    var35 = var33;
                    var36 = var30;
                } else {
                    TextureAtlasSprite var37 = var1[1];
                    float var38 = (float)Mth.atan2(var27.z, var27.x) - (float) (Math.PI / 2);
                    float var39 = Mth.sin(var38) * 0.25F;
                    float var40 = Mth.cos(var38) * 0.25F;
                    float var41 = 8.0F;
                    var29 = var37.getU((double)(8.0F + (-var40 - var39) * 16.0F));
                    var30 = var37.getV((double)(8.0F + (-var40 + var39) * 16.0F));
                    var31 = var37.getU((double)(8.0F + (-var40 + var39) * 16.0F));
                    var32 = var37.getV((double)(8.0F + (var40 + var39) * 16.0F));
                    var33 = var37.getU((double)(8.0F + (var40 + var39) * 16.0F));
                    var34 = var37.getV((double)(8.0F + (var40 - var39) * 16.0F));
                    var35 = var37.getU((double)(8.0F + (var40 - var39) * 16.0F));
                    var36 = var37.getV((double)(8.0F + (-var40 - var39) * 16.0F));
                }

                float var50 = (var29 + var31 + var33 + var35) / 4.0F;
                float var51 = (var30 + var32 + var34 + var36) / 4.0F;
                float var52 = (float)var1[0].getWidth() / (var1[0].getU1() - var1[0].getU0());
                float var53 = (float)var1[0].getHeight() / (var1[0].getV1() - var1[0].getV0());
                float var54 = 4.0F / Math.max(var53, var52);
                var29 = Mth.lerp(var54, var29, var50);
                var31 = Mth.lerp(var54, var31, var50);
                var33 = Mth.lerp(var54, var33, var50);
                var35 = Mth.lerp(var54, var35, var50);
                var30 = Mth.lerp(var54, var30, var51);
                var32 = Mth.lerp(var54, var32, var51);
                var34 = Mth.lerp(var54, var34, var51);
                var36 = Mth.lerp(var54, var36, var51);
                int var55 = this.getLightColor(param0, param1);
                float var56 = 1.0F * var4;
                float var57 = 1.0F * var5;
                float var58 = 1.0F * var6;
                this.vertex(param2, var22 + 0.0, var23 + (double)var18, var24 + 0.0, var56, var57, var58, var29, var30, var55);
                this.vertex(param2, var22 + 0.0, var23 + (double)var19, var24 + 1.0, var56, var57, var58, var31, var32, var55);
                this.vertex(param2, var22 + 1.0, var23 + (double)var20, var24 + 1.0, var56, var57, var58, var33, var34, var55);
                this.vertex(param2, var22 + 1.0, var23 + (double)var21, var24 + 0.0, var56, var57, var58, var35, var36, var55);
                if (param3.shouldRenderBackwardUpFace(param0, param1.above())) {
                    this.vertex(param2, var22 + 0.0, var23 + (double)var18, var24 + 0.0, var56, var57, var58, var29, var30, var55);
                    this.vertex(param2, var22 + 1.0, var23 + (double)var21, var24 + 0.0, var56, var57, var58, var35, var36, var55);
                    this.vertex(param2, var22 + 1.0, var23 + (double)var20, var24 + 1.0, var56, var57, var58, var33, var34, var55);
                    this.vertex(param2, var22 + 0.0, var23 + (double)var19, var24 + 1.0, var56, var57, var58, var31, var32, var55);
                }
            }

            if (var8) {
                float var59 = var1[0].getU0();
                float var60 = var1[0].getU1();
                float var61 = var1[0].getV0();
                float var62 = var1[0].getV1();
                int var63 = this.getLightColor(param0, param1.below());
                float var64 = 0.5F * var4;
                float var65 = 0.5F * var5;
                float var66 = 0.5F * var6;
                this.vertex(param2, var22, var23 + (double)var26, var24 + 1.0, var64, var65, var66, var59, var62, var63);
                this.vertex(param2, var22, var23 + (double)var26, var24, var64, var65, var66, var59, var61, var63);
                this.vertex(param2, var22 + 1.0, var23 + (double)var26, var24, var64, var65, var66, var60, var61, var63);
                this.vertex(param2, var22 + 1.0, var23 + (double)var26, var24 + 1.0, var64, var65, var66, var60, var62, var63);
                var13 = true;
            }

            for(int var67 = 0; var67 < 4; ++var67) {
                float var68;
                float var69;
                double var70;
                double var72;
                double var71;
                double var73;
                Direction var74;
                boolean var75;
                if (var67 == 0) {
                    var68 = var18;
                    var69 = var21;
                    var70 = var22;
                    var71 = var22 + 1.0;
                    var72 = var24 + 0.001F;
                    var73 = var24 + 0.001F;
                    var74 = Direction.NORTH;
                    var75 = var9;
                } else if (var67 == 1) {
                    var68 = var20;
                    var69 = var19;
                    var70 = var22 + 1.0;
                    var71 = var22;
                    var72 = var24 + 1.0 - 0.001F;
                    var73 = var24 + 1.0 - 0.001F;
                    var74 = Direction.SOUTH;
                    var75 = var10;
                } else if (var67 == 2) {
                    var68 = var19;
                    var69 = var18;
                    var70 = var22 + 0.001F;
                    var71 = var22 + 0.001F;
                    var72 = var24 + 1.0;
                    var73 = var24;
                    var74 = Direction.WEST;
                    var75 = var11;
                } else {
                    var68 = var21;
                    var69 = var20;
                    var70 = var22 + 1.0 - 0.001F;
                    var71 = var22 + 1.0 - 0.001F;
                    var72 = var24;
                    var73 = var24 + 1.0;
                    var74 = Direction.EAST;
                    var75 = var12;
                }

                if (var75 && !isFaceOccluded(param0, param1, var74, Math.max(var68, var69))) {
                    var13 = true;
                    BlockPos var100 = param1.relative(var74);
                    TextureAtlasSprite var101 = var1[1];
                    if (!var0) {
                        Block var102 = param0.getBlockState(var100).getBlock();
                        if (var102 == Blocks.GLASS || var102 instanceof StainedGlassBlock) {
                            var101 = this.waterOverlay;
                        }
                    }

                    float var103 = var101.getU(0.0);
                    float var104 = var101.getU(8.0);
                    float var105 = var101.getV((double)((1.0F - var68) * 16.0F * 0.5F));
                    float var106 = var101.getV((double)((1.0F - var69) * 16.0F * 0.5F));
                    float var107 = var101.getV(8.0);
                    int var108 = this.getLightColor(param0, var100);
                    float var109 = var67 < 2 ? 0.8F : 0.6F;
                    float var110 = 1.0F * var109 * var4;
                    float var111 = 1.0F * var109 * var5;
                    float var112 = 1.0F * var109 * var6;
                    this.vertex(param2, var70, var23 + (double)var68, var72, var110, var111, var112, var103, var105, var108);
                    this.vertex(param2, var71, var23 + (double)var69, var73, var110, var111, var112, var104, var106, var108);
                    this.vertex(param2, var71, var23 + (double)var26, var73, var110, var111, var112, var104, var107, var108);
                    this.vertex(param2, var70, var23 + (double)var26, var72, var110, var111, var112, var103, var107, var108);
                    if (var101 != this.waterOverlay) {
                        this.vertex(param2, var70, var23 + (double)var26, var72, var110, var111, var112, var103, var107, var108);
                        this.vertex(param2, var71, var23 + (double)var26, var73, var110, var111, var112, var104, var107, var108);
                        this.vertex(param2, var71, var23 + (double)var69, var73, var110, var111, var112, var104, var106, var108);
                        this.vertex(param2, var70, var23 + (double)var68, var72, var110, var111, var112, var103, var105, var108);
                    }
                }
            }

            return var13;
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
