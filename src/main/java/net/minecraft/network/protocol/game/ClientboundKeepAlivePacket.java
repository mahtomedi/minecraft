package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundKeepAlivePacket implements Packet<ClientGamePacketListener> {
    private long id;

    public ClientboundKeepAlivePacket() {
    }

    public ClientboundKeepAlivePacket(long param0) {
        this.id = param0;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleKeepAlive(this);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.id = param0.readLong();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeLong(this.id);
    }

    @OnlyIn(Dist.CLIENT)
    public long getId() {
        return this.id;
    }
}
