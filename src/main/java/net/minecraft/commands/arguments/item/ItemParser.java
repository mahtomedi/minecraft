package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ItemParser {
    private static final SimpleCommandExceptionType ERROR_NO_TAGS_ALLOWED = new SimpleCommandExceptionType(
        Component.translatable("argument.item.tag.disallowed")
    );
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM = new DynamicCommandExceptionType(
        param0 -> Component.translatable("argument.item.id.invalid", param0)
    );
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(
        param0 -> Component.translatable("arguments.item.tag.unknown", param0)
    );
    private static final char SYNTAX_START_NBT = '{';
    private static final char SYNTAX_TAG = '#';
    private static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_NOTHING = SuggestionsBuilder::buildFuture;
    private final HolderLookup<Item> items;
    private final StringReader reader;
    private final boolean allowTags;
    private Either<Holder<Item>, HolderSet<Item>> result;
    @Nullable
    private CompoundTag nbt;
    private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions = SUGGEST_NOTHING;

    private ItemParser(HolderLookup<Item> param0, StringReader param1, boolean param2) {
        this.items = param0;
        this.reader = param1;
        this.allowTags = param2;
    }

    public static ItemParser.ItemResult parseForItem(HolderLookup<Item> param0, StringReader param1) throws CommandSyntaxException {
        int var0 = param1.getCursor();

        try {
            ItemParser var1 = new ItemParser(param0, param1, false);
            var1.parse();
            Holder<Item> var2 = var1.result.left().orElseThrow(() -> new IllegalStateException("Parser returned unexpected tag name"));
            return new ItemParser.ItemResult(var2, var1.nbt);
        } catch (CommandSyntaxException var5) {
            param1.setCursor(var0);
            throw var5;
        }
    }

    public static Either<ItemParser.ItemResult, ItemParser.TagResult> parseForTesting(HolderLookup<Item> param0, StringReader param1) throws CommandSyntaxException {
        int var0 = param1.getCursor();

        try {
            ItemParser var1 = new ItemParser(param0, param1, true);
            var1.parse();
            return var1.result.mapBoth(param1x -> new ItemParser.ItemResult(param1x, var1.nbt), param1x -> new ItemParser.TagResult(param1x, var1.nbt));
        } catch (CommandSyntaxException var4) {
            param1.setCursor(var0);
            throw var4;
        }
    }

    public static CompletableFuture<Suggestions> fillSuggestions(HolderLookup<Item> param0, SuggestionsBuilder param1, boolean param2) {
        StringReader var0 = new StringReader(param1.getInput());
        var0.setCursor(param1.getStart());
        ItemParser var1 = new ItemParser(param0, var0, param2);

        try {
            var1.parse();
        } catch (CommandSyntaxException var6) {
        }

        return var1.suggestions.apply(param1.createOffset(var0.getCursor()));
    }

    private void readItem() throws CommandSyntaxException {
        int var0 = this.reader.getCursor();
        ResourceLocation var1 = ResourceLocation.read(this.reader);
        Optional<Holder<Item>> var2 = this.items.get(ResourceKey.create(Registry.ITEM_REGISTRY, var1));
        this.result = Either.left(var2.orElseThrow(() -> {
            this.reader.setCursor(var0);
            return ERROR_UNKNOWN_ITEM.createWithContext(this.reader, var1);
        }));
    }

    private void readTag() throws CommandSyntaxException {
        if (!this.allowTags) {
            throw ERROR_NO_TAGS_ALLOWED.createWithContext(this.reader);
        } else {
            int var0 = this.reader.getCursor();
            this.reader.expect('#');
            this.suggestions = this::suggestTag;
            ResourceLocation var1 = ResourceLocation.read(this.reader);
            Optional<? extends HolderSet<Item>> var2 = this.items.get(TagKey.create(Registry.ITEM_REGISTRY, var1));
            this.result = Either.right(var2.orElseThrow(() -> {
                this.reader.setCursor(var0);
                return ERROR_UNKNOWN_TAG.createWithContext(this.reader, var1);
            }));
        }
    }

    private void readNbt() throws CommandSyntaxException {
        this.nbt = new TagParser(this.reader).readStruct();
    }

    private void parse() throws CommandSyntaxException {
        if (this.allowTags) {
            this.suggestions = this::suggestItemIdOrTag;
        } else {
            this.suggestions = this::suggestItem;
        }

        if (this.reader.canRead() && this.reader.peek() == '#') {
            this.readTag();
        } else {
            this.readItem();
        }

        this.suggestions = this::suggestOpenNbt;
        if (this.reader.canRead() && this.reader.peek() == '{') {
            this.suggestions = SUGGEST_NOTHING;
            this.readNbt();
        }

    }

    private CompletableFuture<Suggestions> suggestOpenNbt(SuggestionsBuilder param0) {
        if (param0.getRemaining().isEmpty()) {
            param0.suggest(String.valueOf('{'));
        }

        return param0.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestTag(SuggestionsBuilder param0) {
        return SharedSuggestionProvider.suggestResource(this.items.listTags().map(TagKey::location), param0, String.valueOf('#'));
    }

    private CompletableFuture<Suggestions> suggestItem(SuggestionsBuilder param0) {
        return SharedSuggestionProvider.suggestResource(this.items.listElements().map(ResourceKey::location), param0);
    }

    private CompletableFuture<Suggestions> suggestItemIdOrTag(SuggestionsBuilder param0) {
        this.suggestTag(param0);
        return this.suggestItem(param0);
    }

    public static record ItemResult(Holder<Item> item, @Nullable CompoundTag nbt) {
    }

    public static record TagResult(HolderSet<Item> tag, @Nullable CompoundTag nbt) {
    }
}
