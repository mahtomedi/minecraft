package net.minecraft.world.level.storage;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.Dynamic;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.ChunkGeneratorProvider;
import net.minecraft.world.level.timers.TimerCallbacks;
import net.minecraft.world.level.timers.TimerQueue;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PrimaryLevelData implements ServerLevelData, WorldData {
    private final String minecraftVersionName;
    private final int minecraftVersion;
    private final boolean snapshot;
    private final long seed;
    private final ChunkGeneratorProvider generatorProvider;
    @Nullable
    private String legacyCustomOptions;
    private int xSpawn;
    private int ySpawn;
    private int zSpawn;
    private long gameTime;
    private long dayTime;
    private long lastPlayed;
    private long sizeOnDisk;
    @Nullable
    private final DataFixer fixerUpper;
    private final int playerDataVersion;
    private boolean upgradedPlayerTag;
    private CompoundTag loadedPlayerTag;
    private final String levelName;
    private final int version;
    private int clearWeatherTime;
    private boolean raining;
    private int rainTime;
    private boolean thundering;
    private int thunderTime;
    private GameType gameType;
    private final boolean generateMapFeatures;
    private final boolean hardcore;
    private final boolean allowCommands;
    private final boolean generateBonusChest;
    private boolean initialized;
    private Difficulty difficulty = Difficulty.NORMAL;
    private boolean difficultyLocked;
    private WorldBorder.Settings worldBorder = WorldBorder.DEFAULT_SETTINGS;
    private final Set<String> disabledDataPacks = Sets.newHashSet();
    private final Set<String> enabledDataPacks = Sets.newLinkedHashSet();
    private final Map<DimensionType, CompoundTag> dimensionData = Maps.newIdentityHashMap();
    @Nullable
    private CompoundTag customBossEvents;
    private int wanderingTraderSpawnDelay;
    private int wanderingTraderSpawnChance;
    private UUID wanderingTraderId;
    private Set<String> knownServerBrands = Sets.newLinkedHashSet();
    private boolean wasModded;
    private final GameRules gameRules = new GameRules();
    private final TimerQueue<MinecraftServer> scheduledEvents = new TimerQueue<>(TimerCallbacks.SERVER_CALLBACKS);

    public PrimaryLevelData(CompoundTag param0, DataFixer param1, int param2, @Nullable CompoundTag param3) {
        this.fixerUpper = param1;
        ListTag var0 = param0.getList("ServerBrands", 8);

        for(int var1 = 0; var1 < var0.size(); ++var1) {
            this.knownServerBrands.add(var0.getString(var1));
        }

        this.wasModded = param0.getBoolean("WasModded");
        if (param0.contains("Version", 10)) {
            CompoundTag var2 = param0.getCompound("Version");
            this.minecraftVersionName = var2.getString("Name");
            this.minecraftVersion = var2.getInt("Id");
            this.snapshot = var2.getBoolean("Snapshot");
        } else {
            this.minecraftVersionName = SharedConstants.getCurrentVersion().getName();
            this.minecraftVersion = SharedConstants.getCurrentVersion().getWorldVersion();
            this.snapshot = !SharedConstants.getCurrentVersion().isStable();
        }

        this.seed = param0.getLong("RandomSeed");
        if (param0.contains("generatorName", 8)) {
            String var3 = param0.getString("generatorName");
            LevelType var4 = LevelType.getLevelType(var3);
            if (var4 == null) {
                var4 = LevelType.NORMAL;
            } else if (var4 == LevelType.CUSTOMIZED) {
                this.legacyCustomOptions = param0.getString("generatorOptions");
            } else if (var4.hasReplacement()) {
                int var5 = 0;
                if (param0.contains("generatorVersion", 99)) {
                    var5 = param0.getInt("generatorVersion");
                }

                var4 = var4.getReplacementForVersion(var5);
            }

            CompoundTag var6 = param0.getCompound("generatorOptions");
            Dynamic<?> var7 = new Dynamic<>(NbtOps.INSTANCE, var6);
            Dynamic<?> var8 = datafixGeneratorOptions(var4, var7, param2, param1);
            this.generatorProvider = var4.createProvider(var8);
        } else {
            this.generatorProvider = LevelType.NORMAL.getDefaultProvider();
        }

        this.gameType = GameType.byId(param0.getInt("GameType"));
        if (param0.contains("legacy_custom_options", 8)) {
            this.legacyCustomOptions = param0.getString("legacy_custom_options");
        }

        if (param0.contains("MapFeatures", 99)) {
            this.generateMapFeatures = param0.getBoolean("MapFeatures");
        } else {
            this.generateMapFeatures = true;
        }

        this.xSpawn = param0.getInt("SpawnX");
        this.ySpawn = param0.getInt("SpawnY");
        this.zSpawn = param0.getInt("SpawnZ");
        this.gameTime = param0.getLong("Time");
        if (param0.contains("DayTime", 99)) {
            this.dayTime = param0.getLong("DayTime");
        } else {
            this.dayTime = this.gameTime;
        }

        this.lastPlayed = param0.getLong("LastPlayed");
        this.sizeOnDisk = param0.getLong("SizeOnDisk");
        this.levelName = param0.getString("LevelName");
        this.version = param0.getInt("version");
        this.clearWeatherTime = param0.getInt("clearWeatherTime");
        this.rainTime = param0.getInt("rainTime");
        this.raining = param0.getBoolean("raining");
        this.thunderTime = param0.getInt("thunderTime");
        this.thundering = param0.getBoolean("thundering");
        this.hardcore = param0.getBoolean("hardcore");
        if (param0.contains("initialized", 99)) {
            this.initialized = param0.getBoolean("initialized");
        } else {
            this.initialized = true;
        }

        if (param0.contains("allowCommands", 99)) {
            this.allowCommands = param0.getBoolean("allowCommands");
        } else {
            this.allowCommands = this.gameType == GameType.CREATIVE;
        }

        this.generateBonusChest = param0.getBoolean("BonusChest");
        this.playerDataVersion = param2;
        if (param3 != null) {
            this.loadedPlayerTag = param3;
        }

        if (param0.contains("GameRules", 10)) {
            this.gameRules.loadFromTag(param0.getCompound("GameRules"));
        }

        if (param0.contains("Difficulty", 99)) {
            this.difficulty = Difficulty.byId(param0.getByte("Difficulty"));
        }

        if (param0.contains("DifficultyLocked", 1)) {
            this.difficultyLocked = param0.getBoolean("DifficultyLocked");
        }

        this.worldBorder = WorldBorder.Settings.read(param0, WorldBorder.DEFAULT_SETTINGS);
        if (param0.contains("DimensionData", 10)) {
            CompoundTag var9 = param0.getCompound("DimensionData");

            for(String var10 : var9.getAllKeys()) {
                this.dimensionData.put(DimensionType.getById(Integer.parseInt(var10)), var9.getCompound(var10));
            }
        }

        if (param0.contains("DataPacks", 10)) {
            CompoundTag var11 = param0.getCompound("DataPacks");
            ListTag var12 = var11.getList("Disabled", 8);

            for(int var13 = 0; var13 < var12.size(); ++var13) {
                this.disabledDataPacks.add(var12.getString(var13));
            }

            ListTag var14 = var11.getList("Enabled", 8);

            for(int var15 = 0; var15 < var14.size(); ++var15) {
                this.enabledDataPacks.add(var14.getString(var15));
            }
        }

        if (param0.contains("CustomBossEvents", 10)) {
            this.customBossEvents = param0.getCompound("CustomBossEvents");
        }

        if (param0.contains("ScheduledEvents", 9)) {
            this.scheduledEvents.load(param0.getList("ScheduledEvents", 10));
        }

        if (param0.contains("WanderingTraderSpawnDelay", 99)) {
            this.wanderingTraderSpawnDelay = param0.getInt("WanderingTraderSpawnDelay");
        }

        if (param0.contains("WanderingTraderSpawnChance", 99)) {
            this.wanderingTraderSpawnChance = param0.getInt("WanderingTraderSpawnChance");
        }

        if (param0.hasUUID("WanderingTraderId")) {
            this.wanderingTraderId = param0.getUUID("WanderingTraderId");
        }

    }

    private static <T> Dynamic<T> datafixGeneratorOptions(LevelType param0, Dynamic<T> param1, int param2, DataFixer param3) {
        int var0 = Math.max(param2, 2501);
        Dynamic<T> var1 = param1.merge(param1.createString("levelType"), param1.createString(param0.getSerialization()));
        return param3.update(References.CHUNK_GENERATOR_SETTINGS, var1, var0, SharedConstants.getCurrentVersion().getWorldVersion()).remove("levelType");
    }

    public PrimaryLevelData(LevelSettings param0) {
        this.fixerUpper = null;
        this.playerDataVersion = SharedConstants.getCurrentVersion().getWorldVersion();
        this.seed = param0.getSeed();
        this.gameType = param0.getGameType();
        this.difficulty = param0.getDifficulty();
        this.generateMapFeatures = param0.shouldGenerateMapFeatures();
        this.hardcore = param0.isHardcore();
        this.generatorProvider = param0.getGeneratorProvider();
        this.allowCommands = param0.getAllowCommands();
        this.generateBonusChest = param0.hasStartingBonusItems();
        this.levelName = param0.getLevelName();
        this.version = 19133;
        this.initialized = false;
        this.minecraftVersionName = SharedConstants.getCurrentVersion().getName();
        this.minecraftVersion = SharedConstants.getCurrentVersion().getWorldVersion();
        this.snapshot = !SharedConstants.getCurrentVersion().isStable();
        this.gameRules.assignFrom(param0.getGameRules(), null);
    }

    @Override
    public CompoundTag createTag(@Nullable CompoundTag param0) {
        this.updatePlayerTag();
        if (param0 == null) {
            param0 = this.loadedPlayerTag;
        }

        CompoundTag var0 = new CompoundTag();
        this.setTagData(var0, param0);
        return var0;
    }

    private void setTagData(CompoundTag param0, CompoundTag param1) {
        ListTag var0 = new ListTag();
        this.knownServerBrands.stream().map(StringTag::valueOf).forEach(var0::add);
        param0.put("ServerBrands", var0);
        param0.putBoolean("WasModded", this.wasModded);
        CompoundTag var1 = new CompoundTag();
        var1.putString("Name", SharedConstants.getCurrentVersion().getName());
        var1.putInt("Id", SharedConstants.getCurrentVersion().getWorldVersion());
        var1.putBoolean("Snapshot", !SharedConstants.getCurrentVersion().isStable());
        param0.put("Version", var1);
        param0.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        param0.putLong("RandomSeed", this.seed);
        param0.putString("generatorName", this.generatorProvider.getType().getSerialization());
        param0.putInt("generatorVersion", this.generatorProvider.getType().getVersion());
        CompoundTag var2 = (CompoundTag)this.generatorProvider.getSettings().convert(NbtOps.INSTANCE).getValue();
        if (!var2.isEmpty()) {
            param0.put("generatorOptions", var2);
        }

        if (this.legacyCustomOptions != null) {
            param0.putString("legacy_custom_options", this.legacyCustomOptions);
        }

        param0.putInt("GameType", this.gameType.getId());
        param0.putBoolean("MapFeatures", this.generateMapFeatures);
        param0.putInt("SpawnX", this.xSpawn);
        param0.putInt("SpawnY", this.ySpawn);
        param0.putInt("SpawnZ", this.zSpawn);
        param0.putLong("Time", this.gameTime);
        param0.putLong("DayTime", this.dayTime);
        param0.putLong("SizeOnDisk", this.sizeOnDisk);
        param0.putLong("LastPlayed", Util.getEpochMillis());
        param0.putString("LevelName", this.levelName);
        param0.putInt("version", 19133);
        param0.putInt("clearWeatherTime", this.clearWeatherTime);
        param0.putInt("rainTime", this.rainTime);
        param0.putBoolean("raining", this.raining);
        param0.putInt("thunderTime", this.thunderTime);
        param0.putBoolean("thundering", this.thundering);
        param0.putBoolean("hardcore", this.hardcore);
        param0.putBoolean("allowCommands", this.allowCommands);
        param0.putBoolean("BonusChest", this.generateBonusChest);
        param0.putBoolean("initialized", this.initialized);
        this.worldBorder.write(param0);
        param0.putByte("Difficulty", (byte)this.difficulty.getId());
        param0.putBoolean("DifficultyLocked", this.difficultyLocked);
        param0.put("GameRules", this.gameRules.createTag());
        CompoundTag var3 = new CompoundTag();

        for(Entry<DimensionType, CompoundTag> var4 : this.dimensionData.entrySet()) {
            var3.put(String.valueOf(var4.getKey().getId()), var4.getValue());
        }

        param0.put("DimensionData", var3);
        if (param1 != null) {
            param0.put("Player", param1);
        }

        CompoundTag var5 = new CompoundTag();
        ListTag var6 = new ListTag();

        for(String var7 : this.enabledDataPacks) {
            var6.add(StringTag.valueOf(var7));
        }

        var5.put("Enabled", var6);
        ListTag var8 = new ListTag();

        for(String var9 : this.disabledDataPacks) {
            var8.add(StringTag.valueOf(var9));
        }

        var5.put("Disabled", var8);
        param0.put("DataPacks", var5);
        if (this.customBossEvents != null) {
            param0.put("CustomBossEvents", this.customBossEvents);
        }

        param0.put("ScheduledEvents", this.scheduledEvents.store());
        param0.putInt("WanderingTraderSpawnDelay", this.wanderingTraderSpawnDelay);
        param0.putInt("WanderingTraderSpawnChance", this.wanderingTraderSpawnChance);
        if (this.wanderingTraderId != null) {
            param0.putUUID("WanderingTraderId", this.wanderingTraderId);
        }

    }

    @Override
    public long getSeed() {
        return this.seed;
    }

    @Override
    public int getXSpawn() {
        return this.xSpawn;
    }

    @Override
    public int getYSpawn() {
        return this.ySpawn;
    }

    @Override
    public int getZSpawn() {
        return this.zSpawn;
    }

    @Override
    public long getGameTime() {
        return this.gameTime;
    }

    @Override
    public long getDayTime() {
        return this.dayTime;
    }

    private void updatePlayerTag() {
        if (!this.upgradedPlayerTag && this.loadedPlayerTag != null) {
            if (this.playerDataVersion < SharedConstants.getCurrentVersion().getWorldVersion()) {
                if (this.fixerUpper == null) {
                    throw (NullPointerException)Util.pauseInIde(
                        new NullPointerException("Fixer Upper not set inside LevelData, and the player tag is not upgraded.")
                    );
                }

                this.loadedPlayerTag = NbtUtils.update(this.fixerUpper, DataFixTypes.PLAYER, this.loadedPlayerTag, this.playerDataVersion);
            }

            this.upgradedPlayerTag = true;
        }
    }

    @Override
    public CompoundTag getLoadedPlayerTag() {
        this.updatePlayerTag();
        return this.loadedPlayerTag;
    }

    @Override
    public void setXSpawn(int param0) {
        this.xSpawn = param0;
    }

    @Override
    public void setYSpawn(int param0) {
        this.ySpawn = param0;
    }

    @Override
    public void setZSpawn(int param0) {
        this.zSpawn = param0;
    }

    @Override
    public void setGameTime(long param0) {
        this.gameTime = param0;
    }

    @Override
    public void setDayTime(long param0) {
        this.dayTime = param0;
    }

    @Override
    public void setSpawn(BlockPos param0) {
        this.xSpawn = param0.getX();
        this.ySpawn = param0.getY();
        this.zSpawn = param0.getZ();
    }

    @Override
    public String getLevelName() {
        return this.levelName;
    }

    @Override
    public int getVersion() {
        return this.version;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public long getLastPlayed() {
        return this.lastPlayed;
    }

    @Override
    public int getClearWeatherTime() {
        return this.clearWeatherTime;
    }

    @Override
    public void setClearWeatherTime(int param0) {
        this.clearWeatherTime = param0;
    }

    @Override
    public boolean isThundering() {
        return this.thundering;
    }

    @Override
    public void setThundering(boolean param0) {
        this.thundering = param0;
    }

    @Override
    public int getThunderTime() {
        return this.thunderTime;
    }

    @Override
    public void setThunderTime(int param0) {
        this.thunderTime = param0;
    }

    @Override
    public boolean isRaining() {
        return this.raining;
    }

    @Override
    public void setRaining(boolean param0) {
        this.raining = param0;
    }

    @Override
    public int getRainTime() {
        return this.rainTime;
    }

    @Override
    public void setRainTime(int param0) {
        this.rainTime = param0;
    }

    @Override
    public GameType getGameType() {
        return this.gameType;
    }

    @Override
    public boolean shouldGenerateMapFeatures() {
        return this.generateMapFeatures;
    }

    @Override
    public void setGameType(GameType param0) {
        this.gameType = param0;
    }

    @Override
    public boolean isHardcore() {
        return this.hardcore;
    }

    @Override
    public LevelType getGeneratorType() {
        return this.generatorProvider.getType();
    }

    @Override
    public ChunkGeneratorProvider getGeneratorProvider() {
        return this.generatorProvider;
    }

    @Override
    public boolean getAllowCommands() {
        return this.allowCommands;
    }

    @Override
    public boolean isInitialized() {
        return this.initialized;
    }

    @Override
    public void setInitialized(boolean param0) {
        this.initialized = param0;
    }

    @Override
    public GameRules getGameRules() {
        return this.gameRules;
    }

    @Override
    public WorldBorder.Settings getWorldBorder() {
        return this.worldBorder;
    }

    @Override
    public void setWorldBorder(WorldBorder.Settings param0) {
        this.worldBorder = param0;
    }

    @Override
    public Difficulty getDifficulty() {
        return this.difficulty;
    }

    @Override
    public void setDifficulty(Difficulty param0) {
        this.difficulty = param0;
    }

    @Override
    public boolean isDifficultyLocked() {
        return this.difficultyLocked;
    }

    @Override
    public void setDifficultyLocked(boolean param0) {
        this.difficultyLocked = param0;
    }

    @Override
    public TimerQueue<MinecraftServer> getScheduledEvents() {
        return this.scheduledEvents;
    }

    @Override
    public void fillCrashReportCategory(CrashReportCategory param0) {
        ServerLevelData.super.fillCrashReportCategory(param0);
        WorldData.super.fillCrashReportCategory(param0);
    }

    @Override
    public CompoundTag getDimensionData(DimensionType param0) {
        CompoundTag var0 = this.dimensionData.get(param0);
        return var0 == null ? new CompoundTag() : var0;
    }

    @Override
    public void setDimensionData(DimensionType param0, CompoundTag param1) {
        this.dimensionData.put(param0, param1);
    }

    @Override
    public CompoundTag getDimensionData() {
        return this.getDimensionData(DimensionType.OVERWORLD);
    }

    @Override
    public void setDimensionData(CompoundTag param0) {
        this.setDimensionData(DimensionType.OVERWORLD, param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getMinecraftVersion() {
        return this.minecraftVersion;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isSnapshot() {
        return this.snapshot;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public String getMinecraftVersionName() {
        return this.minecraftVersionName;
    }

    @Override
    public Set<String> getDisabledDataPacks() {
        return this.disabledDataPacks;
    }

    @Override
    public Set<String> getEnabledDataPacks() {
        return this.enabledDataPacks;
    }

    @Nullable
    @Override
    public CompoundTag getCustomBossEvents() {
        return this.customBossEvents;
    }

    @Override
    public void setCustomBossEvents(@Nullable CompoundTag param0) {
        this.customBossEvents = param0;
    }

    @Override
    public int getWanderingTraderSpawnDelay() {
        return this.wanderingTraderSpawnDelay;
    }

    @Override
    public void setWanderingTraderSpawnDelay(int param0) {
        this.wanderingTraderSpawnDelay = param0;
    }

    @Override
    public int getWanderingTraderSpawnChance() {
        return this.wanderingTraderSpawnChance;
    }

    @Override
    public void setWanderingTraderSpawnChance(int param0) {
        this.wanderingTraderSpawnChance = param0;
    }

    @Override
    public void setWanderingTraderId(UUID param0) {
        this.wanderingTraderId = param0;
    }

    @Override
    public void setModdedInfo(String param0, boolean param1) {
        this.knownServerBrands.add(param0);
        this.wasModded |= param1;
    }

    @Override
    public boolean wasModded() {
        return this.wasModded;
    }

    @Override
    public Set<String> getKnownServerBrands() {
        return ImmutableSet.copyOf(this.knownServerBrands);
    }

    @Override
    public ServerLevelData overworldData() {
        return this;
    }

    @Override
    public LevelSettings getLevelSettings() {
        LevelSettings var0 = new LevelSettings(
            this.levelName, this.seed, this.gameType, this.generateMapFeatures, this.hardcore, this.difficulty, this.generatorProvider, this.gameRules.copy()
        );
        if (this.generateBonusChest) {
            var0 = var0.enableStartingBonusItems();
        }

        if (this.allowCommands) {
            var0 = var0.enableSinglePlayerCommands();
        }

        return var0;
    }
}
