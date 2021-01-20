package net.minecraft.server.packs;

import com.mojang.bridge.game.GameVersion;

public enum PackType {
    CLIENT_RESOURCES("assets", com.mojang.bridge.game.PackType.RESOURCE),
    SERVER_DATA("data", com.mojang.bridge.game.PackType.DATA);

    private final String directory;
    private final com.mojang.bridge.game.PackType bridgeType;

    private PackType(String param0, com.mojang.bridge.game.PackType param1) {
        this.directory = param0;
        this.bridgeType = param1;
    }

    public String getDirectory() {
        return this.directory;
    }

    public int getVersion(GameVersion param0) {
        return param0.getPackVersion(this.bridgeType);
    }
}
