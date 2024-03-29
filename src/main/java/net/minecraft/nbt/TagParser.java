package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;

public class TagParser {
    public static final SimpleCommandExceptionType ERROR_TRAILING_DATA = new SimpleCommandExceptionType(Component.translatable("argument.nbt.trailing"));
    public static final SimpleCommandExceptionType ERROR_EXPECTED_KEY = new SimpleCommandExceptionType(Component.translatable("argument.nbt.expected.key"));
    public static final SimpleCommandExceptionType ERROR_EXPECTED_VALUE = new SimpleCommandExceptionType(Component.translatable("argument.nbt.expected.value"));
    public static final Dynamic2CommandExceptionType ERROR_INSERT_MIXED_LIST = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatableEscape("argument.nbt.list.mixed", param0, param1)
    );
    public static final Dynamic2CommandExceptionType ERROR_INSERT_MIXED_ARRAY = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatableEscape("argument.nbt.array.mixed", param0, param1)
    );
    public static final DynamicCommandExceptionType ERROR_INVALID_ARRAY = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("argument.nbt.array.invalid", param0)
    );
    public static final char ELEMENT_SEPARATOR = ',';
    public static final char NAME_VALUE_SEPARATOR = ':';
    private static final char LIST_OPEN = '[';
    private static final char LIST_CLOSE = ']';
    private static final char STRUCT_CLOSE = '}';
    private static final char STRUCT_OPEN = '{';
    private static final Pattern DOUBLE_PATTERN_NOSUFFIX = Pattern.compile("[-+]?(?:[0-9]+[.]|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?", 2);
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?d", 2);
    private static final Pattern FLOAT_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?f", 2);
    private static final Pattern BYTE_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)b", 2);
    private static final Pattern LONG_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)l", 2);
    private static final Pattern SHORT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)s", 2);
    private static final Pattern INT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)");
    public static final Codec<CompoundTag> AS_CODEC = Codec.STRING.comapFlatMap(param0 -> {
        try {
            return DataResult.success(new TagParser(new StringReader(param0)).readSingleStruct(), Lifecycle.stable());
        } catch (CommandSyntaxException var2) {
            return DataResult.error(var2::getMessage);
        }
    }, CompoundTag::toString);
    private final StringReader reader;

    public static CompoundTag parseTag(String param0) throws CommandSyntaxException {
        return new TagParser(new StringReader(param0)).readSingleStruct();
    }

    @VisibleForTesting
    CompoundTag readSingleStruct() throws CommandSyntaxException {
        CompoundTag var0 = this.readStruct();
        this.reader.skipWhitespace();
        if (this.reader.canRead()) {
            throw ERROR_TRAILING_DATA.createWithContext(this.reader);
        } else {
            return var0;
        }
    }

    public TagParser(StringReader param0) {
        this.reader = param0;
    }

    protected String readKey() throws CommandSyntaxException {
        this.reader.skipWhitespace();
        if (!this.reader.canRead()) {
            throw ERROR_EXPECTED_KEY.createWithContext(this.reader);
        } else {
            return this.reader.readString();
        }
    }

    protected Tag readTypedValue() throws CommandSyntaxException {
        this.reader.skipWhitespace();
        int var0 = this.reader.getCursor();
        if (StringReader.isQuotedStringStart(this.reader.peek())) {
            return StringTag.valueOf(this.reader.readQuotedString());
        } else {
            String var1 = this.reader.readUnquotedString();
            if (var1.isEmpty()) {
                this.reader.setCursor(var0);
                throw ERROR_EXPECTED_VALUE.createWithContext(this.reader);
            } else {
                return this.type(var1);
            }
        }
    }

    private Tag type(String param0) {
        try {
            if (FLOAT_PATTERN.matcher(param0).matches()) {
                return FloatTag.valueOf(Float.parseFloat(param0.substring(0, param0.length() - 1)));
            }

            if (BYTE_PATTERN.matcher(param0).matches()) {
                return ByteTag.valueOf(Byte.parseByte(param0.substring(0, param0.length() - 1)));
            }

            if (LONG_PATTERN.matcher(param0).matches()) {
                return LongTag.valueOf(Long.parseLong(param0.substring(0, param0.length() - 1)));
            }

            if (SHORT_PATTERN.matcher(param0).matches()) {
                return ShortTag.valueOf(Short.parseShort(param0.substring(0, param0.length() - 1)));
            }

            if (INT_PATTERN.matcher(param0).matches()) {
                return IntTag.valueOf(Integer.parseInt(param0));
            }

            if (DOUBLE_PATTERN.matcher(param0).matches()) {
                return DoubleTag.valueOf(Double.parseDouble(param0.substring(0, param0.length() - 1)));
            }

            if (DOUBLE_PATTERN_NOSUFFIX.matcher(param0).matches()) {
                return DoubleTag.valueOf(Double.parseDouble(param0));
            }

            if ("true".equalsIgnoreCase(param0)) {
                return ByteTag.ONE;
            }

            if ("false".equalsIgnoreCase(param0)) {
                return ByteTag.ZERO;
            }
        } catch (NumberFormatException var3) {
        }

        return StringTag.valueOf(param0);
    }

    public Tag readValue() throws CommandSyntaxException {
        this.reader.skipWhitespace();
        if (!this.reader.canRead()) {
            throw ERROR_EXPECTED_VALUE.createWithContext(this.reader);
        } else {
            char var0 = this.reader.peek();
            if (var0 == '{') {
                return this.readStruct();
            } else {
                return var0 == '[' ? this.readList() : this.readTypedValue();
            }
        }
    }

    protected Tag readList() throws CommandSyntaxException {
        return this.reader.canRead(3) && !StringReader.isQuotedStringStart(this.reader.peek(1)) && this.reader.peek(2) == ';'
            ? this.readArrayTag()
            : this.readListTag();
    }

    public CompoundTag readStruct() throws CommandSyntaxException {
        this.expect('{');
        CompoundTag var0 = new CompoundTag();
        this.reader.skipWhitespace();

        while(this.reader.canRead() && this.reader.peek() != '}') {
            int var1 = this.reader.getCursor();
            String var2 = this.readKey();
            if (var2.isEmpty()) {
                this.reader.setCursor(var1);
                throw ERROR_EXPECTED_KEY.createWithContext(this.reader);
            }

            this.expect(':');
            var0.put(var2, this.readValue());
            if (!this.hasElementSeparator()) {
                break;
            }

            if (!this.reader.canRead()) {
                throw ERROR_EXPECTED_KEY.createWithContext(this.reader);
            }
        }

        this.expect('}');
        return var0;
    }

    private Tag readListTag() throws CommandSyntaxException {
        this.expect('[');
        this.reader.skipWhitespace();
        if (!this.reader.canRead()) {
            throw ERROR_EXPECTED_VALUE.createWithContext(this.reader);
        } else {
            ListTag var0 = new ListTag();
            TagType<?> var1 = null;

            while(this.reader.peek() != ']') {
                int var2 = this.reader.getCursor();
                Tag var3 = this.readValue();
                TagType<?> var4 = var3.getType();
                if (var1 == null) {
                    var1 = var4;
                } else if (var4 != var1) {
                    this.reader.setCursor(var2);
                    throw ERROR_INSERT_MIXED_LIST.createWithContext(this.reader, var4.getPrettyName(), var1.getPrettyName());
                }

                var0.add(var3);
                if (!this.hasElementSeparator()) {
                    break;
                }

                if (!this.reader.canRead()) {
                    throw ERROR_EXPECTED_VALUE.createWithContext(this.reader);
                }
            }

            this.expect(']');
            return var0;
        }
    }

    private Tag readArrayTag() throws CommandSyntaxException {
        this.expect('[');
        int var0 = this.reader.getCursor();
        char var1 = this.reader.read();
        this.reader.read();
        this.reader.skipWhitespace();
        if (!this.reader.canRead()) {
            throw ERROR_EXPECTED_VALUE.createWithContext(this.reader);
        } else if (var1 == 'B') {
            return new ByteArrayTag(this.readArray(ByteArrayTag.TYPE, ByteTag.TYPE));
        } else if (var1 == 'L') {
            return new LongArrayTag(this.readArray(LongArrayTag.TYPE, LongTag.TYPE));
        } else if (var1 == 'I') {
            return new IntArrayTag(this.readArray(IntArrayTag.TYPE, IntTag.TYPE));
        } else {
            this.reader.setCursor(var0);
            throw ERROR_INVALID_ARRAY.createWithContext(this.reader, String.valueOf(var1));
        }
    }

    private <T extends Number> List<T> readArray(TagType<?> param0, TagType<?> param1) throws CommandSyntaxException {
        List<T> var0 = Lists.newArrayList();

        while(this.reader.peek() != ']') {
            int var1 = this.reader.getCursor();
            Tag var2 = this.readValue();
            TagType<?> var3 = var2.getType();
            if (var3 != param1) {
                this.reader.setCursor(var1);
                throw ERROR_INSERT_MIXED_ARRAY.createWithContext(this.reader, var3.getPrettyName(), param0.getPrettyName());
            }

            if (param1 == ByteTag.TYPE) {
                var0.add((T)((NumericTag)var2).getAsByte());
            } else if (param1 == LongTag.TYPE) {
                var0.add((T)((NumericTag)var2).getAsLong());
            } else {
                var0.add((T)((NumericTag)var2).getAsInt());
            }

            if (!this.hasElementSeparator()) {
                break;
            }

            if (!this.reader.canRead()) {
                throw ERROR_EXPECTED_VALUE.createWithContext(this.reader);
            }
        }

        this.expect(']');
        return var0;
    }

    private boolean hasElementSeparator() {
        this.reader.skipWhitespace();
        if (this.reader.canRead() && this.reader.peek() == ',') {
            this.reader.skip();
            this.reader.skipWhitespace();
            return true;
        } else {
            return false;
        }
    }

    private void expect(char param0) throws CommandSyntaxException {
        this.reader.skipWhitespace();
        this.reader.expect(param0);
    }
}
