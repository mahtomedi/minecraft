package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.packs.resources.SimpleResource;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerFunctionManager implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation TICK_FUNCTION_TAG = new ResourceLocation("tick");
    private static final ResourceLocation LOAD_FUNCTION_TAG = new ResourceLocation("load");
    public static final int PATH_PREFIX_LENGTH = "functions/".length();
    public static final int PATH_SUFFIX_LENGTH = ".mcfunction".length();
    private final MinecraftServer server;
    private final Map<ResourceLocation, CommandFunction> functions = Maps.newHashMap();
    private boolean isInFunction;
    private final ArrayDeque<ServerFunctionManager.QueuedCommand> commandQueue = new ArrayDeque<>();
    private final List<ServerFunctionManager.QueuedCommand> nestedCalls = Lists.newArrayList();
    private final TagCollection<CommandFunction> tags = new TagCollection<>(this::get, "tags/functions", "function");
    private final List<CommandFunction> ticking = Lists.newArrayList();
    private boolean postReload;

    public ServerFunctionManager(MinecraftServer param0) {
        this.server = param0;
    }

    public Optional<CommandFunction> get(ResourceLocation param0x) {
        return Optional.ofNullable(this.functions.get(param0x));
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public int getCommandLimit() {
        return this.server.getGameRules().getInt(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH);
    }

    public Map<ResourceLocation, CommandFunction> getFunctions() {
        return this.functions;
    }

    public CommandDispatcher<CommandSourceStack> getDispatcher() {
        return this.server.getCommands().getDispatcher();
    }

    public void tick() {
        this.server.getProfiler().push(TICK_FUNCTION_TAG::toString);

        for(CommandFunction var0 : this.ticking) {
            this.execute(var0, this.getGameLoopSender());
        }

        this.server.getProfiler().pop();
        if (this.postReload) {
            this.postReload = false;
            Collection<CommandFunction> var1 = this.getTags().getTagOrEmpty(LOAD_FUNCTION_TAG).getValues();
            this.server.getProfiler().push(LOAD_FUNCTION_TAG::toString);

            for(CommandFunction var2 : var1) {
                this.execute(var2, this.getGameLoopSender());
            }

            this.server.getProfiler().pop();
        }

    }

    public int execute(CommandFunction param0, CommandSourceStack param1) {
        int var0 = this.getCommandLimit();
        if (this.isInFunction) {
            if (this.commandQueue.size() + this.nestedCalls.size() < var0) {
                this.nestedCalls.add(new ServerFunctionManager.QueuedCommand(this, param1, new CommandFunction.FunctionEntry(param0)));
            }

            return 0;
        } else {
            try {
                this.isInFunction = true;
                int var1 = 0;
                CommandFunction.Entry[] var2 = param0.getEntries();

                for(int var3 = var2.length - 1; var3 >= 0; --var3) {
                    this.commandQueue.push(new ServerFunctionManager.QueuedCommand(this, param1, var2[var3]));
                }

                while(!this.commandQueue.isEmpty()) {
                    try {
                        ServerFunctionManager.QueuedCommand var4 = this.commandQueue.removeFirst();
                        this.server.getProfiler().push(var4::toString);
                        var4.execute(this.commandQueue, var0);
                        if (!this.nestedCalls.isEmpty()) {
                            Lists.reverse(this.nestedCalls).forEach(this.commandQueue::addFirst);
                            this.nestedCalls.clear();
                        }
                    } finally {
                        this.server.getProfiler().pop();
                    }

                    if (++var1 >= var0) {
                        return var1;
                    }
                }

                return var1;
            } finally {
                this.commandQueue.clear();
                this.nestedCalls.clear();
                this.isInFunction = false;
            }
        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager param0) {
        this.functions.clear();
        this.ticking.clear();
        Collection<ResourceLocation> var0 = param0.listResources("functions", param0x -> param0x.endsWith(".mcfunction"));
        List<CompletableFuture<CommandFunction>> var1 = Lists.newArrayList();

        for(ResourceLocation var2 : var0) {
            String var3 = var2.getPath();
            ResourceLocation var4 = new ResourceLocation(var2.getNamespace(), var3.substring(PATH_PREFIX_LENGTH, var3.length() - PATH_SUFFIX_LENGTH));
            var1.add(
                CompletableFuture.<List<String>>supplyAsync(() -> readLinesAsync(param0, var2), SimpleResource.IO_EXECUTOR)
                    .thenApplyAsync(param1 -> CommandFunction.fromLines(var4, this, param1), this.server.getBackgroundTaskExecutor())
                    .handle((param1, param2) -> this.addFunction(param1, param2, var2))
            );
        }

        CompletableFuture.allOf(var1.toArray(new CompletableFuture[0])).join();
        if (!this.functions.isEmpty()) {
            LOGGER.info("Loaded {} custom command functions", this.functions.size());
        }

        this.tags.load(this.tags.prepare(param0, this.server.getBackgroundTaskExecutor()).join());
        this.ticking.addAll(this.tags.getTagOrEmpty(TICK_FUNCTION_TAG).getValues());
        this.postReload = true;
    }

    @Nullable
    private CommandFunction addFunction(CommandFunction param0, @Nullable Throwable param1, ResourceLocation param2) {
        if (param1 != null) {
            LOGGER.error("Couldn't load function at {}", param2, param1);
            return null;
        } else {
            synchronized(this.functions) {
                this.functions.put(param0.getId(), param0);
                return param0;
            }
        }
    }

    private static List<String> readLinesAsync(ResourceManager param0, ResourceLocation param1) {
        try (Resource var0 = param0.getResource(param1)) {
            return IOUtils.readLines(var0.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException var16) {
            throw new CompletionException(var16);
        }
    }

    public CommandSourceStack getGameLoopSender() {
        return this.server.createCommandSourceStack().withPermission(2).withSuppressedOutput();
    }

    public CommandSourceStack getCompilationContext() {
        return new CommandSourceStack(
            CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null, this.server.getFunctionCompilationLevel(), "", new TextComponent(""), this.server, null
        );
    }

    public TagCollection<CommandFunction> getTags() {
        return this.tags;
    }

    public static class QueuedCommand {
        private final ServerFunctionManager manager;
        private final CommandSourceStack sender;
        private final CommandFunction.Entry entry;

        public QueuedCommand(ServerFunctionManager param0, CommandSourceStack param1, CommandFunction.Entry param2) {
            this.manager = param0;
            this.sender = param1;
            this.entry = param2;
        }

        public void execute(ArrayDeque<ServerFunctionManager.QueuedCommand> param0, int param1) {
            try {
                this.entry.execute(this.manager, this.sender, param0, param1);
            } catch (Throwable var4) {
            }

        }

        @Override
        public String toString() {
            return this.entry.toString();
        }
    }
}
