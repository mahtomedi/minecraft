package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class ClientboundTeleportEntityPacket implements Packet<ClientGamePacketListener> {
    private final int id;
    private final double x;
    private final double y;
    private final double z;
    private final byte yRot;
    private final byte xRot;
    private final boolean onGround;

    public ClientboundTeleportEntityPacket(Entity param0) {
        this.id = param0.getId();
        Vec3 var0 = param0.trackingPosition();
        this.x = var0.x;
        this.y = var0.y;
        this.z = var0.z;
        this.yRot = (byte)((int)(param0.getYRot() * 256.0F / 360.0F));
        this.xRot = (byte)((int)(param0.getXRot() * 256.0F / 360.0F));
        this.onGround = param0.onGround();
    }

    public ClientboundTeleportEntityPacket(FriendlyByteBuf param0) {
        this.id = param0.readVarInt();
        this.x = param0.readDouble();
        this.y = param0.readDouble();
        this.z = param0.readDouble();
        this.yRot = param0.readByte();
        this.xRot = param0.readByte();
        this.onGround = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.id);
        param0.writeDouble(this.x);
        param0.writeDouble(this.y);
        param0.writeDouble(this.z);
        param0.writeByte(this.yRot);
        param0.writeByte(this.xRot);
        param0.writeBoolean(this.onGround);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleTeleportEntity(this);
    }

    public int getId() {
        return this.id;
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

    public boolean isOnGround() {
        return this.onGround;
    }
}
