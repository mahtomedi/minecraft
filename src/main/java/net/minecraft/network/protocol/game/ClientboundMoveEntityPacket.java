package net.minecraft.network.protocol.game;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundMoveEntityPacket implements Packet<ClientGamePacketListener> {
    protected int entityId;
    protected short xa;
    protected short ya;
    protected short za;
    protected byte yRot;
    protected byte xRot;
    protected boolean onGround;
    protected boolean hasRot;
    protected boolean hasPos;

    public static long entityToPacket(double param0) {
        return Mth.lfloor(param0 * 4096.0);
    }

    @OnlyIn(Dist.CLIENT)
    public static double packetToEntity(long param0) {
        return (double)param0 / 4096.0;
    }

    @OnlyIn(Dist.CLIENT)
    public Vec3 updateEntityPosition(Vec3 param0) {
        double var0 = this.xa == 0 ? param0.x : packetToEntity(entityToPacket(param0.x) + (long)this.xa);
        double var1 = this.ya == 0 ? param0.y : packetToEntity(entityToPacket(param0.y) + (long)this.ya);
        double var2 = this.za == 0 ? param0.z : packetToEntity(entityToPacket(param0.z) + (long)this.za);
        return new Vec3(var0, var1, var2);
    }

    public static Vec3 packetToEntity(long param0, long param1, long param2) {
        return new Vec3((double)param0, (double)param1, (double)param2).scale(2.4414062E-4F);
    }

    public ClientboundMoveEntityPacket() {
    }

    public ClientboundMoveEntityPacket(int param0) {
        this.entityId = param0;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.entityId = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.entityId);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleMoveEntity(this);
    }

    @Override
    public String toString() {
        return "Entity_" + super.toString();
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public Entity getEntity(Level param0) {
        return param0.getEntity(this.entityId);
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
    public boolean hasRotation() {
        return this.hasRot;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean hasPosition() {
        return this.hasPos;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isOnGround() {
        return this.onGround;
    }

    public static class Pos extends ClientboundMoveEntityPacket {
        public Pos() {
            this.hasPos = true;
        }

        public Pos(int param0, short param1, short param2, short param3, boolean param4) {
            super(param0);
            this.xa = param1;
            this.ya = param2;
            this.za = param3;
            this.onGround = param4;
            this.hasPos = true;
        }

        @Override
        public void read(FriendlyByteBuf param0) throws IOException {
            super.read(param0);
            this.xa = param0.readShort();
            this.ya = param0.readShort();
            this.za = param0.readShort();
            this.onGround = param0.readBoolean();
        }

        @Override
        public void write(FriendlyByteBuf param0) throws IOException {
            super.write(param0);
            param0.writeShort(this.xa);
            param0.writeShort(this.ya);
            param0.writeShort(this.za);
            param0.writeBoolean(this.onGround);
        }
    }

    public static class PosRot extends ClientboundMoveEntityPacket {
        public PosRot() {
            this.hasRot = true;
            this.hasPos = true;
        }

        public PosRot(int param0, short param1, short param2, short param3, byte param4, byte param5, boolean param6) {
            super(param0);
            this.xa = param1;
            this.ya = param2;
            this.za = param3;
            this.yRot = param4;
            this.xRot = param5;
            this.onGround = param6;
            this.hasRot = true;
            this.hasPos = true;
        }

        @Override
        public void read(FriendlyByteBuf param0) throws IOException {
            super.read(param0);
            this.xa = param0.readShort();
            this.ya = param0.readShort();
            this.za = param0.readShort();
            this.yRot = param0.readByte();
            this.xRot = param0.readByte();
            this.onGround = param0.readBoolean();
        }

        @Override
        public void write(FriendlyByteBuf param0) throws IOException {
            super.write(param0);
            param0.writeShort(this.xa);
            param0.writeShort(this.ya);
            param0.writeShort(this.za);
            param0.writeByte(this.yRot);
            param0.writeByte(this.xRot);
            param0.writeBoolean(this.onGround);
        }
    }

    public static class Rot extends ClientboundMoveEntityPacket {
        public Rot() {
            this.hasRot = true;
        }

        public Rot(int param0, byte param1, byte param2, boolean param3) {
            super(param0);
            this.yRot = param1;
            this.xRot = param2;
            this.hasRot = true;
            this.onGround = param3;
        }

        @Override
        public void read(FriendlyByteBuf param0) throws IOException {
            super.read(param0);
            this.yRot = param0.readByte();
            this.xRot = param0.readByte();
            this.onGround = param0.readBoolean();
        }

        @Override
        public void write(FriendlyByteBuf param0) throws IOException {
            super.write(param0);
            param0.writeByte(this.yRot);
            param0.writeByte(this.xRot);
            param0.writeBoolean(this.onGround);
        }
    }
}
