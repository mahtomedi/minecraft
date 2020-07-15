package net.minecraft.data.worldgen.biome;

import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class BadlandsBiome extends Biome {
    public BadlandsBiome(Biome.BiomeBuilder param0) {
        super(param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getFoliageColor() {
        return 10387789;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getGrassColor(double param0, double param1) {
        return 9470285;
    }
}
