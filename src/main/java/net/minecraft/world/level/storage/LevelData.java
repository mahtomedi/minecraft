package net.minecraft.world.level.storage;

import net.minecraft.CrashReportCategory;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;

public interface LevelData {
    int getXSpawn();

    int getYSpawn();

    int getZSpawn();

    long getGameTime();

    long getDayTime();

    boolean isThundering();

    boolean isRaining();

    void setRaining(boolean var1);

    boolean isHardcore();

    GameRules getGameRules();

    Difficulty getDifficulty();

    boolean isDifficultyLocked();

    default void fillCrashReportCategory(CrashReportCategory param0) {
        param0.setDetail("Level spawn location", () -> CrashReportCategory.formatLocation(this.getXSpawn(), this.getYSpawn(), this.getZSpawn()));
        param0.setDetail("Level time", () -> String.format("%d game time, %d day time", this.getGameTime(), this.getDayTime()));
    }
}
