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
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public class ItemArgument implements ArgumentType<ItemInput> {
    private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "stick{foo=bar}");
    private final HolderLookup<Item> items;

    public ItemArgument(CommandBuildContext param0) {
        this.items = param0.holderLookup(Registries.ITEM);
    }

    public static ItemArgument item(CommandBuildContext param0) {
        return new ItemArgument(param0);
    }

    public ItemInput parse(StringReader param0) throws CommandSyntaxException {
        ItemParser.ItemResult var0 = ItemParser.parseForItem(this.items, param0);
        return new ItemInput(var0.item(), var0.nbt());
    }

    public static <S> ItemInput getItem(CommandContext<S> param0, String param1) {
        return param0.getArgument(param1, ItemInput.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        return ItemParser.fillSuggestions(this.items, param1, false);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
