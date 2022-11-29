package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.Mth;

public class DoubleTag extends NumericTag {
    private static final int SELF_SIZE_IN_BITS = 128;
    public static final DoubleTag ZERO = new DoubleTag(0.0);
    public static final TagType<DoubleTag> TYPE = new TagType.StaticSize<DoubleTag>() {
        public DoubleTag load(DataInput param0, int param1, NbtAccounter param2) throws IOException {
            param2.accountBits(128L);
            return DoubleTag.valueOf(param0.readDouble());
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput param0, StreamTagVisitor param1) throws IOException {
            return param1.visit(param0.readDouble());
        }

        @Override
        public int size() {
            return 8;
        }

        @Override
        public String getName() {
            return "DOUBLE";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Double";
        }

        @Override
        public boolean isValue() {
            return true;
        }
    };
    private final double data;

    private DoubleTag(double param0) {
        this.data = param0;
    }

    public static DoubleTag valueOf(double param0) {
        return param0 == 0.0 ? ZERO : new DoubleTag(param0);
    }

    @Override
    public void write(DataOutput param0) throws IOException {
        param0.writeDouble(this.data);
    }

    @Override
    public int sizeInBits() {
        return 128;
    }

    @Override
    public byte getId() {
        return 6;
    }

    @Override
    public TagType<DoubleTag> getType() {
        return TYPE;
    }

    public DoubleTag copy() {
        return this;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            return param0 instanceof DoubleTag && this.data == ((DoubleTag)param0).data;
        }
    }

    @Override
    public int hashCode() {
        long var0 = Double.doubleToLongBits(this.data);
        return (int)(var0 ^ var0 >>> 32);
    }

    @Override
    public void accept(TagVisitor param0) {
        param0.visitDouble(this);
    }

    @Override
    public long getAsLong() {
        return (long)Math.floor(this.data);
    }

    @Override
    public int getAsInt() {
        return Mth.floor(this.data);
    }

    @Override
    public short getAsShort() {
        return (short)(Mth.floor(this.data) & 65535);
    }

    @Override
    public byte getAsByte() {
        return (byte)(Mth.floor(this.data) & 0xFF);
    }

    @Override
    public double getAsDouble() {
        return this.data;
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
}
