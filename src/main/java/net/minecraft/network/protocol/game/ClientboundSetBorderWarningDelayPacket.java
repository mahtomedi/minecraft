package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderWarningDelayPacket implements Packet<ClientGamePacketListener> {
    private final int warningDelay;

    public ClientboundSetBorderWarningDelayPacket(WorldBorder param0) {
        this.warningDelay = param0.getWarningTime();
    }

    public ClientboundSetBorderWarningDelayPacket(FriendlyByteBuf param0) {
        this.warningDelay = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.warningDelay);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetBorderWarningDelay(this);
    }

    public int getWarningDelay() {
        return this.warningDelay;
    }
}
