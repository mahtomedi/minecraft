package net.minecraft.gametest.framework;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class TestClassNameArgument implements ArgumentType<String> {
    private static final Collection<String> EXAMPLES = Arrays.asList("techtests", "mobtests");

    public String parse(StringReader param0) throws CommandSyntaxException {
        String var0 = param0.readUnquotedString();
        if (GameTestRegistry.isTestClass(var0)) {
            return var0;
        } else {
            Message var1 = Component.literal("No such test class: " + var0);
            throw new CommandSyntaxException(new SimpleCommandExceptionType(var1), var1);
        }
    }

    public static TestClassNameArgument testClassName() {
        return new TestClassNameArgument();
    }

    public static String getTestClassName(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, String.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        return SharedSuggestionProvider.suggest(GameTestRegistry.getAllTestClassNames().stream(), param1);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
