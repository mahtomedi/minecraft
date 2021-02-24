package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundContainerAckPacket implements Packet<ServerGamePacketListener> {
    private final int containerId;
    private final short uid;
    private final boolean accepted;

    @OnlyIn(Dist.CLIENT)
    public ServerboundContainerAckPacket(int param0, short param1, boolean param2) {
        this.containerId = param0;
        this.uid = param1;
        this.accepted = param2;
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleContainerAck(this);
    }

    public ServerboundContainerAckPacket(FriendlyByteBuf param0) {
        this.containerId = param0.readByte();
        this.uid = param0.readShort();
        this.accepted = param0.readByte() != 0;
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeByte(this.containerId);
        param0.writeShort(this.uid);
        param0.writeByte(this.accepted ? 1 : 0);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public short getUid() {
        return this.uid;
    }
}
