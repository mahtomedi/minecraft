package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundAddPlayerPacket implements Packet<ClientGamePacketListener> {
    private int entityId;
    private UUID playerId;
    private double x;
    private double y;
    private double z;
    private byte yRot;
    private byte xRot;

    public ClientboundAddPlayerPacket() {
    }

    public ClientboundAddPlayerPacket(Player param0) {
        this.entityId = param0.getId();
        this.playerId = param0.getGameProfile().getId();
        this.x = param0.x;
        this.y = param0.y;
        this.z = param0.z;
        this.yRot = (byte)((int)(param0.yRot * 256.0F / 360.0F));
        this.xRot = (byte)((int)(param0.xRot * 256.0F / 360.0F));
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.entityId = param0.readVarInt();
        this.playerId = param0.readUUID();
        this.x = param0.readDouble();
        this.y = param0.readDouble();
        this.z = param0.readDouble();
        this.yRot = param0.readByte();
        this.xRot = param0.readByte();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.entityId);
        param0.writeUUID(this.playerId);
        param0.writeDouble(this.x);
        param0.writeDouble(this.y);
        param0.writeDouble(this.z);
        param0.writeByte(this.yRot);
        param0.writeByte(this.xRot);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleAddPlayer(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getEntityId() {
        return this.entityId;
    }

    @OnlyIn(Dist.CLIENT)
    public UUID getPlayerId() {
        return this.playerId;
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
    public byte getyRot() {
        return this.yRot;
    }

    @OnlyIn(Dist.CLIENT)
    public byte getxRot() {
        return this.xRot;
    }
}
