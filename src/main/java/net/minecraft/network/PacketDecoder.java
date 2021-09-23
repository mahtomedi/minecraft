package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.util.profiling.jfr.JvmProfiler;
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
        int var0 = param1.readableBytes();
        if (var0 != 0) {
            FriendlyByteBuf var1 = new FriendlyByteBuf(param1);
            int var2 = var1.readVarInt();
            Packet<?> var3 = param0.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().createPacket(this.flow, var2, var1);
            if (var3 == null) {
                throw new IOException("Bad packet id " + var2);
            } else {
                int var4 = param0.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().getId();
                JvmProfiler.INSTANCE
                    .onPacketReceived(() -> "%d/%d (%s)".formatted(var4, var2, var3.getClass().getSimpleName()), param0.channel().remoteAddress(), var0);
                if (var1.readableBytes() > 0) {
                    throw new IOException(
                        "Packet "
                            + param0.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().getId()
                            + "/"
                            + var2
                            + " ("
                            + var3.getClass().getSimpleName()
                            + ") was larger than I expected, found "
                            + var1.readableBytes()
                            + " bytes extra whilst reading packet "
                            + var2
                    );
                } else {
                    param2.add(var3);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(MARKER, " IN: [{}:{}] {}", param0.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get(), var2, var3.getClass().getName());
                    }

                }
            }
        }
    }
}
