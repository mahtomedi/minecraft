package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class DimensionArgument implements ArgumentType<ResourceLocation> {
    private static final Collection<String> EXAMPLES = Stream.of(Level.OVERWORLD, Level.NETHER)
        .map(param0 -> param0.location().toString())
        .collect(Collectors.toList());
    private static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("argument.dimension.invalid", param0)
    );

    public ResourceLocation parse(StringReader param0) throws CommandSyntaxException {
        return ResourceLocation.read(param0);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        return param0.getSource() instanceof SharedSuggestionProvider
            ? SharedSuggestionProvider.suggestResource(((SharedSuggestionProvider)param0.getSource()).levels().stream().map(ResourceKey::location), param1)
            : Suggestions.empty();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static DimensionArgument dimension() {
        return new DimensionArgument();
    }

    public static ResourceKey<Level> getDimension(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        ResourceLocation var0 = param0.getArgument(param1, ResourceLocation.class);
        ResourceKey<Level> var1 = ResourceKey.create(Registry.DIMENSION_REGISTRY, var0);
        if (param0.getSource().getServer().getLevel(var1) == null) {
            throw ERROR_INVALID_VALUE.create(var0);
        } else {
            return var1;
        }
    }
}
