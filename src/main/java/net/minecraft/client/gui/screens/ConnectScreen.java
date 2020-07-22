package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
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
    private static final Logger LOGGER = LogManager.getLogger();
    private Connection connection;
    private boolean aborted;
    private final Screen parent;
    private Component status = new TranslatableComponent("connect.connecting");
    private long lastNarration = -1L;

    public ConnectScreen(Screen param0, Minecraft param1, ServerData param2) {
        super(NarratorChatListener.NO_TITLE);
        this.minecraft = param1;
        this.parent = param0;
        ServerAddress var0 = ServerAddress.parseString(param2.ip);
        param1.clearLevel();
        param1.setCurrentServer(param2);
        this.connect(var0.getHost(), var0.getPort());
    }

    public ConnectScreen(Screen param0, Minecraft param1, String param2, int param3) {
        super(NarratorChatListener.NO_TITLE);
        this.minecraft = param1;
        this.parent = param0;
        param1.clearLevel();
        this.connect(param2, param3);
    }

    private void connect(final String param0, final int param1) {
        LOGGER.info("Connecting to {}, {}", param0, param1);
        Thread var0 = new Thread("Server Connector #" + UNIQUE_THREAD_ID.incrementAndGet()) {
            @Override
            public void run() {
                InetAddress var0 = null;

                try {
                    if (ConnectScreen.this.aborted) {
                        return;
                    }

                    var0 = InetAddress.getByName(param0);
                    ConnectScreen.this.connection = Connection.connectToServer(var0, param1, ConnectScreen.this.minecraft.options.useNativeTransport());
                    ConnectScreen.this.connection
                        .setListener(
                            new ClientHandshakePacketListenerImpl(
                                ConnectScreen.this.connection,
                                ConnectScreen.this.minecraft,
                                ConnectScreen.this.parent,
                                param1xx -> ConnectScreen.this.updateStatus(param1xx)
                            )
                        );
                    ConnectScreen.this.connection.send(new ClientIntentionPacket(param0, param1, ConnectionProtocol.LOGIN));
                    ConnectScreen.this.connection.send(new ServerboundHelloPacket(ConnectScreen.this.minecraft.getUser().getGameProfile()));
                } catch (UnknownHostException var4) {
                    if (ConnectScreen.this.aborted) {
                        return;
                    }

                    ConnectScreen.LOGGER.error("Couldn't connect to server", (Throwable)var4);
                    ConnectScreen.this.minecraft
                        .execute(
                            () -> ConnectScreen.this.minecraft
                                    .setScreen(
                                        new DisconnectedScreen(
                                            ConnectScreen.this.parent,
                                            CommonComponents.CONNECT_FAILED,
                                            new TranslatableComponent("disconnect.genericReason", "Unknown host")
                                        )
                                    )
                        );
                } catch (Exception var5) {
                    if (ConnectScreen.this.aborted) {
                        return;
                    }

                    ConnectScreen.LOGGER.error("Couldn't connect to server", (Throwable)var5);
                    String var3 = var0 == null ? var5.toString() : var5.toString().replaceAll(var0 + ":" + param1, "");
                    ConnectScreen.this.minecraft
                        .execute(
                            () -> ConnectScreen.this.minecraft
                                    .setScreen(
                                        new DisconnectedScreen(
                                            ConnectScreen.this.parent,
                                            CommonComponents.CONNECT_FAILED,
                                            new TranslatableComponent("disconnect.genericReason", var3)
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
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20, CommonComponents.GUI_CANCEL, param0 -> {
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
            NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("narrator.joining").getString());
        }

        drawCenteredString(param0, this.font, this.status, this.width / 2, this.height / 2 - 50, 16777215);
        super.render(param0, param1, param2, param3);
    }
}
