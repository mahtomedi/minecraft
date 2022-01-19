package net.minecraft.locale;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringDecomposer;
import org.slf4j.Logger;

public abstract class Language {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final Pattern UNSUPPORTED_FORMAT_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
    public static final String DEFAULT = "en_us";
    private static volatile Language instance = loadDefault();

    private static Language loadDefault() {
        Builder<String, String> var0 = ImmutableMap.builder();
        BiConsumer<String, String> var1 = var0::put;
        String var2 = "/assets/minecraft/lang/en_us.json";

        try (InputStream var3 = Language.class.getResourceAsStream("/assets/minecraft/lang/en_us.json")) {
            loadFromJson(var3, var1);
        } catch (JsonParseException | IOException var8) {
            LOGGER.error("Couldn't read strings from {}", "/assets/minecraft/lang/en_us.json", var8);
        }

        final Map<String, String> var5 = var0.build();
        return new Language() {
            @Override
            public String getOrDefault(String param0) {
                return var5.getOrDefault(param0, param0);
            }

            @Override
            public boolean has(String param0) {
                return var5.containsKey(param0);
            }

            @Override
            public boolean isDefaultRightToLeft() {
                return false;
            }

            @Override
            public FormattedCharSequence getVisualOrder(FormattedText param0) {
                return param1 -> param0.visit(
                            (param1x, param2) -> StringDecomposer.iterateFormatted(param2, param1x, param1) ? Optional.empty() : FormattedText.STOP_ITERATION,
                            Style.EMPTY
                        )
                        .isPresent();
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

    public static void inject(Language param0) {
        instance = param0;
    }

    public abstract String getOrDefault(String var1);

    public abstract boolean has(String var1);

    public abstract boolean isDefaultRightToLeft();

    public abstract FormattedCharSequence getVisualOrder(FormattedText var1);

    public List<FormattedCharSequence> getVisualOrder(List<FormattedText> param0) {
        return param0.stream().map(this::getVisualOrder).collect(ImmutableList.toImmutableList());
    }
}
