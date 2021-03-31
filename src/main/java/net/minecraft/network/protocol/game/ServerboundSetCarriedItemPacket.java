package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundSetCarriedItemPacket implements Packet<ServerGamePacketListener> {
    private final int slot;

    public ServerboundSetCarriedItemPacket(int param0) {
        this.slot = param0;
    }

    public ServerboundSetCarriedItemPacket(FriendlyByteBuf param0) {
        this.slot = param0.readShort();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeShort(this.slot);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleSetCarriedItem(this);
    }

    public int getSlot() {
        return this.slot;
    }
}
