package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundContainerClosePacket implements Packet<ServerGamePacketListener> {
    private int containerId;

    public ServerboundContainerClosePacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundContainerClosePacket(int param0) {
        this.containerId = param0;
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleContainerClose(this);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.containerId = param0.readByte();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeByte(this.containerId);
    }
}
