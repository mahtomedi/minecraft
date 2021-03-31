package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public class StringTag implements Tag {
    private static final int SELF_SIZE_IN_BITS = 288;
    public static final TagType<StringTag> TYPE = new TagType<StringTag>() {
        public StringTag load(DataInput param0, int param1, NbtAccounter param2) throws IOException {
            param2.accountBits(288L);
            String var0 = param0.readUTF();
            param2.accountBits((long)(16 * var0.length()));
            return StringTag.valueOf(var0);
        }

        @Override
        public String getName() {
            return "STRING";
        }

        @Override
        public String getPrettyName() {
            return "TAG_String";
        }

        @Override
        public boolean isValue() {
            return true;
        }
    };
    private static final StringTag EMPTY = new StringTag("");
    private static final char DOUBLE_QUOTE = '"';
    private static final char SINGLE_QUOTE = '\'';
    private static final char ESCAPE = '\\';
    private static final char NOT_SET = '\u0000';
    private final String data;

    private StringTag(String param0) {
        Objects.requireNonNull(param0, "Null string not allowed");
        this.data = param0;
    }

    public static StringTag valueOf(String param0) {
        return param0.isEmpty() ? EMPTY : new StringTag(param0);
    }

    @Override
    public void write(DataOutput param0) throws IOException {
        param0.writeUTF(this.data);
    }

    @Override
    public byte getId() {
        return 8;
    }

    @Override
    public TagType<StringTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return Tag.super.getAsString();
    }

    public StringTag copy() {
        return this;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            return param0 instanceof StringTag && Objects.equals(this.data, ((StringTag)param0).data);
        }
    }

    @Override
    public int hashCode() {
        return this.data.hashCode();
    }

    @Override
    public String getAsString() {
        return this.data;
    }

    @Override
    public void accept(TagVisitor param0) {
        param0.visitString(this);
    }

    public static String quoteAndEscape(String param0) {
        StringBuilder var0 = new StringBuilder(" ");
        char var1 = 0;

        for(int var2 = 0; var2 < param0.length(); ++var2) {
            char var3 = param0.charAt(var2);
            if (var3 == '\\') {
                var0.append('\\');
            } else if (var3 == '"' || var3 == '\'') {
                if (var1 == 0) {
                    var1 = (char)(var3 == '"' ? 39 : 34);
                }

                if (var1 == var3) {
                    var0.append('\\');
                }
            }

            var0.append(var3);
        }

        if (var1 == 0) {
            var1 = '"';
        }

        var0.setCharAt(0, var1);
        var0.append(var1);
        return var0.toString();
    }
}
