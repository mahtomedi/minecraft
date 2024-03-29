package net.minecraft.world.level.biome;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;

public class FixedBiomeSource extends BiomeSource implements BiomeManager.NoiseBiomeSource {
    public static final Codec<FixedBiomeSource> CODEC = Biome.CODEC.fieldOf("biome").xmap(FixedBiomeSource::new, param0 -> param0.biome).stable().codec();
    private final Holder<Biome> biome;

    public FixedBiomeSource(Holder<Biome> param0) {
        this.biome = param0;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(this.biome);
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int param0, int param1, int param2, Climate.Sampler param3) {
        return this.biome;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int param0, int param1, int param2) {
        return this.biome;
    }

    @Nullable
    @Override
    public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(
        int param0,
        int param1,
        int param2,
        int param3,
        int param4,
        Predicate<Holder<Biome>> param5,
        RandomSource param6,
        boolean param7,
        Climate.Sampler param8
    ) {
        if (param5.test(this.biome)) {
            return param7
                ? Pair.of(new BlockPos(param0, param1, param2), this.biome)
                : Pair.of(new BlockPos(param0 - param3 + param6.nextInt(param3 * 2 + 1), param1, param2 - param3 + param6.nextInt(param3 * 2 + 1)), this.biome);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public Pair<BlockPos, Holder<Biome>> findClosestBiome3d(
        BlockPos param0, int param1, int param2, int param3, Predicate<Holder<Biome>> param4, Climate.Sampler param5, LevelReader param6
    ) {
        return param4.test(this.biome) ? Pair.of(param0, this.biome) : null;
    }

    @Override
    public Set<Holder<Biome>> getBiomesWithin(int param0, int param1, int param2, int param3, Climate.Sampler param4) {
        return Sets.newHashSet(Set.of(this.biome));
    }
}
