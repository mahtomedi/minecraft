package net.minecraft.world.level;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.level.storage.LevelData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class LevelSettings {
    private final long seed;
    private final GameType gameType;
    private final boolean generateMapFeatures;
    private final boolean hardcore;
    private final LevelType levelType;
    private boolean allowCommands;
    private boolean startingBonusItems;
    private JsonElement levelTypeOptions = new JsonObject();

    public LevelSettings(long param0, GameType param1, boolean param2, boolean param3, LevelType param4) {
        this.seed = param0;
        this.gameType = param1;
        this.generateMapFeatures = param2;
        this.hardcore = param3;
        this.levelType = param4;
    }

    public LevelSettings(LevelData param0) {
        this(param0.getSeed(), param0.getGameType(), param0.isGenerateMapFeatures(), param0.isHardcore(), param0.getGeneratorType());
    }

    public LevelSettings enableStartingBonusItems() {
        this.startingBonusItems = true;
        return this;
    }

    @OnlyIn(Dist.CLIENT)
    public LevelSettings enableSinglePlayerCommands() {
        this.allowCommands = true;
        return this;
    }

    public LevelSettings setLevelTypeOptions(JsonElement param0) {
        this.levelTypeOptions = param0;
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

    public boolean isGenerateMapFeatures() {
        return this.generateMapFeatures;
    }

    public LevelType getLevelType() {
        return this.levelType;
    }

    public boolean getAllowCommands() {
        return this.allowCommands;
    }

    public JsonElement getLevelTypeOptions() {
        return this.levelTypeOptions;
    }
}
