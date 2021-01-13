package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import java.util.List;

public class Varint21FrameDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext param0, ByteBuf param1, List<Object> param2) throws Exception {
        param1.markReaderIndex();
        byte[] var0 = new byte[3];

        for(int var1 = 0; var1 < var0.length; ++var1) {
            if (!param1.isReadable()) {
                param1.resetReaderIndex();
                return;
            }

            var0[var1] = param1.readByte();
            if (var0[var1] >= 0) {
                FriendlyByteBuf var2 = new FriendlyByteBuf(Unpooled.wrappedBuffer(var0));

                try {
                    int var3 = var2.readVarInt();
                    if (param1.readableBytes() >= var3) {
                        param2.add(param1.readBytes(var3));
                        return;
                    }

                    param1.resetReaderIndex();
                } finally {
                    var2.release();
                }

                return;
            }
        }

        throw new CorruptedFrameException("length wider than 21-bit");
    }
}
