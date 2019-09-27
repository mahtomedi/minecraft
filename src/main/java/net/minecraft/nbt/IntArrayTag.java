package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.apache.commons.lang3.ArrayUtils;

public class IntArrayTag extends CollectionTag<IntTag> {
    public static final TagType<IntArrayTag> TYPE = new TagType<IntArrayTag>() {
        public IntArrayTag load(DataInput param0, int param1, NbtAccounter param2) throws IOException {
            param2.accountBits(192L);
            int var0 = param0.readInt();
            param2.accountBits((long)(32 * var0));
            int[] var1 = new int[var0];

            for(int var2 = 0; var2 < var0; ++var2) {
                var1[var2] = param0.readInt();
            }

            return new IntArrayTag(var1);
        }

        @Override
        public String getName() {
            return "INT[]";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Int";
        }
    };
    private int[] data;

    public IntArrayTag(int[] param0) {
        this.data = param0;
    }

    public IntArrayTag(List<Integer> param0) {
        this(toArray(param0));
    }

    private static int[] toArray(List<Integer> param0) {
        int[] var0 = new int[param0.size()];

        for(int var1 = 0; var1 < param0.size(); ++var1) {
            Integer var2 = param0.get(var1);
            var0[var1] = var2 == null ? 0 : var2;
        }

        return var0;
    }

    @Override
    public void write(DataOutput param0) throws IOException {
        param0.writeInt(this.data.length);

        for(int var0 : this.data) {
            param0.writeInt(var0);
        }

    }

    @Override
    public byte getId() {
        return 11;
    }

    @Override
    public TagType<IntArrayTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringBuilder var0 = new StringBuilder("[I;");

        for(int var1 = 0; var1 < this.data.length; ++var1) {
            if (var1 != 0) {
                var0.append(',');
            }

            var0.append(this.data[var1]);
        }

        return var0.append(']').toString();
    }

    public IntArrayTag copy() {
        int[] var0 = new int[this.data.length];
        System.arraycopy(this.data, 0, var0, 0, this.data.length);
        return new IntArrayTag(var0);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            return param0 instanceof IntArrayTag && Arrays.equals(this.data, ((IntArrayTag)param0).data);
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    public int[] getAsIntArray() {
        return this.data;
    }

    @Override
    public Component getPrettyDisplay(String param0, int param1) {
        Component var0 = new TextComponent("I").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        Component var1 = new TextComponent("[").append(var0).append(";");

        for(int var2 = 0; var2 < this.data.length; ++var2) {
            var1.append(" ").append(new TextComponent(String.valueOf(this.data[var2])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER));
            if (var2 != this.data.length - 1) {
                var1.append(",");
            }
        }

        var1.append("]");
        return var1;
    }

    @Override
    public int size() {
        return this.data.length;
    }

    public IntTag get(int param0) {
        return IntTag.valueOf(this.data[param0]);
    }

    public IntTag set(int param0, IntTag param1) {
        int var0 = this.data[param0];
        this.data[param0] = param1.getAsInt();
        return IntTag.valueOf(var0);
    }

    public void add(int param0, IntTag param1) {
        this.data = ArrayUtils.add(this.data, param0, param1.getAsInt());
    }

    @Override
    public boolean setTag(int param0, Tag param1) {
        if (param1 instanceof NumericTag) {
            this.data[param0] = ((NumericTag)param1).getAsInt();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean addTag(int param0, Tag param1) {
        if (param1 instanceof NumericTag) {
            this.data = ArrayUtils.add(this.data, param0, ((NumericTag)param1).getAsInt());
            return true;
        } else {
            return false;
        }
    }

    public IntTag remove(int param0) {
        int var0 = this.data[param0];
        this.data = ArrayUtils.remove(this.data, param0);
        return IntTag.valueOf(var0);
    }

    @Override
    public void clear() {
        this.data = new int[0];
    }
}
