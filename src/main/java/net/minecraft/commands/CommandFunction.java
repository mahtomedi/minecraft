package net.minecraft.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionManager;

public class CommandFunction {
    private final CommandFunction.Entry[] entries;
    private final ResourceLocation id;

    public CommandFunction(ResourceLocation param0, CommandFunction.Entry[] param1) {
        this.id = param0;
        this.entries = param1;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public CommandFunction.Entry[] getEntries() {
        return this.entries;
    }

    public static CommandFunction fromLines(ResourceLocation param0, ServerFunctionManager param1, List<String> param2) {
        List<CommandFunction.Entry> var0 = Lists.newArrayListWithCapacity(param2.size());

        for(int var1 = 0; var1 < param2.size(); ++var1) {
            int var2 = var1 + 1;
            String var3 = param2.get(var1).trim();
            StringReader var4 = new StringReader(var3);
            if (var4.canRead() && var4.peek() != '#') {
                if (var4.peek() == '/') {
                    var4.skip();
                    if (var4.peek() == '/') {
                        throw new IllegalArgumentException(
                            "Unknown or invalid command '" + var3 + "' on line " + var2 + " (if you intended to make a comment, use '#' not '//')"
                        );
                    }

                    String var5 = var4.readUnquotedString();
                    throw new IllegalArgumentException(
                        "Unknown or invalid command '" + var3 + "' on line " + var2 + " (did you mean '" + var5 + "'? Do not use a preceding forwards slash.)"
                    );
                }

                try {
                    ParseResults<CommandSourceStack> var6 = param1.getServer().getCommands().getDispatcher().parse(var4, param1.getCompilationContext());
                    if (var6.getReader().canRead()) {
                        if (var6.getExceptions().size() == 1) {
                            throw (CommandSyntaxException)var6.getExceptions().values().iterator().next();
                        }

                        if (var6.getContext().getRange().isEmpty()) {
                            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(var6.getReader());
                        }

                        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(var6.getReader());
                    }

                    var0.add(new CommandFunction.CommandEntry(var6));
                } catch (CommandSyntaxException var9) {
                    throw new IllegalArgumentException("Whilst parsing command on line " + var2 + ": " + var9.getMessage());
                }
            }
        }

        return new CommandFunction(param0, var0.toArray(new CommandFunction.Entry[0]));
    }

    public static class CacheableFunction {
        public static final CommandFunction.CacheableFunction NONE = new CommandFunction.CacheableFunction((ResourceLocation)null);
        @Nullable
        private final ResourceLocation id;
        private boolean resolved;
        private Optional<CommandFunction> function = Optional.empty();

        public CacheableFunction(@Nullable ResourceLocation param0) {
            this.id = param0;
        }

        public CacheableFunction(CommandFunction param0) {
            this.resolved = true;
            this.id = null;
            this.function = Optional.of(param0);
        }

        public Optional<CommandFunction> get(ServerFunctionManager param0) {
            if (!this.resolved) {
                if (this.id != null) {
                    this.function = param0.get(this.id);
                }

                this.resolved = true;
            }

            return this.function;
        }

        @Nullable
        public ResourceLocation getId() {
            return this.function.<ResourceLocation>map(param0 -> param0.id).orElse(this.id);
        }
    }

    public static class CommandEntry implements CommandFunction.Entry {
        private final ParseResults<CommandSourceStack> parse;

        public CommandEntry(ParseResults<CommandSourceStack> param0) {
            this.parse = param0;
        }

        @Override
        public void execute(ServerFunctionManager param0, CommandSourceStack param1, ArrayDeque<ServerFunctionManager.QueuedCommand> param2, int param3) throws CommandSyntaxException {
            param0.getDispatcher().execute(new ParseResults<>(this.parse.getContext().withSource(param1), this.parse.getReader(), this.parse.getExceptions()));
        }

        @Override
        public String toString() {
            return this.parse.getReader().getString();
        }
    }

    public interface Entry {
        void execute(ServerFunctionManager var1, CommandSourceStack var2, ArrayDeque<ServerFunctionManager.QueuedCommand> var3, int var4) throws CommandSyntaxException;
    }

    public static class FunctionEntry implements CommandFunction.Entry {
        private final CommandFunction.CacheableFunction function;

        public FunctionEntry(CommandFunction param0) {
            this.function = new CommandFunction.CacheableFunction(param0);
        }

        @Override
        public void execute(ServerFunctionManager param0, CommandSourceStack param1, ArrayDeque<ServerFunctionManager.QueuedCommand> param2, int param3) {
            this.function.get(param0).ifPresent(param4 -> {
                CommandFunction.Entry[] var0 = param4.getEntries();
                int var1x = param3 - param2.size();
                int var2x = Math.min(var0.length, var1x);

                for(int var3x = var2x - 1; var3x >= 0; --var3x) {
                    param2.addFirst(new ServerFunctionManager.QueuedCommand(param0, param1, var0[var3x]));
                }

            });
        }

        @Override
        public String toString() {
            return "function " + this.function.getId();
        }
    }
}
