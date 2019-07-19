package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundAnimatePacket implements Packet<ClientGamePacketListener> {
    private int id;
    private int action;

    public ClientboundAnimatePacket() {
    }

    public ClientboundAnimatePacket(Entity param0, int param1) {
        this.id = param0.getId();
        this.action = param1;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.id = param0.readVarInt();
        this.action = param0.readUnsignedByte();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.id);
        param0.writeByte(this.action);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleAnimate(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getId() {
        return this.id;
    }

    @OnlyIn(Dist.CLIENT)
    public int getAction() {
        return this.action;
    }
}
