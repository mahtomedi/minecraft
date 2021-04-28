package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;

public class ClientboundAddPlayerPacket implements Packet<ClientGamePacketListener> {
    private final int entityId;
    private final UUID playerId;
    private final double x;
    private final double y;
    private final double z;
    private final byte yRot;
    private final byte xRot;

    public ClientboundAddPlayerPacket(Player param0) {
        this.entityId = param0.getId();
        this.playerId = param0.getGameProfile().getId();
        this.x = param0.getX();
        this.y = param0.getY();
        this.z = param0.getZ();
        this.yRot = (byte)((int)(param0.getYRot() * 256.0F / 360.0F));
        this.xRot = (byte)((int)(param0.getXRot() * 256.0F / 360.0F));
    }

    public ClientboundAddPlayerPacket(FriendlyByteBuf param0) {
        this.entityId = param0.readVarInt();
        this.playerId = param0.readUUID();
        this.x = param0.readDouble();
        this.y = param0.readDouble();
        this.z = param0.readDouble();
        this.yRot = param0.readByte();
        this.xRot = param0.readByte();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.entityId);
        param0.writeUUID(this.playerId);
        param0.writeDouble(this.x);
        param0.writeDouble(this.y);
        param0.writeDouble(this.z);
        param0.writeByte(this.yRot);
        param0.writeByte(this.xRot);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleAddPlayer(this);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public byte getyRot() {
        return this.yRot;
    }

    public byte getxRot() {
        return this.xRot;
    }
}
