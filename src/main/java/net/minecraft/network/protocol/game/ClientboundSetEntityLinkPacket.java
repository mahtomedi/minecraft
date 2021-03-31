package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class ClientboundSetEntityLinkPacket implements Packet<ClientGamePacketListener> {
    private final int sourceId;
    private final int destId;

    public ClientboundSetEntityLinkPacket(Entity param0, @Nullable Entity param1) {
        this.sourceId = param0.getId();
        this.destId = param1 != null ? param1.getId() : 0;
    }

    public ClientboundSetEntityLinkPacket(FriendlyByteBuf param0) {
        this.sourceId = param0.readInt();
        this.destId = param0.readInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeInt(this.sourceId);
        param0.writeInt(this.destId);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleEntityLinkPacket(this);
    }

    public int getSourceId() {
        return this.sourceId;
    }

    public int getDestId() {
        return this.destId;
    }
}
