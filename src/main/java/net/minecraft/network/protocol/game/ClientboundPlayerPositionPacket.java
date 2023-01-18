package net.minecraft.network.protocol.game;

import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.RelativeMovement;

public class ClientboundPlayerPositionPacket implements Packet<ClientGamePacketListener> {
    private final double x;
    private final double y;
    private final double z;
    private final float yRot;
    private final float xRot;
    private final Set<RelativeMovement> relativeArguments;
    private final int id;
    private final boolean dismountVehicle;

    public ClientboundPlayerPositionPacket(
        double param0, double param1, double param2, float param3, float param4, Set<RelativeMovement> param5, int param6, boolean param7
    ) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
        this.yRot = param3;
        this.xRot = param4;
        this.relativeArguments = param5;
        this.id = param6;
        this.dismountVehicle = param7;
    }

    public ClientboundPlayerPositionPacket(FriendlyByteBuf param0) {
        this.x = param0.readDouble();
        this.y = param0.readDouble();
        this.z = param0.readDouble();
        this.yRot = param0.readFloat();
        this.xRot = param0.readFloat();
        this.relativeArguments = RelativeMovement.unpack(param0.readUnsignedByte());
        this.id = param0.readVarInt();
        this.dismountVehicle = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeDouble(this.x);
        param0.writeDouble(this.y);
        param0.writeDouble(this.z);
        param0.writeFloat(this.yRot);
        param0.writeFloat(this.xRot);
        param0.writeByte(RelativeMovement.pack(this.relativeArguments));
        param0.writeVarInt(this.id);
        param0.writeBoolean(this.dismountVehicle);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleMovePlayer(this);
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

    public float getYRot() {
        return this.yRot;
    }

    public float getXRot() {
        return this.xRot;
    }

    public int getId() {
        return this.id;
    }

    public boolean requestDismountVehicle() {
        return this.dismountVehicle;
    }

    public Set<RelativeMovement> getRelativeArguments() {
        return this.relativeArguments;
    }
}
