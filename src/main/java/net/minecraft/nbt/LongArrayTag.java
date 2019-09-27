package net.minecraft.nbt;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.apache.commons.lang3.ArrayUtils;

public class LongArrayTag extends CollectionTag<LongTag> {
    public static final TagType<LongArrayTag> TYPE = new TagType<LongArrayTag>() {
        public LongArrayTag load(DataInput param0, int param1, NbtAccounter param2) throws IOException {
            param2.accountBits(192L);
            int var0 = param0.readInt();
            param2.accountBits((long)(64 * var0));
            long[] var1 = new long[var0];

            for(int var2 = 0; var2 < var0; ++var2) {
                var1[var2] = param0.readLong();
            }

            return new LongArrayTag(var1);
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
        StringBuilder var0 = new StringBuilder("[L;");

        for(int var1 = 0; var1 < this.data.length; ++var1) {
            if (var1 != 0) {
                var0.append(',');
            }

            var0.append(this.data[var1]).append('L');
        }

        return var0.append(']').toString();
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
    public Component getPrettyDisplay(String param0, int param1) {
        Component var0 = new TextComponent("L").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        Component var1 = new TextComponent("[").append(var0).append(";");

        for(int var2 = 0; var2 < this.data.length; ++var2) {
            Component var3 = new TextComponent(String.valueOf(this.data[var2])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
            var1.append(" ").append(var3).append(var0);
            if (var2 != this.data.length - 1) {
                var1.append(",");
            }
        }

        var1.append("]");
        return var1;
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
    public void clear() {
        this.data = new long[0];
    }
}
