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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Scoreboard;

public class ScoreboardSlotArgument implements ArgumentType<Integer> {
    private static final Collection<String> EXAMPLES = Arrays.asList("sidebar", "foo.bar");
    public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType(
        param0 -> Component.translatable("argument.scoreboardDisplaySlot.invalid", param0)
    );

    private ScoreboardSlotArgument() {
    }

    public static ScoreboardSlotArgument displaySlot() {
        return new ScoreboardSlotArgument();
    }

    public static int getDisplaySlot(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, Integer.class);
    }

    public Integer parse(StringReader param0) throws CommandSyntaxException {
        String var0 = param0.readUnquotedString();
        int var1 = Scoreboard.getDisplaySlotByName(var0);
        if (var1 == -1) {
            throw ERROR_INVALID_VALUE.create(var0);
        } else {
            return var1;
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        return SharedSuggestionProvider.suggest(Scoreboard.getDisplaySlotNames(), param1);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
