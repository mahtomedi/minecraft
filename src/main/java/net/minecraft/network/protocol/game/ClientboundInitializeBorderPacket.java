package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundInitializeBorderPacket implements Packet<ClientGamePacketListener> {
    private final double newCenterX;
    private final double newCenterZ;
    private final double oldSize;
    private final double newSize;
    private final long lerpTime;
    private final int newAbsoluteMaxSize;
    private final int warningBlocks;
    private final int warningTime;

    public ClientboundInitializeBorderPacket(FriendlyByteBuf param0) {
        this.newCenterX = param0.readDouble();
        this.newCenterZ = param0.readDouble();
        this.oldSize = param0.readDouble();
        this.newSize = param0.readDouble();
        this.lerpTime = param0.readVarLong();
        this.newAbsoluteMaxSize = param0.readVarInt();
        this.warningBlocks = param0.readVarInt();
        this.warningTime = param0.readVarInt();
    }

    public ClientboundInitializeBorderPacket(WorldBorder param0) {
        this.newCenterX = param0.getCenterX();
        this.newCenterZ = param0.getCenterZ();
        this.oldSize = param0.getSize();
        this.newSize = param0.getLerpTarget();
        this.lerpTime = param0.getLerpRemainingTime();
        this.newAbsoluteMaxSize = param0.getAbsoluteMaxSize();
        this.warningBlocks = param0.getWarningBlocks();
        this.warningTime = param0.getWarningTime();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeDouble(this.newCenterX);
        param0.writeDouble(this.newCenterZ);
        param0.writeDouble(this.oldSize);
        param0.writeDouble(this.newSize);
        param0.writeVarLong(this.lerpTime);
        param0.writeVarInt(this.newAbsoluteMaxSize);
        param0.writeVarInt(this.warningBlocks);
        param0.writeVarInt(this.warningTime);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleInitializeBorder(this);
    }

    @OnlyIn(Dist.CLIENT)
    public double getNewCenterX() {
        return this.newCenterX;
    }

    @OnlyIn(Dist.CLIENT)
    public double getNewCenterZ() {
        return this.newCenterZ;
    }

    @OnlyIn(Dist.CLIENT)
    public double getNewSize() {
        return this.newSize;
    }

    @OnlyIn(Dist.CLIENT)
    public double getOldSize() {
        return this.oldSize;
    }

    @OnlyIn(Dist.CLIENT)
    public long getLerpTime() {
        return this.lerpTime;
    }

    @OnlyIn(Dist.CLIENT)
    public int getNewAbsoluteMaxSize() {
        return this.newAbsoluteMaxSize;
    }

    @OnlyIn(Dist.CLIENT)
    public int getWarningTime() {
        return this.warningTime;
    }

    @OnlyIn(Dist.CLIENT)
    public int getWarningBlocks() {
        return this.warningBlocks;
    }
}
