package net.minecraft.realms;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.RealmsServer;
import java.net.InetSocketAddress;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsConnect {
    static final Logger LOGGER = LogUtils.getLogger();
    final Screen onlineScreen;
    volatile boolean aborted;
    @Nullable
    Connection connection;

    public RealmsConnect(Screen param0) {
        this.onlineScreen = param0;
    }

    public void connect(final RealmsServer param0, ServerAddress param1) {
        final Minecraft var0 = Minecraft.getInstance();
        var0.setConnectedToRealms(true);
        var0.prepareForMultiplayer();
        NarratorChatListener.INSTANCE.sayNow(Component.translatable("mco.connect.success"));
        final String var1 = param1.getHost();
        final int var2 = param1.getPort();
        (new Thread("Realms-connect-task") {
                @Override
                public void run() {
                    InetSocketAddress var0 = null;
    
                    try {
                        var0 = new InetSocketAddress(var1, var2);
                        if (RealmsConnect.this.aborted) {
                            return;
                        }
    
                        RealmsConnect.this.connection = Connection.connectToServer(var0, var0.options.useNativeTransport());
                        if (RealmsConnect.this.aborted) {
                            return;
                        }
    
                        RealmsConnect.this.connection
                            .setListener(
                                new ClientHandshakePacketListenerImpl(RealmsConnect.this.connection, var0, RealmsConnect.this.onlineScreen, param0xx -> {
                                })
                            );
                        if (RealmsConnect.this.aborted) {
                            return;
                        }
    
                        RealmsConnect.this.connection.send(new ClientIntentionPacket(var1, var2, ConnectionProtocol.LOGIN));
                        if (RealmsConnect.this.aborted) {
                            return;
                        }
    
                        RealmsConnect.this.connection.send(new ServerboundHelloPacket(var0.getUser().getGameProfile()));
                        var0.setCurrentServer(param0.toServerData(var1));
                    } catch (Exception var5) {
                        var0.getClientPackSource().clearServerPack();
                        if (RealmsConnect.this.aborted) {
                            return;
                        }
    
                        RealmsConnect.LOGGER.error("Couldn't connect to world", (Throwable)var5);
                        String var2 = var5.toString();
                        if (var0 != null) {
                            String var3 = var0 + ":" + var2;
                            var2 = var2.replaceAll(var3, "");
                        }
    
                        DisconnectedRealmsScreen var4 = new DisconnectedRealmsScreen(
                            RealmsConnect.this.onlineScreen, CommonComponents.CONNECT_FAILED, Component.translatable("disconnect.genericReason", var2)
                        );
                        var0.execute(() -> var0.setScreen(var4));
                    }
    
                }
            })
            .start();
    }

    public void abort() {
        this.aborted = true;
        if (this.connection != null && this.connection.isConnected()) {
            this.connection.disconnect(Component.translatable("disconnect.genericReason"));
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
