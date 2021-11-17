package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.RuinedPortalFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;

public class StructureFeatures {
    private static final ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> PILLAGER_OUTPOST = register(
        "pillager_outpost", StructureFeature.PILLAGER_OUTPOST.configured(new JigsawConfiguration(() -> PillagerOutpostPools.START, 7))
    );
    private static final ConfiguredStructureFeature<MineshaftConfiguration, ? extends StructureFeature<MineshaftConfiguration>> MINESHAFT = register(
        "mineshaft", StructureFeature.MINESHAFT.configured(new MineshaftConfiguration(0.004F, MineshaftFeature.Type.NORMAL))
    );
    private static final ConfiguredStructureFeature<MineshaftConfiguration, ? extends StructureFeature<MineshaftConfiguration>> MINESHAFT_MESA = register(
        "mineshaft_mesa", StructureFeature.MINESHAFT.configured(new MineshaftConfiguration(0.004F, MineshaftFeature.Type.MESA))
    );
    private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> WOODLAND_MANSION = register(
        "mansion", StructureFeature.WOODLAND_MANSION.configured(NoneFeatureConfiguration.INSTANCE)
    );
    private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> JUNGLE_TEMPLE = register(
        "jungle_pyramid", StructureFeature.JUNGLE_TEMPLE.configured(NoneFeatureConfiguration.INSTANCE)
    );
    private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> DESERT_PYRAMID = register(
        "desert_pyramid", StructureFeature.DESERT_PYRAMID.configured(NoneFeatureConfiguration.INSTANCE)
    );
    private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> IGLOO = register(
        "igloo", StructureFeature.IGLOO.configured(NoneFeatureConfiguration.INSTANCE)
    );
    private static final ConfiguredStructureFeature<ShipwreckConfiguration, ? extends StructureFeature<ShipwreckConfiguration>> SHIPWRECK = register(
        "shipwreck", StructureFeature.SHIPWRECK.configured(new ShipwreckConfiguration(false))
    );
    private static final ConfiguredStructureFeature<ShipwreckConfiguration, ? extends StructureFeature<ShipwreckConfiguration>> SHIPWRECK_BEACHED = register(
        "shipwreck_beached", StructureFeature.SHIPWRECK.configured(new ShipwreckConfiguration(true))
    );
    private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> SWAMP_HUT = register(
        "swamp_hut", StructureFeature.SWAMP_HUT.configured(NoneFeatureConfiguration.INSTANCE)
    );
    public static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> STRONGHOLD = register(
        "stronghold", StructureFeature.STRONGHOLD.configured(NoneFeatureConfiguration.INSTANCE)
    );
    private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> OCEAN_MONUMENT = register(
        "monument", StructureFeature.OCEAN_MONUMENT.configured(NoneFeatureConfiguration.INSTANCE)
    );
    private static final ConfiguredStructureFeature<OceanRuinConfiguration, ? extends StructureFeature<OceanRuinConfiguration>> OCEAN_RUIN_COLD = register(
        "ocean_ruin_cold", StructureFeature.OCEAN_RUIN.configured(new OceanRuinConfiguration(OceanRuinFeature.Type.COLD, 0.3F, 0.9F))
    );
    private static final ConfiguredStructureFeature<OceanRuinConfiguration, ? extends StructureFeature<OceanRuinConfiguration>> OCEAN_RUIN_WARM = register(
        "ocean_ruin_warm", StructureFeature.OCEAN_RUIN.configured(new OceanRuinConfiguration(OceanRuinFeature.Type.WARM, 0.3F, 0.9F))
    );
    private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> NETHER_BRIDGE = register(
        "fortress", StructureFeature.NETHER_BRIDGE.configured(NoneFeatureConfiguration.INSTANCE)
    );
    private static final ConfiguredStructureFeature<RangeConfiguration, ? extends StructureFeature<RangeConfiguration>> NETHER_FOSSIL = register(
        "nether_fossil",
        StructureFeature.NETHER_FOSSIL.configured(new RangeConfiguration(UniformHeight.of(VerticalAnchor.absolute(32), VerticalAnchor.belowTop(2))))
    );
    private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> END_CITY = register(
        "end_city", StructureFeature.END_CITY.configured(NoneFeatureConfiguration.INSTANCE)
    );
    private static final ConfiguredStructureFeature<ProbabilityFeatureConfiguration, ? extends StructureFeature<ProbabilityFeatureConfiguration>> BURIED_TREASURE = register(
        "buried_treasure", StructureFeature.BURIED_TREASURE.configured(new ProbabilityFeatureConfiguration(0.01F))
    );
    private static final ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> BASTION_REMNANT = register(
        "bastion_remnant", StructureFeature.BASTION_REMNANT.configured(new JigsawConfiguration(() -> BastionPieces.START, 6))
    );
    private static final ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> VILLAGE_PLAINS = register(
        "village_plains", StructureFeature.VILLAGE.configured(new JigsawConfiguration(() -> PlainVillagePools.START, 6))
    );
    private static final ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> VILLAGE_DESERT = register(
        "village_desert", StructureFeature.VILLAGE.configured(new JigsawConfiguration(() -> DesertVillagePools.START, 6))
    );
    private static final ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> VILLAGE_SAVANNA = register(
        "village_savanna", StructureFeature.VILLAGE.configured(new JigsawConfiguration(() -> SavannaVillagePools.START, 6))
    );
    private static final ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> VILLAGE_SNOWY = register(
        "village_snowy", StructureFeature.VILLAGE.configured(new JigsawConfiguration(() -> SnowyVillagePools.START, 6))
    );
    private static final ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> VILLAGE_TAIGA = register(
        "village_taiga", StructureFeature.VILLAGE.configured(new JigsawConfiguration(() -> TaigaVillagePools.START, 6))
    );
    private static final ConfiguredStructureFeature<RuinedPortalConfiguration, ? extends StructureFeature<RuinedPortalConfiguration>> RUINED_PORTAL_STANDARD = register(
        "ruined_portal", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.STANDARD))
    );
    private static final ConfiguredStructureFeature<RuinedPortalConfiguration, ? extends StructureFeature<RuinedPortalConfiguration>> RUINED_PORTAL_DESERT = register(
        "ruined_portal_desert", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.DESERT))
    );
    private static final ConfiguredStructureFeature<RuinedPortalConfiguration, ? extends StructureFeature<RuinedPortalConfiguration>> RUINED_PORTAL_JUNGLE = register(
        "ruined_portal_jungle", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.JUNGLE))
    );
    private static final ConfiguredStructureFeature<RuinedPortalConfiguration, ? extends StructureFeature<RuinedPortalConfiguration>> RUINED_PORTAL_SWAMP = register(
        "ruined_portal_swamp", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.SWAMP))
    );
    private static final ConfiguredStructureFeature<RuinedPortalConfiguration, ? extends StructureFeature<RuinedPortalConfiguration>> RUINED_PORTAL_MOUNTAIN = register(
        "ruined_portal_mountain", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.MOUNTAIN))
    );
    private static final ConfiguredStructureFeature<RuinedPortalConfiguration, ? extends StructureFeature<RuinedPortalConfiguration>> RUINED_PORTAL_OCEAN = register(
        "ruined_portal_ocean", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.OCEAN))
    );
    private static final ConfiguredStructureFeature<RuinedPortalConfiguration, ? extends StructureFeature<RuinedPortalConfiguration>> RUINED_PORTAL_NETHER = register(
        "ruined_portal_nether", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.NETHER))
    );

    public static ConfiguredStructureFeature<?, ?> bootstrap() {
        return MINESHAFT;
    }

    private static <FC extends FeatureConfiguration, F extends StructureFeature<FC>> ConfiguredStructureFeature<FC, F> register(
        String param0, ConfiguredStructureFeature<FC, F> param1
    ) {
        return BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, param0, param1);
    }

    private static void register(
        BiConsumer<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> param0, ConfiguredStructureFeature<?, ?> param1, Set<ResourceKey<Biome>> param2
    ) {
        param2.forEach(param2x -> param0.accept(param1, param2x));
    }

    private static void register(
        BiConsumer<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> param0, ConfiguredStructureFeature<?, ?> param1, ResourceKey<Biome> param2
    ) {
        param0.accept(param1, param2);
    }

    public static void registerStructures(BiConsumer<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> param0) {
        Set<ResourceKey<Biome>> var0 = ImmutableSet.<ResourceKey<Biome>>builder()
            .add(Biomes.DEEP_FROZEN_OCEAN)
            .add(Biomes.DEEP_COLD_OCEAN)
            .add(Biomes.DEEP_OCEAN)
            .add(Biomes.DEEP_LUKEWARM_OCEAN)
            .build();
        Set<ResourceKey<Biome>> var1 = ImmutableSet.<ResourceKey<Biome>>builder()
            .add(Biomes.FROZEN_OCEAN)
            .add(Biomes.OCEAN)
            .add(Biomes.COLD_OCEAN)
            .add(Biomes.LUKEWARM_OCEAN)
            .add(Biomes.WARM_OCEAN)
            .addAll(var0)
            .build();
        Set<ResourceKey<Biome>> var2 = ImmutableSet.<ResourceKey<Biome>>builder().add(Biomes.BEACH).add(Biomes.SNOWY_BEACH).build();
        Set<ResourceKey<Biome>> var3 = ImmutableSet.<ResourceKey<Biome>>builder().add(Biomes.RIVER).add(Biomes.FROZEN_RIVER).build();
        Set<ResourceKey<Biome>> var4 = ImmutableSet.<ResourceKey<Biome>>builder()
            .add(Biomes.MEADOW)
            .add(Biomes.FROZEN_PEAKS)
            .add(Biomes.JAGGED_PEAKS)
            .add(Biomes.STONY_PEAKS)
            .add(Biomes.SNOWY_SLOPES)
            .build();
        Set<ResourceKey<Biome>> var5 = ImmutableSet.<ResourceKey<Biome>>builder()
            .add(Biomes.BADLANDS)
            .add(Biomes.ERODED_BADLANDS)
            .add(Biomes.WOODED_BADLANDS)
            .build();
        Set<ResourceKey<Biome>> var6 = ImmutableSet.<ResourceKey<Biome>>builder()
            .add(Biomes.WINDSWEPT_HILLS)
            .add(Biomes.WINDSWEPT_FOREST)
            .add(Biomes.WINDSWEPT_GRAVELLY_HILLS)
            .build();
        Set<ResourceKey<Biome>> var7 = ImmutableSet.<ResourceKey<Biome>>builder()
            .add(Biomes.TAIGA)
            .add(Biomes.SNOWY_TAIGA)
            .add(Biomes.OLD_GROWTH_PINE_TAIGA)
            .add(Biomes.OLD_GROWTH_SPRUCE_TAIGA)
            .build();
        Set<ResourceKey<Biome>> var8 = ImmutableSet.<ResourceKey<Biome>>builder()
            .add(Biomes.BAMBOO_JUNGLE)
            .add(Biomes.JUNGLE)
            .add(Biomes.SPARSE_JUNGLE)
            .build();
        Set<ResourceKey<Biome>> var9 = ImmutableSet.<ResourceKey<Biome>>builder()
            .add(Biomes.FOREST)
            .add(Biomes.FLOWER_FOREST)
            .add(Biomes.BIRCH_FOREST)
            .add(Biomes.OLD_GROWTH_BIRCH_FOREST)
            .add(Biomes.DARK_FOREST)
            .add(Biomes.GROVE)
            .build();
        Set<ResourceKey<Biome>> var10 = ImmutableSet.<ResourceKey<Biome>>builder()
            .add(Biomes.NETHER_WASTES)
            .add(Biomes.BASALT_DELTAS)
            .add(Biomes.SOUL_SAND_VALLEY)
            .add(Biomes.CRIMSON_FOREST)
            .add(Biomes.WARPED_FOREST)
            .build();
        register(param0, BURIED_TREASURE, var2);
        register(param0, DESERT_PYRAMID, Biomes.DESERT);
        register(param0, IGLOO, Biomes.SNOWY_TAIGA);
        register(param0, IGLOO, Biomes.SNOWY_PLAINS);
        register(param0, IGLOO, Biomes.SNOWY_SLOPES);
        register(param0, JUNGLE_TEMPLE, Biomes.BAMBOO_JUNGLE);
        register(param0, JUNGLE_TEMPLE, Biomes.JUNGLE);
        register(param0, MINESHAFT, var1);
        register(param0, MINESHAFT, var3);
        register(param0, MINESHAFT, var2);
        register(param0, MINESHAFT, Biomes.STONY_SHORE);
        register(param0, MINESHAFT, var4);
        register(param0, MINESHAFT, var6);
        register(param0, MINESHAFT, var7);
        register(param0, MINESHAFT, var8);
        register(param0, MINESHAFT, var9);
        register(param0, MINESHAFT, Biomes.MUSHROOM_FIELDS);
        register(param0, MINESHAFT, Biomes.ICE_SPIKES);
        register(param0, MINESHAFT, Biomes.WINDSWEPT_SAVANNA);
        register(param0, MINESHAFT, Biomes.DESERT);
        register(param0, MINESHAFT, Biomes.SAVANNA);
        register(param0, MINESHAFT, Biomes.SNOWY_PLAINS);
        register(param0, MINESHAFT, Biomes.PLAINS);
        register(param0, MINESHAFT, Biomes.SUNFLOWER_PLAINS);
        register(param0, MINESHAFT, Biomes.SWAMP);
        register(param0, MINESHAFT, Biomes.SAVANNA_PLATEAU);
        register(param0, MINESHAFT, Biomes.DRIPSTONE_CAVES);
        register(param0, MINESHAFT, Biomes.LUSH_CAVES);
        register(param0, MINESHAFT_MESA, var5);
        register(param0, OCEAN_MONUMENT, var0);
        register(param0, OCEAN_RUIN_COLD, Biomes.FROZEN_OCEAN);
        register(param0, OCEAN_RUIN_COLD, Biomes.COLD_OCEAN);
        register(param0, OCEAN_RUIN_COLD, Biomes.OCEAN);
        register(param0, OCEAN_RUIN_COLD, Biomes.DEEP_FROZEN_OCEAN);
        register(param0, OCEAN_RUIN_COLD, Biomes.DEEP_COLD_OCEAN);
        register(param0, OCEAN_RUIN_COLD, Biomes.DEEP_OCEAN);
        register(param0, OCEAN_RUIN_WARM, Biomes.LUKEWARM_OCEAN);
        register(param0, OCEAN_RUIN_WARM, Biomes.WARM_OCEAN);
        register(param0, OCEAN_RUIN_WARM, Biomes.DEEP_LUKEWARM_OCEAN);
        register(param0, PILLAGER_OUTPOST, Biomes.DESERT);
        register(param0, PILLAGER_OUTPOST, Biomes.PLAINS);
        register(param0, PILLAGER_OUTPOST, Biomes.SAVANNA);
        register(param0, PILLAGER_OUTPOST, Biomes.SNOWY_PLAINS);
        register(param0, PILLAGER_OUTPOST, Biomes.TAIGA);
        register(param0, PILLAGER_OUTPOST, var4);
        register(param0, PILLAGER_OUTPOST, Biomes.GROVE);
        register(param0, RUINED_PORTAL_DESERT, Biomes.DESERT);
        register(param0, RUINED_PORTAL_JUNGLE, var8);
        register(param0, RUINED_PORTAL_OCEAN, var1);
        register(param0, RUINED_PORTAL_SWAMP, Biomes.SWAMP);
        register(param0, RUINED_PORTAL_MOUNTAIN, var5);
        register(param0, RUINED_PORTAL_MOUNTAIN, var6);
        register(param0, RUINED_PORTAL_MOUNTAIN, Biomes.SAVANNA_PLATEAU);
        register(param0, RUINED_PORTAL_MOUNTAIN, Biomes.WINDSWEPT_SAVANNA);
        register(param0, RUINED_PORTAL_MOUNTAIN, Biomes.STONY_SHORE);
        register(param0, RUINED_PORTAL_MOUNTAIN, var4);
        register(param0, RUINED_PORTAL_STANDARD, Biomes.MUSHROOM_FIELDS);
        register(param0, RUINED_PORTAL_STANDARD, Biomes.ICE_SPIKES);
        register(param0, RUINED_PORTAL_STANDARD, var2);
        register(param0, RUINED_PORTAL_STANDARD, var3);
        register(param0, RUINED_PORTAL_STANDARD, var7);
        register(param0, RUINED_PORTAL_STANDARD, var9);
        register(param0, RUINED_PORTAL_STANDARD, Biomes.DRIPSTONE_CAVES);
        register(param0, RUINED_PORTAL_STANDARD, Biomes.LUSH_CAVES);
        register(param0, RUINED_PORTAL_STANDARD, Biomes.SAVANNA);
        register(param0, RUINED_PORTAL_STANDARD, Biomes.SNOWY_PLAINS);
        register(param0, RUINED_PORTAL_STANDARD, Biomes.PLAINS);
        register(param0, RUINED_PORTAL_STANDARD, Biomes.SUNFLOWER_PLAINS);
        register(param0, SHIPWRECK_BEACHED, var2);
        register(param0, SHIPWRECK, var1);
        register(param0, SWAMP_HUT, Biomes.SWAMP);
        register(param0, VILLAGE_DESERT, Biomes.DESERT);
        register(param0, VILLAGE_PLAINS, Biomes.PLAINS);
        register(param0, VILLAGE_PLAINS, Biomes.MEADOW);
        register(param0, VILLAGE_SAVANNA, Biomes.SAVANNA);
        register(param0, VILLAGE_SNOWY, Biomes.SNOWY_PLAINS);
        register(param0, VILLAGE_TAIGA, Biomes.TAIGA);
        register(param0, WOODLAND_MANSION, Biomes.DARK_FOREST);
        register(param0, NETHER_BRIDGE, var10);
        register(param0, NETHER_FOSSIL, Biomes.SOUL_SAND_VALLEY);
        register(param0, BASTION_REMNANT, Biomes.CRIMSON_FOREST);
        register(param0, BASTION_REMNANT, Biomes.NETHER_WASTES);
        register(param0, BASTION_REMNANT, Biomes.SOUL_SAND_VALLEY);
        register(param0, BASTION_REMNANT, Biomes.WARPED_FOREST);
        register(param0, RUINED_PORTAL_NETHER, var10);
        register(param0, END_CITY, Biomes.END_HIGHLANDS);
        register(param0, END_CITY, Biomes.END_MIDLANDS);
    }
}
