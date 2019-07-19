package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundContainerAckPacket implements Packet<ClientGamePacketListener> {
    private int containerId;
    private short uid;
    private boolean accepted;

    public ClientboundContainerAckPacket() {
    }

    public ClientboundContainerAckPacket(int param0, short param1, boolean param2) {
        this.containerId = param0;
        this.uid = param1;
        this.accepted = param2;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleContainerAck(this);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.containerId = param0.readUnsignedByte();
        this.uid = param0.readShort();
        this.accepted = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeByte(this.containerId);
        param0.writeShort(this.uid);
        param0.writeBoolean(this.accepted);
    }

    @OnlyIn(Dist.CLIENT)
    public int getContainerId() {
        return this.containerId;
    }

    @OnlyIn(Dist.CLIENT)
    public short getUid() {
        return this.uid;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isAccepted() {
        return this.accepted;
    }
}
