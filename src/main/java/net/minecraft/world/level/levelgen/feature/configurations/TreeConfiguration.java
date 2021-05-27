package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;

public class TreeConfiguration implements FeatureConfiguration {
    public static final Codec<TreeConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter(param0x -> param0x.trunkProvider),
                    TrunkPlacer.CODEC.fieldOf("trunk_placer").forGetter(param0x -> param0x.trunkPlacer),
                    BlockStateProvider.CODEC.fieldOf("foliage_provider").forGetter(param0x -> param0x.foliageProvider),
                    BlockStateProvider.CODEC.fieldOf("sapling_provider").forGetter(param0x -> param0x.saplingProvider),
                    FoliagePlacer.CODEC.fieldOf("foliage_placer").forGetter(param0x -> param0x.foliagePlacer),
                    BlockStateProvider.CODEC.fieldOf("dirt_provider").forGetter(param0x -> param0x.dirtProvider),
                    FeatureSize.CODEC.fieldOf("minimum_size").forGetter(param0x -> param0x.minimumSize),
                    TreeDecorator.CODEC.listOf().fieldOf("decorators").forGetter(param0x -> param0x.decorators),
                    Codec.BOOL.fieldOf("ignore_vines").orElse(false).forGetter(param0x -> param0x.ignoreVines),
                    Codec.BOOL.fieldOf("force_dirt").orElse(false).forGetter(param0x -> param0x.forceDirt)
                )
                .apply(param0, TreeConfiguration::new)
    );
    public final BlockStateProvider trunkProvider;
    public final BlockStateProvider dirtProvider;
    public final TrunkPlacer trunkPlacer;
    public final BlockStateProvider foliageProvider;
    public final BlockStateProvider saplingProvider;
    public final FoliagePlacer foliagePlacer;
    public final FeatureSize minimumSize;
    public final List<TreeDecorator> decorators;
    public final boolean ignoreVines;
    public final boolean forceDirt;

    protected TreeConfiguration(
        BlockStateProvider param0,
        TrunkPlacer param1,
        BlockStateProvider param2,
        BlockStateProvider param3,
        FoliagePlacer param4,
        BlockStateProvider param5,
        FeatureSize param6,
        List<TreeDecorator> param7,
        boolean param8,
        boolean param9
    ) {
        this.trunkProvider = param0;
        this.trunkPlacer = param1;
        this.foliageProvider = param2;
        this.foliagePlacer = param4;
        this.dirtProvider = param5;
        this.saplingProvider = param3;
        this.minimumSize = param6;
        this.decorators = param7;
        this.ignoreVines = param8;
        this.forceDirt = param9;
    }

    public TreeConfiguration withDecorators(List<TreeDecorator> param0) {
        return new TreeConfiguration(
            this.trunkProvider,
            this.trunkPlacer,
            this.foliageProvider,
            this.saplingProvider,
            this.foliagePlacer,
            this.dirtProvider,
            this.minimumSize,
            param0,
            this.ignoreVines,
            this.forceDirt
        );
    }

    public static class TreeConfigurationBuilder {
        public final BlockStateProvider trunkProvider;
        private final TrunkPlacer trunkPlacer;
        public final BlockStateProvider foliageProvider;
        public final BlockStateProvider saplingProvider;
        private final FoliagePlacer foliagePlacer;
        private BlockStateProvider dirtProvider;
        private final FeatureSize minimumSize;
        private List<TreeDecorator> decorators = ImmutableList.of();
        private boolean ignoreVines;
        private boolean forceDirt;

        public TreeConfigurationBuilder(
            BlockStateProvider param0, TrunkPlacer param1, BlockStateProvider param2, BlockStateProvider param3, FoliagePlacer param4, FeatureSize param5
        ) {
            this.trunkProvider = param0;
            this.trunkPlacer = param1;
            this.foliageProvider = param2;
            this.saplingProvider = param3;
            this.dirtProvider = new SimpleStateProvider(Blocks.DIRT.defaultBlockState());
            this.foliagePlacer = param4;
            this.minimumSize = param5;
        }

        public TreeConfiguration.TreeConfigurationBuilder dirt(BlockStateProvider param0) {
            this.dirtProvider = param0;
            return this;
        }

        public TreeConfiguration.TreeConfigurationBuilder decorators(List<TreeDecorator> param0) {
            this.decorators = param0;
            return this;
        }

        public TreeConfiguration.TreeConfigurationBuilder ignoreVines() {
            this.ignoreVines = true;
            return this;
        }

        public TreeConfiguration.TreeConfigurationBuilder forceDirt() {
            this.forceDirt = true;
            return this;
        }

        public TreeConfiguration build() {
            return new TreeConfiguration(
                this.trunkProvider,
                this.trunkPlacer,
                this.foliageProvider,
                this.saplingProvider,
                this.foliagePlacer,
                this.dirtProvider,
                this.minimumSize,
                this.decorators,
                this.ignoreVines,
                this.forceDirt
            );
        }
    }
}
