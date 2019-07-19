package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ItemArgument implements ArgumentType<ItemInput> {
    private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "stick{foo=bar}");

    public static ItemArgument item() {
        return new ItemArgument();
    }

    public ItemInput parse(StringReader param0) throws CommandSyntaxException {
        ItemParser var0 = new ItemParser(param0, false).parse();
        return new ItemInput(var0.getItem(), var0.getNbt());
    }

    public static <S> ItemInput getItem(CommandContext<S> param0, String param1) {
        return param0.getArgument(param1, ItemInput.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        StringReader var0 = new StringReader(param1.getInput());
        var0.setCursor(param1.getStart());
        ItemParser var1 = new ItemParser(var0, false);

        try {
            var1.parse();
        } catch (CommandSyntaxException var6) {
        }

        return var1.fillSuggestions(param1);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
