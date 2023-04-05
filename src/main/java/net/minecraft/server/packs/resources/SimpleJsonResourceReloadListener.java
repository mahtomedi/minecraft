package net.minecraft.server.packs.resources;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public abstract class SimpleJsonResourceReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Gson gson;
    private final String directory;

    public SimpleJsonResourceReloadListener(Gson param0, String param1) {
        this.gson = param0;
        this.directory = param1;
    }

    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager param0, ProfilerFiller param1) {
        Map<ResourceLocation, JsonElement> var0 = new HashMap<>();
        scanDirectory(param0, this.directory, this.gson, var0);
        return var0;
    }

    public static void scanDirectory(ResourceManager param0, String param1, Gson param2, Map<ResourceLocation, JsonElement> param3) {
        FileToIdConverter var0 = FileToIdConverter.json(param1);

        for(Entry<ResourceLocation, Resource> var1 : var0.listMatchingResources(param0).entrySet()) {
            ResourceLocation var2 = var1.getKey();
            ResourceLocation var3 = var0.fileToId(var2);

            try (Reader var4 = var1.getValue().openAsReader()) {
                JsonElement var5 = GsonHelper.fromJson(param2, var4, JsonElement.class);
                JsonElement var6 = param3.put(var3, var5);
                if (var6 != null) {
                    throw new IllegalStateException("Duplicate data file ignored with ID " + var3);
                }
            } catch (IllegalArgumentException | IOException | JsonParseException var14) {
                LOGGER.error("Couldn't parse data file {} from {}", var3, var2, var14);
            }
        }

    }
}
