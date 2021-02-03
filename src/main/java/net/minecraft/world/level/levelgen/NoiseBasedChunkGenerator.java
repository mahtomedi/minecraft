package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.world.level.levelgen.synth.SurfaceNoise;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class NoiseBasedChunkGenerator extends ChunkGenerator {
    public static final Codec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(param0x -> param0x.biomeSource),
                    Codec.LONG.fieldOf("seed").stable().forGetter(param0x -> param0x.seed),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(param0x -> param0x.settings)
                )
                .apply(param0, param0.stable(NoiseBasedChunkGenerator::new))
    );
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState[] EMPTY_COLUMN = new BlockState[0];
    private final int cellHeight;
    private final int cellWidth;
    private final int cellCountX;
    private final int cellCountY;
    private final int cellCountZ;
    protected final RandomSource random;
    private final SurfaceNoise surfaceNoise;
    protected final BlockState defaultBlock;
    protected final BlockState defaultFluid;
    private final long seed;
    protected final Supplier<NoiseGeneratorSettings> settings;
    private final int height;
    private final NoiseSampler sampler;

    public NoiseBasedChunkGenerator(BiomeSource param0, long param1, Supplier<NoiseGeneratorSettings> param2) {
        this(param0, param0, param1, param2);
    }

    private NoiseBasedChunkGenerator(BiomeSource param0, BiomeSource param1, long param2, Supplier<NoiseGeneratorSettings> param3) {
        super(param0, param1, param3.get().structureSettings(), param2);
        this.seed = param2;
        NoiseGeneratorSettings var0 = param3.get();
        this.settings = param3;
        NoiseSettings var1 = var0.noiseSettings();
        this.height = var1.height();
        this.cellHeight = QuartPos.toBlock(var1.noiseSizeVertical());
        this.cellWidth = QuartPos.toBlock(var1.noiseSizeHorizontal());
        this.defaultBlock = var0.getDefaultBlock();
        this.defaultFluid = var0.getDefaultFluid();
        this.cellCountX = 16 / this.cellWidth;
        this.cellCountY = var1.height() / this.cellHeight;
        this.cellCountZ = 16 / this.cellWidth;
        this.random = new WorldgenRandom(param2);
        BlendedNoise var2 = new BlendedNoise(this.random);
        this.surfaceNoise = (SurfaceNoise)(var1.useSimplexSurfaceNoise()
            ? new PerlinSimplexNoise(this.random, IntStream.rangeClosed(-3, 0))
            : new PerlinNoise(this.random, IntStream.rangeClosed(-3, 0)));
        this.random.consumeCount(2620);
        PerlinNoise var3 = new PerlinNoise(this.random, IntStream.rangeClosed(-15, 0));
        SimplexNoise var5;
        if (var1.islandNoiseOverride()) {
            WorldgenRandom var4 = new WorldgenRandom(param2);
            var4.consumeCount(17292);
            var5 = new SimplexNoise(var4);
        } else {
            var5 = null;
        }

        this.sampler = new NoiseSampler(param0, this.cellWidth, this.cellHeight, this.cellCountY, var1, var2, var5, var3);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ChunkGenerator withSeed(long param0) {
        return new NoiseBasedChunkGenerator(this.biomeSource.withSeed(param0), param0, this.settings);
    }

    public boolean stable(long param0, ResourceKey<NoiseGeneratorSettings> param1) {
        return this.seed == param0 && this.settings.get().stable(param1);
    }

    private double[] makeAndFillNoiseColumn(int param0, int param1, int param2, int param3) {
        double[] var0 = new double[param3 + 1];
        this.sampler.fillNoiseColumn(var0, param0, param1, this.settings.get().noiseSettings(), this.getSeaLevel(), param2, param3);
        return var0;
    }

    @Override
    public int getBaseHeight(int param0, int param1, Heightmap.Types param2, LevelHeightAccessor param3) {
        int var0 = Math.max(this.settings.get().noiseSettings().minY(), param3.getMinBuildHeight());
        int var1 = Math.min(this.settings.get().noiseSettings().minY() + this.settings.get().noiseSettings().height(), param3.getMaxBuildHeight());
        int var2 = Mth.intFloorDiv(var0, this.cellHeight);
        int var3 = Mth.intFloorDiv(var1 - var0, this.cellHeight);
        return var3 <= 0
            ? param3.getMinBuildHeight()
            : this.iterateNoiseColumn(param0, param1, null, param2.isOpaque(), var2, var3).orElse(param3.getMinBuildHeight());
    }

    @Override
    public NoiseColumn getBaseColumn(int param0, int param1, LevelHeightAccessor param2) {
        int var0 = Math.max(this.settings.get().noiseSettings().minY(), param2.getMinBuildHeight());
        int var1 = Math.min(this.settings.get().noiseSettings().minY() + this.settings.get().noiseSettings().height(), param2.getMaxBuildHeight());
        int var2 = Mth.intFloorDiv(var0, this.cellHeight);
        int var3 = Mth.intFloorDiv(var1 - var0, this.cellHeight);
        if (var3 <= 0) {
            return new NoiseColumn(var0, EMPTY_COLUMN);
        } else {
            BlockState[] var4 = new BlockState[var3 * this.cellHeight];
            this.iterateNoiseColumn(param0, param1, var4, null, var2, var3);
            return new NoiseColumn(var0, var4);
        }
    }

    private OptionalInt iterateNoiseColumn(
        int param0, int param1, @Nullable BlockState[] param2, @Nullable Predicate<BlockState> param3, int param4, int param5
    ) {
        int var0 = Math.floorDiv(param0, this.cellWidth);
        int var1 = Math.floorDiv(param1, this.cellWidth);
        int var2 = Math.floorMod(param0, this.cellWidth);
        int var3 = Math.floorMod(param1, this.cellWidth);
        double var4 = (double)var2 / (double)this.cellWidth;
        double var5 = (double)var3 / (double)this.cellWidth;
        double[][] var6 = new double[][]{
            this.makeAndFillNoiseColumn(var0, var1, param4, param5),
            this.makeAndFillNoiseColumn(var0, var1 + 1, param4, param5),
            this.makeAndFillNoiseColumn(var0 + 1, var1, param4, param5),
            this.makeAndFillNoiseColumn(var0 + 1, var1 + 1, param4, param5)
        };

        for(int var7 = param5 - 1; var7 >= 0; --var7) {
            double var8 = var6[0][var7];
            double var9 = var6[1][var7];
            double var10 = var6[2][var7];
            double var11 = var6[3][var7];
            double var12 = var6[0][var7 + 1];
            double var13 = var6[1][var7 + 1];
            double var14 = var6[2][var7 + 1];
            double var15 = var6[3][var7 + 1];

            for(int var16 = this.cellHeight - 1; var16 >= 0; --var16) {
                double var17 = (double)var16 / (double)this.cellHeight;
                double var18 = Mth.lerp3(var17, var4, var5, var8, var12, var10, var14, var9, var13, var11, var15);
                int var19 = var7 * this.cellHeight + var16;
                int var20 = var19 + param4 * this.cellHeight;
                BlockState var21 = this.updateNoiseAndGenerateBaseState(Beardifier.NO_BEARDS, param0, var20, param1, var18);
                if (param2 != null) {
                    param2[var19] = var21;
                }

                if (param3 != null && param3.test(var21)) {
                    return OptionalInt.of(var20 + 1);
                }
            }
        }

        return OptionalInt.empty();
    }

    protected BlockState updateNoiseAndGenerateBaseState(Beardifier param0, int param1, int param2, int param3, double param4) {
        double var0 = Mth.clamp(param4 / 200.0, -1.0, 1.0);
        var0 = var0 / 2.0 - var0 * var0 * var0 / 24.0;
        var0 += param0.beardify(param1, param2, param3);
        BlockState var1;
        if (var0 > 0.0) {
            var1 = this.defaultBlock;
        } else if (param2 < this.getSeaLevel()) {
            var1 = this.defaultFluid;
        } else {
            var1 = AIR;
        }

        return var1;
    }

    @Override
    public void buildSurfaceAndBedrock(WorldGenRegion param0, ChunkAccess param1) {
        ChunkPos var0 = param1.getPos();
        int var1 = var0.x;
        int var2 = var0.z;
        WorldgenRandom var3 = new WorldgenRandom();
        var3.setBaseChunkSeed(var1, var2);
        ChunkPos var4 = param1.getPos();
        int var5 = var4.getMinBlockX();
        int var6 = var4.getMinBlockZ();
        double var7 = 0.0625;
        BlockPos.MutableBlockPos var8 = new BlockPos.MutableBlockPos();

        for(int var9 = 0; var9 < 16; ++var9) {
            for(int var10 = 0; var10 < 16; ++var10) {
                int var11 = var5 + var9;
                int var12 = var6 + var10;
                int var13 = param1.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var9, var10) + 1;
                double var14 = this.surfaceNoise.getSurfaceNoiseValue((double)var11 * 0.0625, (double)var12 * 0.0625, 0.0625, (double)var9 * 0.0625) * 15.0;
                param0.getBiome(var8.set(var5 + var9, var13, var6 + var10))
                    .buildSurfaceAt(var3, param1, var11, var12, var13, var14, this.defaultBlock, this.defaultFluid, this.getSeaLevel(), param0.getSeed());
            }
        }

        this.setBedrock(param1, var3);
    }

    private void setBedrock(ChunkAccess param0, Random param1) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        int var1 = param0.getPos().getMinBlockX();
        int var2 = param0.getPos().getMinBlockZ();
        NoiseGeneratorSettings var3 = this.settings.get();
        int var4 = var3.getBedrockFloorPosition();
        int var5 = this.height - 1 - var3.getBedrockRoofPosition();
        int var6 = 5;
        boolean var7 = var5 + 5 - 1 >= param0.getMinBuildHeight() && var5 < param0.getMaxBuildHeight();
        boolean var8 = var4 + 5 - 1 >= param0.getMinBuildHeight() && var4 < param0.getMaxBuildHeight();
        if (var7 || var8) {
            for(BlockPos var9 : BlockPos.betweenClosed(var1, 0, var2, var1 + 15, 0, var2 + 15)) {
                if (var7) {
                    for(int var10 = 0; var10 < 5; ++var10) {
                        if (var10 <= param1.nextInt(5)) {
                            param0.setBlockState(var0.set(var9.getX(), var5 - var10, var9.getZ()), Blocks.BEDROCK.defaultBlockState(), false);
                        }
                    }
                }

                if (var8) {
                    for(int var11 = 4; var11 >= 0; --var11) {
                        if (var11 <= param1.nextInt(5)) {
                            param0.setBlockState(var0.set(var9.getX(), var4 + var11, var9.getZ()), Blocks.BEDROCK.defaultBlockState(), false);
                        }
                    }
                }
            }

        }
    }

    @Override
    public void fillFromNoise(LevelAccessor param0, StructureFeatureManager param1, ChunkAccess param2) {
        ChunkPos var0 = param2.getPos();
        ProtoChunk var1 = (ProtoChunk)param2;
        Heightmap var2 = var1.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap var3 = var1.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        int var4 = Math.max(this.settings.get().noiseSettings().minY(), param2.getMinBuildHeight());
        int var5 = Math.min(this.settings.get().noiseSettings().minY() + this.settings.get().noiseSettings().height(), param2.getMaxBuildHeight());
        int var6 = Mth.intFloorDiv(var4, this.cellHeight);
        int var7 = Mth.intFloorDiv(var5 - var4, this.cellHeight);
        if (var7 > 0) {
            int var8 = var0.x;
            int var9 = var0.z;
            int var10 = var0.getMinBlockX();
            int var11 = var0.getMinBlockZ();
            Beardifier var12 = new Beardifier(param1, param2);
            double[][][] var13 = new double[2][this.cellCountZ + 1][var7 + 1];
            NoiseSettings var14 = this.settings.get().noiseSettings();

            for(int var15 = 0; var15 < this.cellCountZ + 1; ++var15) {
                var13[0][var15] = new double[var7 + 1];
                double[] var16 = var13[0][var15];
                int var17 = var8 * this.cellCountX;
                int var18 = var9 * this.cellCountZ + var15;
                this.sampler.fillNoiseColumn(var16, var17, var18, var14, this.getSeaLevel(), var6, var7);
                var13[1][var15] = new double[var7 + 1];
            }

            BlockPos.MutableBlockPos var19 = new BlockPos.MutableBlockPos();

            for(int var20 = 0; var20 < this.cellCountX; ++var20) {
                int var21 = var8 * this.cellCountX + var20 + 1;

                for(int var22 = 0; var22 < this.cellCountZ + 1; ++var22) {
                    double[] var23 = var13[1][var22];
                    int var24 = var9 * this.cellCountZ + var22;
                    this.sampler.fillNoiseColumn(var23, var21, var24, var14, this.getSeaLevel(), var6, var7);
                }

                for(int var25 = 0; var25 < this.cellCountZ; ++var25) {
                    LevelChunkSection var26 = var1.getOrCreateSection(var1.getSectionsCount() - 1);
                    var26.acquire();

                    for(int var27 = var7 - 1; var27 >= 0; --var27) {
                        double var28 = var13[0][var25][var27];
                        double var29 = var13[0][var25 + 1][var27];
                        double var30 = var13[1][var25][var27];
                        double var31 = var13[1][var25 + 1][var27];
                        double var32 = var13[0][var25][var27 + 1];
                        double var33 = var13[0][var25 + 1][var27 + 1];
                        double var34 = var13[1][var25][var27 + 1];
                        double var35 = var13[1][var25 + 1][var27 + 1];

                        for(int var36 = this.cellHeight - 1; var36 >= 0; --var36) {
                            int var37 = var27 * this.cellHeight + var36 + this.settings.get().noiseSettings().minY();
                            int var38 = var37 & 15;
                            int var39 = var1.getSectionIndex(var37);
                            if (var1.getSectionIndex(var26.bottomBlockY()) != var39) {
                                var26.release();
                                var26 = var1.getOrCreateSection(var39);
                                var26.acquire();
                            }

                            double var40 = (double)var36 / (double)this.cellHeight;
                            double var41 = Mth.lerp(var40, var28, var32);
                            double var42 = Mth.lerp(var40, var30, var34);
                            double var43 = Mth.lerp(var40, var29, var33);
                            double var44 = Mth.lerp(var40, var31, var35);

                            for(int var45 = 0; var45 < this.cellWidth; ++var45) {
                                int var46 = var10 + var20 * this.cellWidth + var45;
                                int var47 = var46 & 15;
                                double var48 = (double)var45 / (double)this.cellWidth;
                                double var49 = Mth.lerp(var48, var41, var42);
                                double var50 = Mth.lerp(var48, var43, var44);

                                for(int var51 = 0; var51 < this.cellWidth; ++var51) {
                                    int var52 = var11 + var25 * this.cellWidth + var51;
                                    int var53 = var52 & 15;
                                    double var54 = (double)var51 / (double)this.cellWidth;
                                    double var55 = Mth.lerp(var54, var49, var50);
                                    BlockState var56 = this.updateNoiseAndGenerateBaseState(var12, var46, var37, var52, var55);
                                    if (var56 != AIR) {
                                        if (var56.getLightEmission() != 0) {
                                            var19.set(var46, var37, var52);
                                            var1.addLight(var19);
                                        }

                                        var26.setBlockState(var47, var38, var53, var56, false);
                                        var2.update(var47, var37, var53, var56);
                                        var3.update(var47, var37, var53, var56);
                                    }
                                }
                            }
                        }
                    }

                    var26.release();
                }

                this.swapFirstTwoElements(var13);
            }

        }
    }

    public <T> void swapFirstTwoElements(T[] param0) {
        T var0 = param0[0];
        param0[0] = param0[1];
        param0[1] = var0;
    }

    @Override
    public int getGenDepth() {
        return this.height;
    }

    @Override
    public int getSeaLevel() {
        return this.settings.get().seaLevel();
    }

    @Override
    public List<MobSpawnSettings.SpawnerData> getMobsAt(Biome param0, StructureFeatureManager param1, MobCategory param2, BlockPos param3) {
        if (param1.getStructureAt(param3, true, StructureFeature.SWAMP_HUT).isValid()) {
            if (param2 == MobCategory.MONSTER) {
                return StructureFeature.SWAMP_HUT.getSpecialEnemies();
            }

            if (param2 == MobCategory.CREATURE) {
                return StructureFeature.SWAMP_HUT.getSpecialAnimals();
            }
        }

        if (param2 == MobCategory.MONSTER) {
            if (param1.getStructureAt(param3, false, StructureFeature.PILLAGER_OUTPOST).isValid()) {
                return StructureFeature.PILLAGER_OUTPOST.getSpecialEnemies();
            }

            if (param1.getStructureAt(param3, false, StructureFeature.OCEAN_MONUMENT).isValid()) {
                return StructureFeature.OCEAN_MONUMENT.getSpecialEnemies();
            }

            if (param1.getStructureAt(param3, true, StructureFeature.NETHER_BRIDGE).isValid()) {
                return StructureFeature.NETHER_BRIDGE.getSpecialEnemies();
            }
        }

        return super.getMobsAt(param0, param1, param2, param3);
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion param0) {
        if (!this.settings.get().disableMobGeneration()) {
            ChunkPos var0 = param0.getCenter();
            Biome var1 = param0.getBiome(var0.getWorldPosition());
            WorldgenRandom var2 = new WorldgenRandom();
            var2.setDecorationSeed(param0.getSeed(), var0.getMinBlockX(), var0.getMinBlockZ());
            NaturalSpawner.spawnMobsForChunkGeneration(param0, var1, var0, var2);
        }
    }
}
