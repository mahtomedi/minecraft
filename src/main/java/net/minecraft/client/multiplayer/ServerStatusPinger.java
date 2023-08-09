package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import net.minecraft.network.protocol.status.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.ServerboundPingRequestPacket;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ServerStatusPinger {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component CANT_CONNECT_MESSAGE = Component.translatable("multiplayer.status.cannot_connect")
        .withStyle(param0 -> param0.withColor(-65536));
    private final List<Connection> connections = Collections.synchronizedList(Lists.newArrayList());

    public void pingServer(final ServerData param0, final Runnable param1) throws UnknownHostException {
        final ServerAddress var0 = ServerAddress.parseString(param0.ip);
        Optional<InetSocketAddress> var1 = ServerNameResolver.DEFAULT.resolveAddress(var0).map(ResolvedServerAddress::asInetSocketAddress);
        if (var1.isEmpty()) {
            this.onPingFailed(ConnectScreen.UNKNOWN_HOST_MESSAGE, param0);
        } else {
            final InetSocketAddress var2 = var1.get();
            final Connection var3 = Connection.connectToServer(var2, false, null);
            this.connections.add(var3);
            param0.motd = Component.translatable("multiplayer.status.pinging");
            param0.ping = -1L;
            param0.playerList = Collections.emptyList();
            ClientStatusPacketListener var4 = new ClientStatusPacketListener() {
                private boolean success;
                private boolean receivedPing;
                private long pingStart;

                @Override
                public void handleStatusResponse(ClientboundStatusResponsePacket param0x) {
                    if (this.receivedPing) {
                        var3.disconnect(Component.translatable("multiplayer.status.unrequested"));
                    } else {
                        this.receivedPing = true;
                        ServerStatus var0 = param0.status();
                        param0.motd = var0.description();
                        var0.version().ifPresentOrElse(param1xx -> {
                            param0.version = Component.literal(param1xx.name());
                            param0.protocol = param1xx.protocol();
                        }, () -> {
                            param0.version = Component.translatable("multiplayer.status.old");
                            param0.protocol = 0;
                        });
                        var0.players().ifPresentOrElse(param1xx -> {
                            param0.status = ServerStatusPinger.formatPlayerCount(param1xx.online(), param1xx.max());
                            param0.players = param1xx;
                            if (!param1xx.sample().isEmpty()) {
                                List<Component> var0xxx = new ArrayList(param1xx.sample().size());

                                for(GameProfile var1x : param1xx.sample()) {
                                    var0xxx.add(Component.literal(var1x.getName()));
                                }

                                if (param1xx.sample().size() < param1xx.online()) {
                                    var0xxx.add(Component.translatable("multiplayer.status.and_more", param1xx.online() - param1xx.sample().size()));
                                }

                                param0.playerList = var0xxx;
                            } else {
                                param0.playerList = List.of();
                            }

                        }, () -> param0.status = Component.translatable("multiplayer.status.unknown").withStyle(ChatFormatting.DARK_GRAY));
                        var0.favicon().ifPresent(param2 -> {
                            if (!Arrays.equals(param2.iconBytes(), param0.getIconBytes())) {
                                param0.setIconBytes(param2.iconBytes());
                                param1.run();
                            }

                        });
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
                    var3.disconnect(Component.translatable("multiplayer.status.finished"));
                }

                @Override
                public void onDisconnect(Component param0x) {
                    if (!this.success) {
                        ServerStatusPinger.this.onPingFailed(param0, param0);
                        ServerStatusPinger.this.pingLegacyServer(var2, var0, param0);
                    }

                }

                @Override
                public boolean isAcceptingMessages() {
                    return var3.isConnected();
                }
            };

            try {
                var3.initiateServerboundStatusConnection(var0.getHost(), var0.getPort(), var4);
                var3.send(new ServerboundStatusRequestPacket());
            } catch (Throwable var9) {
                LOGGER.error("Failed to ping server {}", var0, var9);
            }

        }
    }

    void onPingFailed(Component param0, ServerData param1) {
        LOGGER.error("Can't ping {}: {}", param1.ip, param0.getString());
        param1.motd = CANT_CONNECT_MESSAGE;
        param1.status = CommonComponents.EMPTY;
    }

    void pingLegacyServer(InetSocketAddress param0, final ServerAddress param1, final ServerData param2) {
        new Bootstrap().group(Connection.NETWORK_WORKER_GROUP.get()).handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel param0) {
                try {
                    param0.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException var3) {
                }

                param0.pipeline().addLast(new LegacyServerPinger(param1, (param1xx, param2xx, param3, param4, param5) -> {
                    param2.protocol = -1;
                    param2.version = Component.literal(param2xx);
                    param2.motd = Component.literal(param3);
                    param2.status = ServerStatusPinger.formatPlayerCount(param4, param5);
                    param2.players = new ServerStatus.Players(param5, param4, List.of());
                }));
            }
        }).channel(NioSocketChannel.class).connect(param0.getAddress(), param0.getPort());
    }

    public static Component formatPlayerCount(int param0, int param1) {
        return Component.literal(Integer.toString(param0))
            .append(Component.literal("/").withStyle(ChatFormatting.DARK_GRAY))
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
                    var1.disconnect(Component.translatable("multiplayer.status.cancelled"));
                }
            }

        }
    }
}
