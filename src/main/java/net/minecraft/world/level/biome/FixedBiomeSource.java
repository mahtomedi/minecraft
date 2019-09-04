package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

public class FixedBiomeSource extends BiomeSource {
    private final Biome biome;

    public FixedBiomeSource(FixedBiomeSourceSettings param0) {
        super(ImmutableSet.of(param0.getBiome()));
        this.biome = param0.getBiome();
    }

    @Override
    public Biome getNoiseBiome(int param0, int param1, int param2) {
        return this.biome;
    }

    @Nullable
    @Override
    public BlockPos findBiomeHorizontal(int param0, int param1, int param2, int param3, List<Biome> param4, Random param5) {
        return param4.contains(this.biome)
            ? new BlockPos(param0 - param3 + param5.nextInt(param3 * 2 + 1), param1, param2 - param3 + param5.nextInt(param3 * 2 + 1))
            : null;
    }

    @Override
    public Set<Biome> getBiomesWithin(int param0, int param1, int param2, int param3) {
        return Sets.newHashSet(this.biome);
    }
}
