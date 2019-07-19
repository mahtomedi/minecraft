package net.minecraft.server.packs.resources;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class SimpleJsonResourceReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, JsonObject>> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int PATH_SUFFIX_LENGTH = ".json".length();
    private final Gson gson;
    private final String directory;

    public SimpleJsonResourceReloadListener(Gson param0, String param1) {
        this.gson = param0;
        this.directory = param1;
    }

    protected Map<ResourceLocation, JsonObject> prepare(ResourceManager param0, ProfilerFiller param1) {
        Map<ResourceLocation, JsonObject> var0 = Maps.newHashMap();
        int var1 = this.directory.length() + 1;

        for(ResourceLocation var2 : param0.listResources(this.directory, param0x -> param0x.endsWith(".json"))) {
            String var3 = var2.getPath();
            ResourceLocation var4 = new ResourceLocation(var2.getNamespace(), var3.substring(var1, var3.length() - PATH_SUFFIX_LENGTH));

            try (
                Resource var5 = param0.getResource(var2);
                InputStream var6 = var5.getInputStream();
                Reader var7 = new BufferedReader(new InputStreamReader(var6, StandardCharsets.UTF_8));
            ) {
                JsonObject var8 = GsonHelper.fromJson(this.gson, var7, JsonObject.class);
                if (var8 != null) {
                    JsonObject var9 = var0.put(var4, var8);
                    if (var9 != null) {
                        throw new IllegalStateException("Duplicate data file ignored with ID " + var4);
                    }
                } else {
                    LOGGER.error("Couldn't load data file {} from {} as it's null or empty", var4, var2);
                }
            } catch (IllegalArgumentException | IOException | JsonParseException var68) {
                LOGGER.error("Couldn't parse data file {} from {}", var4, var2, var68);
            }
        }

        return var0;
    }
}
