package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.VertexConsumer;
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

    public boolean tesselate(BlockAndBiomeGetter param0, BlockPos param1, VertexConsumer param2, FluidState param3) {
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
                float var54 = 1.0F * var3;
                float var55 = 1.0F * var4;
                float var56 = 1.0F * var5;
                this.vertex(param2, var21 + 0.0, var22 + (double)var17, var23 + 0.0, var54, var55, var56, var27, var28, var53);
                this.vertex(param2, var21 + 0.0, var22 + (double)var18, var23 + 1.0, var54, var55, var56, var29, var30, var53);
                this.vertex(param2, var21 + 1.0, var22 + (double)var19, var23 + 1.0, var54, var55, var56, var31, var32, var53);
                this.vertex(param2, var21 + 1.0, var22 + (double)var20, var23 + 0.0, var54, var55, var56, var33, var34, var53);
                if (param3.shouldRenderBackwardUpFace(param0, param1.above())) {
                    this.vertex(param2, var21 + 0.0, var22 + (double)var17, var23 + 0.0, var54, var55, var56, var27, var28, var53);
                    this.vertex(param2, var21 + 1.0, var22 + (double)var20, var23 + 0.0, var54, var55, var56, var33, var34, var53);
                    this.vertex(param2, var21 + 1.0, var22 + (double)var19, var23 + 1.0, var54, var55, var56, var31, var32, var53);
                    this.vertex(param2, var21 + 0.0, var22 + (double)var18, var23 + 1.0, var54, var55, var56, var29, var30, var53);
                }
            }

            if (var7) {
                float var57 = var1[0].getU0();
                float var58 = var1[0].getU1();
                float var59 = var1[0].getV0();
                float var60 = var1[0].getV1();
                int var61 = this.getLightColor(param0, param1.below());
                float var62 = 0.5F * var3;
                float var63 = 0.5F * var4;
                float var64 = 0.5F * var5;
                this.vertex(param2, var21, var22, var23 + 1.0, var62, var63, var64, var57, var60, var61);
                this.vertex(param2, var21, var22, var23, var62, var63, var64, var57, var59, var61);
                this.vertex(param2, var21 + 1.0, var22, var23, var62, var63, var64, var58, var59, var61);
                this.vertex(param2, var21 + 1.0, var22, var23 + 1.0, var62, var63, var64, var58, var60, var61);
                var12 = true;
            }

            for(int var65 = 0; var65 < 4; ++var65) {
                float var66;
                float var67;
                double var68;
                double var70;
                double var69;
                double var71;
                Direction var72;
                boolean var73;
                if (var65 == 0) {
                    var66 = var17;
                    var67 = var20;
                    var68 = var21;
                    var69 = var21 + 1.0;
                    var70 = var23 + 0.001F;
                    var71 = var23 + 0.001F;
                    var72 = Direction.NORTH;
                    var73 = var8;
                } else if (var65 == 1) {
                    var66 = var19;
                    var67 = var18;
                    var68 = var21 + 1.0;
                    var69 = var21;
                    var70 = var23 + 1.0 - 0.001F;
                    var71 = var23 + 1.0 - 0.001F;
                    var72 = Direction.SOUTH;
                    var73 = var9;
                } else if (var65 == 2) {
                    var66 = var18;
                    var67 = var17;
                    var68 = var21 + 0.001F;
                    var69 = var21 + 0.001F;
                    var70 = var23 + 1.0;
                    var71 = var23;
                    var72 = Direction.WEST;
                    var73 = var10;
                } else {
                    var66 = var20;
                    var67 = var19;
                    var68 = var21 + 1.0 - 0.001F;
                    var69 = var21 + 1.0 - 0.001F;
                    var70 = var23;
                    var71 = var23 + 1.0;
                    var72 = Direction.EAST;
                    var73 = var11;
                }

                if (var73 && !isFaceOccluded(param0, param1, var72, Math.max(var66, var67))) {
                    var12 = true;
                    BlockPos var98 = param1.relative(var72);
                    TextureAtlasSprite var99 = var1[1];
                    if (!var0) {
                        Block var100 = param0.getBlockState(var98).getBlock();
                        if (var100 == Blocks.GLASS || var100 instanceof StainedGlassBlock) {
                            var99 = this.waterOverlay;
                        }
                    }

                    float var101 = var99.getU(0.0);
                    float var102 = var99.getU(8.0);
                    float var103 = var99.getV((double)((1.0F - var66) * 16.0F * 0.5F));
                    float var104 = var99.getV((double)((1.0F - var67) * 16.0F * 0.5F));
                    float var105 = var99.getV(8.0);
                    int var106 = this.getLightColor(param0, var98);
                    float var107 = var65 < 2 ? 0.8F : 0.6F;
                    float var108 = 1.0F * var107 * var3;
                    float var109 = 1.0F * var107 * var4;
                    float var110 = 1.0F * var107 * var5;
                    this.vertex(param2, var68, var22 + (double)var66, var70, var108, var109, var110, var101, var103, var106);
                    this.vertex(param2, var69, var22 + (double)var67, var71, var108, var109, var110, var102, var104, var106);
                    this.vertex(param2, var69, var22 + 0.0, var71, var108, var109, var110, var102, var105, var106);
                    this.vertex(param2, var68, var22 + 0.0, var70, var108, var109, var110, var101, var105, var106);
                    if (var99 != this.waterOverlay) {
                        this.vertex(param2, var68, var22 + 0.0, var70, var108, var109, var110, var101, var105, var106);
                        this.vertex(param2, var69, var22 + 0.0, var71, var108, var109, var110, var102, var105, var106);
                        this.vertex(param2, var69, var22 + (double)var67, var71, var108, var109, var110, var102, var104, var106);
                        this.vertex(param2, var68, var22 + (double)var66, var70, var108, var109, var110, var101, var103, var106);
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
