package com.mojang.realmsclient.util;

import com.google.gson.annotations.SerializedName;
import com.mojang.realmsclient.dto.GuardedSerializer;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;

@OnlyIn(Dist.CLIENT)
public class RealmsPersistence {
    private static final GuardedSerializer GSON = new GuardedSerializer();

    public RealmsPersistence.RealmsPersistenceData read() {
        return readFile();
    }

    public void save(RealmsPersistence.RealmsPersistenceData param0) {
        writeFile(param0);
    }

    public static RealmsPersistence.RealmsPersistenceData readFile() {
        File var0 = getPathToData();

        try {
            String var1 = FileUtils.readFileToString(var0, StandardCharsets.UTF_8);
            RealmsPersistence.RealmsPersistenceData var2 = GSON.fromJson(var1, RealmsPersistence.RealmsPersistenceData.class);
            return var2 != null ? var2 : new RealmsPersistence.RealmsPersistenceData();
        } catch (IOException var31) {
            return new RealmsPersistence.RealmsPersistenceData();
        }
    }

    public static void writeFile(RealmsPersistence.RealmsPersistenceData param0) {
        File var0 = getPathToData();

        try {
            FileUtils.writeStringToFile(var0, GSON.toJson(param0), StandardCharsets.UTF_8);
        } catch (IOException var3) {
        }

    }

    private static File getPathToData() {
        return new File(Minecraft.getInstance().gameDirectory, "realms_persistence.json");
    }

    @OnlyIn(Dist.CLIENT)
    public static class RealmsPersistenceData implements ReflectionBasedSerialization {
        @SerializedName("newsLink")
        public String newsLink;
        @SerializedName("hasUnreadNews")
        public boolean hasUnreadNews;
    }
}
