package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class SmallTreeConfiguration extends TreeConfiguration {
    public final FoliagePlacer foliagePlacer;
    public final TrunkPlacer trunkPlacer;
    public final int maxWaterDepth;
    public final boolean ignoreVines;

    protected SmallTreeConfiguration(
        BlockStateProvider param0, BlockStateProvider param1, FoliagePlacer param2, TrunkPlacer param3, List<TreeDecorator> param4, int param5, boolean param6
    ) {
        super(param0, param1, param4, param3.getBaseHeight());
        this.foliagePlacer = param2;
        this.trunkPlacer = param3;
        this.maxWaterDepth = param5;
        this.ignoreVines = param6;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("foliage_placer"), this.foliagePlacer.serialize(param0))
            .put(param0.createString("trunk_placer"), this.trunkPlacer.serialize(param0))
            .put(param0.createString("max_water_depth"), param0.createInt(this.maxWaterDepth))
            .put(param0.createString("ignore_vines"), param0.createBoolean(this.ignoreVines));
        Dynamic<T> var1 = new Dynamic<>(param0, param0.createMap(var0.build()));
        return var1.merge(super.serialize(param0));
    }

    public static <T> SmallTreeConfiguration deserialize(Dynamic<T> param0) {
        TreeConfiguration var0 = TreeConfiguration.deserialize(param0);
        FoliagePlacerType<?> var1 = Registry.FOLIAGE_PLACER_TYPES
            .get(new ResourceLocation(param0.get("foliage_placer").get("type").asString().orElseThrow(RuntimeException::new)));
        TrunkPlacerType<?> var2 = Registry.TRUNK_PLACER_TYPES
            .get(new ResourceLocation(param0.get("trunk_placer").get("type").asString().orElseThrow(RuntimeException::new)));
        return new SmallTreeConfiguration(
            var0.trunkProvider,
            var0.leavesProvider,
            var1.deserialize(param0.get("foliage_placer").orElseEmptyMap()),
            var2.deserialize(param0.get("trunk_placer").orElseEmptyMap()),
            var0.decorators,
            param0.get("max_water_depth").asInt(0),
            param0.get("ignore_vines").asBoolean(false)
        );
    }

    public static class SmallTreeConfigurationBuilder extends TreeConfiguration.TreeConfigurationBuilder {
        private final FoliagePlacer foliagePlacer;
        private final TrunkPlacer trunkPlacer;
        private List<TreeDecorator> decorators = ImmutableList.of();
        private int maxWaterDepth;
        private boolean ignoreVines;

        public SmallTreeConfigurationBuilder(BlockStateProvider param0, BlockStateProvider param1, FoliagePlacer param2, TrunkPlacer param3) {
            super(param0, param1);
            this.foliagePlacer = param2;
            this.trunkPlacer = param3;
        }

        public SmallTreeConfiguration.SmallTreeConfigurationBuilder decorators(List<TreeDecorator> param0) {
            this.decorators = param0;
            return this;
        }

        public SmallTreeConfiguration.SmallTreeConfigurationBuilder maxWaterDepth(int param0) {
            this.maxWaterDepth = param0;
            return this;
        }

        public SmallTreeConfiguration.SmallTreeConfigurationBuilder ignoreVines() {
            this.ignoreVines = true;
            return this;
        }

        public SmallTreeConfiguration build() {
            return new SmallTreeConfiguration(
                this.trunkProvider, this.leavesProvider, this.foliagePlacer, this.trunkPlacer, this.decorators, this.maxWaterDepth, this.ignoreVines
            );
        }
    }
}
