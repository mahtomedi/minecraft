package net.minecraft.client.multiplayer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.ForcedUsernameChangeException;
import com.mojang.authlib.exceptions.InsufficientPrivilegesException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.exceptions.UserBannedException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.logging.LogUtils;
import java.math.BigInteger;
import java.security.PublicKey;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.minecraft.Util;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.util.Crypt;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientHandshakePacketListenerImpl implements ClientLoginPacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Minecraft minecraft;
    @Nullable
    private final ServerData serverData;
    @Nullable
    private final Screen parent;
    private final Consumer<Component> updateStatus;
    private final Connection connection;
    private final boolean newWorld;
    @Nullable
    private final Duration worldLoadDuration;
    @Nullable
    private String minigameName;
    private final AtomicReference<ClientHandshakePacketListenerImpl.State> state = new AtomicReference<>(ClientHandshakePacketListenerImpl.State.CONNECTING);

    public ClientHandshakePacketListenerImpl(
        Connection param0,
        Minecraft param1,
        @Nullable ServerData param2,
        @Nullable Screen param3,
        boolean param4,
        @Nullable Duration param5,
        Consumer<Component> param6
    ) {
        this.connection = param0;
        this.minecraft = param1;
        this.serverData = param2;
        this.parent = param3;
        this.updateStatus = param6;
        this.newWorld = param4;
        this.worldLoadDuration = param5;
    }

    private void switchState(ClientHandshakePacketListenerImpl.State param0) {
        ClientHandshakePacketListenerImpl.State var0 = this.state.updateAndGet(param1 -> {
            if (!param0.fromStates.contains(param1)) {
                throw new IllegalStateException("Tried to switch to " + param0 + " from " + param1 + ", but expected one of " + param0.fromStates);
            } else {
                return param0;
            }
        });
        this.updateStatus.accept(var0.message);
    }

    @Override
    public void handleHello(ClientboundHelloPacket param0) {
        this.switchState(ClientHandshakePacketListenerImpl.State.AUTHORIZING);

        Cipher var3;
        Cipher var4;
        String var2;
        ServerboundKeyPacket var6;
        try {
            SecretKey var0 = Crypt.generateSecretKey();
            PublicKey var1 = param0.getPublicKey();
            var2 = new BigInteger(Crypt.digestData(param0.getServerId(), var1, var0)).toString(16);
            var3 = Crypt.getCipher(2, var0);
            var4 = Crypt.getCipher(1, var0);
            byte[] var5 = param0.getChallenge();
            var6 = new ServerboundKeyPacket(var0, var1, var5);
        } catch (Exception var91) {
            throw new IllegalStateException("Protocol error", var91);
        }

        Util.ioPool().submit(() -> {
            Component var0x = this.authenticateServer(var2);
            if (var0x != null) {
                if (this.serverData == null || !this.serverData.isLan()) {
                    this.connection.disconnect(var0x);
                    return;
                }

                LOGGER.warn(var0x.getString());
            }

            this.switchState(ClientHandshakePacketListenerImpl.State.ENCRYPTING);
            this.connection.send(var6, PacketSendListener.thenRun(() -> this.connection.setEncryptionKey(var3, var4)));
        });
    }

    @Nullable
    private Component authenticateServer(String param0) {
        try {
            this.getMinecraftSessionService().joinServer(this.minecraft.getUser().getProfileId(), this.minecraft.getUser().getAccessToken(), param0);
            return null;
        } catch (AuthenticationUnavailableException var3) {
            return Component.translatable("disconnect.loginFailedInfo", Component.translatable("disconnect.loginFailedInfo.serversUnavailable"));
        } catch (InvalidCredentialsException var4) {
            return Component.translatable("disconnect.loginFailedInfo", Component.translatable("disconnect.loginFailedInfo.invalidSession"));
        } catch (InsufficientPrivilegesException var5) {
            return Component.translatable("disconnect.loginFailedInfo", Component.translatable("disconnect.loginFailedInfo.insufficientPrivileges"));
        } catch (ForcedUsernameChangeException | UserBannedException var6) {
            return Component.translatable("disconnect.loginFailedInfo", Component.translatable("disconnect.loginFailedInfo.userBanned"));
        } catch (AuthenticationException var7) {
            return Component.translatable("disconnect.loginFailedInfo", var7.getMessage());
        }
    }

    private MinecraftSessionService getMinecraftSessionService() {
        return this.minecraft.getMinecraftSessionService();
    }

    @Override
    public void handleGameProfile(ClientboundGameProfilePacket param0) {
        this.switchState(ClientHandshakePacketListenerImpl.State.JOINING);
        GameProfile var0 = param0.getGameProfile();
        this.connection.send(new ServerboundLoginAcknowledgedPacket());
        this.connection
            .setListener(
                new ClientConfigurationPacketListenerImpl(
                    this.minecraft,
                    this.connection,
                    new CommonListenerCookie(
                        var0,
                        this.minecraft.getTelemetryManager().createWorldSessionManager(this.newWorld, this.worldLoadDuration, this.minigameName),
                        ClientRegistryLayer.createRegistryAccess().compositeAccess(),
                        FeatureFlags.DEFAULT_FLAGS,
                        null,
                        this.serverData,
                        this.parent
                    )
                )
            );
        this.connection.send(new ServerboundCustomPayloadPacket(new BrandPayload(ClientBrandRetriever.getClientModName())));
        this.connection.send(new ServerboundClientInformationPacket(this.minecraft.options.buildPlayerInformation()));
    }

    @Override
    public void onDisconnect(Component param0) {
        if (this.serverData != null && this.serverData.isRealm()) {
            this.minecraft.setScreen(new DisconnectedRealmsScreen(this.parent, CommonComponents.CONNECT_FAILED, param0));
        } else {
            this.minecraft.setScreen(new DisconnectedScreen(this.parent, CommonComponents.CONNECT_FAILED, param0));
        }

    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    @Override
    public void handleDisconnect(ClientboundLoginDisconnectPacket param0) {
        this.connection.disconnect(param0.getReason());
    }

    @Override
    public void handleCompression(ClientboundLoginCompressionPacket param0) {
        if (!this.connection.isMemoryConnection()) {
            this.connection.setupCompression(param0.getCompressionThreshold(), false);
        }

    }

    @Override
    public void handleCustomQuery(ClientboundCustomQueryPacket param0) {
        this.updateStatus.accept(Component.translatable("connect.negotiating"));
        this.connection.send(new ServerboundCustomQueryAnswerPacket(param0.transactionId(), null));
    }

    public void setMinigameName(String param0) {
        this.minigameName = param0;
    }

    @OnlyIn(Dist.CLIENT)
    static enum State {
        CONNECTING(Component.translatable("connect.connecting"), Set.of()),
        AUTHORIZING(Component.translatable("connect.authorizing"), Set.of(CONNECTING)),
        ENCRYPTING(Component.translatable("connect.encrypting"), Set.of(AUTHORIZING)),
        JOINING(Component.translatable("connect.joining"), Set.of(ENCRYPTING, CONNECTING));

        final Component message;
        final Set<ClientHandshakePacketListenerImpl.State> fromStates;

        private State(Component param0, Set<ClientHandshakePacketListenerImpl.State> param1) {
            this.message = param0;
            this.fromStates = param1;
        }
    }
}
