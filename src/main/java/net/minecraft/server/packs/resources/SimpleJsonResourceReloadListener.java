package net.minecraft.server.packs.resources;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public abstract class SimpleJsonResourceReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PATH_SUFFIX = ".json";
    private static final int PATH_SUFFIX_LENGTH = ".json".length();
    private final Gson gson;
    private final String directory;

    public SimpleJsonResourceReloadListener(Gson param0, String param1) {
        this.gson = param0;
        this.directory = param1;
    }

    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager param0, ProfilerFiller param1) {
        Map<ResourceLocation, JsonElement> var0 = Maps.newHashMap();
        int var1 = this.directory.length() + 1;

        for(Entry<ResourceLocation, Resource> var2 : param0.listResources(this.directory, param0x -> param0x.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation var3 = var2.getKey();
            String var4 = var3.getPath();
            ResourceLocation var5 = new ResourceLocation(var3.getNamespace(), var4.substring(var1, var4.length() - PATH_SUFFIX_LENGTH));

            try (Reader var6 = var2.getValue().openAsReader()) {
                JsonElement var7 = GsonHelper.fromJson(this.gson, var6, JsonElement.class);
                if (var7 != null) {
                    JsonElement var8 = var0.put(var5, var7);
                    if (var8 != null) {
                        throw new IllegalStateException("Duplicate data file ignored with ID " + var5);
                    }
                } else {
                    LOGGER.error("Couldn't load data file {} from {} as it's null or empty", var5, var3);
                }
            } catch (IllegalArgumentException | IOException | JsonParseException var15) {
                LOGGER.error("Couldn't parse data file {} from {}", var5, var3, var15);
            }
        }

        return var0;
    }
}
