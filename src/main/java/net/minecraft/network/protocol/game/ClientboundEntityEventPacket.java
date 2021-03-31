package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundEntityEventPacket implements Packet<ClientGamePacketListener> {
    private final int entityId;
    private final byte eventId;

    public ClientboundEntityEventPacket(Entity param0, byte param1) {
        this.entityId = param0.getId();
        this.eventId = param1;
    }

    public ClientboundEntityEventPacket(FriendlyByteBuf param0) {
        this.entityId = param0.readInt();
        this.eventId = param0.readByte();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeInt(this.entityId);
        param0.writeByte(this.eventId);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleEntityEvent(this);
    }

    @Nullable
    public Entity getEntity(Level param0) {
        return param0.getEntity(this.entityId);
    }

    public byte getEventId() {
        return this.eventId;
    }
}
