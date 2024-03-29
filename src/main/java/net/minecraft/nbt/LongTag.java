package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class LongTag extends NumericTag {
    private static final int SELF_SIZE_IN_BYTES = 16;
    public static final TagType<LongTag> TYPE = new TagType.StaticSize<LongTag>() {
        public LongTag load(DataInput param0, NbtAccounter param1) throws IOException {
            return LongTag.valueOf(readAccounted(param0, param1));
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput param0, StreamTagVisitor param1, NbtAccounter param2) throws IOException {
            return param1.visit(readAccounted(param0, param2));
        }

        private static long readAccounted(DataInput param0, NbtAccounter param1) throws IOException {
            param1.accountBytes(16L);
            return param0.readLong();
        }

        @Override
        public int size() {
            return 8;
        }

        @Override
        public String getName() {
            return "LONG";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Long";
        }

        @Override
        public boolean isValue() {
            return true;
        }
    };
    private final long data;

    LongTag(long param0) {
        this.data = param0;
    }

    public static LongTag valueOf(long param0) {
        return param0 >= -128L && param0 <= 1024L ? LongTag.Cache.cache[(int)param0 - -128] : new LongTag(param0);
    }

    @Override
    public void write(DataOutput param0) throws IOException {
        param0.writeLong(this.data);
    }

    @Override
    public int sizeInBytes() {
        return 16;
    }

    @Override
    public byte getId() {
        return 4;
    }

    @Override
    public TagType<LongTag> getType() {
        return TYPE;
    }

    public LongTag copy() {
        return this;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            return param0 instanceof LongTag && this.data == ((LongTag)param0).data;
        }
    }

    @Override
    public int hashCode() {
        return (int)(this.data ^ this.data >>> 32);
    }

    @Override
    public void accept(TagVisitor param0) {
        param0.visitLong(this);
    }

    @Override
    public long getAsLong() {
        return this.data;
    }

    @Override
    public int getAsInt() {
        return (int)(this.data & -1L);
    }

    @Override
    public short getAsShort() {
        return (short)((int)(this.data & 65535L));
    }

    @Override
    public byte getAsByte() {
        return (byte)((int)(this.data & 255L));
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
        static final LongTag[] cache = new LongTag[1153];

        private Cache() {
        }

        static {
            for(int var0 = 0; var0 < cache.length; ++var0) {
                cache[var0] = new LongTag((long)(-128 + var0));
            }

        }
    }
}
