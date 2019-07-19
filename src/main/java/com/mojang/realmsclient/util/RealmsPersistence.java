package com.mojang.realmsclient.util;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import net.minecraft.realms.Realms;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;

@OnlyIn(Dist.CLIENT)
public class RealmsPersistence {
    public static RealmsPersistence.RealmsPersistenceData readFile() {
        File var0 = new File(Realms.getGameDirectoryPath(), "realms_persistence.json");
        Gson var1 = new Gson();

        try {
            return var1.fromJson(FileUtils.readFileToString(var0), RealmsPersistence.RealmsPersistenceData.class);
        } catch (IOException var3) {
            return new RealmsPersistence.RealmsPersistenceData();
        }
    }

    public static void writeFile(RealmsPersistence.RealmsPersistenceData param0) {
        File var0 = new File(Realms.getGameDirectoryPath(), "realms_persistence.json");
        Gson var1 = new Gson();
        String var2 = var1.toJson(param0);

        try {
            FileUtils.writeStringToFile(var0, var2);
        } catch (IOException var5) {
        }

    }

    @OnlyIn(Dist.CLIENT)
    public static class RealmsPersistenceData {
        public String newsLink;
        public boolean hasUnreadNews = false;

        private RealmsPersistenceData() {
        }
    }
}
