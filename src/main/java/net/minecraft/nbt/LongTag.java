package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class LongTag extends NumericTag {
    private long data;

    LongTag() {
    }

    public LongTag(long param0) {
        this.data = param0;
    }

    @Override
    public void write(DataOutput param0) throws IOException {
        param0.writeLong(this.data);
    }

    @Override
    public void load(DataInput param0, int param1, NbtAccounter param2) throws IOException {
        param2.accountBits(128L);
        this.data = param0.readLong();
    }

    @Override
    public byte getId() {
        return 4;
    }

    @Override
    public String toString() {
        return this.data + "L";
    }

    public LongTag copy() {
        return new LongTag(this.data);
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
    public Component getPrettyDisplay(String param0, int param1) {
        Component var0 = new TextComponent("L").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        return new TextComponent(String.valueOf(this.data)).append(var0).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
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
}
