package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.io.IOException;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketEncoder extends MessageToByteEncoder<Packet<?>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final AttributeKey<ConnectionProtocol.CodecData<?>> codecKey;

    public PacketEncoder(AttributeKey<ConnectionProtocol.CodecData<?>> param0) {
        this.codecKey = param0;
    }

    protected void encode(ChannelHandlerContext param0, Packet<?> param1, ByteBuf param2) throws Exception {
        Attribute<ConnectionProtocol.CodecData<?>> var0 = param0.channel().attr(this.codecKey);
        ConnectionProtocol.CodecData<?> var1 = var0.get();
        if (var1 == null) {
            throw new RuntimeException("ConnectionProtocol unknown: " + param1);
        } else {
            int var2 = var1.packetId(param1);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(Connection.PACKET_SENT_MARKER, "OUT: [{}:{}] {}", var1.protocol().id(), var2, param1.getClass().getName());
            }

            if (var2 == -1) {
                throw new IOException("Can't serialize unregistered packet");
            } else {
                FriendlyByteBuf var3 = new FriendlyByteBuf(param2);
                var3.writeVarInt(var2);

                try {
                    int var4 = var3.writerIndex();
                    param1.write(var3);
                    int var5 = var3.writerIndex() - var4;
                    if (var5 > 8388608) {
                        throw new IllegalArgumentException("Packet too big (is " + var5 + ", should be less than 8388608): " + param1);
                    }

                    JvmProfiler.INSTANCE.onPacketSent(var1.protocol(), var2, param0.channel().remoteAddress(), var5);
                } catch (Throwable var13) {
                    LOGGER.error("Error receiving packet {}", var2, var13);
                    if (param1.isSkippable()) {
                        throw new SkipPacketException(var13);
                    }

                    throw var13;
                } finally {
                    ProtocolSwapHandler.swapProtocolIfNeeded(var0, param1);
                }

            }
        }
    }
}
