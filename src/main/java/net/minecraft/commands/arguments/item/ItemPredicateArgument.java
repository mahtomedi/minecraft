package net.minecraft.commands.arguments.item;

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
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemPredicateArgument implements ArgumentType<ItemPredicateArgument.Result> {
    private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "#stick", "#stick{foo=bar}");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("arguments.item.tag.unknown", param0)
    );

    public static ItemPredicateArgument itemPredicate() {
        return new ItemPredicateArgument();
    }

    public ItemPredicateArgument.Result parse(StringReader param0) throws CommandSyntaxException {
        ItemParser var0 = new ItemParser(param0, true).parse();
        if (var0.getItem() != null) {
            ItemPredicateArgument.ItemPredicate var1 = new ItemPredicateArgument.ItemPredicate(var0.getItem(), var0.getNbt());
            return param1 -> var1;
        } else {
            ResourceLocation var2 = var0.getTag();
            return param2 -> {
                Tag<Item> var0x = param2.getSource().getServer().getTags().getItems().getTag(var2);
                if (var0x == null) {
                    throw ERROR_UNKNOWN_TAG.create(var2.toString());
                } else {
                    return new ItemPredicateArgument.TagPredicate(var0x, var0.getNbt());
                }
            };
        }
    }

    public static Predicate<ItemStack> getItemPredicate(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return param0.getArgument(param1, ItemPredicateArgument.Result.class).create(param0);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        StringReader var0 = new StringReader(param1.getInput());
        var0.setCursor(param1.getStart());
        ItemParser var1 = new ItemParser(var0, true);

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

    static class ItemPredicate implements Predicate<ItemStack> {
        private final Item item;
        @Nullable
        private final CompoundTag nbt;

        public ItemPredicate(Item param0, @Nullable CompoundTag param1) {
            this.item = param0;
            this.nbt = param1;
        }

        public boolean test(ItemStack param0) {
            return param0.getItem() == this.item && NbtUtils.compareNbt(this.nbt, param0.getTag(), true);
        }
    }

    public interface Result {
        Predicate<ItemStack> create(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;
    }

    static class TagPredicate implements Predicate<ItemStack> {
        private final Tag<Item> tag;
        @Nullable
        private final CompoundTag nbt;

        public TagPredicate(Tag<Item> param0, @Nullable CompoundTag param1) {
            this.tag = param0;
            this.nbt = param1;
        }

        public boolean test(ItemStack param0) {
            return this.tag.contains(param0.getItem()) && NbtUtils.compareNbt(this.nbt, param0.getTag(), true);
        }
    }
}
