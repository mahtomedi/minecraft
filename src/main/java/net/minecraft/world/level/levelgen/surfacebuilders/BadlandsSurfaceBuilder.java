package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

public class BadlandsSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    protected static final int MAX_CLAY_DEPTH = 15;
    private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
    private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
    private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();
    private static final BlockState YELLOW_TERRACOTTA = Blocks.YELLOW_TERRACOTTA.defaultBlockState();
    private static final BlockState BROWN_TERRACOTTA = Blocks.BROWN_TERRACOTTA.defaultBlockState();
    private static final BlockState RED_TERRACOTTA = Blocks.RED_TERRACOTTA.defaultBlockState();
    private static final BlockState LIGHT_GRAY_TERRACOTTA = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
    protected BlockState[] clayBands;
    protected long seed;
    protected PerlinSimplexNoise pillarNoise;
    protected PerlinSimplexNoise pillarRoofNoise;
    protected PerlinSimplexNoise clayBandsOffsetNoise;

    public BadlandsSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> param0) {
        super(param0);
    }

    public void apply(
        Random param0,
        BlockColumn param1,
        Biome param2,
        int param3,
        int param4,
        int param5,
        double param6,
        BlockState param7,
        BlockState param8,
        int param9,
        int param10,
        long param11,
        SurfaceBuilderBaseConfiguration param12
    ) {
        BlockState var0 = WHITE_TERRACOTTA;
        SurfaceBuilderConfiguration var1 = param2.getGenerationSettings().getSurfaceBuilderConfig();
        BlockState var2 = var1.getUnderMaterial();
        BlockState var3 = var1.getTopMaterial();
        BlockState var4 = var2;
        int var5 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        boolean var6 = Math.cos(param6 / 3.0 * Math.PI) > 0.0;
        int var7 = -1;
        boolean var8 = false;
        int var9 = 0;

        for(int var10 = param5; var10 >= param10; --var10) {
            if (var9 < 15) {
                BlockState var11 = param1.getBlock(var10);
                if (var11.isAir()) {
                    var7 = -1;
                } else if (var11.is(param7.getBlock())) {
                    if (var7 == -1) {
                        var8 = false;
                        if (var5 <= 0) {
                            var0 = Blocks.AIR.defaultBlockState();
                            var4 = param7;
                        } else if (var10 >= param9 - 4 && var10 <= param9 + 1) {
                            var0 = WHITE_TERRACOTTA;
                            var4 = var2;
                        }

                        if (var10 < param9 && (var0 == null || var0.isAir())) {
                            var0 = param8;
                        }

                        var7 = var5 + Math.max(0, var10 - param9);
                        if (var10 >= param9 - 1) {
                            if (var10 > param9 + 10 + var5) {
                                BlockState var12;
                                if (var10 < 64 || var10 > 159) {
                                    var12 = ORANGE_TERRACOTTA;
                                } else if (var6) {
                                    var12 = TERRACOTTA;
                                } else {
                                    var12 = this.getBand(param3, var10, param4);
                                }

                                param1.setBlock(var10, var12);
                            } else {
                                param1.setBlock(var10, var3);
                                var8 = true;
                            }
                        } else {
                            param1.setBlock(var10, var4);
                            if (var4.is(Blocks.WHITE_TERRACOTTA)
                                || var4.is(Blocks.ORANGE_TERRACOTTA)
                                || var4.is(Blocks.MAGENTA_TERRACOTTA)
                                || var4.is(Blocks.LIGHT_BLUE_TERRACOTTA)
                                || var4.is(Blocks.YELLOW_TERRACOTTA)
                                || var4.is(Blocks.LIME_TERRACOTTA)
                                || var4.is(Blocks.PINK_TERRACOTTA)
                                || var4.is(Blocks.GRAY_TERRACOTTA)
                                || var4.is(Blocks.LIGHT_GRAY_TERRACOTTA)
                                || var4.is(Blocks.CYAN_TERRACOTTA)
                                || var4.is(Blocks.PURPLE_TERRACOTTA)
                                || var4.is(Blocks.BLUE_TERRACOTTA)
                                || var4.is(Blocks.BROWN_TERRACOTTA)
                                || var4.is(Blocks.GREEN_TERRACOTTA)
                                || var4.is(Blocks.RED_TERRACOTTA)
                                || var4.is(Blocks.BLACK_TERRACOTTA)) {
                                param1.setBlock(var10, ORANGE_TERRACOTTA);
                            }
                        }
                    } else if (var7 > 0) {
                        --var7;
                        if (var8) {
                            param1.setBlock(var10, ORANGE_TERRACOTTA);
                        } else {
                            param1.setBlock(var10, this.getBand(param3, var10, param4));
                        }
                    }

                    ++var9;
                }
            }
        }

    }

    @Override
    public void initNoise(long param0) {
        if (this.seed != param0 || this.clayBands == null) {
            this.generateBands(param0);
        }

        if (this.seed != param0 || this.pillarNoise == null || this.pillarRoofNoise == null) {
            WorldgenRandom var0 = new WorldgenRandom(new LegacyRandomSource(param0));
            this.pillarNoise = new PerlinSimplexNoise(var0, IntStream.rangeClosed(-3, 0));
            this.pillarRoofNoise = new PerlinSimplexNoise(var0, ImmutableList.of(0));
        }

        this.seed = param0;
    }

    protected void generateBands(long param0) {
        this.clayBands = new BlockState[64];
        Arrays.fill(this.clayBands, TERRACOTTA);
        WorldgenRandom var0 = new WorldgenRandom(new LegacyRandomSource(param0));
        this.clayBandsOffsetNoise = new PerlinSimplexNoise(var0, ImmutableList.of(0));

        for(int var1 = 0; var1 < 64; ++var1) {
            var1 += var0.nextInt(5) + 1;
            if (var1 < 64) {
                this.clayBands[var1] = ORANGE_TERRACOTTA;
            }
        }

        int var2 = var0.nextInt(4) + 2;

        for(int var3 = 0; var3 < var2; ++var3) {
            int var4 = var0.nextInt(3) + 1;
            int var5 = var0.nextInt(64);

            for(int var6 = 0; var5 + var6 < 64 && var6 < var4; ++var6) {
                this.clayBands[var5 + var6] = YELLOW_TERRACOTTA;
            }
        }

        int var7 = var0.nextInt(4) + 2;

        for(int var8 = 0; var8 < var7; ++var8) {
            int var9 = var0.nextInt(3) + 2;
            int var10 = var0.nextInt(64);

            for(int var11 = 0; var10 + var11 < 64 && var11 < var9; ++var11) {
                this.clayBands[var10 + var11] = BROWN_TERRACOTTA;
            }
        }

        int var12 = var0.nextInt(4) + 2;

        for(int var13 = 0; var13 < var12; ++var13) {
            int var14 = var0.nextInt(3) + 1;
            int var15 = var0.nextInt(64);

            for(int var16 = 0; var15 + var16 < 64 && var16 < var14; ++var16) {
                this.clayBands[var15 + var16] = RED_TERRACOTTA;
            }
        }

        int var17 = var0.nextInt(3) + 3;
        int var18 = 0;

        for(int var19 = 0; var19 < var17; ++var19) {
            int var20 = 1;
            var18 += var0.nextInt(16) + 4;

            for(int var21 = 0; var18 + var21 < 64 && var21 < 1; ++var21) {
                this.clayBands[var18 + var21] = WHITE_TERRACOTTA;
                if (var18 + var21 > 1 && var0.nextBoolean()) {
                    this.clayBands[var18 + var21 - 1] = LIGHT_GRAY_TERRACOTTA;
                }

                if (var18 + var21 < 63 && var0.nextBoolean()) {
                    this.clayBands[var18 + var21 + 1] = LIGHT_GRAY_TERRACOTTA;
                }
            }
        }

    }

    protected BlockState getBand(int param0, int param1, int param2) {
        int var0 = (int)Math.round(this.clayBandsOffsetNoise.getValue((double)param0 / 512.0, (double)param2 / 512.0, false) * 2.0);
        return this.clayBands[(param1 + var0 + 64) % 64];
    }
}
