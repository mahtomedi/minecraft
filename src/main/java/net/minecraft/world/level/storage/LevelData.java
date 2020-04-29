package net.minecraft.world.level.storage;

import com.google.common.hash.Hashing;
import net.minecraft.CrashReportCategory;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.levelgen.ChunkGeneratorProvider;

public interface LevelData {
    long getSeed();

    static long obfuscateSeed(long param0) {
        return Hashing.sha256().hashLong(param0).asLong();
    }

    int getXSpawn();

    int getYSpawn();

    int getZSpawn();

    long getGameTime();

    long getDayTime();

    boolean isThundering();

    boolean isRaining();

    void setRaining(boolean var1);

    boolean isHardcore();

    LevelType getGeneratorType();

    ChunkGeneratorProvider getGeneratorProvider();

    GameRules getGameRules();

    Difficulty getDifficulty();

    boolean isDifficultyLocked();

    default void fillCrashReportCategory(CrashReportCategory param0) {
        param0.setDetail("Level seed", () -> String.valueOf(this.getSeed()));
        param0.setDetail("Level generator options", () -> this.getGeneratorProvider().getSettings().toString());
        param0.setDetail("Level spawn location", () -> CrashReportCategory.formatLocation(this.getXSpawn(), this.getYSpawn(), this.getZSpawn()));
        param0.setDetail("Level time", () -> String.format("%d game time, %d day time", this.getGameTime(), this.getDayTime()));
    }
}
