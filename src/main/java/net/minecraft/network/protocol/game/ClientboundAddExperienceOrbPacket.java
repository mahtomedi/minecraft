package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundAddExperienceOrbPacket implements Packet<ClientGamePacketListener> {
    private int id;
    private double x;
    private double y;
    private double z;
    private int value;

    public ClientboundAddExperienceOrbPacket() {
    }

    public ClientboundAddExperienceOrbPacket(ExperienceOrb param0) {
        this.id = param0.getId();
        this.x = param0.x;
        this.y = param0.y;
        this.z = param0.z;
        this.value = param0.getValue();
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.id = param0.readVarInt();
        this.x = param0.readDouble();
        this.y = param0.readDouble();
        this.z = param0.readDouble();
        this.value = param0.readShort();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.id);
        param0.writeDouble(this.x);
        param0.writeDouble(this.y);
        param0.writeDouble(this.z);
        param0.writeShort(this.value);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleAddExperienceOrb(this);
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
    public int getValue() {
        return this.value;
    }
}
