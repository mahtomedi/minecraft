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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.HolderLookup;
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
    private final CompletableFuture<HolderLookup.Provider> contentsProvider;
    private final CompletableFuture<TagsProvider.TagLookup<T>> parentProvider;
    protected final ResourceKey<? extends Registry<T>> registryKey;
    private final Map<ResourceLocation, TagBuilder> builders = Maps.newLinkedHashMap();

    protected TagsProvider(PackOutput param0, ResourceKey<? extends Registry<T>> param1, CompletableFuture<HolderLookup.Provider> param2) {
        this(param0, param1, param2, CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()));
    }

    protected TagsProvider(
        PackOutput param0,
        ResourceKey<? extends Registry<T>> param1,
        CompletableFuture<HolderLookup.Provider> param2,
        CompletableFuture<TagsProvider.TagLookup<T>> param3
    ) {
        this.pathProvider = param0.createPathProvider(PackOutput.Target.DATA_PACK, TagManager.getTagDir(param1));
        this.registryKey = param1;
        this.parentProvider = param3;
        this.contentsProvider = param2.thenApply(param0x -> {
            this.builders.clear();
            this.addTags(param0x);
            return param0x;
        });
    }

    @Override
    public final String getName() {
        return "Tags for " + this.registryKey.location();
    }

    protected abstract void addTags(HolderLookup.Provider var1);

    @Override
    public CompletableFuture<?> run(CachedOutput param0) {
        return this.contentsProvider()
            .thenCombineAsync(this.parentProvider, (param0x, param1) -> new CombinedData(param0x, param1))
            .thenCompose(
                param1 -> {
                    HolderLookup.RegistryLookup<T> var0 = param1.contents.lookupOrThrow(this.registryKey);
                    Predicate<ResourceLocation> var1x = param1x -> var0.get(ResourceKey.create(this.registryKey, param1x)).isPresent();
                    Predicate<ResourceLocation> var2 = param1x -> this.builders.containsKey(param1x)
                            || param1.parent.contains(TagKey.create(this.registryKey, param1x));
                    return CompletableFuture.allOf(
                        this.builders
                            .entrySet()
                            .stream()
                            .map(
                                param3 -> {
                                    ResourceLocation var0x = param3.getKey();
                                    TagBuilder var1xx = param3.getValue();
                                    List<TagEntry> var2x = var1xx.build();
                                    List<TagEntry> var3x = var2x.stream().filter(param2x -> !param2x.verifyIfPresent(var1x, var2)).toList();
                                    if (!var3x.isEmpty()) {
                                        throw new IllegalArgumentException(
                                            String.format(
                                                Locale.ROOT,
                                                "Couldn't define tag %s as it is missing following references: %s",
                                                var0x,
                                                var3x.stream().map(Objects::toString).collect(Collectors.joining(","))
                                            )
                                        );
                                    } else {
                                        JsonElement var4x = TagFile.CODEC
                                            .encodeStart(JsonOps.INSTANCE, new TagFile(var2x, false))
                                            .getOrThrow(false, LOGGER::error);
                                        Path var5x = this.pathProvider.json(var0x);
                                        return DataProvider.saveStable(param0, var4x, var5x);
                                    }
                                }
                            )
                            .toArray(param0x -> new CompletableFuture[param0x])
                    );
                }
            );

        record CombinedData<T>(HolderLookup.Provider contents, TagsProvider.TagLookup<T> parent) {
        }

    }

    protected TagsProvider.TagAppender<T> tag(TagKey<T> param0) {
        TagBuilder var0 = this.getOrCreateRawBuilder(param0);
        return new TagsProvider.TagAppender<>(var0);
    }

    protected TagBuilder getOrCreateRawBuilder(TagKey<T> param0) {
        return this.builders.computeIfAbsent(param0.location(), param0x -> TagBuilder.create());
    }

    public CompletableFuture<TagsProvider.TagLookup<T>> contentsGetter() {
        return this.contentsProvider().thenApply(param0 -> param0x -> Optional.ofNullable(this.builders.get(param0x.location())));
    }

    protected CompletableFuture<HolderLookup.Provider> contentsProvider() {
        return this.contentsProvider;
    }

    protected static class TagAppender<T> {
        private final TagBuilder builder;

        protected TagAppender(TagBuilder param0) {
            this.builder = param0;
        }

        public final TagsProvider.TagAppender<T> add(ResourceKey<T> param0) {
            this.builder.addElement(param0.location());
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
    }

    @FunctionalInterface
    public interface TagLookup<T> extends Function<TagKey<T>, Optional<TagBuilder>> {
        static <T> TagsProvider.TagLookup<T> empty() {
            return param0 -> Optional.empty();
        }

        default boolean contains(TagKey<T> param0) {
            return this.apply((T)param0).isPresent();
        }
    }
}
