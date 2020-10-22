package net.minecraft.network;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
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
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Queue;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class Connection extends SimpleChannelInboundHandler<Packet<?>> {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Marker ROOT_MARKER = MarkerManager.getMarker("NETWORK");
    public static final Marker PACKET_MARKER = MarkerManager.getMarker("NETWORK_PACKETS", ROOT_MARKER);
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
            LOGGER.fatal(var3);
        }

    }

    public void setProtocol(ConnectionProtocol param0) {
        this.channel.attr(ATTRIBUTE_PROTOCOL).set(param0);
        this.channel.config().setAutoRead(true);
        LOGGER.debug("Enabled auto read");
    }

    @Override
    public void channelInactive(ChannelHandlerContext param0) throws Exception {
        this.disconnect(new TranslatableComponent("disconnect.endOfStream"));
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
                    this.disconnect(new TranslatableComponent("disconnect.timeout"));
                } else {
                    Component var1 = new TranslatableComponent("disconnect.genericReason", "Internal Exception: " + param1);
                    if (var0) {
                        LOGGER.debug("Failed to sent packet", param1);
                        this.send(new ClientboundDisconnectPacket(var1), param1x -> this.disconnect(var1));
                        this.setReadOnly();
                    } else {
                        LOGGER.debug("Double fault", param1);
                        this.disconnect(var1);
                    }
                }

            }
        }
    }

    protected void channelRead0(ChannelHandlerContext param0, Packet<?> param1) throws Exception {
        if (this.channel.isOpen()) {
            try {
                genericsFtw(param1, this.packetListener);
            } catch (RunningOnDifferentThreadException var4) {
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

    public void send(Packet<?> param0, @Nullable GenericFutureListener<? extends Future<? super Void>> param1) {
        if (this.isConnected()) {
            this.flushQueue();
            this.sendPacket(param0, param1);
        } else {
            this.queue.add(new Connection.PacketHolder(param0, param1));
        }

    }

    private void sendPacket(Packet<?> param0, @Nullable GenericFutureListener<? extends Future<? super Void>> param1) {
        ConnectionProtocol var0 = ConnectionProtocol.getProtocolForPacket(param0);
        ConnectionProtocol var1 = this.channel.attr(ATTRIBUTE_PROTOCOL).get();
        ++this.sentPackets;
        if (var1 != var0) {
            LOGGER.debug("Disabled auto read");
            this.channel.config().setAutoRead(false);
        }

        if (this.channel.eventLoop().inEventLoop()) {
            if (var0 != var1) {
                this.setProtocol(var0);
            }

            ChannelFuture var2 = this.channel.writeAndFlush(param0);
            if (param1 != null) {
                var2.addListener(param1);
            }

            var2.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        } else {
            this.channel.eventLoop().execute(() -> {
                if (var0 != var1) {
                    this.setProtocol(var0);
                }

                ChannelFuture var0x = this.channel.writeAndFlush(param0);
                if (param1 != null) {
                    var0x.addListener(param1);
                }

                var0x.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            });
        }

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
        if (this.packetListener instanceof ServerLoginPacketListenerImpl) {
            ((ServerLoginPacketListenerImpl)this.packetListener).tick();
        }

        if (this.packetListener instanceof ServerGamePacketListenerImpl) {
            ((ServerGamePacketListenerImpl)this.packetListener).tick();
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

    @OnlyIn(Dist.CLIENT)
    public static Connection connectToServer(InetAddress param0, int param1, boolean param2) {
        final Connection var0 = new Connection(PacketFlow.CLIENTBOUND);
        Class<? extends SocketChannel> var1;
        LazyLoadedValue<? extends EventLoopGroup> var2;
        if (Epoll.isAvailable() && param2) {
            var1 = EpollSocketChannel.class;
            var2 = NETWORK_EPOLL_WORKER_GROUP;
        } else {
            var1 = NioSocketChannel.class;
            var2 = NETWORK_WORKER_GROUP;
        }

        new Bootstrap()
            .group(var2.get())
            .handler(
                new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel param0) throws Exception {
                        try {
                            param0.config().setOption(ChannelOption.TCP_NODELAY, true);
                        } catch (ChannelException var3) {
                        }
        
                        param0.pipeline()
                            .addLast("timeout", new ReadTimeoutHandler(30))
                            .addLast("splitter", new Varint21FrameDecoder())
                            .addLast("decoder", new PacketDecoder(PacketFlow.CLIENTBOUND))
                            .addLast("prepender", new Varint21LengthFieldPrepender())
                            .addLast("encoder", new PacketEncoder(PacketFlow.SERVERBOUND))
                            .addLast("packet_handler", var0);
                    }
                }
            )
            .channel(var1)
            .connect(param0, param1)
            .syncUninterruptibly();
        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    public static Connection connectToLocalServer(SocketAddress param0) {
        final Connection var0 = new Connection(PacketFlow.CLIENTBOUND);
        new Bootstrap().group(LOCAL_WORKER_GROUP.get()).handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel param0) throws Exception {
                param0.pipeline().addLast("packet_handler", var0);
            }
        }).channel(LocalChannel.class).connect(param0).syncUninterruptibly();
        return var0;
    }

    public void setEncryptionKey(Cipher param0, Cipher param1) {
        this.encrypted = true;
        this.channel.pipeline().addBefore("splitter", "decrypt", new CipherDecoder(param0));
        this.channel.pipeline().addBefore("prepender", "encrypt", new CipherEncoder(param1));
    }

    @OnlyIn(Dist.CLIENT)
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

    public void setupCompression(int param0) {
        if (param0 >= 0) {
            if (this.channel.pipeline().get("decompress") instanceof CompressionDecoder) {
                ((CompressionDecoder)this.channel.pipeline().get("decompress")).setThreshold(param0);
            } else {
                this.channel.pipeline().addBefore("decoder", "decompress", new CompressionDecoder(param0));
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
                    this.getPacketListener().onDisconnect(new TranslatableComponent("multiplayer.disconnect.generic"));
                }
            }

        }
    }

    public float getAverageReceivedPackets() {
        return this.averageReceivedPackets;
    }

    @OnlyIn(Dist.CLIENT)
    public float getAverageSentPackets() {
        return this.averageSentPackets;
    }

    static class PacketHolder {
        private final Packet<?> packet;
        @Nullable
        private final GenericFutureListener<? extends Future<? super Void>> listener;

        public PacketHolder(Packet<?> param0, @Nullable GenericFutureListener<? extends Future<? super Void>> param1) {
            this.packet = param0;
            this.listener = param1;
        }
    }
}
