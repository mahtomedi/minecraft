package net.minecraft.tags;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TagCollection<T> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();
    private static final int PATH_SUFFIX_LENGTH = ".json".length();
    private Map<ResourceLocation, Tag<T>> tags = ImmutableMap.of();
    private final Function<ResourceLocation, Optional<T>> idToValue;
    private final String directory;
    private final boolean ordered;
    private final String name;

    public TagCollection(Function<ResourceLocation, Optional<T>> param0, String param1, boolean param2, String param3) {
        this.idToValue = param0;
        this.directory = param1;
        this.ordered = param2;
        this.name = param3;
    }

    @Nullable
    public Tag<T> getTag(ResourceLocation param0) {
        return this.tags.get(param0);
    }

    public Tag<T> getTagOrEmpty(ResourceLocation param0) {
        Tag<T> var0 = this.tags.get(param0);
        return var0 == null ? new Tag<>(param0) : var0;
    }

    public Collection<ResourceLocation> getAvailableTags() {
        return this.tags.keySet();
    }

    @OnlyIn(Dist.CLIENT)
    public Collection<ResourceLocation> getMatchingTags(T param0) {
        List<ResourceLocation> var0 = Lists.newArrayList();

        for(Entry<ResourceLocation, Tag<T>> var1 : this.tags.entrySet()) {
            if (var1.getValue().contains(param0)) {
                var0.add(var1.getKey());
            }
        }

        return var0;
    }

    public CompletableFuture<Map<ResourceLocation, Tag.Builder<T>>> prepare(ResourceManager param0, Executor param1) {
        return CompletableFuture.supplyAsync(
            () -> {
                Map<ResourceLocation, Tag.Builder<T>> var0 = Maps.newHashMap();
    
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
                                        "Couldn't load {} tag list {} from {} in data pack {} as it's empty or null",
                                        this.name,
                                        var3,
                                        var1x,
                                        var4.getSourceName()
                                    );
                                } else {
                                    var0.computeIfAbsent(var3, param0x -> Util.make(Tag.Builder.tag(), param0xx -> param0xx.keepOrder(this.ordered)))
                                        .addFromJson(this.idToValue, var7);
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

    public void load(Map<ResourceLocation, Tag.Builder<T>> param0) {
        Map<ResourceLocation, Tag<T>> var0 = Maps.newHashMap();

        while(!param0.isEmpty()) {
            boolean var1 = false;
            Iterator<Entry<ResourceLocation, Tag.Builder<T>>> var2 = param0.entrySet().iterator();

            while(var2.hasNext()) {
                Entry<ResourceLocation, Tag.Builder<T>> var3 = var2.next();
                Tag.Builder<T> var4 = var3.getValue();
                if (var4.canBuild(var0::get)) {
                    var1 = true;
                    ResourceLocation var5 = var3.getKey();
                    var0.put(var5, var4.build(var5));
                    var2.remove();
                }
            }

            if (!var1) {
                param0.forEach(
                    (param0x, param1) -> LOGGER.error(
                            "Couldn't load {} tag {} as it either references another tag that doesn't exist, or ultimately references itself",
                            this.name,
                            param0x
                        )
                );
                break;
            }
        }

        param0.forEach((param1, param2) -> {
        });
        this.replace(var0);
    }

    protected void replace(Map<ResourceLocation, Tag<T>> param0) {
        this.tags = ImmutableMap.copyOf(param0);
    }

    public Map<ResourceLocation, Tag<T>> getAllTags() {
        return this.tags;
    }
}
