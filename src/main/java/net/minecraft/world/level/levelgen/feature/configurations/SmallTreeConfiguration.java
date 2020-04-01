package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;

public class SmallTreeConfiguration extends TreeConfiguration {
    public final FoliagePlacer foliagePlacer;
    public final int heightRandA;
    public final int heightRandB;
    public final int trunkHeight;
    public final int trunkHeightRandom;
    public final int trunkTopOffset;
    public final int trunkTopOffsetRandom;
    public final int foliageHeight;
    public final int foliageHeightRandom;
    public final int maxWaterDepth;
    public final boolean ignoreVines;

    protected SmallTreeConfiguration(
        BlockStateProvider param0,
        BlockStateProvider param1,
        FoliagePlacer param2,
        List<TreeDecorator> param3,
        int param4,
        int param5,
        int param6,
        int param7,
        int param8,
        int param9,
        int param10,
        int param11,
        int param12,
        int param13,
        boolean param14
    ) {
        super(param0, param1, param3, param4);
        this.foliagePlacer = param2;
        this.heightRandA = param5;
        this.heightRandB = param6;
        this.trunkHeight = param7;
        this.trunkHeightRandom = param8;
        this.trunkTopOffset = param9;
        this.trunkTopOffsetRandom = param10;
        this.foliageHeight = param11;
        this.foliageHeightRandom = param12;
        this.maxWaterDepth = param13;
        this.ignoreVines = param14;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("foliage_placer"), this.foliagePlacer.serialize(param0))
            .put(param0.createString("height_rand_a"), param0.createInt(this.heightRandA))
            .put(param0.createString("height_rand_b"), param0.createInt(this.heightRandB))
            .put(param0.createString("trunk_height"), param0.createInt(this.trunkHeight))
            .put(param0.createString("trunk_height_random"), param0.createInt(this.trunkHeightRandom))
            .put(param0.createString("trunk_top_offset"), param0.createInt(this.trunkTopOffset))
            .put(param0.createString("trunk_top_offset_random"), param0.createInt(this.trunkTopOffsetRandom))
            .put(param0.createString("foliage_height"), param0.createInt(this.foliageHeight))
            .put(param0.createString("foliage_height_random"), param0.createInt(this.foliageHeightRandom))
            .put(param0.createString("max_water_depth"), param0.createInt(this.maxWaterDepth))
            .put(param0.createString("ignore_vines"), param0.createBoolean(this.ignoreVines));
        Dynamic<T> var1 = new Dynamic<>(param0, param0.createMap(var0.build()));
        return var1.merge(super.serialize(param0));
    }

    public static <T> SmallTreeConfiguration deserialize(Dynamic<T> param0) {
        TreeConfiguration var0 = TreeConfiguration.deserialize(param0);
        FoliagePlacerType<?> var1 = Registry.FOLIAGE_PLACER_TYPES
            .get(new ResourceLocation(param0.get("foliage_placer").get("type").asString().orElseThrow(RuntimeException::new)));
        return new SmallTreeConfiguration(
            var0.trunkProvider,
            var0.leavesProvider,
            var1.deserialize(param0.get("foliage_placer").orElseEmptyMap()),
            var0.decorators,
            var0.baseHeight,
            param0.get("height_rand_a").asInt(0),
            param0.get("height_rand_b").asInt(0),
            param0.get("trunk_height").asInt(-1),
            param0.get("trunk_height_random").asInt(0),
            param0.get("trunk_top_offset").asInt(0),
            param0.get("trunk_top_offset_random").asInt(0),
            param0.get("foliage_height").asInt(-1),
            param0.get("foliage_height_random").asInt(0),
            param0.get("max_water_depth").asInt(0),
            param0.get("ignore_vines").asBoolean(false)
        );
    }

    public static SmallTreeConfiguration random(Random param0) {
        TreeConfiguration var0 = TreeConfiguration.random(param0);
        return new SmallTreeConfiguration(
            var0.trunkProvider,
            var0.leavesProvider,
            Registry.FOLIAGE_PLACER_TYPES.getRandom(param0).random(param0),
            var0.decorators,
            var0.baseHeight,
            param0.nextInt(10) + 1,
            param0.nextInt(10) + 1,
            Math.max(2, var0.baseHeight - param0.nextInt(20)),
            param0.nextInt(5),
            param0.nextInt(10) + 1,
            param0.nextInt(10) + 1,
            param0.nextInt(10) + 1,
            param0.nextInt(10) + 1,
            param0.nextInt(10),
            param0.nextBoolean()
        );
    }

    public static SmallTreeConfiguration fancyRandom(Random param0) {
        SmallTreeConfiguration var0 = random(param0);
        return new SmallTreeConfiguration(
            BlockStateProvider.random(param0, BlockStateProvider.ROTATABLE_BLOCKS),
            var0.leavesProvider,
            var0.foliagePlacer,
            var0.decorators,
            var0.baseHeight,
            var0.heightRandA,
            var0.heightRandB,
            var0.trunkHeight,
            var0.trunkHeightRandom,
            var0.trunkTopOffset,
            var0.trunkTopOffsetRandom,
            var0.foliageHeight,
            var0.foliageHeightRandom,
            var0.maxWaterDepth,
            var0.ignoreVines
        );
    }

    public static class SmallTreeConfigurationBuilder extends TreeConfiguration.TreeConfigurationBuilder {
        private final FoliagePlacer foliagePlacer;
        private List<TreeDecorator> decorators = ImmutableList.of();
        private int baseHeight;
        private int heightRandA;
        private int heightRandB;
        private int trunkHeight = -1;
        private int trunkHeightRandom;
        private int trunkTopOffset;
        private int trunkTopOffsetRandom;
        private int foliageHeight = -1;
        private int foliageHeightRandom;
        private int maxWaterDepth;
        private boolean ignoreVines;

        public SmallTreeConfigurationBuilder(BlockStateProvider param0, BlockStateProvider param1, FoliagePlacer param2) {
            super(param0, param1);
            this.foliagePlacer = param2;
        }

        public SmallTreeConfiguration.SmallTreeConfigurationBuilder decorators(List<TreeDecorator> param0) {
            this.decorators = param0;
            return this;
        }

        public SmallTreeConfiguration.SmallTreeConfigurationBuilder baseHeight(int param0) {
            this.baseHeight = param0;
            return this;
        }

        public SmallTreeConfiguration.SmallTreeConfigurationBuilder heightRandA(int param0) {
            this.heightRandA = param0;
            return this;
        }

        public SmallTreeConfiguration.SmallTreeConfigurationBuilder heightRandB(int param0) {
            this.heightRandB = param0;
            return this;
        }

        public SmallTreeConfiguration.SmallTreeConfigurationBuilder trunkHeight(int param0) {
            this.trunkHeight = param0;
            return this;
        }

        public SmallTreeConfiguration.SmallTreeConfigurationBuilder trunkHeightRandom(int param0) {
            this.trunkHeightRandom = param0;
            return this;
        }

        public SmallTreeConfiguration.SmallTreeConfigurationBuilder trunkTopOffset(int param0) {
            this.trunkTopOffset = param0;
            return this;
        }

        public SmallTreeConfiguration.SmallTreeConfigurationBuilder trunkTopOffsetRandom(int param0) {
            this.trunkTopOffsetRandom = param0;
            return this;
        }

        public SmallTreeConfiguration.SmallTreeConfigurationBuilder foliageHeight(int param0) {
            this.foliageHeight = param0;
            return this;
        }

        public SmallTreeConfiguration.SmallTreeConfigurationBuilder foliageHeightRandom(int param0) {
            this.foliageHeightRandom = param0;
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
                this.trunkProvider,
                this.leavesProvider,
                this.foliagePlacer,
                this.decorators,
                this.baseHeight,
                this.heightRandA,
                this.heightRandB,
                this.trunkHeight,
                this.trunkHeightRandom,
                this.trunkTopOffset,
                this.trunkTopOffsetRandom,
                this.foliageHeight,
                this.foliageHeightRandom,
                this.maxWaterDepth,
                this.ignoreVines
            );
        }
    }
}
