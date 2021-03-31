package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundRotateHeadPacket implements Packet<ClientGamePacketListener> {
    private final int entityId;
    private final byte yHeadRot;

    public ClientboundRotateHeadPacket(Entity param0, byte param1) {
        this.entityId = param0.getId();
        this.yHeadRot = param1;
    }

    public ClientboundRotateHeadPacket(FriendlyByteBuf param0) {
        this.entityId = param0.readVarInt();
        this.yHeadRot = param0.readByte();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.entityId);
        param0.writeByte(this.yHeadRot);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleRotateMob(this);
    }

    public Entity getEntity(Level param0) {
        return param0.getEntity(this.entityId);
    }

    public byte getYHeadRot() {
        return this.yHeadRot;
    }
}
