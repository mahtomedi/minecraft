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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;

public class GamemodeArgument implements ArgumentType<GameType> {
    private static final Collection<String> EXAMPLES = Arrays.asList("survival", "creative");
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_GAMEMODE = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("gamemode.gamemodeNotFound", param0)
    );

    public static GamemodeArgument gamemode() {
        return new GamemodeArgument();
    }

    public static GameType getGamemode(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, GameType.class);
    }

    public GameType parse(StringReader param0) throws CommandSyntaxException {
        String var0 = ResourceLocation.read(param0).getPath();
        GameType var1 = GameType.byName(var0, null);
        if (var1 == null) {
            throw ERROR_UNKNOWN_GAMEMODE.create(var0);
        } else {
            return var1;
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        return SharedSuggestionProvider.suggest(Arrays.stream(GameType.values()).map(GameType::getName), param1);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
