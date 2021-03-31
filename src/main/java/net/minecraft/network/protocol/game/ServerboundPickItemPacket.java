package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPickItemPacket implements Packet<ServerGamePacketListener> {
    private final int slot;

    public ServerboundPickItemPacket(int param0) {
        this.slot = param0;
    }

    public ServerboundPickItemPacket(FriendlyByteBuf param0) {
        this.slot = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.slot);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handlePickItem(this);
    }

    public int getSlot() {
        return this.slot;
    }
}
