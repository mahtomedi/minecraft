package net.minecraft.server.network;

import com.google.common.primitives.Ints;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.logging.LogUtils;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class ServerLoginPacketListenerImpl implements TickablePacketListener, ServerLoginPacketListener {
    private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_TICKS_BEFORE_LOGIN = 600;
    private static final Component DISCONNECT_UNEXPECTED_QUERY = Component.translatable("multiplayer.disconnect.unexpected_query_response");
    private final byte[] challenge;
    final MinecraftServer server;
    final Connection connection;
    private volatile ServerLoginPacketListenerImpl.State state = ServerLoginPacketListenerImpl.State.HELLO;
    private int tick;
    @Nullable
    String requestedUsername;
    @Nullable
    private GameProfile authenticatedProfile;
    private final String serverId = "";

    public ServerLoginPacketListenerImpl(MinecraftServer param0, Connection param1) {
        this.server = param0;
        this.connection = param1;
        this.challenge = Ints.toByteArray(RandomSource.create().nextInt());
    }

    @Override
    public void tick() {
        if (this.state == ServerLoginPacketListenerImpl.State.VERIFYING) {
            this.verifyLoginAndFinishConnectionSetup(Objects.requireNonNull(this.authenticatedProfile));
        }

        if (this.state == ServerLoginPacketListenerImpl.State.WAITING_FOR_DUPE_DISCONNECT
            && !this.isPlayerAlreadyInWorld(Objects.requireNonNull(this.authenticatedProfile))) {
            this.finishLoginAndWaitForClient(this.authenticatedProfile);
        }

        if (this.tick++ == 600) {
            this.disconnect(Component.translatable("multiplayer.disconnect.slow_login"));
        }

    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    public void disconnect(Component param0) {
        try {
            LOGGER.info("Disconnecting {}: {}", this.getUserName(), param0.getString());
            this.connection.send(new ClientboundLoginDisconnectPacket(param0));
            this.connection.disconnect(param0);
        } catch (Exception var3) {
            LOGGER.error("Error whilst disconnecting player", (Throwable)var3);
        }

    }

    private boolean isPlayerAlreadyInWorld(GameProfile param0) {
        return this.server.getPlayerList().getPlayer(param0.getId()) != null;
    }

    @Override
    public void onDisconnect(Component param0) {
        LOGGER.info("{} lost connection: {}", this.getUserName(), param0.getString());
    }

    public String getUserName() {
        String var0 = this.connection.getLoggableAddress(this.server.logIPs());
        return this.requestedUsername != null ? this.requestedUsername + " (" + var0 + ")" : var0;
    }

    @Override
    public void handleHello(ServerboundHelloPacket param0) {
        Validate.validState(this.state == ServerLoginPacketListenerImpl.State.HELLO, "Unexpected hello packet");
        Validate.validState(Player.isValidUsername(param0.name()), "Invalid characters in username");
        this.requestedUsername = param0.name();
        GameProfile var0 = this.server.getSingleplayerProfile();
        if (var0 != null && this.requestedUsername.equalsIgnoreCase(var0.getName())) {
            this.startClientVerification(var0);
        } else {
            if (this.server.usesAuthentication() && !this.connection.isMemoryConnection()) {
                this.state = ServerLoginPacketListenerImpl.State.KEY;
                this.connection.send(new ClientboundHelloPacket("", this.server.getKeyPair().getPublic().getEncoded(), this.challenge));
            } else {
                this.startClientVerification(UUIDUtil.createOfflineProfile(this.requestedUsername));
            }

        }
    }

    void startClientVerification(GameProfile param0) {
        this.authenticatedProfile = param0;
        this.state = ServerLoginPacketListenerImpl.State.VERIFYING;
    }

    private void verifyLoginAndFinishConnectionSetup(GameProfile param0) {
        PlayerList var0 = this.server.getPlayerList();
        Component var1 = var0.canPlayerLogin(this.connection.getRemoteAddress(), param0);
        if (var1 != null) {
            this.disconnect(var1);
        } else {
            if (this.server.getCompressionThreshold() >= 0 && !this.connection.isMemoryConnection()) {
                this.connection
                    .send(
                        new ClientboundLoginCompressionPacket(this.server.getCompressionThreshold()),
                        PacketSendListener.thenRun(() -> this.connection.setupCompression(this.server.getCompressionThreshold(), true))
                    );
            }

            boolean var2 = var0.disconnectAllPlayersWithProfile(param0);
            if (var2) {
                this.state = ServerLoginPacketListenerImpl.State.WAITING_FOR_DUPE_DISCONNECT;
            } else {
                this.finishLoginAndWaitForClient(param0);
            }
        }

    }

    private void finishLoginAndWaitForClient(GameProfile param0) {
        this.state = ServerLoginPacketListenerImpl.State.PROTOCOL_SWITCHING;
        this.connection.send(new ClientboundGameProfilePacket(param0));
    }

    @Override
    public void handleKey(ServerboundKeyPacket param0) {
        Validate.validState(this.state == ServerLoginPacketListenerImpl.State.KEY, "Unexpected key packet");

        final String var4;
        try {
            PrivateKey var0 = this.server.getKeyPair().getPrivate();
            if (!param0.isChallengeValid(this.challenge, var0)) {
                throw new IllegalStateException("Protocol error");
            }

            SecretKey var1 = param0.getSecretKey(var0);
            Cipher var2 = Crypt.getCipher(2, var1);
            Cipher var3 = Crypt.getCipher(1, var1);
            var4 = new BigInteger(Crypt.digestData("", this.server.getKeyPair().getPublic(), var1)).toString(16);
            this.state = ServerLoginPacketListenerImpl.State.AUTHENTICATING;
            this.connection.setEncryptionKey(var2, var3);
        } catch (CryptException var71) {
            throw new IllegalStateException("Protocol error", var71);
        }

        Thread var7 = new Thread("User Authenticator #" + UNIQUE_THREAD_ID.incrementAndGet()) {
            @Override
            public void run() {
                String var0 = Objects.requireNonNull(ServerLoginPacketListenerImpl.this.requestedUsername, "Player name not initialized");

                try {
                    ProfileResult var1 = ServerLoginPacketListenerImpl.this.server.getSessionService().hasJoinedServer(var0, var4, this.getAddress());
                    if (var1 != null) {
                        GameProfile var2 = var1.profile();
                        ServerLoginPacketListenerImpl.LOGGER.info("UUID of player {} is {}", var2.getName(), var2.getId());
                        ServerLoginPacketListenerImpl.this.startClientVerification(var2);
                    } else if (ServerLoginPacketListenerImpl.this.server.isSingleplayer()) {
                        ServerLoginPacketListenerImpl.LOGGER.warn("Failed to verify username but will let them in anyway!");
                        ServerLoginPacketListenerImpl.this.startClientVerification(UUIDUtil.createOfflineProfile(var0));
                    } else {
                        ServerLoginPacketListenerImpl.this.disconnect(Component.translatable("multiplayer.disconnect.unverified_username"));
                        ServerLoginPacketListenerImpl.LOGGER.error("Username '{}' tried to join with an invalid session", var0);
                    }
                } catch (AuthenticationUnavailableException var4) {
                    if (ServerLoginPacketListenerImpl.this.server.isSingleplayer()) {
                        ServerLoginPacketListenerImpl.LOGGER.warn("Authentication servers are down but will let them in anyway!");
                        ServerLoginPacketListenerImpl.this.startClientVerification(UUIDUtil.createOfflineProfile(var0));
                    } else {
                        ServerLoginPacketListenerImpl.this.disconnect(Component.translatable("multiplayer.disconnect.authservers_down"));
                        ServerLoginPacketListenerImpl.LOGGER.error("Couldn't verify username because servers are unavailable");
                    }
                }

            }

            @Nullable
            private InetAddress getAddress() {
                SocketAddress var0 = ServerLoginPacketListenerImpl.this.connection.getRemoteAddress();
                return ServerLoginPacketListenerImpl.this.server.getPreventProxyConnections() && var0 instanceof InetSocketAddress
                    ? ((InetSocketAddress)var0).getAddress()
                    : null;
            }
        };
        var7.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        var7.start();
    }

    @Override
    public void handleCustomQueryPacket(ServerboundCustomQueryAnswerPacket param0) {
        this.disconnect(DISCONNECT_UNEXPECTED_QUERY);
    }

    @Override
    public void handleLoginAcknowledgement(ServerboundLoginAcknowledgedPacket param0) {
        Validate.validState(this.state == ServerLoginPacketListenerImpl.State.PROTOCOL_SWITCHING, "Unexpected login acknowledgement packet");
        CommonListenerCookie var0 = CommonListenerCookie.createInitial(Objects.requireNonNull(this.authenticatedProfile));
        ServerConfigurationPacketListenerImpl var1 = new ServerConfigurationPacketListenerImpl(this.server, this.connection, var0);
        this.connection.setListener(var1);
        var1.startConfiguration();
        this.state = ServerLoginPacketListenerImpl.State.ACCEPTED;
    }

    static enum State {
        HELLO,
        KEY,
        AUTHENTICATING,
        NEGOTIATING,
        VERIFYING,
        WAITING_FOR_DUPE_DISCONNECT,
        PROTOCOL_SWITCHING,
        ACCEPTED;
    }
}
