package net.minecraft.world.level;

import com.mojang.serialization.Dynamic;
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

    public static LevelSettings parse(Dynamic<?> param0, WorldGenSettings param1) {
        GameType var0 = GameType.byId(param0.get("GameType").asInt(0));
        return new LevelSettings(
            param0.get("LevelName").asString(""),
            var0,
            param0.get("hardcore").asBoolean(false),
            param0.get("Difficulty").asNumber().map(param0x -> Difficulty.byId(param0x.byteValue())).result().orElse(Difficulty.NORMAL),
            param0.get("allowCommands").asBoolean(var0 == GameType.CREATIVE),
            new GameRules(param0.get("GameRules")),
            param1
        );
    }

    public String levelName() {
        return this.levelName;
    }

    public GameType gameType() {
        return this.gameType;
    }

    public boolean hardcore() {
        return this.hardcore;
    }

    public Difficulty difficulty() {
        return this.difficulty;
    }

    public boolean allowCommands() {
        return this.allowCommands;
    }

    public GameRules gameRules() {
        return this.gameRules;
    }

    public WorldGenSettings worldGenSettings() {
        return this.worldGenSettings;
    }

    public LevelSettings withGameType(GameType param0) {
        return new LevelSettings(this.levelName, param0, this.hardcore, this.difficulty, this.allowCommands, this.gameRules, this.worldGenSettings);
    }

    public LevelSettings withDifficulty(Difficulty param0) {
        return new LevelSettings(this.levelName, this.gameType, this.hardcore, param0, this.allowCommands, this.gameRules, this.worldGenSettings);
    }

    public LevelSettings copy() {
        return new LevelSettings(
            this.levelName, this.gameType, this.hardcore, this.difficulty, this.allowCommands, this.gameRules.copy(), this.worldGenSettings
        );
    }
}
