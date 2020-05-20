package net.minecraft.world.level.storage;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SerializableUUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.timers.TimerCallbacks;
import net.minecraft.world.level.timers.TimerQueue;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PrimaryLevelData implements ServerLevelData, WorldData {
    private static final Logger LOGGER = LogManager.getLogger();
    private LevelSettings settings;
    private int xSpawn;
    private int ySpawn;
    private int zSpawn;
    private long gameTime;
    private long dayTime;
    @Nullable
    private final DataFixer fixerUpper;
    private final int playerDataVersion;
    private boolean upgradedPlayerTag;
    @Nullable
    private CompoundTag loadedPlayerTag;
    private final int version;
    private int clearWeatherTime;
    private boolean raining;
    private int rainTime;
    private boolean thundering;
    private int thunderTime;
    private boolean initialized;
    private boolean difficultyLocked;
    private WorldBorder.Settings worldBorder;
    private final Set<String> disabledDataPacks;
    private final Set<String> enabledDataPacks;
    private CompoundTag endDragonFightData;
    @Nullable
    private CompoundTag customBossEvents;
    private int wanderingTraderSpawnDelay;
    private int wanderingTraderSpawnChance;
    @Nullable
    private UUID wanderingTraderId;
    private final Set<String> knownServerBrands;
    private boolean wasModded;
    private final TimerQueue<MinecraftServer> scheduledEvents;

    private PrimaryLevelData(
        @Nullable DataFixer param0,
        int param1,
        @Nullable CompoundTag param2,
        boolean param3,
        int param4,
        int param5,
        int param6,
        long param7,
        long param8,
        int param9,
        int param10,
        int param11,
        boolean param12,
        int param13,
        boolean param14,
        boolean param15,
        boolean param16,
        WorldBorder.Settings param17,
        int param18,
        int param19,
        @Nullable UUID param20,
        LinkedHashSet<String> param21,
        LinkedHashSet<String> param22,
        Set<String> param23,
        TimerQueue<MinecraftServer> param24,
        @Nullable CompoundTag param25,
        CompoundTag param26,
        LevelSettings param27
    ) {
        this.fixerUpper = param0;
        this.wasModded = param3;
        this.settings = param27;
        this.xSpawn = param4;
        this.ySpawn = param5;
        this.zSpawn = param6;
        this.gameTime = param7;
        this.dayTime = param8;
        this.version = param9;
        this.clearWeatherTime = param10;
        this.rainTime = param11;
        this.raining = param12;
        this.thunderTime = param13;
        this.thundering = param14;
        this.initialized = param15;
        this.difficultyLocked = param16;
        this.worldBorder = param17;
        this.wanderingTraderSpawnDelay = param18;
        this.wanderingTraderSpawnChance = param19;
        this.wanderingTraderId = param20;
        this.knownServerBrands = param21;
        this.loadedPlayerTag = param2;
        this.playerDataVersion = param1;
        this.scheduledEvents = param24;
        this.enabledDataPacks = param22;
        this.disabledDataPacks = param23;
        this.customBossEvents = param25;
        this.endDragonFightData = param26;
    }

    public PrimaryLevelData(LevelSettings param0) {
        this(
            null,
            SharedConstants.getCurrentVersion().getWorldVersion(),
            null,
            false,
            0,
            0,
            0,
            0L,
            0L,
            19133,
            0,
            0,
            false,
            0,
            false,
            false,
            false,
            WorldBorder.DEFAULT_SETTINGS,
            0,
            0,
            null,
            Sets.newLinkedHashSet(),
            Sets.newLinkedHashSet(),
            Sets.newHashSet(),
            new TimerQueue<>(TimerCallbacks.SERVER_CALLBACKS),
            null,
            new CompoundTag(),
            param0.copy()
        );
    }

    public static PrimaryLevelData parse(
        Dynamic<Tag> param0, DataFixer param1, int param2, @Nullable CompoundTag param3, LevelSettings param4, LevelVersion param5
    ) {
        long var0 = param0.get("Time").asLong(0L);
        OptionalDynamic<?> var1 = param0.get("DataPacks");
        CompoundTag var2 = param0.get("DragonFight")
            .result()
            .map(Dynamic::getValue)
            .orElseGet(() -> param0.get("DimensionData").get("1").get("DragonFight").orElseEmptyMap().getValue());
        return new PrimaryLevelData(
            param1,
            param2,
            param3,
            param0.get("WasModded").asBoolean(false),
            param0.get("SpawnX").asInt(0),
            param0.get("SpawnY").asInt(0),
            param0.get("SpawnZ").asInt(0),
            var0,
            param0.get("DayTime").asLong(var0),
            param5.levelDataVersion(),
            param0.get("clearWeatherTime").asInt(0),
            param0.get("rainTime").asInt(0),
            param0.get("raining").asBoolean(false),
            param0.get("thunderTime").asInt(0),
            param0.get("thundering").asBoolean(false),
            param0.get("initialized").asBoolean(true),
            param0.get("DifficultyLocked").asBoolean(false),
            WorldBorder.Settings.read(param0, WorldBorder.DEFAULT_SETTINGS),
            param0.get("WanderingTraderSpawnDelay").asInt(0),
            param0.get("WanderingTraderSpawnChance").asInt(0),
            param0.get("WanderingTraderId").read(SerializableUUID.CODEC).result().map(SerializableUUID::value).orElse(null),
            param0.get("ServerBrands")
                .asStream()
                .flatMap(param0x -> Util.toStream(param0x.asString().result()))
                .collect(Collectors.toCollection(Sets::newLinkedHashSet)),
            var1.get("Enabled")
                .asStream()
                .flatMap(param0x -> Util.toStream(param0x.asString().result()))
                .collect(Collectors.toCollection(Sets::newLinkedHashSet)),
            var1.get("Disabled").asStream().flatMap(param0x -> Util.toStream(param0x.asString().result())).collect(Collectors.toSet()),
            new TimerQueue<>(TimerCallbacks.SERVER_CALLBACKS, param0.get("ScheduledEvents").asStream()),
            (CompoundTag)param0.get("CustomBossEvents").orElseEmptyMap().getValue(),
            var2,
            param4
        );
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
        WorldGenSettings.CODEC
            .encodeStart(NbtOps.INSTANCE, this.settings.worldGenSettings())
            .resultOrPartial(Util.prefix("WorldGenSettings: ", LOGGER::error))
            .ifPresent(param1x -> param0.put("WorldGenSettings", param1x));
        param0.putInt("GameType", this.settings.gameType().getId());
        param0.putInt("SpawnX", this.xSpawn);
        param0.putInt("SpawnY", this.ySpawn);
        param0.putInt("SpawnZ", this.zSpawn);
        param0.putLong("Time", this.gameTime);
        param0.putLong("DayTime", this.dayTime);
        param0.putLong("LastPlayed", Util.getEpochMillis());
        param0.putString("LevelName", this.settings.levelName());
        param0.putInt("version", 19133);
        param0.putInt("clearWeatherTime", this.clearWeatherTime);
        param0.putInt("rainTime", this.rainTime);
        param0.putBoolean("raining", this.raining);
        param0.putInt("thunderTime", this.thunderTime);
        param0.putBoolean("thundering", this.thundering);
        param0.putBoolean("hardcore", this.settings.hardcore());
        param0.putBoolean("allowCommands", this.settings.allowCommands());
        param0.putBoolean("initialized", this.initialized);
        this.worldBorder.write(param0);
        param0.putByte("Difficulty", (byte)this.settings.difficulty().getId());
        param0.putBoolean("DifficultyLocked", this.difficultyLocked);
        param0.put("GameRules", this.settings.gameRules().createTag());
        param0.put("DragonFight", this.endDragonFightData);
        if (param1 != null) {
            param0.put("Player", param1);
        }

        CompoundTag var2 = new CompoundTag();
        ListTag var3 = new ListTag();

        for(String var4 : this.enabledDataPacks) {
            var3.add(StringTag.valueOf(var4));
        }

        var2.put("Enabled", var3);
        ListTag var5 = new ListTag();

        for(String var6 : this.disabledDataPacks) {
            var5.add(StringTag.valueOf(var6));
        }

        var2.put("Disabled", var5);
        param0.put("DataPacks", var2);
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
        return this.settings.levelName();
    }

    @Override
    public int getVersion() {
        return this.version;
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
        return this.settings.gameType();
    }

    @Override
    public void setGameType(GameType param0) {
        this.settings = this.settings.withGameType(param0);
    }

    @Override
    public boolean isHardcore() {
        return this.settings.hardcore();
    }

    @Override
    public boolean getAllowCommands() {
        return this.settings.allowCommands();
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
        return this.settings.gameRules();
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
        return this.settings.difficulty();
    }

    @Override
    public void setDifficulty(Difficulty param0) {
        this.settings = this.settings.withDifficulty(param0);
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
    public WorldGenSettings worldGenSettings() {
        return this.settings.worldGenSettings();
    }

    @Override
    public CompoundTag endDragonFightData() {
        return this.endDragonFightData;
    }

    @Override
    public void setEndDragonFightData(CompoundTag param0) {
        this.endDragonFightData = param0;
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public LevelSettings getLevelSettings() {
        return this.settings.copy();
    }
}
