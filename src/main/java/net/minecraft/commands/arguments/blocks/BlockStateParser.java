package net.minecraft.commands.arguments.blocks;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockStateParser {
    public static final SimpleCommandExceptionType ERROR_NO_TAGS_ALLOWED = new SimpleCommandExceptionType(
        new TranslatableComponent("argument.block.tag.disallowed")
    );
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_BLOCK = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("argument.block.id.invalid", param0)
    );
    public static final Dynamic2CommandExceptionType ERROR_UNKNOWN_PROPERTY = new Dynamic2CommandExceptionType(
        (param0, param1) -> new TranslatableComponent("argument.block.property.unknown", param0, param1)
    );
    public static final Dynamic2CommandExceptionType ERROR_DUPLICATE_PROPERTY = new Dynamic2CommandExceptionType(
        (param0, param1) -> new TranslatableComponent("argument.block.property.duplicate", param1, param0)
    );
    public static final Dynamic3CommandExceptionType ERROR_INVALID_VALUE = new Dynamic3CommandExceptionType(
        (param0, param1, param2) -> new TranslatableComponent("argument.block.property.invalid", param0, param2, param1)
    );
    public static final Dynamic2CommandExceptionType ERROR_EXPECTED_VALUE = new Dynamic2CommandExceptionType(
        (param0, param1) -> new TranslatableComponent("argument.block.property.novalue", param0, param1)
    );
    public static final SimpleCommandExceptionType ERROR_EXPECTED_END_OF_PROPERTIES = new SimpleCommandExceptionType(
        new TranslatableComponent("argument.block.property.unclosed")
    );
    private static final BiFunction<SuggestionsBuilder, TagCollection<Block>, CompletableFuture<Suggestions>> SUGGEST_NOTHING = (param0, param1) -> param0.buildFuture(
            
        );
    private final StringReader reader;
    private final boolean forTesting;
    private final Map<Property<?>, Comparable<?>> properties = Maps.newHashMap();
    private final Map<String, String> vagueProperties = Maps.newHashMap();
    private ResourceLocation id = new ResourceLocation("");
    private StateDefinition<Block, BlockState> definition;
    private BlockState state;
    @Nullable
    private CompoundTag nbt;
    private ResourceLocation tag = new ResourceLocation("");
    private int tagCursor;
    private BiFunction<SuggestionsBuilder, TagCollection<Block>, CompletableFuture<Suggestions>> suggestions = SUGGEST_NOTHING;

    public BlockStateParser(StringReader param0, boolean param1) {
        this.reader = param0;
        this.forTesting = param1;
    }

    public Map<Property<?>, Comparable<?>> getProperties() {
        return this.properties;
    }

    @Nullable
    public BlockState getState() {
        return this.state;
    }

    @Nullable
    public CompoundTag getNbt() {
        return this.nbt;
    }

    @Nullable
    public ResourceLocation getTag() {
        return this.tag;
    }

    public BlockStateParser parse(boolean param0) throws CommandSyntaxException {
        this.suggestions = this::suggestBlockIdOrTag;
        if (this.reader.canRead() && this.reader.peek() == '#') {
            this.readTag();
            this.suggestions = this::suggestOpenVaguePropertiesOrNbt;
            if (this.reader.canRead() && this.reader.peek() == '[') {
                this.readVagueProperties();
                this.suggestions = this::suggestOpenNbt;
            }
        } else {
            this.readBlock();
            this.suggestions = this::suggestOpenPropertiesOrNbt;
            if (this.reader.canRead() && this.reader.peek() == '[') {
                this.readProperties();
                this.suggestions = this::suggestOpenNbt;
            }
        }

        if (param0 && this.reader.canRead() && this.reader.peek() == '{') {
            this.suggestions = SUGGEST_NOTHING;
            this.readNbt();
        }

        return this;
    }

    private CompletableFuture<Suggestions> suggestPropertyNameOrEnd(SuggestionsBuilder param0, TagCollection<Block> param1) {
        if (param0.getRemaining().isEmpty()) {
            param0.suggest(String.valueOf(']'));
        }

        return this.suggestPropertyName(param0, param1);
    }

    private CompletableFuture<Suggestions> suggestVaguePropertyNameOrEnd(SuggestionsBuilder param0, TagCollection<Block> param1) {
        if (param0.getRemaining().isEmpty()) {
            param0.suggest(String.valueOf(']'));
        }

        return this.suggestVaguePropertyName(param0, param1);
    }

    private CompletableFuture<Suggestions> suggestPropertyName(SuggestionsBuilder param0, TagCollection<Block> param1) {
        String var0 = param0.getRemaining().toLowerCase(Locale.ROOT);

        for(Property<?> var1 : this.state.getProperties()) {
            if (!this.properties.containsKey(var1) && var1.getName().startsWith(var0)) {
                param0.suggest(var1.getName() + '=');
            }
        }

        return param0.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestVaguePropertyName(SuggestionsBuilder param0, TagCollection<Block> param1) {
        String var0 = param0.getRemaining().toLowerCase(Locale.ROOT);
        if (this.tag != null && !this.tag.getPath().isEmpty()) {
            Tag<Block> var1 = param1.getTag(this.tag);
            if (var1 != null) {
                for(Block var2 : var1.getValues()) {
                    for(Property<?> var3 : var2.getStateDefinition().getProperties()) {
                        if (!this.vagueProperties.containsKey(var3.getName()) && var3.getName().startsWith(var0)) {
                            param0.suggest(var3.getName() + '=');
                        }
                    }
                }
            }
        }

        return param0.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestOpenNbt(SuggestionsBuilder param0x, TagCollection<Block> param1) {
        if (param0x.getRemaining().isEmpty() && this.hasBlockEntity(param1)) {
            param0x.suggest(String.valueOf('{'));
        }

        return param0x.buildFuture();
    }

    private boolean hasBlockEntity(TagCollection<Block> param0) {
        if (this.state != null) {
            return this.state.getBlock().isEntityBlock();
        } else {
            if (this.tag != null) {
                Tag<Block> var0 = param0.getTag(this.tag);
                if (var0 != null) {
                    for(Block var1 : var0.getValues()) {
                        if (var1.isEntityBlock()) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }
    }

    private CompletableFuture<Suggestions> suggestEquals(SuggestionsBuilder param0, TagCollection<Block> param1) {
        if (param0.getRemaining().isEmpty()) {
            param0.suggest(String.valueOf('='));
        }

        return param0.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestNextPropertyOrEnd(SuggestionsBuilder param0, TagCollection<Block> param1) {
        if (param0.getRemaining().isEmpty()) {
            param0.suggest(String.valueOf(']'));
        }

        if (param0.getRemaining().isEmpty() && this.properties.size() < this.state.getProperties().size()) {
            param0.suggest(String.valueOf(','));
        }

        return param0.buildFuture();
    }

    private static <T extends Comparable<T>> SuggestionsBuilder addSuggestions(SuggestionsBuilder param0, Property<T> param1) {
        for(T var0 : param1.getPossibleValues()) {
            if (var0 instanceof Integer) {
                param0.suggest((Integer)var0);
            } else {
                param0.suggest(param1.getName(var0));
            }
        }

        return param0;
    }

    private CompletableFuture<Suggestions> suggestVaguePropertyValue(SuggestionsBuilder param0, TagCollection<Block> param1, String param2) {
        boolean var0 = false;
        if (this.tag != null && !this.tag.getPath().isEmpty()) {
            Tag<Block> var1 = param1.getTag(this.tag);
            if (var1 != null) {
                for(Block var2 : var1.getValues()) {
                    Property<?> var3 = var2.getStateDefinition().getProperty(param2);
                    if (var3 != null) {
                        addSuggestions(param0, var3);
                    }

                    if (!var0) {
                        for(Property<?> var4 : var2.getStateDefinition().getProperties()) {
                            if (!this.vagueProperties.containsKey(var4.getName())) {
                                var0 = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (var0) {
            param0.suggest(String.valueOf(','));
        }

        param0.suggest(String.valueOf(']'));
        return param0.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestOpenVaguePropertiesOrNbt(SuggestionsBuilder param0x, TagCollection<Block> param1) {
        if (param0x.getRemaining().isEmpty()) {
            Tag<Block> var0 = param1.getTag(this.tag);
            if (var0 != null) {
                boolean var1 = false;
                boolean var2 = false;

                for(Block var3 : var0.getValues()) {
                    var1 |= !var3.getStateDefinition().getProperties().isEmpty();
                    var2 |= var3.isEntityBlock();
                    if (var1 && var2) {
                        break;
                    }
                }

                if (var1) {
                    param0x.suggest(String.valueOf('['));
                }

                if (var2) {
                    param0x.suggest(String.valueOf('{'));
                }
            }
        }

        return this.suggestTag(param0x, param1);
    }

    private CompletableFuture<Suggestions> suggestOpenPropertiesOrNbt(SuggestionsBuilder param0x, TagCollection<Block> param1) {
        if (param0x.getRemaining().isEmpty()) {
            if (!this.state.getBlock().getStateDefinition().getProperties().isEmpty()) {
                param0x.suggest(String.valueOf('['));
            }

            if (this.state.getBlock().isEntityBlock()) {
                param0x.suggest(String.valueOf('{'));
            }
        }

        return param0x.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestTag(SuggestionsBuilder param0, TagCollection<Block> param1) {
        return SharedSuggestionProvider.suggestResource(param1.getAvailableTags(), param0.createOffset(this.tagCursor).add(param0));
    }

    private CompletableFuture<Suggestions> suggestBlockIdOrTag(SuggestionsBuilder param0x, TagCollection<Block> param1) {
        if (this.forTesting) {
            SharedSuggestionProvider.suggestResource(param1.getAvailableTags(), param0x, String.valueOf('#'));
        }

        SharedSuggestionProvider.suggestResource(Registry.BLOCK.keySet(), param0x);
        return param0x.buildFuture();
    }

    public void readBlock() throws CommandSyntaxException {
        int var0 = this.reader.getCursor();
        this.id = ResourceLocation.read(this.reader);
        Block var1 = Registry.BLOCK.getOptional(this.id).orElseThrow(() -> {
            this.reader.setCursor(var0);
            return ERROR_UNKNOWN_BLOCK.createWithContext(this.reader, this.id.toString());
        });
        this.definition = var1.getStateDefinition();
        this.state = var1.defaultBlockState();
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

    public void readProperties() throws CommandSyntaxException {
        this.reader.skip();
        this.suggestions = this::suggestPropertyNameOrEnd;
        this.reader.skipWhitespace();

        while(this.reader.canRead() && this.reader.peek() != ']') {
            this.reader.skipWhitespace();
            int var0 = this.reader.getCursor();
            String var1 = this.reader.readString();
            Property<?> var2 = this.definition.getProperty(var1);
            if (var2 == null) {
                this.reader.setCursor(var0);
                throw ERROR_UNKNOWN_PROPERTY.createWithContext(this.reader, this.id.toString(), var1);
            }

            if (this.properties.containsKey(var2)) {
                this.reader.setCursor(var0);
                throw ERROR_DUPLICATE_PROPERTY.createWithContext(this.reader, this.id.toString(), var1);
            }

            this.reader.skipWhitespace();
            this.suggestions = this::suggestEquals;
            if (!this.reader.canRead() || this.reader.peek() != '=') {
                throw ERROR_EXPECTED_VALUE.createWithContext(this.reader, this.id.toString(), var1);
            }

            this.reader.skip();
            this.reader.skipWhitespace();
            this.suggestions = (param1, param2) -> addSuggestions(param1, var2).buildFuture();
            int var3 = this.reader.getCursor();
            this.setValue(var2, this.reader.readString(), var3);
            this.suggestions = this::suggestNextPropertyOrEnd;
            this.reader.skipWhitespace();
            if (this.reader.canRead()) {
                if (this.reader.peek() != ',') {
                    if (this.reader.peek() != ']') {
                        throw ERROR_EXPECTED_END_OF_PROPERTIES.createWithContext(this.reader);
                    }
                    break;
                }

                this.reader.skip();
                this.suggestions = this::suggestPropertyName;
            }
        }

        if (this.reader.canRead()) {
            this.reader.skip();
        } else {
            throw ERROR_EXPECTED_END_OF_PROPERTIES.createWithContext(this.reader);
        }
    }

    public void readVagueProperties() throws CommandSyntaxException {
        this.reader.skip();
        this.suggestions = this::suggestVaguePropertyNameOrEnd;
        int var0 = -1;
        this.reader.skipWhitespace();

        while(this.reader.canRead() && this.reader.peek() != ']') {
            this.reader.skipWhitespace();
            int var1 = this.reader.getCursor();
            String var2 = this.reader.readString();
            if (this.vagueProperties.containsKey(var2)) {
                this.reader.setCursor(var1);
                throw ERROR_DUPLICATE_PROPERTY.createWithContext(this.reader, this.id.toString(), var2);
            }

            this.reader.skipWhitespace();
            if (!this.reader.canRead() || this.reader.peek() != '=') {
                this.reader.setCursor(var1);
                throw ERROR_EXPECTED_VALUE.createWithContext(this.reader, this.id.toString(), var2);
            }

            this.reader.skip();
            this.reader.skipWhitespace();
            this.suggestions = (param1, param2) -> this.suggestVaguePropertyValue(param1, param2, var2);
            var0 = this.reader.getCursor();
            String var3 = this.reader.readString();
            this.vagueProperties.put(var2, var3);
            this.reader.skipWhitespace();
            if (this.reader.canRead()) {
                var0 = -1;
                if (this.reader.peek() != ',') {
                    if (this.reader.peek() != ']') {
                        throw ERROR_EXPECTED_END_OF_PROPERTIES.createWithContext(this.reader);
                    }
                    break;
                }

                this.reader.skip();
                this.suggestions = this::suggestVaguePropertyName;
            }
        }

        if (this.reader.canRead()) {
            this.reader.skip();
        } else {
            if (var0 >= 0) {
                this.reader.setCursor(var0);
            }

            throw ERROR_EXPECTED_END_OF_PROPERTIES.createWithContext(this.reader);
        }
    }

    public void readNbt() throws CommandSyntaxException {
        this.nbt = new TagParser(this.reader).readStruct();
    }

    private <T extends Comparable<T>> void setValue(Property<T> param0, String param1, int param2) throws CommandSyntaxException {
        Optional<T> var0 = param0.getValue(param1);
        if (var0.isPresent()) {
            this.state = this.state.setValue(param0, var0.get());
            this.properties.put(param0, var0.get());
        } else {
            this.reader.setCursor(param2);
            throw ERROR_INVALID_VALUE.createWithContext(this.reader, this.id.toString(), param0.getName(), param1);
        }
    }

    public static String serialize(BlockState param0) {
        StringBuilder var0 = new StringBuilder(Registry.BLOCK.getKey(param0.getBlock()).toString());
        if (!param0.getProperties().isEmpty()) {
            var0.append('[');
            boolean var1 = false;

            for(Entry<Property<?>, Comparable<?>> var2 : param0.getValues().entrySet()) {
                if (var1) {
                    var0.append(',');
                }

                appendProperty(var0, var2.getKey(), var2.getValue());
                var1 = true;
            }

            var0.append(']');
        }

        return var0.toString();
    }

    private static <T extends Comparable<T>> void appendProperty(StringBuilder param0, Property<T> param1, Comparable<?> param2) {
        param0.append(param1.getName());
        param0.append('=');
        param0.append(param1.getName((T)param2));
    }

    public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder param0, TagCollection<Block> param1) {
        return this.suggestions.apply(param0.createOffset(this.reader.getCursor()), param1);
    }

    public Map<String, String> getVagueProperties() {
        return this.vagueProperties;
    }
}
