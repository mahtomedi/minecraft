package net.minecraft.util;

import java.io.IOException;
import java.io.InputStream;

public class FastBufferedInputStream extends InputStream {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private final InputStream in;
    private final byte[] buffer;
    private int limit;
    private int position;

    public FastBufferedInputStream(InputStream param0) {
        this(param0, 8192);
    }

    public FastBufferedInputStream(InputStream param0, int param1) {
        this.in = param0;
        this.buffer = new byte[param1];
    }

    @Override
    public int read() throws IOException {
        if (this.position >= this.limit) {
            this.fill();
            if (this.position >= this.limit) {
                return -1;
            }
        }

        return Byte.toUnsignedInt(this.buffer[this.position++]);
    }

    @Override
    public int read(byte[] param0, int param1, int param2) throws IOException {
        int var0 = this.bytesInBuffer();
        if (var0 <= 0) {
            if (param2 >= this.buffer.length) {
                return this.in.read(param0, param1, param2);
            }

            this.fill();
            var0 = this.bytesInBuffer();
            if (var0 <= 0) {
                return -1;
            }
        }

        if (param2 > var0) {
            param2 = var0;
        }

        System.arraycopy(this.buffer, this.position, param0, param1, param2);
        this.position += param2;
        return param2;
    }

    @Override
    public long skip(long param0) throws IOException {
        if (param0 <= 0L) {
            return 0L;
        } else {
            long var0 = (long)this.bytesInBuffer();
            if (var0 <= 0L) {
                return this.in.skip(param0);
            } else {
                if (param0 > var0) {
                    param0 = var0;
                }

                this.position = (int)((long)this.position + param0);
                return param0;
            }
        }
    }

    @Override
    public int available() throws IOException {
        return this.bytesInBuffer() + this.in.available();
    }

    @Override
    public void close() throws IOException {
        this.in.close();
    }

    private int bytesInBuffer() {
        return this.limit - this.position;
    }

    private void fill() throws IOException {
        this.limit = 0;
        this.position = 0;
        int var0 = this.in.read(this.buffer, 0, this.buffer.length);
        if (var0 > 0) {
            this.limit = var0;
        }

    }
}
