package net.minecraft.world.level.storage;

import com.google.common.hash.Hashing;
import java.util.UUID;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.ChunkGeneratorProvider;
import net.minecraft.world.level.timers.TimerQueue;

public interface LevelData {
    long getSeed();

    static long obfuscateSeed(long param0) {
        return Hashing.sha256().hashLong(param0).asLong();
    }

    int getXSpawn();

    void setXSpawn(int var1);

    int getYSpawn();

    void setYSpawn(int var1);

    int getZSpawn();

    void setZSpawn(int var1);

    default void setSpawn(BlockPos param0) {
        this.setXSpawn(param0.getX());
        this.setYSpawn(param0.getY());
        this.setZSpawn(param0.getZ());
    }

    long getGameTime();

    void setGameTime(long var1);

    long getDayTime();

    void setDayTime(long var1);

    String getLevelName();

    int getClearWeatherTime();

    void setClearWeatherTime(int var1);

    boolean isThundering();

    void setThundering(boolean var1);

    int getThunderTime();

    void setThunderTime(int var1);

    boolean isRaining();

    void setRaining(boolean var1);

    int getRainTime();

    void setRainTime(int var1);

    GameType getGameType();

    boolean shouldGenerateMapFeatures();

    void setGameType(GameType var1);

    boolean isHardcore();

    LevelType getGeneratorType();

    ChunkGeneratorProvider getGeneratorProvider();

    boolean getAllowCommands();

    boolean isInitialized();

    void setInitialized(boolean var1);

    GameRules getGameRules();

    WorldBorder.Settings getWorldBorder();

    void setWorldBorder(WorldBorder.Settings var1);

    Difficulty getDifficulty();

    boolean isDifficultyLocked();

    TimerQueue<MinecraftServer> getScheduledEvents();

    default void fillCrashReportCategory(CrashReportCategory param0) {
        param0.setDetail("Level name", this::getLevelName);
        param0.setDetail("Level seed", () -> String.valueOf(this.getSeed()));
        param0.setDetail(
            "Level generator",
            () -> {
                LevelType var0 = this.getGeneratorProvider().getType();
                return String.format(
                    "ID %02d - %s, ver %d. Features enabled: %b", var0.getId(), var0.getName(), var0.getVersion(), this.shouldGenerateMapFeatures()
                );
            }
        );
        param0.setDetail("Level generator options", () -> this.getGeneratorProvider().getSettings().toString());
        param0.setDetail("Level spawn location", () -> CrashReportCategory.formatLocation(this.getXSpawn(), this.getYSpawn(), this.getZSpawn()));
        param0.setDetail("Level time", () -> String.format("%d game time, %d day time", this.getGameTime(), this.getDayTime()));
        param0.setDetail(
            "Level weather",
            () -> String.format(
                    "Rain time: %d (now: %b), thunder time: %d (now: %b)", this.getRainTime(), this.isRaining(), this.getThunderTime(), this.isThundering()
                )
        );
        param0.setDetail(
            "Level game mode",
            () -> String.format(
                    "Game mode: %s (ID %d). Hardcore: %b. Cheats: %b",
                    this.getGameType().getName(),
                    this.getGameType().getId(),
                    this.isHardcore(),
                    this.getAllowCommands()
                )
        );
    }

    CompoundTag getDimensionData();

    void setDimensionData(CompoundTag var1);

    int getWanderingTraderSpawnDelay();

    void setWanderingTraderSpawnDelay(int var1);

    int getWanderingTraderSpawnChance();

    void setWanderingTraderSpawnChance(int var1);

    void setWanderingTraderId(UUID var1);
}
