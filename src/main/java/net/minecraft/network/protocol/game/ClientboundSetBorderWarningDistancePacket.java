package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderWarningDistancePacket implements Packet<ClientGamePacketListener> {
    private final int warningBlocks;

    public ClientboundSetBorderWarningDistancePacket(WorldBorder param0) {
        this.warningBlocks = param0.getWarningBlocks();
    }

    public ClientboundSetBorderWarningDistancePacket(FriendlyByteBuf param0) {
        this.warningBlocks = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.warningBlocks);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetBorderWarningDistance(this);
    }

    public int getWarningBlocks() {
        return this.warningBlocks;
    }
}
