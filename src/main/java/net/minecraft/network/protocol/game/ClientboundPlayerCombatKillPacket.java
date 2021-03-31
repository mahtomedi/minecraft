package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.CombatTracker;

public class ClientboundPlayerCombatKillPacket implements Packet<ClientGamePacketListener> {
    private final int playerId;
    private final int killerId;
    private final Component message;

    public ClientboundPlayerCombatKillPacket(CombatTracker param0, Component param1) {
        this(param0.getMob().getId(), param0.getKillerId(), param1);
    }

    public ClientboundPlayerCombatKillPacket(int param0, int param1, Component param2) {
        this.playerId = param0;
        this.killerId = param1;
        this.message = param2;
    }

    public ClientboundPlayerCombatKillPacket(FriendlyByteBuf param0) {
        this.playerId = param0.readVarInt();
        this.killerId = param0.readInt();
        this.message = param0.readComponent();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.playerId);
        param0.writeInt(this.killerId);
        param0.writeComponent(this.message);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handlePlayerCombatKill(this);
    }

    @Override
    public boolean isSkippable() {
        return true;
    }

    public int getKillerId() {
        return this.killerId;
    }

    public int getPlayerId() {
        return this.playerId;
    }

    public Component getMessage() {
        return this.message;
    }
}
