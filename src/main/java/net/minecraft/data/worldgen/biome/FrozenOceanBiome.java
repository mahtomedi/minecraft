package net.minecraft.data.worldgen.biome;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

public final class FrozenOceanBiome extends Biome {
    protected static final PerlinSimplexNoise FROZEN_TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(3456L), ImmutableList.of(-2, -1, 0));

    public FrozenOceanBiome(Biome.BiomeBuilder param0) {
        super(param0);
    }

    @Override
    protected float getTemperatureNoCache(BlockPos param0) {
        float var0 = this.getTemperature();
        double var1 = FROZEN_TEMPERATURE_NOISE.getValue((double)param0.getX() * 0.05, (double)param0.getZ() * 0.05, false) * 7.0;
        double var2 = BIOME_INFO_NOISE.getValue((double)param0.getX() * 0.2, (double)param0.getZ() * 0.2, false);
        double var3 = var1 + var2;
        if (var3 < 0.3) {
            double var4 = BIOME_INFO_NOISE.getValue((double)param0.getX() * 0.09, (double)param0.getZ() * 0.09, false);
            if (var4 < 0.8) {
                var0 = 0.2F;
            }
        }

        if (param0.getY() > 64) {
            float var5 = (float)(TEMPERATURE_NOISE.getValue((double)((float)param0.getX() / 8.0F), (double)((float)param0.getZ() / 8.0F), false) * 4.0);
            return var0 - (var5 + (float)param0.getY() - 64.0F) * 0.05F / 30.0F;
        } else {
            return var0;
        }
    }
}
