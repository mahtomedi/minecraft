package net.minecraft.server.dedicated;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.slf4j.Logger;

public class DedicatedServerProperties extends Settings<DedicatedServerProperties> {
    static final Logger LOGGER = LogUtils.getLogger();
    public final boolean onlineMode = this.get("online-mode", true);
    public final boolean preventProxyConnections = this.get("prevent-proxy-connections", false);
    public final String serverIp = this.get("server-ip", "");
    public final boolean spawnAnimals = this.get("spawn-animals", true);
    public final boolean spawnNpcs = this.get("spawn-npcs", true);
    public final boolean pvp = this.get("pvp", true);
    public final boolean allowFlight = this.get("allow-flight", false);
    public final String resourcePack = this.get("resource-pack", "");
    public final boolean requireResourcePack = this.get("require-resource-pack", false);
    public final String resourcePackPrompt = this.get("resource-pack-prompt", "");
    public final String motd = this.get("motd", "A Minecraft Server");
    public final boolean forceGameMode = this.get("force-gamemode", false);
    public final boolean enforceWhitelist = this.get("enforce-whitelist", false);
    public final Difficulty difficulty = this.get(
        "difficulty", dispatchNumberOrString(Difficulty::byId, Difficulty::byName), Difficulty::getKey, Difficulty.EASY
    );
    public final GameType gamemode = this.get("gamemode", dispatchNumberOrString(GameType::byId, GameType::byName), GameType::getName, GameType.SURVIVAL);
    public final String levelName = this.get("level-name", "world");
    public final int serverPort = this.get("server-port", 25565);
    @Nullable
    public final Boolean announcePlayerAchievements = this.getLegacyBoolean("announce-player-achievements");
    public final boolean enableQuery = this.get("enable-query", false);
    public final int queryPort = this.get("query.port", 25565);
    public final boolean enableRcon = this.get("enable-rcon", false);
    public final int rconPort = this.get("rcon.port", 25575);
    public final String rconPassword = this.get("rcon.password", "");
    @Nullable
    public final String resourcePackHash = this.getLegacyString("resource-pack-hash");
    public final String resourcePackSha1 = this.get("resource-pack-sha1", "");
    public final boolean hardcore = this.get("hardcore", false);
    public final boolean allowNether = this.get("allow-nether", true);
    public final boolean spawnMonsters = this.get("spawn-monsters", true);
    public final boolean useNativeTransport = this.get("use-native-transport", true);
    public final boolean enableCommandBlock = this.get("enable-command-block", false);
    public final int spawnProtection = this.get("spawn-protection", 16);
    public final int opPermissionLevel = this.get("op-permission-level", 4);
    public final int functionPermissionLevel = this.get("function-permission-level", 2);
    public final long maxTickTime = this.get("max-tick-time", TimeUnit.MINUTES.toMillis(1L));
    public final int maxChainedNeighborUpdates = this.get("max-chained-neighbor-updates", 1000000);
    public final int rateLimitPacketsPerSecond = this.get("rate-limit", 0);
    public final int viewDistance = this.get("view-distance", 10);
    public final int simulationDistance = this.get("simulation-distance", 10);
    public final int maxPlayers = this.get("max-players", 20);
    public final int networkCompressionThreshold = this.get("network-compression-threshold", 256);
    public final boolean broadcastRconToOps = this.get("broadcast-rcon-to-ops", true);
    public final boolean broadcastConsoleToOps = this.get("broadcast-console-to-ops", true);
    public final int maxWorldSize = this.get("max-world-size", param0x -> Mth.clamp(param0x, 1, 29999984), 29999984);
    public final boolean syncChunkWrites = this.get("sync-chunk-writes", true);
    public final boolean enableJmxMonitoring = this.get("enable-jmx-monitoring", false);
    public final boolean enableStatus = this.get("enable-status", true);
    public final boolean hideOnlinePlayers = this.get("hide-online-players", false);
    public final int entityBroadcastRangePercentage = this.get("entity-broadcast-range-percentage", param0x -> Mth.clamp(param0x, 10, 1000), 100);
    public final String textFilteringConfig = this.get("text-filtering-config", "");
    public final Settings<DedicatedServerProperties>.MutableValue<Integer> playerIdleTimeout = this.getMutable("player-idle-timeout", 0);
    public final Settings<DedicatedServerProperties>.MutableValue<Boolean> whiteList = this.getMutable("white-list", false);
    public final boolean enforceSecureProfile = this.get("enforce-secure-profile", false);
    private final DedicatedServerProperties.WorldGenProperties worldGenProperties = new DedicatedServerProperties.WorldGenProperties(
        this.get("level-seed", ""),
        this.get("generator-settings", param0x -> GsonHelper.parse(!param0x.isEmpty() ? param0x : "{}"), new JsonObject()),
        this.get("generate-structures", true),
        this.get("level-type", param0x -> param0x.toLowerCase(Locale.ROOT), WorldPresets.NORMAL.location().toString())
    );
    @Nullable
    private WorldGenSettings worldGenSettings;

    public DedicatedServerProperties(Properties param0) {
        super(param0);
    }

    public static DedicatedServerProperties fromFile(Path param0) {
        return new DedicatedServerProperties(loadFromFile(param0));
    }

    protected DedicatedServerProperties reload(RegistryAccess param0, Properties param1) {
        DedicatedServerProperties var0 = new DedicatedServerProperties(param1);
        var0.getWorldGenSettings(param0);
        return var0;
    }

    public WorldGenSettings getWorldGenSettings(RegistryAccess param0) {
        if (this.worldGenSettings == null) {
            this.worldGenSettings = this.worldGenProperties.create(param0);
        }

        return this.worldGenSettings;
    }

    public static record WorldGenProperties(String levelSeed, JsonObject generatorSettings, boolean generateStructures, String levelType) {
        private static final Map<String, ResourceKey<WorldPreset>> LEGACY_PRESET_NAMES = Map.of(
            "default", WorldPresets.NORMAL, "largebiomes", WorldPresets.LARGE_BIOMES
        );

        public WorldGenSettings create(RegistryAccess param0) {
            long var0 = WorldGenSettings.parseSeed(this.levelSeed()).orElse(RandomSource.create().nextLong());
            Registry<WorldPreset> var1 = param0.registryOrThrow(Registry.WORLD_PRESET_REGISTRY);
            Holder<WorldPreset> var2 = var1.getHolder(WorldPresets.NORMAL)
                .or(() -> var1.holders().findAny())
                .orElseThrow(() -> new IllegalStateException("Invalid datapack contents: can't find default preset"));
            Holder<WorldPreset> var3 = Optional.ofNullable(ResourceLocation.tryParse(this.levelType))
                .map(param0x -> ResourceKey.create(Registry.WORLD_PRESET_REGISTRY, param0x))
                .or(() -> Optional.ofNullable(LEGACY_PRESET_NAMES.get(this.levelType)))
                .flatMap(var1::getHolder)
                .orElseGet(
                    () -> {
                        DedicatedServerProperties.LOGGER
                            .warn(
                                "Failed to parse level-type {}, defaulting to {}",
                                this.levelType,
                                var2.unwrapKey().map(param0x -> param0x.location().toString()).orElse("[unnamed]")
                            );
                        return var2;
                    }
                );
            WorldGenSettings var4 = var3.value().createWorldGenSettings(var0, this.generateStructures, false);
            if (var3.is(WorldPresets.FLAT)) {
                RegistryOps<JsonElement> var5 = RegistryOps.create(JsonOps.INSTANCE, param0);
                Optional<FlatLevelGeneratorSettings> var6 = FlatLevelGeneratorSettings.CODEC
                    .parse(new Dynamic<>(var5, this.generatorSettings()))
                    .resultOrPartial(DedicatedServerProperties.LOGGER::error);
                if (var6.isPresent()) {
                    Registry<StructureSet> var7 = param0.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
                    return WorldGenSettings.replaceOverworldGenerator(param0, var4, new FlatLevelSource(var7, var6.get()));
                }
            }

            return var4;
        }
    }
}
