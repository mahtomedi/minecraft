package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ClientboundAddMobPacket implements Packet<ClientGamePacketListener> {
    private final int id;
    private final UUID uuid;
    private final int type;
    private final double x;
    private final double y;
    private final double z;
    private final int xd;
    private final int yd;
    private final int zd;
    private final byte yRot;
    private final byte xRot;
    private final byte yHeadRot;

    public ClientboundAddMobPacket(LivingEntity param0) {
        this.id = param0.getId();
        this.uuid = param0.getUUID();
        this.type = Registry.ENTITY_TYPE.getId(param0.getType());
        this.x = param0.getX();
        this.y = param0.getY();
        this.z = param0.getZ();
        this.yRot = (byte)((int)(param0.getYRot() * 256.0F / 360.0F));
        this.xRot = (byte)((int)(param0.getXRot() * 256.0F / 360.0F));
        this.yHeadRot = (byte)((int)(param0.yHeadRot * 256.0F / 360.0F));
        double var0 = 3.9;
        Vec3 var1 = param0.getDeltaMovement();
        double var2 = Mth.clamp(var1.x, -3.9, 3.9);
        double var3 = Mth.clamp(var1.y, -3.9, 3.9);
        double var4 = Mth.clamp(var1.z, -3.9, 3.9);
        this.xd = (int)(var2 * 8000.0);
        this.yd = (int)(var3 * 8000.0);
        this.zd = (int)(var4 * 8000.0);
    }

    public ClientboundAddMobPacket(FriendlyByteBuf param0) {
        this.id = param0.readVarInt();
        this.uuid = param0.readUUID();
        this.type = param0.readVarInt();
        this.x = param0.readDouble();
        this.y = param0.readDouble();
        this.z = param0.readDouble();
        this.yRot = param0.readByte();
        this.xRot = param0.readByte();
        this.yHeadRot = param0.readByte();
        this.xd = param0.readShort();
        this.yd = param0.readShort();
        this.zd = param0.readShort();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.id);
        param0.writeUUID(this.uuid);
        param0.writeVarInt(this.type);
        param0.writeDouble(this.x);
        param0.writeDouble(this.y);
        param0.writeDouble(this.z);
        param0.writeByte(this.yRot);
        param0.writeByte(this.xRot);
        param0.writeByte(this.yHeadRot);
        param0.writeShort(this.xd);
        param0.writeShort(this.yd);
        param0.writeShort(this.zd);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleAddMob(this);
    }

    public int getId() {
        return this.id;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public int getType() {
        return this.type;
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

    public int getXd() {
        return this.xd;
    }

    public int getYd() {
        return this.yd;
    }

    public int getZd() {
        return this.zd;
    }

    public byte getyRot() {
        return this.yRot;
    }

    public byte getxRot() {
        return this.xRot;
    }

    public byte getyHeadRot() {
        return this.yHeadRot;
    }
}
