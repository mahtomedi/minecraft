package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class StringTag implements Tag {
    private String data;

    public StringTag() {
        this("");
    }

    public StringTag(String param0) {
        Objects.requireNonNull(param0, "Null string not allowed");
        this.data = param0;
    }

    @Override
    public void write(DataOutput param0) throws IOException {
        param0.writeUTF(this.data);
    }

    @Override
    public void load(DataInput param0, int param1, NbtAccounter param2) throws IOException {
        param2.accountBits(288L);
        this.data = param0.readUTF();
        param2.accountBits((long)(16 * this.data.length()));
    }

    @Override
    public byte getId() {
        return 8;
    }

    @Override
    public String toString() {
        return quoteAndEscape(this.data);
    }

    public StringTag copy() {
        return new StringTag(this.data);
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
    public Component getPrettyDisplay(String param0, int param1) {
        String var0 = quoteAndEscape(this.data);
        String var1 = var0.substring(0, 1);
        Component var2 = new TextComponent(var0.substring(1, var0.length() - 1)).withStyle(SYNTAX_HIGHLIGHTING_STRING);
        return new TextComponent(var1).append(var2).append(var1);
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
