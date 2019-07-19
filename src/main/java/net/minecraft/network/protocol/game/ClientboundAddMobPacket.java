package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundAddMobPacket implements Packet<ClientGamePacketListener> {
    private int id;
    private UUID uuid;
    private int type;
    private double x;
    private double y;
    private double z;
    private int xd;
    private int yd;
    private int zd;
    private byte yRot;
    private byte xRot;
    private byte yHeadRot;
    private SynchedEntityData entityData;
    private List<SynchedEntityData.DataItem<?>> unpack;

    public ClientboundAddMobPacket() {
    }

    public ClientboundAddMobPacket(LivingEntity param0) {
        this.id = param0.getId();
        this.uuid = param0.getUUID();
        this.type = Registry.ENTITY_TYPE.getId(param0.getType());
        this.x = param0.x;
        this.y = param0.y;
        this.z = param0.z;
        this.yRot = (byte)((int)(param0.yRot * 256.0F / 360.0F));
        this.xRot = (byte)((int)(param0.xRot * 256.0F / 360.0F));
        this.yHeadRot = (byte)((int)(param0.yHeadRot * 256.0F / 360.0F));
        double var0 = 3.9;
        Vec3 var1 = param0.getDeltaMovement();
        double var2 = Mth.clamp(var1.x, -3.9, 3.9);
        double var3 = Mth.clamp(var1.y, -3.9, 3.9);
        double var4 = Mth.clamp(var1.z, -3.9, 3.9);
        this.xd = (int)(var2 * 8000.0);
        this.yd = (int)(var3 * 8000.0);
        this.zd = (int)(var4 * 8000.0);
        this.entityData = param0.getEntityData();
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
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
        this.unpack = SynchedEntityData.unpack(param0);
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
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
        this.entityData.packAll(param0);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleAddMob(this);
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public List<SynchedEntityData.DataItem<?>> getUnpackedData() {
        return this.unpack;
    }

    @OnlyIn(Dist.CLIENT)
    public int getId() {
        return this.id;
    }

    @OnlyIn(Dist.CLIENT)
    public UUID getUUID() {
        return this.uuid;
    }

    @OnlyIn(Dist.CLIENT)
    public int getType() {
        return this.type;
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
    public int getXd() {
        return this.xd;
    }

    @OnlyIn(Dist.CLIENT)
    public int getYd() {
        return this.yd;
    }

    @OnlyIn(Dist.CLIENT)
    public int getZd() {
        return this.zd;
    }

    @OnlyIn(Dist.CLIENT)
    public byte getyRot() {
        return this.yRot;
    }

    @OnlyIn(Dist.CLIENT)
    public byte getxRot() {
        return this.xRot;
    }

    @OnlyIn(Dist.CLIENT)
    public byte getyHeadRot() {
        return this.yHeadRot;
    }
}
