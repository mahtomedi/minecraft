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
        Map<ResourceLocation, JsonElement> var0 = Maps.newHashMap();
        FileToIdConverter var1 = FileToIdConverter.json(this.directory);

        for(Entry<ResourceLocation, Resource> var2 : var1.listMatchingResources(param0).entrySet()) {
            ResourceLocation var3 = var2.getKey();
            ResourceLocation var4 = var1.fileToId(var3);

            try (Reader var5 = var2.getValue().openAsReader()) {
                JsonElement var6 = GsonHelper.fromJson(this.gson, var5, JsonElement.class);
                JsonElement var7 = var0.put(var4, var6);
                if (var7 != null) {
                    throw new IllegalStateException("Duplicate data file ignored with ID " + var4);
                }
            } catch (IllegalArgumentException | IOException | JsonParseException var14) {
                LOGGER.error("Couldn't parse data file {} from {}", var4, var3, var14);
            }
        }

        return var0;
    }
}
