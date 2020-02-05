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
    public BlockPos findBiomeHorizontal(int param0, int param1, int param2, int param3, int param4, List<Biome> param5, Random param6, boolean param7) {
        if (param5.contains(this.biome)) {
            return param7
                ? new BlockPos(param0, param1, param2)
                : new BlockPos(param0 - param3 + param6.nextInt(param3 * 2 + 1), param1, param2 - param3 + param6.nextInt(param3 * 2 + 1));
        } else {
            return null;
        }
    }

    @Override
    public Set<Biome> getBiomesWithin(int param0, int param1, int param2, int param3) {
        return Sets.newHashSet(this.biome);
    }
}
