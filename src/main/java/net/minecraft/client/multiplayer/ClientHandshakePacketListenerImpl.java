package net.minecraft.client.multiplayer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientHandshakePacketListenerImpl implements ClientLoginPacketListener {
    private static final Logger LOGGER = LogManager.getLogger();
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
        SecretKey var0 = Crypt.generateSecretKey();
        PublicKey var1 = param0.getPublicKey();
        String var2 = new BigInteger(Crypt.digestData(param0.getServerId(), var1, var0)).toString(16);
        ServerboundKeyPacket var3 = new ServerboundKeyPacket(var0, var1, param0.getNonce());
        this.updateStatus.accept(new TranslatableComponent("connect.authorizing"));
        HttpUtil.DOWNLOAD_EXECUTOR.submit(() -> {
            Component var0x = this.authenticateServer(var2);
            if (var0x != null) {
                if (this.minecraft.getCurrentServer() == null || !this.minecraft.getCurrentServer().isLan()) {
                    this.connection.disconnect(var0x);
                    return;
                }

                LOGGER.warn(var0x.getString());
            }

            this.updateStatus.accept(new TranslatableComponent("connect.encrypting"));
            this.connection.send(var3, param1x -> this.connection.setEncryptionKey(var0));
        });
    }

    @Nullable
    private Component authenticateServer(String param0) {
        try {
            this.getMinecraftSessionService().joinServer(this.minecraft.getUser().getGameProfile(), this.minecraft.getUser().getAccessToken(), param0);
            return null;
        } catch (AuthenticationUnavailableException var3) {
            return new TranslatableComponent("disconnect.loginFailedInfo", new TranslatableComponent("disconnect.loginFailedInfo.serversUnavailable"));
        } catch (InvalidCredentialsException var4) {
            return new TranslatableComponent("disconnect.loginFailedInfo", new TranslatableComponent("disconnect.loginFailedInfo.invalidSession"));
        } catch (AuthenticationException var5) {
            return new TranslatableComponent("disconnect.loginFailedInfo", var5.getMessage());
        }
    }

    private MinecraftSessionService getMinecraftSessionService() {
        return this.minecraft.getMinecraftSessionService();
    }

    @Override
    public void handleGameProfile(ClientboundGameProfilePacket param0) {
        this.updateStatus.accept(new TranslatableComponent("connect.joining"));
        this.localGameProfile = param0.getGameProfile();
        this.connection.setProtocol(ConnectionProtocol.PLAY);
        this.connection.setListener(new ClientPacketListener(this.minecraft, this.parent, this.connection, this.localGameProfile));
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
            this.connection.setupCompression(param0.getCompressionThreshold());
        }

    }

    @Override
    public void handleCustomQuery(ClientboundCustomQueryPacket param0) {
        this.updateStatus.accept(new TranslatableComponent("connect.negotiating"));
        this.connection.send(new ServerboundCustomQueryPacket(param0.getTransactionId(), null));
    }
}
