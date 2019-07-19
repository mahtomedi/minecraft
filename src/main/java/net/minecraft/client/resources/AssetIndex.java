package net.minecraft.client.resources;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class AssetIndex {
    protected static final Logger LOGGER = LogManager.getLogger();
    private final Map<String, File> mapping = Maps.newHashMap();

    protected AssetIndex() {
    }

    public AssetIndex(File param0, String param1) {
        File var0 = new File(param0, "objects");
        File var1 = new File(param0, "indexes/" + param1 + ".json");
        BufferedReader var2 = null;

        try {
            var2 = Files.newReader(var1, StandardCharsets.UTF_8);
            JsonObject var3 = GsonHelper.parse(var2);
            JsonObject var4 = GsonHelper.getAsJsonObject(var3, "objects", null);
            if (var4 != null) {
                for(Entry<String, JsonElement> var5 : var4.entrySet()) {
                    JsonObject var6 = (JsonObject)var5.getValue();
                    String var7 = var5.getKey();
                    String[] var8 = var7.split("/", 2);
                    String var9 = var8.length == 1 ? var8[0] : var8[0] + ":" + var8[1];
                    String var10 = GsonHelper.getAsString(var6, "hash");
                    File var11 = new File(var0, var10.substring(0, 2) + "/" + var10);
                    this.mapping.put(var9, var11);
                }
            }
        } catch (JsonParseException var20) {
            LOGGER.error("Unable to parse resource index file: {}", var1);
        } catch (FileNotFoundException var21) {
            LOGGER.error("Can't find the resource index file: {}", var1);
        } finally {
            IOUtils.closeQuietly((Reader)var2);
        }

    }

    @Nullable
    public File getFile(ResourceLocation param0) {
        return this.getFile(param0.toString());
    }

    @Nullable
    public File getFile(String param0) {
        return this.mapping.get(param0);
    }

    public Collection<String> getFiles(String param0, int param1, Predicate<String> param2) {
        return this.mapping
            .keySet()
            .stream()
            .filter(param0x -> !param0x.endsWith(".mcmeta"))
            .map(ResourceLocation::new)
            .map(ResourceLocation::getPath)
            .filter(param1x -> param1x.startsWith(param0 + "/"))
            .filter(param2)
            .collect(Collectors.toList());
    }
}
