package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketDecoder extends ByteToMessageDecoder implements ProtocolSwapHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final AttributeKey<ConnectionProtocol.CodecData<?>> codecKey;

    public PacketDecoder(AttributeKey<ConnectionProtocol.CodecData<?>> param0) {
        this.codecKey = param0;
    }

    @Override
    protected void decode(ChannelHandlerContext param0, ByteBuf param1, List<Object> param2) throws Exception {
        int var0 = param1.readableBytes();
        if (var0 != 0) {
            Attribute<ConnectionProtocol.CodecData<?>> var1 = param0.channel().attr(this.codecKey);
            ConnectionProtocol.CodecData<?> var2 = var1.get();
            FriendlyByteBuf var3 = new FriendlyByteBuf(param1);
            int var4 = var3.readVarInt();
            Packet<?> var5 = var2.createPacket(var4, var3);
            if (var5 == null) {
                throw new IOException("Bad packet id " + var4);
            } else {
                JvmProfiler.INSTANCE.onPacketReceived(var2.protocol(), var4, param0.channel().remoteAddress(), var0);
                if (var3.readableBytes() > 0) {
                    throw new IOException(
                        "Packet "
                            + var2.protocol().id()
                            + "/"
                            + var4
                            + " ("
                            + var5.getClass().getSimpleName()
                            + ") was larger than I expected, found "
                            + var3.readableBytes()
                            + " bytes extra whilst reading packet "
                            + var4
                    );
                } else {
                    param2.add(var5);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(Connection.PACKET_RECEIVED_MARKER, " IN: [{}:{}] {}", var2.protocol().id(), var4, var5.getClass().getName());
                    }

                    ProtocolSwapHandler.swapProtocolIfNeeded(var1, var5);
                }
            }
        }
    }
}
