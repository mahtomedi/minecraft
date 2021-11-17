package net.minecraft.tags;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TagContainer {
    static final Logger LOGGER = LogManager.getLogger();
    public static final TagContainer EMPTY = new TagContainer(ImmutableMap.of());
    private final Map<ResourceKey<? extends Registry<?>>, TagCollection<?>> collections;

    TagContainer(Map<ResourceKey<? extends Registry<?>>, TagCollection<?>> param0) {
        this.collections = param0;
    }

    @Nullable
    private <T> TagCollection<T> get(ResourceKey<? extends Registry<T>> param0) {
        return (TagCollection<T>)this.collections.get(param0);
    }

    public <T> TagCollection<T> getOrEmpty(ResourceKey<? extends Registry<T>> param0) {
        return (TagCollection<T>)this.collections.getOrDefault(param0, TagCollection.empty());
    }

    public <T, E extends Exception> Tag<T> getTagOrThrow(
        ResourceKey<? extends Registry<T>> param0, ResourceLocation param1, Function<ResourceLocation, E> param2
    ) throws E {
        TagCollection<T> var0 = this.get(param0);
        if (var0 == null) {
            throw param2.apply(param1);
        } else {
            Tag<T> var1 = var0.getTag(param1);
            if (var1 == null) {
                throw param2.apply(param1);
            } else {
                return var1;
            }
        }
    }

    public <T, E extends Exception> ResourceLocation getIdOrThrow(ResourceKey<? extends Registry<T>> param0, Tag<T> param1, Supplier<E> param2) throws E {
        TagCollection<T> var0 = this.get(param0);
        if (var0 == null) {
            throw param2.get();
        } else {
            ResourceLocation var1 = var0.getId(param1);
            if (var1 == null) {
                throw param2.get();
            } else {
                return var1;
            }
        }
    }

    public void getAll(TagContainer.CollectionConsumer param0) {
        this.collections.forEach((param1, param2) -> acceptCap(param0, param1, param2));
    }

    private static <T> void acceptCap(TagContainer.CollectionConsumer param0, ResourceKey<? extends Registry<?>> param1, TagCollection<?> param2) {
        param0.accept(param1, param2);
    }

    public void bindToGlobal() {
        StaticTags.resetAll(this);
        Blocks.rebuildCache();
    }

    public Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> serializeToNetwork(final RegistryAccess param0) {
        final Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> var0 = Maps.newHashMap();
        this.getAll(new TagContainer.CollectionConsumer() {
            @Override
            public <T> void accept(ResourceKey<? extends Registry<T>> param0x, TagCollection<T> param1) {
                Optional<? extends Registry<T>> var0 = param0.registry(param0);
                if (var0.isPresent()) {
                    var0.put(param0, param1.serializeToNetwork(var0.get()));
                } else {
                    TagContainer.LOGGER.error("Unknown registry {}", param0);
                }

            }
        });
        return var0;
    }

    public static TagContainer deserializeFromNetwork(RegistryAccess param0, Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> param1) {
        TagContainer.Builder var0 = new TagContainer.Builder();
        param1.forEach((param2, param3) -> addTagsFromPayload(param0, var0, param2, param3));
        return var0.build();
    }

    private static <T> void addTagsFromPayload(
        RegistryAccess param0, TagContainer.Builder param1, ResourceKey<? extends Registry<? extends T>> param2, TagCollection.NetworkPayload param3
    ) {
        Optional<? extends Registry<? extends T>> var0 = param0.registry(param2);
        if (var0.isPresent()) {
            param1.add(param2, TagCollection.createFromNetwork(param3, var0.get()));
        } else {
            LOGGER.error("Unknown registry {}", param2);
        }

    }

    public static class Builder {
        private final ImmutableMap.Builder<ResourceKey<? extends Registry<?>>, TagCollection<?>> result = ImmutableMap.builder();

        public <T> TagContainer.Builder add(ResourceKey<? extends Registry<? extends T>> param0, TagCollection<T> param1) {
            this.result.put(param0, param1);
            return this;
        }

        public TagContainer build() {
            return new TagContainer(this.result.build());
        }
    }

    @FunctionalInterface
    interface CollectionConsumer {
        <T> void accept(ResourceKey<? extends Registry<T>> var1, TagCollection<T> var2);
    }
}
