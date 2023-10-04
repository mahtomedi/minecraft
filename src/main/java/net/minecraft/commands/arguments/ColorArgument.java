package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class ColorArgument implements ArgumentType<ChatFormatting> {
    private static final Collection<String> EXAMPLES = Arrays.asList("red", "green");
    public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("argument.color.invalid", param0)
    );

    private ColorArgument() {
    }

    public static ColorArgument color() {
        return new ColorArgument();
    }

    public static ChatFormatting getColor(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, ChatFormatting.class);
    }

    public ChatFormatting parse(StringReader param0) throws CommandSyntaxException {
        String var0 = param0.readUnquotedString();
        ChatFormatting var1 = ChatFormatting.getByName(var0);
        if (var1 != null && !var1.isFormat()) {
            return var1;
        } else {
            throw ERROR_INVALID_VALUE.create(var0);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        return SharedSuggestionProvider.suggest(ChatFormatting.getNames(true, false), param1);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
