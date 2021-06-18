package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ConnectScreen extends Screen {
    private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    static final Logger LOGGER = LogManager.getLogger();
    private static final long NARRATION_DELAY_MS = 2000L;
    public static final Component UNKNOWN_HOST_MESSAGE = new TranslatableComponent(
        "disconnect.genericReason", new TranslatableComponent("disconnect.unknownHost")
    );
    @Nullable
    volatile Connection connection;
    volatile boolean aborted;
    final Screen parent;
    private Component status = new TranslatableComponent("connect.connecting");
    private long lastNarration = -1L;

    private ConnectScreen(Screen param0) {
        super(NarratorChatListener.NO_TITLE);
        this.parent = param0;
    }

    public static void startConnecting(Screen param0, Minecraft param1, ServerAddress param2, @Nullable ServerData param3) {
        ConnectScreen var0 = new ConnectScreen(param0);
        param1.clearLevel();
        param1.setCurrentServer(param3);
        param1.setScreen(var0);
        var0.connect(param1, param2);
    }

    private void connect(final Minecraft param0, final ServerAddress param1) {
        LOGGER.info("Connecting to {}, {}", param1.getHost(), param1.getPort());
        Thread var0 = new Thread("Server Connector #" + UNIQUE_THREAD_ID.incrementAndGet()) {
            @Override
            public void run() {
                InetSocketAddress var0 = null;

                try {
                    if (ConnectScreen.this.aborted) {
                        return;
                    }

                    Optional<InetSocketAddress> var1 = ServerNameResolver.DEFAULT.resolveAddress(param1).map(ResolvedServerAddress::asInetSocketAddress);
                    if (ConnectScreen.this.aborted) {
                        return;
                    }

                    if (!var1.isPresent()) {
                        param0.execute(
                            () -> param0.setScreen(
                                    new DisconnectedScreen(ConnectScreen.this.parent, CommonComponents.CONNECT_FAILED, ConnectScreen.UNKNOWN_HOST_MESSAGE)
                                )
                        );
                        return;
                    }

                    var0 = var1.get();
                    ConnectScreen.this.connection = Connection.connectToServer(var0, param0.options.useNativeTransport());
                    ConnectScreen.this.connection
                        .setListener(
                            new ClientHandshakePacketListenerImpl(
                                ConnectScreen.this.connection, param0, ConnectScreen.this.parent, ConnectScreen.this::updateStatus
                            )
                        );
                    ConnectScreen.this.connection.send(new ClientIntentionPacket(var0.getHostName(), var0.getPort(), ConnectionProtocol.LOGIN));
                    ConnectScreen.this.connection.send(new ServerboundHelloPacket(param0.getUser().getGameProfile()));
                } catch (Exception var4) {
                    if (ConnectScreen.this.aborted) {
                        return;
                    }

                    ConnectScreen.LOGGER.error("Couldn't connect to server", (Throwable)var4);
                    String var3 = var0 == null ? var4.toString() : var4.toString().replaceAll(var0.getHostName() + ":" + var0.getPort(), "");
                    param0.execute(
                        () -> param0.setScreen(
                                new DisconnectedScreen(
                                    ConnectScreen.this.parent, CommonComponents.CONNECT_FAILED, new TranslatableComponent("disconnect.genericReason", var3)
                                )
                            )
                    );
                }

            }
        };
        var0.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        var0.start();
    }

    private void updateStatus(Component param0) {
        this.status = param0;
    }

    @Override
    public void tick() {
        if (this.connection != null) {
            if (this.connection.isConnected()) {
                this.connection.tick();
            } else {
                this.connection.handleDisconnection();
            }
        }

    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20, CommonComponents.GUI_CANCEL, param0 -> {
            this.aborted = true;
            if (this.connection != null) {
                this.connection.disconnect(new TranslatableComponent("connect.aborted"));
            }

            this.minecraft.setScreen(this.parent);
        }));
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        long var0 = Util.getMillis();
        if (var0 - this.lastNarration > 2000L) {
            this.lastNarration = var0;
            NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("narrator.joining"));
        }

        drawCenteredString(param0, this.font, this.status, this.width / 2, this.height / 2 - 50, 16777215);
        super.render(param0, param1, param2, param3);
    }
}
