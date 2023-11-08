package net.minecraft.util;

import java.io.DataOutput;
import java.io.IOException;

public class DelegateDataOutput implements DataOutput {
    private final DataOutput parent;

    public DelegateDataOutput(DataOutput param0) {
        this.parent = param0;
    }

    @Override
    public void write(int param0) throws IOException {
        this.parent.write(param0);
    }

    @Override
    public void write(byte[] param0) throws IOException {
        this.parent.write(param0);
    }

    @Override
    public void write(byte[] param0, int param1, int param2) throws IOException {
        this.parent.write(param0, param1, param2);
    }

    @Override
    public void writeBoolean(boolean param0) throws IOException {
        this.parent.writeBoolean(param0);
    }

    @Override
    public void writeByte(int param0) throws IOException {
        this.parent.writeByte(param0);
    }

    @Override
    public void writeShort(int param0) throws IOException {
        this.parent.writeShort(param0);
    }

    @Override
    public void writeChar(int param0) throws IOException {
        this.parent.writeChar(param0);
    }

    @Override
    public void writeInt(int param0) throws IOException {
        this.parent.writeInt(param0);
    }

    @Override
    public void writeLong(long param0) throws IOException {
        this.parent.writeLong(param0);
    }

    @Override
    public void writeFloat(float param0) throws IOException {
        this.parent.writeFloat(param0);
    }

    @Override
    public void writeDouble(double param0) throws IOException {
        this.parent.writeDouble(param0);
    }

    @Override
    public void writeBytes(String param0) throws IOException {
        this.parent.writeBytes(param0);
    }

    @Override
    public void writeChars(String param0) throws IOException {
        this.parent.writeChars(param0);
    }

    @Override
    public void writeUTF(String param0) throws IOException {
        this.parent.writeUTF(param0);
    }
}
