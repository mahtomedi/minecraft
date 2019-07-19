package net.minecraft.commands.arguments;

import com.google.common.collect.Streams;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;

public class DimensionTypeArgument implements ArgumentType<DimensionType> {
    private static final Collection<String> EXAMPLES = Stream.of(DimensionType.OVERWORLD, DimensionType.NETHER)
        .map(param0 -> DimensionType.getName(param0).toString())
        .collect(Collectors.toList());
    public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("argument.dimension.invalid", param0)
    );

    public DimensionType parse(StringReader param0) throws CommandSyntaxException {
        ResourceLocation var0 = ResourceLocation.read(param0);
        return Registry.DIMENSION_TYPE.getOptional(var0).orElseThrow(() -> ERROR_INVALID_VALUE.create(var0));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        return SharedSuggestionProvider.suggestResource(Streams.stream(DimensionType.getAllTypes()).map(DimensionType::getName), param1);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static DimensionTypeArgument dimension() {
        return new DimensionTypeArgument();
    }

    public static DimensionType getDimension(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, DimensionType.class);
    }
}
