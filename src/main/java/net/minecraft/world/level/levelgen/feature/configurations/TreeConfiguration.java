package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;

public class TreeConfiguration implements FeatureConfiguration {
    public static final Codec<TreeConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter(param0x -> param0x.trunkProvider),
                    BlockStateProvider.CODEC.fieldOf("leaves_provider").forGetter(param0x -> param0x.leavesProvider),
                    FoliagePlacer.CODEC.fieldOf("foliage_placer").forGetter(param0x -> param0x.foliagePlacer),
                    TrunkPlacer.CODEC.fieldOf("trunk_placer").forGetter(param0x -> param0x.trunkPlacer),
                    FeatureSize.CODEC.fieldOf("minimum_size").forGetter(param0x -> param0x.minimumSize),
                    TreeDecorator.CODEC.listOf().fieldOf("decorators").forGetter(param0x -> param0x.decorators),
                    Codec.INT.fieldOf("max_water_depth").withDefault(0).forGetter(param0x -> param0x.maxWaterDepth),
                    Codec.BOOL.fieldOf("ignore_vines").withDefault(false).forGetter(param0x -> param0x.ignoreVines),
                    Heightmap.Types.CODEC.fieldOf("heightmap").forGetter(param0x -> param0x.heightmap)
                )
                .apply(param0, TreeConfiguration::new)
    );
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
