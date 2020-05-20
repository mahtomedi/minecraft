package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.apache.commons.lang3.ArrayUtils;

public class ByteArrayTag extends CollectionTag<ByteTag> {
    public static final TagType<ByteArrayTag> TYPE = new TagType<ByteArrayTag>() {
        public ByteArrayTag load(DataInput param0, int param1, NbtAccounter param2) throws IOException {
            param2.accountBits(192L);
            int var0 = param0.readInt();
            param2.accountBits(8L * (long)var0);
            byte[] var1 = new byte[var0];
            param0.readFully(var1);
            return new ByteArrayTag(var1);
        }

        @Override
        public String getName() {
            return "BYTE[]";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Byte_Array";
        }
    };
    private byte[] data;

    public ByteArrayTag(byte[] param0) {
        this.data = param0;
    }

    public ByteArrayTag(List<Byte> param0) {
        this(toArray(param0));
    }

    private static byte[] toArray(List<Byte> param0) {
        byte[] var0 = new byte[param0.size()];

        for(int var1 = 0; var1 < param0.size(); ++var1) {
            Byte var2 = param0.get(var1);
            var0[var1] = var2 == null ? 0 : var2;
        }

        return var0;
    }

    @Override
    public void write(DataOutput param0) throws IOException {
        param0.writeInt(this.data.length);
        param0.write(this.data);
    }

    @Override
    public byte getId() {
        return 7;
    }

    @Override
    public TagType<ByteArrayTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringBuilder var0 = new StringBuilder("[B;");

        for(int var1 = 0; var1 < this.data.length; ++var1) {
            if (var1 != 0) {
                var0.append(',');
            }

            var0.append(this.data[var1]).append('B');
        }

        return var0.append(']').toString();
    }

    @Override
    public Tag copy() {
        byte[] var0 = new byte[this.data.length];
        System.arraycopy(this.data, 0, var0, 0, this.data.length);
        return new ByteArrayTag(var0);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            return param0 instanceof ByteArrayTag && Arrays.equals(this.data, ((ByteArrayTag)param0).data);
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    @Override
    public Component getPrettyDisplay(String param0, int param1) {
        Component var0 = new TextComponent("B").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        MutableComponent var1 = new TextComponent("[").append(var0).append(";");

        for(int var2 = 0; var2 < this.data.length; ++var2) {
            MutableComponent var3 = new TextComponent(String.valueOf(this.data[var2])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
            var1.append(" ").append(var3).append(var0);
            if (var2 != this.data.length - 1) {
                var1.append(",");
            }
        }

        var1.append("]");
        return var1;
    }

    public byte[] getAsByteArray() {
        return this.data;
    }

    @Override
    public int size() {
        return this.data.length;
    }

    public ByteTag get(int param0) {
        return ByteTag.valueOf(this.data[param0]);
    }

    public ByteTag set(int param0, ByteTag param1) {
        byte var0 = this.data[param0];
        this.data[param0] = param1.getAsByte();
        return ByteTag.valueOf(var0);
    }

    public void add(int param0, ByteTag param1) {
        this.data = ArrayUtils.add(this.data, param0, param1.getAsByte());
    }

    @Override
    public boolean setTag(int param0, Tag param1) {
        if (param1 instanceof NumericTag) {
            this.data[param0] = ((NumericTag)param1).getAsByte();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean addTag(int param0, Tag param1) {
        if (param1 instanceof NumericTag) {
            this.data = ArrayUtils.add(this.data, param0, ((NumericTag)param1).getAsByte());
            return true;
        } else {
            return false;
        }
    }

    public ByteTag remove(int param0) {
        byte var0 = this.data[param0];
        this.data = ArrayUtils.remove(this.data, param0);
        return ByteTag.valueOf(var0);
    }

    @Override
    public byte getElementType() {
        return 1;
    }

    @Override
    public void clear() {
        this.data = new byte[0];
    }
}
