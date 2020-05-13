package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.ChanceRangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.CountRangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoiseDependantDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.nether.ChanceRangeDecorator;
import net.minecraft.world.level.levelgen.placement.nether.CountRangeDecorator;
import net.minecraft.world.level.levelgen.placement.nether.FireDecorator;
import net.minecraft.world.level.levelgen.placement.nether.LightGemChanceDecorator;
import net.minecraft.world.level.levelgen.placement.nether.MagmaDecorator;
import net.minecraft.world.level.levelgen.placement.nether.RandomCountRangeDecorator;

public abstract class FeatureDecorator<DC extends DecoratorConfiguration> {
    public static final FeatureDecorator<NoneDecoratorConfiguration> NOPE = register(
        "nope", new NopePlacementDecorator(NoneDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<FrequencyDecoratorConfiguration> COUNT_HEIGHTMAP = register(
        "count_heightmap", new CountHeightmapDecorator(FrequencyDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<FrequencyDecoratorConfiguration> COUNT_TOP_SOLID = register(
        "count_top_solid", new CountTopSolidDecorator(FrequencyDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<FrequencyDecoratorConfiguration> COUNT_HEIGHTMAP_32 = register(
        "count_heightmap_32", new CountHeightmap32Decorator(FrequencyDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<FrequencyDecoratorConfiguration> COUNT_HEIGHTMAP_DOUBLE = register(
        "count_heightmap_double", new CountHeighmapDoubleDecorator(FrequencyDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<FrequencyDecoratorConfiguration> COUNT_HEIGHT_64 = register(
        "count_height_64", new CountHeight64Decorator(FrequencyDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<NoiseDependantDecoratorConfiguration> NOISE_HEIGHTMAP_32 = register(
        "noise_heightmap_32", new NoiseHeightmap32Decorator(NoiseDependantDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<NoiseDependantDecoratorConfiguration> NOISE_HEIGHTMAP_DOUBLE = register(
        "noise_heightmap_double", new NoiseHeightmapDoubleDecorator(NoiseDependantDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<ChanceDecoratorConfiguration> CHANCE_HEIGHTMAP = register(
        "chance_heightmap", new ChanceHeightmapDecorator(ChanceDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<ChanceDecoratorConfiguration> CHANCE_HEIGHTMAP_DOUBLE = register(
        "chance_heightmap_double", new ChanceHeightmapDoubleDecorator(ChanceDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<ChanceDecoratorConfiguration> CHANCE_PASSTHROUGH = register(
        "chance_passthrough", new ChancePassthroughDecorator(ChanceDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<ChanceDecoratorConfiguration> CHANCE_TOP_SOLID_HEIGHTMAP = register(
        "chance_top_solid_heightmap", new ChanceTopSolidHeightmapDecorator(ChanceDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<FrequencyWithExtraChanceDecoratorConfiguration> COUNT_EXTRA_HEIGHTMAP = register(
        "count_extra_heightmap", new CountWithExtraChanceHeightmapDecorator(FrequencyWithExtraChanceDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<CountRangeDecoratorConfiguration> COUNT_RANGE = register(
        "count_range", new CountRangeDecorator(CountRangeDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<CountRangeDecoratorConfiguration> COUNT_BIASED_RANGE = register(
        "count_biased_range", new CountBiasedRangeDecorator(CountRangeDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<CountRangeDecoratorConfiguration> COUNT_VERY_BIASED_RANGE = register(
        "count_very_biased_range", new CountVeryBiasedRangeDecorator(CountRangeDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<CountRangeDecoratorConfiguration> RANDOM_COUNT_RANGE = register(
        "random_count_range", new RandomCountRangeDecorator(CountRangeDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<ChanceRangeDecoratorConfiguration> CHANCE_RANGE = register(
        "chance_range", new ChanceRangeDecorator(ChanceRangeDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<FrequencyChanceDecoratorConfiguration> COUNT_CHANCE_HEIGHTMAP = register(
        "count_chance_heightmap", new CountChanceHeightmapDecorator(FrequencyChanceDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<FrequencyChanceDecoratorConfiguration> COUNT_CHANCE_HEIGHTMAP_DOUBLE = register(
        "count_chance_heightmap_double", new CountChanceHeightmapDoubleDecorator(FrequencyChanceDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<DepthAverageConfigation> COUNT_DEPTH_AVERAGE = register(
        "count_depth_average", new CountDepthAverageDecorator(DepthAverageConfigation::deserialize)
    );
    public static final FeatureDecorator<NoneDecoratorConfiguration> TOP_SOLID_HEIGHTMAP = register(
        "top_solid_heightmap", new TopSolidHeightMapDecorator(NoneDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<RangeDecoratorConfiguration> TOP_SOLID_HEIGHTMAP_RANGE = register(
        "top_solid_heightmap_range", new TopSolidHeightMapRangeDecorator(RangeDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<NoiseCountFactorDecoratorConfiguration> TOP_SOLID_HEIGHTMAP_NOISE_BIASED = register(
        "top_solid_heightmap_noise_biased", new TopSolidHeightMapNoiseBasedDecorator(NoiseCountFactorDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<CarvingMaskDecoratorConfiguration> CARVING_MASK = register(
        "carving_mask", new CarvingMaskDecorator(CarvingMaskDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<FrequencyDecoratorConfiguration> FOREST_ROCK = register(
        "forest_rock", new ForestRockPlacementDecorator(FrequencyDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<FrequencyDecoratorConfiguration> FIRE = register(
        "fire", new FireDecorator(FrequencyDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<FrequencyDecoratorConfiguration> MAGMA = register(
        "magma", new MagmaDecorator(FrequencyDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<NoneDecoratorConfiguration> EMERALD_ORE = register(
        "emerald_ore", new EmeraldPlacementDecorator(NoneDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<ChanceDecoratorConfiguration> LAVA_LAKE = register(
        "lava_lake", new LakeLavaPlacementDecorator(ChanceDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<ChanceDecoratorConfiguration> WATER_LAKE = register(
        "water_lake", new LakeWaterPlacementDecorator(ChanceDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<ChanceDecoratorConfiguration> DUNGEONS = register(
        "dungeons", new MonsterRoomPlacementDecorator(ChanceDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<NoneDecoratorConfiguration> DARK_OAK_TREE = register(
        "dark_oak_tree", new DarkOakTreePlacementDecorator(NoneDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<ChanceDecoratorConfiguration> ICEBERG = register(
        "iceberg", new IcebergPlacementDecorator(ChanceDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<FrequencyDecoratorConfiguration> LIGHT_GEM_CHANCE = register(
        "light_gem_chance", new LightGemChanceDecorator(FrequencyDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<NoneDecoratorConfiguration> END_ISLAND = register(
        "end_island", new EndIslandPlacementDecorator(NoneDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<NoneDecoratorConfiguration> CHORUS_PLANT = register(
        "chorus_plant", new ChorusPlantPlacementDecorator(NoneDecoratorConfiguration::deserialize)
    );
    public static final FeatureDecorator<NoneDecoratorConfiguration> END_GATEWAY = register(
        "end_gateway", new EndGatewayPlacementDecorator(NoneDecoratorConfiguration::deserialize)
    );
    private final Function<Dynamic<?>, ? extends DC> configurationFactory;

    private static <T extends DecoratorConfiguration, G extends FeatureDecorator<T>> G register(String param0, G param1) {
        return Registry.register(Registry.DECORATOR, param0, param1);
    }

    public FeatureDecorator(Function<Dynamic<?>, ? extends DC> param0) {
        this.configurationFactory = param0;
    }

    public DC createSettings(Dynamic<?> param0) {
        return this.configurationFactory.apply(param0);
    }

    public ConfiguredDecorator<DC> configured(DC param0) {
        return new ConfiguredDecorator<>(this, param0);
    }

    protected <FC extends FeatureConfiguration, F extends Feature<FC>> boolean placeFeature(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, DC param5, ConfiguredFeature<FC, F> param6
    ) {
        AtomicBoolean var0 = new AtomicBoolean(false);
        this.getPositions(param0, param2, param3, param5, param4).forEach(param6x -> {
            boolean var0x = param6.place(param0, param1, param2, param3, param6x);
            var0.set(var0.get() || var0x);
        });
        return var0.get();
    }

    public abstract Stream<BlockPos> getPositions(LevelAccessor var1, ChunkGenerator var2, Random var3, DC var4, BlockPos var5);

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(this.hashCode());
    }
}
