package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ShortTag extends NumericTag {
    private static final int SELF_SIZE_IN_BITS = 80;
    public static final TagType<ShortTag> TYPE = new TagType.StaticSize<ShortTag>() {
        public ShortTag load(DataInput param0, int param1, NbtAccounter param2) throws IOException {
            param2.accountBits(80L);
            return ShortTag.valueOf(param0.readShort());
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput param0, StreamTagVisitor param1) throws IOException {
            return param1.visit(param0.readShort());
        }

        @Override
        public int size() {
            return 2;
        }

        @Override
        public String getName() {
            return "SHORT";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Short";
        }

        @Override
        public boolean isValue() {
            return true;
        }
    };
    private final short data;

    ShortTag(short param0) {
        this.data = param0;
    }

    public static ShortTag valueOf(short param0) {
        return param0 >= -128 && param0 <= 1024 ? ShortTag.Cache.cache[param0 - -128] : new ShortTag(param0);
    }

    @Override
    public void write(DataOutput param0) throws IOException {
        param0.writeShort(this.data);
    }

    @Override
    public int sizeInBits() {
        return 80;
    }

    @Override
    public byte getId() {
        return 2;
    }

    @Override
    public TagType<ShortTag> getType() {
        return TYPE;
    }

    public ShortTag copy() {
        return this;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            return param0 instanceof ShortTag && this.data == ((ShortTag)param0).data;
        }
    }

    @Override
    public int hashCode() {
        return this.data;
    }

    @Override
    public void accept(TagVisitor param0) {
        param0.visitShort(this);
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
        return this.data;
    }

    @Override
    public byte getAsByte() {
        return (byte)(this.data & 255);
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

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor param0) {
        return param0.visit(this.data);
    }

    static class Cache {
        private static final int HIGH = 1024;
        private static final int LOW = -128;
        static final ShortTag[] cache = new ShortTag[1153];

        private Cache() {
        }

        static {
            for(int var0 = 0; var0 < cache.length; ++var0) {
                cache[var0] = new ShortTag((short)(-128 + var0));
            }

        }
    }
}
