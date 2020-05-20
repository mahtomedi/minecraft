package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.JigsawJunction;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
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
    private static final float[] BEARD_KERNEL = Util.make(new float[13824], param0 -> {
        for(int var0 = 0; var0 < 24; ++var0) {
            for(int var1 = 0; var1 < 24; ++var1) {
                for(int var2 = 0; var2 < 24; ++var2) {
                    param0[var0 * 24 * 24 + var1 * 24 + var2] = (float)computeContribution(var1 - 12, var2 - 12, var0 - 12);
                }
            }
        }

    });
    private static final float[] BIOME_WEIGHTS = Util.make(new float[25], param0 -> {
        for(int var0 = -2; var0 <= 2; ++var0) {
            for(int var1 = -2; var1 <= 2; ++var1) {
                float var2 = 10.0F / Mth.sqrt((float)(var0 * var0 + var1 * var1) + 0.2F);
                param0[var0 + 2 + (var1 + 2) * 5] = var2;
            }
        }

    });
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private final int chunkHeight;
    private final int chunkWidth;
    private final int chunkCountX;
    private final int chunkCountY;
    private final int chunkCountZ;
    protected final WorldgenRandom random;
    private final PerlinNoise minLimitPerlinNoise;
    private final PerlinNoise maxLimitPerlinNoise;
    private final PerlinNoise mainPerlinNoise;
    private final SurfaceNoise surfaceNoise;
    private final PerlinNoise depthNoise;
    @Nullable
    private final SimplexNoise islandNoise;
    protected final BlockState defaultBlock;
    protected final BlockState defaultFluid;
    private final long seed;
    protected final NoiseGeneratorSettings settings;
    private final int height;

    public NoiseBasedChunkGenerator(BiomeSource param0, long param1, NoiseGeneratorSettings param2) {
        this(param0, param0, param1, param2);
    }

    private NoiseBasedChunkGenerator(BiomeSource param0, BiomeSource param1, long param2, NoiseGeneratorSettings param3) {
        super(param0, param1, param3.structureSettings(), param2);
        this.seed = param2;
        this.settings = param3;
        NoiseSettings var0 = param3.noiseSettings();
        this.height = var0.height();
        this.chunkHeight = var0.noiseSizeVertical() * 4;
        this.chunkWidth = var0.noiseSizeHorizontal() * 4;
        this.defaultBlock = param3.getDefaultBlock();
        this.defaultFluid = param3.getDefaultFluid();
        this.chunkCountX = 16 / this.chunkWidth;
        this.chunkCountY = var0.height() / this.chunkHeight;
        this.chunkCountZ = 16 / this.chunkWidth;
        this.random = new WorldgenRandom(param2);
        this.minLimitPerlinNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-15, 0));
        this.maxLimitPerlinNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-15, 0));
        this.mainPerlinNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-7, 0));
        this.surfaceNoise = (SurfaceNoise)(var0.useSimplexSurfaceNoise()
            ? new PerlinSimplexNoise(this.random, IntStream.rangeClosed(-3, 0))
            : new PerlinNoise(this.random, IntStream.rangeClosed(-3, 0)));
        this.random.consumeCount(2620);
        this.depthNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-15, 0));
        if (var0.islandNoiseOverride()) {
            WorldgenRandom var1 = new WorldgenRandom(param2);
            var1.consumeCount(17292);
            this.islandNoise = new SimplexNoise(var1);
        } else {
            this.islandNoise = null;
        }

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

    public boolean stable(long param0, NoiseGeneratorSettings.Preset param1) {
        return this.seed == param0 && this.settings.stable(param1);
    }

    private double sampleAndClampNoise(int param0, int param1, int param2, double param3, double param4, double param5, double param6) {
        double var0 = 0.0;
        double var1 = 0.0;
        double var2 = 0.0;
        boolean var3 = true;
        double var4 = 1.0;

        for(int var5 = 0; var5 < 16; ++var5) {
            double var6 = PerlinNoise.wrap((double)param0 * param3 * var4);
            double var7 = PerlinNoise.wrap((double)param1 * param4 * var4);
            double var8 = PerlinNoise.wrap((double)param2 * param3 * var4);
            double var9 = param4 * var4;
            ImprovedNoise var10 = this.minLimitPerlinNoise.getOctaveNoise(var5);
            if (var10 != null) {
                var0 += var10.noise(var6, var7, var8, var9, (double)param1 * var9) / var4;
            }

            ImprovedNoise var11 = this.maxLimitPerlinNoise.getOctaveNoise(var5);
            if (var11 != null) {
                var1 += var11.noise(var6, var7, var8, var9, (double)param1 * var9) / var4;
            }

            if (var5 < 8) {
                ImprovedNoise var12 = this.mainPerlinNoise.getOctaveNoise(var5);
                if (var12 != null) {
                    var2 += var12.noise(
                            PerlinNoise.wrap((double)param0 * param5 * var4),
                            PerlinNoise.wrap((double)param1 * param6 * var4),
                            PerlinNoise.wrap((double)param2 * param5 * var4),
                            param6 * var4,
                            (double)param1 * param6 * var4
                        )
                        / var4;
                }
            }

            var4 /= 2.0;
        }

        return Mth.clampedLerp(var0 / 512.0, var1 / 512.0, (var2 / 10.0 + 1.0) / 2.0);
    }

    private double[] makeAndFillNoiseColumn(int param0, int param1) {
        double[] var0 = new double[this.chunkCountY + 1];
        this.fillNoiseColumn(var0, param0, param1);
        return var0;
    }

    private void fillNoiseColumn(double[] param0, int param1, int param2) {
        NoiseSettings var0 = this.settings.noiseSettings();
        double var1;
        double var2;
        if (this.islandNoise != null) {
            var1 = (double)(TheEndBiomeSource.getHeightValue(this.islandNoise, param1, param2) - 8.0F);
            if (var1 > 0.0) {
                var2 = 0.25;
            } else {
                var2 = 1.0;
            }
        } else {
            float var4 = 0.0F;
            float var5 = 0.0F;
            float var6 = 0.0F;
            int var7 = 2;
            int var8 = this.getSeaLevel();
            float var9 = this.biomeSource.getNoiseBiome(param1, var8, param2).getDepth();

            for(int var10 = -2; var10 <= 2; ++var10) {
                for(int var11 = -2; var11 <= 2; ++var11) {
                    Biome var12 = this.biomeSource.getNoiseBiome(param1 + var10, var8, param2 + var11);
                    float var13 = var12.getDepth();
                    float var14 = var12.getScale();
                    float var15;
                    float var16;
                    if (var0.isAmplified() && var13 > 0.0F) {
                        var15 = 1.0F + var13 * 2.0F;
                        var16 = 1.0F + var14 * 4.0F;
                    } else {
                        var15 = var13;
                        var16 = var14;
                    }

                    float var19 = var13 > var9 ? 0.5F : 1.0F;
                    float var20 = var19 * BIOME_WEIGHTS[var10 + 2 + (var11 + 2) * 5] / (var15 + 2.0F);
                    var4 += var16 * var20;
                    var5 += var15 * var20;
                    var6 += var20;
                }
            }

            float var21 = var5 / var6;
            float var22 = var4 / var6;
            double var23 = (double)(var21 * 0.5F - 0.125F);
            double var24 = (double)(var22 * 0.9F + 0.1F);
            var1 = var23 * 0.265625;
            var2 = 96.0 / var24;
        }

        double var27 = 684.412 * var0.noiseSamplingSettings().xzScale();
        double var28 = 684.412 * var0.noiseSamplingSettings().yScale();
        double var29 = var27 / var0.noiseSamplingSettings().xzFactor();
        double var30 = var28 / var0.noiseSamplingSettings().yFactor();
        double var31 = (double)var0.topSlideSettings().target();
        double var32 = (double)var0.topSlideSettings().size();
        double var33 = (double)var0.topSlideSettings().offset();
        double var34 = (double)var0.bottomSlideSettings().target();
        double var35 = (double)var0.bottomSlideSettings().size();
        double var36 = (double)var0.bottomSlideSettings().offset();
        double var37 = var0.randomDensityOffset() ? this.getRandomDensity(param1, param2) : 0.0;
        double var38 = var0.densityFactor();
        double var39 = var0.densityOffset();

        for(int var40 = 0; var40 <= this.chunkCountY; ++var40) {
            double var41 = this.sampleAndClampNoise(param1, var40, param2, var27, var28, var29, var30);
            double var42 = 1.0 - (double)var40 * 2.0 / (double)this.chunkCountY + var37;
            double var43 = var42 * var38 + var39;
            double var44 = (var43 + var1) * var2;
            if (var44 > 0.0) {
                var41 += var44 * 4.0;
            } else {
                var41 += var44;
            }

            if (var32 > 0.0) {
                double var45 = ((double)(this.chunkCountY - var40) - var33) / var32;
                var41 = Mth.clampedLerp(var31, var41, var45);
            }

            if (var35 > 0.0) {
                double var46 = ((double)var40 - var36) / var35;
                var41 = Mth.clampedLerp(var34, var41, var46);
            }

            param0[var40] = var41;
        }

    }

    private double getRandomDensity(int param0, int param1) {
        double var0 = this.depthNoise.getValue((double)(param0 * 200), 10.0, (double)(param1 * 200), 1.0, 0.0, true);
        double var1;
        if (var0 < 0.0) {
            var1 = -var0 * 0.3;
        } else {
            var1 = var0;
        }

        double var3 = var1 * 24.575625 - 2.0;
        return var3 < 0.0 ? var3 * 0.009486607142857142 : Math.min(var3, 1.0) * 0.006640625;
    }

    @Override
    public int getBaseHeight(int param0, int param1, Heightmap.Types param2) {
        return this.iterateNoiseColumn(param0, param1, null, param2.isOpaque());
    }

    @Override
    public BlockGetter getBaseColumn(int param0, int param1) {
        BlockState[] var0 = new BlockState[this.chunkCountY * this.chunkHeight];
        this.iterateNoiseColumn(param0, param1, var0, null);
        return new NoiseColumn(var0);
    }

    private int iterateNoiseColumn(int param0, int param1, @Nullable BlockState[] param2, @Nullable Predicate<BlockState> param3) {
        int var0 = Math.floorDiv(param0, this.chunkWidth);
        int var1 = Math.floorDiv(param1, this.chunkWidth);
        int var2 = Math.floorMod(param0, this.chunkWidth);
        int var3 = Math.floorMod(param1, this.chunkWidth);
        double var4 = (double)var2 / (double)this.chunkWidth;
        double var5 = (double)var3 / (double)this.chunkWidth;
        double[][] var6 = new double[][]{
            this.makeAndFillNoiseColumn(var0, var1),
            this.makeAndFillNoiseColumn(var0, var1 + 1),
            this.makeAndFillNoiseColumn(var0 + 1, var1),
            this.makeAndFillNoiseColumn(var0 + 1, var1 + 1)
        };

        for(int var7 = this.chunkCountY - 1; var7 >= 0; --var7) {
            double var8 = var6[0][var7];
            double var9 = var6[1][var7];
            double var10 = var6[2][var7];
            double var11 = var6[3][var7];
            double var12 = var6[0][var7 + 1];
            double var13 = var6[1][var7 + 1];
            double var14 = var6[2][var7 + 1];
            double var15 = var6[3][var7 + 1];

            for(int var16 = this.chunkHeight - 1; var16 >= 0; --var16) {
                double var17 = (double)var16 / (double)this.chunkHeight;
                double var18 = Mth.lerp3(var17, var4, var5, var8, var12, var10, var14, var9, var13, var11, var15);
                int var19 = var7 * this.chunkHeight + var16;
                BlockState var20 = this.generateBaseState(var18, var19);
                if (param2 != null) {
                    param2[var19] = var20;
                }

                if (param3 != null && param3.test(var20)) {
                    return var19 + 1;
                }
            }
        }

        return 0;
    }

    protected BlockState generateBaseState(double param0, int param1) {
        BlockState var0;
        if (param0 > 0.0) {
            var0 = this.defaultBlock;
        } else if (param1 < this.getSeaLevel()) {
            var0 = this.defaultFluid;
        } else {
            var0 = AIR;
        }

        return var0;
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
        int var3 = this.settings.getBedrockFloorPosition();
        int var4 = this.height - 1 - this.settings.getBedrockRoofPosition();
        int var5 = 5;
        boolean var6 = var4 + 4 >= 0 && var4 < this.height;
        boolean var7 = var3 + 4 >= 0 && var3 < this.height;
        if (var6 || var7) {
            for(BlockPos var8 : BlockPos.betweenClosed(var1, 0, var2, var1 + 15, 0, var2 + 15)) {
                if (var6) {
                    for(int var9 = 0; var9 < 5; ++var9) {
                        if (var9 <= param1.nextInt(5)) {
                            param0.setBlockState(var0.set(var8.getX(), var4 - var9, var8.getZ()), Blocks.BEDROCK.defaultBlockState(), false);
                        }
                    }
                }

                if (var7) {
                    for(int var10 = 4; var10 >= 0; --var10) {
                        if (var10 <= param1.nextInt(5)) {
                            param0.setBlockState(var0.set(var8.getX(), var3 + var10, var8.getZ()), Blocks.BEDROCK.defaultBlockState(), false);
                        }
                    }
                }
            }

        }
    }

    @Override
    public void fillFromNoise(LevelAccessor param0, StructureFeatureManager param1, ChunkAccess param2) {
        ObjectList<StructurePiece> var0 = new ObjectArrayList<>(10);
        ObjectList<JigsawJunction> var1 = new ObjectArrayList<>(32);
        ChunkPos var2 = param2.getPos();
        int var3 = var2.x;
        int var4 = var2.z;
        int var5 = var3 << 4;
        int var6 = var4 << 4;

        for(StructureFeature<?> var7 : StructureFeature.NOISE_AFFECTING_FEATURES) {
            param1.startsForFeature(SectionPos.of(var2, 0), var7).forEach(param5 -> {
                for(StructurePiece var0x : param5.getPieces()) {
                    if (var0x.isCloseToChunk(var2, 12)) {
                        if (var0x instanceof PoolElementStructurePiece) {
                            PoolElementStructurePiece var1x = (PoolElementStructurePiece)var0x;
                            StructureTemplatePool.Projection var2x = var1x.getElement().getProjection();
                            if (var2x == StructureTemplatePool.Projection.RIGID) {
                                var0.add(var1x);
                            }

                            for(JigsawJunction var3x : var1x.getJunctions()) {
                                int var4x = var3x.getSourceX();
                                int var5x = var3x.getSourceZ();
                                if (var4x > var5 - 12 && var5x > var6 - 12 && var4x < var5 + 15 + 12 && var5x < var6 + 15 + 12) {
                                    var1.add(var3x);
                                }
                            }
                        } else {
                            var0.add(var0x);
                        }
                    }
                }

            });
        }

        double[][][] var8 = new double[2][this.chunkCountZ + 1][this.chunkCountY + 1];

        for(int var9 = 0; var9 < this.chunkCountZ + 1; ++var9) {
            var8[0][var9] = new double[this.chunkCountY + 1];
            this.fillNoiseColumn(var8[0][var9], var3 * this.chunkCountX, var4 * this.chunkCountZ + var9);
            var8[1][var9] = new double[this.chunkCountY + 1];
        }

        ProtoChunk var10 = (ProtoChunk)param2;
        Heightmap var11 = var10.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap var12 = var10.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        BlockPos.MutableBlockPos var13 = new BlockPos.MutableBlockPos();
        ObjectListIterator<StructurePiece> var14 = var0.iterator();
        ObjectListIterator<JigsawJunction> var15 = var1.iterator();

        for(int var16 = 0; var16 < this.chunkCountX; ++var16) {
            for(int var17 = 0; var17 < this.chunkCountZ + 1; ++var17) {
                this.fillNoiseColumn(var8[1][var17], var3 * this.chunkCountX + var16 + 1, var4 * this.chunkCountZ + var17);
            }

            for(int var18 = 0; var18 < this.chunkCountZ; ++var18) {
                LevelChunkSection var19 = var10.getOrCreateSection(15);
                var19.acquire();

                for(int var20 = this.chunkCountY - 1; var20 >= 0; --var20) {
                    double var21 = var8[0][var18][var20];
                    double var22 = var8[0][var18 + 1][var20];
                    double var23 = var8[1][var18][var20];
                    double var24 = var8[1][var18 + 1][var20];
                    double var25 = var8[0][var18][var20 + 1];
                    double var26 = var8[0][var18 + 1][var20 + 1];
                    double var27 = var8[1][var18][var20 + 1];
                    double var28 = var8[1][var18 + 1][var20 + 1];

                    for(int var29 = this.chunkHeight - 1; var29 >= 0; --var29) {
                        int var30 = var20 * this.chunkHeight + var29;
                        int var31 = var30 & 15;
                        int var32 = var30 >> 4;
                        if (var19.bottomBlockY() >> 4 != var32) {
                            var19.release();
                            var19 = var10.getOrCreateSection(var32);
                            var19.acquire();
                        }

                        double var33 = (double)var29 / (double)this.chunkHeight;
                        double var34 = Mth.lerp(var33, var21, var25);
                        double var35 = Mth.lerp(var33, var23, var27);
                        double var36 = Mth.lerp(var33, var22, var26);
                        double var37 = Mth.lerp(var33, var24, var28);

                        for(int var38 = 0; var38 < this.chunkWidth; ++var38) {
                            int var39 = var5 + var16 * this.chunkWidth + var38;
                            int var40 = var39 & 15;
                            double var41 = (double)var38 / (double)this.chunkWidth;
                            double var42 = Mth.lerp(var41, var34, var35);
                            double var43 = Mth.lerp(var41, var36, var37);

                            for(int var44 = 0; var44 < this.chunkWidth; ++var44) {
                                int var45 = var6 + var18 * this.chunkWidth + var44;
                                int var46 = var45 & 15;
                                double var47 = (double)var44 / (double)this.chunkWidth;
                                double var48 = Mth.lerp(var47, var42, var43);
                                double var49 = Mth.clamp(var48 / 200.0, -1.0, 1.0);

                                int var52;
                                int var53;
                                int var54;
                                for(var49 = var49 / 2.0 - var49 * var49 * var49 / 24.0; var14.hasNext(); var49 += getContribution(var52, var53, var54) * 0.8) {
                                    StructurePiece var50 = var14.next();
                                    BoundingBox var51 = var50.getBoundingBox();
                                    var52 = Math.max(0, Math.max(var51.x0 - var39, var39 - var51.x1));
                                    var53 = var30
                                        - (
                                            var51.y0
                                                + (var50 instanceof PoolElementStructurePiece ? ((PoolElementStructurePiece)var50).getGroundLevelDelta() : 0)
                                        );
                                    var54 = Math.max(0, Math.max(var51.z0 - var45, var45 - var51.z1));
                                }

                                var14.back(var0.size());

                                while(var15.hasNext()) {
                                    JigsawJunction var55 = var15.next();
                                    int var56 = var39 - var55.getSourceX();
                                    var52 = var30 - var55.getSourceGroundY();
                                    var53 = var45 - var55.getSourceZ();
                                    var49 += getContribution(var56, var52, var53) * 0.4;
                                }

                                var15.back(var1.size());
                                BlockState var59 = this.generateBaseState(var49, var30);
                                if (var59 != AIR) {
                                    if (var59.getLightEmission() != 0) {
                                        var13.set(var39, var30, var45);
                                        var10.addLight(var13);
                                    }

                                    var19.setBlockState(var40, var31, var46, var59, false);
                                    var11.update(var40, var30, var46, var59);
                                    var12.update(var40, var30, var46, var59);
                                }
                            }
                        }
                    }
                }

                var19.release();
            }

            double[][] var60 = var8[0];
            var8[0] = var8[1];
            var8[1] = var60;
        }

    }

    private static double getContribution(int param0, int param1, int param2) {
        int var0 = param0 + 12;
        int var1 = param1 + 12;
        int var2 = param2 + 12;
        if (var0 < 0 || var0 >= 24) {
            return 0.0;
        } else if (var1 < 0 || var1 >= 24) {
            return 0.0;
        } else {
            return var2 >= 0 && var2 < 24 ? (double)BEARD_KERNEL[var2 * 24 * 24 + var0 * 24 + var1] : 0.0;
        }
    }

    private static double computeContribution(int param0, int param1, int param2) {
        double var0 = (double)(param0 * param0 + param2 * param2);
        double var1 = (double)param1 + 0.5;
        double var2 = var1 * var1;
        double var3 = Math.pow(Math.E, -(var2 / 16.0 + var0 / 16.0));
        double var4 = -var1 * Mth.fastInvSqrt(var2 / 2.0 + var0 / 2.0) / 2.0;
        return var4 * var3;
    }

    @Override
    public int getGenDepth() {
        return this.height;
    }

    @Override
    public int getSeaLevel() {
        return this.settings.seaLevel();
    }

    @Override
    public List<Biome.SpawnerData> getMobsAt(Biome param0, StructureFeatureManager param1, MobCategory param2, BlockPos param3) {
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
        if (!this.settings.disableMobGeneration()) {
            int var0 = param0.getCenterX();
            int var1 = param0.getCenterZ();
            Biome var2 = param0.getBiome(new ChunkPos(var0, var1).getWorldPosition());
            WorldgenRandom var3 = new WorldgenRandom();
            var3.setDecorationSeed(param0.getSeed(), var0 << 4, var1 << 4);
            NaturalSpawner.spawnMobsForChunkGeneration(param0, var2, var0, var1, var3);
        }
    }
}
