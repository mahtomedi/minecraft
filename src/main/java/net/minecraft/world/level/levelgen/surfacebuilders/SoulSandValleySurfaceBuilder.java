package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public class SoulSandValleySurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState SOUL_SAND = Blocks.SOUL_SAND.defaultBlockState();
    private static final BlockState SOUL_SOIL = Blocks.SOUL_SOIL.defaultBlockState();
    private long seed;
    private PerlinNoise soulSandNoiseFloor;
    private PerlinNoise soulSoilNoiseFloor;
    private PerlinNoise soulSandNoiseCeiling;
    private PerlinNoise soulSoilNoiseCeiling;
    private PerlinNoise gravelNoise;

    public SoulSandValleySurfaceBuilder(Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration> param0) {
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
        int var0 = param9 + 1;
        int var1 = param3 & 15;
        int var2 = param4 & 15;
        int var3 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        int var4 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        double var5 = 0.03125;
        boolean var6 = this.gravelNoise.getValue((double)param3 * 0.03125, 109.0, (double)param4 * 0.03125) * 75.0 + param0.nextDouble() > 0.0;
        double var7 = this.soulSoilNoiseFloor.getValue((double)param3, (double)param9, (double)param4);
        double var8 = this.soulSandNoiseFloor.getValue((double)param3, (double)param9, (double)param4);
        double var9 = this.soulSoilNoiseCeiling.getValue((double)param3, (double)param9, (double)param4);
        double var10 = this.soulSandNoiseCeiling.getValue((double)param3, (double)param9, (double)param4);
        BlockState var11 = var9 > var10 ? SOUL_SOIL : SOUL_SAND;
        BlockState var12 = var7 > var8 ? SOUL_SOIL : SOUL_SAND;
        BlockPos.MutableBlockPos var13 = new BlockPos.MutableBlockPos();
        BlockState var14 = param1.getBlockState(var13.set(var1, 128, var2));

        for(int var15 = 127; var15 >= 0; --var15) {
            var13.set(var1, var15, var2);
            BlockState var16 = param1.getBlockState(var13);
            if (var14.getBlock() == param7.getBlock() && (var16.isAir() || var16 == param8)) {
                for(int var17 = 0; var17 < var3; ++var17) {
                    var13.move(Direction.UP);
                    if (param1.getBlockState(var13).getBlock() != param7.getBlock()) {
                        break;
                    }

                    param1.setBlockState(var13, var11, false);
                }

                var13.set(var1, var15, var2);
            }

            if ((var14.isAir() || var14 == param8) && var16.getBlock() == param7.getBlock()) {
                for(int var18 = 0; var18 < var4 && param1.getBlockState(var13).getBlock() == param7.getBlock(); ++var18) {
                    if (var6 && var15 >= var0 - 4 && var15 <= var0 + 1) {
                        param1.setBlockState(var13, GRAVEL, false);
                    } else {
                        param1.setBlockState(var13, var12, false);
                    }

                    var13.move(Direction.DOWN);
                }
            }

            var14 = var16;
        }

    }

    @Override
    public void initNoise(long param0) {
        if (this.seed != param0
            || this.soulSandNoiseFloor == null
            || this.soulSoilNoiseFloor == null
            || this.soulSandNoiseCeiling == null
            || this.soulSoilNoiseCeiling == null
            || this.gravelNoise == null) {
            this.soulSandNoiseFloor = new PerlinNoise(new WorldgenRandom(param0), ImmutableList.of(-4));
            this.soulSoilNoiseFloor = new PerlinNoise(new WorldgenRandom(param0 + 1L), ImmutableList.of(-4));
            this.soulSandNoiseCeiling = new PerlinNoise(new WorldgenRandom(param0 + 2L), ImmutableList.of(-4));
            this.soulSoilNoiseCeiling = new PerlinNoise(new WorldgenRandom(param0 + 3L), ImmutableList.of(-4));
            this.gravelNoise = new PerlinNoise(new WorldgenRandom(param0 + 4L), ImmutableList.of(0));
        }

        this.seed = param0;
    }
}
