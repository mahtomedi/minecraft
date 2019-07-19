package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;

public class FloatTag extends NumericTag {
    private float data;

    FloatTag() {
    }

    public FloatTag(float param0) {
        this.data = param0;
    }

    @Override
    public void write(DataOutput param0) throws IOException {
        param0.writeFloat(this.data);
    }

    @Override
    public void load(DataInput param0, int param1, NbtAccounter param2) throws IOException {
        param2.accountBits(96L);
        this.data = param0.readFloat();
    }

    @Override
    public byte getId() {
        return 5;
    }

    @Override
    public String toString() {
        return this.data + "f";
    }

    public FloatTag copy() {
        return new FloatTag(this.data);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            return param0 instanceof FloatTag && this.data == ((FloatTag)param0).data;
        }
    }

    @Override
    public int hashCode() {
        return Float.floatToIntBits(this.data);
    }

    @Override
    public Component getPrettyDisplay(String param0, int param1) {
        Component var0 = new TextComponent("f").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        return new TextComponent(String.valueOf(this.data)).append(var0).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public long getAsLong() {
        return (long)this.data;
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
        return (double)this.data;
    }

    @Override
    public float getAsFloat() {
        return this.data;
    }

    @Override
    public Number getAsNumber() {
        return this.data;
    }
}
