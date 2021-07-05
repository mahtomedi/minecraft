package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import java.util.List;
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
            FriendlyByteBuf var0 = new FriendlyByteBuf(param1);
            int var1 = var0.readVarInt();
            if (var1 == 0) {
                param2.add(var0.readBytes(var0.readableBytes()));
            } else {
                if (this.validateDecompressed) {
                    if (var1 < this.threshold) {
                        throw new DecoderException("Badly compressed packet - size of " + var1 + " is below server threshold of " + this.threshold);
                    }

                    if (var1 > 8388608) {
                        throw new DecoderException("Badly compressed packet - size of " + var1 + " is larger than protocol maximum of 8388608");
                    }
                }

                byte[] var2 = new byte[var0.readableBytes()];
                var0.readBytes(var2);
                this.inflater.setInput(var2);
                byte[] var3 = new byte[var1];
                this.inflater.inflate(var3);
                param2.add(Unpooled.wrappedBuffer(var3));
                this.inflater.reset();
            }
        }
    }

    public void setThreshold(int param0, boolean param1) {
        this.threshold = param0;
        this.validateDecompressed = param1;
    }
}
