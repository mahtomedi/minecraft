package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundPlayerCombatEnterPacket implements Packet<ClientGamePacketListener> {
    public ClientboundPlayerCombatEnterPacket() {
    }

    public ClientboundPlayerCombatEnterPacket(FriendlyByteBuf param0) {
    }

    @Override
    public void write(FriendlyByteBuf param0) {
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handlePlayerCombatEnter(this);
    }
}
