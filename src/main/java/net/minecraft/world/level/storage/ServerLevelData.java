package net.minecraft.world.level.storage;

import java.util.UUID;
import net.minecraft.CrashReportCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.timers.TimerQueue;

public interface ServerLevelData extends WritableLevelData {
    String getLevelName();

    void setThundering(boolean var1);

    int getRainTime();

    void setRainTime(int var1);

    void setThunderTime(int var1);

    int getThunderTime();

    @Override
    default void fillCrashReportCategory(CrashReportCategory param0) {
        WritableLevelData.super.fillCrashReportCategory(param0);
        param0.setDetail("Level name", this::getLevelName);
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
        param0.setDetail(
            "Level weather",
            () -> String.format(
                    "Rain time: %d (now: %b), thunder time: %d (now: %b)", this.getRainTime(), this.isRaining(), this.getThunderTime(), this.isThundering()
                )
        );
    }

    int getClearWeatherTime();

    void setClearWeatherTime(int var1);

    CompoundTag getDimensionData();

    void setDimensionData(CompoundTag var1);

    int getWanderingTraderSpawnDelay();

    void setWanderingTraderSpawnDelay(int var1);

    int getWanderingTraderSpawnChance();

    void setWanderingTraderSpawnChance(int var1);

    void setWanderingTraderId(UUID var1);

    GameType getGameType();

    void setWorldBorder(WorldBorder.Settings var1);

    WorldBorder.Settings getWorldBorder();

    boolean isInitialized();

    void setInitialized(boolean var1);

    boolean getAllowCommands();

    void setGameType(GameType var1);

    TimerQueue<MinecraftServer> getScheduledEvents();
}
