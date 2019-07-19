package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundKeepAlivePacket implements Packet<ServerGamePacketListener> {
    private long id;

    public ServerboundKeepAlivePacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundKeepAlivePacket(long param0) {
        this.id = param0;
    }

    public void handle(ServerGamePacketListener param0) {
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

    public long getId() {
        return this.id;
    }
}
