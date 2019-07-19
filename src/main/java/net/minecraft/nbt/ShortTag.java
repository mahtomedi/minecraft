package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class ShortTag extends NumericTag {
    private short data;

    public ShortTag() {
    }

    public ShortTag(short param0) {
        this.data = param0;
    }

    @Override
    public void write(DataOutput param0) throws IOException {
        param0.writeShort(this.data);
    }

    @Override
    public void load(DataInput param0, int param1, NbtAccounter param2) throws IOException {
        param2.accountBits(80L);
        this.data = param0.readShort();
    }

    @Override
    public byte getId() {
        return 2;
    }

    @Override
    public String toString() {
        return this.data + "s";
    }

    public ShortTag copy() {
        return new ShortTag(this.data);
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
    public Component getPrettyDisplay(String param0, int param1) {
        Component var0 = new TextComponent("s").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        return new TextComponent(String.valueOf(this.data)).append(var0).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
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
}
