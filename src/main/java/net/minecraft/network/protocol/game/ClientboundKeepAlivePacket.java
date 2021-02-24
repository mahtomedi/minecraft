package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundKeepAlivePacket implements Packet<ClientGamePacketListener> {
    private final long id;

    public ClientboundKeepAlivePacket(long param0) {
        this.id = param0;
    }

    public ClientboundKeepAlivePacket(FriendlyByteBuf param0) {
        this.id = param0.readLong();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeLong(this.id);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleKeepAlive(this);
    }

    @OnlyIn(Dist.CLIENT)
    public long getId() {
        return this.id;
    }
}
