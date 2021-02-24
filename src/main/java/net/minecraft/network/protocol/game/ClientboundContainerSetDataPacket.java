package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundContainerSetDataPacket implements Packet<ClientGamePacketListener> {
    private final int containerId;
    private final int id;
    private final int value;

    public ClientboundContainerSetDataPacket(int param0, int param1, int param2) {
        this.containerId = param0;
        this.id = param1;
        this.value = param2;
    }

    public ClientboundContainerSetDataPacket(FriendlyByteBuf param0) {
        this.containerId = param0.readUnsignedByte();
        this.id = param0.readShort();
        this.value = param0.readShort();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeByte(this.containerId);
        param0.writeShort(this.id);
        param0.writeShort(this.value);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleContainerSetData(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getContainerId() {
        return this.containerId;
    }

    @OnlyIn(Dist.CLIENT)
    public int getId() {
        return this.id;
    }

    @OnlyIn(Dist.CLIENT)
    public int getValue() {
        return this.value;
    }
}
