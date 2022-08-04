package net.minecraft.realms;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.RealmsServer;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.world.entity.player.ProfilePublicKey;
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
        var0.getNarrator().sayNow(Component.translatable("mco.connect.success"));
        final CompletableFuture<Optional<ProfilePublicKey.Data>> var1 = var0.getProfileKeyPairManager().preparePublicKey();
        final String var2 = param1.getHost();
        final int var3 = param1.getPort();
        (new Thread("Realms-connect-task") {
                @Override
                public void run() {
                    InetSocketAddress var0 = null;
    
                    try {
                        var0 = new InetSocketAddress(var2, var3);
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
    
                        RealmsConnect.this.connection.send(new ClientIntentionPacket(var2, var3, ConnectionProtocol.LOGIN));
                        if (RealmsConnect.this.aborted) {
                            return;
                        }
    
                        String var1 = var0.getUser().getName();
                        UUID var2 = var0.getUser().getProfileId();
                        RealmsConnect.this.connection.send(new ServerboundHelloPacket(var1, var1.join(), Optional.ofNullable(var2)));
                        var0.setCurrentServer(param0, var2);
                    } catch (Exception var5) {
                        var0.getClientPackSource().clearServerPack();
                        if (RealmsConnect.this.aborted) {
                            return;
                        }
    
                        RealmsConnect.LOGGER.error("Couldn't connect to world", (Throwable)var5);
                        String var4 = var5.toString();
                        if (var0 != null) {
                            String var5 = var0 + ":" + var3;
                            var4 = var4.replaceAll(var5, "");
                        }
    
                        DisconnectedRealmsScreen var6 = new DisconnectedRealmsScreen(
                            RealmsConnect.this.onlineScreen, CommonComponents.CONNECT_FAILED, Component.translatable("disconnect.genericReason", var4)
                        );
                        var0.execute(() -> var0.setScreen(var6));
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
