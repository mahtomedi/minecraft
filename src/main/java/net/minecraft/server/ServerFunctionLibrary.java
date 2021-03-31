package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerFunctionLibrary implements PreparableReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String FILE_EXTENSION = ".mcfunction";
    private static final int PATH_PREFIX_LENGTH = "functions/".length();
    private static final int PATH_SUFFIX_LENGTH = ".mcfunction".length();
    private volatile Map<ResourceLocation, CommandFunction> functions = ImmutableMap.of();
    private final TagLoader<CommandFunction> tagsLoader = new TagLoader<>(this::getFunction, "tags/functions");
    private volatile TagCollection<CommandFunction> tags = TagCollection.empty();
    private final int functionCompilationLevel;
    private final CommandDispatcher<CommandSourceStack> dispatcher;

    public Optional<CommandFunction> getFunction(ResourceLocation param0) {
        return Optional.ofNullable(this.functions.get(param0));
    }

    public Map<ResourceLocation, CommandFunction> getFunctions() {
        return this.functions;
    }

    public TagCollection<CommandFunction> getTags() {
        return this.tags;
    }

    public Tag<CommandFunction> getTag(ResourceLocation param0) {
        return this.tags.getTagOrEmpty(param0);
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
        CompletableFuture<Map<ResourceLocation, Tag.Builder>> var0 = CompletableFuture.supplyAsync(() -> this.tagsLoader.load(param1), param4);
        CompletableFuture<Map<ResourceLocation, CompletableFuture<CommandFunction>>> var1 = CompletableFuture.<Collection<ResourceLocation>>supplyAsync(
                () -> param1.listResources("functions", param0x -> param0x.endsWith(".mcfunction")), param4
            )
            .thenCompose(
                param2x -> {
                    Map<ResourceLocation, CompletableFuture<CommandFunction>> var0x = Maps.newHashMap();
                    CommandSourceStack var1x = new CommandSourceStack(
                        CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null, this.functionCompilationLevel, "", TextComponent.EMPTY, null, null
                    );
        
                    for(ResourceLocation var2x : param2x) {
                        String var3x = var2x.getPath();
                        ResourceLocation var4x = new ResourceLocation(
                            var2x.getNamespace(), var3x.substring(PATH_PREFIX_LENGTH, var3x.length() - PATH_SUFFIX_LENGTH)
                        );
                        var0x.put(var4x, CompletableFuture.supplyAsync(() -> {
                            List<String> var0xx = readLines(param1, var2x);
                            return CommandFunction.fromLines(var4x, this.dispatcher, var1x, var0xx);
                        }, param4));
                    }
        
                    CompletableFuture<?>[] var5 = var0x.values().toArray(new CompletableFuture[0]);
                    return CompletableFuture.allOf(var5).handle((param1x, param2xx) -> var0x);
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
            this.tags = this.tagsLoader.build((Map<ResourceLocation, Tag.Builder>)param0x.getFirst());
        }, param5);
    }

    private static List<String> readLines(ResourceManager param0, ResourceLocation param1) {
        try (Resource var0 = param0.getResource(param1)) {
            return IOUtils.readLines(var0.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException var16) {
            throw new CompletionException(var16);
        }
    }
}
