package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;

public class BiomeFilter extends PlacementFilter {
    private static final BiomeFilter INSTANCE = new BiomeFilter();
    public static Codec<BiomeFilter> CODEC = Codec.unit(() -> INSTANCE);

    private BiomeFilter() {
    }

    public static BiomeFilter biome() {
        return INSTANCE;
    }

    @Override
    protected boolean shouldPlace(PlacementContext param0, RandomSource param1, BlockPos param2) {
        PlacedFeature var0 = (PlacedFeature)param0.topFeature()
            .orElseThrow(() -> new IllegalStateException("Tried to biome check an unregistered feature, or a feature that should not restrict the biome"));
        Holder<Biome> var1 = param0.getLevel().getBiome(param2);
        return param0.generator().getBiomeGenerationSettings(var1).hasFeature(var0);
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.BIOME_FILTER;
    }
}
