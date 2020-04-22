package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableSet;

public class CheckerboardColumnBiomeSource extends BiomeSource {
    private final Biome[] allowedBiomes;
    private final int bitShift;

    public CheckerboardColumnBiomeSource(CheckerboardBiomeSourceSettings param0) {
        super(ImmutableSet.copyOf(param0.getAllowedBiomes()));
        this.allowedBiomes = param0.getAllowedBiomes();
        this.bitShift = param0.getSize() + 2;
    }

    @Override
    public Biome getNoiseBiome(int param0, int param1, int param2) {
        return this.allowedBiomes[Math.floorMod((param0 >> this.bitShift) + (param2 >> this.bitShift), this.allowedBiomes.length)];
    }
}
