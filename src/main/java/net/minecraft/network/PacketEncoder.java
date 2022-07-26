package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.IOException;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketEncoder extends MessageToByteEncoder<Packet<?>> {
    private static final Logger LOGGER = LogUtils.getLogger();
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
                LOGGER.debug(
                    Connection.PACKET_SENT_MARKER,
                    "OUT: [{}:{}] {}",
                    param0.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get(),
                    var1,
                    param1.getClass().getName()
                );
            }

            if (var1 == null) {
                throw new IOException("Can't serialize unregistered packet");
            } else {
                FriendlyByteBuf var2 = new FriendlyByteBuf(param2);
                var2.writeVarInt(var1);

                try {
                    int var3 = var2.writerIndex();
                    param1.write(var2);
                    int var4 = var2.writerIndex() - var3;
                    if (var4 > 8388608) {
                        throw new IllegalArgumentException("Packet too big (is " + var4 + ", should be less than 8388608): " + param1);
                    } else {
                        int var5 = param0.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().getId();
                        JvmProfiler.INSTANCE.onPacketSent(var5, var1, param0.channel().remoteAddress(), var4);
                    }
                } catch (Throwable var10) {
                    LOGGER.error("Error receiving packet {}", var1, var10);
                    if (param1.isSkippable()) {
                        throw new SkipPacketException(var10);
                    } else {
                        throw var10;
                    }
                }
            }
        }
    }
}
