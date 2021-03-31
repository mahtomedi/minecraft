package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.ExperienceOrb;

public class ClientboundAddExperienceOrbPacket implements Packet<ClientGamePacketListener> {
    private final int id;
    private final double x;
    private final double y;
    private final double z;
    private final int value;

    public ClientboundAddExperienceOrbPacket(ExperienceOrb param0) {
        this.id = param0.getId();
        this.x = param0.getX();
        this.y = param0.getY();
        this.z = param0.getZ();
        this.value = param0.getValue();
    }

    public ClientboundAddExperienceOrbPacket(FriendlyByteBuf param0) {
        this.id = param0.readVarInt();
        this.x = param0.readDouble();
        this.y = param0.readDouble();
        this.z = param0.readDouble();
        this.value = param0.readShort();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.id);
        param0.writeDouble(this.x);
        param0.writeDouble(this.y);
        param0.writeDouble(this.z);
        param0.writeShort(this.value);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleAddExperienceOrb(this);
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

    public int getValue() {
        return this.value;
    }
}
