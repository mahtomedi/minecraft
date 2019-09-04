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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;

public class TestFunctionArgument implements ArgumentType<TestFunction> {
    private static final Collection<String> EXAMPLES = Arrays.asList("techtests.piston", "techtests");

    public TestFunction parse(StringReader param0) throws CommandSyntaxException {
        String var0 = param0.readUnquotedString();
        Optional<TestFunction> var1 = GameTestRegistry.findTestFunction(var0);
        if (var1.isPresent()) {
            return var1.get();
        } else {
            Message var2 = new TextComponent("No such test: " + var0);
            throw new CommandSyntaxException(new SimpleCommandExceptionType(var2), var2);
        }
    }

    public static TestFunctionArgument testFunctionArgument() {
        return new TestFunctionArgument();
    }

    public static TestFunction getTestFunction(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, TestFunction.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        Stream<String> var0 = GameTestRegistry.getAllTestFunctions().stream().map(TestFunction::getTestName);
        return SharedSuggestionProvider.suggest(var0, param1);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
