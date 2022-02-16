package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.NetherFossilFeature;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;
import net.minecraft.world.level.levelgen.structure.PostPlacementProcessor;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.slf4j.Logger;

public class StructureFeature<C extends FeatureConfiguration> {
    public static final BiMap<String, StructureFeature<?>> STRUCTURES_REGISTRY = HashBiMap.create();
    private static final Map<StructureFeature<?>, GenerationStep.Decoration> STEP = Maps.newHashMap();
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final StructureFeature<JigsawConfiguration> PILLAGER_OUTPOST = register(
        "Pillager_Outpost", new PillagerOutpostFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<MineshaftConfiguration> MINESHAFT = register(
        "Mineshaft", new MineshaftFeature(MineshaftConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_STRUCTURES
    );
    public static final StructureFeature<NoneFeatureConfiguration> WOODLAND_MANSION = register(
        "Mansion", new WoodlandMansionFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<NoneFeatureConfiguration> JUNGLE_TEMPLE = register(
        "Jungle_Pyramid", new JunglePyramidFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<NoneFeatureConfiguration> DESERT_PYRAMID = register(
        "Desert_Pyramid", new DesertPyramidFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<NoneFeatureConfiguration> IGLOO = register(
        "Igloo", new IglooFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<RuinedPortalConfiguration> RUINED_PORTAL = register(
        "Ruined_Portal", new RuinedPortalFeature(RuinedPortalConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<ShipwreckConfiguration> SHIPWRECK = register(
        "Shipwreck", new ShipwreckFeature(ShipwreckConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<NoneFeatureConfiguration> SWAMP_HUT = register(
        "Swamp_Hut", new SwamplandHutFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<NoneFeatureConfiguration> STRONGHOLD = register(
        "Stronghold", new StrongholdFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.STRONGHOLDS
    );
    public static final StructureFeature<NoneFeatureConfiguration> OCEAN_MONUMENT = register(
        "Monument", new OceanMonumentFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<OceanRuinConfiguration> OCEAN_RUIN = register(
        "Ocean_Ruin", new OceanRuinFeature(OceanRuinConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<NoneFeatureConfiguration> NETHER_BRIDGE = register(
        "Fortress", new NetherFortressFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_DECORATION
    );
    public static final StructureFeature<NoneFeatureConfiguration> END_CITY = register(
        "EndCity", new EndCityFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<ProbabilityFeatureConfiguration> BURIED_TREASURE = register(
        "Buried_Treasure", new BuriedTreasureFeature(ProbabilityFeatureConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_STRUCTURES
    );
    public static final StructureFeature<JigsawConfiguration> VILLAGE = register(
        "Village", new VillageFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<RangeConfiguration> NETHER_FOSSIL = register(
        "Nether_Fossil", new NetherFossilFeature(RangeConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_DECORATION
    );
    public static final StructureFeature<JigsawConfiguration> BASTION_REMNANT = register(
        "Bastion_Remnant", new BastionFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final List<StructureFeature<?>> NOISE_AFFECTING_FEATURES = ImmutableList.of(PILLAGER_OUTPOST, VILLAGE, NETHER_FOSSIL, STRONGHOLD);
    public static final int MAX_STRUCTURE_RANGE = 8;
    private final Codec<ConfiguredStructureFeature<C, StructureFeature<C>>> configuredStructureCodec;
    private final PieceGeneratorSupplier<C> pieceGenerator;
    private final PostPlacementProcessor postPlacementProcessor;

    private static <F extends StructureFeature<?>> F register(String param0, F param1, GenerationStep.Decoration param2) {
        STRUCTURES_REGISTRY.put(param0.toLowerCase(Locale.ROOT), param1);
        STEP.put(param1, param2);
        return Registry.register(Registry.STRUCTURE_FEATURE, param0.toLowerCase(Locale.ROOT), param1);
    }

    public StructureFeature(Codec<C> param0, PieceGeneratorSupplier<C> param1) {
        this(param0, param1, PostPlacementProcessor.NONE);
    }

    public StructureFeature(Codec<C> param0, PieceGeneratorSupplier<C> param1, PostPlacementProcessor param2) {
        this.configuredStructureCodec = RecordCodecBuilder.create(
            param1x -> param1x.group(
                        param0.fieldOf("config").forGetter(param0x -> (C)param0x.config),
                        RegistryCodecs.homogeneousList(Registry.BIOME_REGISTRY).fieldOf("biomes").forGetter(ConfiguredStructureFeature::biomes)
                    )
                    .apply(param1x, (param0x, param1xx) -> new ConfiguredStructureFeature<>(this, param0x, param1xx))
        );
        this.pieceGenerator = param1;
        this.postPlacementProcessor = param2;
    }

    public GenerationStep.Decoration step() {
        return STEP.get(this);
    }

    public static void bootstrap() {
    }

    @Nullable
    public static StructureStart<?> loadStaticStart(StructurePieceSerializationContext param0, CompoundTag param1, long param2) {
        String var0 = param1.getString("id");
        if ("INVALID".equals(var0)) {
            return StructureStart.INVALID_START;
        } else {
            StructureFeature<?> var1 = Registry.STRUCTURE_FEATURE.get(new ResourceLocation(var0.toLowerCase(Locale.ROOT)));
            if (var1 == null) {
                LOGGER.error("Unknown feature id: {}", var0);
                return null;
            } else {
                ChunkPos var2 = new ChunkPos(param1.getInt("ChunkX"), param1.getInt("ChunkZ"));
                int var3 = param1.getInt("references");
                ListTag var4 = param1.getList("Children", 10);

                try {
                    PiecesContainer var5 = PiecesContainer.load(var4, param0);
                    if (var1 == OCEAN_MONUMENT) {
                        var5 = OceanMonumentFeature.regeneratePiecesAfterLoad(var2, param2, var5);
                    }

                    return new StructureStart<>(var1, var2, var3, var5);
                } catch (Exception var10) {
                    LOGGER.error("Failed Start with id {}", var0, var10);
                    return null;
                }
            }
        }
    }

    public Codec<ConfiguredStructureFeature<C, StructureFeature<C>>> configuredStructureCodec() {
        return this.configuredStructureCodec;
    }

    public ConfiguredStructureFeature<C, ? extends StructureFeature<C>> configured(C param0, TagKey<Biome> param1) {
        return new ConfiguredStructureFeature<>(this, param0, BuiltinRegistries.BIOME.getOrCreateTag(param1));
    }

    public static BlockPos getLocatePos(RandomSpreadStructurePlacement param0, ChunkPos param1) {
        return new BlockPos(param1.getMinBlockX(), 0, param1.getMinBlockZ()).offset(param0.locateOffset());
    }

    public StructureStart<?> generate(
        RegistryAccess param0,
        ChunkGenerator param1,
        BiomeSource param2,
        StructureManager param3,
        long param4,
        ChunkPos param5,
        int param6,
        C param7,
        LevelHeightAccessor param8,
        Predicate<Holder<Biome>> param9
    ) {
        Optional<PieceGenerator<C>> var0 = this.pieceGenerator
            .createGenerator(new PieceGeneratorSupplier.Context<>(param1, param2, param4, param5, param7, param8, param9, param3, param0));
        if (var0.isPresent()) {
            StructurePiecesBuilder var1 = new StructurePiecesBuilder();
            WorldgenRandom var2 = new WorldgenRandom(new LegacyRandomSource(0L));
            var2.setLargeFeatureSeed(param4, param5.x, param5.z);
            var0.get().generatePieces(var1, new PieceGenerator.Context<>(param7, param1, param3, param5, param8, var2, param4));
            StructureStart<C> var3 = new StructureStart<>(this, param5, param6, var1.build());
            if (var3.isValid()) {
                return var3;
            }
        }

        return StructureStart.INVALID_START;
    }

    public boolean canGenerate(
        RegistryAccess param0,
        ChunkGenerator param1,
        BiomeSource param2,
        StructureManager param3,
        long param4,
        ChunkPos param5,
        C param6,
        LevelHeightAccessor param7,
        Predicate<Holder<Biome>> param8
    ) {
        return this.pieceGenerator
            .createGenerator(new PieceGeneratorSupplier.Context<>(param1, param2, param4, param5, param6, param7, param8, param3, param0))
            .isPresent();
    }

    public PostPlacementProcessor getPostPlacementProcessor() {
        return this.postPlacementProcessor;
    }

    public String getFeatureName() {
        return STRUCTURES_REGISTRY.inverse().get(this);
    }

    public BoundingBox adjustBoundingBox(BoundingBox param0) {
        return param0;
    }
}
