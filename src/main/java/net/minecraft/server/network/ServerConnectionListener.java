package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
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
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.RateKickingConnection;
import net.minecraft.network.Varint21FrameDecoder;
import net.minecraft.network.Varint21LengthFieldPrepender;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.LazyLoadedValue;
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
                                protected void initChannel(Channel param0) {
                                    try {
                                        param0.config().setOption(ChannelOption.TCP_NODELAY, true);
                                    } catch (ChannelException var4) {
                                    }
                
                                    param0.pipeline()
                                        .addLast("timeout", new ReadTimeoutHandler(30))
                                        .addLast("legacy_query", new LegacyQueryHandler(ServerConnectionListener.this))
                                        .addLast("splitter", new Varint21FrameDecoder())
                                        .addLast("decoder", new PacketDecoder(PacketFlow.SERVERBOUND))
                                        .addLast("prepender", new Varint21LengthFieldPrepender())
                                        .addLast("encoder", new PacketEncoder(PacketFlow.CLIENTBOUND));
                                    int var0 = ServerConnectionListener.this.server.getRateLimitPacketsPerSecond();
                                    Connection var1 = (Connection)(var0 > 0 ? new RateKickingConnection(var0) : new Connection(PacketFlow.SERVERBOUND));
                                    ServerConnectionListener.this.connections.add(var1);
                                    param0.pipeline().addLast("packet_handler", var1);
                                    var1.setListener(new ServerHandshakePacketListenerImpl(ServerConnectionListener.this.server, var1));
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

    public SocketAddress startMemoryChannel() {
        ChannelFuture var0;
        synchronized(this.channels) {
            var0 = new ServerBootstrap().channel(LocalServerChannel.class).childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel param0) {
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

    static class LatencySimulator extends ChannelInboundHandlerAdapter {
        private static final Timer TIMER = new HashedWheelTimer();
        private final int delay;
        private final int jitter;
        private final List<ServerConnectionListener.LatencySimulator.DelayedMessage> queuedMessages = Lists.newArrayList();

        public LatencySimulator(int param0, int param1) {
            this.delay = param0;
            this.jitter = param1;
        }

        @Override
        public void channelRead(ChannelHandlerContext param0, Object param1) {
            this.delayDownstream(param0, param1);
        }

        private void delayDownstream(ChannelHandlerContext param0, Object param1) {
            int var0 = this.delay + (int)(Math.random() * (double)this.jitter);
            this.queuedMessages.add(new ServerConnectionListener.LatencySimulator.DelayedMessage(param0, param1));
            TIMER.newTimeout(this::onTimeout, (long)var0, TimeUnit.MILLISECONDS);
        }

        private void onTimeout(Timeout param0x) {
            ServerConnectionListener.LatencySimulator.DelayedMessage var0x = this.queuedMessages.remove(0);
            var0x.ctx.fireChannelRead(var0x.msg);
        }

        static class DelayedMessage {
            public final ChannelHandlerContext ctx;
            public final Object msg;

            public DelayedMessage(ChannelHandlerContext param0, Object param1) {
                this.ctx = param0;
                this.msg = param1;
            }
        }
    }
}
