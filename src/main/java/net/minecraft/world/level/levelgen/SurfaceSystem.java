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
    private final Registry<NormalNoise.NoiseParameters> noises;
    private final Map<ResourceKey<NormalNoise.NoiseParameters>, NormalNoise> noiseIntances = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, PositionalRandomFactory> positionalRandoms = new ConcurrentHashMap<>();
    private final PositionalRandomFactory randomFactory;
    private final NormalNoise surfaceNoise;
    private final NormalNoise surfaceSecondaryNoise;

    public SurfaceSystem(Registry<NormalNoise.NoiseParameters> param0, BlockState param1, int param2, long param3, WorldgenRandom.Algorithm param4) {
        this.noises = param0;
        this.defaultBlock = param1;
        this.seaLevel = param2;
        this.randomFactory = param4.newInstance(param3).forkPositional();
        this.clayBandsOffsetNoise = Noises.instantiate(param0, this.randomFactory, Noises.CLAY_BANDS_OFFSET);
        this.clayBands = generateBands(this.randomFactory.fromHashOf(new ResourceLocation("clay_bands")));
        this.surfaceNoise = Noises.instantiate(param0, this.randomFactory, Noises.SURFACE);
        this.surfaceSecondaryNoise = Noises.instantiate(param0, this.randomFactory, Noises.SURFACE_SECONDARY);
        this.badlandsPillarNoise = Noises.instantiate(param0, this.randomFactory, Noises.BADLANDS_PILLAR);
        this.badlandsPillarRoofNoise = Noises.instantiate(param0, this.randomFactory, Noises.BADLANDS_PILLAR_ROOF);
        this.badlandsSurfaceNoise = Noises.instantiate(param0, this.randomFactory, Noises.BADLANDS_SURFACE);
        this.icebergPillarNoise = Noises.instantiate(param0, this.randomFactory, Noises.ICEBERG_PILLAR);
        this.icebergPillarRoofNoise = Noises.instantiate(param0, this.randomFactory, Noises.ICEBERG_PILLAR_ROOF);
        this.icebergSurfaceNoise = Noises.instantiate(param0, this.randomFactory, Noises.ICEBERG_SURFACE);
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
                LevelHeightAccessor var0 = param4.getHeightAccessorForGeneration();
                if (param0 >= var0.getMinBuildHeight() && param0 < var0.getMaxBuildHeight()) {
                    param4.setBlockState(var0.setY(param0), param1, false);
                    if (!param1.getFluidState().isEmpty()) {
                        param4.markPosForPostprocessing(var0);
                    }
                }

            }

            @Override
            public String toString() {
                return "ChunkBlockColumn " + var1;
            }
        };
        SurfaceRules.Context var5 = new SurfaceRules.Context(this, param4, param5, param0::getBiome, param1, param3);
        SurfaceRules.SurfaceRule var6 = param6.apply(var5);
        BlockPos.MutableBlockPos var7 = new BlockPos.MutableBlockPos();

        for(int var8 = 0; var8 < 16; ++var8) {
            for(int var9 = 0; var9 < 16; ++var9) {
                int var10 = var2 + var8;
                int var11 = var3 + var9;
                int var12 = param4.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var8, var9) + 1;
                var0.setX(var10).setZ(var11);
                Biome var13 = param0.getBiome(var7.set(var10, param2 ? 0 : var12, var11));
                ResourceKey<Biome> var14 = param1.getResourceKey(var13).orElseThrow(() -> new IllegalStateException("Unregistered biome: " + var13));
                if (var14 == Biomes.ERODED_BADLANDS) {
                    this.erodedBadlandsExtension(var4, var10, var11, var12, param4);
                }

                int var15 = param4.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var8, var9) + 1;
                var5.updateXZ(var10, var11);
                int var16 = 0;
                int var17 = Integer.MIN_VALUE;
                int var18 = Integer.MAX_VALUE;
                int var19 = param4.getMinBuildHeight();

                for(int var20 = var15; var20 >= var19; --var20) {
                    BlockState var21 = var4.getBlock(var20);
                    if (var21.isAir()) {
                        var16 = 0;
                        var17 = Integer.MIN_VALUE;
                    } else if (!var21.getFluidState().isEmpty()) {
                        if (var17 == Integer.MIN_VALUE) {
                            var17 = var20 + 1;
                        }
                    } else {
                        if (var18 >= var20) {
                            var18 = DimensionType.WAY_BELOW_MIN_Y;

                            for(int var22 = var20 - 1; var22 >= var19 - 1; --var22) {
                                BlockState var23 = var4.getBlock(var22);
                                if (!this.isStone(var23)) {
                                    var18 = var22 + 1;
                                    break;
                                }
                            }
                        }

                        ++var16;
                        int var24 = var20 - var18 + 1;
                        var5.updateY(var16, var24, var17, var10, var20, var11);
                        if (var21 == this.defaultBlock) {
                            BlockState var25 = var6.tryApply(var10, var20, var11);
                            if (var25 != null) {
                                var4.setBlock(var20, var25);
                            }
                        }
                    }
                }

                if (var14 == Biomes.FROZEN_OCEAN || var14 == Biomes.DEEP_FROZEN_OCEAN) {
                    this.frozenOceanExtension(var5.getMinSurfaceLevel(), var13, var4, var7, var10, var11, var12);
                }
            }
        }

    }

    protected int getSurfaceDepth(int param0, int param1) {
        double var0 = this.surfaceNoise.getValue((double)param0, 0.0, (double)param1);
        return (int)(var0 * 2.75 + 3.0 + this.randomFactory.at(param0, 0, param1).nextDouble() * 0.25);
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
        Function<BlockPos, Biome> param2,
        ChunkAccess param3,
        NoiseChunk param4,
        BlockPos param5,
        boolean param6
    ) {
        SurfaceRules.Context var0 = new SurfaceRules.Context(
            this, param3, param4, param2, param1.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), param1
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
            RandomSource var9 = this.randomFactory.at(param4, 0, param5);
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
