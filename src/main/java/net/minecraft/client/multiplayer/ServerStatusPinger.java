package net.minecraft.client.multiplayer;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import net.minecraft.network.protocol.status.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.ServerboundPingRequestPacket;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ServerStatusPinger {
    static final Splitter SPLITTER = Splitter.on('\u0000').limit(6);
    static final Logger LOGGER = LogManager.getLogger();
    private static final Component CANT_CONNECT_MESSAGE = new TranslatableComponent("multiplayer.status.cannot_connect").withStyle(ChatFormatting.DARK_RED);
    private final List<Connection> connections = Collections.synchronizedList(Lists.newArrayList());

    public void pingServer(final ServerData param0, final Runnable param1) throws UnknownHostException {
        ServerAddress var0 = ServerAddress.parseString(param0.ip);
        Optional<InetSocketAddress> var1 = ServerNameResolver.DEFAULT.resolveAddress(var0).map(ResolvedServerAddress::asInetSocketAddress);
        if (!var1.isPresent()) {
            this.onPingFailed(ConnectScreen.UNKNOWN_HOST_MESSAGE, param0);
        } else {
            final InetSocketAddress var2 = var1.get();
            final Connection var3 = Connection.connectToServer(var2, false);
            this.connections.add(var3);
            param0.motd = new TranslatableComponent("multiplayer.status.pinging");
            param0.ping = -1L;
            param0.playerList = null;
            var3.setListener(
                new ClientStatusPacketListener() {
                    private boolean success;
                    private boolean receivedPing;
                    private long pingStart;
    
                    @Override
                    public void handleStatusResponse(ClientboundStatusResponsePacket param0x) {
                        if (this.receivedPing) {
                            var3.disconnect(new TranslatableComponent("multiplayer.status.unrequested"));
                        } else {
                            this.receivedPing = true;
                            ServerStatus var0 = param0.getStatus();
                            if (var0.getDescription() != null) {
                                param0.motd = var0.getDescription();
                            } else {
                                param0.motd = TextComponent.EMPTY;
                            }
    
                            if (var0.getVersion() != null) {
                                param0.version = new TextComponent(var0.getVersion().getName());
                                param0.protocol = var0.getVersion().getProtocol();
                            } else {
                                param0.version = new TranslatableComponent("multiplayer.status.old");
                                param0.protocol = 0;
                            }
    
                            if (var0.getPlayers() != null) {
                                param0.status = ServerStatusPinger.formatPlayerCount(var0.getPlayers().getNumPlayers(), var0.getPlayers().getMaxPlayers());
                                List<Component> var1 = Lists.newArrayList();
                                if (ArrayUtils.isNotEmpty(var0.getPlayers().getSample())) {
                                    for(GameProfile var2 : var0.getPlayers().getSample()) {
                                        var1.add(new TextComponent(var2.getName()));
                                    }
    
                                    if (var0.getPlayers().getSample().length < var0.getPlayers().getNumPlayers()) {
                                        var1.add(
                                            new TranslatableComponent(
                                                "multiplayer.status.and_more", var0.getPlayers().getNumPlayers() - var0.getPlayers().getSample().length
                                            )
                                        );
                                    }
    
                                    param0.playerList = var1;
                                }
                            } else {
                                param0.status = new TranslatableComponent("multiplayer.status.unknown").withStyle(ChatFormatting.DARK_GRAY);
                            }
    
                            String var3 = null;
                            if (var0.getFavicon() != null) {
                                String var4 = var0.getFavicon();
                                if (var4.startsWith("data:image/png;base64,")) {
                                    var3 = var4.substring("data:image/png;base64,".length());
                                } else {
                                    ServerStatusPinger.LOGGER.error("Invalid server icon (unknown format)");
                                }
                            }
    
                            if (!Objects.equals(var3, param0.getIconB64())) {
                                param0.setIconB64(var3);
                                param1.run();
                            }
    
                            this.pingStart = Util.getMillis();
                            var3.send(new ServerboundPingRequestPacket(this.pingStart));
                            this.success = true;
                        }
                    }
    
                    @Override
                    public void handlePongResponse(ClientboundPongResponsePacket param0x) {
                        long var0 = this.pingStart;
                        long var1 = Util.getMillis();
                        param0.ping = var1 - var0;
                        var3.disconnect(new TranslatableComponent("multiplayer.status.finished"));
                    }
    
                    @Override
                    public void onDisconnect(Component param0x) {
                        if (!this.success) {
                            ServerStatusPinger.this.onPingFailed(param0, param0);
                            ServerStatusPinger.this.pingLegacyServer(var2, param0);
                        }
    
                    }
    
                    @Override
                    public Connection getConnection() {
                        return var3;
                    }
                }
            );

            try {
                var3.send(new ClientIntentionPacket(var0.getHost(), var0.getPort(), ConnectionProtocol.STATUS));
                var3.send(new ServerboundStatusRequestPacket());
            } catch (Throwable var8) {
                LOGGER.error(var8);
            }

        }
    }

    void onPingFailed(Component param0, ServerData param1) {
        LOGGER.error("Can't ping {}: {}", param1.ip, param0.getString());
        param1.motd = CANT_CONNECT_MESSAGE;
        param1.status = TextComponent.EMPTY;
    }

    void pingLegacyServer(final InetSocketAddress param0, final ServerData param1) {
        new Bootstrap().group(Connection.NETWORK_WORKER_GROUP.get()).handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel param0x) {
                try {
                    param0.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException var3) {
                }

                param0.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                    @Override
                    public void channelActive(ChannelHandlerContext param0x) throws Exception {
                        super.channelActive(param0);
                        ByteBuf var0 = Unpooled.buffer();

                        try {
                            var0.writeByte(254);
                            var0.writeByte(1);
                            var0.writeByte(250);
                            char[] var1 = "MC|PingHost".toCharArray();
                            var0.writeShort(var1.length);

                            for(char var2 : var1) {
                                var0.writeChar(var2);
                            }

                            var0.writeShort(7 + 2 * param0.getHostName().length());
                            var0.writeByte(127);
                            var1 = param0.getHostName().toCharArray();
                            var0.writeShort(var1.length);

                            for(char var3 : var1) {
                                var0.writeChar(var3);
                            }

                            var0.writeInt(param0.getPort());
                            param0.channel().writeAndFlush(var0).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                        } finally {
                            var0.release();
                        }

                    }

                    protected void channelRead0(ChannelHandlerContext param0x, ByteBuf param1x) {
                        short var0 = param1.readUnsignedByte();
                        if (var0 == 255) {
                            String var1 = new String(param1.readBytes(param1.readShort() * 2).array(), StandardCharsets.UTF_16BE);
                            String[] var2 = Iterables.toArray(ServerStatusPinger.SPLITTER.split(var1), String.class);
                            if ("\u00a71".equals(var2[0])) {
                                int var3 = Mth.getInt(var2[1], 0);
                                String var4 = var2[2];
                                String var5 = var2[3];
                                int var6 = Mth.getInt(var2[4], -1);
                                int var7 = Mth.getInt(var2[5], -1);
                                param1.protocol = -1;
                                param1.version = new TextComponent(var4);
                                param1.motd = new TextComponent(var5);
                                param1.status = ServerStatusPinger.formatPlayerCount(var6, var7);
                            }
                        }

                        param0.close();
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext param0x, Throwable param1x) {
                        param0.close();
                    }
                });
            }
        }).channel(NioSocketChannel.class).connect(param0.getAddress(), param0.getPort());
    }

    static Component formatPlayerCount(int param0, int param1) {
        return new TextComponent(Integer.toString(param0))
            .append(new TextComponent("/").withStyle(ChatFormatting.DARK_GRAY))
            .append(Integer.toString(param1))
            .withStyle(ChatFormatting.GRAY);
    }

    public void tick() {
        synchronized(this.connections) {
            Iterator<Connection> var0 = this.connections.iterator();

            while(var0.hasNext()) {
                Connection var1 = var0.next();
                if (var1.isConnected()) {
                    var1.tick();
                } else {
                    var0.remove();
                    var1.handleDisconnection();
                }
            }

        }
    }

    public void removeAll() {
        synchronized(this.connections) {
            Iterator<Connection> var0 = this.connections.iterator();

            while(var0.hasNext()) {
                Connection var1 = var0.next();
                if (var1.isConnected()) {
                    var0.remove();
                    var1.disconnect(new TranslatableComponent("multiplayer.status.cancelled"));
                }
            }

        }
    }
}
