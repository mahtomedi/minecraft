package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.IOException;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class PacketEncoder extends MessageToByteEncoder<Packet<?>> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker MARKER = MarkerManager.getMarker("PACKET_SENT", Connection.PACKET_MARKER);
    private final PacketFlow flow;

    public PacketEncoder(PacketFlow param0) {
        this.flow = param0;
    }

    protected void encode(ChannelHandlerContext param0, Packet<?> param1, ByteBuf param2) throws Exception {
        ConnectionProtocol var0 = param0.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get();
        if (var0 == null) {
            throw new RuntimeException("ConnectionProtocol unknown: " + param1);
        } else {
            Integer var1 = var0.getPacketId(this.flow, param1);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MARKER, "OUT: [{}:{}] {}", param0.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get(), var1, param1.getClass().getName());
            }

            if (var1 == null) {
                throw new IOException("Can't serialize unregistered packet");
            } else {
                FriendlyByteBuf var2 = new FriendlyByteBuf(param2);
                var2.writeVarInt(var1);

                try {
                    param1.write(var2);
                } catch (Throwable var8) {
                    LOGGER.error(var8);
                    if (param1.isSkippable()) {
                        throw new SkipPacketException(var8);
                    } else {
                        throw var8;
                    }
                }
            }
        }
    }
}
