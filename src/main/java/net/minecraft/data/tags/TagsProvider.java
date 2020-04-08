package net.minecraft.data.tags;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class TagsProvider<T> implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    protected final DataGenerator generator;
    protected final Registry<T> registry;
    private final Map<ResourceLocation, Tag.Builder> builders = Maps.newLinkedHashMap();

    protected TagsProvider(DataGenerator param0, Registry<T> param1) {
        this.generator = param0;
        this.registry = param1;
    }

    protected abstract void addTags();

    @Override
    public void run(HashCache param0) {
        this.builders.clear();
        this.addTags();
        Tag<T> var0 = Tag.fromSet(ImmutableSet.of());
        Function<ResourceLocation, Tag<T>> var1 = param1 -> this.builders.containsKey(param1) ? var0 : null;
        Function<ResourceLocation, T> var2 = param0x -> this.registry.getOptional(param0x).orElse((T)null);
        this.builders
            .forEach(
                (param3, param4) -> {
                    List<Tag.BuilderEntry> var0x = param4.getUnresolvedEntries(var1, var2).collect(Collectors.toList());
                    if (!var0x.isEmpty()) {
                        throw new IllegalArgumentException(
                            String.format(
                                "Couldn't define tag %s as it is missing following references: %s",
                                param3,
                                var0x.stream().map(Objects::toString).collect(Collectors.joining(","))
                            )
                        );
                    } else {
                        JsonObject var1x = param4.serializeToJson();
                        Path var2x = this.getPath(param3);
        
                        try {
                            String var3 = GSON.toJson((JsonElement)var1x);
                            String var4x = SHA1.hashUnencodedChars(var3).toString();
                            if (!Objects.equals(param0.getHash(var2x), var4x) || !Files.exists(var2x)) {
                                Files.createDirectories(var2x.getParent());
        
                                try (BufferedWriter var5 = Files.newBufferedWriter(var2x)) {
                                    var5.write(var3);
                                }
                            }
        
                            param0.putNew(var2x, var4x);
                        } catch (IOException var24) {
                            LOGGER.error("Couldn't save tags to {}", var2x, var24);
                        }
        
                    }
                }
            );
    }

    protected abstract Path getPath(ResourceLocation var1);

    protected TagsProvider.TagAppender<T> tag(Tag.Named<T> param0) {
        Tag.Builder var0 = this.getOrCreateRawBuilder(param0);
        return new TagsProvider.TagAppender<>(var0, this.registry, "vanilla");
    }

    protected Tag.Builder getOrCreateRawBuilder(Tag.Named<T> param0) {
        return this.builders.computeIfAbsent(param0.getName(), param0x -> new Tag.Builder());
    }

    public static class TagAppender<T> {
        private final Tag.Builder builder;
        private final Registry<T> registry;
        private final String source;

        private TagAppender(Tag.Builder param0, Registry<T> param1, String param2) {
            this.builder = param0;
            this.registry = param1;
            this.source = param2;
        }

        public TagsProvider.TagAppender<T> add(T param0) {
            this.builder.addElement(this.registry.getKey(param0), this.source);
            return this;
        }

        public TagsProvider.TagAppender<T> addTag(Tag.Named<T> param0) {
            this.builder.addTag(param0.getName(), this.source);
            return this;
        }

        @SafeVarargs
        public final TagsProvider.TagAppender<T> add(T... param0) {
            Stream.<T>of(param0).map(this.registry::getKey).forEach(param0x -> this.builder.addElement(param0x, this.source));
            return this;
        }
    }
}
