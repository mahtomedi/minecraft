package net.minecraft.data.worldgen.biome;

import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class SwampBiome extends Biome {
    public SwampBiome(Biome.BiomeBuilder param0) {
        super(param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getGrassColor(double param0, double param1) {
        double var0 = BIOME_INFO_NOISE.getValue(param0 * 0.0225, param1 * 0.0225, false);
        return var0 < -0.1 ? 5011004 : 6975545;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getFoliageColor() {
        return 6975545;
    }
}
