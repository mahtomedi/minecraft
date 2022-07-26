package net.minecraft.server.network;

import com.google.common.primitives.Ints;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.logging.LogUtils;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.UUID;
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
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class ServerLoginPacketListenerImpl implements TickablePacketListener, ServerLoginPacketListener {
    private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_TICKS_BEFORE_LOGIN = 600;
    private static final RandomSource RANDOM = RandomSource.create();
    private final byte[] challenge;
    final MinecraftServer server;
    public final Connection connection;
    ServerLoginPacketListenerImpl.State state = ServerLoginPacketListenerImpl.State.HELLO;
    private int tick;
    @Nullable
    GameProfile gameProfile;
    private final String serverId = "";
    @Nullable
    private ServerPlayer delayedAcceptPlayer;

    public ServerLoginPacketListenerImpl(MinecraftServer param0, Connection param1) {
        this.server = param0;
        this.connection = param1;
        this.challenge = Ints.toByteArray(RANDOM.nextInt());
    }

    @Override
    public void tick() {
        if (this.state == ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT) {
            this.handleAcceptedLogin();
        } else if (this.state == ServerLoginPacketListenerImpl.State.DELAY_ACCEPT) {
            ServerPlayer var0 = this.server.getPlayerList().getPlayer(this.gameProfile.getId());
            if (var0 == null) {
                this.state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
                this.placeNewPlayer(this.delayedAcceptPlayer);
                this.delayedAcceptPlayer = null;
            }
        }

        if (this.tick++ == 600) {
            this.disconnect(Component.translatable("multiplayer.disconnect.slow_login"));
        }

    }

    @Override
    public Connection getConnection() {
        return this.connection;
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

    public void handleAcceptedLogin() {
        if (!this.gameProfile.isComplete()) {
            this.gameProfile = this.createFakeProfile(this.gameProfile);
        }

        Component var0 = this.server.getPlayerList().canPlayerLogin(this.connection.getRemoteAddress(), this.gameProfile);
        if (var0 != null) {
            this.disconnect(var0);
        } else {
            this.state = ServerLoginPacketListenerImpl.State.ACCEPTED;
            if (this.server.getCompressionThreshold() >= 0 && !this.connection.isMemoryConnection()) {
                this.connection
                    .send(
                        new ClientboundLoginCompressionPacket(this.server.getCompressionThreshold()),
                        PacketSendListener.thenRun(() -> this.connection.setupCompression(this.server.getCompressionThreshold(), true))
                    );
            }

            this.connection.send(new ClientboundGameProfilePacket(this.gameProfile));
            ServerPlayer var1 = this.server.getPlayerList().getPlayer(this.gameProfile.getId());

            try {
                ServerPlayer var2 = this.server.getPlayerList().getPlayerForLogin(this.gameProfile);
                if (var1 != null) {
                    this.state = ServerLoginPacketListenerImpl.State.DELAY_ACCEPT;
                    this.delayedAcceptPlayer = var2;
                } else {
                    this.placeNewPlayer(var2);
                }
            } catch (Exception var5) {
                LOGGER.error("Couldn't place player in world", (Throwable)var5);
                Component var4 = Component.translatable("multiplayer.disconnect.invalid_player_data");
                this.connection.send(new ClientboundDisconnectPacket(var4));
                this.connection.disconnect(var4);
            }
        }

    }

    private void placeNewPlayer(ServerPlayer param0) {
        this.server.getPlayerList().placeNewPlayer(this.connection, param0);
    }

    @Override
    public void onDisconnect(Component param0) {
        LOGGER.info("{} lost connection: {}", this.getUserName(), param0.getString());
    }

    public String getUserName() {
        return this.gameProfile != null
            ? this.gameProfile + " (" + this.connection.getRemoteAddress() + ")"
            : String.valueOf(this.connection.getRemoteAddress());
    }

    @Override
    public void handleHello(ServerboundHelloPacket param0) {
        Validate.validState(this.state == ServerLoginPacketListenerImpl.State.HELLO, "Unexpected hello packet");
        Validate.validState(isValidUsername(param0.name()), "Invalid characters in username");
        GameProfile var0 = this.server.getSingleplayerProfile();
        if (var0 != null && param0.name().equalsIgnoreCase(var0.getName())) {
            this.gameProfile = var0;
            this.state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
        } else {
            this.gameProfile = new GameProfile(null, param0.name());
            if (this.server.usesAuthentication() && !this.connection.isMemoryConnection()) {
                this.state = ServerLoginPacketListenerImpl.State.KEY;
                this.connection.send(new ClientboundHelloPacket("", this.server.getKeyPair().getPublic().getEncoded(), this.challenge));
            } else {
                this.state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
            }

        }
    }

    public static boolean isValidUsername(String param0) {
        return param0.chars().filter(param0x -> param0x <= 32 || param0x >= 127).findAny().isEmpty();
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
                GameProfile var0 = ServerLoginPacketListenerImpl.this.gameProfile;

                try {
                    ServerLoginPacketListenerImpl.this.gameProfile = ServerLoginPacketListenerImpl.this.server
                        .getSessionService()
                        .hasJoinedServer(new GameProfile(null, var0.getName()), var4, this.getAddress());
                    if (ServerLoginPacketListenerImpl.this.gameProfile != null) {
                        ServerLoginPacketListenerImpl.LOGGER
                            .info(
                                "UUID of player {} is {}",
                                ServerLoginPacketListenerImpl.this.gameProfile.getName(),
                                ServerLoginPacketListenerImpl.this.gameProfile.getId()
                            );
                        ServerLoginPacketListenerImpl.this.state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
                    } else if (ServerLoginPacketListenerImpl.this.server.isSingleplayer()) {
                        ServerLoginPacketListenerImpl.LOGGER.warn("Failed to verify username but will let them in anyway!");
                        ServerLoginPacketListenerImpl.this.gameProfile = var0;
                        ServerLoginPacketListenerImpl.this.state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
                    } else {
                        ServerLoginPacketListenerImpl.this.disconnect(Component.translatable("multiplayer.disconnect.unverified_username"));
                        ServerLoginPacketListenerImpl.LOGGER.error("Username '{}' tried to join with an invalid session", var0.getName());
                    }
                } catch (AuthenticationUnavailableException var3) {
                    if (ServerLoginPacketListenerImpl.this.server.isSingleplayer()) {
                        ServerLoginPacketListenerImpl.LOGGER.warn("Authentication servers are down but will let them in anyway!");
                        ServerLoginPacketListenerImpl.this.gameProfile = var0;
                        ServerLoginPacketListenerImpl.this.state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
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
    public void handleCustomQueryPacket(ServerboundCustomQueryPacket param0) {
        this.disconnect(Component.translatable("multiplayer.disconnect.unexpected_query_response"));
    }

    protected GameProfile createFakeProfile(GameProfile param0) {
        UUID var0 = UUIDUtil.createOfflinePlayerUUID(param0.getName());
        return new GameProfile(var0, param0.getName());
    }

    static enum State {
        HELLO,
        KEY,
        AUTHENTICATING,
        NEGOTIATING,
        READY_TO_ACCEPT,
        DELAY_ACCEPT,
        ACCEPTED;
    }
}
