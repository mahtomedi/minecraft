package net.minecraft.network.protocol.game;

import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;

public class ClientboundBundlePacket extends BundlePacket<ClientGamePacketListener> {
    public ClientboundBundlePacket(Iterable<Packet<ClientGamePacketListener>> param0) {
        super(param0);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleBundlePacket(this);
    }
}
