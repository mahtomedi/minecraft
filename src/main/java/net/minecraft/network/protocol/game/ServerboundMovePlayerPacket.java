package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public abstract class ServerboundMovePlayerPacket implements Packet<ServerGamePacketListener> {
    protected final double x;
    protected final double y;
    protected final double z;
    protected final float yRot;
    protected final float xRot;
    protected final boolean onGround;
    protected final boolean hasPos;
    protected final boolean hasRot;

    protected ServerboundMovePlayerPacket(
        double param0, double param1, double param2, float param3, float param4, boolean param5, boolean param6, boolean param7
    ) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
        this.yRot = param3;
        this.xRot = param4;
        this.onGround = param5;
        this.hasPos = param6;
        this.hasRot = param7;
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleMovePlayer(this);
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

    public boolean hasPosition() {
        return this.hasPos;
    }

    public boolean hasRotation() {
        return this.hasRot;
    }

    public static class Pos extends ServerboundMovePlayerPacket {
        public Pos(double param0, double param1, double param2, boolean param3) {
            super(param0, param1, param2, 0.0F, 0.0F, param3, true, false);
        }

        public static ServerboundMovePlayerPacket.Pos read(FriendlyByteBuf param0) {
            double var0 = param0.readDouble();
            double var1 = param0.readDouble();
            double var2 = param0.readDouble();
            boolean var3 = param0.readUnsignedByte() != 0;
            return new ServerboundMovePlayerPacket.Pos(var0, var1, var2, var3);
        }

        @Override
        public void write(FriendlyByteBuf param0) {
            param0.writeDouble(this.x);
            param0.writeDouble(this.y);
            param0.writeDouble(this.z);
            param0.writeByte(this.onGround ? 1 : 0);
        }
    }

    public static class PosRot extends ServerboundMovePlayerPacket {
        public PosRot(double param0, double param1, double param2, float param3, float param4, boolean param5) {
            super(param0, param1, param2, param3, param4, param5, true, true);
        }

        public static ServerboundMovePlayerPacket.PosRot read(FriendlyByteBuf param0) {
            double var0 = param0.readDouble();
            double var1 = param0.readDouble();
            double var2 = param0.readDouble();
            float var3 = param0.readFloat();
            float var4 = param0.readFloat();
            boolean var5 = param0.readUnsignedByte() != 0;
            return new ServerboundMovePlayerPacket.PosRot(var0, var1, var2, var3, var4, var5);
        }

        @Override
        public void write(FriendlyByteBuf param0) {
            param0.writeDouble(this.x);
            param0.writeDouble(this.y);
            param0.writeDouble(this.z);
            param0.writeFloat(this.yRot);
            param0.writeFloat(this.xRot);
            param0.writeByte(this.onGround ? 1 : 0);
        }
    }

    public static class Rot extends ServerboundMovePlayerPacket {
        public Rot(float param0, float param1, boolean param2) {
            super(0.0, 0.0, 0.0, param0, param1, param2, false, true);
        }

        public static ServerboundMovePlayerPacket.Rot read(FriendlyByteBuf param0) {
            float var0 = param0.readFloat();
            float var1 = param0.readFloat();
            boolean var2 = param0.readUnsignedByte() != 0;
            return new ServerboundMovePlayerPacket.Rot(var0, var1, var2);
        }

        @Override
        public void write(FriendlyByteBuf param0) {
            param0.writeFloat(this.yRot);
            param0.writeFloat(this.xRot);
            param0.writeByte(this.onGround ? 1 : 0);
        }
    }

    public static class StatusOnly extends ServerboundMovePlayerPacket {
        public StatusOnly(boolean param0) {
            super(0.0, 0.0, 0.0, 0.0F, 0.0F, param0, false, false);
        }

        public static ServerboundMovePlayerPacket.StatusOnly read(FriendlyByteBuf param0) {
            boolean var0 = param0.readUnsignedByte() != 0;
            return new ServerboundMovePlayerPacket.StatusOnly(var0);
        }

        @Override
        public void write(FriendlyByteBuf param0) {
            param0.writeByte(this.onGround ? 1 : 0);
        }
    }
}
