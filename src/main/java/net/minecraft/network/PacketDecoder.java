package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketDecoder extends ByteToMessageDecoder {
    private static final Logger LOGGER = LogUtils.getLogger();
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
                JvmProfiler.INSTANCE.onPacketReceived(var4, var2, param0.channel().remoteAddress(), var0);
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
                        LOGGER.debug(
                            Connection.PACKET_RECEIVED_MARKER,
                            " IN: [{}:{}] {}",
                            param0.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get(),
                            var2,
                            var3.getClass().getName()
                        );
                    }

                }
            }
        }
    }
}
