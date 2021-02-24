package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundContainerClosePacket implements Packet<ServerGamePacketListener> {
    private final int containerId;

    @OnlyIn(Dist.CLIENT)
    public ServerboundContainerClosePacket(int param0) {
        this.containerId = param0;
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleContainerClose(this);
    }

    public ServerboundContainerClosePacket(FriendlyByteBuf param0) {
        this.containerId = param0.readByte();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeByte(this.containerId);
    }
}
