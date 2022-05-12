package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ServerFunctionLibrary implements PreparableReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FILE_EXTENSION = ".mcfunction";
    private static final int PATH_PREFIX_LENGTH = "functions/".length();
    private static final int PATH_SUFFIX_LENGTH = ".mcfunction".length();
    private volatile Map<ResourceLocation, CommandFunction> functions = ImmutableMap.of();
    private final TagLoader<CommandFunction> tagsLoader = new TagLoader<>(this::getFunction, "tags/functions");
    private volatile Map<ResourceLocation, Collection<CommandFunction>> tags = Map.of();
    private final int functionCompilationLevel;
    private final CommandDispatcher<CommandSourceStack> dispatcher;

    public Optional<CommandFunction> getFunction(ResourceLocation param0) {
        return Optional.ofNullable(this.functions.get(param0));
    }

    public Map<ResourceLocation, CommandFunction> getFunctions() {
        return this.functions;
    }

    public Collection<CommandFunction> getTag(ResourceLocation param0) {
        return this.tags.getOrDefault(param0, List.of());
    }

    public Iterable<ResourceLocation> getAvailableTags() {
        return this.tags.keySet();
    }

    public ServerFunctionLibrary(int param0, CommandDispatcher<CommandSourceStack> param1) {
        this.functionCompilationLevel = param0;
        this.dispatcher = param1;
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
        CompletableFuture<Map<ResourceLocation, List<TagLoader.EntryWithSource>>> var0 = CompletableFuture.supplyAsync(
            () -> this.tagsLoader.load(param1), param4
        );
        CompletableFuture<Map<ResourceLocation, CompletableFuture<CommandFunction>>> var1 = CompletableFuture.<Map<ResourceLocation, Resource>>supplyAsync(
                () -> param1.listResources("functions", param0x -> param0x.getPath().endsWith(".mcfunction")), param4
            )
            .thenCompose(
                param1x -> {
                    Map<ResourceLocation, CompletableFuture<CommandFunction>> var0x = Maps.newHashMap();
                    CommandSourceStack var1x = new CommandSourceStack(
                        CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null, this.functionCompilationLevel, "", CommonComponents.EMPTY, null, null
                    );
        
                    for(Entry<ResourceLocation, Resource> var2x : param1x.entrySet()) {
                        ResourceLocation var3x = (ResourceLocation)var2x.getKey();
                        String var4x = var3x.getPath();
                        ResourceLocation var5x = new ResourceLocation(
                            var3x.getNamespace(), var4x.substring(PATH_PREFIX_LENGTH, var4x.length() - PATH_SUFFIX_LENGTH)
                        );
                        var0x.put(var5x, CompletableFuture.supplyAsync(() -> {
                            List<String> var0xx = readLines((Resource)var2x.getValue());
                            return CommandFunction.fromLines(var5x, this.dispatcher, var1x, var0xx);
                        }, param4));
                    }
        
                    CompletableFuture<?>[] var6 = var0x.values().toArray(new CompletableFuture[0]);
                    return CompletableFuture.allOf(var6).handle((param1xx, param2x) -> var0x);
                }
            );
        return var0.thenCombine(var1, Pair::of).thenCompose(param0::wait).thenAcceptAsync(param0x -> {
            Map<ResourceLocation, CompletableFuture<CommandFunction>> var0x = (Map)param0x.getSecond();
            Builder<ResourceLocation, CommandFunction> var1x = ImmutableMap.builder();
            var0x.forEach((param1x, param2x) -> param2x.handle((param2xx, param3x) -> {
                    if (param3x != null) {
                        LOGGER.error("Failed to load function {}", param1x, param3x);
                    } else {
                        var1x.put(param1x, param2xx);
                    }

                    return null;
                }).join());
            this.functions = var1x.build();
            this.tags = this.tagsLoader.build((Map<ResourceLocation, List<TagLoader.EntryWithSource>>)param0x.getFirst());
        }, param5);
    }

    private static List<String> readLines(Resource param0) {
        try {
            List var2;
            try (BufferedReader var0 = param0.openAsReader()) {
                var2 = var0.lines().toList();
            }

            return var2;
        } catch (IOException var6) {
            throw new CompletionException(var6);
        }
    }
}
