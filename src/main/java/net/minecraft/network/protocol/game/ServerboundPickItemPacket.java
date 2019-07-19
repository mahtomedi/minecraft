package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundPickItemPacket implements Packet<ServerGamePacketListener> {
    private int slot;

    public ServerboundPickItemPacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundPickItemPacket(int param0) {
        this.slot = param0;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.slot = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.slot);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handlePickItem(this);
    }

    public int getSlot() {
        return this.slot;
    }
}
