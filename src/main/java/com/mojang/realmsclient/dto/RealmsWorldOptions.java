package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsWorldOptions extends ValueObject {
    public final boolean pvp;
    public final boolean spawnAnimals;
    public final boolean spawnMonsters;
    public final boolean spawnNPCs;
    public final int spawnProtection;
    public final boolean commandBlocks;
    public final boolean forceGameMode;
    public final int difficulty;
    public final int gameMode;
    private final String slotName;
    public final String version;
    public final RealmsServer.Compatibility compatibility;
    public long templateId;
    @Nullable
    public String templateImage;
    public boolean empty;
    private static final boolean DEFAULT_FORCE_GAME_MODE = false;
    private static final boolean DEFAULT_PVP = true;
    private static final boolean DEFAULT_SPAWN_ANIMALS = true;
    private static final boolean DEFAULT_SPAWN_MONSTERS = true;
    private static final boolean DEFAULT_SPAWN_NPCS = true;
    private static final int DEFAULT_SPAWN_PROTECTION = 0;
    private static final boolean DEFAULT_COMMAND_BLOCKS = false;
    private static final int DEFAULT_DIFFICULTY = 2;
    private static final int DEFAULT_GAME_MODE = 0;
    private static final String DEFAULT_SLOT_NAME = "";
    private static final String DEFAULT_VERSION = "";
    private static final RealmsServer.Compatibility DEFAULT_COMPATIBILITY = RealmsServer.Compatibility.UNVERIFIABLE;
    private static final long DEFAULT_TEMPLATE_ID = -1L;
    private static final String DEFAULT_TEMPLATE_IMAGE = null;

    public RealmsWorldOptions(
        boolean param0,
        boolean param1,
        boolean param2,
        boolean param3,
        int param4,
        boolean param5,
        int param6,
        int param7,
        boolean param8,
        String param9,
        String param10,
        RealmsServer.Compatibility param11
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
        this.version = param10;
        this.compatibility = param11;
    }

    public static RealmsWorldOptions createDefaults() {
        return new RealmsWorldOptions(true, true, true, true, 0, false, 2, 0, false, "", "", DEFAULT_COMPATIBILITY);
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
            JsonUtils.getRequiredStringOr("slotName", param0, ""),
            JsonUtils.getRequiredStringOr("version", param0, ""),
            RealmsServer.getCompatibility(JsonUtils.getRequiredStringOr("compatibility", param0, RealmsServer.Compatibility.UNVERIFIABLE.name()))
        );
        var0.templateId = JsonUtils.getLongOr("worldTemplateId", param0, -1L);
        var0.templateImage = JsonUtils.getStringOr("worldTemplateImage", param0, DEFAULT_TEMPLATE_IMAGE);
        return var0;
    }

    public String getSlotName(int param0) {
        if (Util.isBlank(this.slotName)) {
            return this.empty ? I18n.get("mco.configure.world.slot.empty") : this.getDefaultSlotName(param0);
        } else {
            return this.slotName;
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

        if (!Objects.equals(this.version, "")) {
            var0.addProperty("version", this.version);
        }

        if (this.compatibility != DEFAULT_COMPATIBILITY) {
            var0.addProperty("compatibility", this.compatibility.name());
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
            this.slotName,
            this.version,
            this.compatibility
        );
    }
}
