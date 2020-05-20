package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.BlockBlobConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.CountFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratedFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureRadiusConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceSpheroidConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SeagrassFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public abstract class Feature<FC extends FeatureConfiguration> {
    public static final Feature<NoneFeatureConfiguration> NO_OP = register("no_op", new NoOpFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<TreeConfiguration> TREE = register("tree", new TreeFeature(TreeConfiguration.CODEC));
    public static final AbstractFlowerFeature<RandomPatchConfiguration> FLOWER = register("flower", new DefaultFlowerFeature(RandomPatchConfiguration.CODEC));
    public static final Feature<RandomPatchConfiguration> RANDOM_PATCH = register("random_patch", new RandomPatchFeature(RandomPatchConfiguration.CODEC));
    public static final Feature<BlockPileConfiguration> BLOCK_PILE = register("block_pile", new BlockPileFeature(BlockPileConfiguration.CODEC));
    public static final Feature<SpringConfiguration> SPRING = register("spring_feature", new SpringFeature(SpringConfiguration.CODEC));
    public static final Feature<NoneFeatureConfiguration> CHORUS_PLANT = register("chorus_plant", new ChorusPlantFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<ReplaceBlockConfiguration> EMERALD_ORE = register("emerald_ore", new ReplaceBlockFeature(ReplaceBlockConfiguration.CODEC));
    public static final Feature<NoneFeatureConfiguration> VOID_START_PLATFORM = register(
        "void_start_platform", new VoidStartPlatformFeature(NoneFeatureConfiguration.CODEC)
    );
    public static final Feature<NoneFeatureConfiguration> DESERT_WELL = register("desert_well", new DesertWellFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<NoneFeatureConfiguration> FOSSIL = register("fossil", new FossilFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<HugeMushroomFeatureConfiguration> HUGE_RED_MUSHROOM = register(
        "huge_red_mushroom", new HugeRedMushroomFeature(HugeMushroomFeatureConfiguration.CODEC)
    );
    public static final Feature<HugeMushroomFeatureConfiguration> HUGE_BROWN_MUSHROOM = register(
        "huge_brown_mushroom", new HugeBrownMushroomFeature(HugeMushroomFeatureConfiguration.CODEC)
    );
    public static final Feature<NoneFeatureConfiguration> ICE_SPIKE = register("ice_spike", new IceSpikeFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<NoneFeatureConfiguration> GLOWSTONE_BLOB = register("glowstone_blob", new GlowstoneFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<NoneFeatureConfiguration> FREEZE_TOP_LAYER = register(
        "freeze_top_layer", new SnowAndFreezeFeature(NoneFeatureConfiguration.CODEC)
    );
    public static final Feature<NoneFeatureConfiguration> VINES = register("vines", new VinesFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<NoneFeatureConfiguration> MONSTER_ROOM = register("monster_room", new MonsterRoomFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<NoneFeatureConfiguration> BLUE_ICE = register("blue_ice", new BlueIceFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<BlockStateConfiguration> ICEBERG = register("iceberg", new IcebergFeature(BlockStateConfiguration.CODEC));
    public static final Feature<BlockBlobConfiguration> FOREST_ROCK = register("forest_rock", new BlockBlobFeature(BlockBlobConfiguration.CODEC));
    public static final Feature<DiskConfiguration> DISK = register("disk", new DiskReplaceFeature(DiskConfiguration.CODEC));
    public static final Feature<FeatureRadiusConfiguration> ICE_PATCH = register("ice_patch", new IcePatchFeature(FeatureRadiusConfiguration.CODEC));
    public static final Feature<BlockStateConfiguration> LAKE = register("lake", new LakeFeature(BlockStateConfiguration.CODEC));
    public static final Feature<OreConfiguration> ORE = register("ore", new OreFeature(OreConfiguration.CODEC));
    public static final Feature<SpikeConfiguration> END_SPIKE = register("end_spike", new SpikeFeature(SpikeConfiguration.CODEC));
    public static final Feature<NoneFeatureConfiguration> END_ISLAND = register("end_island", new EndIslandFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<EndGatewayConfiguration> END_GATEWAY = register("end_gateway", new EndGatewayFeature(EndGatewayConfiguration.CODEC));
    public static final Feature<SeagrassFeatureConfiguration> SEAGRASS = register("seagrass", new SeagrassFeature(SeagrassFeatureConfiguration.CODEC));
    public static final Feature<NoneFeatureConfiguration> KELP = register("kelp", new KelpFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<NoneFeatureConfiguration> CORAL_TREE = register("coral_tree", new CoralTreeFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<NoneFeatureConfiguration> CORAL_MUSHROOM = register("coral_mushroom", new CoralMushroomFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<NoneFeatureConfiguration> CORAL_CLAW = register("coral_claw", new CoralClawFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<CountFeatureConfiguration> SEA_PICKLE = register("sea_pickle", new SeaPickleFeature(CountFeatureConfiguration.CODEC));
    public static final Feature<SimpleBlockConfiguration> SIMPLE_BLOCK = register("simple_block", new SimpleBlockFeature(SimpleBlockConfiguration.CODEC));
    public static final Feature<ProbabilityFeatureConfiguration> BAMBOO = register("bamboo", new BambooFeature(ProbabilityFeatureConfiguration.CODEC));
    public static final Feature<HugeFungusConfiguration> HUGE_FUNGUS = register("huge_fungus", new HugeFungusFeature(HugeFungusConfiguration.CODEC));
    public static final Feature<BlockPileConfiguration> NETHER_FOREST_VEGETATION = register(
        "nether_forest_vegetation", new NetherForestVegetationFeature(BlockPileConfiguration.CODEC)
    );
    public static final Feature<NoneFeatureConfiguration> WEEPING_VINES = register("weeping_vines", new WeepingVinesFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<NoneFeatureConfiguration> TWISTING_VINES = register("twisting_vines", new TwistingVinesFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<ColumnFeatureConfiguration> BASALT_COLUMNS = register(
        "basalt_columns", new BasaltColumnsFeature(ColumnFeatureConfiguration.CODEC)
    );
    public static final Feature<DeltaFeatureConfiguration> DELTA_FEATURE = register("delta_feature", new DeltaFeature(DeltaFeatureConfiguration.CODEC));
    public static final Feature<ReplaceSpheroidConfiguration> REPLACE_BLOBS = register(
        "netherrack_replace_blobs", new ReplaceBlobsFeature(ReplaceSpheroidConfiguration.CODEC)
    );
    public static final Feature<LayerConfiguration> FILL_LAYER = register("fill_layer", new FillLayerFeature(LayerConfiguration.CODEC));
    public static final BonusChestFeature BONUS_CHEST = register("bonus_chest", new BonusChestFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<NoneFeatureConfiguration> BASALT_PILLAR = register("basalt_pillar", new BasaltPillarFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<OreConfiguration> NO_SURFACE_ORE = register("no_surface_ore", new NoSurfaceOreFeature(OreConfiguration.CODEC));
    public static final Feature<RandomRandomFeatureConfiguration> RANDOM_RANDOM_SELECTOR = register(
        "random_random_selector", new RandomRandomFeature(RandomRandomFeatureConfiguration.CODEC)
    );
    public static final Feature<RandomFeatureConfiguration> RANDOM_SELECTOR = register(
        "random_selector", new RandomSelectorFeature(RandomFeatureConfiguration.CODEC)
    );
    public static final Feature<SimpleRandomFeatureConfiguration> SIMPLE_RANDOM_SELECTOR = register(
        "simple_random_selector", new SimpleRandomSelectorFeature(SimpleRandomFeatureConfiguration.CODEC)
    );
    public static final Feature<RandomBooleanFeatureConfiguration> RANDOM_BOOLEAN_SELECTOR = register(
        "random_boolean_selector", new RandomBooleanSelectorFeature(RandomBooleanFeatureConfiguration.CODEC)
    );
    public static final Feature<DecoratedFeatureConfiguration> DECORATED = register("decorated", new DecoratedFeature(DecoratedFeatureConfiguration.CODEC));
    public static final Feature<DecoratedFeatureConfiguration> DECORATED_FLOWER = register(
        "decorated_flower", new DecoratedFlowerFeature(DecoratedFeatureConfiguration.CODEC)
    );
    private final Codec<ConfiguredFeature<FC, Feature<FC>>> configuredCodec;

    private static <C extends FeatureConfiguration, F extends Feature<C>> F register(String param0, F param1) {
        return Registry.register(Registry.FEATURE, param0, param1);
    }

    public Feature(Codec<FC> param0) {
        this.configuredCodec = param0.fieldOf("config").xmap(param0x -> new ConfiguredFeature<>(this, param0x), param0x -> param0x.config).codec();
    }

    public Codec<ConfiguredFeature<FC, Feature<FC>>> configuredCodec() {
        return this.configuredCodec;
    }

    public ConfiguredFeature<FC, ?> configured(FC param0) {
        return new ConfiguredFeature<>(this, param0);
    }

    protected void setBlock(LevelWriter param0, BlockPos param1, BlockState param2) {
        param0.setBlock(param1, param2, 3);
    }

    public abstract boolean place(WorldGenLevel var1, StructureFeatureManager var2, ChunkGenerator var3, Random var4, BlockPos var5, FC var6);

    protected static boolean isStone(Block param0) {
        return param0 == Blocks.STONE || param0 == Blocks.GRANITE || param0 == Blocks.DIORITE || param0 == Blocks.ANDESITE;
    }

    public static boolean isDirt(Block param0) {
        return param0 == Blocks.DIRT || param0 == Blocks.GRASS_BLOCK || param0 == Blocks.PODZOL || param0 == Blocks.COARSE_DIRT || param0 == Blocks.MYCELIUM;
    }

    public static boolean isGrassOrDirt(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> isDirt(param0x.getBlock()));
    }

    public static boolean isAir(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, BlockBehaviour.BlockStateBase::isAir);
    }
}
