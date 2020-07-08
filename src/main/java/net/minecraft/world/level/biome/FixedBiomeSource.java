package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FixedBiomeSource extends BiomeSource {
    public static final Codec<FixedBiomeSource> CODEC = Biome.CODEC.fieldOf("biome").xmap(FixedBiomeSource::new, param0 -> param0.biome).stable().codec();
    private final Supplier<Biome> biome;

    public FixedBiomeSource(Biome param0) {
        this(() -> param0);
    }

    public FixedBiomeSource(Supplier<Biome> param0) {
        super(ImmutableList.of(param0.get()));
        this.biome = param0;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BiomeSource withSeed(long param0) {
        return this;
    }

    @Override
    public Biome getNoiseBiome(int param0, int param1, int param2) {
        return this.biome.get();
    }

    @Nullable
    @Override
    public BlockPos findBiomeHorizontal(int param0, int param1, int param2, int param3, int param4, List<Biome> param5, Random param6, boolean param7) {
        if (param5.contains(this.biome.get())) {
            return param7
                ? new BlockPos(param0, param1, param2)
                : new BlockPos(param0 - param3 + param6.nextInt(param3 * 2 + 1), param1, param2 - param3 + param6.nextInt(param3 * 2 + 1));
        } else {
            return null;
        }
    }

    @Override
    public Set<Biome> getBiomesWithin(int param0, int param1, int param2, int param3) {
        return Sets.newHashSet(this.biome.get());
    }
}
