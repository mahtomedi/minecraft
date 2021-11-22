package net.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.bridge.game.PackType;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.DataVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DetectedVersion implements WorldVersion {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final WorldVersion BUILT_IN = new DetectedVersion();
    private final String id;
    private final String name;
    private final boolean stable;
    private final DataVersion worldVersion;
    private final int protocolVersion;
    private final int resourcePackVersion;
    private final int dataPackVersion;
    private final Date buildTime;
    private final String releaseTarget;

    private DetectedVersion() {
        this.id = UUID.randomUUID().toString().replaceAll("-", "");
        this.name = "1.18-pre6";
        this.stable = false;
        this.worldVersion = new DataVersion(2853, "main");
        this.protocolVersion = SharedConstants.getProtocolVersion();
        this.resourcePackVersion = 8;
        this.dataPackVersion = 8;
        this.buildTime = new Date();
        this.releaseTarget = "1.18";
    }

    private DetectedVersion(JsonObject param0) {
        this.id = GsonHelper.getAsString(param0, "id");
        this.name = GsonHelper.getAsString(param0, "name");
        this.releaseTarget = GsonHelper.getAsString(param0, "release_target");
        this.stable = GsonHelper.getAsBoolean(param0, "stable");
        this.worldVersion = new DataVersion(GsonHelper.getAsInt(param0, "world_version"), GsonHelper.getAsString(param0, "series_id", DataVersion.MAIN_SERIES));
        this.protocolVersion = GsonHelper.getAsInt(param0, "protocol_version");
        JsonObject var0 = GsonHelper.getAsJsonObject(param0, "pack_version");
        this.resourcePackVersion = GsonHelper.getAsInt(var0, "resource");
        this.dataPackVersion = GsonHelper.getAsInt(var0, "data");
        this.buildTime = Date.from(ZonedDateTime.parse(GsonHelper.getAsString(param0, "build_time")).toInstant());
    }

    public static WorldVersion tryDetectVersion() {
        try {
            DetectedVersion var21;
            try (InputStream var0 = DetectedVersion.class.getResourceAsStream("/version.json")) {
                if (var0 == null) {
                    LOGGER.warn("Missing version information!");
                    return BUILT_IN;
                }

                try (InputStreamReader var1 = new InputStreamReader(var0)) {
                    var21 = new DetectedVersion(GsonHelper.parse(var1));
                }
            }

            return var21;
        } catch (JsonParseException | IOException var8) {
            throw new IllegalStateException("Game version information is corrupt", var8);
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getReleaseTarget() {
        return this.releaseTarget;
    }

    @Override
    public DataVersion getDataVersion() {
        return this.worldVersion;
    }

    @Override
    public int getProtocolVersion() {
        return this.protocolVersion;
    }

    @Override
    public int getPackVersion(PackType param0) {
        return param0 == PackType.DATA ? this.dataPackVersion : this.resourcePackVersion;
    }

    @Override
    public Date getBuildTime() {
        return this.buildTime;
    }

    @Override
    public boolean isStable() {
        return this.stable;
    }
}
