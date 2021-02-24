package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class PacketDecoder extends ByteToMessageDecoder {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker MARKER = MarkerManager.getMarker("PACKET_RECEIVED", Connection.PACKET_MARKER);
    private final PacketFlow flow;

    public PacketDecoder(PacketFlow param0) {
        this.flow = param0;
    }

    @Override
    protected void decode(ChannelHandlerContext param0, ByteBuf param1, List<Object> param2) throws Exception {
        if (param1.readableBytes() != 0) {
            FriendlyByteBuf var0 = new FriendlyByteBuf(param1);
            int var1 = var0.readVarInt();
            Packet<?> var2 = param0.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().createPacket(this.flow, var1, var0);
            if (var2 == null) {
                throw new IOException("Bad packet id " + var1);
            } else if (var0.readableBytes() > 0) {
                throw new IOException(
                    "Packet "
                        + param0.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().getId()
                        + "/"
                        + var1
                        + " ("
                        + var2.getClass().getSimpleName()
                        + ") was larger than I expected, found "
                        + var0.readableBytes()
                        + " bytes extra whilst reading packet "
                        + var1
                );
            } else {
                param2.add(var2);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(MARKER, " IN: [{}:{}] {}", param0.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get(), var1, var2.getClass().getName());
                }

            }
        }
    }
}
