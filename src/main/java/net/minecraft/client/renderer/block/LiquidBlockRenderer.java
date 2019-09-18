package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndBiomeGetter;
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
        TextureAtlas var0 = Minecraft.getInstance().getTextureAtlas();
        this.lavaIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.LAVA.defaultBlockState()).getParticleIcon();
        this.lavaIcons[1] = var0.getSprite(ModelBakery.LAVA_FLOW);
        this.waterIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).getParticleIcon();
        this.waterIcons[1] = var0.getSprite(ModelBakery.WATER_FLOW);
        this.waterOverlay = var0.getSprite(ModelBakery.WATER_OVERLAY);
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

    public boolean tesselate(BlockAndBiomeGetter param0, BlockPos param1, BufferBuilder param2, FluidState param3) {
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
            double var21 = (double)param1.getX();
            double var22 = (double)param1.getY();
            double var23 = (double)param1.getZ();
            float var24 = 0.001F;
            if (var6 && !isFaceOccluded(param0, param1, Direction.UP, Math.min(Math.min(var17, var18), Math.min(var19, var20)))) {
                var12 = true;
                var17 -= 0.001F;
                var18 -= 0.001F;
                var19 -= 0.001F;
                var20 -= 0.001F;
                Vec3 var25 = param3.getFlow(param0, param1);
                float var27;
                float var29;
                float var31;
                float var33;
                float var28;
                float var30;
                float var32;
                float var34;
                if (var25.x == 0.0 && var25.z == 0.0) {
                    TextureAtlasSprite var26 = var1[0];
                    var27 = var26.getU(0.0);
                    var28 = var26.getV(0.0);
                    var29 = var27;
                    var30 = var26.getV(16.0);
                    var31 = var26.getU(16.0);
                    var32 = var30;
                    var33 = var31;
                    var34 = var28;
                } else {
                    TextureAtlasSprite var35 = var1[1];
                    float var36 = (float)Mth.atan2(var25.z, var25.x) - (float) (Math.PI / 2);
                    float var37 = Mth.sin(var36) * 0.25F;
                    float var38 = Mth.cos(var36) * 0.25F;
                    float var39 = 8.0F;
                    var27 = var35.getU((double)(8.0F + (-var38 - var37) * 16.0F));
                    var28 = var35.getV((double)(8.0F + (-var38 + var37) * 16.0F));
                    var29 = var35.getU((double)(8.0F + (-var38 + var37) * 16.0F));
                    var30 = var35.getV((double)(8.0F + (var38 + var37) * 16.0F));
                    var31 = var35.getU((double)(8.0F + (var38 + var37) * 16.0F));
                    var32 = var35.getV((double)(8.0F + (var38 - var37) * 16.0F));
                    var33 = var35.getU((double)(8.0F + (var38 - var37) * 16.0F));
                    var34 = var35.getV((double)(8.0F + (-var38 - var37) * 16.0F));
                }

                float var48 = (var27 + var29 + var31 + var33) / 4.0F;
                float var49 = (var28 + var30 + var32 + var34) / 4.0F;
                float var50 = (float)var1[0].getWidth() / (var1[0].getU1() - var1[0].getU0());
                float var51 = (float)var1[0].getHeight() / (var1[0].getV1() - var1[0].getV0());
                float var52 = 4.0F / Math.max(var51, var50);
                var27 = Mth.lerp(var52, var27, var48);
                var29 = Mth.lerp(var52, var29, var48);
                var31 = Mth.lerp(var52, var31, var48);
                var33 = Mth.lerp(var52, var33, var48);
                var28 = Mth.lerp(var52, var28, var49);
                var30 = Mth.lerp(var52, var30, var49);
                var32 = Mth.lerp(var52, var32, var49);
                var34 = Mth.lerp(var52, var34, var49);
                int var53 = this.getLightColor(param0, param1);
                int var54 = var53 >> 16 & 65535;
                int var55 = var53 & 65535;
                float var56 = 1.0F * var3;
                float var57 = 1.0F * var4;
                float var58 = 1.0F * var5;
                param2.vertex(var21 + 0.0, var22 + (double)var17, var23 + 0.0)
                    .color(var56, var57, var58, 1.0F)
                    .uv((double)var27, (double)var28)
                    .uv2(var54, var55)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();
                param2.vertex(var21 + 0.0, var22 + (double)var18, var23 + 1.0)
                    .color(var56, var57, var58, 1.0F)
                    .uv((double)var29, (double)var30)
                    .uv2(var54, var55)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();
                param2.vertex(var21 + 1.0, var22 + (double)var19, var23 + 1.0)
                    .color(var56, var57, var58, 1.0F)
                    .uv((double)var31, (double)var32)
                    .uv2(var54, var55)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();
                param2.vertex(var21 + 1.0, var22 + (double)var20, var23 + 0.0)
                    .color(var56, var57, var58, 1.0F)
                    .uv((double)var33, (double)var34)
                    .uv2(var54, var55)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();
                if (param3.shouldRenderBackwardUpFace(param0, param1.above())) {
                    param2.vertex(var21 + 0.0, var22 + (double)var17, var23 + 0.0)
                        .color(var56, var57, var58, 1.0F)
                        .uv((double)var27, (double)var28)
                        .uv2(var54, var55)
                        .normal(0.0F, 1.0F, 0.0F)
                        .endVertex();
                    param2.vertex(var21 + 1.0, var22 + (double)var20, var23 + 0.0)
                        .color(var56, var57, var58, 1.0F)
                        .uv((double)var33, (double)var34)
                        .uv2(var54, var55)
                        .normal(0.0F, 1.0F, 0.0F)
                        .endVertex();
                    param2.vertex(var21 + 1.0, var22 + (double)var19, var23 + 1.0)
                        .color(var56, var57, var58, 1.0F)
                        .uv((double)var31, (double)var32)
                        .uv2(var54, var55)
                        .normal(0.0F, 1.0F, 0.0F)
                        .endVertex();
                    param2.vertex(var21 + 0.0, var22 + (double)var18, var23 + 1.0)
                        .color(var56, var57, var58, 1.0F)
                        .uv((double)var29, (double)var30)
                        .uv2(var54, var55)
                        .normal(0.0F, 1.0F, 0.0F)
                        .endVertex();
                }
            }

            if (var7) {
                float var59 = var1[0].getU0();
                float var60 = var1[0].getU1();
                float var61 = var1[0].getV0();
                float var62 = var1[0].getV1();
                int var63 = this.getLightColor(param0, param1.below());
                int var64 = var63 >> 16 & 65535;
                int var65 = var63 & 65535;
                float var66 = 0.5F * var3;
                float var67 = 0.5F * var4;
                float var68 = 0.5F * var5;
                param2.vertex(var21, var22, var23 + 1.0)
                    .color(var66, var67, var68, 1.0F)
                    .uv((double)var59, (double)var62)
                    .uv2(var64, var65)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();
                param2.vertex(var21, var22, var23)
                    .color(var66, var67, var68, 1.0F)
                    .uv((double)var59, (double)var61)
                    .uv2(var64, var65)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();
                param2.vertex(var21 + 1.0, var22, var23)
                    .color(var66, var67, var68, 1.0F)
                    .uv((double)var60, (double)var61)
                    .uv2(var64, var65)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();
                param2.vertex(var21 + 1.0, var22, var23 + 1.0)
                    .color(var66, var67, var68, 1.0F)
                    .uv((double)var60, (double)var62)
                    .uv2(var64, var65)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();
                var12 = true;
            }

            for(int var69 = 0; var69 < 4; ++var69) {
                float var70;
                float var71;
                double var72;
                double var74;
                double var73;
                double var75;
                Direction var76;
                boolean var77;
                if (var69 == 0) {
                    var70 = var17;
                    var71 = var20;
                    var72 = var21;
                    var73 = var21 + 1.0;
                    var74 = var23 + 0.001F;
                    var75 = var23 + 0.001F;
                    var76 = Direction.NORTH;
                    var77 = var8;
                } else if (var69 == 1) {
                    var70 = var19;
                    var71 = var18;
                    var72 = var21 + 1.0;
                    var73 = var21;
                    var74 = var23 + 1.0 - 0.001F;
                    var75 = var23 + 1.0 - 0.001F;
                    var76 = Direction.SOUTH;
                    var77 = var9;
                } else if (var69 == 2) {
                    var70 = var18;
                    var71 = var17;
                    var72 = var21 + 0.001F;
                    var73 = var21 + 0.001F;
                    var74 = var23 + 1.0;
                    var75 = var23;
                    var76 = Direction.WEST;
                    var77 = var10;
                } else {
                    var70 = var20;
                    var71 = var19;
                    var72 = var21 + 1.0 - 0.001F;
                    var73 = var21 + 1.0 - 0.001F;
                    var74 = var23;
                    var75 = var23 + 1.0;
                    var76 = Direction.EAST;
                    var77 = var11;
                }

                if (var77 && !isFaceOccluded(param0, param1, var76, Math.max(var70, var71))) {
                    var12 = true;
                    BlockPos var102 = param1.relative(var76);
                    TextureAtlasSprite var103 = var1[1];
                    if (!var0) {
                        Block var104 = param0.getBlockState(var102).getBlock();
                        if (var104 == Blocks.GLASS || var104 instanceof StainedGlassBlock) {
                            var103 = this.waterOverlay;
                        }
                    }

                    float var105 = var103.getU(0.0);
                    float var106 = var103.getU(8.0);
                    float var107 = var103.getV((double)((1.0F - var70) * 16.0F * 0.5F));
                    float var108 = var103.getV((double)((1.0F - var71) * 16.0F * 0.5F));
                    float var109 = var103.getV(8.0);
                    int var110 = this.getLightColor(param0, var102);
                    int var111 = var110 >> 16 & 65535;
                    int var112 = var110 & 65535;
                    float var113 = var69 < 2 ? 0.8F : 0.6F;
                    float var114 = 1.0F * var113 * var3;
                    float var115 = 1.0F * var113 * var4;
                    float var116 = 1.0F * var113 * var5;
                    param2.vertex(var72, var22 + (double)var70, var74)
                        .color(var114, var115, var116, 1.0F)
                        .uv((double)var105, (double)var107)
                        .uv2(var111, var112)
                        .normal(0.0F, 1.0F, 0.0F)
                        .endVertex();
                    param2.vertex(var73, var22 + (double)var71, var75)
                        .color(var114, var115, var116, 1.0F)
                        .uv((double)var106, (double)var108)
                        .uv2(var111, var112)
                        .normal(0.0F, 1.0F, 0.0F)
                        .endVertex();
                    param2.vertex(var73, var22 + 0.0, var75)
                        .color(var114, var115, var116, 1.0F)
                        .uv((double)var106, (double)var109)
                        .uv2(var111, var112)
                        .normal(0.0F, 1.0F, 0.0F)
                        .endVertex();
                    param2.vertex(var72, var22 + 0.0, var74)
                        .color(var114, var115, var116, 1.0F)
                        .uv((double)var105, (double)var109)
                        .uv2(var111, var112)
                        .normal(0.0F, 1.0F, 0.0F)
                        .endVertex();
                    if (var103 != this.waterOverlay) {
                        param2.vertex(var72, var22 + 0.0, var74)
                            .color(var114, var115, var116, 1.0F)
                            .uv((double)var105, (double)var109)
                            .uv2(var111, var112)
                            .normal(0.0F, 1.0F, 0.0F)
                            .endVertex();
                        param2.vertex(var73, var22 + 0.0, var75)
                            .color(var114, var115, var116, 1.0F)
                            .uv((double)var106, (double)var109)
                            .uv2(var111, var112)
                            .normal(0.0F, 1.0F, 0.0F)
                            .endVertex();
                        param2.vertex(var73, var22 + (double)var71, var75)
                            .color(var114, var115, var116, 1.0F)
                            .uv((double)var106, (double)var108)
                            .uv2(var111, var112)
                            .normal(0.0F, 1.0F, 0.0F)
                            .endVertex();
                        param2.vertex(var72, var22 + (double)var70, var74)
                            .color(var114, var115, var116, 1.0F)
                            .uv((double)var105, (double)var107)
                            .uv2(var111, var112)
                            .normal(0.0F, 1.0F, 0.0F)
                            .endVertex();
                    }
                }
            }

            return var12;
        }
    }

    private int getLightColor(BlockAndBiomeGetter param0, BlockPos param1) {
        int var0 = param0.getLightColor(param1);
        int var1 = param0.getLightColor(param1.above());
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
