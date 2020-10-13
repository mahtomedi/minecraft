package net.minecraft.server.network;

import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.server.MinecraftServer;

public class ServerHandshakePacketListenerImpl implements ServerHandshakePacketListener {
    private static final Component IGNORE_STATUS_REASON = new TextComponent("Ignoring status request");
    private final MinecraftServer server;
    private final Connection connection;

    public ServerHandshakePacketListenerImpl(MinecraftServer param0, Connection param1) {
        this.server = param0;
        this.connection = param1;
    }

    @Override
    public void handleIntention(ClientIntentionPacket param0) {
        switch(param0.getIntention()) {
            case LOGIN:
                this.connection.setProtocol(ConnectionProtocol.LOGIN);
                if (param0.getProtocolVersion() != SharedConstants.getCurrentVersion().getProtocolVersion()) {
                    Component var0 = new TranslatableComponent("multiplayer.disconnect.incompatible", SharedConstants.getCurrentVersion().getName());
                    this.connection.send(new ClientboundLoginDisconnectPacket(var0));
                    this.connection.disconnect(var0);
                } else {
                    this.connection.setListener(new ServerLoginPacketListenerImpl(this.server, this.connection));
                }
                break;
            case STATUS:
                if (this.server.repliesToStatus()) {
                    this.connection.setProtocol(ConnectionProtocol.STATUS);
                    this.connection.setListener(new ServerStatusPacketListenerImpl(this.server, this.connection));
                } else {
                    this.connection.disconnect(IGNORE_STATUS_REASON);
                }
                break;
            default:
                throw new UnsupportedOperationException("Invalid intention " + param0.getIntention());
        }

    }

    @Override
    public void onDisconnect(Component param0) {
    }

    @Override
    public Connection getConnection() {
        return this.connection;
    }
}
