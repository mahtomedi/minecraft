package com.mojang.blaze3d.audio;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public class OggAudioStream implements AudioStream {
    private static final int EXPECTED_MAX_FRAME_SIZE = 8192;
    private long handle;
    private final AudioFormat audioFormat;
    private final InputStream input;
    private ByteBuffer buffer = MemoryUtil.memAlloc(8192);

    public OggAudioStream(InputStream param0) throws IOException {
        this.input = param0;
        ((Buffer)this.buffer).limit(0);

        try (MemoryStack var0 = MemoryStack.stackPush()) {
            IntBuffer var1 = var0.mallocInt(1);
            IntBuffer var2 = var0.mallocInt(1);

            while(this.handle == 0L) {
                if (!this.refillFromStream()) {
                    throw new IOException("Failed to find Ogg header");
                }

                int var3 = this.buffer.position();
                ((Buffer)this.buffer).position(0);
                this.handle = STBVorbis.stb_vorbis_open_pushdata(this.buffer, var1, var2, null);
                ((Buffer)this.buffer).position(var3);
                int var4 = var2.get(0);
                if (var4 == 1) {
                    this.forwardBuffer();
                } else if (var4 != 0) {
                    throw new IOException("Failed to read Ogg file " + var4);
                }
            }

            ((Buffer)this.buffer).position(this.buffer.position() + var1.get(0));
            STBVorbisInfo var5 = STBVorbisInfo.mallocStack(var0);
            STBVorbis.stb_vorbis_get_info(this.handle, var5);
            this.audioFormat = new AudioFormat((float)var5.sample_rate(), 16, var5.channels(), true, false);
        }

    }

    private boolean refillFromStream() throws IOException {
        int var0 = this.buffer.limit();
        int var1 = this.buffer.capacity() - var0;
        if (var1 == 0) {
            return true;
        } else {
            byte[] var2 = new byte[var1];
            int var3 = this.input.read(var2);
            if (var3 == -1) {
                return false;
            } else {
                int var4 = this.buffer.position();
                ((Buffer)this.buffer).limit(var0 + var3);
                ((Buffer)this.buffer).position(var0);
                this.buffer.put(var2, 0, var3);
                ((Buffer)this.buffer).position(var4);
                return true;
            }
        }
    }

    private void forwardBuffer() {
        boolean var0 = this.buffer.position() == 0;
        boolean var1 = this.buffer.position() == this.buffer.limit();
        if (var1 && !var0) {
            ((Buffer)this.buffer).position(0);
            ((Buffer)this.buffer).limit(0);
        } else {
            ByteBuffer var2 = MemoryUtil.memAlloc(var0 ? 2 * this.buffer.capacity() : this.buffer.capacity());
            var2.put(this.buffer);
            MemoryUtil.memFree(this.buffer);
            ((Buffer)var2).flip();
            this.buffer = var2;
        }

    }

    private boolean readFrame(OggAudioStream.OutputConcat param0) throws IOException {
        if (this.handle == 0L) {
            return false;
        } else {
            try (MemoryStack var0 = MemoryStack.stackPush()) {
                PointerBuffer var1 = var0.mallocPointer(1);
                IntBuffer var2 = var0.mallocInt(1);
                IntBuffer var3 = var0.mallocInt(1);

                while(true) {
                    int var4 = STBVorbis.stb_vorbis_decode_frame_pushdata(this.handle, this.buffer, var2, var1, var3);
                    ((Buffer)this.buffer).position(this.buffer.position() + var4);
                    int var5 = STBVorbis.stb_vorbis_get_error(this.handle);
                    if (var5 == 1) {
                        this.forwardBuffer();
                        if (!this.refillFromStream()) {
                            return false;
                        }
                    } else {
                        if (var5 != 0) {
                            throw new IOException("Failed to read Ogg file " + var5);
                        }

                        int var6 = var3.get(0);
                        if (var6 != 0) {
                            int var7 = var2.get(0);
                            PointerBuffer var8 = var1.getPointerBuffer(var7);
                            if (var7 != 1) {
                                if (var7 == 2) {
                                    this.convertStereo(var8.getFloatBuffer(0, var6), var8.getFloatBuffer(1, var6), param0);
                                    return true;
                                }

                                throw new IllegalStateException("Invalid number of channels: " + var7);
                            }

                            this.convertMono(var8.getFloatBuffer(0, var6), param0);
                            return true;
                        }
                    }
                }
            }
        }
    }

    private void convertMono(FloatBuffer param0, OggAudioStream.OutputConcat param1) {
        while(param0.hasRemaining()) {
            param1.put(param0.get());
        }

    }

    private void convertStereo(FloatBuffer param0, FloatBuffer param1, OggAudioStream.OutputConcat param2) {
        while(param0.hasRemaining() && param1.hasRemaining()) {
            param2.put(param0.get());
            param2.put(param1.get());
        }

    }

    @Override
    public void close() throws IOException {
        if (this.handle != 0L) {
            STBVorbis.stb_vorbis_close(this.handle);
            this.handle = 0L;
        }

        MemoryUtil.memFree(this.buffer);
        this.input.close();
    }

    @Override
    public AudioFormat getFormat() {
        return this.audioFormat;
    }

    @Override
    public ByteBuffer read(int param0) throws IOException {
        OggAudioStream.OutputConcat var0 = new OggAudioStream.OutputConcat(param0 + 8192);

        while(this.readFrame(var0) && var0.byteCount < param0) {
        }

        return var0.get();
    }

    public ByteBuffer readAll() throws IOException {
        OggAudioStream.OutputConcat var0 = new OggAudioStream.OutputConcat(16384);

        while(this.readFrame(var0)) {
        }

        return var0.get();
    }

    @OnlyIn(Dist.CLIENT)
    static class OutputConcat {
        private final List<ByteBuffer> buffers = Lists.newArrayList();
        private final int bufferSize;
        private int byteCount;
        private ByteBuffer currentBuffer;

        public OutputConcat(int param0) {
            this.bufferSize = param0 + 1 & -2;
            this.createNewBuffer();
        }

        private void createNewBuffer() {
            this.currentBuffer = BufferUtils.createByteBuffer(this.bufferSize);
        }

        public void put(float param0) {
            if (this.currentBuffer.remaining() == 0) {
                ((Buffer)this.currentBuffer).flip();
                this.buffers.add(this.currentBuffer);
                this.createNewBuffer();
            }

            int var0 = Mth.clamp((int)(param0 * 32767.5F - 0.5F), -32768, 32767);
            this.currentBuffer.putShort((short)var0);
            this.byteCount += 2;
        }

        public ByteBuffer get() {
            ((Buffer)this.currentBuffer).flip();
            if (this.buffers.isEmpty()) {
                return this.currentBuffer;
            } else {
                ByteBuffer var0 = BufferUtils.createByteBuffer(this.byteCount);
                this.buffers.forEach(var0::put);
                var0.put(this.currentBuffer);
                ((Buffer)var0).flip();
                return var0;
            }
        }
    }
}
