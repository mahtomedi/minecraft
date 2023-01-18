package net.minecraft.network;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class Connection extends SimpleChannelInboundHandler<Packet<?>> {
    private static final float AVERAGE_PACKETS_SMOOTHING = 0.75F;
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Marker ROOT_MARKER = MarkerFactory.getMarker("NETWORK");
    public static final Marker PACKET_MARKER = Util.make(MarkerFactory.getMarker("NETWORK_PACKETS"), param0 -> param0.add(ROOT_MARKER));
    public static final Marker PACKET_RECEIVED_MARKER = Util.make(MarkerFactory.getMarker("PACKET_RECEIVED"), param0 -> param0.add(PACKET_MARKER));
    public static final Marker PACKET_SENT_MARKER = Util.make(MarkerFactory.getMarker("PACKET_SENT"), param0 -> param0.add(PACKET_MARKER));
    public static final AttributeKey<ConnectionProtocol> ATTRIBUTE_PROTOCOL = AttributeKey.valueOf("protocol");
    public static final LazyLoadedValue<NioEventLoopGroup> NETWORK_WORKER_GROUP = new LazyLoadedValue<>(
        () -> new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Client IO #%d").setDaemon(true).build())
    );
    public static final LazyLoadedValue<EpollEventLoopGroup> NETWORK_EPOLL_WORKER_GROUP = new LazyLoadedValue<>(
        () -> new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Epoll Client IO #%d").setDaemon(true).build())
    );
    public static final LazyLoadedValue<DefaultEventLoopGroup> LOCAL_WORKER_GROUP = new LazyLoadedValue<>(
        () -> new DefaultEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Local Client IO #%d").setDaemon(true).build())
    );
    private final PacketFlow receiving;
    private final Queue<Connection.PacketHolder> queue = Queues.newConcurrentLinkedQueue();
    private Channel channel;
    private SocketAddress address;
    private PacketListener packetListener;
    private Component disconnectedReason;
    private boolean encrypted;
    private boolean disconnectionHandled;
    private int receivedPackets;
    private int sentPackets;
    private float averageReceivedPackets;
    private float averageSentPackets;
    private int tickCount;
    private boolean handlingFault;

    public Connection(PacketFlow param0) {
        this.receiving = param0;
    }

    @Override
    public void channelActive(ChannelHandlerContext param0) throws Exception {
        super.channelActive(param0);
        this.channel = param0.channel();
        this.address = this.channel.remoteAddress();

        try {
            this.setProtocol(ConnectionProtocol.HANDSHAKING);
        } catch (Throwable var3) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Failed to change protocol to handshake", var3);
        }

    }

    public void setProtocol(ConnectionProtocol param0) {
        this.channel.attr(ATTRIBUTE_PROTOCOL).set(param0);
        this.channel.attr(BundlerInfo.BUNDLER_PROVIDER).set(param0);
        this.channel.config().setAutoRead(true);
        LOGGER.debug("Enabled auto read");
    }

    @Override
    public void channelInactive(ChannelHandlerContext param0) {
        this.disconnect(Component.translatable("disconnect.endOfStream"));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext param0, Throwable param1) {
        if (param1 instanceof SkipPacketException) {
            LOGGER.debug("Skipping packet due to errors", param1.getCause());
        } else {
            boolean var0 = !this.handlingFault;
            this.handlingFault = true;
            if (this.channel.isOpen()) {
                if (param1 instanceof TimeoutException) {
                    LOGGER.debug("Timeout", param1);
                    this.disconnect(Component.translatable("disconnect.timeout"));
                } else {
                    Component var1 = Component.translatable("disconnect.genericReason", "Internal Exception: " + param1);
                    if (var0) {
                        LOGGER.debug("Failed to sent packet", param1);
                        ConnectionProtocol var2 = this.getCurrentProtocol();
                        Packet<?> var3 = (Packet<?>)(var2 == ConnectionProtocol.LOGIN
                            ? new ClientboundLoginDisconnectPacket(var1)
                            : new ClientboundDisconnectPacket(var1));
                        this.send(var3, PacketSendListener.thenRun(() -> this.disconnect(var1)));
                        this.setReadOnly();
                    } else {
                        LOGGER.debug("Double fault", param1);
                        this.disconnect(var1);
                    }
                }

            }
        }
    }

    protected void channelRead0(ChannelHandlerContext param0, Packet<?> param1) {
        if (this.channel.isOpen()) {
            try {
                genericsFtw(param1, this.packetListener);
            } catch (RunningOnDifferentThreadException var4) {
            } catch (RejectedExecutionException var5) {
                this.disconnect(Component.translatable("multiplayer.disconnect.server_shutdown"));
            } catch (ClassCastException var6) {
                LOGGER.error("Received {} that couldn't be processed", param1.getClass(), var6);
                this.disconnect(Component.translatable("multiplayer.disconnect.invalid_packet"));
            }

            ++this.receivedPackets;
        }

    }

    private static <T extends PacketListener> void genericsFtw(Packet<T> param0, PacketListener param1) {
        param0.handle((T)param1);
    }

    public void setListener(PacketListener param0) {
        Validate.notNull(param0, "packetListener");
        this.packetListener = param0;
    }

    public void send(Packet<?> param0) {
        this.send(param0, null);
    }

    public void send(Packet<?> param0, @Nullable PacketSendListener param1) {
        if (this.isConnected()) {
            this.flushQueue();
            this.sendPacket(param0, param1);
        } else {
            this.queue.add(new Connection.PacketHolder(param0, param1));
        }

    }

    private void sendPacket(Packet<?> param0, @Nullable PacketSendListener param1) {
        ConnectionProtocol var0 = ConnectionProtocol.getProtocolForPacket(param0);
        ConnectionProtocol var1 = this.getCurrentProtocol();
        ++this.sentPackets;
        if (var1 != var0) {
            if (var0 == null) {
                throw new IllegalStateException("Encountered packet without set protocol: " + param0);
            }

            LOGGER.debug("Disabled auto read");
            this.channel.config().setAutoRead(false);
        }

        if (this.channel.eventLoop().inEventLoop()) {
            this.doSendPacket(param0, param1, var0, var1);
        } else {
            this.channel.eventLoop().execute(() -> this.doSendPacket(param0, param1, var0, var1));
        }

    }

    private void doSendPacket(Packet<?> param0, @Nullable PacketSendListener param1, ConnectionProtocol param2, ConnectionProtocol param3) {
        if (param2 != param3) {
            this.setProtocol(param2);
        }

        ChannelFuture var0 = this.channel.writeAndFlush(param0);
        if (param1 != null) {
            var0.addListener(param1x -> {
                if (param1x.isSuccess()) {
                    param1.onSuccess();
                } else {
                    Packet<?> var0x = param1.onFailure();
                    if (var0x != null) {
                        ChannelFuture var1x = this.channel.writeAndFlush(var0x);
                        var1x.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                    }
                }

            });
        }

        var0.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    private ConnectionProtocol getCurrentProtocol() {
        return this.channel.attr(ATTRIBUTE_PROTOCOL).get();
    }

    private void flushQueue() {
        if (this.channel != null && this.channel.isOpen()) {
            synchronized(this.queue) {
                Connection.PacketHolder var0;
                while((var0 = this.queue.poll()) != null) {
                    this.sendPacket(var0.packet, var0.listener);
                }

            }
        }
    }

    public void tick() {
        this.flushQueue();
        PacketListener var2 = this.packetListener;
        if (var2 instanceof TickablePacketListener var0) {
            var0.tick();
        }

        if (!this.isConnected() && !this.disconnectionHandled) {
            this.handleDisconnection();
        }

        if (this.channel != null) {
            this.channel.flush();
        }

        if (this.tickCount++ % 20 == 0) {
            this.tickSecond();
        }

    }

    protected void tickSecond() {
        this.averageSentPackets = Mth.lerp(0.75F, (float)this.sentPackets, this.averageSentPackets);
        this.averageReceivedPackets = Mth.lerp(0.75F, (float)this.receivedPackets, this.averageReceivedPackets);
        this.sentPackets = 0;
        this.receivedPackets = 0;
    }

    public SocketAddress getRemoteAddress() {
        return this.address;
    }

    public void disconnect(Component param0) {
        if (this.channel.isOpen()) {
            this.channel.close().awaitUninterruptibly();
            this.disconnectedReason = param0;
        }

    }

    public boolean isMemoryConnection() {
        return this.channel instanceof LocalChannel || this.channel instanceof LocalServerChannel;
    }

    public PacketFlow getReceiving() {
        return this.receiving;
    }

    public PacketFlow getSending() {
        return this.receiving.getOpposite();
    }

    public static Connection connectToServer(InetSocketAddress param0, boolean param1) {
        final Connection var0 = new Connection(PacketFlow.CLIENTBOUND);
        Class<? extends SocketChannel> var1;
        LazyLoadedValue<? extends EventLoopGroup> var2;
        if (Epoll.isAvailable() && param1) {
            var1 = EpollSocketChannel.class;
            var2 = NETWORK_EPOLL_WORKER_GROUP;
        } else {
            var1 = NioSocketChannel.class;
            var2 = NETWORK_WORKER_GROUP;
        }

        new Bootstrap().group(var2.get()).handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel param0) {
                try {
                    param0.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException var3) {
                }

                ChannelPipeline var0 = param0.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
                Connection.configureSerialization(var0, PacketFlow.CLIENTBOUND);
                var0.addLast("packet_handler", var0);
            }
        }).channel(var1).connect(param0.getAddress(), param0.getPort()).syncUninterruptibly();
        return var0;
    }

    public static void configureSerialization(ChannelPipeline param0, PacketFlow param1) {
        PacketFlow var0 = param1.getOpposite();
        param0.addLast("splitter", new Varint21FrameDecoder())
            .addLast("decoder", new PacketDecoder(param1))
            .addLast("prepender", new Varint21LengthFieldPrepender())
            .addLast("encoder", new PacketEncoder(var0))
            .addLast("unbundler", new PacketBundleUnpacker(var0))
            .addLast("bundler", new PacketBundlePacker(param1));
    }

    public static Connection connectToLocalServer(SocketAddress param0) {
        final Connection var0 = new Connection(PacketFlow.CLIENTBOUND);
        new Bootstrap().group(LOCAL_WORKER_GROUP.get()).handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel param0) {
                ChannelPipeline var0 = param0.pipeline();
                var0.addLast("packet_handler", var0);
            }
        }).channel(LocalChannel.class).connect(param0).syncUninterruptibly();
        return var0;
    }

    public void setEncryptionKey(Cipher param0, Cipher param1) {
        this.encrypted = true;
        this.channel.pipeline().addBefore("splitter", "decrypt", new CipherDecoder(param0));
        this.channel.pipeline().addBefore("prepender", "encrypt", new CipherEncoder(param1));
    }

    public boolean isEncrypted() {
        return this.encrypted;
    }

    public boolean isConnected() {
        return this.channel != null && this.channel.isOpen();
    }

    public boolean isConnecting() {
        return this.channel == null;
    }

    public PacketListener getPacketListener() {
        return this.packetListener;
    }

    @Nullable
    public Component getDisconnectedReason() {
        return this.disconnectedReason;
    }

    public void setReadOnly() {
        this.channel.config().setAutoRead(false);
    }

    public void setupCompression(int param0, boolean param1) {
        if (param0 >= 0) {
            if (this.channel.pipeline().get("decompress") instanceof CompressionDecoder) {
                ((CompressionDecoder)this.channel.pipeline().get("decompress")).setThreshold(param0, param1);
            } else {
                this.channel.pipeline().addBefore("decoder", "decompress", new CompressionDecoder(param0, param1));
            }

            if (this.channel.pipeline().get("compress") instanceof CompressionEncoder) {
                ((CompressionEncoder)this.channel.pipeline().get("compress")).setThreshold(param0);
            } else {
                this.channel.pipeline().addBefore("encoder", "compress", new CompressionEncoder(param0));
            }
        } else {
            if (this.channel.pipeline().get("decompress") instanceof CompressionDecoder) {
                this.channel.pipeline().remove("decompress");
            }

            if (this.channel.pipeline().get("compress") instanceof CompressionEncoder) {
                this.channel.pipeline().remove("compress");
            }
        }

    }

    public void handleDisconnection() {
        if (this.channel != null && !this.channel.isOpen()) {
            if (this.disconnectionHandled) {
                LOGGER.warn("handleDisconnection() called twice");
            } else {
                this.disconnectionHandled = true;
                if (this.getDisconnectedReason() != null) {
                    this.getPacketListener().onDisconnect(this.getDisconnectedReason());
                } else if (this.getPacketListener() != null) {
                    this.getPacketListener().onDisconnect(Component.translatable("multiplayer.disconnect.generic"));
                }
            }

        }
    }

    public float getAverageReceivedPackets() {
        return this.averageReceivedPackets;
    }

    public float getAverageSentPackets() {
        return this.averageSentPackets;
    }

    static class PacketHolder {
        final Packet<?> packet;
        @Nullable
        final PacketSendListener listener;

        public PacketHolder(Packet<?> param0, @Nullable PacketSendListener param1) {
            this.packet = param0;
            this.listener = param1;
        }
    }
}
