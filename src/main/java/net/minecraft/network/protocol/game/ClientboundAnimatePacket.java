package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundAnimatePacket implements Packet<ClientGamePacketListener> {
    private final int id;
    private final int action;

    public ClientboundAnimatePacket(Entity param0, int param1) {
        this.id = param0.getId();
        this.action = param1;
    }

    public ClientboundAnimatePacket(FriendlyByteBuf param0) {
        this.id = param0.readVarInt();
        this.action = param0.readUnsignedByte();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
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
