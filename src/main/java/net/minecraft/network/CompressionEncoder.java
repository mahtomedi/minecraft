package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.zip.Deflater;

public class CompressionEncoder extends MessageToByteEncoder<ByteBuf> {
    private final byte[] encodeBuf = new byte[8192];
    private final Deflater deflater;
    private int threshold;

    public CompressionEncoder(int param0) {
        this.threshold = param0;
        this.deflater = new Deflater();
    }

    protected void encode(ChannelHandlerContext param0, ByteBuf param1, ByteBuf param2) {
        int var0 = param1.readableBytes();
        FriendlyByteBuf var1 = new FriendlyByteBuf(param2);
        if (var0 < this.threshold) {
            var1.writeVarInt(0);
            var1.writeBytes(param1);
        } else {
            byte[] var2 = new byte[var0];
            param1.readBytes(var2);
            var1.writeVarInt(var2.length);
            this.deflater.setInput(var2, 0, var0);
            this.deflater.finish();

            while(!this.deflater.finished()) {
                int var3 = this.deflater.deflate(this.encodeBuf);
                var1.writeBytes(this.encodeBuf, 0, var3);
            }

            this.deflater.reset();
        }

    }

    public void setThreshold(int param0) {
        this.threshold = param0;
    }
}
