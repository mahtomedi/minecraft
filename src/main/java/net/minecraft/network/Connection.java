package net.minecraft.network;

import com.google.common.base.Suppliers;
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
import io.netty.handler.flow.FlowControlHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.util.Mth;
import net.minecraft.util.SampleLogger;
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
    public static final AttributeKey<ConnectionProtocol.CodecData<?>> ATTRIBUTE_SERVERBOUND_PROTOCOL = AttributeKey.valueOf("serverbound_protocol");
    public static final AttributeKey<ConnectionProtocol.CodecData<?>> ATTRIBUTE_CLIENTBOUND_PROTOCOL = AttributeKey.valueOf("clientbound_protocol");
    public static final Supplier<NioEventLoopGroup> NETWORK_WORKER_GROUP = Suppliers.memoize(
        () -> new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Client IO #%d").setDaemon(true).build())
    );
    public static final Supplier<EpollEventLoopGroup> NETWORK_EPOLL_WORKER_GROUP = Suppliers.memoize(
        () -> new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Epoll Client IO #%d").setDaemon(true).build())
    );
    public static final Supplier<DefaultEventLoopGroup> LOCAL_WORKER_GROUP = Suppliers.memoize(
        () -> new DefaultEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Local Client IO #%d").setDaemon(true).build())
    );
    private final PacketFlow receiving;
    private final Queue<Consumer<Connection>> pendingActions = Queues.newConcurrentLinkedQueue();
    private Channel channel;
    private SocketAddress address;
    @Nullable
    private volatile PacketListener disconnectListener;
    @Nullable
    private volatile PacketListener packetListener;
    @Nullable
    private Component disconnectedReason;
    private boolean encrypted;
    private boolean disconnectionHandled;
    private int receivedPackets;
    private int sentPackets;
    private float averageReceivedPackets;
    private float averageSentPackets;
    private int tickCount;
    private boolean handlingFault;
    @Nullable
    private volatile Component delayedDisconnect;
    @Nullable
    BandwidthDebugMonitor bandwidthDebugMonitor;

    public Connection(PacketFlow param0) {
        this.receiving = param0;
    }

    @Override
    public void channelActive(ChannelHandlerContext param0) throws Exception {
        super.channelActive(param0);
        this.channel = param0.channel();
        this.address = this.channel.remoteAddress();
        if (this.delayedDisconnect != null) {
            this.disconnect(this.delayedDisconnect);
        }

    }

    public static void setInitialProtocolAttributes(Channel param0) {
        param0.attr(ATTRIBUTE_SERVERBOUND_PROTOCOL).set(ConnectionProtocol.HANDSHAKING.codec(PacketFlow.SERVERBOUND));
        param0.attr(ATTRIBUTE_CLIENTBOUND_PROTOCOL).set(ConnectionProtocol.HANDSHAKING.codec(PacketFlow.CLIENTBOUND));
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
                        if (this.getSending() == PacketFlow.CLIENTBOUND) {
                            ConnectionProtocol var2 = this.channel.attr(ATTRIBUTE_CLIENTBOUND_PROTOCOL).get().protocol();
                            Packet<?> var3 = (Packet<?>)(var2 == ConnectionProtocol.LOGIN
                                ? new ClientboundLoginDisconnectPacket(var1)
                                : new ClientboundDisconnectPacket(var1));
                            this.send(var3, PacketSendListener.thenRun(() -> this.disconnect(var1)));
                        } else {
                            this.disconnect(var1);
                        }

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
            PacketListener var0 = this.packetListener;
            if (var0 == null) {
                throw new IllegalStateException("Received a packet before the packet listener was initialized");
            } else {
                if (var0.shouldHandleMessage(param1)) {
                    try {
                        genericsFtw(param1, var0);
                    } catch (RunningOnDifferentThreadException var5) {
                    } catch (RejectedExecutionException var6) {
                        this.disconnect(Component.translatable("multiplayer.disconnect.server_shutdown"));
                    } catch (ClassCastException var7) {
                        LOGGER.error("Received {} that couldn't be processed", param1.getClass(), var7);
                        this.disconnect(Component.translatable("multiplayer.disconnect.invalid_packet"));
                    }

                    ++this.receivedPackets;
                }

            }
        }
    }

    private static <T extends PacketListener> void genericsFtw(Packet<T> param0, PacketListener param1) {
        param0.handle((T)param1);
    }

    public void suspendInboundAfterProtocolChange() {
        this.channel.config().setAutoRead(false);
    }

    public void resumeInboundAfterProtocolChange() {
        this.channel.config().setAutoRead(true);
    }

    public void setListener(PacketListener param0) {
        Validate.notNull(param0, "packetListener");
        PacketFlow var0 = param0.flow();
        if (var0 != this.receiving) {
            throw new IllegalStateException("Trying to set listener for wrong side: connection is " + this.receiving + ", but listener is " + var0);
        } else {
            ConnectionProtocol var1 = param0.protocol();
            ConnectionProtocol var2 = this.channel.attr(getProtocolKey(var0)).get().protocol();
            if (var2 != var1) {
                throw new IllegalStateException("Trying to set listener for protocol " + var1.id() + ", but current " + var0 + " protocol is " + var2.id());
            } else {
                this.packetListener = param0;
                this.disconnectListener = null;
            }
        }
    }

    public void setListenerForServerboundHandshake(PacketListener param0) {
        if (this.packetListener != null) {
            throw new IllegalStateException("Listener already set");
        } else if (this.receiving == PacketFlow.SERVERBOUND && param0.flow() == PacketFlow.SERVERBOUND && param0.protocol() == ConnectionProtocol.HANDSHAKING) {
            this.packetListener = param0;
        } else {
            throw new IllegalStateException("Invalid initial listener");
        }
    }

    public void initiateServerboundStatusConnection(String param0, int param1, ClientStatusPacketListener param2) {
        this.initiateServerboundConnection(param0, param1, param2, ClientIntent.STATUS);
    }

    public void initiateServerboundPlayConnection(String param0, int param1, ClientLoginPacketListener param2) {
        this.initiateServerboundConnection(param0, param1, param2, ClientIntent.LOGIN);
    }

    private void initiateServerboundConnection(String param0, int param1, PacketListener param2, ClientIntent param3) {
        this.disconnectListener = param2;
        this.runOnceConnected(param4 -> {
            param4.setClientboundProtocolAfterHandshake(param3);
            this.setListener(param2);
            param4.sendPacket(new ClientIntentionPacket(SharedConstants.getCurrentVersion().getProtocolVersion(), param0, param1, param3), null, true);
        });
    }

    public void setClientboundProtocolAfterHandshake(ClientIntent param0) {
        this.channel.attr(ATTRIBUTE_CLIENTBOUND_PROTOCOL).set(param0.protocol().codec(PacketFlow.CLIENTBOUND));
    }

    public void send(Packet<?> param0) {
        this.send(param0, null);
    }

    public void send(Packet<?> param0, @Nullable PacketSendListener param1) {
        this.send(param0, param1, true);
    }

    public void send(Packet<?> param0, @Nullable PacketSendListener param1, boolean param2) {
        if (this.isConnected()) {
            this.flushQueue();
            this.sendPacket(param0, param1, param2);
        } else {
            this.pendingActions.add(param3 -> param3.sendPacket(param0, param1, param2));
        }

    }

    public void runOnceConnected(Consumer<Connection> param0) {
        if (this.isConnected()) {
            this.flushQueue();
            param0.accept(this);
        } else {
            this.pendingActions.add(param0);
        }

    }

    private void sendPacket(Packet<?> param0, @Nullable PacketSendListener param1, boolean param2) {
        ++this.sentPackets;
        if (this.channel.eventLoop().inEventLoop()) {
            this.doSendPacket(param0, param1, param2);
        } else {
            this.channel.eventLoop().execute(() -> this.doSendPacket(param0, param1, param2));
        }

    }

    private void doSendPacket(Packet<?> param0, @Nullable PacketSendListener param1, boolean param2) {
        ChannelFuture var0 = param2 ? this.channel.writeAndFlush(param0) : this.channel.write(param0);
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

    public void flushChannel() {
        if (this.isConnected()) {
            this.flush();
        } else {
            this.pendingActions.add(Connection::flush);
        }

    }

    private void flush() {
        if (this.channel.eventLoop().inEventLoop()) {
            this.channel.flush();
        } else {
            this.channel.eventLoop().execute(() -> this.channel.flush());
        }

    }

    private static AttributeKey<ConnectionProtocol.CodecData<?>> getProtocolKey(PacketFlow param0) {
        return switch(param0) {
            case CLIENTBOUND -> ATTRIBUTE_CLIENTBOUND_PROTOCOL;
            case SERVERBOUND -> ATTRIBUTE_SERVERBOUND_PROTOCOL;
        };
    }

    private void flushQueue() {
        if (this.channel != null && this.channel.isOpen()) {
            synchronized(this.pendingActions) {
                Consumer<Connection> var0;
                while((var0 = this.pendingActions.poll()) != null) {
                    var0.accept(this);
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

        if (this.bandwidthDebugMonitor != null) {
            this.bandwidthDebugMonitor.tick();
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

    public String getLoggableAddress(boolean param0) {
        if (this.address == null) {
            return "local";
        } else {
            return param0 ? this.address.toString() : "IP hidden";
        }
    }

    public void disconnect(Component param0) {
        if (this.channel == null) {
            this.delayedDisconnect = param0;
        }

        if (this.isConnected()) {
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

    public static Connection connectToServer(InetSocketAddress param0, boolean param1, @Nullable SampleLogger param2) {
        Connection var0 = new Connection(PacketFlow.CLIENTBOUND);
        if (param2 != null) {
            var0.setBandwidthLogger(param2);
        }

        ChannelFuture var1 = connect(param0, param1, var0);
        var1.syncUninterruptibly();
        return var0;
    }

    public static ChannelFuture connect(InetSocketAddress param0, boolean param1, final Connection param2) {
        Class<? extends SocketChannel> var0;
        EventLoopGroup var1;
        if (Epoll.isAvailable() && param1) {
            var0 = EpollSocketChannel.class;
            var1 = NETWORK_EPOLL_WORKER_GROUP.get();
        } else {
            var0 = NioSocketChannel.class;
            var1 = NETWORK_WORKER_GROUP.get();
        }

        return new Bootstrap().group(var1).handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel param0) {
                Connection.setInitialProtocolAttributes(param0);

                try {
                    param0.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException var3) {
                }

                ChannelPipeline var0 = param0.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
                Connection.configureSerialization(var0, PacketFlow.CLIENTBOUND, param2.bandwidthDebugMonitor);
                param2.configurePacketHandler(var0);
            }
        }).channel(var0).connect(param0.getAddress(), param0.getPort());
    }

    public static void configureSerialization(ChannelPipeline param0, PacketFlow param1, @Nullable BandwidthDebugMonitor param2) {
        PacketFlow var0 = param1.getOpposite();
        AttributeKey<ConnectionProtocol.CodecData<?>> var1 = getProtocolKey(param1);
        AttributeKey<ConnectionProtocol.CodecData<?>> var2 = getProtocolKey(var0);
        param0.addLast("splitter", new Varint21FrameDecoder(param2))
            .addLast("decoder", new PacketDecoder(var1))
            .addLast("prepender", new Varint21LengthFieldPrepender())
            .addLast("encoder", new PacketEncoder(var2))
            .addLast("unbundler", new PacketBundleUnpacker(var2))
            .addLast("bundler", new PacketBundlePacker(var1));
    }

    public void configurePacketHandler(ChannelPipeline param0) {
        param0.addLast(new FlowControlHandler()).addLast("packet_handler", this);
    }

    private static void configureInMemoryPacketValidation(ChannelPipeline param0, PacketFlow param1) {
        PacketFlow var0 = param1.getOpposite();
        AttributeKey<ConnectionProtocol.CodecData<?>> var1 = getProtocolKey(param1);
        AttributeKey<ConnectionProtocol.CodecData<?>> var2 = getProtocolKey(var0);
        param0.addLast("validator", new PacketFlowValidator(var1, var2));
    }

    public static void configureInMemoryPipeline(ChannelPipeline param0, PacketFlow param1) {
        configureInMemoryPacketValidation(param0, param1);
    }

    public static Connection connectToLocalServer(SocketAddress param0) {
        final Connection var0 = new Connection(PacketFlow.CLIENTBOUND);
        new Bootstrap().group(LOCAL_WORKER_GROUP.get()).handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel param0) {
                Connection.setInitialProtocolAttributes(param0);
                ChannelPipeline var0 = param0.pipeline();
                Connection.configureInMemoryPipeline(var0, PacketFlow.CLIENTBOUND);
                var0.configurePacketHandler(var0);
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

    @Nullable
    public PacketListener getPacketListener() {
        return this.packetListener;
    }

    @Nullable
    public Component getDisconnectedReason() {
        return this.disconnectedReason;
    }

    public void setReadOnly() {
        if (this.channel != null) {
            this.channel.config().setAutoRead(false);
        }

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
                PacketListener var0 = this.getPacketListener();
                PacketListener var1 = var0 != null ? var0 : this.disconnectListener;
                if (var1 != null) {
                    Component var2 = Objects.requireNonNullElseGet(this.getDisconnectedReason(), () -> Component.translatable("multiplayer.disconnect.generic"));
                    var1.onDisconnect(var2);
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

    public void setBandwidthLogger(SampleLogger param0) {
        this.bandwidthDebugMonitor = new BandwidthDebugMonitor(param0);
    }
}
