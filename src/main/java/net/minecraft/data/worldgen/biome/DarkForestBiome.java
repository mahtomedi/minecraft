package net.minecraft.data.worldgen.biome;

import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class DarkForestBiome extends Biome {
    public DarkForestBiome(Biome.BiomeBuilder param0) {
        super(param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getGrassColor(double param0, double param1) {
        int var0 = super.getGrassColor(param0, param1);
        return (var0 & 16711422) + 2634762 >> 1;
    }
}
