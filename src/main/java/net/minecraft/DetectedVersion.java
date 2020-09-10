package net.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.bridge.game.GameVersion;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DetectedVersion implements GameVersion {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final GameVersion BUILT_IN = new DetectedVersion();
    private final String id;
    private final String name;
    private final boolean stable;
    private final int worldVersion;
    private final int protocolVersion;
    private final int packVersion;
    private final Date buildTime;
    private final String releaseTarget;

    private DetectedVersion() {
        this.id = UUID.randomUUID().toString().replaceAll("-", "");
        this.name = "1.16.3";
        this.stable = true;
        this.worldVersion = 2580;
        this.protocolVersion = 753;
        this.packVersion = 6;
        this.buildTime = new Date();
        this.releaseTarget = "1.16.3";
    }

    private DetectedVersion(JsonObject param0) {
        this.id = GsonHelper.getAsString(param0, "id");
        this.name = GsonHelper.getAsString(param0, "name");
        this.releaseTarget = GsonHelper.getAsString(param0, "release_target");
        this.stable = GsonHelper.getAsBoolean(param0, "stable");
        this.worldVersion = GsonHelper.getAsInt(param0, "world_version");
        this.protocolVersion = GsonHelper.getAsInt(param0, "protocol_version");
        this.packVersion = GsonHelper.getAsInt(param0, "pack_version");
        this.buildTime = Date.from(ZonedDateTime.parse(GsonHelper.getAsString(param0, "build_time")).toInstant());
    }

    public static GameVersion tryDetectVersion() {
        try (InputStream var0 = DetectedVersion.class.getResourceAsStream("/version.json")) {
            if (var0 == null) {
                LOGGER.warn("Missing version information!");
                return BUILT_IN;
            } else {
                DetectedVersion var4;
                try (InputStreamReader var1 = new InputStreamReader(var0)) {
                    var4 = new DetectedVersion(GsonHelper.parse(var1));
                }

                return var4;
            }
        } catch (JsonParseException | IOException var34) {
            throw new IllegalStateException("Game version information is corrupt", var34);
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
    public int getWorldVersion() {
        return this.worldVersion;
    }

    @Override
    public int getProtocolVersion() {
        return this.protocolVersion;
    }

    @Override
    public int getPackVersion() {
        return this.packVersion;
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
