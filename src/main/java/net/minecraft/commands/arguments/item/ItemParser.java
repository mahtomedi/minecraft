package net.minecraft.commands.arguments.item;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.properties.Property;

public class ItemParser {
    public static final SimpleCommandExceptionType ERROR_NO_TAGS_ALLOWED = new SimpleCommandExceptionType(
        new TranslatableComponent("argument.item.tag.disallowed")
    );
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("argument.item.id.invalid", param0)
    );
    private static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_NOTHING = SuggestionsBuilder::buildFuture;
    private final StringReader reader;
    private final boolean forTesting;
    private final Map<Property<?>, Comparable<?>> properties = Maps.newHashMap();
    private Item item;
    @Nullable
    private CompoundTag nbt;
    private ResourceLocation tag = new ResourceLocation("");
    private int tagCursor;
    private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions = SUGGEST_NOTHING;

    public ItemParser(StringReader param0, boolean param1) {
        this.reader = param0;
        this.forTesting = param1;
    }

    public Item getItem() {
        return this.item;
    }

    @Nullable
    public CompoundTag getNbt() {
        return this.nbt;
    }

    public ResourceLocation getTag() {
        return this.tag;
    }

    public void readItem() throws CommandSyntaxException {
        int var0 = this.reader.getCursor();
        ResourceLocation var1 = ResourceLocation.read(this.reader);
        this.item = Registry.ITEM.getOptional(var1).orElseThrow(() -> {
            this.reader.setCursor(var0);
            return ERROR_UNKNOWN_ITEM.createWithContext(this.reader, var1.toString());
        });
    }

    public void readTag() throws CommandSyntaxException {
        if (!this.forTesting) {
            throw ERROR_NO_TAGS_ALLOWED.create();
        } else {
            this.suggestions = this::suggestTag;
            this.reader.expect('#');
            this.tagCursor = this.reader.getCursor();
            this.tag = ResourceLocation.read(this.reader);
        }
    }

    public void readNbt() throws CommandSyntaxException {
        this.nbt = new TagParser(this.reader).readStruct();
    }

    public ItemParser parse() throws CommandSyntaxException {
        this.suggestions = this::suggestItemIdOrTag;
        if (this.reader.canRead() && this.reader.peek() == '#') {
            this.readTag();
        } else {
            this.readItem();
            this.suggestions = this::suggestOpenNbt;
        }

        if (this.reader.canRead() && this.reader.peek() == '{') {
            this.suggestions = SUGGEST_NOTHING;
            this.readNbt();
        }

        return this;
    }

    private CompletableFuture<Suggestions> suggestOpenNbt(SuggestionsBuilder param0) {
        if (param0.getRemaining().isEmpty()) {
            param0.suggest(String.valueOf('{'));
        }

        return param0.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestTag(SuggestionsBuilder param0) {
        return SharedSuggestionProvider.suggestResource(ItemTags.getAllTags().getAvailableTags(), param0.createOffset(this.tagCursor));
    }

    private CompletableFuture<Suggestions> suggestItemIdOrTag(SuggestionsBuilder param0) {
        if (this.forTesting) {
            SharedSuggestionProvider.suggestResource(ItemTags.getAllTags().getAvailableTags(), param0, String.valueOf('#'));
        }

        return SharedSuggestionProvider.suggestResource(Registry.ITEM.keySet(), param0);
    }

    public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder param0) {
        return this.suggestions.apply(param0.createOffset(this.reader.getCursor()));
    }
}
