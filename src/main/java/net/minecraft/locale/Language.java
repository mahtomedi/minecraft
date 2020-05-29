package net.minecraft.locale;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
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
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Language {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();
    private static final Pattern UNSUPPORTED_FORMAT_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
    private static volatile Language instance = loadDefault();

    private static Language loadDefault() {
        Builder<String, String> var0 = ImmutableMap.builder();
        BiConsumer<String, String> var1 = var0::put;

        try (InputStream var2 = Language.class.getResourceAsStream("/assets/minecraft/lang/en_us.json")) {
            loadFromJson(var2, var1);
        } catch (JsonParseException | IOException var15) {
            LOGGER.error("Couldn't read strings from /assets/minecraft/lang/en_us.json", (Throwable)var15);
        }

        final Map<String, String> var4 = var0.build();
        return new Language() {
            @Override
            public String getOrDefault(String param0) {
                return var4.getOrDefault(param0, param0);
            }

            @Override
            public boolean has(String param0) {
                return var4.containsKey(param0);
            }

            @OnlyIn(Dist.CLIENT)
            @Override
            public boolean requiresReordering() {
                return false;
            }

            @Override
            public String reorder(String param0, boolean param1) {
                return param0;
            }
        };
    }

    public static void loadFromJson(InputStream param0, BiConsumer<String, String> param1) {
        JsonObject var0 = GSON.fromJson(new InputStreamReader(param0, StandardCharsets.UTF_8), JsonObject.class);

        for(Entry<String, JsonElement> var1 : var0.entrySet()) {
            String var2 = UNSUPPORTED_FORMAT_PATTERN.matcher(GsonHelper.convertToString(var1.getValue(), var1.getKey())).replaceAll("%$1s");
            param1.accept(var1.getKey(), var2);
        }

    }

    public static Language getInstance() {
        return instance;
    }

    @OnlyIn(Dist.CLIENT)
    public static void inject(Language param0) {
        instance = param0;
    }

    public abstract String getOrDefault(String var1);

    public abstract boolean has(String var1);

    @OnlyIn(Dist.CLIENT)
    public abstract boolean requiresReordering();

    public abstract String reorder(String var1, boolean var2);
}
