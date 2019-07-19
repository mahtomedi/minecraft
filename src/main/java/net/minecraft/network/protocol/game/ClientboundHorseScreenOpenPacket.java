package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundHorseScreenOpenPacket implements Packet<ClientGamePacketListener> {
    private int containerId;
    private int size;
    private int entityId;

    public ClientboundHorseScreenOpenPacket() {
    }

    public ClientboundHorseScreenOpenPacket(int param0, int param1, int param2) {
        this.containerId = param0;
        this.size = param1;
        this.entityId = param2;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleHorseScreenOpen(this);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.containerId = param0.readUnsignedByte();
        this.size = param0.readVarInt();
        this.entityId = param0.readInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeByte(this.containerId);
        param0.writeVarInt(this.size);
        param0.writeInt(this.entityId);
    }

    @OnlyIn(Dist.CLIENT)
    public int getContainerId() {
        return this.containerId;
    }

    @OnlyIn(Dist.CLIENT)
    public int getSize() {
        return this.size;
    }

    @OnlyIn(Dist.CLIENT)
    public int getEntityId() {
        return this.entityId;
    }
}
