package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.global.LightningBolt;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundAddGlobalEntityPacket implements Packet<ClientGamePacketListener> {
    private int id;
    private double x;
    private double y;
    private double z;
    private int type;

    public ClientboundAddGlobalEntityPacket() {
    }

    public ClientboundAddGlobalEntityPacket(Entity param0) {
        this.id = param0.getId();
        this.x = param0.getX();
        this.y = param0.getY();
        this.z = param0.getZ();
        if (param0 instanceof LightningBolt) {
            this.type = 1;
        }

    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.id = param0.readVarInt();
        this.type = param0.readByte();
        this.x = param0.readDouble();
        this.y = param0.readDouble();
        this.z = param0.readDouble();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.id);
        param0.writeByte(this.type);
        param0.writeDouble(this.x);
        param0.writeDouble(this.y);
        param0.writeDouble(this.z);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleAddGlobalEntity(this);
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
    public int getType() {
        return this.type;
    }
}
