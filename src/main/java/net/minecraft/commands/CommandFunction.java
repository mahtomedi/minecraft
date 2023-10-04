package net.minecraft.commands;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionManager;

public class CommandFunction {
    private final CommandFunction.Entry[] entries;
    final ResourceLocation id;

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

    public CommandFunction instantiate(@Nullable CompoundTag param0, CommandDispatcher<CommandSourceStack> param1, CommandSourceStack param2) throws FunctionInstantiationException {
        return this;
    }

    private static boolean shouldConcatenateNextLine(CharSequence param0) {
        int var0 = param0.length();
        return var0 > 0 && param0.charAt(var0 - 1) == '\\';
    }

    public static CommandFunction fromLines(
        ResourceLocation param0, CommandDispatcher<CommandSourceStack> param1, CommandSourceStack param2, List<String> param3
    ) {
        List<CommandFunction.Entry> var0 = new ArrayList<>(param3.size());
        Set<String> var1 = new ObjectArraySet<>();

        for(int var2 = 0; var2 < param3.size(); ++var2) {
            int var3 = var2 + 1;
            String var4 = param3.get(var2).trim();
            String var7;
            if (shouldConcatenateNextLine(var4)) {
                StringBuilder var5 = new StringBuilder(var4);

                do {
                    if (++var2 == param3.size()) {
                        throw new IllegalArgumentException("Line continuation at end of file");
                    }

                    var5.deleteCharAt(var5.length() - 1);
                    String var6 = param3.get(var2).trim();
                    var5.append(var6);
                } while(shouldConcatenateNextLine(var5));

                var7 = var5.toString();
            } else {
                var7 = var4;
            }

            StringReader var9 = new StringReader(var7);
            if (var9.canRead() && var9.peek() != '#') {
                if (var9.peek() == '/') {
                    var9.skip();
                    if (var9.peek() == '/') {
                        throw new IllegalArgumentException(
                            "Unknown or invalid command '" + var7 + "' on line " + var3 + " (if you intended to make a comment, use '#' not '//')"
                        );
                    }

                    String var10 = var9.readUnquotedString();
                    throw new IllegalArgumentException(
                        "Unknown or invalid command '" + var7 + "' on line " + var3 + " (did you mean '" + var10 + "'? Do not use a preceding forwards slash.)"
                    );
                }

                if (var9.peek() == '$') {
                    CommandFunction.MacroEntry var11 = decomposeMacro(var7.substring(1), var3);
                    var0.add(var11);
                    var1.addAll(var11.parameters());
                } else {
                    try {
                        ParseResults<CommandSourceStack> var12 = param1.parse(var9, param2);
                        if (var12.getReader().canRead()) {
                            throw Commands.getParseException(var12);
                        }

                        var0.add(new CommandFunction.CommandEntry(var12));
                    } catch (CommandSyntaxException var12) {
                        throw new IllegalArgumentException("Whilst parsing command on line " + var3 + ": " + var12.getMessage());
                    }
                }
            }
        }

        return (CommandFunction)(var1.isEmpty()
            ? new CommandFunction(param0, var0.toArray(param0x -> new CommandFunction.Entry[param0x]))
            : new CommandFunction.CommandMacro(param0, var0.toArray(param0x -> new CommandFunction.Entry[param0x]), List.copyOf(var1)));
    }

    @VisibleForTesting
    public static CommandFunction.MacroEntry decomposeMacro(String param0, int param1) {
        Builder<String> var0 = ImmutableList.builder();
        Builder<String> var1 = ImmutableList.builder();
        int var2 = param0.length();
        int var3 = 0;
        int var4 = param0.indexOf(36);

        while(var4 != -1) {
            if (var4 != var2 - 1 && param0.charAt(var4 + 1) == '(') {
                var0.add(param0.substring(var3, var4));
                int var5 = param0.indexOf(41, var4 + 1);
                if (var5 == -1) {
                    throw new IllegalArgumentException("Unterminated macro variable in macro '" + param0 + "' on line " + param1);
                }

                String var6 = param0.substring(var4 + 2, var5);
                if (!isValidVariableName(var6)) {
                    throw new IllegalArgumentException("Invalid macro variable name '" + var6 + "' on line " + param1);
                }

                var1.add(var6);
                var3 = var5 + 1;
                var4 = param0.indexOf(36, var3);
            } else {
                var4 = param0.indexOf(36, var4 + 1);
            }
        }

        if (var3 == 0) {
            throw new IllegalArgumentException("Macro without variables on line " + param1);
        } else {
            if (var3 != var2) {
                var0.add(param0.substring(var3));
            }

            return new CommandFunction.MacroEntry(var0.build(), var1.build());
        }
    }

    private static boolean isValidVariableName(String param0) {
        for(int var0 = 0; var0 < param0.length(); ++var0) {
            char var1 = param0.charAt(var0);
            if (!Character.isLetterOrDigit(var1) && var1 != '_') {
                return false;
            }
        }

        return true;
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
        public void execute(
            ServerFunctionManager param0,
            CommandSourceStack param1,
            Deque<ServerFunctionManager.QueuedCommand> param2,
            int param3,
            int param4,
            @Nullable ServerFunctionManager.TraceCallbacks param5
        ) throws CommandSyntaxException {
            if (param5 != null) {
                String var0 = this.parse.getReader().getString();
                param5.onCommand(param4, var0);
                int var1 = this.execute(param0, param1);
                param5.onReturn(param4, var0, var1);
            } else {
                this.execute(param0, param1);
            }

        }

        private int execute(ServerFunctionManager param0, CommandSourceStack param1) throws CommandSyntaxException {
            return param0.getDispatcher().execute(Commands.mapSource(this.parse, param1x -> param1));
        }

        @Override
        public String toString() {
            return this.parse.getReader().getString();
        }
    }

    static class CommandMacro extends CommandFunction {
        private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#");
        private final List<String> parameters;
        private static final int MAX_CACHE_ENTRIES = 8;
        private final Object2ObjectLinkedOpenHashMap<List<String>, CommandFunction> cache = new Object2ObjectLinkedOpenHashMap<>(8, 0.25F);

        public CommandMacro(ResourceLocation param0, CommandFunction.Entry[] param1, List<String> param2) {
            super(param0, param1);
            this.parameters = param2;
        }

        @Override
        public CommandFunction instantiate(@Nullable CompoundTag param0, CommandDispatcher<CommandSourceStack> param1, CommandSourceStack param2) throws FunctionInstantiationException {
            if (param0 == null) {
                throw new FunctionInstantiationException(
                    Component.translatable("commands.function.error.missing_arguments", Component.translationArg(this.getId()))
                );
            } else {
                List<String> var0 = new ArrayList<>(this.parameters.size());

                for(String var1 : this.parameters) {
                    if (!param0.contains(var1)) {
                        throw new FunctionInstantiationException(
                            Component.translatable("commands.function.error.missing_argument", Component.translationArg(this.getId()), var1)
                        );
                    }

                    var0.add(stringify(param0.get(var1)));
                }

                CommandFunction var2 = this.cache.getAndMoveToLast(var0);
                if (var2 != null) {
                    return var2;
                } else {
                    if (this.cache.size() >= 8) {
                        this.cache.removeFirst();
                    }

                    CommandFunction var3 = this.substituteAndParse(var0, param1, param2);
                    if (var3 != null) {
                        this.cache.put(var0, var3);
                    }

                    return var3;
                }
            }
        }

        private static String stringify(Tag param0) {
            if (param0 instanceof FloatTag var0) {
                return DECIMAL_FORMAT.format((double)var0.getAsFloat());
            } else if (param0 instanceof DoubleTag var1) {
                return DECIMAL_FORMAT.format(var1.getAsDouble());
            } else if (param0 instanceof ByteTag var2) {
                return String.valueOf(var2.getAsByte());
            } else if (param0 instanceof ShortTag var3) {
                return String.valueOf(var3.getAsShort());
            } else {
                return param0 instanceof LongTag var4 ? String.valueOf(var4.getAsLong()) : param0.getAsString();
            }
        }

        private CommandFunction substituteAndParse(List<String> param0, CommandDispatcher<CommandSourceStack> param1, CommandSourceStack param2) throws FunctionInstantiationException {
            CommandFunction.Entry[] var0 = this.getEntries();
            CommandFunction.Entry[] var1 = new CommandFunction.Entry[var0.length];

            for(int var2 = 0; var2 < var0.length; ++var2) {
                CommandFunction.Entry var3 = var0[var2];
                if (!(var3 instanceof CommandFunction.MacroEntry)) {
                    var1[var2] = var3;
                } else {
                    CommandFunction.MacroEntry var4 = (CommandFunction.MacroEntry)var3;
                    List<String> var5 = var4.parameters();
                    List<String> var6 = new ArrayList<>(var5.size());

                    for(String var7 : var5) {
                        var6.add(param0.get(this.parameters.indexOf(var7)));
                    }

                    String var8 = var4.substitute(var6);

                    try {
                        ParseResults<CommandSourceStack> var9 = param1.parse(var8, param2);
                        if (var9.getReader().canRead()) {
                            throw Commands.getParseException(var9);
                        }

                        var1[var2] = new CommandFunction.CommandEntry(var9);
                    } catch (CommandSyntaxException var13) {
                        throw new FunctionInstantiationException(
                            Component.translatable("commands.function.error.parse", Component.translationArg(this.getId()), var8, var13.getMessage())
                        );
                    }
                }
            }

            ResourceLocation var11 = this.getId();
            return new CommandFunction(new ResourceLocation(var11.getNamespace(), var11.getPath() + "/" + param0.hashCode()), var1);
        }

        static {
            DECIMAL_FORMAT.setMaximumFractionDigits(15);
            DECIMAL_FORMAT.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
        }
    }

    @FunctionalInterface
    public interface Entry {
        void execute(
            ServerFunctionManager var1,
            CommandSourceStack var2,
            Deque<ServerFunctionManager.QueuedCommand> var3,
            int var4,
            int var5,
            @Nullable ServerFunctionManager.TraceCallbacks var6
        ) throws CommandSyntaxException;
    }

    public static class FunctionEntry implements CommandFunction.Entry {
        private final CommandFunction.CacheableFunction function;

        public FunctionEntry(CommandFunction param0) {
            this.function = new CommandFunction.CacheableFunction(param0);
        }

        @Override
        public void execute(
            ServerFunctionManager param0,
            CommandSourceStack param1,
            Deque<ServerFunctionManager.QueuedCommand> param2,
            int param3,
            int param4,
            @Nullable ServerFunctionManager.TraceCallbacks param5
        ) {
            Util.ifElse(this.function.get(param0), param5x -> {
                CommandFunction.Entry[] var0 = param5x.getEntries();
                if (param5 != null) {
                    param5.onCall(param4, param5x.getId(), var0.length);
                }

                int var1x = param3 - param2.size();
                int var2x = Math.min(var0.length, var1x);

                for(int var3x = var2x - 1; var3x >= 0; --var3x) {
                    param2.addFirst(new ServerFunctionManager.QueuedCommand(param1, param4 + 1, var0[var3x]));
                }

            }, () -> {
                if (param5 != null) {
                    param5.onCall(param4, this.function.getId(), -1);
                }

            });
        }

        @Override
        public String toString() {
            return "function " + this.function.getId();
        }
    }

    public static class MacroEntry implements CommandFunction.Entry {
        private final List<String> segments;
        private final List<String> parameters;

        public MacroEntry(List<String> param0, List<String> param1) {
            this.segments = param0;
            this.parameters = param1;
        }

        public List<String> parameters() {
            return this.parameters;
        }

        public String substitute(List<String> param0) {
            StringBuilder var0 = new StringBuilder();

            for(int var1 = 0; var1 < this.parameters.size(); ++var1) {
                var0.append(this.segments.get(var1)).append(param0.get(var1));
            }

            if (this.segments.size() > this.parameters.size()) {
                var0.append(this.segments.get(this.segments.size() - 1));
            }

            return var0.toString();
        }

        @Override
        public void execute(
            ServerFunctionManager param0,
            CommandSourceStack param1,
            Deque<ServerFunctionManager.QueuedCommand> param2,
            int param3,
            int param4,
            @Nullable ServerFunctionManager.TraceCallbacks param5
        ) throws CommandSyntaxException {
            throw new IllegalStateException("Tried to execute an uninstantiated macro");
        }
    }
}
