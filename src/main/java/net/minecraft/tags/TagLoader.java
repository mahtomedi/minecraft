package net.minecraft.tags;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

public class TagLoader<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final String PATH_SUFFIX = ".json";
    private static final int PATH_SUFFIX_LENGTH = ".json".length();
    private final Function<ResourceLocation, Optional<T>> idToValue;
    private final String directory;

    public TagLoader(Function<ResourceLocation, Optional<T>> param0, String param1) {
        this.idToValue = param0;
        this.directory = param1;
    }

    public Map<ResourceLocation, Tag.Builder> load(ResourceManager param0) {
        Map<ResourceLocation, Tag.Builder> var0 = Maps.newHashMap();

        for(Entry<ResourceLocation, List<Resource>> var1 : param0.listResourceStacks(this.directory, param0x -> param0x.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation var2 = var1.getKey();
            String var3 = var2.getPath();
            ResourceLocation var4 = new ResourceLocation(var2.getNamespace(), var3.substring(this.directory.length() + 1, var3.length() - PATH_SUFFIX_LENGTH));

            for(Resource var5 : var1.getValue()) {
                try (Reader var6 = var5.openAsReader()) {
                    JsonObject var7 = GsonHelper.fromJson(GSON, var6, JsonObject.class);
                    if (var7 == null) {
                        throw new NullPointerException("Invalid JSON contents");
                    }

                    var0.computeIfAbsent(var4, param0x -> Tag.Builder.tag()).addFromJson(var7, var5.sourcePackId());
                } catch (Exception var15) {
                    LOGGER.error("Couldn't read tag list {} from {} in data pack {}", var4, var2, var5.sourcePackId(), var15);
                }
            }
        }

        return var0;
    }

    private static void visitDependenciesAndElement(
        Map<ResourceLocation, Tag.Builder> param0,
        Multimap<ResourceLocation, ResourceLocation> param1,
        Set<ResourceLocation> param2,
        ResourceLocation param3,
        BiConsumer<ResourceLocation, Tag.Builder> param4
    ) {
        if (param2.add(param3)) {
            param1.get(param3).forEach(param4x -> visitDependenciesAndElement(param0, param1, param2, param4x, param4));
            Tag.Builder var0 = param0.get(param3);
            if (var0 != null) {
                param4.accept(param3, var0);
            }

        }
    }

    private static boolean isCyclic(Multimap<ResourceLocation, ResourceLocation> param0, ResourceLocation param1, ResourceLocation param2) {
        Collection<ResourceLocation> var0 = param0.get(param2);
        return var0.contains(param1) ? true : var0.stream().anyMatch(param2x -> isCyclic(param0, param1, param2x));
    }

    private static void addDependencyIfNotCyclic(Multimap<ResourceLocation, ResourceLocation> param0, ResourceLocation param1, ResourceLocation param2) {
        if (!isCyclic(param0, param1, param2)) {
            param0.put(param1, param2);
        }

    }

    public Map<ResourceLocation, Tag<T>> build(Map<ResourceLocation, Tag.Builder> param0) {
        Map<ResourceLocation, Tag<T>> var0 = Maps.newHashMap();
        Function<ResourceLocation, Tag<T>> var1 = var0::get;
        Function<ResourceLocation, T> var2 = param0x -> this.idToValue.apply(param0x).orElse((T)null);
        Multimap<ResourceLocation, ResourceLocation> var3 = HashMultimap.create();
        param0.forEach((param1, param2) -> param2.visitRequiredDependencies(param2x -> addDependencyIfNotCyclic(var3, param1, param2x)));
        param0.forEach((param1, param2) -> param2.visitOptionalDependencies(param2x -> addDependencyIfNotCyclic(var3, param1, param2x)));
        Set<ResourceLocation> var4 = Sets.newHashSet();
        param0.keySet()
            .forEach(
                param6 -> visitDependenciesAndElement(
                        param0,
                        var3,
                        var4,
                        param6,
                        (param3x, param4x) -> param4x.build(var1, var2)
                                .ifLeft(
                                    param1x -> LOGGER.error(
                                            "Couldn't load tag {} as it is missing following references: {}",
                                            param3x,
                                            param1x.stream().map(Objects::toString).collect(Collectors.joining(", "))
                                        )
                                )
                                .ifRight(param2x -> var0.put(param3x, param2x))
                    )
            );
        return var0;
    }

    public Map<ResourceLocation, Tag<T>> loadAndBuild(ResourceManager param0) {
        return this.build(this.load(param0));
    }
}
