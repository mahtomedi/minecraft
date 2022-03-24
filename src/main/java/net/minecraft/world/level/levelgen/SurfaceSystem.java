package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.Material;

public class SurfaceSystem {
    private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
    private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
    private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();
    private static final BlockState YELLOW_TERRACOTTA = Blocks.YELLOW_TERRACOTTA.defaultBlockState();
    private static final BlockState BROWN_TERRACOTTA = Blocks.BROWN_TERRACOTTA.defaultBlockState();
    private static final BlockState RED_TERRACOTTA = Blocks.RED_TERRACOTTA.defaultBlockState();
    private static final BlockState LIGHT_GRAY_TERRACOTTA = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
    private static final BlockState PACKED_ICE = Blocks.PACKED_ICE.defaultBlockState();
    private static final BlockState SNOW_BLOCK = Blocks.SNOW_BLOCK.defaultBlockState();
    private final BlockState defaultBlock;
    private final int seaLevel;
    private final BlockState[] clayBands;
    private final NormalNoise clayBandsOffsetNoise;
    private final NormalNoise badlandsPillarNoise;
    private final NormalNoise badlandsPillarRoofNoise;
    private final NormalNoise badlandsSurfaceNoise;
    private final NormalNoise icebergPillarNoise;
    private final NormalNoise icebergPillarRoofNoise;
    private final NormalNoise icebergSurfaceNoise;
    private final PositionalRandomFactory noiseRandom;
    private final NormalNoise surfaceNoise;
    private final NormalNoise surfaceSecondaryNoise;

    public SurfaceSystem(RandomState param0, BlockState param1, int param2, PositionalRandomFactory param3) {
        this.defaultBlock = param1;
        this.seaLevel = param2;
        this.noiseRandom = param3;
        this.clayBandsOffsetNoise = param0.getOrCreateNoise(Noises.CLAY_BANDS_OFFSET);
        this.clayBands = generateBands(param3.fromHashOf(new ResourceLocation("clay_bands")));
        this.surfaceNoise = param0.getOrCreateNoise(Noises.SURFACE);
        this.surfaceSecondaryNoise = param0.getOrCreateNoise(Noises.SURFACE_SECONDARY);
        this.badlandsPillarNoise = param0.getOrCreateNoise(Noises.BADLANDS_PILLAR);
        this.badlandsPillarRoofNoise = param0.getOrCreateNoise(Noises.BADLANDS_PILLAR_ROOF);
        this.badlandsSurfaceNoise = param0.getOrCreateNoise(Noises.BADLANDS_SURFACE);
        this.icebergPillarNoise = param0.getOrCreateNoise(Noises.ICEBERG_PILLAR);
        this.icebergPillarRoofNoise = param0.getOrCreateNoise(Noises.ICEBERG_PILLAR_ROOF);
        this.icebergSurfaceNoise = param0.getOrCreateNoise(Noises.ICEBERG_SURFACE);
    }

    public void buildSurface(
        RandomState param0,
        BiomeManager param1,
        Registry<Biome> param2,
        boolean param3,
        WorldGenerationContext param4,
        final ChunkAccess param5,
        NoiseChunk param6,
        SurfaceRules.RuleSource param7
    ) {
        final BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        final ChunkPos var1 = param5.getPos();
        int var2 = var1.getMinBlockX();
        int var3 = var1.getMinBlockZ();
        BlockColumn var4 = new BlockColumn() {
            @Override
            public BlockState getBlock(int param0) {
                return param5.getBlockState(var0.setY(param0));
            }

            @Override
            public void setBlock(int param0, BlockState param1) {
                LevelHeightAccessor var0 = param5.getHeightAccessorForGeneration();
                if (param0 >= var0.getMinBuildHeight() && param0 < var0.getMaxBuildHeight()) {
                    param5.setBlockState(var0.setY(param0), param1, false);
                    if (!param1.getFluidState().isEmpty()) {
                        param5.markPosForPostprocessing(var0);
                    }
                }

            }

            @Override
            public String toString() {
                return "ChunkBlockColumn " + var1;
            }
        };
        SurfaceRules.Context var5 = new SurfaceRules.Context(this, param0, param5, param6, param1::getBiome, param2, param4);
        SurfaceRules.SurfaceRule var6 = param7.apply(var5);
        BlockPos.MutableBlockPos var7 = new BlockPos.MutableBlockPos();

        for(int var8 = 0; var8 < 16; ++var8) {
            for(int var9 = 0; var9 < 16; ++var9) {
                int var10 = var2 + var8;
                int var11 = var3 + var9;
                int var12 = param5.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var8, var9) + 1;
                var0.setX(var10).setZ(var11);
                Holder<Biome> var13 = param1.getBiome(var7.set(var10, param3 ? 0 : var12, var11));
                if (var13.is(Biomes.ERODED_BADLANDS)) {
                    this.erodedBadlandsExtension(var4, var10, var11, var12, param5);
                }

                int var14 = param5.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var8, var9) + 1;
                var5.updateXZ(var10, var11);
                int var15 = 0;
                int var16 = Integer.MIN_VALUE;
                int var17 = Integer.MAX_VALUE;
                int var18 = param5.getMinBuildHeight();

                for(int var19 = var14; var19 >= var18; --var19) {
                    BlockState var20 = var4.getBlock(var19);
                    if (var20.isAir()) {
                        var15 = 0;
                        var16 = Integer.MIN_VALUE;
                    } else if (!var20.getFluidState().isEmpty()) {
                        if (var16 == Integer.MIN_VALUE) {
                            var16 = var19 + 1;
                        }
                    } else {
                        if (var17 >= var19) {
                            var17 = DimensionType.WAY_BELOW_MIN_Y;

                            for(int var21 = var19 - 1; var21 >= var18 - 1; --var21) {
                                BlockState var22 = var4.getBlock(var21);
                                if (!this.isStone(var22)) {
                                    var17 = var21 + 1;
                                    break;
                                }
                            }
                        }

                        ++var15;
                        int var23 = var19 - var17 + 1;
                        var5.updateY(var15, var23, var16, var10, var19, var11);
                        if (var20 == this.defaultBlock) {
                            BlockState var24 = var6.tryApply(var10, var19, var11);
                            if (var24 != null) {
                                var4.setBlock(var19, var24);
                            }
                        }
                    }
                }

                if (var13.is(Biomes.FROZEN_OCEAN) || var13.is(Biomes.DEEP_FROZEN_OCEAN)) {
                    this.frozenOceanExtension(var5.getMinSurfaceLevel(), var13.value(), var4, var7, var10, var11, var12);
                }
            }
        }

    }

    protected int getSurfaceDepth(int param0, int param1) {
        double var0 = this.surfaceNoise.getValue((double)param0, 0.0, (double)param1);
        return (int)(var0 * 2.75 + 3.0 + this.noiseRandom.at(param0, 0, param1).nextDouble() * 0.25);
    }

    protected double getSurfaceSecondary(int param0, int param1) {
        return this.surfaceSecondaryNoise.getValue((double)param0, 0.0, (double)param1);
    }

    private boolean isStone(BlockState param0) {
        return !param0.isAir() && param0.getFluidState().isEmpty();
    }

    @Deprecated
    public Optional<BlockState> topMaterial(
        SurfaceRules.RuleSource param0,
        CarvingContext param1,
        Function<BlockPos, Holder<Biome>> param2,
        ChunkAccess param3,
        NoiseChunk param4,
        BlockPos param5,
        boolean param6
    ) {
        SurfaceRules.Context var0 = new SurfaceRules.Context(
            this, param1.randomState(), param3, param4, param2, param1.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), param1
        );
        SurfaceRules.SurfaceRule var1 = param0.apply(var0);
        int var2 = param5.getX();
        int var3 = param5.getY();
        int var4 = param5.getZ();
        var0.updateXZ(var2, var4);
        var0.updateY(1, 1, param6 ? var3 + 1 : Integer.MIN_VALUE, var2, var3, var4);
        BlockState var5 = var1.tryApply(var2, var3, var4);
        return Optional.ofNullable(var5);
    }

    private void erodedBadlandsExtension(BlockColumn param0, int param1, int param2, int param3, LevelHeightAccessor param4) {
        double var0 = 0.2;
        double var1 = Math.min(
            Math.abs(this.badlandsSurfaceNoise.getValue((double)param1, 0.0, (double)param2) * 8.25),
            this.badlandsPillarNoise.getValue((double)param1 * 0.2, 0.0, (double)param2 * 0.2) * 15.0
        );
        if (!(var1 <= 0.0)) {
            double var2 = 0.75;
            double var3 = 1.5;
            double var4 = Math.abs(this.badlandsPillarRoofNoise.getValue((double)param1 * 0.75, 0.0, (double)param2 * 0.75) * 1.5);
            double var5 = 64.0 + Math.min(var1 * var1 * 2.5, Math.ceil(var4 * 50.0) + 24.0);
            int var6 = Mth.floor(var5);
            if (param3 <= var6) {
                for(int var7 = var6; var7 >= param4.getMinBuildHeight(); --var7) {
                    BlockState var8 = param0.getBlock(var7);
                    if (var8.is(this.defaultBlock.getBlock())) {
                        break;
                    }

                    if (var8.is(Blocks.WATER)) {
                        return;
                    }
                }

                for(int var9 = var6; var9 >= param4.getMinBuildHeight() && param0.getBlock(var9).isAir(); --var9) {
                    param0.setBlock(var9, this.defaultBlock);
                }

            }
        }
    }

    private void frozenOceanExtension(int param0, Biome param1, BlockColumn param2, BlockPos.MutableBlockPos param3, int param4, int param5, int param6) {
        double var0 = 1.28;
        double var1 = Math.min(
            Math.abs(this.icebergSurfaceNoise.getValue((double)param4, 0.0, (double)param5) * 8.25),
            this.icebergPillarNoise.getValue((double)param4 * 1.28, 0.0, (double)param5 * 1.28) * 15.0
        );
        if (!(var1 <= 1.8)) {
            double var2 = 1.17;
            double var3 = 1.5;
            double var4 = Math.abs(this.icebergPillarRoofNoise.getValue((double)param4 * 1.17, 0.0, (double)param5 * 1.17) * 1.5);
            double var5 = Math.min(var1 * var1 * 1.2, Math.ceil(var4 * 40.0) + 14.0);
            if (param1.shouldMeltFrozenOceanIcebergSlightly(param3.set(param4, 63, param5))) {
                var5 -= 2.0;
            }

            double var6;
            if (var5 > 2.0) {
                var6 = (double)this.seaLevel - var5 - 7.0;
                var5 += (double)this.seaLevel;
            } else {
                var5 = 0.0;
                var6 = 0.0;
            }

            double var8 = var5;
            RandomSource var9 = this.noiseRandom.at(param4, 0, param5);
            int var10 = 2 + var9.nextInt(4);
            int var11 = this.seaLevel + 18 + var9.nextInt(10);
            int var12 = 0;

            for(int var13 = Math.max(param6, (int)var5 + 1); var13 >= param0; --var13) {
                if (param2.getBlock(var13).isAir() && var13 < (int)var8 && var9.nextDouble() > 0.01
                    || param2.getBlock(var13).getMaterial() == Material.WATER
                        && var13 > (int)var6
                        && var13 < this.seaLevel
                        && var6 != 0.0
                        && var9.nextDouble() > 0.15) {
                    if (var12 <= var10 && var13 > var11) {
                        param2.setBlock(var13, SNOW_BLOCK);
                        ++var12;
                    } else {
                        param2.setBlock(var13, PACKED_ICE);
                    }
                }
            }

        }
    }

    private static BlockState[] generateBands(RandomSource param0) {
        BlockState[] var0 = new BlockState[192];
        Arrays.fill(var0, TERRACOTTA);

        for(int var1 = 0; var1 < var0.length; ++var1) {
            var1 += param0.nextInt(5) + 1;
            if (var1 < var0.length) {
                var0[var1] = ORANGE_TERRACOTTA;
            }
        }

        makeBands(param0, var0, 1, YELLOW_TERRACOTTA);
        makeBands(param0, var0, 2, BROWN_TERRACOTTA);
        makeBands(param0, var0, 1, RED_TERRACOTTA);
        int var2 = param0.nextIntBetweenInclusive(9, 15);
        int var3 = 0;

        for(int var4 = 0; var3 < var2 && var4 < var0.length; var4 += param0.nextInt(16) + 4) {
            var0[var4] = WHITE_TERRACOTTA;
            if (var4 - 1 > 0 && param0.nextBoolean()) {
                var0[var4 - 1] = LIGHT_GRAY_TERRACOTTA;
            }

            if (var4 + 1 < var0.length && param0.nextBoolean()) {
                var0[var4 + 1] = LIGHT_GRAY_TERRACOTTA;
            }

            ++var3;
        }

        return var0;
    }

    private static void makeBands(RandomSource param0, BlockState[] param1, int param2, BlockState param3) {
        int var0 = param0.nextIntBetweenInclusive(6, 15);

        for(int var1 = 0; var1 < var0; ++var1) {
            int var2 = param2 + param0.nextInt(3);
            int var3 = param0.nextInt(param1.length);

            for(int var4 = 0; var3 + var4 < param1.length && var4 < var2; ++var4) {
                param1[var3 + var4] = param3;
            }
        }

    }

    protected BlockState getBand(int param0, int param1, int param2) {
        int var0 = (int)Math.round(this.clayBandsOffsetNoise.getValue((double)param0, 0.0, (double)param2) * 4.0);
        return this.clayBands[(param1 + var0 + this.clayBands.length) % this.clayBands.length];
    }
}
