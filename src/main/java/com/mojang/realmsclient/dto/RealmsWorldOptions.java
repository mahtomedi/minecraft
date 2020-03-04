package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.Objects;
import net.minecraft.client.resources.language.I18n;
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
    private static final String DEFAULT_TEMPLATE_IMAGE = null;

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

    public static RealmsWorldOptions createDefaults() {
        return new RealmsWorldOptions(true, true, true, true, 0, false, 2, 0, false, "");
    }

    public static RealmsWorldOptions createEmptyDefaults() {
        RealmsWorldOptions var0 = createDefaults();
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
        var0.templateImage = JsonUtils.getStringOr("worldTemplateImage", param0, DEFAULT_TEMPLATE_IMAGE);
        var0.adventureMap = JsonUtils.getBooleanOr("adventureMap", param0, false);
        return var0;
    }

    public String getSlotName(int param0) {
        if (this.slotName != null && !this.slotName.isEmpty()) {
            return this.slotName;
        } else {
            return this.empty ? I18n.get("mco.configure.world.slot.empty") : this.getDefaultSlotName(param0);
        }
    }

    public String getDefaultSlotName(int param0) {
        return I18n.get("mco.configure.world.slot", param0);
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

        if (!Objects.equals(this.slotName, "")) {
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
