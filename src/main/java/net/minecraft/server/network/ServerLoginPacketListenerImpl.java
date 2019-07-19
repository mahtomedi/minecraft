package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerLoginPacketListenerImpl implements ServerLoginPacketListener {
    private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random RANDOM = new Random();
    private final byte[] nonce = new byte[4];
    private final MinecraftServer server;
    public final Connection connection;
    private ServerLoginPacketListenerImpl.State state = ServerLoginPacketListenerImpl.State.HELLO;
    private int tick;
    private GameProfile gameProfile;
    private final String serverId = "";
    private SecretKey secretKey;
    private ServerPlayer delayedAcceptPlayer;

    public ServerLoginPacketListenerImpl(MinecraftServer param0, Connection param1) {
        this.server = param0;
        this.connection = param1;
        RANDOM.nextBytes(this.nonce);
    }

    public void tick() {
        if (this.state == ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT) {
            this.handleAcceptedLogin();
        } else if (this.state == ServerLoginPacketListenerImpl.State.DELAY_ACCEPT) {
            ServerPlayer var0 = this.server.getPlayerList().getPlayer(this.gameProfile.getId());
            if (var0 == null) {
                this.state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
                this.server.getPlayerList().placeNewPlayer(this.connection, this.delayedAcceptPlayer);
                this.delayedAcceptPlayer = null;
            }
        }

        if (this.tick++ == 600) {
            this.disconnect(new TranslatableComponent("multiplayer.disconnect.slow_login"));
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
                        param0 -> this.connection.setupCompression(this.server.getCompressionThreshold())
                    );
            }

            this.connection.send(new ClientboundGameProfilePacket(this.gameProfile));
            ServerPlayer var1 = this.server.getPlayerList().getPlayer(this.gameProfile.getId());
            if (var1 != null) {
                this.state = ServerLoginPacketListenerImpl.State.DELAY_ACCEPT;
                this.delayedAcceptPlayer = this.server.getPlayerList().getPlayerForLogin(this.gameProfile);
            } else {
                this.server.getPlayerList().placeNewPlayer(this.connection, this.server.getPlayerList().getPlayerForLogin(this.gameProfile));
            }
        }

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
        this.gameProfile = param0.getGameProfile();
        if (this.server.usesAuthentication() && !this.connection.isMemoryConnection()) {
            this.state = ServerLoginPacketListenerImpl.State.KEY;
            this.connection.send(new ClientboundHelloPacket("", this.server.getKeyPair().getPublic(), this.nonce));
        } else {
            this.state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
        }

    }

    @Override
    public void handleKey(ServerboundKeyPacket param0) {
        Validate.validState(this.state == ServerLoginPacketListenerImpl.State.KEY, "Unexpected key packet");
        PrivateKey var0 = this.server.getKeyPair().getPrivate();
        if (!Arrays.equals(this.nonce, param0.getNonce(var0))) {
            throw new IllegalStateException("Invalid nonce!");
        } else {
            this.secretKey = param0.getSecretKey(var0);
            this.state = ServerLoginPacketListenerImpl.State.AUTHENTICATING;
            this.connection.setEncryptionKey(this.secretKey);
            Thread var1 = new Thread("User Authenticator #" + UNIQUE_THREAD_ID.incrementAndGet()) {
                @Override
                public void run() {
                    GameProfile var0 = ServerLoginPacketListenerImpl.this.gameProfile;

                    try {
                        String var1 = new BigInteger(
                                Crypt.digestData(
                                    "", ServerLoginPacketListenerImpl.this.server.getKeyPair().getPublic(), ServerLoginPacketListenerImpl.this.secretKey
                                )
                            )
                            .toString(16);
                        ServerLoginPacketListenerImpl.this.gameProfile = ServerLoginPacketListenerImpl.this.server
                            .getSessionService()
                            .hasJoinedServer(new GameProfile(null, var0.getName()), var1, this.getAddress());
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
                            ServerLoginPacketListenerImpl.this.gameProfile = ServerLoginPacketListenerImpl.this.createFakeProfile(var0);
                            ServerLoginPacketListenerImpl.this.state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
                        } else {
                            ServerLoginPacketListenerImpl.this.disconnect(new TranslatableComponent("multiplayer.disconnect.unverified_username"));
                            ServerLoginPacketListenerImpl.LOGGER.error("Username '{}' tried to join with an invalid session", var0.getName());
                        }
                    } catch (AuthenticationUnavailableException var3) {
                        if (ServerLoginPacketListenerImpl.this.server.isSingleplayer()) {
                            ServerLoginPacketListenerImpl.LOGGER.warn("Authentication servers are down but will let them in anyway!");
                            ServerLoginPacketListenerImpl.this.gameProfile = ServerLoginPacketListenerImpl.this.createFakeProfile(var0);
                            ServerLoginPacketListenerImpl.this.state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
                        } else {
                            ServerLoginPacketListenerImpl.this.disconnect(new TranslatableComponent("multiplayer.disconnect.authservers_down"));
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
            var1.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
            var1.start();
        }
    }

    @Override
    public void handleCustomQueryPacket(ServerboundCustomQueryPacket param0) {
        this.disconnect(new TranslatableComponent("multiplayer.disconnect.unexpected_query_response"));
    }

    protected GameProfile createFakeProfile(GameProfile param0) {
        UUID var0 = Player.createPlayerUUID(param0.getName());
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
