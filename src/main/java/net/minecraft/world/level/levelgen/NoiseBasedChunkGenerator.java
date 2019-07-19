package net.minecraft.world.level.levelgen;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
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
import net.minecraft.world.level.levelgen.structure.StructureStart;
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
        this.minLimitPerlinNoise = new PerlinNoise(this.random, 16);
        this.maxLimitPerlinNoise = new PerlinNoise(this.random, 16);
        this.mainPerlinNoise = new PerlinNoise(this.random, 8);
        this.surfaceNoise = (SurfaceNoise)(param6 ? new PerlinSimplexNoise(this.random, 4) : new PerlinNoise(this.random, 4));
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
            var0 += this.minLimitPerlinNoise.getOctaveNoise(var4).noise(var5, var6, var7, var8, (double)param1 * var8) / var3;
            var1 += this.maxLimitPerlinNoise.getOctaveNoise(var4).noise(var5, var6, var7, var8, (double)param1 * var8) / var3;
            if (var4 < 8) {
                var2 += this.mainPerlinNoise
                        .getOctaveNoise(var4)
                        .noise(
                            PerlinNoise.wrap((double)param0 * param5 * var3),
                            PerlinNoise.wrap((double)param1 * param6 * var3),
                            PerlinNoise.wrap((double)param2 * param5 * var3),
                            param6 * var3,
                            (double)param1 * param6 * var3
                        )
                    / var3;
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
        int var7 = this.getSeaLevel();

        for(int var8 = this.chunkCountY - 1; var8 >= 0; --var8) {
            double var9 = var6[0][var8];
            double var10 = var6[1][var8];
            double var11 = var6[2][var8];
            double var12 = var6[3][var8];
            double var13 = var6[0][var8 + 1];
            double var14 = var6[1][var8 + 1];
            double var15 = var6[2][var8 + 1];
            double var16 = var6[3][var8 + 1];

            for(int var17 = this.chunkHeight - 1; var17 >= 0; --var17) {
                double var18 = (double)var17 / (double)this.chunkHeight;
                double var19 = Mth.lerp3(var18, var4, var5, var9, var13, var11, var15, var10, var14, var12, var16);
                int var20 = var8 * this.chunkHeight + var17;
                if (var19 > 0.0 || var20 < var7) {
                    BlockState var21;
                    if (var19 > 0.0) {
                        var21 = this.defaultBlock;
                    } else {
                        var21 = this.defaultFluid;
                    }

                    if (param2.isOpaque().test(var21)) {
                        return var20 + 1;
                    }
                }
            }
        }

        return 0;
    }

    protected abstract void fillNoiseColumn(double[] var1, int var2, int var3);

    public int getNoiseSizeY() {
        return this.chunkCountY + 1;
    }

    @Override
    public void buildSurfaceAndBedrock(ChunkAccess param0) {
        ChunkPos var0 = param0.getPos();
        int var1 = var0.x;
        int var2 = var0.z;
        WorldgenRandom var3 = new WorldgenRandom();
        var3.setBaseChunkSeed(var1, var2);
        ChunkPos var4 = param0.getPos();
        int var5 = var4.getMinBlockX();
        int var6 = var4.getMinBlockZ();
        double var7 = 0.0625;
        Biome[] var8 = param0.getBiomes();

        for(int var9 = 0; var9 < 16; ++var9) {
            for(int var10 = 0; var10 < 16; ++var10) {
                int var11 = var5 + var9;
                int var12 = var6 + var10;
                int var13 = param0.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var9, var10) + 1;
                double var14 = this.surfaceNoise.getSurfaceNoiseValue((double)var11 * 0.0625, (double)var12 * 0.0625, 0.0625, (double)var9 * 0.0625);
                var8[var10 * 16 + var9]
                    .buildSurfaceAt(
                        var3,
                        param0,
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

        this.setBedrock(param0, var3);
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
    public void fillFromNoise(LevelAccessor param0, ChunkAccess param1) {
        int var0 = this.getSeaLevel();
        ObjectList<PoolElementStructurePiece> var1 = new ObjectArrayList<>(10);
        ObjectList<JigsawJunction> var2 = new ObjectArrayList<>(32);
        ChunkPos var3 = param1.getPos();
        int var4 = var3.x;
        int var5 = var3.z;
        int var6 = var4 << 4;
        int var7 = var5 << 4;

        for(StructureFeature<?> var8 : Feature.NOISE_AFFECTING_FEATURES) {
            String var9 = var8.getFeatureName();
            LongIterator var10 = param1.getReferencesForFeature(var9).iterator();

            while(var10.hasNext()) {
                long var11 = var10.nextLong();
                ChunkPos var12 = new ChunkPos(var11);
                ChunkAccess var13 = param0.getChunk(var12.x, var12.z);
                StructureStart var14 = var13.getStartForFeature(var9);
                if (var14 != null && var14.isValid()) {
                    for(StructurePiece var15 : var14.getPieces()) {
                        if (var15.isCloseToChunk(var3, 12) && var15 instanceof PoolElementStructurePiece) {
                            PoolElementStructurePiece var16 = (PoolElementStructurePiece)var15;
                            StructureTemplatePool.Projection var17 = var16.getElement().getProjection();
                            if (var17 == StructureTemplatePool.Projection.RIGID) {
                                var1.add(var16);
                            }

                            for(JigsawJunction var18 : var16.getJunctions()) {
                                int var19 = var18.getSourceX();
                                int var20 = var18.getSourceZ();
                                if (var19 > var6 - 12 && var20 > var7 - 12 && var19 < var6 + 15 + 12 && var20 < var7 + 15 + 12) {
                                    var2.add(var18);
                                }
                            }
                        }
                    }
                }
            }
        }

        double[][][] var21 = new double[2][this.chunkCountZ + 1][this.chunkCountY + 1];

        for(int var22 = 0; var22 < this.chunkCountZ + 1; ++var22) {
            var21[0][var22] = new double[this.chunkCountY + 1];
            this.fillNoiseColumn(var21[0][var22], var4 * this.chunkCountX, var5 * this.chunkCountZ + var22);
            var21[1][var22] = new double[this.chunkCountY + 1];
        }

        ProtoChunk var23 = (ProtoChunk)param1;
        Heightmap var24 = var23.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap var25 = var23.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        BlockPos.MutableBlockPos var26 = new BlockPos.MutableBlockPos();
        ObjectListIterator<PoolElementStructurePiece> var27 = var1.iterator();
        ObjectListIterator<JigsawJunction> var28 = var2.iterator();

        for(int var29 = 0; var29 < this.chunkCountX; ++var29) {
            for(int var30 = 0; var30 < this.chunkCountZ + 1; ++var30) {
                this.fillNoiseColumn(var21[1][var30], var4 * this.chunkCountX + var29 + 1, var5 * this.chunkCountZ + var30);
            }

            for(int var31 = 0; var31 < this.chunkCountZ; ++var31) {
                LevelChunkSection var32 = var23.getOrCreateSection(15);
                var32.acquire();

                for(int var33 = this.chunkCountY - 1; var33 >= 0; --var33) {
                    double var34 = var21[0][var31][var33];
                    double var35 = var21[0][var31 + 1][var33];
                    double var36 = var21[1][var31][var33];
                    double var37 = var21[1][var31 + 1][var33];
                    double var38 = var21[0][var31][var33 + 1];
                    double var39 = var21[0][var31 + 1][var33 + 1];
                    double var40 = var21[1][var31][var33 + 1];
                    double var41 = var21[1][var31 + 1][var33 + 1];

                    for(int var42 = this.chunkHeight - 1; var42 >= 0; --var42) {
                        int var43 = var33 * this.chunkHeight + var42;
                        int var44 = var43 & 15;
                        int var45 = var43 >> 4;
                        if (var32.bottomBlockY() >> 4 != var45) {
                            var32.release();
                            var32 = var23.getOrCreateSection(var45);
                            var32.acquire();
                        }

                        double var46 = (double)var42 / (double)this.chunkHeight;
                        double var47 = Mth.lerp(var46, var34, var38);
                        double var48 = Mth.lerp(var46, var36, var40);
                        double var49 = Mth.lerp(var46, var35, var39);
                        double var50 = Mth.lerp(var46, var37, var41);

                        for(int var51 = 0; var51 < this.chunkWidth; ++var51) {
                            int var52 = var6 + var29 * this.chunkWidth + var51;
                            int var53 = var52 & 15;
                            double var54 = (double)var51 / (double)this.chunkWidth;
                            double var55 = Mth.lerp(var54, var47, var48);
                            double var56 = Mth.lerp(var54, var49, var50);

                            for(int var57 = 0; var57 < this.chunkWidth; ++var57) {
                                int var58 = var7 + var31 * this.chunkWidth + var57;
                                int var59 = var58 & 15;
                                double var60 = (double)var57 / (double)this.chunkWidth;
                                double var61 = Mth.lerp(var60, var55, var56);
                                double var62 = Mth.clamp(var61 / 200.0, -1.0, 1.0);

                                int var65;
                                int var66;
                                int var67;
                                for(var62 = var62 / 2.0 - var62 * var62 * var62 / 24.0; var27.hasNext(); var62 += getContribution(var65, var66, var67) * 0.8) {
                                    PoolElementStructurePiece var63 = var27.next();
                                    BoundingBox var64 = var63.getBoundingBox();
                                    var65 = Math.max(0, Math.max(var64.x0 - var52, var52 - var64.x1));
                                    var66 = var43 - (var64.y0 + var63.getGroundLevelDelta());
                                    var67 = Math.max(0, Math.max(var64.z0 - var58, var58 - var64.z1));
                                }

                                var27.back(var1.size());

                                while(var28.hasNext()) {
                                    JigsawJunction var68 = var28.next();
                                    int var69 = var52 - var68.getSourceX();
                                    var65 = var43 - var68.getSourceGroundY();
                                    var66 = var58 - var68.getSourceZ();
                                    var62 += getContribution(var69, var65, var66) * 0.4;
                                }

                                var28.back(var2.size());
                                BlockState var72;
                                if (var62 > 0.0) {
                                    var72 = this.defaultBlock;
                                } else if (var43 < var0) {
                                    var72 = this.defaultFluid;
                                } else {
                                    var72 = AIR;
                                }

                                if (var72 != AIR) {
                                    if (var72.getLightEmission() != 0) {
                                        var26.set(var52, var43, var58);
                                        var23.addLight(var26);
                                    }

                                    var32.setBlockState(var53, var44, var59, var72, false);
                                    var24.update(var53, var43, var59, var72);
                                    var25.update(var53, var43, var59, var72);
                                }
                            }
                        }
                    }
                }

                var32.release();
            }

            double[][] var75 = var21[0];
            var21[0] = var21[1];
            var21[1] = var75;
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
