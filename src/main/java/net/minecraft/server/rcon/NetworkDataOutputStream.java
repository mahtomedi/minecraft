package net.minecraft.server.rcon;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NetworkDataOutputStream {
    private final ByteArrayOutputStream outputStream;
    private final DataOutputStream dataOutputStream;

    public NetworkDataOutputStream(int param0) {
        this.outputStream = new ByteArrayOutputStream(param0);
        this.dataOutputStream = new DataOutputStream(this.outputStream);
    }

    public void writeBytes(byte[] param0) throws IOException {
        this.dataOutputStream.write(param0, 0, param0.length);
    }

    public void writeString(String param0) throws IOException {
        this.dataOutputStream.writeBytes(param0);
        this.dataOutputStream.write(0);
    }

    public void write(int param0) throws IOException {
        this.dataOutputStream.write(param0);
    }

    public void writeShort(short param0) throws IOException {
        this.dataOutputStream.writeShort(Short.reverseBytes(param0));
    }

    public byte[] toByteArray() {
        return this.outputStream.toByteArray();
    }

    public void reset() {
        this.outputStream.reset();
    }
}
