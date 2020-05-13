package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableSet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CheckerboardColumnBiomeSource extends BiomeSource {
    private final Biome[] allowedBiomes;
    private final int bitShift;

    public CheckerboardColumnBiomeSource(Biome[] param0, int param1) {
        super(ImmutableSet.copyOf(param0));
        this.allowedBiomes = param0;
        this.bitShift = param1 + 2;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BiomeSource withSeed(long param0) {
        return this;
    }

    @Override
    public Biome getNoiseBiome(int param0, int param1, int param2) {
        return this.allowedBiomes[Math.floorMod((param0 >> this.bitShift) + (param2 >> this.bitShift), this.allowedBiomes.length)];
    }
}
