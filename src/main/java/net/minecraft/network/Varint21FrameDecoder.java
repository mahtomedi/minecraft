package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import java.util.List;
import javax.annotation.Nullable;

public class Varint21FrameDecoder extends ByteToMessageDecoder {
    private static final int MAX_VARINT21_BYTES = 3;
    private final ByteBuf helperBuf = Unpooled.directBuffer(3);
    @Nullable
    private final BandwidthDebugMonitor monitor;

    public Varint21FrameDecoder(@Nullable BandwidthDebugMonitor param0) {
        this.monitor = param0;
    }

    @Override
    protected void handlerRemoved0(ChannelHandlerContext param0) {
        this.helperBuf.release();
    }

    private static boolean copyVarint(ByteBuf param0, ByteBuf param1) {
        for(int var0 = 0; var0 < 3; ++var0) {
            if (!param0.isReadable()) {
                return false;
            }

            byte var1 = param0.readByte();
            param1.writeByte(var1);
            if (!VarInt.hasContinuationBit(var1)) {
                return true;
            }
        }

        throw new CorruptedFrameException("length wider than 21-bit");
    }

    @Override
    protected void decode(ChannelHandlerContext param0, ByteBuf param1, List<Object> param2) {
        param1.markReaderIndex();
        this.helperBuf.clear();
        if (!copyVarint(param1, this.helperBuf)) {
            param1.resetReaderIndex();
        } else {
            int var0 = VarInt.read(this.helperBuf);
            if (param1.readableBytes() < var0) {
                param1.resetReaderIndex();
            } else {
                if (this.monitor != null) {
                    this.monitor.onReceive(var0 + VarInt.getByteSize(var0));
                }

                param2.add(param1.readBytes(var0));
            }
        }
    }
}
