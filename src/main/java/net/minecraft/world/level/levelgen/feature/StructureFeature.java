package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
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
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.NetherFossilFeature;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;
import net.minecraft.world.level.levelgen.structure.PostPlacementProcessor;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StructureFeature<C extends FeatureConfiguration> {
    public static final BiMap<String, StructureFeature<?>> STRUCTURES_REGISTRY = HashBiMap.create();
    private static final Map<StructureFeature<?>, GenerationStep.Decoration> STEP = Maps.newHashMap();
    private static final Logger LOGGER = LogManager.getLogger();
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
    private final PieceGenerator<C> pieceGenerator;
    private final PostPlacementProcessor postPlacementProcessor;

    private static <F extends StructureFeature<?>> F register(String param0, F param1, GenerationStep.Decoration param2) {
        STRUCTURES_REGISTRY.put(param0.toLowerCase(Locale.ROOT), param1);
        STEP.put(param1, param2);
        return Registry.register(Registry.STRUCTURE_FEATURE, param0.toLowerCase(Locale.ROOT), param1);
    }

    public StructureFeature(Codec<C> param0, PieceGenerator<C> param1) {
        this(param0, param1, PostPlacementProcessor.NONE);
    }

    public StructureFeature(Codec<C> param0, PieceGenerator<C> param1, PostPlacementProcessor param2) {
        this.configuredStructureCodec = param0.fieldOf("config")
            .xmap(param0x -> new ConfiguredStructureFeature<>(this, param0x), param0x -> param0x.config)
            .codec();
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

    public ConfiguredStructureFeature<C, ? extends StructureFeature<C>> configured(C param0) {
        return new ConfiguredStructureFeature<>(this, param0);
    }

    public BlockPos getLocatePos(ChunkPos param0) {
        return new BlockPos(param0.getMinBlockX(), 0, param0.getMinBlockZ());
    }

    @Nullable
    public BlockPos getNearestGeneratedFeature(
        LevelReader param0, StructureFeatureManager param1, BlockPos param2, int param3, boolean param4, long param5, StructureFeatureConfiguration param6
    ) {
        int var0 = param6.spacing();
        int var1 = SectionPos.blockToSectionCoord(param2.getX());
        int var2 = SectionPos.blockToSectionCoord(param2.getZ());

        for(int var3 = 0; var3 <= param3; ++var3) {
            for(int var4 = -var3; var4 <= var3; ++var4) {
                boolean var5 = var4 == -var3 || var4 == var3;

                for(int var6 = -var3; var6 <= var3; ++var6) {
                    boolean var7 = var6 == -var3 || var6 == var3;
                    if (var5 || var7) {
                        int var8 = var1 + var0 * var4;
                        int var9 = var2 + var0 * var6;
                        ChunkPos var10 = this.getPotentialFeatureChunk(param6, param5, var8, var9);
                        ChunkAccess var11 = param0.getChunk(var10.x, var10.z, ChunkStatus.STRUCTURE_STARTS);
                        StructureStart<?> var12 = param1.getStartForFeature(SectionPos.bottomOf(var11), this, var11);
                        if (var12 != null && var12.isValid()) {
                            if (param4 && var12.canBeReferenced()) {
                                var12.addReference();
                                return this.getLocatePos(var12.getChunkPos());
                            }

                            if (!param4) {
                                return this.getLocatePos(var12.getChunkPos());
                            }
                        }

                        if (var3 == 0) {
                            break;
                        }
                    }
                }

                if (var3 == 0) {
                    break;
                }
            }
        }

        return null;
    }

    protected boolean linearSeparation() {
        return true;
    }

    public final ChunkPos getPotentialFeatureChunk(StructureFeatureConfiguration param0, long param1, int param2, int param3) {
        int var0 = param0.spacing();
        int var1 = param0.separation();
        int var2 = Math.floorDiv(param2, var0);
        int var3 = Math.floorDiv(param3, var0);
        WorldgenRandom var4 = new WorldgenRandom(new LegacyRandomSource(0L));
        var4.setLargeFeatureWithSalt(param1, var2, var3, param0.salt());
        int var5;
        int var6;
        if (this.linearSeparation()) {
            var5 = var4.nextInt(var0 - var1);
            var6 = var4.nextInt(var0 - var1);
        } else {
            var5 = (var4.nextInt(var0 - var1) + var4.nextInt(var0 - var1)) / 2;
            var6 = (var4.nextInt(var0 - var1) + var4.nextInt(var0 - var1)) / 2;
        }

        return new ChunkPos(var2 * var0 + var5, var3 * var0 + var6);
    }

    protected boolean isFeatureChunk(ChunkGenerator param0, BiomeSource param1, long param2, ChunkPos param3, C param4, LevelHeightAccessor param5) {
        return true;
    }

    public StructureStart<?> generate(
        RegistryAccess param0,
        ChunkGenerator param1,
        BiomeSource param2,
        StructureManager param3,
        long param4,
        ChunkPos param5,
        int param6,
        StructureFeatureConfiguration param7,
        C param8,
        LevelHeightAccessor param9,
        Predicate<Biome> param10
    ) {
        ChunkPos var0 = this.getPotentialFeatureChunk(param7, param4, param5.x, param5.z);
        if (param5.x == var0.x && param5.z == var0.z && this.isFeatureChunk(param1, param2, param4, param5, param8, param9)) {
            StructurePiecesBuilder var1 = new StructurePiecesBuilder();
            WorldgenRandom var2 = new WorldgenRandom(new LegacyRandomSource(0L));
            var2.setLargeFeatureSeed(param4, param5.x, param5.z);
            this.pieceGenerator.generatePieces(var1, param8, new PieceGenerator.Context(param0, param1, param3, param5, param10, param9, var2, param4));
            StructureStart<C> var3 = new StructureStart<>(this, param5, param6, var1.build());
            if (var3.isValid()) {
                return var3;
            }
        }

        return StructureStart.INVALID_START;
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
