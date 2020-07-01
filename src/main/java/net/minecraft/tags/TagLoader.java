package net.minecraft.tags;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TagLoader<T> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();
    private static final int PATH_SUFFIX_LENGTH = ".json".length();
    private final Function<ResourceLocation, Optional<T>> idToValue;
    private final String directory;
    private final String name;

    public TagLoader(Function<ResourceLocation, Optional<T>> param0, String param1, String param2) {
        this.idToValue = param0;
        this.directory = param1;
        this.name = param2;
    }

    public CompletableFuture<Map<ResourceLocation, Tag.Builder>> prepare(ResourceManager param0, Executor param1) {
        return CompletableFuture.supplyAsync(
            () -> {
                Map<ResourceLocation, Tag.Builder> var0 = Maps.newHashMap();
    
                for(ResourceLocation var1x : param0.listResources(this.directory, param0x -> param0x.endsWith(".json"))) {
                    String var2x = var1x.getPath();
                    ResourceLocation var3 = new ResourceLocation(
                        var1x.getNamespace(), var2x.substring(this.directory.length() + 1, var2x.length() - PATH_SUFFIX_LENGTH)
                    );
    
                    try {
                        for(Resource var4 : param0.getResources(var1x)) {
                            try (
                                InputStream var5 = var4.getInputStream();
                                Reader var6 = new BufferedReader(new InputStreamReader(var5, StandardCharsets.UTF_8));
                            ) {
                                JsonObject var7 = GsonHelper.fromJson(GSON, var6, JsonObject.class);
                                if (var7 == null) {
                                    LOGGER.error(
                                        "Couldn't load {} tag list {} from {} in data pack {} as it is empty or null",
                                        this.name,
                                        var3,
                                        var1x,
                                        var4.getSourceName()
                                    );
                                } else {
                                    var0.computeIfAbsent(var3, param0x -> Tag.Builder.tag()).addFromJson(var7, var4.getSourceName());
                                }
                            } catch (RuntimeException | IOException var57) {
                                LOGGER.error("Couldn't read {} tag list {} from {} in data pack {}", this.name, var3, var1x, var4.getSourceName(), var57);
                            } finally {
                                IOUtils.closeQuietly((Closeable)var4);
                            }
                        }
                    } catch (IOException var59) {
                        LOGGER.error("Couldn't read {} tag list {} from {}", this.name, var3, var1x, var59);
                    }
                }
    
                return var0;
            },
            param1
        );
    }

    public TagCollection<T> load(Map<ResourceLocation, Tag.Builder> param0) {
        Map<ResourceLocation, Tag<T>> var0 = Maps.newHashMap();
        Function<ResourceLocation, Tag<T>> var1 = var0::get;
        Function<ResourceLocation, T> var2 = param0x -> this.idToValue.apply(param0x).orElse((T)null);

        while(!param0.isEmpty()) {
            boolean var3 = false;
            Iterator<Entry<ResourceLocation, Tag.Builder>> var4 = param0.entrySet().iterator();

            while(var4.hasNext()) {
                Entry<ResourceLocation, Tag.Builder> var5 = var4.next();
                Optional<Tag<T>> var6 = var5.getValue().build(var1, var2);
                if (var6.isPresent()) {
                    var0.put(var5.getKey(), var6.get());
                    var4.remove();
                    var3 = true;
                }
            }

            if (!var3) {
                break;
            }
        }

        param0.forEach(
            (param2, param3) -> LOGGER.error(
                    "Couldn't load {} tag {} as it is missing following references: {}",
                    this.name,
                    param2,
                    param3.getUnresolvedEntries(var1, var2).map(Objects::toString).collect(Collectors.joining(","))
                )
        );
        return TagCollection.of(var0);
    }
}
