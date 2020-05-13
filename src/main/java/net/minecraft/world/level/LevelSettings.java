package net.minecraft.world.level;

import net.minecraft.world.Difficulty;
import net.minecraft.world.level.levelgen.WorldGenSettings;

public final class LevelSettings {
    private final String levelName;
    private final GameType gameType;
    private final boolean hardcore;
    private final Difficulty difficulty;
    private final boolean allowCommands;
    private final GameRules gameRules;
    private final WorldGenSettings worldGenSettings;

    public LevelSettings(String param0, GameType param1, boolean param2, Difficulty param3, boolean param4, GameRules param5, WorldGenSettings param6) {
        this.levelName = param0;
        this.gameType = param1;
        this.hardcore = param2;
        this.difficulty = param3;
        this.allowCommands = param4;
        this.gameRules = param5;
        this.worldGenSettings = param6;
    }

    public WorldGenSettings worldGenSettings() {
        return this.worldGenSettings;
    }

    public GameType getGameType() {
        return this.gameType;
    }

    public boolean isHardcore() {
        return this.hardcore;
    }

    public boolean getAllowCommands() {
        return this.allowCommands;
    }

    public String getLevelName() {
        return this.levelName;
    }

    public Difficulty getDifficulty() {
        return this.difficulty;
    }

    public GameRules getGameRules() {
        return this.gameRules;
    }
}
