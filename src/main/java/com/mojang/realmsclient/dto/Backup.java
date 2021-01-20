package com.mojang.realmsclient.dto;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Backup extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
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
                        var1.metadata.put(format(var4.getKey()), var4.getValue().getAsString());
                    }
                }
            }
        } catch (Exception var7) {
            LOGGER.error("Could not parse Backup: {}", var7.getMessage());
        }

        return var1;
    }

    private static String format(String param0) {
        String[] var0 = param0.split("_");
        StringBuilder var1 = new StringBuilder();

        for(String var2 : var0) {
            if (var2 != null && var2.length() >= 1) {
                if ("of".equals(var2)) {
                    var1.append(var2).append(" ");
                } else {
                    char var3 = Character.toUpperCase(var2.charAt(0));
                    var1.append(var3).append(var2.substring(1)).append(" ");
                }
            }
        }

        return var1.toString();
    }

    public boolean isUploadedVersion() {
        return this.uploadedVersion;
    }

    public void setUploadedVersion(boolean param0) {
        this.uploadedVersion = param0;
    }
}
