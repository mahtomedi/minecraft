package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderCenterPacket implements Packet<ClientGamePacketListener> {
    private final double newCenterX;
    private final double newCenterZ;

    public ClientboundSetBorderCenterPacket(WorldBorder param0) {
        this.newCenterX = param0.getCenterX();
        this.newCenterZ = param0.getCenterZ();
    }

    public ClientboundSetBorderCenterPacket(FriendlyByteBuf param0) {
        this.newCenterX = param0.readDouble();
        this.newCenterZ = param0.readDouble();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeDouble(this.newCenterX);
        param0.writeDouble(this.newCenterZ);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetBorderCenter(this);
    }

    public double getNewCenterZ() {
        return this.newCenterZ;
    }

    public double getNewCenterX() {
        return this.newCenterX;
    }
}
