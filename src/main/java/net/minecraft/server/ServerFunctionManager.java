package net.minecraft.server;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.GameRules;

public class ServerFunctionManager {
    private static final ResourceLocation TICK_FUNCTION_TAG = new ResourceLocation("tick");
    private static final ResourceLocation LOAD_FUNCTION_TAG = new ResourceLocation("load");
    private final MinecraftServer server;
    private boolean isInFunction;
    private final ArrayDeque<ServerFunctionManager.QueuedCommand> commandQueue = new ArrayDeque<>();
    private final List<ServerFunctionManager.QueuedCommand> nestedCalls = Lists.newArrayList();
    private final List<CommandFunction> ticking = Lists.newArrayList();
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
            Collection<CommandFunction> var0 = this.library.getTags().getTagOrEmpty(LOAD_FUNCTION_TAG).getValues();
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

    public void replaceLibrary(ServerFunctionLibrary param0) {
        this.library = param0;
        this.postReload(param0);
    }

    private void postReload(ServerFunctionLibrary param0) {
        this.ticking.clear();
        this.ticking.addAll(param0.getTags().getTagOrEmpty(TICK_FUNCTION_TAG).getValues());
        this.postReload = true;
    }

    public CommandSourceStack getGameLoopSender() {
        return this.server.createCommandSourceStack().withPermission(2).withSuppressedOutput();
    }

    public Optional<CommandFunction> get(ResourceLocation param0) {
        return this.library.getFunction(param0);
    }

    public Tag<CommandFunction> getTag(ResourceLocation param0) {
        return this.library.getTag(param0);
    }

    public Iterable<ResourceLocation> getFunctionNames() {
        return this.library.getFunctions().keySet();
    }

    public Iterable<ResourceLocation> getTagNames() {
        return this.library.getTags().getAvailableTags();
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
