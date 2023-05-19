package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundPlayerCombatKillPacket implements Packet<ClientGamePacketListener> {
    private final int playerId;
    private final Component message;

    public ClientboundPlayerCombatKillPacket(int param0, Component param1) {
        this.playerId = param0;
        this.message = param1;
    }

    public ClientboundPlayerCombatKillPacket(FriendlyByteBuf param0) {
        this.playerId = param0.readVarInt();
        this.message = param0.readComponent();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.playerId);
        param0.writeComponent(this.message);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handlePlayerCombatKill(this);
    }

    @Override
    public boolean isSkippable() {
        return true;
    }

    public int getPlayerId() {
        return this.playerId;
    }

    public Component getMessage() {
        return this.message;
    }
}
