package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSizeType;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class TreeConfiguration implements FeatureConfiguration {
    public final BlockStateProvider trunkProvider;
    public final BlockStateProvider leavesProvider;
    public final List<TreeDecorator> decorators;
    public transient boolean fromSapling;
    public final FoliagePlacer foliagePlacer;
    public final TrunkPlacer trunkPlacer;
    public final FeatureSize minimumSize;
    public final int maxWaterDepth;
    public final boolean ignoreVines;
    public final Heightmap.Types heightmap;

    protected TreeConfiguration(
        BlockStateProvider param0,
        BlockStateProvider param1,
        FoliagePlacer param2,
        TrunkPlacer param3,
        FeatureSize param4,
        List<TreeDecorator> param5,
        int param6,
        boolean param7,
        Heightmap.Types param8
    ) {
        this.trunkProvider = param0;
        this.leavesProvider = param1;
        this.decorators = param5;
        this.foliagePlacer = param2;
        this.minimumSize = param4;
        this.trunkPlacer = param3;
        this.maxWaterDepth = param6;
        this.ignoreVines = param7;
        this.heightmap = param8;
    }

    public void setFromSapling() {
        this.fromSapling = true;
    }

    public TreeConfiguration withDecorators(List<TreeDecorator> param0) {
        return new TreeConfiguration(
            this.trunkProvider,
            this.leavesProvider,
            this.foliagePlacer,
            this.trunkPlacer,
            this.minimumSize,
            param0,
            this.maxWaterDepth,
            this.ignoreVines,
            this.heightmap
        );
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("trunk_provider"), this.trunkProvider.serialize(param0))
            .put(param0.createString("leaves_provider"), this.leavesProvider.serialize(param0))
            .put(param0.createString("decorators"), param0.createList(this.decorators.stream().map(param1 -> param1.serialize(param0))))
            .put(param0.createString("foliage_placer"), this.foliagePlacer.serialize(param0))
            .put(param0.createString("trunk_placer"), this.trunkPlacer.serialize(param0))
            .put(param0.createString("minimum_size"), this.minimumSize.serialize(param0))
            .put(param0.createString("max_water_depth"), param0.createInt(this.maxWaterDepth))
            .put(param0.createString("ignore_vines"), param0.createBoolean(this.ignoreVines))
            .put(param0.createString("heightmap"), param0.createString(this.heightmap.getSerializationKey()));
        return new Dynamic<>(param0, param0.createMap(var0.build()));
    }

    public static <T> TreeConfiguration deserialize(Dynamic<T> param0) {
        BlockStateProviderType<?> var0 = Registry.BLOCKSTATE_PROVIDER_TYPES
            .get(new ResourceLocation(param0.get("trunk_provider").get("type").asString().orElseThrow(RuntimeException::new)));
        BlockStateProviderType<?> var1 = Registry.BLOCKSTATE_PROVIDER_TYPES
            .get(new ResourceLocation(param0.get("leaves_provider").get("type").asString().orElseThrow(RuntimeException::new)));
        FoliagePlacerType<?> var2 = Registry.FOLIAGE_PLACER_TYPES
            .get(new ResourceLocation(param0.get("foliage_placer").get("type").asString().orElseThrow(RuntimeException::new)));
        TrunkPlacerType<?> var3 = Registry.TRUNK_PLACER_TYPES
            .get(new ResourceLocation(param0.get("trunk_placer").get("type").asString().orElseThrow(RuntimeException::new)));
        FeatureSizeType<?> var4 = Registry.FEATURE_SIZE_TYPES
            .get(new ResourceLocation(param0.get("minimum_size").get("type").asString().orElseThrow(RuntimeException::new)));
        return new TreeConfiguration(
            var0.deserialize(param0.get("trunk_provider").orElseEmptyMap()),
            var1.deserialize(param0.get("leaves_provider").orElseEmptyMap()),
            var2.deserialize(param0.get("foliage_placer").orElseEmptyMap()),
            var3.deserialize(param0.get("trunk_placer").orElseEmptyMap()),
            var4.deserialize(param0.get("minimum_size").orElseEmptyMap()),
            param0.get("decorators")
                .asList(
                    param0x -> Registry.TREE_DECORATOR_TYPES
                            .get(new ResourceLocation(param0x.get("type").asString().orElseThrow(RuntimeException::new)))
                            .deserialize(param0x)
                ),
            param0.get("max_water_depth").asInt(0),
            param0.get("ignore_vines").asBoolean(false),
            Heightmap.Types.getFromKey(param0.get("heightmap").asString(""))
        );
    }

    public static class TreeConfigurationBuilder {
        public final BlockStateProvider trunkProvider;
        public final BlockStateProvider leavesProvider;
        private final FoliagePlacer foliagePlacer;
        private final TrunkPlacer trunkPlacer;
        private final FeatureSize minimumSize;
        private List<TreeDecorator> decorators = ImmutableList.of();
        private int maxWaterDepth;
        private boolean ignoreVines;
        private Heightmap.Types heightmap = Heightmap.Types.OCEAN_FLOOR;

        public TreeConfigurationBuilder(BlockStateProvider param0, BlockStateProvider param1, FoliagePlacer param2, TrunkPlacer param3, FeatureSize param4) {
            this.trunkProvider = param0;
            this.leavesProvider = param1;
            this.foliagePlacer = param2;
            this.trunkPlacer = param3;
            this.minimumSize = param4;
        }

        public TreeConfiguration.TreeConfigurationBuilder decorators(List<TreeDecorator> param0) {
            this.decorators = param0;
            return this;
        }

        public TreeConfiguration.TreeConfigurationBuilder maxWaterDepth(int param0) {
            this.maxWaterDepth = param0;
            return this;
        }

        public TreeConfiguration.TreeConfigurationBuilder ignoreVines() {
            this.ignoreVines = true;
            return this;
        }

        public TreeConfiguration.TreeConfigurationBuilder heightmap(Heightmap.Types param0) {
            this.heightmap = param0;
            return this;
        }

        public TreeConfiguration build() {
            return new TreeConfiguration(
                this.trunkProvider,
                this.leavesProvider,
                this.foliagePlacer,
                this.trunkPlacer,
                this.minimumSize,
                this.decorators,
                this.maxWaterDepth,
                this.ignoreVines,
                this.heightmap
            );
        }
    }
}
