package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class IntTag extends NumericTag {
    private static final int SELF_SIZE_IN_BITS = 96;
    public static final TagType<IntTag> TYPE = new TagType<IntTag>() {
        public IntTag load(DataInput param0, int param1, NbtAccounter param2) throws IOException {
            param2.accountBits(96L);
            return IntTag.valueOf(param0.readInt());
        }

        @Override
        public String getName() {
            return "INT";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Int";
        }

        @Override
        public boolean isValue() {
            return true;
        }
    };
    private final int data;

    IntTag(int param0) {
        this.data = param0;
    }

    public static IntTag valueOf(int param0) {
        return param0 >= -128 && param0 <= 1024 ? IntTag.Cache.cache[param0 - -128] : new IntTag(param0);
    }

    @Override
    public void write(DataOutput param0) throws IOException {
        param0.writeInt(this.data);
    }

    @Override
    public byte getId() {
        return 3;
    }

    @Override
    public TagType<IntTag> getType() {
        return TYPE;
    }

    public IntTag copy() {
        return this;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            return param0 instanceof IntTag && this.data == ((IntTag)param0).data;
        }
    }

    @Override
    public int hashCode() {
        return this.data;
    }

    @Override
    public void accept(TagVisitor param0) {
        param0.visitInt(this);
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
        return (short)(this.data & 65535);
    }

    @Override
    public byte getAsByte() {
        return (byte)(this.data & 0xFF);
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
        private static final int HIGH = 1024;
        private static final int LOW = -128;
        static final IntTag[] cache = new IntTag[1153];

        private Cache() {
        }

        static {
            for(int var0 = 0; var0 < cache.length; ++var0) {
                cache[var0] = new IntTag(-128 + var0);
            }

        }
    }
}
