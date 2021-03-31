package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderLerpSizePacket implements Packet<ClientGamePacketListener> {
    private final double oldSize;
    private final double newSize;
    private final long lerpTime;

    public ClientboundSetBorderLerpSizePacket(WorldBorder param0) {
        this.oldSize = param0.getSize();
        this.newSize = param0.getLerpTarget();
        this.lerpTime = param0.getLerpRemainingTime();
    }

    public ClientboundSetBorderLerpSizePacket(FriendlyByteBuf param0) {
        this.oldSize = param0.readDouble();
        this.newSize = param0.readDouble();
        this.lerpTime = param0.readVarLong();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeDouble(this.oldSize);
        param0.writeDouble(this.newSize);
        param0.writeVarLong(this.lerpTime);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetBorderLerpSize(this);
    }

    public double getOldSize() {
        return this.oldSize;
    }

    public double getNewSize() {
        return this.newSize;
    }

    public long getLerpTime() {
        return this.lerpTime;
    }
}
