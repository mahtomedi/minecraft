package net.minecraft.data.tags;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import org.slf4j.Logger;

public abstract class TagsProvider<T> implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final PackOutput.PathProvider pathProvider;
    protected final Registry<T> registry;
    private final Map<ResourceLocation, TagBuilder> builders = Maps.newLinkedHashMap();

    protected TagsProvider(PackOutput param0, Registry<T> param1) {
        this.pathProvider = param0.createPathProvider(PackOutput.Target.DATA_PACK, TagManager.getTagDir(param1.key()));
        this.registry = param1;
    }

    @Override
    public final String getName() {
        return "Tags for " + this.registry.key().location();
    }

    protected abstract void addTags();

    @Override
    public CompletableFuture<?> run(CachedOutput param0) {
        this.builders.clear();
        this.addTags();
        return CompletableFuture.allOf(
            this.builders
                .entrySet()
                .stream()
                .map(
                    param1 -> {
                        ResourceLocation var0 = param1.getKey();
                        TagBuilder var1x = param1.getValue();
                        List<TagEntry> var2 = var1x.build();
                        List<TagEntry> var3 = var2.stream()
                            .filter(param0x -> !param0x.verifyIfPresent(this.registry::containsKey, this.builders::containsKey))
                            .toList();
                        if (!var3.isEmpty()) {
                            throw new IllegalArgumentException(
                                String.format(
                                    Locale.ROOT,
                                    "Couldn't define tag %s as it is missing following references: %s",
                                    var0,
                                    var3.stream().map(Objects::toString).collect(Collectors.joining(","))
                                )
                            );
                        } else {
                            JsonElement var4 = TagFile.CODEC.encodeStart(JsonOps.INSTANCE, new TagFile(var2, false)).getOrThrow(false, LOGGER::error);
                            Path var5 = this.pathProvider.json(var0);
                            return DataProvider.saveStable(param0, var4, var5);
                        }
                    }
                )
                .toArray(param0x -> new CompletableFuture[param0x])
        );
    }

    protected TagsProvider.TagAppender<T> tag(TagKey<T> param0) {
        TagBuilder var0 = this.getOrCreateRawBuilder(param0);
        return new TagsProvider.TagAppender<>(var0, this.registry);
    }

    protected TagBuilder getOrCreateRawBuilder(TagKey<T> param0) {
        return this.builders.computeIfAbsent(param0.location(), param0x -> TagBuilder.create());
    }

    protected static class TagAppender<T> {
        private final TagBuilder builder;
        private final Registry<T> registry;

        TagAppender(TagBuilder param0, Registry<T> param1) {
            this.builder = param0;
            this.registry = param1;
        }

        public TagsProvider.TagAppender<T> add(T param0) {
            this.builder.addElement(this.registry.getKey(param0));
            return this;
        }

        @SafeVarargs
        public final TagsProvider.TagAppender<T> add(ResourceKey<T>... param0) {
            for(ResourceKey<T> var0 : param0) {
                this.builder.addElement(var0.location());
            }

            return this;
        }

        public TagsProvider.TagAppender<T> addOptional(ResourceLocation param0) {
            this.builder.addOptionalElement(param0);
            return this;
        }

        public TagsProvider.TagAppender<T> addTag(TagKey<T> param0) {
            this.builder.addTag(param0.location());
            return this;
        }

        public TagsProvider.TagAppender<T> addOptionalTag(ResourceLocation param0) {
            this.builder.addOptionalTag(param0);
            return this;
        }

        @SafeVarargs
        public final TagsProvider.TagAppender<T> add(T... param0) {
            Stream.<T>of(param0).map(this.registry::getKey).forEach(param0x -> this.builder.addElement(param0x));
            return this;
        }
    }
}
