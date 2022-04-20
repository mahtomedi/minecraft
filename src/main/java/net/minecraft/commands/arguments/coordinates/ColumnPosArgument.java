package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ColumnPos;

public class ColumnPosArgument implements ArgumentType<Coordinates> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0 0", "~ ~", "~1 ~-2", "^ ^", "^-1 ^0");
    public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(Component.translatable("argument.pos2d.incomplete"));

    public static ColumnPosArgument columnPos() {
        return new ColumnPosArgument();
    }

    public static ColumnPos getColumnPos(CommandContext<CommandSourceStack> param0, String param1) {
        BlockPos var0 = param0.getArgument(param1, Coordinates.class).getBlockPos(param0.getSource());
        return new ColumnPos(var0.getX(), var0.getZ());
    }

    public Coordinates parse(StringReader param0) throws CommandSyntaxException {
        int var0 = param0.getCursor();
        if (!param0.canRead()) {
            throw ERROR_NOT_COMPLETE.createWithContext(param0);
        } else {
            WorldCoordinate var1 = WorldCoordinate.parseInt(param0);
            if (param0.canRead() && param0.peek() == ' ') {
                param0.skip();
                WorldCoordinate var2 = WorldCoordinate.parseInt(param0);
                return new WorldCoordinates(var1, new WorldCoordinate(true, 0.0), var2);
            } else {
                param0.setCursor(var0);
                throw ERROR_NOT_COMPLETE.createWithContext(param0);
            }
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        if (!(param0.getSource() instanceof SharedSuggestionProvider)) {
            return Suggestions.empty();
        } else {
            String var0 = param1.getRemaining();
            Collection<SharedSuggestionProvider.TextCoordinates> var1;
            if (!var0.isEmpty() && var0.charAt(0) == '^') {
                var1 = Collections.singleton(SharedSuggestionProvider.TextCoordinates.DEFAULT_LOCAL);
            } else {
                var1 = ((SharedSuggestionProvider)param0.getSource()).getRelevantCoordinates();
            }

            return SharedSuggestionProvider.suggest2DCoordinates(var0, var1, param1, Commands.createValidator(this::parse));
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
