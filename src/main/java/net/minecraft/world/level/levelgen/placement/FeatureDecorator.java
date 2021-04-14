package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HeightmapConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoiseDependantDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.nether.CountMultiLayerDecorator;

public abstract class FeatureDecorator<DC extends DecoratorConfiguration> {
    public static final FeatureDecorator<NoneDecoratorConfiguration> NOPE = register("nope", new NopePlacementDecorator(NoneDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<DecoratedDecoratorConfiguration> DECORATED = register(
        "decorated", new DecoratedDecorator(DecoratedDecoratorConfiguration.CODEC)
    );
    public static final FeatureDecorator<CarvingMaskDecoratorConfiguration> CARVING_MASK = register(
        "carving_mask", new CarvingMaskDecorator(CarvingMaskDecoratorConfiguration.CODEC)
    );
    public static final FeatureDecorator<CountConfiguration> COUNT_MULTILAYER = register(
        "count_multilayer", new CountMultiLayerDecorator(CountConfiguration.CODEC)
    );
    public static final FeatureDecorator<NoneDecoratorConfiguration> SQUARE = register("square", new SquareDecorator(NoneDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<NoneDecoratorConfiguration> DARK_OAK_TREE = register(
        "dark_oak_tree", new DarkOakTreePlacementDecorator(NoneDecoratorConfiguration.CODEC)
    );
    public static final FeatureDecorator<NoneDecoratorConfiguration> ICEBERG = register(
        "iceberg", new IcebergPlacementDecorator(NoneDecoratorConfiguration.CODEC)
    );
    public static final FeatureDecorator<ChanceDecoratorConfiguration> CHANCE = register("chance", new ChanceDecorator(ChanceDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<CountConfiguration> COUNT = register("count", new CountDecorator(CountConfiguration.CODEC));
    public static final FeatureDecorator<NoiseDependantDecoratorConfiguration> COUNT_NOISE = register(
        "count_noise", new CountNoiseDecorator(NoiseDependantDecoratorConfiguration.CODEC)
    );
    public static final FeatureDecorator<NoiseCountFactorDecoratorConfiguration> COUNT_NOISE_BIASED = register(
        "count_noise_biased", new NoiseBasedDecorator(NoiseCountFactorDecoratorConfiguration.CODEC)
    );
    public static final FeatureDecorator<FrequencyWithExtraChanceDecoratorConfiguration> COUNT_EXTRA = register(
        "count_extra", new CountWithExtraChanceDecorator(FrequencyWithExtraChanceDecoratorConfiguration.CODEC)
    );
    public static final FeatureDecorator<ChanceDecoratorConfiguration> LAVA_LAKE = register(
        "lava_lake", new LakeLavaPlacementDecorator(ChanceDecoratorConfiguration.CODEC)
    );
    public static final FeatureDecorator<HeightmapConfiguration> HEIGHTMAP = register("heightmap", new HeightmapDecorator(HeightmapConfiguration.CODEC));
    public static final FeatureDecorator<HeightmapConfiguration> HEIGHTMAP_SPREAD_DOUBLE = register(
        "heightmap_spread_double", new HeightmapDoubleDecorator(HeightmapConfiguration.CODEC)
    );
    public static final FeatureDecorator<WaterDepthThresholdConfiguration> WATER_DEPTH_THRESHOLD = register(
        "water_depth_threshold", new WaterDepthThresholdDecorator(WaterDepthThresholdConfiguration.CODEC)
    );
    public static final FeatureDecorator<CaveDecoratorConfiguration> CAVE_SURFACE = register(
        "cave_surface", new CaveSurfaceDecorator(CaveDecoratorConfiguration.CODEC)
    );
    public static final FeatureDecorator<RangeDecoratorConfiguration> RANGE = register("range", new RangeDecorator(RangeDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<NoneDecoratorConfiguration> SPREAD_32_ABOVE = register(
        "spread_32_above", new Spread32Decorator(NoneDecoratorConfiguration.CODEC)
    );
    public static final FeatureDecorator<NoneDecoratorConfiguration> END_GATEWAY = register(
        "end_gateway", new EndGatewayPlacementDecorator(NoneDecoratorConfiguration.CODEC)
    );
    private final Codec<ConfiguredDecorator<DC>> configuredCodec;

    private static <T extends DecoratorConfiguration, G extends FeatureDecorator<T>> G register(String param0, G param1) {
        return Registry.register(Registry.DECORATOR, param0, param1);
    }

    public FeatureDecorator(Codec<DC> param0) {
        this.configuredCodec = param0.fieldOf("config").xmap(param0x -> new ConfiguredDecorator<>(this, param0x), ConfiguredDecorator::config).codec();
    }

    public ConfiguredDecorator<DC> configured(DC param0) {
        return new ConfiguredDecorator<>(this, param0);
    }

    public Codec<ConfiguredDecorator<DC>> configuredCodec() {
        return this.configuredCodec;
    }

    public abstract Stream<BlockPos> getPositions(DecorationContext var1, Random var2, DC var3, BlockPos var4);

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(this.hashCode());
    }
}
