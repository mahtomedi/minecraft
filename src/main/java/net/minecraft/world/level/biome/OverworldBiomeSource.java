package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.newbiome.layer.Layer;
import net.minecraft.world.level.newbiome.layer.Layers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class OverworldBiomeSource extends BiomeSource {
    public static final Codec<OverworldBiomeSource> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.LONG.fieldOf("seed").stable().forGetter(param0x -> param0x.seed),
                    Codec.BOOL
                        .optionalFieldOf("legacy_biome_init_layer", Boolean.valueOf(false), Lifecycle.stable())
                        .forGetter(param0x -> param0x.legacyBiomeInitLayer),
                    Codec.BOOL.fieldOf("large_biomes").withDefault(false).stable().forGetter(param0x -> param0x.largeBiomes)
                )
                .apply(param0, param0.stable(OverworldBiomeSource::new))
    );
    private final Layer noiseBiomeLayer;
    private static final List<Biome> POSSIBLE_BIOMES = ImmutableList.of(
        Biomes.OCEAN,
        Biomes.PLAINS,
        Biomes.DESERT,
        Biomes.MOUNTAINS,
        Biomes.FOREST,
        Biomes.TAIGA,
        Biomes.SWAMP,
        Biomes.RIVER,
        Biomes.FROZEN_OCEAN,
        Biomes.FROZEN_RIVER,
        Biomes.SNOWY_TUNDRA,
        Biomes.SNOWY_MOUNTAINS,
        Biomes.MUSHROOM_FIELDS,
        Biomes.MUSHROOM_FIELD_SHORE,
        Biomes.BEACH,
        Biomes.DESERT_HILLS,
        Biomes.WOODED_HILLS,
        Biomes.TAIGA_HILLS,
        Biomes.MOUNTAIN_EDGE,
        Biomes.JUNGLE,
        Biomes.JUNGLE_HILLS,
        Biomes.JUNGLE_EDGE,
        Biomes.DEEP_OCEAN,
        Biomes.STONE_SHORE,
        Biomes.SNOWY_BEACH,
        Biomes.BIRCH_FOREST,
        Biomes.BIRCH_FOREST_HILLS,
        Biomes.DARK_FOREST,
        Biomes.SNOWY_TAIGA,
        Biomes.SNOWY_TAIGA_HILLS,
        Biomes.GIANT_TREE_TAIGA,
        Biomes.GIANT_TREE_TAIGA_HILLS,
        Biomes.WOODED_MOUNTAINS,
        Biomes.SAVANNA,
        Biomes.SAVANNA_PLATEAU,
        Biomes.BADLANDS,
        Biomes.WOODED_BADLANDS_PLATEAU,
        Biomes.BADLANDS_PLATEAU,
        Biomes.WARM_OCEAN,
        Biomes.LUKEWARM_OCEAN,
        Biomes.COLD_OCEAN,
        Biomes.DEEP_WARM_OCEAN,
        Biomes.DEEP_LUKEWARM_OCEAN,
        Biomes.DEEP_COLD_OCEAN,
        Biomes.DEEP_FROZEN_OCEAN,
        Biomes.SUNFLOWER_PLAINS,
        Biomes.DESERT_LAKES,
        Biomes.GRAVELLY_MOUNTAINS,
        Biomes.FLOWER_FOREST,
        Biomes.TAIGA_MOUNTAINS,
        Biomes.SWAMP_HILLS,
        Biomes.ICE_SPIKES,
        Biomes.MODIFIED_JUNGLE,
        Biomes.MODIFIED_JUNGLE_EDGE,
        Biomes.TALL_BIRCH_FOREST,
        Biomes.TALL_BIRCH_HILLS,
        Biomes.DARK_FOREST_HILLS,
        Biomes.SNOWY_TAIGA_MOUNTAINS,
        Biomes.GIANT_SPRUCE_TAIGA,
        Biomes.GIANT_SPRUCE_TAIGA_HILLS,
        Biomes.MODIFIED_GRAVELLY_MOUNTAINS,
        Biomes.SHATTERED_SAVANNA,
        Biomes.SHATTERED_SAVANNA_PLATEAU,
        Biomes.ERODED_BADLANDS,
        Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU,
        Biomes.MODIFIED_BADLANDS_PLATEAU
    );
    private final long seed;
    private final boolean legacyBiomeInitLayer;
    private final boolean largeBiomes;

    public OverworldBiomeSource(long param0, boolean param1, boolean param2) {
        super(POSSIBLE_BIOMES);
        this.seed = param0;
        this.legacyBiomeInitLayer = param1;
        this.largeBiomes = param2;
        this.noiseBiomeLayer = Layers.getDefaultLayer(param0, param1, param2 ? 6 : 4, 4);
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BiomeSource withSeed(long param0) {
        return new OverworldBiomeSource(param0, this.legacyBiomeInitLayer, this.largeBiomes);
    }

    @Override
    public Biome getNoiseBiome(int param0, int param1, int param2) {
        return this.noiseBiomeLayer.get(param0, param2);
    }
}
