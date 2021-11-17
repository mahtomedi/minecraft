package net.minecraft.nbt;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

public class LongArrayTag extends CollectionTag<LongTag> {
    private static final int SELF_SIZE_IN_BITS = 192;
    public static final TagType<LongArrayTag> TYPE = new TagType.VariableSize<LongArrayTag>() {
        public LongArrayTag load(DataInput param0, int param1, NbtAccounter param2) throws IOException {
            param2.accountBits(192L);
            int var0 = param0.readInt();
            param2.accountBits(64L * (long)var0);
            long[] var1 = new long[var0];

            for(int var2 = 0; var2 < var0; ++var2) {
                var1[var2] = param0.readLong();
            }

            return new LongArrayTag(var1);
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput param0, StreamTagVisitor param1) throws IOException {
            int var0 = param0.readInt();
            long[] var1 = new long[var0];

            for(int var2 = 0; var2 < var0; ++var2) {
                var1[var2] = param0.readLong();
            }

            return param1.visit(var1);
        }

        @Override
        public void skip(DataInput param0) throws IOException {
            param0.skipBytes(param0.readInt() * 8);
        }

        @Override
        public String getName() {
            return "LONG[]";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Long_Array";
        }
    };
    private long[] data;

    public LongArrayTag(long[] param0) {
        this.data = param0;
    }

    public LongArrayTag(LongSet param0) {
        this.data = param0.toLongArray();
    }

    public LongArrayTag(List<Long> param0) {
        this(toArray(param0));
    }

    private static long[] toArray(List<Long> param0) {
        long[] var0 = new long[param0.size()];

        for(int var1 = 0; var1 < param0.size(); ++var1) {
            Long var2 = param0.get(var1);
            var0[var1] = var2 == null ? 0L : var2;
        }

        return var0;
    }

    @Override
    public void write(DataOutput param0) throws IOException {
        param0.writeInt(this.data.length);

        for(long var0 : this.data) {
            param0.writeLong(var0);
        }

    }

    @Override
    public byte getId() {
        return 12;
    }

    @Override
    public TagType<LongArrayTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return this.getAsString();
    }

    public LongArrayTag copy() {
        long[] var0 = new long[this.data.length];
        System.arraycopy(this.data, 0, var0, 0, this.data.length);
        return new LongArrayTag(var0);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            return param0 instanceof LongArrayTag && Arrays.equals(this.data, ((LongArrayTag)param0).data);
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    @Override
    public void accept(TagVisitor param0) {
        param0.visitLongArray(this);
    }

    public long[] getAsLongArray() {
        return this.data;
    }

    @Override
    public int size() {
        return this.data.length;
    }

    public LongTag get(int param0) {
        return LongTag.valueOf(this.data[param0]);
    }

    public LongTag set(int param0, LongTag param1) {
        long var0 = this.data[param0];
        this.data[param0] = param1.getAsLong();
        return LongTag.valueOf(var0);
    }

    public void add(int param0, LongTag param1) {
        this.data = ArrayUtils.add(this.data, param0, param1.getAsLong());
    }

    @Override
    public boolean setTag(int param0, Tag param1) {
        if (param1 instanceof NumericTag) {
            this.data[param0] = ((NumericTag)param1).getAsLong();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean addTag(int param0, Tag param1) {
        if (param1 instanceof NumericTag) {
            this.data = ArrayUtils.add(this.data, param0, ((NumericTag)param1).getAsLong());
            return true;
        } else {
            return false;
        }
    }

    public LongTag remove(int param0) {
        long var0 = this.data[param0];
        this.data = ArrayUtils.remove(this.data, param0);
        return LongTag.valueOf(var0);
    }

    @Override
    public byte getElementType() {
        return 4;
    }

    @Override
    public void clear() {
        this.data = new long[0];
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor param0) {
        return param0.visit(this.data);
    }
}
