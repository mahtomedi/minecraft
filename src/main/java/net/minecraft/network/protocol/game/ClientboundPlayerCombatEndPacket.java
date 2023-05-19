package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.CombatTracker;

public class ClientboundPlayerCombatEndPacket implements Packet<ClientGamePacketListener> {
    private final int duration;

    public ClientboundPlayerCombatEndPacket(CombatTracker param0) {
        this(param0.getCombatDuration());
    }

    public ClientboundPlayerCombatEndPacket(int param0) {
        this.duration = param0;
    }

    public ClientboundPlayerCombatEndPacket(FriendlyByteBuf param0) {
        this.duration = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.duration);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handlePlayerCombatEnd(this);
    }
}
