package com.mojang.realmsclient.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.Iterator;
import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class BackupList extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    public List<Backup> backups;

    public static BackupList parse(String param0) {
        JsonParser var0 = new JsonParser();
        BackupList var1 = new BackupList();
        var1.backups = Lists.newArrayList();

        try {
            JsonElement var2 = var0.parse(param0).getAsJsonObject().get("backups");
            if (var2.isJsonArray()) {
                Iterator<JsonElement> var3 = var2.getAsJsonArray().iterator();

                while(var3.hasNext()) {
                    var1.backups.add(Backup.parse(var3.next()));
                }
            }
        } catch (Exception var5) {
            LOGGER.error("Could not parse BackupList: " + var5.getMessage());
        }

        return var1;
    }
}
