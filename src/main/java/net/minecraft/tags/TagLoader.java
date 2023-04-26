package net.minecraft.tags;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
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
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.DependencySorter;
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
        DependencySorter<ResourceLocation, TagLoader.SortingEntry> var2 = new DependencySorter<>();
        param0.forEach((param1, param2) -> var2.addEntry(param1, new TagLoader.SortingEntry(param2)));
        var2.orderByDependencies(
            (param2, param3) -> this.build(var1, param3.entries)
                    .ifLeft(
                        param1x -> LOGGER.error(
                                "Couldn't load tag {} as it is missing following references: {}",
                                param2,
                                param1x.stream().map(Objects::toString).collect(Collectors.joining(", "))
                            )
                    )
                    .ifRight(param2x -> var0.put(param2, param2x))
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

    static record SortingEntry(List<TagLoader.EntryWithSource> entries) implements DependencySorter.Entry<ResourceLocation> {
        @Override
        public void visitRequiredDependencies(Consumer<ResourceLocation> param0) {
            this.entries.forEach(param1 -> param1.entry.visitRequiredDependencies(param0));
        }

        @Override
        public void visitOptionalDependencies(Consumer<ResourceLocation> param0) {
            this.entries.forEach(param1 -> param1.entry.visitOptionalDependencies(param0));
        }
    }
}
