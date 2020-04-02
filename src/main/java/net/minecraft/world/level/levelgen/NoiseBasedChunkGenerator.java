package net.minecraft.world.level.levelgen;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.JigsawJunction;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.levelgen.synth.SurfaceNoise;

public abstract class NoiseBasedChunkGenerator<T extends ChunkGeneratorSettings> extends ChunkGenerator<T> {
    private static final float[] BEARD_KERNEL = Util.make(new float[13824], param0 -> {
        for(int var0 = 0; var0 < 24; ++var0) {
            for(int var1 = 0; var1 < 24; ++var1) {
                for(int var2 = 0; var2 < 24; ++var2) {
                    param0[var0 * 24 * 24 + var1 * 24 + var2] = (float)computeContribution(var1 - 12, var2 - 12, var0 - 12);
                }
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
    protected final BlockState defaultBlock;
    protected final BlockState defaultFluid;

    public NoiseBasedChunkGenerator(LevelAccessor param0, BiomeSource param1, int param2, int param3, int param4, T param5, boolean param6) {
        super(param0, param1, param5);
        this.chunkHeight = param3;
        this.chunkWidth = param2;
        this.defaultBlock = param5.getDefaultBlock();
        this.defaultFluid = param5.getDefaultFluid();
        this.chunkCountX = 16 / this.chunkWidth;
        this.chunkCountY = param4 / this.chunkHeight;
        this.chunkCountZ = 16 / this.chunkWidth;
        this.random = new WorldgenRandom(this.seed);
        this.minLimitPerlinNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-15, 0));
        this.maxLimitPerlinNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-15, 0));
        this.mainPerlinNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-7, 0));
        this.surfaceNoise = (SurfaceNoise)(param6
            ? new PerlinSimplexNoise(this.random, IntStream.rangeClosed(-3, 0))
            : new PerlinNoise(this.random, IntStream.rangeClosed(-3, 0)));
    }

    private double sampleAndClampNoise(int param0, int param1, int param2, double param3, double param4, double param5, double param6) {
        double var0 = 0.0;
        double var1 = 0.0;
        double var2 = 0.0;
        double var3 = 1.0;

        for(int var4 = 0; var4 < 16; ++var4) {
            double var5 = PerlinNoise.wrap((double)param0 * param3 * var3);
            double var6 = PerlinNoise.wrap((double)param1 * param4 * var3);
            double var7 = PerlinNoise.wrap((double)param2 * param3 * var3);
            double var8 = param4 * var3;
            ImprovedNoise var9 = this.minLimitPerlinNoise.getOctaveNoise(var4);
            if (var9 != null) {
                var0 += var9.noise(var5, var6, var7, var8, (double)param1 * var8) / var3;
            }

            ImprovedNoise var10 = this.maxLimitPerlinNoise.getOctaveNoise(var4);
            if (var10 != null) {
                var1 += var10.noise(var5, var6, var7, var8, (double)param1 * var8) / var3;
            }

            if (var4 < 8) {
                ImprovedNoise var11 = this.mainPerlinNoise.getOctaveNoise(var4);
                if (var11 != null) {
                    var2 += var11.noise(
                            PerlinNoise.wrap((double)param0 * param5 * var3),
                            PerlinNoise.wrap((double)param1 * param6 * var3),
                            PerlinNoise.wrap((double)param2 * param5 * var3),
                            param6 * var3,
                            (double)param1 * param6 * var3
                        )
                        / var3;
                }
            }

            var3 /= 2.0;
        }

        return Mth.clampedLerp(var0 / 512.0, var1 / 512.0, (var2 / 10.0 + 1.0) / 2.0);
    }

    protected double[] makeAndFillNoiseColumn(int param0, int param1) {
        double[] var0 = new double[this.chunkCountY + 1];
        this.fillNoiseColumn(var0, param0, param1);
        return var0;
    }

    protected void fillNoiseColumn(double[] param0, int param1, int param2, double param3, double param4, double param5, double param6, int param7, int param8) {
        double[] var0 = this.getDepthAndScale(param1, param2);
        double var1 = var0[0];
        double var2 = var0[1];
        double var3 = this.getTopSlideStart();
        double var4 = this.getBottomSlideStart();

        for(int var5 = 0; var5 < this.getNoiseSizeY(); ++var5) {
            double var6 = this.sampleAndClampNoise(param1, var5, param2, param3, param4, param5, param6);
            var6 -= this.getYOffset(var1, var2, var5);
            if ((double)var5 > var3) {
                var6 = Mth.clampedLerp(var6, (double)param8, ((double)var5 - var3) / (double)param7);
            } else if ((double)var5 < var4) {
                var6 = Mth.clampedLerp(var6, -30.0, (var4 - (double)var5) / (var4 - 1.0));
            }

            param0[var5] = var6;
        }

    }

    protected abstract double[] getDepthAndScale(int var1, int var2);

    protected abstract double getYOffset(double var1, double var3, int var5);

    protected double getTopSlideStart() {
        return (double)(this.getNoiseSizeY() - 4);
    }

    protected double getBottomSlideStart() {
        return 0.0;
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

    protected abstract void fillNoiseColumn(double[] var1, int var2, int var3);

    public int getNoiseSizeY() {
        return this.chunkCountY + 1;
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
                    .buildSurfaceAt(
                        var3,
                        param1,
                        var11,
                        var12,
                        var13,
                        var14,
                        this.getSettings().getDefaultBlock(),
                        this.getSettings().getDefaultFluid(),
                        this.getSeaLevel(),
                        this.level.getSeed()
                    );
            }
        }

        this.setBedrock(param1, var3);
    }

    protected void setBedrock(ChunkAccess param0, Random param1) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        int var1 = param0.getPos().getMinBlockX();
        int var2 = param0.getPos().getMinBlockZ();
        T var3 = this.getSettings();
        int var4 = var3.getBedrockFloorPosition();
        int var5 = var3.getBedrockRoofPosition();

        for(BlockPos var6 : BlockPos.betweenClosed(var1, 0, var2, var1 + 15, 0, var2 + 15)) {
            if (var5 > 0) {
                for(int var7 = var5; var7 >= var5 - 4; --var7) {
                    if (var7 >= var5 - param1.nextInt(5)) {
                        param0.setBlockState(var0.set(var6.getX(), var7, var6.getZ()), Blocks.BEDROCK.defaultBlockState(), false);
                    }
                }
            }

            if (var4 < 256) {
                for(int var8 = var4 + 4; var8 >= var4; --var8) {
                    if (var8 <= var4 + param1.nextInt(5)) {
                        param0.setBlockState(var0.set(var6.getX(), var8, var6.getZ()), Blocks.BEDROCK.defaultBlockState(), false);
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

        for(StructureFeature<?> var7 : Feature.NOISE_AFFECTING_FEATURES) {
            param1.startsForFeature(SectionPos.of(var2, 0), var7, param0).forEach(param5 -> {
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
}
