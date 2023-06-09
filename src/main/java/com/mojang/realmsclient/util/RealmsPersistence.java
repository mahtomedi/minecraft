package com.mojang.realmsclient.util;

import com.google.gson.annotations.SerializedName;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.GuardedSerializer;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsPersistence {
    private static final String FILE_NAME = "realms_persistence.json";
    private static final GuardedSerializer GSON = new GuardedSerializer();
    private static final Logger LOGGER = LogUtils.getLogger();

    public RealmsPersistence.RealmsPersistenceData read() {
        return readFile();
    }

    public void save(RealmsPersistence.RealmsPersistenceData param0) {
        writeFile(param0);
    }

    public static RealmsPersistence.RealmsPersistenceData readFile() {
        Path var0 = getPathToData();

        try {
            String var1 = Files.readString(var0, StandardCharsets.UTF_8);
            RealmsPersistence.RealmsPersistenceData var2 = GSON.fromJson(var1, RealmsPersistence.RealmsPersistenceData.class);
            if (var2 != null) {
                return var2;
            }
        } catch (NoSuchFileException var31) {
        } catch (Exception var4) {
            LOGGER.warn("Failed to read Realms storage {}", var0, var4);
        }

        return new RealmsPersistence.RealmsPersistenceData();
    }

    public static void writeFile(RealmsPersistence.RealmsPersistenceData param0) {
        Path var0 = getPathToData();

        try {
            Files.writeString(var0, GSON.toJson(param0), StandardCharsets.UTF_8);
        } catch (Exception var3) {
        }

    }

    private static Path getPathToData() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("realms_persistence.json");
    }

    @OnlyIn(Dist.CLIENT)
    public static class RealmsPersistenceData implements ReflectionBasedSerialization {
        @SerializedName("newsLink")
        public String newsLink;
        @SerializedName("hasUnreadNews")
        public boolean hasUnreadNews;
    }
}
