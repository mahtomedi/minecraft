package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
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
    private static final int HOW_FAR_BELOW_PRELIMINARY_SURFACE_LEVEL_TO_BUILD_SURFACE = 8;
    private static final int MAX_CLAY_DEPTH = 15;
    private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
    private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
    private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();
    private static final BlockState YELLOW_TERRACOTTA = Blocks.YELLOW_TERRACOTTA.defaultBlockState();
    private static final BlockState BROWN_TERRACOTTA = Blocks.BROWN_TERRACOTTA.defaultBlockState();
    private static final BlockState RED_TERRACOTTA = Blocks.RED_TERRACOTTA.defaultBlockState();
    private static final BlockState LIGHT_GRAY_TERRACOTTA = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
    private static final BlockState PACKED_ICE = Blocks.PACKED_ICE.defaultBlockState();
    private static final BlockState SNOW_BLOCK = Blocks.SNOW_BLOCK.defaultBlockState();
    private final NoiseSampler sampler;
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
    private final Registry<NormalNoise.NoiseParameters> noises;
    private final Map<ResourceKey<NormalNoise.NoiseParameters>, NormalNoise> noiseIntances = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, PositionalRandomFactory> positionalRandoms = new ConcurrentHashMap<>();
    private final PositionalRandomFactory randomFactory;
    private final NormalNoise surfaceNoise;

    public SurfaceSystem(
        NoiseSampler param0, Registry<NormalNoise.NoiseParameters> param1, BlockState param2, int param3, long param4, WorldgenRandom.Algorithm param5
    ) {
        this.sampler = param0;
        this.noises = param1;
        this.defaultBlock = param2;
        this.seaLevel = param3;
        this.randomFactory = param5.newInstance(param4).forkPositional();
        this.clayBandsOffsetNoise = Noises.instantiate(param1, this.randomFactory, Noises.CLAY_BANDS_OFFSET);
        this.clayBands = generateBands(this.randomFactory.fromHashOf(new ResourceLocation("clay_bands")));
        this.surfaceNoise = Noises.instantiate(param1, this.randomFactory, Noises.SURFACE);
        this.badlandsPillarNoise = Noises.instantiate(param1, this.randomFactory, Noises.BADLANDS_PILLAR);
        this.badlandsPillarRoofNoise = Noises.instantiate(param1, this.randomFactory, Noises.BADLANDS_PILLAR_ROOF);
        this.badlandsSurfaceNoise = Noises.instantiate(param1, this.randomFactory, Noises.BADLANDS_SURFACE);
        this.icebergPillarNoise = Noises.instantiate(param1, this.randomFactory, Noises.ICEBERG_PILLAR);
        this.icebergPillarRoofNoise = Noises.instantiate(param1, this.randomFactory, Noises.ICEBERG_PILLAR_ROOF);
        this.icebergSurfaceNoise = Noises.instantiate(param1, this.randomFactory, Noises.ICEBERG_SURFACE);
    }

    protected NormalNoise getOrCreateNoise(ResourceKey<NormalNoise.NoiseParameters> param0) {
        return this.noiseIntances.computeIfAbsent(param0, param1 -> Noises.instantiate(this.noises, this.randomFactory, param0));
    }

    protected PositionalRandomFactory getOrCreateRandomFactory(ResourceLocation param0) {
        return this.positionalRandoms.computeIfAbsent(param0, param1 -> this.randomFactory.fromHashOf(param0).forkPositional());
    }

    public void buildSurface(
        BiomeManager param0,
        Registry<Biome> param1,
        boolean param2,
        WorldGenerationContext param3,
        final ChunkAccess param4,
        NoiseChunk param5,
        SurfaceRules.RuleSource param6
    ) {
        final BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        final ChunkPos var1 = param4.getPos();
        int var2 = var1.getMinBlockX();
        int var3 = var1.getMinBlockZ();
        BlockColumn var4 = new BlockColumn() {
            @Override
            public BlockState getBlock(int param0) {
                return param4.getBlockState(var0.setY(param0));
            }

            @Override
            public void setBlock(int param0, BlockState param1) {
                param4.setBlockState(var0.setY(param0), param1, false);
            }

            @Override
            public String toString() {
                return "ChunkBlockColumn " + var1;
            }
        };
        SurfaceRules.Context var5 = new SurfaceRules.Context(this, param4, param0::getBiome, param1, param3);
        SurfaceRules.SurfaceRule var6 = param6.apply(var5);
        BlockPos.MutableBlockPos var7 = new BlockPos.MutableBlockPos();

        for(int var8 = 0; var8 < 16; ++var8) {
            for(int var9 = 0; var9 < 16; ++var9) {
                int var10 = var2 + var8;
                int var11 = var3 + var9;
                int var12 = param4.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var8, var9) + 1;
                RandomSource var13 = this.randomFactory.at(var10, 0, var11);
                double var14 = this.surfaceNoise.getValue((double)var10, 0.0, (double)var11);
                var0.setX(var10).setZ(var11);
                int var15 = this.sampler.getPreliminarySurfaceLevel(var10, var11, param5.terrainInfoInterpolated(var10, var11));
                int var16 = var15 - 8;
                Biome var17 = param0.getBiome(var7.set(var10, param2 ? 0 : var12, var11));
                ResourceKey<Biome> var18 = param1.getResourceKey(var17).orElseThrow(() -> new IllegalStateException("Unregistered biome: " + var17));
                if (var18 == Biomes.ERODED_BADLANDS) {
                    this.erodedBadlandsExtension(var4, var10, var11, var12, param4);
                }

                int var19 = param4.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var8, var9) + 1;
                int var20 = (int)(var14 * 2.75 + 3.0 + var13.nextDouble() * 0.25);
                int var21 = var18 != Biomes.WOODED_BADLANDS && var18 != Biomes.BADLANDS ? Integer.MAX_VALUE : 15;
                var5.updateXZ(var10, var11, var20);
                int var22 = 0;
                int var23 = 0;
                int var24 = Integer.MIN_VALUE;
                int var25 = Integer.MAX_VALUE;
                int var26 = param4.getMinBuildHeight();

                for(int var27 = var19; var27 >= var26 && var23 < var21; --var27) {
                    BlockState var28 = var4.getBlock(var27);
                    if (var28.isAir()) {
                        var22 = 0;
                        var24 = Integer.MIN_VALUE;
                    } else if (!var28.getFluidState().isEmpty()) {
                        if (var24 == Integer.MIN_VALUE) {
                            var24 = var27 + 1;
                        }
                    } else {
                        if (var25 >= var27) {
                            var25 = DimensionType.WAY_BELOW_MIN_Y;

                            for(int var29 = var27 - 1; var29 >= var26 - 1; --var29) {
                                BlockState var30 = var4.getBlock(var29);
                                if (!this.isStone(var30)) {
                                    var25 = var29 + 1;
                                    break;
                                }
                            }
                        }

                        ++var22;
                        ++var23;
                        int var31 = var27 - var25 + 1;
                        var5.updateY(var16, var22, var31, var24, var10, var27, var11);
                        BlockState var32 = var6.tryApply(var10, var27, var11);
                        if (var32 != null) {
                            var4.setBlock(var27, var32);
                        }
                    }
                }

                if (var18 == Biomes.FROZEN_OCEAN || var18 == Biomes.DEEP_FROZEN_OCEAN) {
                    this.frozenOceanExtension(var16, var17, var4, var7, var10, var11, var12);
                }
            }
        }

    }

    private boolean isStone(BlockState param0) {
        return !param0.isAir() && param0.getFluidState().isEmpty();
    }

    @Deprecated
    public Optional<BlockState> topMaterial(
        SurfaceRules.RuleSource param0, CarvingContext param1, Function<BlockPos, Biome> param2, ChunkAccess param3, BlockPos param4, boolean param5
    ) {
        SurfaceRules.Context var0 = new SurfaceRules.Context(this, param3, param2, param1.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), param1);
        SurfaceRules.SurfaceRule var1 = param0.apply(var0);
        int var2 = param4.getX();
        int var3 = param4.getY();
        int var4 = param4.getZ();
        RandomSource var5 = this.randomFactory.at(var2, 0, var4);
        double var6 = this.surfaceNoise.getValue((double)var2, 0.0, (double)var4);
        int var7 = (int)(var6 * 2.75 + 3.0 + var5.nextDouble() * 0.25);
        var0.updateXZ(var2, var4, var7);
        int var8 = var3 - 16;
        var0.updateY(var8, 1, 1, param5 ? var3 + 1 : Integer.MIN_VALUE, var2, var3, var4);
        BlockState var9 = var1.tryApply(var2, var3, var4);
        return Optional.ofNullable(var9);
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
        float var0 = param1.getTemperature(param3.set(param4, 63, param5));
        double var1 = 1.28;
        double var2 = Math.min(
            Math.abs(this.icebergSurfaceNoise.getValue((double)param4, 0.0, (double)param5) * 8.25),
            this.icebergPillarNoise.getValue((double)param4 * 1.28, 0.0, (double)param5 * 1.28) * 15.0
        );
        if (!(var2 <= 1.8)) {
            double var3 = 1.17;
            double var4 = 1.5;
            double var5 = Math.abs(this.icebergPillarRoofNoise.getValue((double)param4 * 1.17, 0.0, (double)param5 * 1.17) * 1.5);
            double var6 = Math.min(var2 * var2 * 1.2, Math.ceil(var5 * 40.0) + 14.0);
            if (var0 > 0.1F) {
                var6 -= 2.0;
            }

            double var7;
            if (var6 > 2.0) {
                var7 = (double)this.seaLevel - var6 - 7.0;
                var6 += (double)this.seaLevel;
            } else {
                var6 = 0.0;
                var7 = 0.0;
            }

            double var9 = var6;
            RandomSource var10 = this.randomFactory.at(param4, 0, param5);
            int var11 = 2 + var10.nextInt(4);
            int var12 = this.seaLevel + 18 + var10.nextInt(10);
            int var13 = 0;

            for(int var14 = Math.max(param6, (int)var6 + 1); var14 >= param0; --var14) {
                if (param2.getBlock(var14).isAir() && var14 < (int)var9 && var10.nextDouble() > 0.01
                    || param2.getBlock(var14).getMaterial() == Material.WATER
                        && var14 > (int)var7
                        && var14 < this.seaLevel
                        && var7 != 0.0
                        && var10.nextDouble() > 0.15) {
                    if (var13 <= var11 && var14 > var12) {
                        param2.setBlock(var14, SNOW_BLOCK);
                        ++var13;
                    } else {
                        param2.setBlock(var14, PACKED_ICE);
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
