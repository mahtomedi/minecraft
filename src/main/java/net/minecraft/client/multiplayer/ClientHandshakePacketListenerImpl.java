package net.minecraft.client.multiplayer;

import com.google.common.primitives.Longs;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InsufficientPrivilegesException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.logging.LogUtils;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.Crypt;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.SignatureUpdater;
import net.minecraft.util.Signer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientHandshakePacketListenerImpl implements ClientLoginPacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Minecraft minecraft;
    @Nullable
    private final Screen parent;
    private final Consumer<Component> updateStatus;
    private final Connection connection;
    private GameProfile localGameProfile;

    public ClientHandshakePacketListenerImpl(Connection param0, Minecraft param1, @Nullable Screen param2, Consumer<Component> param3) {
        this.connection = param0;
        this.minecraft = param1;
        this.parent = param2;
        this.updateStatus = param3;
    }

    @Override
    public void handleHello(ClientboundHelloPacket param0) {
        Cipher var3;
        Cipher var4;
        String var2;
        ServerboundKeyPacket var7;
        try {
            SecretKey var0 = Crypt.generateSecretKey();
            PublicKey var1 = param0.getPublicKey();
            var2 = new BigInteger(Crypt.digestData(param0.getServerId(), var1, var0)).toString(16);
            var3 = Crypt.getCipher(2, var0);
            var4 = Crypt.getCipher(1, var0);
            byte[] var5 = param0.getNonce();
            Signer var6 = this.minecraft.getProfileKeyPairManager().signer();
            if (var6 == null) {
                var7 = new ServerboundKeyPacket(var0, var1, var5);
            } else {
                long var8 = Crypt.SaltSupplier.getLong();
                byte[] var9 = var6.sign((SignatureUpdater)(param2 -> {
                    param2.update(var5);
                    param2.update(Longs.toByteArray(var8));
                }));
                var7 = new ServerboundKeyPacket(var0, var1, var8, var9);
            }
        } catch (Exception var131) {
            throw new IllegalStateException("Protocol error", var131);
        }

        this.updateStatus.accept(Component.translatable("connect.authorizing"));
        HttpUtil.DOWNLOAD_EXECUTOR.submit(() -> {
            Component var0x = this.authenticateServer(var2);
            if (var0x != null) {
                if (this.minecraft.getCurrentServer() == null || !this.minecraft.getCurrentServer().isLan()) {
                    this.connection.disconnect(var0x);
                    return;
                }

                LOGGER.warn(var0x.getString());
            }

            this.updateStatus.accept(Component.translatable("connect.encrypting"));
            this.connection.send(var7, param2x -> this.connection.setEncryptionKey(var3, var4));
        });
    }

    @Nullable
    private Component authenticateServer(String param0) {
        try {
            this.getMinecraftSessionService().joinServer(this.minecraft.getUser().getGameProfile(), this.minecraft.getUser().getAccessToken(), param0);
            return null;
        } catch (AuthenticationUnavailableException var3) {
            return Component.translatable("disconnect.loginFailedInfo", Component.translatable("disconnect.loginFailedInfo.serversUnavailable"));
        } catch (InvalidCredentialsException var4) {
            return Component.translatable("disconnect.loginFailedInfo", Component.translatable("disconnect.loginFailedInfo.invalidSession"));
        } catch (InsufficientPrivilegesException var5) {
            return Component.translatable("disconnect.loginFailedInfo", Component.translatable("disconnect.loginFailedInfo.insufficientPrivileges"));
        } catch (AuthenticationException var6) {
            return Component.translatable("disconnect.loginFailedInfo", var6.getMessage());
        }
    }

    private MinecraftSessionService getMinecraftSessionService() {
        return this.minecraft.getMinecraftSessionService();
    }

    @Override
    public void handleGameProfile(ClientboundGameProfilePacket param0) {
        this.updateStatus.accept(Component.translatable("connect.joining"));
        this.localGameProfile = param0.getGameProfile();
        this.connection.setProtocol(ConnectionProtocol.PLAY);
        this.connection
            .setListener(new ClientPacketListener(this.minecraft, this.parent, this.connection, this.localGameProfile, this.minecraft.createTelemetryManager()));
    }

    @Override
    public void onDisconnect(Component param0) {
        if (this.parent != null && this.parent instanceof RealmsScreen) {
            this.minecraft.setScreen(new DisconnectedRealmsScreen(this.parent, CommonComponents.CONNECT_FAILED, param0));
        } else {
            this.minecraft.setScreen(new DisconnectedScreen(this.parent, CommonComponents.CONNECT_FAILED, param0));
        }

    }

    @Override
    public Connection getConnection() {
        return this.connection;
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
        this.connection.send(new ServerboundCustomQueryPacket(param0.getTransactionId(), null));
    }
}
