package net.minecraft.tags;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TagManager implements PreparableReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private final RegistryAccess registryAccess;
    private TagContainer tags = TagContainer.EMPTY;

    public TagManager(RegistryAccess param0) {
        this.registryAccess = param0;
    }

    public TagContainer getTags() {
        return this.tags;
    }

    @Override
    public CompletableFuture<Void> reload(
        PreparableReloadListener.PreparationBarrier param0,
        ResourceManager param1,
        ProfilerFiller param2,
        ProfilerFiller param3,
        Executor param4,
        Executor param5
    ) {
        List<TagManager.LoaderInfo<?>> var0 = Lists.newArrayList();
        StaticTags.visitHelpers(param3x -> {
            TagManager.LoaderInfo<?> var0x = this.createLoader(param1, param4, param3x);
            if (var0x != null) {
                var0.add(var0x);
            }

        });
        return CompletableFuture.allOf(var0.stream().map(param0x -> param0x.pendingLoad).toArray(param0x -> new CompletableFuture[param0x]))
            .thenCompose(param0::wait)
            .thenAcceptAsync(
                param1x -> {
                    TagContainer.Builder var0x = new TagContainer.Builder();
                    var0.forEach(param1xx -> param1xx.addToBuilder(var0x));
                    TagContainer var1x = var0x.build();
                    Multimap<ResourceKey<? extends Registry<?>>, ResourceLocation> var2x = StaticTags.getAllMissingTags(var1x);
                    if (!var2x.isEmpty()) {
                        throw new IllegalStateException(
                            "Missing required tags: "
                                + (String)var2x.entries()
                                    .stream()
                                    .map(param0x -> param0x.getKey() + ":" + param0x.getValue())
                                    .sorted()
                                    .collect(Collectors.joining(","))
                        );
                    } else {
                        SerializationTags.bind(var1x);
                        this.tags = var1x;
                    }
                },
                param5
            );
    }

    @Nullable
    private <T> TagManager.LoaderInfo<T> createLoader(ResourceManager param0, Executor param1, StaticTagHelper<T> param2) {
        Optional<? extends Registry<T>> var0 = this.registryAccess.registry(param2.getKey());
        if (var0.isPresent()) {
            Registry<T> var1 = var0.get();
            TagLoader<T> var2 = new TagLoader<>(var1::getOptional, param2.getDirectory());
            CompletableFuture<? extends TagCollection<T>> var3 = CompletableFuture.supplyAsync(() -> var2.loadAndBuild(param0), param1);
            return new TagManager.LoaderInfo<>(param2, var3);
        } else {
            LOGGER.warn("Can't find registry for {}", param2.getKey());
            return null;
        }
    }

    static class LoaderInfo<T> {
        private final StaticTagHelper<T> helper;
        final CompletableFuture<? extends TagCollection<T>> pendingLoad;

        LoaderInfo(StaticTagHelper<T> param0, CompletableFuture<? extends TagCollection<T>> param1) {
            this.helper = param0;
            this.pendingLoad = param1;
        }

        public void addToBuilder(TagContainer.Builder param0) {
            param0.add(this.helper.getKey(), this.pendingLoad.join());
        }
    }
}
