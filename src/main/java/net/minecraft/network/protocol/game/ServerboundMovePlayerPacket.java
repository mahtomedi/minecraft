package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundMovePlayerPacket implements Packet<ServerGamePacketListener> {
    protected double x;
    protected double y;
    protected double z;
    protected float yRot;
    protected float xRot;
    protected boolean onGround;
    protected boolean hasPos;
    protected boolean hasRot;

    public ServerboundMovePlayerPacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundMovePlayerPacket(boolean param0) {
        this.onGround = param0;
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleMovePlayer(this);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.onGround = param0.readUnsignedByte() != 0;
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeByte(this.onGround ? 1 : 0);
    }

    public double getX(double param0) {
        return this.hasPos ? this.x : param0;
    }

    public double getY(double param0) {
        return this.hasPos ? this.y : param0;
    }

    public double getZ(double param0) {
        return this.hasPos ? this.z : param0;
    }

    public float getYRot(float param0) {
        return this.hasRot ? this.yRot : param0;
    }

    public float getXRot(float param0) {
        return this.hasRot ? this.xRot : param0;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public static class Pos extends ServerboundMovePlayerPacket {
        public Pos() {
            this.hasPos = true;
        }

        @OnlyIn(Dist.CLIENT)
        public Pos(double param0, double param1, double param2, boolean param3) {
            this.x = param0;
            this.y = param1;
            this.z = param2;
            this.onGround = param3;
            this.hasPos = true;
        }

        @Override
        public void read(FriendlyByteBuf param0) throws IOException {
            this.x = param0.readDouble();
            this.y = param0.readDouble();
            this.z = param0.readDouble();
            super.read(param0);
        }

        @Override
        public void write(FriendlyByteBuf param0) throws IOException {
            param0.writeDouble(this.x);
            param0.writeDouble(this.y);
            param0.writeDouble(this.z);
            super.write(param0);
        }
    }

    public static class PosRot extends ServerboundMovePlayerPacket {
        public PosRot() {
            this.hasPos = true;
            this.hasRot = true;
        }

        @OnlyIn(Dist.CLIENT)
        public PosRot(double param0, double param1, double param2, float param3, float param4, boolean param5) {
            this.x = param0;
            this.y = param1;
            this.z = param2;
            this.yRot = param3;
            this.xRot = param4;
            this.onGround = param5;
            this.hasRot = true;
            this.hasPos = true;
        }

        @Override
        public void read(FriendlyByteBuf param0) throws IOException {
            this.x = param0.readDouble();
            this.y = param0.readDouble();
            this.z = param0.readDouble();
            this.yRot = param0.readFloat();
            this.xRot = param0.readFloat();
            super.read(param0);
        }

        @Override
        public void write(FriendlyByteBuf param0) throws IOException {
            param0.writeDouble(this.x);
            param0.writeDouble(this.y);
            param0.writeDouble(this.z);
            param0.writeFloat(this.yRot);
            param0.writeFloat(this.xRot);
            super.write(param0);
        }
    }

    public static class Rot extends ServerboundMovePlayerPacket {
        public Rot() {
            this.hasRot = true;
        }

        @OnlyIn(Dist.CLIENT)
        public Rot(float param0, float param1, boolean param2) {
            this.yRot = param0;
            this.xRot = param1;
            this.onGround = param2;
            this.hasRot = true;
        }

        @Override
        public void read(FriendlyByteBuf param0) throws IOException {
            this.yRot = param0.readFloat();
            this.xRot = param0.readFloat();
            super.read(param0);
        }

        @Override
        public void write(FriendlyByteBuf param0) throws IOException {
            param0.writeFloat(this.yRot);
            param0.writeFloat(this.xRot);
            super.write(param0);
        }
    }
}
