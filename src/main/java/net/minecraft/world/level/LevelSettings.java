package net.minecraft.world.level;

import net.minecraft.world.Difficulty;
import net.minecraft.world.level.levelgen.ChunkGeneratorProvider;

public final class LevelSettings {
    private final String levelName;
    private final long seed;
    private final GameType gameType;
    private final boolean generateMapFeatures;
    private final boolean hardcore;
    private final ChunkGeneratorProvider generatorProvider;
    private final Difficulty difficulty;
    private boolean allowCommands;
    private boolean startingBonusItems;
    private final GameRules gameRules;

    public LevelSettings(String param0, long param1, GameType param2, boolean param3, boolean param4, Difficulty param5, ChunkGeneratorProvider param6) {
        this(param0, param1, param2, param3, param4, param5, param6, new GameRules());
    }

    public LevelSettings(
        String param0, long param1, GameType param2, boolean param3, boolean param4, Difficulty param5, ChunkGeneratorProvider param6, GameRules param7
    ) {
        this.levelName = param0;
        this.seed = param1;
        this.gameType = param2;
        this.generateMapFeatures = param3;
        this.hardcore = param4;
        this.generatorProvider = param6;
        this.difficulty = param5;
        this.gameRules = param7;
    }

    public LevelSettings enableStartingBonusItems() {
        this.startingBonusItems = true;
        return this;
    }

    public LevelSettings enableSinglePlayerCommands() {
        this.allowCommands = true;
        return this;
    }

    public boolean hasStartingBonusItems() {
        return this.startingBonusItems;
    }

    public long getSeed() {
        return this.seed;
    }

    public GameType getGameType() {
        return this.gameType;
    }

    public boolean isHardcore() {
        return this.hardcore;
    }

    public boolean shouldGenerateMapFeatures() {
        return this.generateMapFeatures;
    }

    public ChunkGeneratorProvider getGeneratorProvider() {
        return this.generatorProvider;
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
