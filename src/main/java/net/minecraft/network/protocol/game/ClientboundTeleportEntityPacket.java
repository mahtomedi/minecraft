package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
        this.x = param0.getX();
        this.y = param0.getY();
        this.z = param0.getZ();
        this.yRot = (byte)((int)(param0.yRot * 256.0F / 360.0F));
        this.xRot = (byte)((int)(param0.xRot * 256.0F / 360.0F));
        this.onGround = param0.isOnGround();
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

    @OnlyIn(Dist.CLIENT)
    public int getId() {
        return this.id;
    }

    @OnlyIn(Dist.CLIENT)
    public double getX() {
        return this.x;
    }

    @OnlyIn(Dist.CLIENT)
    public double getY() {
        return this.y;
    }

    @OnlyIn(Dist.CLIENT)
    public double getZ() {
        return this.z;
    }

    @OnlyIn(Dist.CLIENT)
    public byte getyRot() {
        return this.yRot;
    }

    @OnlyIn(Dist.CLIENT)
    public byte getxRot() {
        return this.xRot;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isOnGround() {
        return this.onGround;
    }
}
