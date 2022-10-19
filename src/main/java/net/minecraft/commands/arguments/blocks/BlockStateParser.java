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
import com.mojang.datafixers.util.Either;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockStateParser {
    public static final SimpleCommandExceptionType ERROR_NO_TAGS_ALLOWED = new SimpleCommandExceptionType(
        Component.translatable("argument.block.tag.disallowed")
    );
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_BLOCK = new DynamicCommandExceptionType(
        param0 -> Component.translatable("argument.block.id.invalid", param0)
    );
    public static final Dynamic2CommandExceptionType ERROR_UNKNOWN_PROPERTY = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("argument.block.property.unknown", param0, param1)
    );
    public static final Dynamic2CommandExceptionType ERROR_DUPLICATE_PROPERTY = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("argument.block.property.duplicate", param1, param0)
    );
    public static final Dynamic3CommandExceptionType ERROR_INVALID_VALUE = new Dynamic3CommandExceptionType(
        (param0, param1, param2) -> Component.translatable("argument.block.property.invalid", param0, param2, param1)
    );
    public static final Dynamic2CommandExceptionType ERROR_EXPECTED_VALUE = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("argument.block.property.novalue", param0, param1)
    );
    public static final SimpleCommandExceptionType ERROR_EXPECTED_END_OF_PROPERTIES = new SimpleCommandExceptionType(
        Component.translatable("argument.block.property.unclosed")
    );
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(
        param0 -> Component.translatable("arguments.block.tag.unknown", param0)
    );
    private static final char SYNTAX_START_PROPERTIES = '[';
    private static final char SYNTAX_START_NBT = '{';
    private static final char SYNTAX_END_PROPERTIES = ']';
    private static final char SYNTAX_EQUALS = '=';
    private static final char SYNTAX_PROPERTY_SEPARATOR = ',';
    private static final char SYNTAX_TAG = '#';
    private static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_NOTHING = SuggestionsBuilder::buildFuture;
    private final HolderLookup<Block> blocks;
    private final StringReader reader;
    private final boolean forTesting;
    private final boolean allowNbt;
    private final Map<Property<?>, Comparable<?>> properties = Maps.newHashMap();
    private final Map<String, String> vagueProperties = Maps.newHashMap();
    private ResourceLocation id = new ResourceLocation("");
    @Nullable
    private StateDefinition<Block, BlockState> definition;
    @Nullable
    private BlockState state;
    @Nullable
    private CompoundTag nbt;
    @Nullable
    private HolderSet<Block> tag;
    private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions = SUGGEST_NOTHING;

    private BlockStateParser(HolderLookup<Block> param0, StringReader param1, boolean param2, boolean param3) {
        this.blocks = param0;
        this.reader = param1;
        this.forTesting = param2;
        this.allowNbt = param3;
    }

    public static BlockStateParser.BlockResult parseForBlock(HolderLookup<Block> param0, String param1, boolean param2) throws CommandSyntaxException {
        return parseForBlock(param0, new StringReader(param1), param2);
    }

    public static BlockStateParser.BlockResult parseForBlock(HolderLookup<Block> param0, StringReader param1, boolean param2) throws CommandSyntaxException {
        int var0 = param1.getCursor();

        try {
            BlockStateParser var1 = new BlockStateParser(param0, param1, false, param2);
            var1.parse();
            return new BlockStateParser.BlockResult(var1.state, var1.properties, var1.nbt);
        } catch (CommandSyntaxException var5) {
            param1.setCursor(var0);
            throw var5;
        }
    }

    public static Either<BlockStateParser.BlockResult, BlockStateParser.TagResult> parseForTesting(HolderLookup<Block> param0, String param1, boolean param2) throws CommandSyntaxException {
        return parseForTesting(param0, new StringReader(param1), param2);
    }

    public static Either<BlockStateParser.BlockResult, BlockStateParser.TagResult> parseForTesting(
        HolderLookup<Block> param0, StringReader param1, boolean param2
    ) throws CommandSyntaxException {
        int var0 = param1.getCursor();

        try {
            BlockStateParser var1 = new BlockStateParser(param0, param1, true, param2);
            var1.parse();
            return var1.tag != null
                ? Either.right(new BlockStateParser.TagResult(var1.tag, var1.vagueProperties, var1.nbt))
                : Either.left(new BlockStateParser.BlockResult(var1.state, var1.properties, var1.nbt));
        } catch (CommandSyntaxException var5) {
            param1.setCursor(var0);
            throw var5;
        }
    }

    public static CompletableFuture<Suggestions> fillSuggestions(HolderLookup<Block> param0, SuggestionsBuilder param1, boolean param2, boolean param3) {
        StringReader var0 = new StringReader(param1.getInput());
        var0.setCursor(param1.getStart());
        BlockStateParser var1 = new BlockStateParser(param0, var0, param2, param3);

        try {
            var1.parse();
        } catch (CommandSyntaxException var7) {
        }

        return var1.suggestions.apply(param1.createOffset(var0.getCursor()));
    }

    private void parse() throws CommandSyntaxException {
        if (this.forTesting) {
            this.suggestions = this::suggestBlockIdOrTag;
        } else {
            this.suggestions = this::suggestItem;
        }

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

        if (this.allowNbt && this.reader.canRead() && this.reader.peek() == '{') {
            this.suggestions = SUGGEST_NOTHING;
            this.readNbt();
        }

    }

    private CompletableFuture<Suggestions> suggestPropertyNameOrEnd(SuggestionsBuilder param0) {
        if (param0.getRemaining().isEmpty()) {
            param0.suggest(String.valueOf(']'));
        }

        return this.suggestPropertyName(param0);
    }

    private CompletableFuture<Suggestions> suggestVaguePropertyNameOrEnd(SuggestionsBuilder param0) {
        if (param0.getRemaining().isEmpty()) {
            param0.suggest(String.valueOf(']'));
        }

        return this.suggestVaguePropertyName(param0);
    }

    private CompletableFuture<Suggestions> suggestPropertyName(SuggestionsBuilder param0) {
        String var0 = param0.getRemaining().toLowerCase(Locale.ROOT);

        for(Property<?> var1 : this.state.getProperties()) {
            if (!this.properties.containsKey(var1) && var1.getName().startsWith(var0)) {
                param0.suggest(var1.getName() + "=");
            }
        }

        return param0.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestVaguePropertyName(SuggestionsBuilder param0) {
        String var0 = param0.getRemaining().toLowerCase(Locale.ROOT);
        if (this.tag != null) {
            for(Holder<Block> var1 : this.tag) {
                for(Property<?> var2 : var1.value().getStateDefinition().getProperties()) {
                    if (!this.vagueProperties.containsKey(var2.getName()) && var2.getName().startsWith(var0)) {
                        param0.suggest(var2.getName() + "=");
                    }
                }
            }
        }

        return param0.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestOpenNbt(SuggestionsBuilder param0) {
        if (param0.getRemaining().isEmpty() && this.hasBlockEntity()) {
            param0.suggest(String.valueOf('{'));
        }

        return param0.buildFuture();
    }

    private boolean hasBlockEntity() {
        if (this.state != null) {
            return this.state.hasBlockEntity();
        } else {
            if (this.tag != null) {
                for(Holder<Block> var0 : this.tag) {
                    if (var0.value().defaultBlockState().hasBlockEntity()) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    private CompletableFuture<Suggestions> suggestEquals(SuggestionsBuilder param0) {
        if (param0.getRemaining().isEmpty()) {
            param0.suggest(String.valueOf('='));
        }

        return param0.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestNextPropertyOrEnd(SuggestionsBuilder param0) {
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
            if (var0 instanceof Integer var1) {
                param0.suggest(var1);
            } else {
                param0.suggest(param1.getName(var0));
            }
        }

        return param0;
    }

    private CompletableFuture<Suggestions> suggestVaguePropertyValue(SuggestionsBuilder param0, String param1) {
        boolean var0 = false;
        if (this.tag != null) {
            for(Holder<Block> var1 : this.tag) {
                Block var2 = var1.value();
                Property<?> var3 = var2.getStateDefinition().getProperty(param1);
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

        if (var0) {
            param0.suggest(String.valueOf(','));
        }

        param0.suggest(String.valueOf(']'));
        return param0.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestOpenVaguePropertiesOrNbt(SuggestionsBuilder param0) {
        if (param0.getRemaining().isEmpty() && this.tag != null) {
            boolean var0 = false;
            boolean var1 = false;

            for(Holder<Block> var2 : this.tag) {
                Block var3 = var2.value();
                var0 |= !var3.getStateDefinition().getProperties().isEmpty();
                var1 |= var3.defaultBlockState().hasBlockEntity();
                if (var0 && var1) {
                    break;
                }
            }

            if (var0) {
                param0.suggest(String.valueOf('['));
            }

            if (var1) {
                param0.suggest(String.valueOf('{'));
            }
        }

        return param0.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestOpenPropertiesOrNbt(SuggestionsBuilder param0) {
        if (param0.getRemaining().isEmpty()) {
            if (!this.definition.getProperties().isEmpty()) {
                param0.suggest(String.valueOf('['));
            }

            if (this.state.hasBlockEntity()) {
                param0.suggest(String.valueOf('{'));
            }
        }

        return param0.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestTag(SuggestionsBuilder param0) {
        return SharedSuggestionProvider.suggestResource(this.blocks.listTags().map(TagKey::location), param0, String.valueOf('#'));
    }

    private CompletableFuture<Suggestions> suggestItem(SuggestionsBuilder param0) {
        return SharedSuggestionProvider.suggestResource(this.blocks.listElements().map(ResourceKey::location), param0);
    }

    private CompletableFuture<Suggestions> suggestBlockIdOrTag(SuggestionsBuilder param0) {
        this.suggestTag(param0);
        this.suggestItem(param0);
        return param0.buildFuture();
    }

    private void readBlock() throws CommandSyntaxException {
        int var0 = this.reader.getCursor();
        this.id = ResourceLocation.read(this.reader);
        Block var1 = this.blocks.get(ResourceKey.create(Registry.BLOCK_REGISTRY, this.id)).orElseThrow(() -> {
            this.reader.setCursor(var0);
            return ERROR_UNKNOWN_BLOCK.createWithContext(this.reader, this.id.toString());
        }).value();
        this.definition = var1.getStateDefinition();
        this.state = var1.defaultBlockState();
    }

    private void readTag() throws CommandSyntaxException {
        if (!this.forTesting) {
            throw ERROR_NO_TAGS_ALLOWED.createWithContext(this.reader);
        } else {
            int var0 = this.reader.getCursor();
            this.reader.expect('#');
            this.suggestions = this::suggestTag;
            ResourceLocation var1 = ResourceLocation.read(this.reader);
            this.tag = this.blocks.get(TagKey.create(Registry.BLOCK_REGISTRY, var1)).orElseThrow(() -> {
                this.reader.setCursor(var0);
                return ERROR_UNKNOWN_TAG.createWithContext(this.reader, var1.toString());
            });
        }
    }

    private void readProperties() throws CommandSyntaxException {
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
            this.suggestions = param1 -> addSuggestions(param1, var2).buildFuture();
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

    private void readVagueProperties() throws CommandSyntaxException {
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
            this.suggestions = param1 -> this.suggestVaguePropertyValue(param1, var2);
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

    private void readNbt() throws CommandSyntaxException {
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
        StringBuilder var0 = new StringBuilder(param0.getBlockHolder().unwrapKey().map(param0x -> param0x.location().toString()).orElse("air"));
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

    public static record BlockResult(BlockState blockState, Map<Property<?>, Comparable<?>> properties, @Nullable CompoundTag nbt) {
    }

    public static record TagResult(HolderSet<Block> tag, Map<String, String> vagueProperties, @Nullable CompoundTag nbt) {
    }
}
