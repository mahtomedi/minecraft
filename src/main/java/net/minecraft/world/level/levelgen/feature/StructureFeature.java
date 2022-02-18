package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
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
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.structure.NetherFossilFeature;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;
import net.minecraft.world.level.levelgen.structure.PostPlacementProcessor;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.slf4j.Logger;

public class StructureFeature<C extends FeatureConfiguration> {
    private static final Map<StructureFeature<?>, GenerationStep.Decoration> STEP = Maps.newHashMap();
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final StructureFeature<JigsawConfiguration> PILLAGER_OUTPOST = register(
        "pillager_outpost", new PillagerOutpostFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<MineshaftConfiguration> MINESHAFT = register(
        "mineshaft", new MineshaftFeature(MineshaftConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_STRUCTURES
    );
    public static final StructureFeature<NoneFeatureConfiguration> WOODLAND_MANSION = register(
        "mansion", new WoodlandMansionFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<NoneFeatureConfiguration> JUNGLE_TEMPLE = register(
        "jungle_pyramid", new JunglePyramidFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<NoneFeatureConfiguration> DESERT_PYRAMID = register(
        "desert_pyramid", new DesertPyramidFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<NoneFeatureConfiguration> IGLOO = register(
        "igloo", new IglooFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<RuinedPortalConfiguration> RUINED_PORTAL = register(
        "ruined_portal", new RuinedPortalFeature(RuinedPortalConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<ShipwreckConfiguration> SHIPWRECK = register(
        "shipwreck", new ShipwreckFeature(ShipwreckConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<NoneFeatureConfiguration> SWAMP_HUT = register(
        "swamp_hut", new SwamplandHutFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<NoneFeatureConfiguration> STRONGHOLD = register(
        "stronghold", new StrongholdFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.STRONGHOLDS
    );
    public static final StructureFeature<NoneFeatureConfiguration> OCEAN_MONUMENT = register(
        "monument", new OceanMonumentFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<OceanRuinConfiguration> OCEAN_RUIN = register(
        "ocean_ruin", new OceanRuinFeature(OceanRuinConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<NoneFeatureConfiguration> FORTRESS = register(
        "fortress", new NetherFortressFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_DECORATION
    );
    public static final StructureFeature<NoneFeatureConfiguration> END_CITY = register(
        "endcity", new EndCityFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<ProbabilityFeatureConfiguration> BURIED_TREASURE = register(
        "buried_treasure", new BuriedTreasureFeature(ProbabilityFeatureConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_STRUCTURES
    );
    public static final StructureFeature<JigsawConfiguration> VILLAGE = register(
        "village", new VillageFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final StructureFeature<RangeConfiguration> NETHER_FOSSIL = register(
        "nether_fossil", new NetherFossilFeature(RangeConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_DECORATION
    );
    public static final StructureFeature<JigsawConfiguration> BASTION_REMNANT = register(
        "bastion_remnant", new BastionFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final int MAX_STRUCTURE_RANGE = 8;
    private final Codec<ConfiguredStructureFeature<C, StructureFeature<C>>> configuredStructureCodec;
    private final PieceGeneratorSupplier<C> pieceGenerator;
    private final PostPlacementProcessor postPlacementProcessor;

    private static <F extends StructureFeature<?>> F register(String param0, F param1, GenerationStep.Decoration param2) {
        STEP.put(param1, param2);
        return Registry.register(Registry.STRUCTURE_FEATURE, param0, param1);
    }

    public StructureFeature(Codec<C> param0, PieceGeneratorSupplier<C> param1) {
        this(param0, param1, PostPlacementProcessor.NONE);
    }

    public StructureFeature(Codec<C> param0, PieceGeneratorSupplier<C> param1, PostPlacementProcessor param2) {
        this.configuredStructureCodec = RecordCodecBuilder.create(
            param1x -> param1x.group(
                        param0.fieldOf("config").forGetter(param0x -> (C)param0x.config),
                        RegistryCodecs.homogeneousList(Registry.BIOME_REGISTRY).fieldOf("biomes").forGetter(ConfiguredStructureFeature::biomes),
                        Codec.BOOL.optionalFieldOf("adapt_noise", Boolean.valueOf(false)).forGetter(param0x -> param0x.adaptNoise),
                        Codec.simpleMap(MobCategory.CODEC, StructureSpawnOverride.CODEC, StringRepresentable.keys(MobCategory.values()))
                            .fieldOf("spawn_overrides")
                            .forGetter(param0x -> param0x.spawnOverrides)
                    )
                    .apply(param1x, (param0x, param1xx, param2x, param3) -> new ConfiguredStructureFeature<>(this, param0x, param1xx, param2x, param3))
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
    public static StructureStart loadStaticStart(StructurePieceSerializationContext param0, CompoundTag param1, long param2) {
        String var0 = param1.getString("id");
        if ("INVALID".equals(var0)) {
            return StructureStart.INVALID_START;
        } else {
            Registry<ConfiguredStructureFeature<?, ?>> var1 = param0.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
            ConfiguredStructureFeature<?, ?> var2 = var1.get(new ResourceLocation(var0));
            if (var2 == null) {
                LOGGER.error("Unknown feature id: {}", var0);
                return null;
            } else {
                ChunkPos var3 = new ChunkPos(param1.getInt("ChunkX"), param1.getInt("ChunkZ"));
                int var4 = param1.getInt("references");
                ListTag var5 = param1.getList("Children", 10);

                try {
                    PiecesContainer var6 = PiecesContainer.load(var5, param0);
                    if (var2.feature == OCEAN_MONUMENT) {
                        var6 = OceanMonumentFeature.regeneratePiecesAfterLoad(var3, param2, var6);
                    }

                    return new StructureStart(var2, var3, var4, var6);
                } catch (Exception var11) {
                    LOGGER.error("Failed Start with id {}", var0, var11);
                    return null;
                }
            }
        }
    }

    public Codec<ConfiguredStructureFeature<C, StructureFeature<C>>> configuredStructureCodec() {
        return this.configuredStructureCodec;
    }

    public ConfiguredStructureFeature<C, ? extends StructureFeature<C>> configured(C param0, TagKey<Biome> param1) {
        return this.configured(param0, param1, false);
    }

    public ConfiguredStructureFeature<C, ? extends StructureFeature<C>> configured(C param0, TagKey<Biome> param1, boolean param2) {
        return new ConfiguredStructureFeature<>(this, param0, BuiltinRegistries.BIOME.getOrCreateTag(param1), param2, Map.of());
    }

    public ConfiguredStructureFeature<C, ? extends StructureFeature<C>> configured(
        C param0, TagKey<Biome> param1, Map<MobCategory, StructureSpawnOverride> param2
    ) {
        return new ConfiguredStructureFeature<>(this, param0, BuiltinRegistries.BIOME.getOrCreateTag(param1), false, param2);
    }

    public ConfiguredStructureFeature<C, ? extends StructureFeature<C>> configured(
        C param0, TagKey<Biome> param1, boolean param2, Map<MobCategory, StructureSpawnOverride> param3
    ) {
        return new ConfiguredStructureFeature<>(this, param0, BuiltinRegistries.BIOME.getOrCreateTag(param1), param2, param3);
    }

    public static BlockPos getLocatePos(RandomSpreadStructurePlacement param0, ChunkPos param1) {
        return new BlockPos(param1.getMinBlockX(), 0, param1.getMinBlockZ()).offset(param0.locateOffset());
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

    public PieceGeneratorSupplier<C> pieceGeneratorSupplier() {
        return this.pieceGenerator;
    }

    public PostPlacementProcessor getPostPlacementProcessor() {
        return this.postPlacementProcessor;
    }
}
