package net.minecraft.realms;

import java.net.InetAddress;
import java.net.UnknownHostException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsConnect {
    private static final Logger LOGGER = LogManager.getLogger();
    private final RealmsScreen onlineScreen;
    private volatile boolean aborted;
    private Connection connection;

    public RealmsConnect(RealmsScreen param0) {
        this.onlineScreen = param0;
    }

    public void connect(final String param0, final int param1) {
        Realms.setConnectedToRealms(true);
        Realms.narrateNow(Realms.getLocalizedString("mco.connect.success"));
        (new Thread("Realms-connect-task") {
                @Override
                public void run() {
                    InetAddress var0 = null;
    
                    try {
                        var0 = InetAddress.getByName(param0);
                        if (RealmsConnect.this.aborted) {
                            return;
                        }
    
                        RealmsConnect.this.connection = Connection.connectToServer(var0, param1, Minecraft.getInstance().options.useNativeTransport());
                        if (RealmsConnect.this.aborted) {
                            return;
                        }
    
                        RealmsConnect.this.connection
                            .setListener(
                                new ClientHandshakePacketListenerImpl(
                                    RealmsConnect.this.connection, Minecraft.getInstance(), RealmsConnect.this.onlineScreen.getProxy(), param0xx -> {
                                    }
                                )
                            );
                        if (RealmsConnect.this.aborted) {
                            return;
                        }
    
                        RealmsConnect.this.connection.send(new ClientIntentionPacket(param0, param1, ConnectionProtocol.LOGIN));
                        if (RealmsConnect.this.aborted) {
                            return;
                        }
    
                        RealmsConnect.this.connection.send(new ServerboundHelloPacket(Minecraft.getInstance().getUser().getGameProfile()));
                    } catch (UnknownHostException var5) {
                        Realms.clearResourcePack();
                        if (RealmsConnect.this.aborted) {
                            return;
                        }
    
                        RealmsConnect.LOGGER.error("Couldn't connect to world", (Throwable)var5);
                        Realms.setScreen(
                            new DisconnectedRealmsScreen(
                                RealmsConnect.this.onlineScreen,
                                "connect.failed",
                                new TranslatableComponent("disconnect.genericReason", "Unknown host '" + param0 + "'")
                            )
                        );
                    } catch (Exception var6) {
                        Realms.clearResourcePack();
                        if (RealmsConnect.this.aborted) {
                            return;
                        }
    
                        RealmsConnect.LOGGER.error("Couldn't connect to world", (Throwable)var6);
                        String var3 = var6.toString();
                        if (var0 != null) {
                            String var4 = var0 + ":" + param1;
                            var3 = var3.replaceAll(var4, "");
                        }
    
                        Realms.setScreen(
                            new DisconnectedRealmsScreen(
                                RealmsConnect.this.onlineScreen, "connect.failed", new TranslatableComponent("disconnect.genericReason", var3)
                            )
                        );
                    }
    
                }
            })
            .start();
    }

    public void abort() {
        this.aborted = true;
        if (this.connection != null && this.connection.isConnected()) {
            this.connection.disconnect(new TranslatableComponent("disconnect.genericReason"));
            this.connection.handleDisconnection();
        }

    }

    public void tick() {
        if (this.connection != null) {
            if (this.connection.isConnected()) {
                this.connection.tick();
            } else {
                this.connection.handleDisconnection();
            }
        }

    }
}
