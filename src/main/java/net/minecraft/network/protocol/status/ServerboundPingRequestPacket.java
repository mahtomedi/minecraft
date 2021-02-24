package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundPingRequestPacket implements Packet<ServerStatusPacketListener> {
    private final long time;

    @OnlyIn(Dist.CLIENT)
    public ServerboundPingRequestPacket(long param0) {
        this.time = param0;
    }

    public ServerboundPingRequestPacket(FriendlyByteBuf param0) {
        this.time = param0.readLong();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeLong(this.time);
    }

    public void handle(ServerStatusPacketListener param0) {
        param0.handlePingRequest(this);
    }

    public long getTime() {
        return this.time;
    }
}
