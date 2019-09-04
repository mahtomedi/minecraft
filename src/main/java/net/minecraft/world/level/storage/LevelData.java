package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
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
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.timers.TimerCallbacks;
import net.minecraft.world.level.timers.TimerQueue;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LevelData {
    private String minecraftVersionName;
    private int minecraftVersion;
    private boolean snapshot;
    public static final Difficulty DEFAULT_DIFFICULTY = Difficulty.NORMAL;
    private long seed;
    private LevelType generator = LevelType.NORMAL;
    private CompoundTag generatorOptions = new CompoundTag();
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
    private String levelName;
    private int version;
    private int clearWeatherTime;
    private boolean raining;
    private int rainTime;
    private boolean thundering;
    private int thunderTime;
    private GameType gameType;
    private boolean generateMapFeatures;
    private boolean hardcore;
    private boolean allowCommands;
    private boolean initialized;
    private Difficulty difficulty;
    private boolean difficultyLocked;
    private double borderX;
    private double borderZ;
    private double borderSize = 6.0E7;
    private long borderSizeLerpTime;
    private double borderSizeLerpTarget;
    private double borderSafeZone = 5.0;
    private double borderDamagePerBlock = 0.2;
    private int borderWarningBlocks = 5;
    private int borderWarningTime = 15;
    private final Set<String> disabledDataPacks = Sets.newHashSet();
    private final Set<String> enabledDataPacks = Sets.newLinkedHashSet();
    private final Map<DimensionType, CompoundTag> dimensionData = Maps.newIdentityHashMap();
    private CompoundTag customBossEvents;
    private int wanderingTraderSpawnDelay;
    private int wanderingTraderSpawnChance;
    private UUID wanderingTraderId;
    private final GameRules gameRules = new GameRules();
    private final TimerQueue<MinecraftServer> scheduledEvents = new TimerQueue<>(TimerCallbacks.SERVER_CALLBACKS);

    protected LevelData() {
        this.fixerUpper = null;
        this.playerDataVersion = SharedConstants.getCurrentVersion().getWorldVersion();
        this.setGeneratorOptions(new CompoundTag());
    }

    public LevelData(CompoundTag param0, DataFixer param1, int param2, @Nullable CompoundTag param3) {
        this.fixerUpper = param1;
        if (param0.contains("Version", 10)) {
            CompoundTag var0 = param0.getCompound("Version");
            this.minecraftVersionName = var0.getString("Name");
            this.minecraftVersion = var0.getInt("Id");
            this.snapshot = var0.getBoolean("Snapshot");
        }

        this.seed = param0.getLong("RandomSeed");
        if (param0.contains("generatorName", 8)) {
            String var1 = param0.getString("generatorName");
            this.generator = LevelType.getLevelType(var1);
            if (this.generator == null) {
                this.generator = LevelType.NORMAL;
            } else if (this.generator == LevelType.CUSTOMIZED) {
                this.legacyCustomOptions = param0.getString("generatorOptions");
            } else if (this.generator.hasReplacement()) {
                int var2 = 0;
                if (param0.contains("generatorVersion", 99)) {
                    var2 = param0.getInt("generatorVersion");
                }

                this.generator = this.generator.getReplacementForVersion(var2);
            }

            this.setGeneratorOptions(param0.getCompound("generatorOptions"));
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

        if (param0.contains("BorderCenterX", 99)) {
            this.borderX = param0.getDouble("BorderCenterX");
        }

        if (param0.contains("BorderCenterZ", 99)) {
            this.borderZ = param0.getDouble("BorderCenterZ");
        }

        if (param0.contains("BorderSize", 99)) {
            this.borderSize = param0.getDouble("BorderSize");
        }

        if (param0.contains("BorderSizeLerpTime", 99)) {
            this.borderSizeLerpTime = param0.getLong("BorderSizeLerpTime");
        }

        if (param0.contains("BorderSizeLerpTarget", 99)) {
            this.borderSizeLerpTarget = param0.getDouble("BorderSizeLerpTarget");
        }

        if (param0.contains("BorderSafeZone", 99)) {
            this.borderSafeZone = param0.getDouble("BorderSafeZone");
        }

        if (param0.contains("BorderDamagePerBlock", 99)) {
            this.borderDamagePerBlock = param0.getDouble("BorderDamagePerBlock");
        }

        if (param0.contains("BorderWarningBlocks", 99)) {
            this.borderWarningBlocks = param0.getInt("BorderWarningBlocks");
        }

        if (param0.contains("BorderWarningTime", 99)) {
            this.borderWarningTime = param0.getInt("BorderWarningTime");
        }

        if (param0.contains("DimensionData", 10)) {
            CompoundTag var3 = param0.getCompound("DimensionData");

            for(String var4 : var3.getAllKeys()) {
                this.dimensionData.put(DimensionType.getById(Integer.parseInt(var4)), var3.getCompound(var4));
            }
        }

        if (param0.contains("DataPacks", 10)) {
            CompoundTag var5 = param0.getCompound("DataPacks");
            ListTag var6 = var5.getList("Disabled", 8);

            for(int var7 = 0; var7 < var6.size(); ++var7) {
                this.disabledDataPacks.add(var6.getString(var7));
            }

            ListTag var8 = var5.getList("Enabled", 8);

            for(int var9 = 0; var9 < var8.size(); ++var9) {
                this.enabledDataPacks.add(var8.getString(var9));
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

        if (param0.contains("WanderingTraderId", 8)) {
            this.wanderingTraderId = UUID.fromString(param0.getString("WanderingTraderId"));
        }

    }

    public LevelData(LevelSettings param0, String param1) {
        this.fixerUpper = null;
        this.playerDataVersion = SharedConstants.getCurrentVersion().getWorldVersion();
        this.setLevelSettings(param0);
        this.levelName = param1;
        this.difficulty = DEFAULT_DIFFICULTY;
        this.initialized = false;
    }

    public void setLevelSettings(LevelSettings param0) {
        this.seed = param0.getSeed();
        this.gameType = param0.getGameType();
        this.generateMapFeatures = param0.isGenerateMapFeatures();
        this.hardcore = param0.isHardcore();
        this.generator = param0.getLevelType();
        this.setGeneratorOptions(Dynamic.convert(JsonOps.INSTANCE, NbtOps.INSTANCE, param0.getLevelTypeOptions()));
        this.allowCommands = param0.getAllowCommands();
    }

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
        CompoundTag var0 = new CompoundTag();
        var0.putString("Name", SharedConstants.getCurrentVersion().getName());
        var0.putInt("Id", SharedConstants.getCurrentVersion().getWorldVersion());
        var0.putBoolean("Snapshot", !SharedConstants.getCurrentVersion().isStable());
        param0.put("Version", var0);
        param0.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        param0.putLong("RandomSeed", this.seed);
        param0.putString("generatorName", this.generator.getSerialization());
        param0.putInt("generatorVersion", this.generator.getVersion());
        if (!this.generatorOptions.isEmpty()) {
            param0.put("generatorOptions", this.generatorOptions);
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
        param0.putInt("version", this.version);
        param0.putInt("clearWeatherTime", this.clearWeatherTime);
        param0.putInt("rainTime", this.rainTime);
        param0.putBoolean("raining", this.raining);
        param0.putInt("thunderTime", this.thunderTime);
        param0.putBoolean("thundering", this.thundering);
        param0.putBoolean("hardcore", this.hardcore);
        param0.putBoolean("allowCommands", this.allowCommands);
        param0.putBoolean("initialized", this.initialized);
        param0.putDouble("BorderCenterX", this.borderX);
        param0.putDouble("BorderCenterZ", this.borderZ);
        param0.putDouble("BorderSize", this.borderSize);
        param0.putLong("BorderSizeLerpTime", this.borderSizeLerpTime);
        param0.putDouble("BorderSafeZone", this.borderSafeZone);
        param0.putDouble("BorderDamagePerBlock", this.borderDamagePerBlock);
        param0.putDouble("BorderSizeLerpTarget", this.borderSizeLerpTarget);
        param0.putDouble("BorderWarningBlocks", (double)this.borderWarningBlocks);
        param0.putDouble("BorderWarningTime", (double)this.borderWarningTime);
        if (this.difficulty != null) {
            param0.putByte("Difficulty", (byte)this.difficulty.getId());
        }

        param0.putBoolean("DifficultyLocked", this.difficultyLocked);
        param0.put("GameRules", this.gameRules.createTag());
        CompoundTag var1 = new CompoundTag();

        for(Entry<DimensionType, CompoundTag> var2 : this.dimensionData.entrySet()) {
            var1.put(String.valueOf(var2.getKey().getId()), var2.getValue());
        }

        param0.put("DimensionData", var1);
        if (param1 != null) {
            param0.put("Player", param1);
        }

        CompoundTag var3 = new CompoundTag();
        ListTag var4 = new ListTag();

        for(String var5 : this.enabledDataPacks) {
            var4.add(new StringTag(var5));
        }

        var3.put("Enabled", var4);
        ListTag var6 = new ListTag();

        for(String var7 : this.disabledDataPacks) {
            var6.add(new StringTag(var7));
        }

        var3.put("Disabled", var6);
        param0.put("DataPacks", var3);
        if (this.customBossEvents != null) {
            param0.put("CustomBossEvents", this.customBossEvents);
        }

        param0.put("ScheduledEvents", this.scheduledEvents.store());
        param0.putInt("WanderingTraderSpawnDelay", this.wanderingTraderSpawnDelay);
        param0.putInt("WanderingTraderSpawnChance", this.wanderingTraderSpawnChance);
        if (this.wanderingTraderId != null) {
            param0.putString("WanderingTraderId", this.wanderingTraderId.toString());
        }

    }

    public long getSeed() {
        return this.seed;
    }

    public static long obfuscateSeed(long param0) {
        return Hashing.sha256().hashLong(param0).asLong();
    }

    public int getXSpawn() {
        return this.xSpawn;
    }

    public int getYSpawn() {
        return this.ySpawn;
    }

    public int getZSpawn() {
        return this.zSpawn;
    }

    public long getGameTime() {
        return this.gameTime;
    }

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

    public CompoundTag getLoadedPlayerTag() {
        this.updatePlayerTag();
        return this.loadedPlayerTag;
    }

    @OnlyIn(Dist.CLIENT)
    public void setXSpawn(int param0) {
        this.xSpawn = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public void setYSpawn(int param0) {
        this.ySpawn = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public void setZSpawn(int param0) {
        this.zSpawn = param0;
    }

    public void setGameTime(long param0) {
        this.gameTime = param0;
    }

    public void setDayTime(long param0) {
        this.dayTime = param0;
    }

    public void setSpawn(BlockPos param0) {
        this.xSpawn = param0.getX();
        this.ySpawn = param0.getY();
        this.zSpawn = param0.getZ();
    }

    public String getLevelName() {
        return this.levelName;
    }

    public void setLevelName(String param0) {
        this.levelName = param0;
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(int param0) {
        this.version = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public long getLastPlayed() {
        return this.lastPlayed;
    }

    public int getClearWeatherTime() {
        return this.clearWeatherTime;
    }

    public void setClearWeatherTime(int param0) {
        this.clearWeatherTime = param0;
    }

    public boolean isThundering() {
        return this.thundering;
    }

    public void setThundering(boolean param0) {
        this.thundering = param0;
    }

    public int getThunderTime() {
        return this.thunderTime;
    }

    public void setThunderTime(int param0) {
        this.thunderTime = param0;
    }

    public boolean isRaining() {
        return this.raining;
    }

    public void setRaining(boolean param0) {
        this.raining = param0;
    }

    public int getRainTime() {
        return this.rainTime;
    }

    public void setRainTime(int param0) {
        this.rainTime = param0;
    }

    public GameType getGameType() {
        return this.gameType;
    }

    public boolean isGenerateMapFeatures() {
        return this.generateMapFeatures;
    }

    public void setGenerateMapFeatures(boolean param0) {
        this.generateMapFeatures = param0;
    }

    public void setGameType(GameType param0) {
        this.gameType = param0;
    }

    public boolean isHardcore() {
        return this.hardcore;
    }

    public void setHardcore(boolean param0) {
        this.hardcore = param0;
    }

    public LevelType getGeneratorType() {
        return this.generator;
    }

    public void setGenerator(LevelType param0) {
        this.generator = param0;
    }

    public CompoundTag getGeneratorOptions() {
        return this.generatorOptions;
    }

    public void setGeneratorOptions(CompoundTag param0) {
        this.generatorOptions = param0;
    }

    public boolean getAllowCommands() {
        return this.allowCommands;
    }

    public void setAllowCommands(boolean param0) {
        this.allowCommands = param0;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    public void setInitialized(boolean param0) {
        this.initialized = param0;
    }

    public GameRules getGameRules() {
        return this.gameRules;
    }

    public double getBorderX() {
        return this.borderX;
    }

    public double getBorderZ() {
        return this.borderZ;
    }

    public double getBorderSize() {
        return this.borderSize;
    }

    public void setBorderSize(double param0) {
        this.borderSize = param0;
    }

    public long getBorderSizeLerpTime() {
        return this.borderSizeLerpTime;
    }

    public void setBorderSizeLerpTime(long param0) {
        this.borderSizeLerpTime = param0;
    }

    public double getBorderSizeLerpTarget() {
        return this.borderSizeLerpTarget;
    }

    public void setBorderSizeLerpTarget(double param0) {
        this.borderSizeLerpTarget = param0;
    }

    public void setBorderZ(double param0) {
        this.borderZ = param0;
    }

    public void setBorderX(double param0) {
        this.borderX = param0;
    }

    public double getBorderSafeZone() {
        return this.borderSafeZone;
    }

    public void setBorderSafeZone(double param0) {
        this.borderSafeZone = param0;
    }

    public double getBorderDamagePerBlock() {
        return this.borderDamagePerBlock;
    }

    public void setBorderDamagePerBlock(double param0) {
        this.borderDamagePerBlock = param0;
    }

    public int getBorderWarningBlocks() {
        return this.borderWarningBlocks;
    }

    public int getBorderWarningTime() {
        return this.borderWarningTime;
    }

    public void setBorderWarningBlocks(int param0) {
        this.borderWarningBlocks = param0;
    }

    public void setBorderWarningTime(int param0) {
        this.borderWarningTime = param0;
    }

    public Difficulty getDifficulty() {
        return this.difficulty;
    }

    public void setDifficulty(Difficulty param0) {
        this.difficulty = param0;
    }

    public boolean isDifficultyLocked() {
        return this.difficultyLocked;
    }

    public void setDifficultyLocked(boolean param0) {
        this.difficultyLocked = param0;
    }

    public TimerQueue<MinecraftServer> getScheduledEvents() {
        return this.scheduledEvents;
    }

    public void fillCrashReportCategory(CrashReportCategory param0) {
        param0.setDetail("Level name", () -> this.levelName);
        param0.setDetail("Level seed", () -> String.valueOf(this.seed));
        param0.setDetail(
            "Level generator",
            () -> String.format(
                    "ID %02d - %s, ver %d. Features enabled: %b",
                    this.generator.getId(),
                    this.generator.getName(),
                    this.generator.getVersion(),
                    this.generateMapFeatures
                )
        );
        param0.setDetail("Level generator options", () -> this.generatorOptions.toString());
        param0.setDetail("Level spawn location", () -> CrashReportCategory.formatLocation(this.xSpawn, this.ySpawn, this.zSpawn));
        param0.setDetail("Level time", () -> String.format("%d game time, %d day time", this.gameTime, this.dayTime));
        param0.setDetail("Level storage version", () -> {
            String var0 = "Unknown?";

            try {
                switch(this.version) {
                    case 19132:
                        var0 = "McRegion";
                        break;
                    case 19133:
                        var0 = "Anvil";
                }
            } catch (Throwable var3) {
            }

            return String.format("0x%05X - %s", this.version, var0);
        });
        param0.setDetail(
            "Level weather",
            () -> String.format("Rain time: %d (now: %b), thunder time: %d (now: %b)", this.rainTime, this.raining, this.thunderTime, this.thundering)
        );
        param0.setDetail(
            "Level game mode",
            () -> String.format(
                    "Game mode: %s (ID %d). Hardcore: %b. Cheats: %b", this.gameType.getName(), this.gameType.getId(), this.hardcore, this.allowCommands
                )
        );
    }

    public CompoundTag getDimensionData(DimensionType param0) {
        CompoundTag var0 = this.dimensionData.get(param0);
        return var0 == null ? new CompoundTag() : var0;
    }

    public void setDimensionData(DimensionType param0, CompoundTag param1) {
        this.dimensionData.put(param0, param1);
    }

    @OnlyIn(Dist.CLIENT)
    public int getMinecraftVersion() {
        return this.minecraftVersion;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isSnapshot() {
        return this.snapshot;
    }

    @OnlyIn(Dist.CLIENT)
    public String getMinecraftVersionName() {
        return this.minecraftVersionName;
    }

    public Set<String> getDisabledDataPacks() {
        return this.disabledDataPacks;
    }

    public Set<String> getEnabledDataPacks() {
        return this.enabledDataPacks;
    }

    @Nullable
    public CompoundTag getCustomBossEvents() {
        return this.customBossEvents;
    }

    public void setCustomBossEvents(@Nullable CompoundTag param0) {
        this.customBossEvents = param0;
    }

    public int getWanderingTraderSpawnDelay() {
        return this.wanderingTraderSpawnDelay;
    }

    public void setWanderingTraderSpawnDelay(int param0) {
        this.wanderingTraderSpawnDelay = param0;
    }

    public int getWanderingTraderSpawnChance() {
        return this.wanderingTraderSpawnChance;
    }

    public void setWanderingTraderSpawnChance(int param0) {
        this.wanderingTraderSpawnChance = param0;
    }

    public void setWanderingTraderId(UUID param0) {
        this.wanderingTraderId = param0;
    }
}
