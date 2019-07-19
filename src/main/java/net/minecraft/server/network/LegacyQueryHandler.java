package net.minecraft.server.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LegacyQueryHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ServerConnectionListener serverConnectionListener;

    public LegacyQueryHandler(ServerConnectionListener param0) {
        this.serverConnectionListener = param0;
    }

    @Override
    public void channelRead(ChannelHandlerContext param0, Object param1) throws Exception {
        ByteBuf var0 = (ByteBuf)param1;
        var0.markReaderIndex();
        boolean var1 = true;

        try {
            if (var0.readUnsignedByte() == 254) {
                InetSocketAddress var2 = (InetSocketAddress)param0.channel().remoteAddress();
                MinecraftServer var3 = this.serverConnectionListener.getServer();
                int var4 = var0.readableBytes();
                switch(var4) {
                    case 0:
                        LOGGER.debug("Ping: (<1.3.x) from {}:{}", var2.getAddress(), var2.getPort());
                        String var5 = String.format("%s\u00a7%d\u00a7%d", var3.getMotd(), var3.getPlayerCount(), var3.getMaxPlayers());
                        this.sendFlushAndClose(param0, this.createReply(var5));
                        break;
                    case 1:
                        if (var0.readUnsignedByte() != 1) {
                            return;
                        }

                        LOGGER.debug("Ping: (1.4-1.5.x) from {}:{}", var2.getAddress(), var2.getPort());
                        String var6 = String.format(
                            "\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d",
                            127,
                            var3.getServerVersion(),
                            var3.getMotd(),
                            var3.getPlayerCount(),
                            var3.getMaxPlayers()
                        );
                        this.sendFlushAndClose(param0, this.createReply(var6));
                        break;
                    default:
                        boolean var7 = var0.readUnsignedByte() == 1;
                        var7 &= var0.readUnsignedByte() == 250;
                        var7 &= "MC|PingHost".equals(new String(var0.readBytes(var0.readShort() * 2).array(), StandardCharsets.UTF_16BE));
                        int var8 = var0.readUnsignedShort();
                        var7 &= var0.readUnsignedByte() >= 73;
                        var7 &= 3 + var0.readBytes(var0.readShort() * 2).array().length + 4 == var8;
                        var7 &= var0.readInt() <= 65535;
                        var7 &= var0.readableBytes() == 0;
                        if (!var7) {
                            return;
                        }

                        LOGGER.debug("Ping: (1.6) from {}:{}", var2.getAddress(), var2.getPort());
                        String var9 = String.format(
                            "\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d",
                            127,
                            var3.getServerVersion(),
                            var3.getMotd(),
                            var3.getPlayerCount(),
                            var3.getMaxPlayers()
                        );
                        ByteBuf var10 = this.createReply(var9);

                        try {
                            this.sendFlushAndClose(param0, var10);
                        } finally {
                            var10.release();
                        }
                }

                var0.release();
                var1 = false;
                return;
            }
        } catch (RuntimeException var21) {
            return;
        } finally {
            if (var1) {
                var0.resetReaderIndex();
                param0.channel().pipeline().remove("legacy_query");
                param0.fireChannelRead(param1);
            }

        }

    }

    private void sendFlushAndClose(ChannelHandlerContext param0, ByteBuf param1) {
        param0.pipeline().firstContext().writeAndFlush(param1).addListener(ChannelFutureListener.CLOSE);
    }

    private ByteBuf createReply(String param0) {
        ByteBuf var0 = Unpooled.buffer();
        var0.writeByte(255);
        char[] var1 = param0.toCharArray();
        var0.writeShort(var1.length);

        for(char var2 : var1) {
            var0.writeChar(var2);
        }

        return var0;
    }
}
