package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundSetCarriedItemPacket implements Packet<ServerGamePacketListener> {
    private int slot;

    public ServerboundSetCarriedItemPacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundSetCarriedItemPacket(int param0) {
        this.slot = param0;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.slot = param0.readShort();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeShort(this.slot);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleSetCarriedItem(this);
    }

    public int getSlot() {
        return this.slot;
    }
}
