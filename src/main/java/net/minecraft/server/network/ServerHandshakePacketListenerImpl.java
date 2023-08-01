package net.minecraft.server.network;

import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;

public class ServerHandshakePacketListenerImpl implements ServerHandshakePacketListener {
    private static final Component IGNORE_STATUS_REASON = Component.translatable("disconnect.ignoring_status_request");
    private final MinecraftServer server;
    private final Connection connection;

    public ServerHandshakePacketListenerImpl(MinecraftServer param0, Connection param1) {
        this.server = param0;
        this.connection = param1;
    }

    @Override
    public void handleIntention(ClientIntentionPacket param0) {
        switch(param0.intention()) {
            case LOGIN:
                this.connection.setClientboundProtocolAfterHandshake(ClientIntent.LOGIN);
                if (param0.protocolVersion() != SharedConstants.getCurrentVersion().getProtocolVersion()) {
                    Component var0;
                    if (param0.protocolVersion() < 754) {
                        var0 = Component.translatable("multiplayer.disconnect.outdated_client", SharedConstants.getCurrentVersion().getName());
                    } else {
                        var0 = Component.translatable("multiplayer.disconnect.incompatible", SharedConstants.getCurrentVersion().getName());
                    }

                    this.connection.send(new ClientboundLoginDisconnectPacket(var0));
                    this.connection.disconnect(var0);
                } else {
                    this.connection.setListener(new ServerLoginPacketListenerImpl(this.server, this.connection));
                }
                break;
            case STATUS:
                ServerStatus var2 = this.server.getStatus();
                if (this.server.repliesToStatus() && var2 != null) {
                    this.connection.setClientboundProtocolAfterHandshake(ClientIntent.STATUS);
                    this.connection.setListener(new ServerStatusPacketListenerImpl(var2, this.connection));
                } else {
                    this.connection.disconnect(IGNORE_STATUS_REASON);
                }
                break;
            default:
                throw new UnsupportedOperationException("Invalid intention " + param0.intention());
        }

    }

    @Override
    public void onDisconnect(Component param0) {
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }
}
