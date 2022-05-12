package net.minecraft.server;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameRules;

public class ServerFunctionManager {
    private static final Component NO_RECURSIVE_TRACES = Component.translatable("commands.debug.function.noRecursion");
    private static final ResourceLocation TICK_FUNCTION_TAG = new ResourceLocation("tick");
    private static final ResourceLocation LOAD_FUNCTION_TAG = new ResourceLocation("load");
    final MinecraftServer server;
    @Nullable
    private ServerFunctionManager.ExecutionContext context;
    private List<CommandFunction> ticking = ImmutableList.of();
    private boolean postReload;
    private ServerFunctionLibrary library;

    public ServerFunctionManager(MinecraftServer param0, ServerFunctionLibrary param1) {
        this.server = param0;
        this.library = param1;
        this.postReload(param1);
    }

    public int getCommandLimit() {
        return this.server.getGameRules().getInt(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH);
    }

    public CommandDispatcher<CommandSourceStack> getDispatcher() {
        return this.server.getCommands().getDispatcher();
    }

    public void tick() {
        this.executeTagFunctions(this.ticking, TICK_FUNCTION_TAG);
        if (this.postReload) {
            this.postReload = false;
            Collection<CommandFunction> var0 = this.library.getTag(LOAD_FUNCTION_TAG);
            this.executeTagFunctions(var0, LOAD_FUNCTION_TAG);
        }

    }

    private void executeTagFunctions(Collection<CommandFunction> param0, ResourceLocation param1) {
        this.server.getProfiler().push(param1::toString);

        for(CommandFunction var0 : param0) {
            this.execute(var0, this.getGameLoopSender());
        }

        this.server.getProfiler().pop();
    }

    public int execute(CommandFunction param0, CommandSourceStack param1) {
        return this.execute(param0, param1, null);
    }

    public int execute(CommandFunction param0, CommandSourceStack param1, @Nullable ServerFunctionManager.TraceCallbacks param2) {
        if (this.context != null) {
            if (param2 != null) {
                this.context.reportError(NO_RECURSIVE_TRACES.getString());
                return 0;
            } else {
                this.context.delayFunctionCall(param0, param1);
                return 0;
            }
        } else {
            int var4;
            try {
                this.context = new ServerFunctionManager.ExecutionContext(param2);
                var4 = this.context.runTopCommand(param0, param1);
            } finally {
                this.context = null;
            }

            return var4;
        }
    }

    public void replaceLibrary(ServerFunctionLibrary param0) {
        this.library = param0;
        this.postReload(param0);
    }

    private void postReload(ServerFunctionLibrary param0) {
        this.ticking = ImmutableList.copyOf(param0.getTag(TICK_FUNCTION_TAG));
        this.postReload = true;
    }

    public CommandSourceStack getGameLoopSender() {
        return this.server.createCommandSourceStack().withPermission(2).withSuppressedOutput();
    }

    public Optional<CommandFunction> get(ResourceLocation param0) {
        return this.library.getFunction(param0);
    }

    public Collection<CommandFunction> getTag(ResourceLocation param0) {
        return this.library.getTag(param0);
    }

    public Iterable<ResourceLocation> getFunctionNames() {
        return this.library.getFunctions().keySet();
    }

    public Iterable<ResourceLocation> getTagNames() {
        return this.library.getAvailableTags();
    }

    class ExecutionContext {
        private int depth;
        @Nullable
        private final ServerFunctionManager.TraceCallbacks tracer;
        private final Deque<ServerFunctionManager.QueuedCommand> commandQueue = Queues.newArrayDeque();
        private final List<ServerFunctionManager.QueuedCommand> nestedCalls = Lists.newArrayList();

        ExecutionContext(ServerFunctionManager.TraceCallbacks param0) {
            this.tracer = param0;
        }

        void delayFunctionCall(CommandFunction param0, CommandSourceStack param1) {
            int var0 = ServerFunctionManager.this.getCommandLimit();
            if (this.commandQueue.size() + this.nestedCalls.size() < var0) {
                this.nestedCalls.add(new ServerFunctionManager.QueuedCommand(param1, this.depth, new CommandFunction.FunctionEntry(param0)));
            }

        }

        int runTopCommand(CommandFunction param0, CommandSourceStack param1) {
            int var0 = ServerFunctionManager.this.getCommandLimit();
            int var1 = 0;
            CommandFunction.Entry[] var2 = param0.getEntries();

            for(int var3 = var2.length - 1; var3 >= 0; --var3) {
                this.commandQueue.push(new ServerFunctionManager.QueuedCommand(param1, 0, var2[var3]));
            }

            while(!this.commandQueue.isEmpty()) {
                try {
                    ServerFunctionManager.QueuedCommand var4 = this.commandQueue.removeFirst();
                    ServerFunctionManager.this.server.getProfiler().push(var4::toString);
                    this.depth = var4.depth;
                    var4.execute(ServerFunctionManager.this, this.commandQueue, var0, this.tracer);
                    if (!this.nestedCalls.isEmpty()) {
                        Lists.reverse(this.nestedCalls).forEach(this.commandQueue::addFirst);
                        this.nestedCalls.clear();
                    }
                } finally {
                    ServerFunctionManager.this.server.getProfiler().pop();
                }

                if (++var1 >= var0) {
                    return var1;
                }
            }

            return var1;
        }

        public void reportError(String param0) {
            if (this.tracer != null) {
                this.tracer.onError(this.depth, param0);
            }

        }
    }

    public static class QueuedCommand {
        private final CommandSourceStack sender;
        final int depth;
        private final CommandFunction.Entry entry;

        public QueuedCommand(CommandSourceStack param0, int param1, CommandFunction.Entry param2) {
            this.sender = param0;
            this.depth = param1;
            this.entry = param2;
        }

        public void execute(
            ServerFunctionManager param0, Deque<ServerFunctionManager.QueuedCommand> param1, int param2, @Nullable ServerFunctionManager.TraceCallbacks param3
        ) {
            try {
                this.entry.execute(param0, this.sender, param1, param2, this.depth, param3);
            } catch (CommandSyntaxException var6) {
                if (param3 != null) {
                    param3.onError(this.depth, var6.getRawMessage().getString());
                }
            } catch (Exception var7) {
                if (param3 != null) {
                    param3.onError(this.depth, var7.getMessage());
                }
            }

        }

        @Override
        public String toString() {
            return this.entry.toString();
        }
    }

    public interface TraceCallbacks {
        void onCommand(int var1, String var2);

        void onReturn(int var1, String var2, int var3);

        void onError(int var1, String var2);

        void onCall(int var1, ResourceLocation var2, int var3);
    }
}
