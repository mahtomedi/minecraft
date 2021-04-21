package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.NetherFossilFeature;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class StructureFeature<C extends FeatureConfiguration> {
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
    public static final SwamplandHutFeature SWAMP_HUT = register(
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
    public static final StructureFeature<NoneFeatureConfiguration> NETHER_FOSSIL = register(
        "Nether_Fossil", new NetherFossilFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_DECORATION
    );
    public static final StructureFeature<JigsawConfiguration> BASTION_REMNANT = register(
        "Bastion_Remnant", new BastionFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
    );
    public static final List<StructureFeature<?>> NOISE_AFFECTING_FEATURES = ImmutableList.of(PILLAGER_OUTPOST, VILLAGE, NETHER_FOSSIL, STRONGHOLD);
    private static final ResourceLocation JIGSAW_RENAME = new ResourceLocation("jigsaw");
    private static final Map<ResourceLocation, ResourceLocation> RENAMES = ImmutableMap.<ResourceLocation, ResourceLocation>builder()
        .put(new ResourceLocation("nvi"), JIGSAW_RENAME)
        .put(new ResourceLocation("pcp"), JIGSAW_RENAME)
        .put(new ResourceLocation("bastionremnant"), JIGSAW_RENAME)
        .put(new ResourceLocation("runtime"), JIGSAW_RENAME)
        .build();
    public static final int MAX_STRUCTURE_RANGE = 8;
    private final Codec<ConfiguredStructureFeature<C, StructureFeature<C>>> configuredStructureCodec;

    private static <F extends StructureFeature<?>> F register(String param0, F param1, GenerationStep.Decoration param2) {
        STRUCTURES_REGISTRY.put(param0.toLowerCase(Locale.ROOT), param1);
        STEP.put(param1, param2);
        return Registry.register(Registry.STRUCTURE_FEATURE, param0.toLowerCase(Locale.ROOT), param1);
    }

    public StructureFeature(Codec<C> param0) {
        this.configuredStructureCodec = param0.fieldOf("config")
            .xmap(param0x -> new ConfiguredStructureFeature<>(this, param0x), param0x -> param0x.config)
            .codec();
    }

    public GenerationStep.Decoration step() {
        return STEP.get(this);
    }

    public static void bootstrap() {
    }

    @Nullable
    public static StructureStart<?> loadStaticStart(ServerLevel param0, CompoundTag param1, long param2) {
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
                    StructureStart<?> var5 = var1.createStart(var2, var3, param2);

                    for(int var6 = 0; var6 < var4.size(); ++var6) {
                        CompoundTag var7 = var4.getCompound(var6);
                        String var8 = var7.getString("id").toLowerCase(Locale.ROOT);
                        ResourceLocation var9 = new ResourceLocation(var8);
                        ResourceLocation var10 = RENAMES.getOrDefault(var9, var9);
                        StructurePieceType var11 = Registry.STRUCTURE_PIECE.get(var10);
                        if (var11 == null) {
                            LOGGER.error("Unknown structure piece id: {}", var10);
                        } else {
                            try {
                                StructurePiece var12 = var11.load(param0, var7);
                                var5.addPiece(var12);
                            } catch (Exception var17) {
                                LOGGER.error("Exception loading structure piece with id {}", var10, var17);
                            }
                        }
                    }

                    return var5;
                } catch (Exception var18) {
                    LOGGER.error("Failed Start with id {}", var0, var18);
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

    @Nullable
    public BlockPos getNearestGeneratedFeature(
        LevelReader param0, StructureFeatureManager param1, BlockPos param2, int param3, boolean param4, long param5, StructureFeatureConfiguration param6
    ) {
        int var0 = param6.spacing();
        int var1 = SectionPos.blockToSectionCoord(param2.getX());
        int var2 = SectionPos.blockToSectionCoord(param2.getZ());
        int var3 = 0;

        for(WorldgenRandom var4 = new WorldgenRandom(); var3 <= param3; ++var3) {
            for(int var5 = -var3; var5 <= var3; ++var5) {
                boolean var6 = var5 == -var3 || var5 == var3;

                for(int var7 = -var3; var7 <= var3; ++var7) {
                    boolean var8 = var7 == -var3 || var7 == var3;
                    if (var6 || var8) {
                        int var9 = var1 + var0 * var5;
                        int var10 = var2 + var0 * var7;
                        ChunkPos var11 = this.getPotentialFeatureChunk(param6, param5, var4, var9, var10);
                        boolean var12 = param0.getBiomeManager().getPrimaryBiomeAtChunk(var11).getGenerationSettings().isValidStart(this);
                        if (var12) {
                            ChunkAccess var13 = param0.getChunk(var11.x, var11.z, ChunkStatus.STRUCTURE_STARTS);
                            StructureStart<?> var14 = param1.getStartForFeature(SectionPos.bottomOf(var13), this, var13);
                            if (var14 != null && var14.isValid()) {
                                if (param4 && var14.canBeReferenced()) {
                                    var14.addReference();
                                    return var14.getLocatePos();
                                }

                                if (!param4) {
                                    return var14.getLocatePos();
                                }
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

    public final ChunkPos getPotentialFeatureChunk(StructureFeatureConfiguration param0, long param1, WorldgenRandom param2, int param3, int param4) {
        int var0 = param0.spacing();
        int var1 = param0.separation();
        int var2 = Math.floorDiv(param3, var0);
        int var3 = Math.floorDiv(param4, var0);
        param2.setLargeFeatureWithSalt(param1, var2, var3, param0.salt());
        int var4;
        int var5;
        if (this.linearSeparation()) {
            var4 = param2.nextInt(var0 - var1);
            var5 = param2.nextInt(var0 - var1);
        } else {
            var4 = (param2.nextInt(var0 - var1) + param2.nextInt(var0 - var1)) / 2;
            var5 = (param2.nextInt(var0 - var1) + param2.nextInt(var0 - var1)) / 2;
        }

        return new ChunkPos(var2 * var0 + var4, var3 * var0 + var5);
    }

    protected boolean isFeatureChunk(
        ChunkGenerator param0,
        BiomeSource param1,
        long param2,
        WorldgenRandom param3,
        ChunkPos param4,
        Biome param5,
        ChunkPos param6,
        C param7,
        LevelHeightAccessor param8
    ) {
        return true;
    }

    private StructureStart<C> createStart(ChunkPos param0, int param1, long param2) {
        return this.getStartFactory().create(this, param0, param1, param2);
    }

    public StructureStart<?> generate(
        RegistryAccess param0,
        ChunkGenerator param1,
        BiomeSource param2,
        StructureManager param3,
        long param4,
        ChunkPos param5,
        Biome param6,
        int param7,
        WorldgenRandom param8,
        StructureFeatureConfiguration param9,
        C param10,
        LevelHeightAccessor param11
    ) {
        ChunkPos var0 = this.getPotentialFeatureChunk(param9, param4, param8, param5.x, param5.z);
        if (param5.x == var0.x && param5.z == var0.z && this.isFeatureChunk(param1, param2, param4, param8, param5, param6, var0, param10, param11)) {
            StructureStart<C> var1 = this.createStart(param5, param7, param4);
            var1.generatePieces(param0, param1, param3, param5, param6, param10, param11);
            if (var1.isValid()) {
                return var1;
            }
        }

        return StructureStart.INVALID_START;
    }

    public abstract StructureFeature.StructureStartFactory<C> getStartFactory();

    public String getFeatureName() {
        return STRUCTURES_REGISTRY.inverse().get(this);
    }

    public WeightedRandomList<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
        return MobSpawnSettings.EMPTY_MOB_LIST;
    }

    public WeightedRandomList<MobSpawnSettings.SpawnerData> getSpecialAnimals() {
        return MobSpawnSettings.EMPTY_MOB_LIST;
    }

    public WeightedRandomList<MobSpawnSettings.SpawnerData> getSpecialUndergroundWaterAnimals() {
        return MobSpawnSettings.EMPTY_MOB_LIST;
    }

    public interface StructureStartFactory<C extends FeatureConfiguration> {
        StructureStart<C> create(StructureFeature<C> var1, ChunkPos var2, int var3, long var4);
    }
}
