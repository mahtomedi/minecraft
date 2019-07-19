package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundRotateHeadPacket implements Packet<ClientGamePacketListener> {
    private int entityId;
    private byte yHeadRot;

    public ClientboundRotateHeadPacket() {
    }

    public ClientboundRotateHeadPacket(Entity param0, byte param1) {
        this.entityId = param0.getId();
        this.yHeadRot = param1;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.entityId = param0.readVarInt();
        this.yHeadRot = param0.readByte();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.entityId);
        param0.writeByte(this.yHeadRot);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleRotateMob(this);
    }

    @OnlyIn(Dist.CLIENT)
    public Entity getEntity(Level param0) {
        return param0.getEntity(this.entityId);
    }

    @OnlyIn(Dist.CLIENT)
    public byte getYHeadRot() {
        return this.yHeadRot;
    }
}
