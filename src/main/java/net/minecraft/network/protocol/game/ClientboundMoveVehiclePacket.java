package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundMoveVehiclePacket implements Packet<ClientGamePacketListener> {
    private final double x;
    private final double y;
    private final double z;
    private final float yRot;
    private final float xRot;

    public ClientboundMoveVehiclePacket(Entity param0) {
        this.x = param0.getX();
        this.y = param0.getY();
        this.z = param0.getZ();
        this.yRot = param0.yRot;
        this.xRot = param0.xRot;
    }

    public ClientboundMoveVehiclePacket(FriendlyByteBuf param0) {
        this.x = param0.readDouble();
        this.y = param0.readDouble();
        this.z = param0.readDouble();
        this.yRot = param0.readFloat();
        this.xRot = param0.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeDouble(this.x);
        param0.writeDouble(this.y);
        param0.writeDouble(this.z);
        param0.writeFloat(this.yRot);
        param0.writeFloat(this.xRot);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleMoveVehicle(this);
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
    public float getYRot() {
        return this.yRot;
    }

    @OnlyIn(Dist.CLIENT)
    public float getXRot() {
        return this.xRot;
    }
}
