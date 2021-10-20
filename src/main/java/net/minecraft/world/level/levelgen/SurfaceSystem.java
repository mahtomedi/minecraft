package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
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
    private final NormalNoise icebergAndBadlandsPillarNoise;
    private final NormalNoise icebergAndBadlandsPillarRoofNoise;
    private final Registry<NormalNoise.NoiseParameters> noises;
    private final Map<ResourceKey<NormalNoise.NoiseParameters>, NormalNoise> noiseIntances = new ConcurrentHashMap<>();
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
        this.clayBands = generateBands(this.randomFactory.fromHashOf("clay_bands"));
        this.surfaceNoise = Noises.instantiate(param1, this.randomFactory, Noises.SURFACE);
        this.icebergAndBadlandsPillarNoise = Noises.instantiate(param1, this.randomFactory, Noises.ICEBERG_AND_BADLANDS_PILLAR);
        this.icebergAndBadlandsPillarRoofNoise = Noises.instantiate(param1, this.randomFactory, Noises.ICEBERG_AND_BADLANDS_PILLAR_ROOF);
    }

    protected NormalNoise getOrCreateNoise(ResourceKey<NormalNoise.NoiseParameters> param0) {
        return this.noiseIntances.computeIfAbsent(param0, param1 -> Noises.instantiate(this.noises, this.randomFactory, param0));
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
        SurfaceRules.Context var5 = new SurfaceRules.Context(this, param3);
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
                    this.erodedBadlandsExtension(var16, var14, var4, var10, var11, var12);
                }

                int var19 = param4.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var8, var9) + 1;
                int var20 = (int)(var14 * 2.75 + 3.0 + var13.nextDouble() * 0.25);
                int var23;
                int var24;
                if (var18 != Biomes.BASALT_DELTAS
                    && var18 != Biomes.SOUL_SAND_VALLEY
                    && var18 != Biomes.WARPED_FOREST
                    && var18 != Biomes.CRIMSON_FOREST
                    && var18 != Biomes.NETHER_WASTES) {
                    var23 = var19;
                    var24 = var16;
                } else {
                    var23 = 127;
                    var24 = 0;
                }

                int var25 = var18 != Biomes.WOODED_BADLANDS && var18 != Biomes.BADLANDS ? Integer.MAX_VALUE : 15;
                var5.updateXZ(param4, var10, var11, var20);
                int var26 = 0;
                int var27 = 0;
                int var28 = Integer.MIN_VALUE;
                int var29 = Integer.MAX_VALUE;

                for(int var30 = var23; var30 >= var24 && var27 < var25; --var30) {
                    BlockState var31 = var4.getBlock(var30);
                    if (var31.isAir()) {
                        var26 = 0;
                        var28 = Integer.MIN_VALUE;
                    } else if (!var31.is(this.defaultBlock.getBlock())) {
                        if (var28 == Integer.MIN_VALUE) {
                            var28 = var30 + 1;
                        }
                    } else {
                        if (var5.hasCeilingRules() && var29 >= var30) {
                            var29 = Integer.MIN_VALUE;

                            for(int var32 = var30 - 1; var32 >= var24; --var32) {
                                BlockState var33 = var4.getBlock(var32);
                                if (!var33.is(this.defaultBlock.getBlock())) {
                                    var29 = var32 + 1;
                                    break;
                                }
                            }
                        }

                        ++var26;
                        ++var27;
                        int var34 = var30 - var29 + 1;
                        Biome var35 = param0.getBiome(var7.set(var10, var30, var11));
                        ResourceKey<Biome> var36 = param1.getResourceKey(var35).orElseThrow(() -> new IllegalStateException("Unregistered biome: " + var17));
                        var5.updateY(var36, var35, var20, var26, var34, var28, var10, var30, var11);
                        BlockState var37 = var6.tryApply(var10, var30, var11);
                        if (var37 != null) {
                            var4.setBlock(var30, this.supportState(var4, var30, var37, (double)var28));
                        }
                    }
                }

                if (var18 == Biomes.FROZEN_OCEAN || var18 == Biomes.DEEP_FROZEN_OCEAN) {
                    this.frozenOceanExtension(var16, var17, var14, var4, var7, var10, var11, var12);
                }
            }
        }

    }

    @Deprecated
    public Optional<BlockState> topMaterial(
        SurfaceRules.RuleSource param0, CarvingContext param1, Biome param2, ResourceKey<Biome> param3, ChunkAccess param4, BlockPos param5, boolean param6
    ) {
        SurfaceRules.Context var0 = new SurfaceRules.Context(this, param1);
        SurfaceRules.SurfaceRule var1 = param0.apply(var0);
        RandomSource var2 = this.randomFactory.at(param5.getX(), 0, param5.getZ());
        double var3 = this.surfaceNoise.getValue((double)param5.getX(), 0.0, (double)param5.getZ());
        int var4 = (int)(var3 * 2.75 + 3.0 + var2.nextDouble() * 0.25);
        var0.updateXZ(param4, param5.getX(), param5.getZ(), var4);
        var0.updateY(param3, param2, var4, 1, 1, param6 ? param5.getY() + 1 : Integer.MIN_VALUE, param5.getX(), param5.getY(), param5.getZ());
        BlockState var5 = var1.tryApply(param5.getX(), param5.getY(), param5.getZ());
        return Optional.ofNullable(var5);
    }

    private void erodedBadlandsExtension(int param0, double param1, BlockColumn param2, int param3, int param4, int param5) {
        double var0 = Math.min(Math.abs(param1 * 8.25), this.icebergAndBadlandsPillarNoise.getValue((double)param3 * 0.25, 0.0, (double)param4 * 0.25) * 15.0);
        if (!(var0 <= 0.0)) {
            double var1 = 0.001953125;
            double var2 = Math.abs(this.icebergAndBadlandsPillarRoofNoise.getValue((double)param3 * 0.001953125, 0.0, (double)param4 * 0.001953125));
            double var3 = 64.0 + Math.min(var0 * var0 * 2.5, Math.ceil(var2 * 50.0) + 14.0);
            int var4 = Math.max(param5, (int)var3 + 1);

            for(int var5 = var4; var5 >= param0; --var5) {
                BlockState var6 = param2.getBlock(var5);
                if (var6.is(this.defaultBlock.getBlock())) {
                    break;
                }

                if (var6.is(Blocks.WATER)) {
                    return;
                }
            }

            for(int var7 = var4; var7 >= param0; --var7) {
                if (param2.getBlock(var7).isAir() && var7 < (int)var3) {
                    param2.setBlock(var7, this.defaultBlock);
                }
            }

        }
    }

    private void frozenOceanExtension(
        int param0, Biome param1, double param2, BlockColumn param3, BlockPos.MutableBlockPos param4, int param5, int param6, int param7
    ) {
        float var0 = param1.getTemperature(param4.set(param5, 63, param6));
        double var1 = Math.min(Math.abs(param2 * 8.25), this.icebergAndBadlandsPillarNoise.getValue((double)param5 * 0.1, 0.0, (double)param6 * 0.1) * 15.0);
        if (!(var1 <= 1.8)) {
            double var2 = 0.09765625;
            double var3 = Math.abs(this.icebergAndBadlandsPillarRoofNoise.getValue((double)param5 * 0.09765625, 0.0, (double)param6 * 0.09765625));
            double var4 = Math.min(var1 * var1 * 1.2, Math.ceil(var3 * 40.0) + 14.0);
            if (var0 > 0.1F) {
                var4 -= 2.0;
            }

            double var5;
            if (var4 > 2.0) {
                var4 += (double)this.seaLevel;
                var5 = (double)this.seaLevel - var4 - 7.0;
            } else {
                var4 = 0.0;
                var5 = 0.0;
            }

            double var7 = var4;
            RandomSource var8 = this.randomFactory.at(param5, 0, param6);
            int var9 = 2 + var8.nextInt(4);
            int var10 = this.seaLevel + 18 + var8.nextInt(10);
            int var11 = 0;

            for(int var12 = Math.max(param7, (int)var4 + 1); var12 >= param0; --var12) {
                if (param3.getBlock(var12).isAir() && var12 < (int)var7 && var8.nextDouble() > 0.01
                    || param3.getBlock(var12).getMaterial() == Material.WATER
                        && var12 > (int)var5
                        && var12 < this.seaLevel
                        && var5 != 0.0
                        && var8.nextDouble() > 0.15) {
                    if (var11 <= var9 && var12 > var10) {
                        param3.setBlock(var12, SNOW_BLOCK);
                        ++var11;
                    } else {
                        param3.setBlock(var12, PACKED_ICE);
                    }
                }
            }

        }
    }

    private BlockState supportState(BlockColumn param0, int param1, BlockState param2, double param3) {
        if ((double)param1 <= param3 && param2.is(Blocks.GRASS_BLOCK)) {
            return Blocks.DIRT.defaultBlockState();
        } else if (param0.getBlock(param1 - 1).is(this.defaultBlock.getBlock())) {
            return param2;
        } else if (param2.is(Blocks.SAND)) {
            return Blocks.SANDSTONE.defaultBlockState();
        } else if (param2.is(Blocks.RED_SAND)) {
            return Blocks.RED_SANDSTONE.defaultBlockState();
        } else {
            return param2.is(Blocks.GRAVEL) ? Blocks.STONE.defaultBlockState() : param2;
        }
    }

    private static BlockState[] generateBands(RandomSource param0) {
        BlockState[] var0 = new BlockState[64];
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
        int var2 = param0.nextInt(3) + 3;
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
        int var0 = param0.nextInt(4) + 2;

        for(int var1 = 0; var1 < var0; ++var1) {
            int var2 = param2 + param0.nextInt(3);
            int var3 = param0.nextInt(param1.length);

            for(int var4 = 0; var3 + var4 < param1.length && var4 < var2; ++var4) {
                param1[var3 + var4] = param3;
            }
        }

    }

    protected BlockState getBand(int param0, int param1, int param2) {
        int var0 = (int)Math.round(this.clayBandsOffsetNoise.getValue((double)param0, 0.0, (double)param2) * 2.0);
        return this.clayBands[(param1 + var0 + this.clayBands.length) % this.clayBands.length];
    }
}
