package net.minecraft.server.network;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.server.MinecraftServer;

public class MemoryServerHandshakePacketListenerImpl implements ServerHandshakePacketListener {
    private final MinecraftServer server;
    private final Connection connection;

    public MemoryServerHandshakePacketListenerImpl(MinecraftServer param0, Connection param1) {
        this.server = param0;
        this.connection = param1;
    }

    @Override
    public void handleIntention(ClientIntentionPacket param0) {
        if (param0.intention() != ClientIntent.LOGIN) {
            throw new UnsupportedOperationException("Invalid intention " + param0.intention());
        } else {
            this.connection.setClientboundProtocolAfterHandshake(ClientIntent.LOGIN);
            this.connection.setListener(new ServerLoginPacketListenerImpl(this.server, this.connection));
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
