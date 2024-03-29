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
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

public class ObjectiveArgument implements ArgumentType<String> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "*", "012");
    private static final DynamicCommandExceptionType ERROR_OBJECTIVE_NOT_FOUND = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("arguments.objective.notFound", param0)
    );
    private static final DynamicCommandExceptionType ERROR_OBJECTIVE_READ_ONLY = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("arguments.objective.readonly", param0)
    );

    public static ObjectiveArgument objective() {
        return new ObjectiveArgument();
    }

    public static Objective getObjective(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        String var0 = param0.getArgument(param1, String.class);
        Scoreboard var1 = param0.getSource().getServer().getScoreboard();
        Objective var2 = var1.getObjective(var0);
        if (var2 == null) {
            throw ERROR_OBJECTIVE_NOT_FOUND.create(var0);
        } else {
            return var2;
        }
    }

    public static Objective getWritableObjective(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        Objective var0 = getObjective(param0, param1);
        if (var0.getCriteria().isReadOnly()) {
            throw ERROR_OBJECTIVE_READ_ONLY.create(var0.getName());
        } else {
            return var0;
        }
    }

    public String parse(StringReader param0) throws CommandSyntaxException {
        return param0.readUnquotedString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        S var0 = param0.getSource();
        if (var0 instanceof CommandSourceStack var1) {
            return SharedSuggestionProvider.suggest(var1.getServer().getScoreboard().getObjectiveNames(), param1);
        } else {
            return var0 instanceof SharedSuggestionProvider var2 ? var2.customSuggestion(param0) : Suggestions.empty();
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
