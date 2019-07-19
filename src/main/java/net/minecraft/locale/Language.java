package net.minecraft.locale;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import net.minecraft.Util;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Language {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern UNSUPPORTED_FORMAT_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");
    private static final Language SINGLETON = new Language();
    private final Map<String, String> storage = Maps.newHashMap();
    private long lastUpdateTime;

    public Language() {
        try (InputStream var0 = Language.class.getResourceAsStream("/assets/minecraft/lang/en_us.json")) {
            JsonElement var1 = new Gson().fromJson(new InputStreamReader(var0, StandardCharsets.UTF_8), JsonElement.class);
            JsonObject var2 = GsonHelper.convertToJsonObject(var1, "strings");

            for(Entry<String, JsonElement> var3 : var2.entrySet()) {
                String var4 = UNSUPPORTED_FORMAT_PATTERN.matcher(GsonHelper.convertToString(var3.getValue(), var3.getKey())).replaceAll("%$1s");
                this.storage.put(var3.getKey(), var4);
            }

            this.lastUpdateTime = Util.getMillis();
        } catch (JsonParseException | IOException var18) {
            LOGGER.error("Couldn't read strings from /assets/minecraft/lang/en_us.json", (Throwable)var18);
        }

    }

    public static Language getInstance() {
        return SINGLETON;
    }

    @OnlyIn(Dist.CLIENT)
    public static synchronized void forceData(Map<String, String> param0) {
        SINGLETON.storage.clear();
        SINGLETON.storage.putAll(param0);
        SINGLETON.lastUpdateTime = Util.getMillis();
    }

    public synchronized String getElement(String param0) {
        return this.getProperty(param0);
    }

    private String getProperty(String param0) {
        String var0 = this.storage.get(param0);
        return var0 == null ? param0 : var0;
    }

    public synchronized boolean exists(String param0) {
        return this.storage.containsKey(param0);
    }

    public long getLastUpdateTime() {
        return this.lastUpdateTime;
    }
}
