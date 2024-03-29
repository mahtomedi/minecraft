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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class BlockPosArgument implements ArgumentType<Coordinates> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0 0 0", "~ ~ ~", "^ ^ ^", "^1 ^ ^-5", "~0.5 ~1 ~-5");
    public static final SimpleCommandExceptionType ERROR_NOT_LOADED = new SimpleCommandExceptionType(Component.translatable("argument.pos.unloaded"));
    public static final SimpleCommandExceptionType ERROR_OUT_OF_WORLD = new SimpleCommandExceptionType(Component.translatable("argument.pos.outofworld"));
    public static final SimpleCommandExceptionType ERROR_OUT_OF_BOUNDS = new SimpleCommandExceptionType(Component.translatable("argument.pos.outofbounds"));

    public static BlockPosArgument blockPos() {
        return new BlockPosArgument();
    }

    public static BlockPos getLoadedBlockPos(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        ServerLevel var0 = param0.getSource().getLevel();
        return getLoadedBlockPos(param0, var0, param1);
    }

    public static BlockPos getLoadedBlockPos(CommandContext<CommandSourceStack> param0, ServerLevel param1, String param2) throws CommandSyntaxException {
        BlockPos var0 = getBlockPos(param0, param2);
        if (!param1.hasChunkAt(var0)) {
            throw ERROR_NOT_LOADED.create();
        } else if (!param1.isInWorldBounds(var0)) {
            throw ERROR_OUT_OF_WORLD.create();
        } else {
            return var0;
        }
    }

    public static BlockPos getBlockPos(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, Coordinates.class).getBlockPos(param0.getSource());
    }

    public static BlockPos getSpawnablePos(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        BlockPos var0 = getBlockPos(param0, param1);
        if (!Level.isInSpawnableBounds(var0)) {
            throw ERROR_OUT_OF_BOUNDS.create();
        } else {
            return var0;
        }
    }

    public Coordinates parse(StringReader param0) throws CommandSyntaxException {
        return (Coordinates)(param0.canRead() && param0.peek() == '^' ? LocalCoordinates.parse(param0) : WorldCoordinates.parseInt(param0));
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

            return SharedSuggestionProvider.suggestCoordinates(var0, var1, param1, Commands.createValidator(this::parse));
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
