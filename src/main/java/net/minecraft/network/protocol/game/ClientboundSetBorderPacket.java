package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetBorderPacket implements Packet<ClientGamePacketListener> {
    private ClientboundSetBorderPacket.Type type;
    private int newAbsoluteMaxSize;
    private double newCenterX;
    private double newCenterZ;
    private double newSize;
    private double oldSize;
    private long lerpTime;
    private int warningTime;
    private int warningBlocks;

    public ClientboundSetBorderPacket() {
    }

    public ClientboundSetBorderPacket(WorldBorder param0, ClientboundSetBorderPacket.Type param1) {
        this.type = param1;
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
    public void read(FriendlyByteBuf param0) throws IOException {
        this.type = param0.readEnum(ClientboundSetBorderPacket.Type.class);
        switch(this.type) {
            case SET_SIZE:
                this.newSize = param0.readDouble();
                break;
            case LERP_SIZE:
                this.oldSize = param0.readDouble();
                this.newSize = param0.readDouble();
                this.lerpTime = param0.readVarLong();
                break;
            case SET_CENTER:
                this.newCenterX = param0.readDouble();
                this.newCenterZ = param0.readDouble();
                break;
            case SET_WARNING_BLOCKS:
                this.warningBlocks = param0.readVarInt();
                break;
            case SET_WARNING_TIME:
                this.warningTime = param0.readVarInt();
                break;
            case INITIALIZE:
                this.newCenterX = param0.readDouble();
                this.newCenterZ = param0.readDouble();
                this.oldSize = param0.readDouble();
                this.newSize = param0.readDouble();
                this.lerpTime = param0.readVarLong();
                this.newAbsoluteMaxSize = param0.readVarInt();
                this.warningBlocks = param0.readVarInt();
                this.warningTime = param0.readVarInt();
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeEnum(this.type);
        switch(this.type) {
            case SET_SIZE:
                param0.writeDouble(this.newSize);
                break;
            case LERP_SIZE:
                param0.writeDouble(this.oldSize);
                param0.writeDouble(this.newSize);
                param0.writeVarLong(this.lerpTime);
                break;
            case SET_CENTER:
                param0.writeDouble(this.newCenterX);
                param0.writeDouble(this.newCenterZ);
                break;
            case SET_WARNING_BLOCKS:
                param0.writeVarInt(this.warningBlocks);
                break;
            case SET_WARNING_TIME:
                param0.writeVarInt(this.warningTime);
                break;
            case INITIALIZE:
                param0.writeDouble(this.newCenterX);
                param0.writeDouble(this.newCenterZ);
                param0.writeDouble(this.oldSize);
                param0.writeDouble(this.newSize);
                param0.writeVarLong(this.lerpTime);
                param0.writeVarInt(this.newAbsoluteMaxSize);
                param0.writeVarInt(this.warningBlocks);
                param0.writeVarInt(this.warningTime);
        }

    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetBorder(this);
    }

    @OnlyIn(Dist.CLIENT)
    public void applyChanges(WorldBorder param0) {
        switch(this.type) {
            case SET_SIZE:
                param0.setSize(this.newSize);
                break;
            case LERP_SIZE:
                param0.lerpSizeBetween(this.oldSize, this.newSize, this.lerpTime);
                break;
            case SET_CENTER:
                param0.setCenter(this.newCenterX, this.newCenterZ);
                break;
            case SET_WARNING_BLOCKS:
                param0.setWarningBlocks(this.warningBlocks);
                break;
            case SET_WARNING_TIME:
                param0.setWarningTime(this.warningTime);
                break;
            case INITIALIZE:
                param0.setCenter(this.newCenterX, this.newCenterZ);
                if (this.lerpTime > 0L) {
                    param0.lerpSizeBetween(this.oldSize, this.newSize, this.lerpTime);
                } else {
                    param0.setSize(this.newSize);
                }

                param0.setAbsoluteMaxSize(this.newAbsoluteMaxSize);
                param0.setWarningBlocks(this.warningBlocks);
                param0.setWarningTime(this.warningTime);
        }

    }

    public static enum Type {
        SET_SIZE,
        LERP_SIZE,
        SET_CENTER,
        INITIALIZE,
        SET_WARNING_TIME,
        SET_WARNING_BLOCKS;
    }
}
