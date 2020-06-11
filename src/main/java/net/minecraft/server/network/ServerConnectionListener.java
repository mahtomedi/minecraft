package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.Varint21FrameDecoder;
import net.minecraft.network.Varint21LengthFieldPrepender;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.LazyLoadedValue;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerConnectionListener {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final LazyLoadedValue<NioEventLoopGroup> SERVER_EVENT_GROUP = new LazyLoadedValue<>(
        () -> new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Server IO #%d").setDaemon(true).build())
    );
    public static final LazyLoadedValue<EpollEventLoopGroup> SERVER_EPOLL_EVENT_GROUP = new LazyLoadedValue<>(
        () -> new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Epoll Server IO #%d").setDaemon(true).build())
    );
    private final MinecraftServer server;
    public volatile boolean running;
    private final List<ChannelFuture> channels = Collections.synchronizedList(Lists.newArrayList());
    private final List<Connection> connections = Collections.synchronizedList(Lists.newArrayList());

    public ServerConnectionListener(MinecraftServer param0) {
        this.server = param0;
        this.running = true;
    }

    public void startTcpServerListener(@Nullable InetAddress param0, int param1) throws IOException {
        synchronized(this.channels) {
            Class<? extends ServerSocketChannel> var0;
            LazyLoadedValue<? extends EventLoopGroup> var1;
            if (Epoll.isAvailable() && this.server.isEpollEnabled()) {
                var0 = EpollServerSocketChannel.class;
                var1 = SERVER_EPOLL_EVENT_GROUP;
                LOGGER.info("Using epoll channel type");
            } else {
                var0 = NioServerSocketChannel.class;
                var1 = SERVER_EVENT_GROUP;
                LOGGER.info("Using default channel type");
            }

            this.channels
                .add(
                    new ServerBootstrap()
                        .channel(var0)
                        .childHandler(
                            new ChannelInitializer<Channel>() {
                                @Override
                                protected void initChannel(Channel param0) throws Exception {
                                    try {
                                        param0.config().setOption(ChannelOption.TCP_NODELAY, true);
                                    } catch (ChannelException var3) {
                                    }
                
                                    param0.pipeline()
                                        .addLast("timeout", new ReadTimeoutHandler(30))
                                        .addLast("legacy_query", new LegacyQueryHandler(ServerConnectionListener.this))
                                        .addLast("splitter", new Varint21FrameDecoder())
                                        .addLast("decoder", new PacketDecoder(PacketFlow.SERVERBOUND))
                                        .addLast("prepender", new Varint21LengthFieldPrepender())
                                        .addLast("encoder", new PacketEncoder(PacketFlow.CLIENTBOUND));
                                    Connection var0 = new Connection(PacketFlow.SERVERBOUND);
                                    ServerConnectionListener.this.connections.add(var0);
                                    param0.pipeline().addLast("packet_handler", var0);
                                    var0.setListener(new ServerHandshakePacketListenerImpl(ServerConnectionListener.this.server, var0));
                                }
                            }
                        )
                        .group(var1.get())
                        .localAddress(param0, param1)
                        .bind()
                        .syncUninterruptibly()
                );
        }
    }

    @OnlyIn(Dist.CLIENT)
    public SocketAddress startMemoryChannel() {
        ChannelFuture var0;
        synchronized(this.channels) {
            var0 = new ServerBootstrap().channel(LocalServerChannel.class).childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel param0) throws Exception {
                    Connection var0 = new Connection(PacketFlow.SERVERBOUND);
                    var0.setListener(new MemoryServerHandshakePacketListenerImpl(ServerConnectionListener.this.server, var0));
                    ServerConnectionListener.this.connections.add(var0);
                    param0.pipeline().addLast("packet_handler", var0);
                }
            }).group(SERVER_EVENT_GROUP.get()).localAddress(LocalAddress.ANY).bind().syncUninterruptibly();
            this.channels.add(var0);
        }

        return var0.channel().localAddress();
    }

    public void stop() {
        this.running = false;

        for(ChannelFuture var0 : this.channels) {
            try {
                var0.channel().close().sync();
            } catch (InterruptedException var4) {
                LOGGER.error("Interrupted whilst closing channel");
            }
        }

    }

    public void tick() {
        synchronized(this.connections) {
            Iterator<Connection> var0 = this.connections.iterator();

            while(var0.hasNext()) {
                Connection var1 = var0.next();
                if (!var1.isConnecting()) {
                    if (var1.isConnected()) {
                        try {
                            var1.tick();
                        } catch (Exception var7) {
                            if (var1.isMemoryConnection()) {
                                throw new ReportedException(CrashReport.forThrowable(var7, "Ticking memory connection"));
                            }

                            LOGGER.warn("Failed to handle packet for {}", var1.getRemoteAddress(), var7);
                            Component var3 = new TextComponent("Internal server error");
                            var1.send(new ClientboundDisconnectPacket(var3), param2 -> var1.disconnect(var3));
                            var1.setReadOnly();
                        }
                    } else {
                        var0.remove();
                        var1.handleDisconnection();
                    }
                }
            }

        }
    }

    public MinecraftServer getServer() {
        return this.server;
    }
}
