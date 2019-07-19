package net.minecraft.network.protocol.login;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundLoginCompressionPacket implements Packet<ClientLoginPacketListener> {
    private int compressionThreshold;

    public ClientboundLoginCompressionPacket() {
    }

    public ClientboundLoginCompressionPacket(int param0) {
        this.compressionThreshold = param0;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.compressionThreshold = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.compressionThreshold);
    }

    public void handle(ClientLoginPacketListener param0) {
        param0.handleCompression(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getCompressionThreshold() {
        return this.compressionThreshold;
    }
}
