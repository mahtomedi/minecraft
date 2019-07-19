package net.minecraft.client.resources.language;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Locale {
    private static final Gson GSON = new Gson();
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern UNSUPPORTED_FORMAT_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");
    protected final Map<String, String> storage = Maps.newHashMap();

    public synchronized void loadFrom(ResourceManager param0, List<String> param1) {
        this.storage.clear();

        for(String var0 : param1) {
            String var1 = String.format("lang/%s.json", var0);

            for(String var2 : param0.getNamespaces()) {
                try {
                    ResourceLocation var3 = new ResourceLocation(var2, var1);
                    this.appendFrom(param0.getResources(var3));
                } catch (FileNotFoundException var9) {
                } catch (Exception var10) {
                    LOGGER.warn("Skipped language file: {}:{} ({})", var2, var1, var10.toString());
                }
            }
        }

    }

    private void appendFrom(List<Resource> param0) {
        for(Resource var0 : param0) {
            InputStream var1 = var0.getInputStream();

            try {
                this.appendFrom(var1);
            } finally {
                IOUtils.closeQuietly(var1);
            }
        }

    }

    private void appendFrom(InputStream param0) {
        JsonElement var0 = GSON.fromJson(new InputStreamReader(param0, StandardCharsets.UTF_8), JsonElement.class);
        JsonObject var1 = GsonHelper.convertToJsonObject(var0, "strings");

        for(Entry<String, JsonElement> var2 : var1.entrySet()) {
            String var3 = UNSUPPORTED_FORMAT_PATTERN.matcher(GsonHelper.convertToString(var2.getValue(), var2.getKey())).replaceAll("%$1s");
            this.storage.put(var2.getKey(), var3);
        }

    }

    private String getOrDefault(String param0) {
        String var0 = this.storage.get(param0);
        return var0 == null ? param0 : var0;
    }

    public String get(String param0, Object[] param1) {
        String var0 = this.getOrDefault(param0);

        try {
            return String.format(var0, param1);
        } catch (IllegalFormatException var5) {
            return "Format error: " + var0;
        }
    }

    public boolean has(String param0) {
        return this.storage.containsKey(param0);
    }
}
