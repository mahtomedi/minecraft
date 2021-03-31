package com.mojang.blaze3d.platform;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.stb.STBIEOFCallback;
import org.lwjgl.stb.STBIIOCallbacks;
import org.lwjgl.stb.STBIReadCallback;
import org.lwjgl.stb.STBISkipCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public class PngInfo {
    public final int width;
    public final int height;

    public PngInfo(String param0, InputStream param1) throws IOException {
        try (
            MemoryStack var0 = MemoryStack.stackPush();
            PngInfo.StbReader var1 = createCallbacks(param1);
            STBIReadCallback var2 = STBIReadCallback.create(var1::read);
            STBISkipCallback var3 = STBISkipCallback.create(var1::skip);
            STBIEOFCallback var4 = STBIEOFCallback.create(var1::eof);
        ) {
            STBIIOCallbacks var5 = STBIIOCallbacks.mallocStack(var0);
            var5.read(var2);
            var5.skip(var3);
            var5.eof(var4);
            IntBuffer var6 = var0.mallocInt(1);
            IntBuffer var7 = var0.mallocInt(1);
            IntBuffer var8 = var0.mallocInt(1);
            if (!STBImage.stbi_info_from_callbacks(var5, 0L, var6, var7, var8)) {
                throw new IOException("Could not read info from the PNG file " + param0 + " " + STBImage.stbi_failure_reason());
            }

            this.width = var6.get(0);
            this.height = var7.get(0);
        }

    }

    private static PngInfo.StbReader createCallbacks(InputStream param0) {
        return (PngInfo.StbReader)(param0 instanceof FileInputStream
            ? new PngInfo.StbReaderSeekableByteChannel(((FileInputStream)param0).getChannel())
            : new PngInfo.StbReaderBufferedChannel(Channels.newChannel(param0)));
    }

    @OnlyIn(Dist.CLIENT)
    abstract static class StbReader implements AutoCloseable {
        protected boolean closed;

        private StbReader() {
        }

        int read(long param0, long param1, int param2) {
            try {
                return this.read(param1, param2);
            } catch (IOException var7) {
                this.closed = true;
                return 0;
            }
        }

        void skip(long param0, int param1) {
            try {
                this.skip(param1);
            } catch (IOException var5) {
                this.closed = true;
            }

        }

        int eof(long param0) {
            return this.closed ? 1 : 0;
        }

        protected abstract int read(long var1, int var3) throws IOException;

        protected abstract void skip(int var1) throws IOException;

        @Override
        public abstract void close() throws IOException;
    }

    @OnlyIn(Dist.CLIENT)
    static class StbReaderBufferedChannel extends PngInfo.StbReader {
        private static final int START_BUFFER_SIZE = 128;
        private final ReadableByteChannel channel;
        private long readBufferAddress = MemoryUtil.nmemAlloc(128L);
        private int bufferSize = 128;
        private int read;
        private int consumed;

        private StbReaderBufferedChannel(ReadableByteChannel param0) {
            this.channel = param0;
        }

        private void fillReadBuffer(int param0) throws IOException {
            ByteBuffer var0 = MemoryUtil.memByteBuffer(this.readBufferAddress, this.bufferSize);
            if (param0 + this.consumed > this.bufferSize) {
                this.bufferSize = param0 + this.consumed;
                var0 = MemoryUtil.memRealloc(var0, this.bufferSize);
                this.readBufferAddress = MemoryUtil.memAddress(var0);
            }

            ((Buffer)var0).position(this.read);

            while(param0 + this.consumed > this.read) {
                try {
                    int var1 = this.channel.read(var0);
                    if (var1 == -1) {
                        break;
                    }
                } finally {
                    this.read = var0.position();
                }
            }

        }

        @Override
        public int read(long param0, int param1) throws IOException {
            this.fillReadBuffer(param1);
            if (param1 + this.consumed > this.read) {
                param1 = this.read - this.consumed;
            }

            MemoryUtil.memCopy(this.readBufferAddress + (long)this.consumed, param0, (long)param1);
            this.consumed += param1;
            return param1;
        }

        @Override
        public void skip(int param0) throws IOException {
            if (param0 > 0) {
                this.fillReadBuffer(param0);
                if (param0 + this.consumed > this.read) {
                    throw new EOFException("Can't skip past the EOF.");
                }
            }

            if (this.consumed + param0 < 0) {
                throw new IOException("Can't seek before the beginning: " + (this.consumed + param0));
            } else {
                this.consumed += param0;
            }
        }

        @Override
        public void close() throws IOException {
            MemoryUtil.nmemFree(this.readBufferAddress);
            this.channel.close();
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class StbReaderSeekableByteChannel extends PngInfo.StbReader {
        private final SeekableByteChannel channel;

        private StbReaderSeekableByteChannel(SeekableByteChannel param0) {
            this.channel = param0;
        }

        @Override
        public int read(long param0, int param1) throws IOException {
            ByteBuffer var0 = MemoryUtil.memByteBuffer(param0, param1);
            return this.channel.read(var0);
        }

        @Override
        public void skip(int param0) throws IOException {
            this.channel.position(this.channel.position() + (long)param0);
        }

        @Override
        public int eof(long param0) {
            return super.eof(param0) != 0 && this.channel.isOpen() ? 1 : 0;
        }

        @Override
        public void close() throws IOException {
            this.channel.close();
        }
    }
}
