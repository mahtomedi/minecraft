package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ByteTag extends NumericTag {
    public static final TagType<ByteTag> TYPE = new TagType<ByteTag>() {
        public ByteTag load(DataInput param0, int param1, NbtAccounter param2) throws IOException {
            param2.accountBits(72L);
            return ByteTag.valueOf(param0.readByte());
        }

        @Override
        public String getName() {
            return "BYTE";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Byte";
        }

        @Override
        public boolean isValue() {
            return true;
        }
    };
    public static final ByteTag ZERO = valueOf((byte)0);
    public static final ByteTag ONE = valueOf((byte)1);
    private final byte data;

    private ByteTag(byte param0) {
        this.data = param0;
    }

    public static ByteTag valueOf(byte param0) {
        return ByteTag.Cache.cache[128 + param0];
    }

    public static ByteTag valueOf(boolean param0) {
        return param0 ? ONE : ZERO;
    }

    @Override
    public void write(DataOutput param0) throws IOException {
        param0.writeByte(this.data);
    }

    @Override
    public byte getId() {
        return 1;
    }

    @Override
    public TagType<ByteTag> getType() {
        return TYPE;
    }

    public ByteTag copy() {
        return this;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            return param0 instanceof ByteTag && this.data == ((ByteTag)param0).data;
        }
    }

    @Override
    public int hashCode() {
        return this.data;
    }

    @Override
    public void accept(TagVisitor param0) {
        param0.visitByte(this);
    }

    @Override
    public long getAsLong() {
        return (long)this.data;
    }

    @Override
    public int getAsInt() {
        return this.data;
    }

    @Override
    public short getAsShort() {
        return (short)this.data;
    }

    @Override
    public byte getAsByte() {
        return this.data;
    }

    @Override
    public double getAsDouble() {
        return (double)this.data;
    }

    @Override
    public float getAsFloat() {
        return (float)this.data;
    }

    @Override
    public Number getAsNumber() {
        return this.data;
    }

    static class Cache {
        private static final ByteTag[] cache = new ByteTag[256];

        static {
            for(int var0 = 0; var0 < cache.length; ++var0) {
                cache[var0] = new ByteTag((byte)(var0 - 128));
            }

        }
    }
}
