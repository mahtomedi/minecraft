package net.minecraft.commands.functions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.Commands;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.execution.tasks.BuildContexts;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public interface CommandFunction<T> {
    ResourceLocation id();

    InstantiatedFunction<T> instantiate(@Nullable CompoundTag var1, CommandDispatcher<T> var2, T var3) throws FunctionInstantiationException;

    private static boolean shouldConcatenateNextLine(CharSequence param0) {
        int var0 = param0.length();
        return var0 > 0 && param0.charAt(var0 - 1) == '\\';
    }

    static <T extends ExecutionCommandSource<T>> CommandFunction<T> fromLines(
        ResourceLocation param0, CommandDispatcher<T> param1, T param2, List<String> param3
    ) {
        FunctionBuilder<T> var0 = new FunctionBuilder<>();

        for(int var1 = 0; var1 < param3.size(); ++var1) {
            int var2 = var1 + 1;
            String var3 = param3.get(var1).trim();
            String var6;
            if (shouldConcatenateNextLine(var3)) {
                StringBuilder var4 = new StringBuilder(var3);

                do {
                    if (++var1 == param3.size()) {
                        throw new IllegalArgumentException("Line continuation at end of file");
                    }

                    var4.deleteCharAt(var4.length() - 1);
                    String var5 = param3.get(var1).trim();
                    var4.append(var5);
                } while(shouldConcatenateNextLine(var4));

                var6 = var4.toString();
            } else {
                var6 = var3;
            }

            StringReader var8 = new StringReader(var6);
            if (var8.canRead() && var8.peek() != '#') {
                if (var8.peek() == '/') {
                    var8.skip();
                    if (var8.peek() == '/') {
                        throw new IllegalArgumentException(
                            "Unknown or invalid command '" + var6 + "' on line " + var2 + " (if you intended to make a comment, use '#' not '//')"
                        );
                    }

                    String var9 = var8.readUnquotedString();
                    throw new IllegalArgumentException(
                        "Unknown or invalid command '" + var6 + "' on line " + var2 + " (did you mean '" + var9 + "'? Do not use a preceding forwards slash.)"
                    );
                }

                if (var8.peek() == '$') {
                    var0.addMacro(var6.substring(1), var2);
                } else {
                    try {
                        var0.addCommand(parseCommand(param1, param2, var8));
                    } catch (CommandSyntaxException var11) {
                        throw new IllegalArgumentException("Whilst parsing command on line " + var2 + ": " + var11.getMessage());
                    }
                }
            }
        }

        return var0.build(param0);
    }

    static <T extends ExecutionCommandSource<T>> UnboundEntryAction<T> parseCommand(CommandDispatcher<T> param0, T param1, StringReader param2) throws CommandSyntaxException {
        ParseResults<T> var0 = param0.parse(param2, param1);
        Commands.validateParseResults(var0);
        Optional<ContextChain<T>> var1 = ContextChain.tryFlatten(var0.getContext().build(param2.getString()));
        if (var1.isEmpty()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(var0.getReader());
        } else {
            return new BuildContexts.Unbound<>(param2.getString(), var1.get());
        }
    }
}
