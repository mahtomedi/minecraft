package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

public class BadlandsSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
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
        ChunkAccess param1,
        Biome param2,
        int param3,
        int param4,
        int param5,
        double param6,
        BlockState param7,
        BlockState param8,
        int param9,
        long param10,
        SurfaceBuilderBaseConfiguration param11
    ) {
        int var0 = param3 & 15;
        int var1 = param4 & 15;
        BlockState var2 = WHITE_TERRACOTTA;
        BlockState var3 = param2.getSurfaceBuilderConfig().getUnderMaterial();
        int var4 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        boolean var5 = Math.cos(param6 / 3.0 * Math.PI) > 0.0;
        int var6 = -1;
        boolean var7 = false;
        int var8 = 0;
        BlockPos.MutableBlockPos var9 = new BlockPos.MutableBlockPos();

        for(int var10 = param5; var10 >= 0; --var10) {
            if (var8 < 15) {
                var9.set(var0, var10, var1);
                BlockState var11 = param1.getBlockState(var9);
                if (var11.isAir()) {
                    var6 = -1;
                } else if (var11.is(param7.getBlock())) {
                    if (var6 == -1) {
                        var7 = false;
                        if (var4 <= 0) {
                            var2 = Blocks.AIR.defaultBlockState();
                            var3 = param7;
                        } else if (var10 >= param9 - 4 && var10 <= param9 + 1) {
                            var2 = WHITE_TERRACOTTA;
                            var3 = param2.getSurfaceBuilderConfig().getUnderMaterial();
                        }

                        if (var10 < param9 && (var2 == null || var2.isAir())) {
                            var2 = param8;
                        }

                        var6 = var4 + Math.max(0, var10 - param9);
                        if (var10 >= param9 - 1) {
                            if (var10 > param9 + 3 + var4) {
                                BlockState var12;
                                if (var10 < 64 || var10 > 127) {
                                    var12 = ORANGE_TERRACOTTA;
                                } else if (var5) {
                                    var12 = TERRACOTTA;
                                } else {
                                    var12 = this.getBand(param3, var10, param4);
                                }

                                param1.setBlockState(var9, var12, false);
                            } else {
                                param1.setBlockState(var9, param2.getSurfaceBuilderConfig().getTopMaterial(), false);
                                var7 = true;
                            }
                        } else {
                            param1.setBlockState(var9, var3, false);
                            Block var15 = var3.getBlock();
                            if (var15 == Blocks.WHITE_TERRACOTTA
                                || var15 == Blocks.ORANGE_TERRACOTTA
                                || var15 == Blocks.MAGENTA_TERRACOTTA
                                || var15 == Blocks.LIGHT_BLUE_TERRACOTTA
                                || var15 == Blocks.YELLOW_TERRACOTTA
                                || var15 == Blocks.LIME_TERRACOTTA
                                || var15 == Blocks.PINK_TERRACOTTA
                                || var15 == Blocks.GRAY_TERRACOTTA
                                || var15 == Blocks.LIGHT_GRAY_TERRACOTTA
                                || var15 == Blocks.CYAN_TERRACOTTA
                                || var15 == Blocks.PURPLE_TERRACOTTA
                                || var15 == Blocks.BLUE_TERRACOTTA
                                || var15 == Blocks.BROWN_TERRACOTTA
                                || var15 == Blocks.GREEN_TERRACOTTA
                                || var15 == Blocks.RED_TERRACOTTA
                                || var15 == Blocks.BLACK_TERRACOTTA) {
                                param1.setBlockState(var9, ORANGE_TERRACOTTA, false);
                            }
                        }
                    } else if (var6 > 0) {
                        --var6;
                        if (var7) {
                            param1.setBlockState(var9, ORANGE_TERRACOTTA, false);
                        } else {
                            param1.setBlockState(var9, this.getBand(param3, var10, param4), false);
                        }
                    }

                    ++var8;
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
            WorldgenRandom var0 = new WorldgenRandom(param0);
            this.pillarNoise = new PerlinSimplexNoise(var0, IntStream.rangeClosed(-3, 0));
            this.pillarRoofNoise = new PerlinSimplexNoise(var0, ImmutableList.of(0));
        }

        this.seed = param0;
    }

    protected void generateBands(long param0) {
        this.clayBands = new BlockState[64];
        Arrays.fill(this.clayBands, TERRACOTTA);
        WorldgenRandom var0 = new WorldgenRandom(param0);
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
