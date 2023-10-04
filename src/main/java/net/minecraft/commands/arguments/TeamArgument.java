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
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public class TeamArgument implements ArgumentType<String> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "123");
    private static final DynamicCommandExceptionType ERROR_TEAM_NOT_FOUND = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("team.notFound", param0)
    );

    public static TeamArgument team() {
        return new TeamArgument();
    }

    public static PlayerTeam getTeam(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        String var0 = param0.getArgument(param1, String.class);
        Scoreboard var1 = param0.getSource().getServer().getScoreboard();
        PlayerTeam var2 = var1.getPlayerTeam(var0);
        if (var2 == null) {
            throw ERROR_TEAM_NOT_FOUND.create(var0);
        } else {
            return var2;
        }
    }

    public String parse(StringReader param0) throws CommandSyntaxException {
        return param0.readUnquotedString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        return param0.getSource() instanceof SharedSuggestionProvider
            ? SharedSuggestionProvider.suggest(((SharedSuggestionProvider)param0.getSource()).getAllTeams(), param1)
            : Suggestions.empty();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
