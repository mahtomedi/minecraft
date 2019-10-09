package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundAddEntityPacket implements Packet<ClientGamePacketListener> {
    private int id;
    private UUID uuid;
    private double x;
    private double y;
    private double z;
    private int xa;
    private int ya;
    private int za;
    private int xRot;
    private int yRot;
    private EntityType<?> type;
    private int data;

    public ClientboundAddEntityPacket() {
    }

    public ClientboundAddEntityPacket(
        int param0, UUID param1, double param2, double param3, double param4, float param5, float param6, EntityType<?> param7, int param8, Vec3 param9
    ) {
        this.id = param0;
        this.uuid = param1;
        this.x = param2;
        this.y = param3;
        this.z = param4;
        this.xRot = Mth.floor(param5 * 256.0F / 360.0F);
        this.yRot = Mth.floor(param6 * 256.0F / 360.0F);
        this.type = param7;
        this.data = param8;
        this.xa = (int)(Mth.clamp(param9.x, -3.9, 3.9) * 8000.0);
        this.ya = (int)(Mth.clamp(param9.y, -3.9, 3.9) * 8000.0);
        this.za = (int)(Mth.clamp(param9.z, -3.9, 3.9) * 8000.0);
    }

    public ClientboundAddEntityPacket(Entity param0) {
        this(param0, 0);
    }

    public ClientboundAddEntityPacket(Entity param0, int param1) {
        this(
            param0.getId(),
            param0.getUUID(),
            param0.getX(),
            param0.getY(),
            param0.getZ(),
            param0.xRot,
            param0.yRot,
            param0.getType(),
            param1,
            param0.getDeltaMovement()
        );
    }

    public ClientboundAddEntityPacket(Entity param0, EntityType<?> param1, int param2, BlockPos param3) {
        this(
            param0.getId(),
            param0.getUUID(),
            (double)param3.getX(),
            (double)param3.getY(),
            (double)param3.getZ(),
            param0.xRot,
            param0.yRot,
            param1,
            param2,
            param0.getDeltaMovement()
        );
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.id = param0.readVarInt();
        this.uuid = param0.readUUID();
        this.type = Registry.ENTITY_TYPE.byId(param0.readVarInt());
        this.x = param0.readDouble();
        this.y = param0.readDouble();
        this.z = param0.readDouble();
        this.xRot = param0.readByte();
        this.yRot = param0.readByte();
        this.data = param0.readInt();
        this.xa = param0.readShort();
        this.ya = param0.readShort();
        this.za = param0.readShort();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.id);
        param0.writeUUID(this.uuid);
        param0.writeVarInt(Registry.ENTITY_TYPE.getId(this.type));
        param0.writeDouble(this.x);
        param0.writeDouble(this.y);
        param0.writeDouble(this.z);
        param0.writeByte(this.xRot);
        param0.writeByte(this.yRot);
        param0.writeInt(this.data);
        param0.writeShort(this.xa);
        param0.writeShort(this.ya);
        param0.writeShort(this.za);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleAddEntity(this);
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
    public double getXa() {
        return (double)this.xa / 8000.0;
    }

    @OnlyIn(Dist.CLIENT)
    public double getYa() {
        return (double)this.ya / 8000.0;
    }

    @OnlyIn(Dist.CLIENT)
    public double getZa() {
        return (double)this.za / 8000.0;
    }

    @OnlyIn(Dist.CLIENT)
    public int getxRot() {
        return this.xRot;
    }

    @OnlyIn(Dist.CLIENT)
    public int getyRot() {
        return this.yRot;
    }

    @OnlyIn(Dist.CLIENT)
    public EntityType<?> getType() {
        return this.type;
    }

    @OnlyIn(Dist.CLIENT)
    public int getData() {
        return this.data;
    }
}
