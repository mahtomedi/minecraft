package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;

public class DoubleTag extends NumericTag {
    private double data;

    DoubleTag() {
    }

    public DoubleTag(double param0) {
        this.data = param0;
    }

    @Override
    public void write(DataOutput param0) throws IOException {
        param0.writeDouble(this.data);
    }

    @Override
    public void load(DataInput param0, int param1, NbtAccounter param2) throws IOException {
        param2.accountBits(128L);
        this.data = param0.readDouble();
    }

    @Override
    public byte getId() {
        return 6;
    }

    @Override
    public String toString() {
        return this.data + "d";
    }

    public DoubleTag copy() {
        return new DoubleTag(this.data);
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
    public Component getPrettyDisplay(String param0, int param1) {
        Component var0 = new TextComponent("d").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        return new TextComponent(String.valueOf(this.data)).append(var0).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
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
}
