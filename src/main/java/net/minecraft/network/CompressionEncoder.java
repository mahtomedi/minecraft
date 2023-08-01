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
        if (var0 < this.threshold) {
            VarInt.write(param2, 0);
            param2.writeBytes(param1);
        } else {
            byte[] var1 = new byte[var0];
            param1.readBytes(var1);
            VarInt.write(param2, var1.length);
            this.deflater.setInput(var1, 0, var0);
            this.deflater.finish();

            while(!this.deflater.finished()) {
                int var2 = this.deflater.deflate(this.encodeBuf);
                param2.writeBytes(this.encodeBuf, 0, var2);
            }

            this.deflater.reset();
        }

    }

    public int getThreshold() {
        return this.threshold;
    }

    public void setThreshold(int param0) {
        this.threshold = param0;
    }
}
