package net.minecraft.tags;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import java.util.ArrayList;
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
import javax.annotation.Nullable;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

public class TagLoader<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    final Function<ResourceLocation, Optional<? extends T>> idToValue;
    private final String directory;

    public TagLoader(Function<ResourceLocation, Optional<? extends T>> param0, String param1) {
        this.idToValue = param0;
        this.directory = param1;
    }

    public Map<ResourceLocation, List<TagLoader.EntryWithSource>> load(ResourceManager param0) {
        Map<ResourceLocation, List<TagLoader.EntryWithSource>> var0 = Maps.newHashMap();
        FileToIdConverter var1 = FileToIdConverter.json(this.directory);

        for(Entry<ResourceLocation, List<Resource>> var2 : var1.listMatchingResourceStacks(param0).entrySet()) {
            ResourceLocation var3 = var2.getKey();
            ResourceLocation var4 = var1.fileToId(var3);

            for(Resource var5 : var2.getValue()) {
                try (Reader var6 = var5.openAsReader()) {
                    JsonElement var7 = JsonParser.parseReader(var6);
                    List<TagLoader.EntryWithSource> var8 = var0.computeIfAbsent(var4, param0x -> new ArrayList());
                    TagFile var9 = TagFile.CODEC.parse(new Dynamic<>(JsonOps.INSTANCE, var7)).getOrThrow(false, LOGGER::error);
                    if (var9.replace()) {
                        var8.clear();
                    }

                    String var10 = var5.sourcePackId();
                    var9.entries().forEach(param2 -> var8.add(new TagLoader.EntryWithSource(param2, var10)));
                } catch (Exception var17) {
                    LOGGER.error("Couldn't read tag list {} from {} in data pack {}", var4, var3, var5.sourcePackId(), var17);
                }
            }
        }

        return var0;
    }

    private static void visitDependenciesAndElement(
        Map<ResourceLocation, List<TagLoader.EntryWithSource>> param0,
        Multimap<ResourceLocation, ResourceLocation> param1,
        Set<ResourceLocation> param2,
        ResourceLocation param3,
        BiConsumer<ResourceLocation, List<TagLoader.EntryWithSource>> param4
    ) {
        if (param2.add(param3)) {
            param1.get(param3).forEach(param4x -> visitDependenciesAndElement(param0, param1, param2, param4x, param4));
            List<TagLoader.EntryWithSource> var0 = param0.get(param3);
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

    private Either<Collection<TagLoader.EntryWithSource>, Collection<T>> build(TagEntry.Lookup<T> param0, List<TagLoader.EntryWithSource> param1) {
        Builder<T> var0 = ImmutableSet.builder();
        List<TagLoader.EntryWithSource> var1 = new ArrayList<>();

        for(TagLoader.EntryWithSource var2 : param1) {
            if (!var2.entry().build(param0, var0::add)) {
                var1.add(var2);
            }
        }

        return var1.isEmpty() ? Either.right(var0.build()) : Either.left(var1);
    }

    public Map<ResourceLocation, Collection<T>> build(Map<ResourceLocation, List<TagLoader.EntryWithSource>> param0) {
        final Map<ResourceLocation, Collection<T>> var0 = Maps.newHashMap();
        TagEntry.Lookup<T> var1 = new TagEntry.Lookup<T>() {
            @Nullable
            @Override
            public T element(ResourceLocation param0) {
                return TagLoader.this.idToValue.apply(param0).orElse((T)null);
            }

            @Nullable
            @Override
            public Collection<T> tag(ResourceLocation param0) {
                return var0.get(param0);
            }
        };
        Multimap<ResourceLocation, ResourceLocation> var2 = HashMultimap.create();
        param0.forEach(
            (param1, param2) -> param2.forEach(param2x -> param2x.entry.visitRequiredDependencies(param2xx -> addDependencyIfNotCyclic(var2, param1, param2xx)))
        );
        param0.forEach(
            (param1, param2) -> param2.forEach(param2x -> param2x.entry.visitOptionalDependencies(param2xx -> addDependencyIfNotCyclic(var2, param1, param2xx)))
        );
        Set<ResourceLocation> var3 = Sets.newHashSet();
        param0.keySet()
            .forEach(
                param5 -> visitDependenciesAndElement(
                        param0,
                        var2,
                        var3,
                        param5,
                        (param2x, param3x) -> this.build(var1, param3x)
                                .ifLeft(
                                    param1x -> LOGGER.error(
                                            "Couldn't load tag {} as it is missing following references: {}",
                                            param2x,
                                            param1x.stream().map(Objects::toString).collect(Collectors.joining(", "))
                                        )
                                )
                                .ifRight(param2xx -> var0.put(param2x, param2xx))
                    )
            );
        return var0;
    }

    public Map<ResourceLocation, Collection<T>> loadAndBuild(ResourceManager param0) {
        return this.build(this.load(param0));
    }

    public static record EntryWithSource(TagEntry entry, String source) {
        @Override
        public String toString() {
            return this.entry + " (from " + this.source + ")";
        }
    }
}
