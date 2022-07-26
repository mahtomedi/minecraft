package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetCarriedItemPacket implements Packet<ClientGamePacketListener> {
    private final int slot;

    public ClientboundSetCarriedItemPacket(int param0) {
        this.slot = param0;
    }

    public ClientboundSetCarriedItemPacket(FriendlyByteBuf param0) {
        this.slot = param0.readByte();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeByte(this.slot);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetCarriedItem(this);
    }

    public int getSlot() {
        return this.slot;
    }
}
