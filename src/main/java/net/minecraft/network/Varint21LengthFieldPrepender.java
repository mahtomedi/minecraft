package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToByteEncoder;

@Sharable
public class Varint21LengthFieldPrepender extends MessageToByteEncoder<ByteBuf> {
    protected void encode(ChannelHandlerContext param0, ByteBuf param1, ByteBuf param2) throws Exception {
        int var0 = param1.readableBytes();
        int var1 = FriendlyByteBuf.getVarIntSize(var0);
        if (var1 > 3) {
            throw new IllegalArgumentException("unable to fit " + var0 + " into " + 3);
        } else {
            FriendlyByteBuf var2 = new FriendlyByteBuf(param2);
            var2.ensureWritable(var1 + var0);
            var2.writeVarInt(var0);
            var2.writeBytes(param1, param1.readerIndex(), var0);
        }
    }
}
