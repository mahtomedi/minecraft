package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Dynamic;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.BlockBlobConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BuriedTreasureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.CountFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratedFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureRadiusConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MultiJigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceSpheroidConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SeagrassFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.NetherFossilFeature;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;

public abstract class Feature<FC extends FeatureConfiguration> {
    public static final StructureFeature<NoneFeatureConfiguration> PILLAGER_OUTPOST = register(
        "pillager_outpost", new PillagerOutpostFeature(NoneFeatureConfiguration::deserialize)
    );
    public static final StructureFeature<MineshaftConfiguration> MINESHAFT = register("mineshaft", new MineshaftFeature(MineshaftConfiguration::deserialize));
    public static final StructureFeature<NoneFeatureConfiguration> WOODLAND_MANSION = register(
        "woodland_mansion", new WoodlandMansionFeature(NoneFeatureConfiguration::deserialize)
    );
    public static final StructureFeature<NoneFeatureConfiguration> JUNGLE_TEMPLE = register(
        "jungle_temple", new JunglePyramidFeature(NoneFeatureConfiguration::deserialize)
    );
    public static final StructureFeature<NoneFeatureConfiguration> DESERT_PYRAMID = register(
        "desert_pyramid", new DesertPyramidFeature(NoneFeatureConfiguration::deserialize)
    );
    public static final StructureFeature<NoneFeatureConfiguration> IGLOO = register("igloo", new IglooFeature(NoneFeatureConfiguration::deserialize));
    public static final StructureFeature<RuinedPortalConfiguration> RUINED_PORTAL = register(
        "ruined_portal", new RuinedPortalFeature(RuinedPortalConfiguration::deserialize)
    );
    public static final StructureFeature<ShipwreckConfiguration> SHIPWRECK = register("shipwreck", new ShipwreckFeature(ShipwreckConfiguration::deserialize));
    public static final SwamplandHutFeature SWAMP_HUT = register("swamp_hut", new SwamplandHutFeature(NoneFeatureConfiguration::deserialize));
    public static final StructureFeature<NoneFeatureConfiguration> STRONGHOLD = register(
        "stronghold", new StrongholdFeature(NoneFeatureConfiguration::deserialize)
    );
    public static final StructureFeature<NoneFeatureConfiguration> OCEAN_MONUMENT = register(
        "ocean_monument", new OceanMonumentFeature(NoneFeatureConfiguration::deserialize)
    );
    public static final StructureFeature<OceanRuinConfiguration> OCEAN_RUIN = register("ocean_ruin", new OceanRuinFeature(OceanRuinConfiguration::deserialize));
    public static final StructureFeature<NoneFeatureConfiguration> NETHER_BRIDGE = register(
        "nether_bridge", new NetherFortressFeature(NoneFeatureConfiguration::deserialize)
    );
    public static final StructureFeature<NoneFeatureConfiguration> END_CITY = register("end_city", new EndCityFeature(NoneFeatureConfiguration::deserialize));
    public static final StructureFeature<BuriedTreasureConfiguration> BURIED_TREASURE = register(
        "buried_treasure", new BuriedTreasureFeature(BuriedTreasureConfiguration::deserialize)
    );
    public static final StructureFeature<JigsawConfiguration> VILLAGE = register("village", new VillageFeature(JigsawConfiguration::deserialize));
    public static final StructureFeature<NoneFeatureConfiguration> NETHER_FOSSIL = register(
        "nether_fossil", new NetherFossilFeature(NoneFeatureConfiguration::deserialize)
    );
    public static final StructureFeature<MultiJigsawConfiguration> BASTION_REMNANT = register(
        "bastion_remnant", new BastionFeature(MultiJigsawConfiguration::deserialize)
    );
    public static final Feature<NoneFeatureConfiguration> NO_OP = register("no_op", new NoOpFeature(NoneFeatureConfiguration::deserialize));
    public static final Feature<TreeConfiguration> TREE = register("tree", new TreeFeature(TreeConfiguration::deserialize));
    public static final AbstractFlowerFeature<RandomPatchConfiguration> FLOWER = register(
        "flower", new DefaultFlowerFeature(RandomPatchConfiguration::deserialize)
    );
    public static final Feature<RandomPatchConfiguration> RANDOM_PATCH = register("random_patch", new RandomPatchFeature(RandomPatchConfiguration::deserialize));
    public static final Feature<BlockPileConfiguration> BLOCK_PILE = register("block_pile", new BlockPileFeature(BlockPileConfiguration::deserialize));
    public static final Feature<SpringConfiguration> SPRING = register("spring_feature", new SpringFeature(SpringConfiguration::deserialize));
    public static final Feature<NoneFeatureConfiguration> CHORUS_PLANT = register("chorus_plant", new ChorusPlantFeature(NoneFeatureConfiguration::deserialize));
    public static final Feature<ReplaceBlockConfiguration> EMERALD_ORE = register(
        "emerald_ore", new ReplaceBlockFeature(ReplaceBlockConfiguration::deserialize)
    );
    public static final Feature<NoneFeatureConfiguration> VOID_START_PLATFORM = register(
        "void_start_platform", new VoidStartPlatformFeature(NoneFeatureConfiguration::deserialize)
    );
    public static final Feature<NoneFeatureConfiguration> DESERT_WELL = register("desert_well", new DesertWellFeature(NoneFeatureConfiguration::deserialize));
    public static final Feature<NoneFeatureConfiguration> FOSSIL = register("fossil", new FossilFeature(NoneFeatureConfiguration::deserialize));
    public static final Feature<HugeMushroomFeatureConfiguration> HUGE_RED_MUSHROOM = register(
        "huge_red_mushroom", new HugeRedMushroomFeature(HugeMushroomFeatureConfiguration::deserialize)
    );
    public static final Feature<HugeMushroomFeatureConfiguration> HUGE_BROWN_MUSHROOM = register(
        "huge_brown_mushroom", new HugeBrownMushroomFeature(HugeMushroomFeatureConfiguration::deserialize)
    );
    public static final Feature<NoneFeatureConfiguration> ICE_SPIKE = register("ice_spike", new IceSpikeFeature(NoneFeatureConfiguration::deserialize));
    public static final Feature<NoneFeatureConfiguration> GLOWSTONE_BLOB = register(
        "glowstone_blob", new GlowstoneFeature(NoneFeatureConfiguration::deserialize)
    );
    public static final Feature<NoneFeatureConfiguration> FREEZE_TOP_LAYER = register(
        "freeze_top_layer", new SnowAndFreezeFeature(NoneFeatureConfiguration::deserialize)
    );
    public static final Feature<NoneFeatureConfiguration> VINES = register("vines", new VinesFeature(NoneFeatureConfiguration::deserialize));
    public static final Feature<NoneFeatureConfiguration> MONSTER_ROOM = register("monster_room", new MonsterRoomFeature(NoneFeatureConfiguration::deserialize));
    public static final Feature<NoneFeatureConfiguration> BLUE_ICE = register("blue_ice", new BlueIceFeature(NoneFeatureConfiguration::deserialize));
    public static final Feature<BlockStateConfiguration> ICEBERG = register("iceberg", new IcebergFeature(BlockStateConfiguration::deserialize));
    public static final Feature<BlockBlobConfiguration> FOREST_ROCK = register("forest_rock", new BlockBlobFeature(BlockBlobConfiguration::deserialize));
    public static final Feature<DiskConfiguration> DISK = register("disk", new DiskReplaceFeature(DiskConfiguration::deserialize));
    public static final Feature<FeatureRadiusConfiguration> ICE_PATCH = register("ice_patch", new IcePatchFeature(FeatureRadiusConfiguration::deserialize));
    public static final Feature<BlockStateConfiguration> LAKE = register("lake", new LakeFeature(BlockStateConfiguration::deserialize));
    public static final Feature<OreConfiguration> ORE = register("ore", new OreFeature(OreConfiguration::deserialize));
    public static final Feature<SpikeConfiguration> END_SPIKE = register("end_spike", new SpikeFeature(SpikeConfiguration::deserialize));
    public static final Feature<NoneFeatureConfiguration> END_ISLAND = register("end_island", new EndIslandFeature(NoneFeatureConfiguration::deserialize));
    public static final Feature<EndGatewayConfiguration> END_GATEWAY = register("end_gateway", new EndGatewayFeature(EndGatewayConfiguration::deserialize));
    public static final Feature<SeagrassFeatureConfiguration> SEAGRASS = register("seagrass", new SeagrassFeature(SeagrassFeatureConfiguration::deserialize));
    public static final Feature<NoneFeatureConfiguration> KELP = register("kelp", new KelpFeature(NoneFeatureConfiguration::deserialize));
    public static final Feature<NoneFeatureConfiguration> CORAL_TREE = register("coral_tree", new CoralTreeFeature(NoneFeatureConfiguration::deserialize));
    public static final Feature<NoneFeatureConfiguration> CORAL_MUSHROOM = register(
        "coral_mushroom", new CoralMushroomFeature(NoneFeatureConfiguration::deserialize)
    );
    public static final Feature<NoneFeatureConfiguration> CORAL_CLAW = register("coral_claw", new CoralClawFeature(NoneFeatureConfiguration::deserialize));
    public static final Feature<CountFeatureConfiguration> SEA_PICKLE = register("sea_pickle", new SeaPickleFeature(CountFeatureConfiguration::deserialize));
    public static final Feature<SimpleBlockConfiguration> SIMPLE_BLOCK = register("simple_block", new SimpleBlockFeature(SimpleBlockConfiguration::deserialize));
    public static final Feature<ProbabilityFeatureConfiguration> BAMBOO = register("bamboo", new BambooFeature(ProbabilityFeatureConfiguration::deserialize));
    public static final Feature<HugeFungusConfiguration> HUGE_FUNGUS = register("huge_fungus", new HugeFungusFeature(HugeFungusConfiguration::deserialize));
    public static final Feature<BlockPileConfiguration> NETHER_FOREST_VEGETATION = register(
        "nether_forest_vegetation", new NetherForestVegetationFeature(BlockPileConfiguration::deserialize)
    );
    public static final Feature<NoneFeatureConfiguration> WEEPING_VINES = register(
        "weeping_vines", new WeepingVinesFeature(NoneFeatureConfiguration::deserialize)
    );
    public static final Feature<NoneFeatureConfiguration> TWISTING_VINES = register(
        "twisting_vines", new TwistingVinesFeature(NoneFeatureConfiguration::deserialize)
    );
    public static final Feature<ColumnFeatureConfiguration> BASALT_COLUMNS = register(
        "basalt_columns", new BasaltColumnsFeature(ColumnFeatureConfiguration::deserialize)
    );
    public static final Feature<DeltaFeatureConfiguration> DELTA_FEATURE = register("delta_feature", new DeltaFeature(DeltaFeatureConfiguration::deserialize));
    public static final Feature<ReplaceSpheroidConfiguration> REPLACE_BLOBS = register(
        "netherrack_replace_blobs", new ReplaceBlobsFeature(ReplaceSpheroidConfiguration::deserialize)
    );
    public static final Feature<LayerConfiguration> FILL_LAYER = register("fill_layer", new FillLayerFeature(LayerConfiguration::deserialize));
    public static final BonusChestFeature BONUS_CHEST = register("bonus_chest", new BonusChestFeature(NoneFeatureConfiguration::deserialize));
    public static final Feature<NoneFeatureConfiguration> BASALT_PILLAR = register(
        "basalt_pillar", new BasaltPillarFeature(NoneFeatureConfiguration::deserialize)
    );
    public static final Feature<OreConfiguration> NO_SURFACE_ORE = register("no_surface_ore", new NoSurfaceOreFeature(OreConfiguration::deserialize));
    public static final Feature<RandomRandomFeatureConfiguration> RANDOM_RANDOM_SELECTOR = register(
        "random_random_selector", new RandomRandomFeature(RandomRandomFeatureConfiguration::deserialize)
    );
    public static final Feature<RandomFeatureConfiguration> RANDOM_SELECTOR = register(
        "random_selector", new RandomSelectorFeature(RandomFeatureConfiguration::deserialize)
    );
    public static final Feature<SimpleRandomFeatureConfiguration> SIMPLE_RANDOM_SELECTOR = register(
        "simple_random_selector", new SimpleRandomSelectorFeature(SimpleRandomFeatureConfiguration::deserialize)
    );
    public static final Feature<RandomBooleanFeatureConfiguration> RANDOM_BOOLEAN_SELECTOR = register(
        "random_boolean_selector", new RandomBooleanSelectorFeature(RandomBooleanFeatureConfiguration::deserialize)
    );
    public static final Feature<DecoratedFeatureConfiguration> DECORATED = register(
        "decorated", new DecoratedFeature(DecoratedFeatureConfiguration::deserialize)
    );
    public static final Feature<DecoratedFeatureConfiguration> DECORATED_FLOWER = register(
        "decorated_flower", new DecoratedFlowerFeature(DecoratedFeatureConfiguration::deserialize)
    );
    public static final BiMap<String, StructureFeature<?>> STRUCTURES_REGISTRY = Util.make(HashBiMap.create(), param0 -> {
        param0.put("Pillager_Outpost".toLowerCase(Locale.ROOT), PILLAGER_OUTPOST);
        param0.put("Mineshaft".toLowerCase(Locale.ROOT), MINESHAFT);
        param0.put("Mansion".toLowerCase(Locale.ROOT), WOODLAND_MANSION);
        param0.put("Jungle_Pyramid".toLowerCase(Locale.ROOT), JUNGLE_TEMPLE);
        param0.put("Desert_Pyramid".toLowerCase(Locale.ROOT), DESERT_PYRAMID);
        param0.put("Igloo".toLowerCase(Locale.ROOT), IGLOO);
        param0.put("Ruined_Portal".toLowerCase(Locale.ROOT), RUINED_PORTAL);
        param0.put("Shipwreck".toLowerCase(Locale.ROOT), SHIPWRECK);
        param0.put("Swamp_Hut".toLowerCase(Locale.ROOT), SWAMP_HUT);
        param0.put("Stronghold".toLowerCase(Locale.ROOT), STRONGHOLD);
        param0.put("Monument".toLowerCase(Locale.ROOT), OCEAN_MONUMENT);
        param0.put("Ocean_Ruin".toLowerCase(Locale.ROOT), OCEAN_RUIN);
        param0.put("Fortress".toLowerCase(Locale.ROOT), NETHER_BRIDGE);
        param0.put("EndCity".toLowerCase(Locale.ROOT), END_CITY);
        param0.put("Buried_Treasure".toLowerCase(Locale.ROOT), BURIED_TREASURE);
        param0.put("Village".toLowerCase(Locale.ROOT), VILLAGE);
        param0.put("Nether_Fossil".toLowerCase(Locale.ROOT), NETHER_FOSSIL);
        param0.put("Bastion_Remnant".toLowerCase(Locale.ROOT), BASTION_REMNANT);
    });
    public static final List<StructureFeature<?>> NOISE_AFFECTING_FEATURES = ImmutableList.of(PILLAGER_OUTPOST, VILLAGE, NETHER_FOSSIL);
    private final Function<Dynamic<?>, ? extends FC> configurationFactory;

    private static <C extends FeatureConfiguration, F extends Feature<C>> F register(String param0, F param1) {
        return Registry.register(Registry.FEATURE, param0, param1);
    }

    public Feature(Function<Dynamic<?>, ? extends FC> param0) {
        this.configurationFactory = param0;
    }

    public ConfiguredFeature<FC, ?> configured(FC param0) {
        return new ConfiguredFeature<>(this, param0);
    }

    public FC createSettings(Dynamic<?> param0) {
        return this.configurationFactory.apply(param0);
    }

    protected void setBlock(LevelWriter param0, BlockPos param1, BlockState param2) {
        param0.setBlock(param1, param2, 3);
    }

    public abstract boolean place(
        LevelAccessor var1, StructureFeatureManager var2, ChunkGenerator<? extends ChunkGeneratorSettings> var3, Random var4, BlockPos var5, FC var6
    );

    public List<Biome.SpawnerData> getSpecialEnemies() {
        return Collections.emptyList();
    }

    public List<Biome.SpawnerData> getSpecialAnimals() {
        return Collections.emptyList();
    }

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
