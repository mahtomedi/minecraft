package net.minecraft.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record PngInfo(int width, int height) {
    private static final long PNG_HEADER = -8552249625308161526L;
    private static final int IHDR_TYPE = 1229472850;
    private static final int IHDR_SIZE = 13;

    public static PngInfo fromStream(InputStream param0) throws IOException {
        DataInputStream var0 = new DataInputStream(param0);
        if (var0.readLong() != -8552249625308161526L) {
            throw new IOException("Bad PNG Signature");
        } else if (var0.readInt() != 13) {
            throw new IOException("Bad length for IHDR chunk!");
        } else if (var0.readInt() != 1229472850) {
            throw new IOException("Bad type for IHDR chunk!");
        } else {
            int var1 = var0.readInt();
            int var2 = var0.readInt();
            return new PngInfo(var1, var2);
        }
    }

    public static PngInfo fromBytes(byte[] param0) throws IOException {
        return fromStream(new ByteArrayInputStream(param0));
    }

    public static void validateHeader(ByteBuffer param0) throws IOException {
        ByteOrder var0 = param0.order();
        param0.order(ByteOrder.BIG_ENDIAN);
        if (param0.getLong(0) != -8552249625308161526L) {
            throw new IOException("Bad PNG Signature");
        } else if (param0.getInt(8) != 13) {
            throw new IOException("Bad length for IHDR chunk!");
        } else if (param0.getInt(12) != 1229472850) {
            throw new IOException("Bad type for IHDR chunk!");
        } else {
            param0.order(var0);
        }
    }
}
