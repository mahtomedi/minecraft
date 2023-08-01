package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class CompressionDecoder extends ByteToMessageDecoder {
    public static final int MAXIMUM_COMPRESSED_LENGTH = 2097152;
    public static final int MAXIMUM_UNCOMPRESSED_LENGTH = 8388608;
    private final Inflater inflater;
    private int threshold;
    private boolean validateDecompressed;

    public CompressionDecoder(int param0, boolean param1) {
        this.threshold = param0;
        this.validateDecompressed = param1;
        this.inflater = new Inflater();
    }

    @Override
    protected void decode(ChannelHandlerContext param0, ByteBuf param1, List<Object> param2) throws Exception {
        if (param1.readableBytes() != 0) {
            int var0 = VarInt.read(param1);
            if (var0 == 0) {
                param2.add(param1.readBytes(param1.readableBytes()));
            } else {
                if (this.validateDecompressed) {
                    if (var0 < this.threshold) {
                        throw new DecoderException("Badly compressed packet - size of " + var0 + " is below server threshold of " + this.threshold);
                    }

                    if (var0 > 8388608) {
                        throw new DecoderException("Badly compressed packet - size of " + var0 + " is larger than protocol maximum of 8388608");
                    }
                }

                this.setupInflaterInput(param1);
                ByteBuf var1 = this.inflate(param0, var0);
                this.inflater.reset();
                param2.add(var1);
            }
        }
    }

    private void setupInflaterInput(ByteBuf param0) {
        ByteBuffer var0;
        if (param0.nioBufferCount() > 0) {
            var0 = param0.nioBuffer();
            param0.skipBytes(param0.readableBytes());
        } else {
            var0 = ByteBuffer.allocateDirect(param0.readableBytes());
            param0.readBytes(var0);
            var0.flip();
        }

        this.inflater.setInput(var0);
    }

    private ByteBuf inflate(ChannelHandlerContext param0, int param1) throws DataFormatException {
        ByteBuf var0 = param0.alloc().directBuffer(param1);

        try {
            ByteBuffer var1 = var0.internalNioBuffer(0, param1);
            int var2 = var1.position();
            this.inflater.inflate(var1);
            int var3 = var1.position() - var2;
            if (var3 != param1) {
                throw new DecoderException(
                    "Badly compressed packet - actual length of uncompressed payload " + var3 + " is does not match declared size " + param1
                );
            } else {
                var0.writerIndex(var0.writerIndex() + var3);
                return var0;
            }
        } catch (Exception var7) {
            var0.release();
            throw var7;
        }
    }

    public void setThreshold(int param0, boolean param1) {
        this.threshold = param0;
        this.validateDecompressed = param1;
    }
}
