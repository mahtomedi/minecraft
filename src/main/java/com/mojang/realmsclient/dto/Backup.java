package com.mojang.realmsclient.dto;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Backup extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public String backupId;
    public Date lastModifiedDate;
    public long size;
    private boolean uploadedVersion;
    public Map<String, String> metadata = Maps.newHashMap();
    public Map<String, String> changeList = Maps.newHashMap();

    public static Backup parse(JsonElement param0) {
        JsonObject var0 = param0.getAsJsonObject();
        Backup var1 = new Backup();

        try {
            var1.backupId = JsonUtils.getStringOr("backupId", var0, "");
            var1.lastModifiedDate = JsonUtils.getDateOr("lastModifiedDate", var0);
            var1.size = JsonUtils.getLongOr("size", var0, 0L);
            if (var0.has("metadata")) {
                JsonObject var2 = var0.getAsJsonObject("metadata");

                for(Entry<String, JsonElement> var4 : var2.entrySet()) {
                    if (!var4.getValue().isJsonNull()) {
                        var1.metadata.put(var4.getKey(), var4.getValue().getAsString());
                    }
                }
            }
        } catch (Exception var7) {
            LOGGER.error("Could not parse Backup: {}", var7.getMessage());
        }

        return var1;
    }

    public boolean isUploadedVersion() {
        return this.uploadedVersion;
    }

    public void setUploadedVersion(boolean param0) {
        this.uploadedVersion = param0;
    }
}
