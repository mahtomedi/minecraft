package net.minecraft.server.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.net.SocketAddress;
import java.util.Locale;
import net.minecraft.server.ServerInfo;
import org.slf4j.Logger;

public class LegacyQueryHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ServerInfo server;

    public LegacyQueryHandler(ServerInfo param0) {
        this.server = param0;
    }

    @Override
    public void channelRead(ChannelHandlerContext param0, Object param1) {
        ByteBuf var0 = (ByteBuf)param1;
        var0.markReaderIndex();
        boolean var1 = true;

        try {
            if (var0.readUnsignedByte() == 254) {
                SocketAddress var2 = param0.channel().remoteAddress();
                int var3 = var0.readableBytes();
                if (var3 == 0) {
                    LOGGER.debug("Ping: (<1.3.x) from {}", var2);
                    String var4 = createVersion0Response(this.server);
                    sendFlushAndClose(param0, createLegacyDisconnectPacket(param0.alloc(), var4));
                } else {
                    if (var0.readUnsignedByte() != 1) {
                        return;
                    }

                    if (var0.isReadable()) {
                        if (!readCustomPayloadPacket(var0)) {
                            return;
                        }

                        LOGGER.debug("Ping: (1.6) from {}", var2);
                    } else {
                        LOGGER.debug("Ping: (1.4-1.5.x) from {}", var2);
                    }

                    String var5 = createVersion1Response(this.server);
                    sendFlushAndClose(param0, createLegacyDisconnectPacket(param0.alloc(), var5));
                }

                var0.release();
                var1 = false;
                return;
            }
        } catch (RuntimeException var11) {
            return;
        } finally {
            if (var1) {
                var0.resetReaderIndex();
                param0.channel().pipeline().remove(this);
                param0.fireChannelRead(param1);
            }

        }

    }

    private static boolean readCustomPayloadPacket(ByteBuf param0) {
        short var0 = param0.readUnsignedByte();
        if (var0 != 250) {
            return false;
        } else {
            String var1 = LegacyProtocolUtils.readLegacyString(param0);
            if (!"MC|PingHost".equals(var1)) {
                return false;
            } else {
                int var2 = param0.readUnsignedShort();
                if (param0.readableBytes() != var2) {
                    return false;
                } else {
                    short var3 = param0.readUnsignedByte();
                    if (var3 < 73) {
                        return false;
                    } else {
                        String var4 = LegacyProtocolUtils.readLegacyString(param0);
                        int var5 = param0.readInt();
                        return var5 <= 65535;
                    }
                }
            }
        }
    }

    private static String createVersion0Response(ServerInfo param0) {
        return String.format(Locale.ROOT, "%s\u00a7%d\u00a7%d", param0.getMotd(), param0.getPlayerCount(), param0.getMaxPlayers());
    }

    private static String createVersion1Response(ServerInfo param0) {
        return String.format(
            Locale.ROOT,
            "\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d",
            127,
            param0.getServerVersion(),
            param0.getMotd(),
            param0.getPlayerCount(),
            param0.getMaxPlayers()
        );
    }

    private static void sendFlushAndClose(ChannelHandlerContext param0, ByteBuf param1) {
        param0.pipeline().firstContext().writeAndFlush(param1).addListener(ChannelFutureListener.CLOSE);
    }

    private static ByteBuf createLegacyDisconnectPacket(ByteBufAllocator param0, String param1) {
        ByteBuf var0 = param0.buffer();
        var0.writeByte(255);
        LegacyProtocolUtils.writeLegacyString(var0, param1);
        return var0;
    }
}
