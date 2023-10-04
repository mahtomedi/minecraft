package net.minecraft.commands.arguments;

import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public class StringRepresentableArgument<T extends Enum<T> & StringRepresentable> implements ArgumentType<T> {
    private static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("argument.enum.invalid", param0)
    );
    private final Codec<T> codec;
    private final Supplier<T[]> values;

    protected StringRepresentableArgument(Codec<T> param0, Supplier<T[]> param1) {
        this.codec = param0;
        this.values = param1;
    }

    public T parse(StringReader param0) throws CommandSyntaxException {
        String var0 = param0.readUnquotedString();
        return this.codec.parse(JsonOps.INSTANCE, new JsonPrimitive(var0)).result().orElseThrow(() -> ERROR_INVALID_VALUE.create(var0));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        return SharedSuggestionProvider.suggest(
            Arrays.<Enum>stream((Enum[])this.values.get())
                .map(param0x -> ((StringRepresentable)param0x).getSerializedName())
                .map(this::convertId)
                .collect(Collectors.toList()),
            param1
        );
    }

    @Override
    public Collection<String> getExamples() {
        return Arrays.<Enum>stream((Enum[])this.values.get())
            .map(param0 -> ((StringRepresentable)param0).getSerializedName())
            .map(this::convertId)
            .limit(2L)
            .collect(Collectors.toList());
    }

    protected String convertId(String param0x) {
        return param0x;
    }
}
