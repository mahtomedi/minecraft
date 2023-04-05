package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.material.Material;

@Deprecated
public class LakeFeature extends Feature<LakeFeature.Configuration> {
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

    public LakeFeature(Codec<LakeFeature.Configuration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<LakeFeature.Configuration> param0) {
        BlockPos var0 = param0.origin();
        WorldGenLevel var1 = param0.level();
        RandomSource var2 = param0.random();
        LakeFeature.Configuration var3 = param0.config();
        if (var0.getY() <= var1.getMinBuildHeight() + 4) {
            return false;
        } else {
            var0 = var0.below(4);
            boolean[] var4 = new boolean[2048];
            int var5 = var2.nextInt(4) + 4;

            for(int var6 = 0; var6 < var5; ++var6) {
                double var7 = var2.nextDouble() * 6.0 + 3.0;
                double var8 = var2.nextDouble() * 4.0 + 2.0;
                double var9 = var2.nextDouble() * 6.0 + 3.0;
                double var10 = var2.nextDouble() * (16.0 - var7 - 2.0) + 1.0 + var7 / 2.0;
                double var11 = var2.nextDouble() * (8.0 - var8 - 4.0) + 2.0 + var8 / 2.0;
                double var12 = var2.nextDouble() * (16.0 - var9 - 2.0) + 1.0 + var9 / 2.0;

                for(int var13 = 1; var13 < 15; ++var13) {
                    for(int var14 = 1; var14 < 15; ++var14) {
                        for(int var15 = 1; var15 < 7; ++var15) {
                            double var16 = ((double)var13 - var10) / (var7 / 2.0);
                            double var17 = ((double)var15 - var11) / (var8 / 2.0);
                            double var18 = ((double)var14 - var12) / (var9 / 2.0);
                            double var19 = var16 * var16 + var17 * var17 + var18 * var18;
                            if (var19 < 1.0) {
                                var4[(var13 * 16 + var14) * 8 + var15] = true;
                            }
                        }
                    }
                }
            }

            BlockState var20 = var3.fluid().getState(var2, var0);

            for(int var21 = 0; var21 < 16; ++var21) {
                for(int var22 = 0; var22 < 16; ++var22) {
                    for(int var23 = 0; var23 < 8; ++var23) {
                        boolean var24 = !var4[(var21 * 16 + var22) * 8 + var23]
                            && (
                                var21 < 15 && var4[((var21 + 1) * 16 + var22) * 8 + var23]
                                    || var21 > 0 && var4[((var21 - 1) * 16 + var22) * 8 + var23]
                                    || var22 < 15 && var4[(var21 * 16 + var22 + 1) * 8 + var23]
                                    || var22 > 0 && var4[(var21 * 16 + (var22 - 1)) * 8 + var23]
                                    || var23 < 7 && var4[(var21 * 16 + var22) * 8 + var23 + 1]
                                    || var23 > 0 && var4[(var21 * 16 + var22) * 8 + (var23 - 1)]
                            );
                        if (var24) {
                            BlockState var25 = var1.getBlockState(var0.offset(var21, var23, var22));
                            Material var26 = var25.getMaterial();
                            if (var23 >= 4 && var25.liquid()) {
                                return false;
                            }

                            if (var23 < 4 && !var26.isSolid() && var1.getBlockState(var0.offset(var21, var23, var22)) != var20) {
                                return false;
                            }
                        }
                    }
                }
            }

            for(int var27 = 0; var27 < 16; ++var27) {
                for(int var28 = 0; var28 < 16; ++var28) {
                    for(int var29 = 0; var29 < 8; ++var29) {
                        if (var4[(var27 * 16 + var28) * 8 + var29]) {
                            BlockPos var30 = var0.offset(var27, var29, var28);
                            if (this.canReplaceBlock(var1.getBlockState(var30))) {
                                boolean var31 = var29 >= 4;
                                var1.setBlock(var30, var31 ? AIR : var20, 2);
                                if (var31) {
                                    var1.scheduleTick(var30, AIR.getBlock(), 0);
                                    this.markAboveForPostProcessing(var1, var30);
                                }
                            }
                        }
                    }
                }
            }

            BlockState var32 = var3.barrier().getState(var2, var0);
            if (!var32.isAir()) {
                for(int var33 = 0; var33 < 16; ++var33) {
                    for(int var34 = 0; var34 < 16; ++var34) {
                        for(int var35 = 0; var35 < 8; ++var35) {
                            boolean var36 = !var4[(var33 * 16 + var34) * 8 + var35]
                                && (
                                    var33 < 15 && var4[((var33 + 1) * 16 + var34) * 8 + var35]
                                        || var33 > 0 && var4[((var33 - 1) * 16 + var34) * 8 + var35]
                                        || var34 < 15 && var4[(var33 * 16 + var34 + 1) * 8 + var35]
                                        || var34 > 0 && var4[(var33 * 16 + (var34 - 1)) * 8 + var35]
                                        || var35 < 7 && var4[(var33 * 16 + var34) * 8 + var35 + 1]
                                        || var35 > 0 && var4[(var33 * 16 + var34) * 8 + (var35 - 1)]
                                );
                            if (var36 && (var35 < 4 || var2.nextInt(2) != 0)) {
                                BlockState var37 = var1.getBlockState(var0.offset(var33, var35, var34));
                                if (var37.getMaterial().isSolid() && !var37.is(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE)) {
                                    BlockPos var38 = var0.offset(var33, var35, var34);
                                    var1.setBlock(var38, var32, 2);
                                    this.markAboveForPostProcessing(var1, var38);
                                }
                            }
                        }
                    }
                }
            }

            if (var20.getFluidState().is(FluidTags.WATER)) {
                for(int var39 = 0; var39 < 16; ++var39) {
                    for(int var40 = 0; var40 < 16; ++var40) {
                        int var41 = 4;
                        BlockPos var42 = var0.offset(var39, 4, var40);
                        if (var1.getBiome(var42).value().shouldFreeze(var1, var42, false) && this.canReplaceBlock(var1.getBlockState(var42))) {
                            var1.setBlock(var42, Blocks.ICE.defaultBlockState(), 2);
                        }
                    }
                }
            }

            return true;
        }
    }

    private boolean canReplaceBlock(BlockState param0) {
        return !param0.is(BlockTags.FEATURES_CANNOT_REPLACE);
    }

    public static record Configuration(BlockStateProvider fluid, BlockStateProvider barrier) implements FeatureConfiguration {
        public static final Codec<LakeFeature.Configuration> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        BlockStateProvider.CODEC.fieldOf("fluid").forGetter(LakeFeature.Configuration::fluid),
                        BlockStateProvider.CODEC.fieldOf("barrier").forGetter(LakeFeature.Configuration::barrier)
                    )
                    .apply(param0, LakeFeature.Configuration::new)
        );
    }
}
