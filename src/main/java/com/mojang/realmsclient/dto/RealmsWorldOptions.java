package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsWorldOptions extends ValueObject {
    public Boolean pvp;
    public Boolean spawnAnimals;
    public Boolean spawnMonsters;
    public Boolean spawnNPCs;
    public Integer spawnProtection;
    public Boolean commandBlocks;
    public Boolean forceGameMode;
    public Integer difficulty;
    public Integer gameMode;
    public String slotName;
    public long templateId;
    public String templateImage;
    public boolean adventureMap;
    public boolean empty;
    private static final boolean forceGameModeDefault = false;
    private static final boolean pvpDefault = true;
    private static final boolean spawnAnimalsDefault = true;
    private static final boolean spawnMonstersDefault = true;
    private static final boolean spawnNPCsDefault = true;
    private static final int spawnProtectionDefault = 0;
    private static final boolean commandBlocksDefault = false;
    private static final int difficultyDefault = 2;
    private static final int gameModeDefault = 0;
    private static final String slotNameDefault = "";
    private static final long templateIdDefault = -1L;
    private static final String templateImageDefault = null;
    private static final boolean adventureMapDefault = false;

    public RealmsWorldOptions(
        Boolean param0,
        Boolean param1,
        Boolean param2,
        Boolean param3,
        Integer param4,
        Boolean param5,
        Integer param6,
        Integer param7,
        Boolean param8,
        String param9
    ) {
        this.pvp = param0;
        this.spawnAnimals = param1;
        this.spawnMonsters = param2;
        this.spawnNPCs = param3;
        this.spawnProtection = param4;
        this.commandBlocks = param5;
        this.difficulty = param6;
        this.gameMode = param7;
        this.forceGameMode = param8;
        this.slotName = param9;
    }

    public static RealmsWorldOptions getDefaults() {
        return new RealmsWorldOptions(true, true, true, true, 0, false, 2, 0, false, "");
    }

    public static RealmsWorldOptions getEmptyDefaults() {
        RealmsWorldOptions var0 = new RealmsWorldOptions(true, true, true, true, 0, false, 2, 0, false, "");
        var0.setEmpty(true);
        return var0;
    }

    public void setEmpty(boolean param0) {
        this.empty = param0;
    }

    public static RealmsWorldOptions parse(JsonObject param0) {
        RealmsWorldOptions var0 = new RealmsWorldOptions(
            JsonUtils.getBooleanOr("pvp", param0, true),
            JsonUtils.getBooleanOr("spawnAnimals", param0, true),
            JsonUtils.getBooleanOr("spawnMonsters", param0, true),
            JsonUtils.getBooleanOr("spawnNPCs", param0, true),
            JsonUtils.getIntOr("spawnProtection", param0, 0),
            JsonUtils.getBooleanOr("commandBlocks", param0, false),
            JsonUtils.getIntOr("difficulty", param0, 2),
            JsonUtils.getIntOr("gameMode", param0, 0),
            JsonUtils.getBooleanOr("forceGameMode", param0, false),
            JsonUtils.getStringOr("slotName", param0, "")
        );
        var0.templateId = JsonUtils.getLongOr("worldTemplateId", param0, -1L);
        var0.templateImage = JsonUtils.getStringOr("worldTemplateImage", param0, templateImageDefault);
        var0.adventureMap = JsonUtils.getBooleanOr("adventureMap", param0, false);
        return var0;
    }

    public String getSlotName(int param0) {
        if (this.slotName != null && !this.slotName.isEmpty()) {
            return this.slotName;
        } else {
            return this.empty ? RealmsScreen.getLocalizedString("mco.configure.world.slot.empty") : this.getDefaultSlotName(param0);
        }
    }

    public String getDefaultSlotName(int param0) {
        return RealmsScreen.getLocalizedString("mco.configure.world.slot", param0);
    }

    public String toJson() {
        JsonObject var0 = new JsonObject();
        if (!this.pvp) {
            var0.addProperty("pvp", this.pvp);
        }

        if (!this.spawnAnimals) {
            var0.addProperty("spawnAnimals", this.spawnAnimals);
        }

        if (!this.spawnMonsters) {
            var0.addProperty("spawnMonsters", this.spawnMonsters);
        }

        if (!this.spawnNPCs) {
            var0.addProperty("spawnNPCs", this.spawnNPCs);
        }

        if (this.spawnProtection != 0) {
            var0.addProperty("spawnProtection", this.spawnProtection);
        }

        if (this.commandBlocks) {
            var0.addProperty("commandBlocks", this.commandBlocks);
        }

        if (this.difficulty != 2) {
            var0.addProperty("difficulty", this.difficulty);
        }

        if (this.gameMode != 0) {
            var0.addProperty("gameMode", this.gameMode);
        }

        if (this.forceGameMode) {
            var0.addProperty("forceGameMode", this.forceGameMode);
        }

        if (this.slotName != null && !this.slotName.equals("")) {
            var0.addProperty("slotName", this.slotName);
        }

        return var0.toString();
    }

    public RealmsWorldOptions clone() {
        return new RealmsWorldOptions(
            this.pvp,
            this.spawnAnimals,
            this.spawnMonsters,
            this.spawnNPCs,
            this.spawnProtection,
            this.commandBlocks,
            this.difficulty,
            this.gameMode,
            this.forceGameMode,
            this.slotName
        );
    }
}
