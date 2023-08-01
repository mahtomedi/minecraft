package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;

@Sharable
public class Varint21LengthFieldPrepender extends MessageToByteEncoder<ByteBuf> {
    public static final int MAX_VARINT21_BYTES = 3;

    protected void encode(ChannelHandlerContext param0, ByteBuf param1, ByteBuf param2) {
        int var0 = param1.readableBytes();
        int var1 = VarInt.getByteSize(var0);
        if (var1 > 3) {
            throw new EncoderException("unable to fit " + var0 + " into 3");
        } else {
            param2.ensureWritable(var1 + var0);
            VarInt.write(param2, var0);
            param2.writeBytes(param1, param1.readerIndex(), var0);
        }
    }
}
