package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetCarriedItemPacket implements Packet<ClientGamePacketListener> {
    private int slot;

    public ClientboundSetCarriedItemPacket() {
    }

    public ClientboundSetCarriedItemPacket(int param0) {
        this.slot = param0;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.slot = param0.readByte();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeByte(this.slot);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetCarriedItem(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getSlot() {
        return this.slot;
    }
}
