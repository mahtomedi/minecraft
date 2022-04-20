package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public abstract class ClientboundMoveEntityPacket implements Packet<ClientGamePacketListener> {
    protected final int entityId;
    protected final short xa;
    protected final short ya;
    protected final short za;
    protected final byte yRot;
    protected final byte xRot;
    protected final boolean onGround;
    protected final boolean hasRot;
    protected final boolean hasPos;

    protected ClientboundMoveEntityPacket(
        int param0, short param1, short param2, short param3, byte param4, byte param5, boolean param6, boolean param7, boolean param8
    ) {
        this.entityId = param0;
        this.xa = param1;
        this.ya = param2;
        this.za = param3;
        this.yRot = param4;
        this.xRot = param5;
        this.onGround = param6;
        this.hasRot = param7;
        this.hasPos = param8;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleMoveEntity(this);
    }

    @Override
    public String toString() {
        return "Entity_" + super.toString();
    }

    @Nullable
    public Entity getEntity(Level param0) {
        return param0.getEntity(this.entityId);
    }

    public short getXa() {
        return this.xa;
    }

    public short getYa() {
        return this.ya;
    }

    public short getZa() {
        return this.za;
    }

    public byte getyRot() {
        return this.yRot;
    }

    public byte getxRot() {
        return this.xRot;
    }

    public boolean hasRotation() {
        return this.hasRot;
    }

    public boolean hasPosition() {
        return this.hasPos;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public static class Pos extends ClientboundMoveEntityPacket {
        public Pos(int param0, short param1, short param2, short param3, boolean param4) {
            super(param0, param1, param2, param3, (byte)0, (byte)0, param4, false, true);
        }

        public static ClientboundMoveEntityPacket.Pos read(FriendlyByteBuf param0) {
            int var0 = param0.readVarInt();
            short var1 = param0.readShort();
            short var2 = param0.readShort();
            short var3 = param0.readShort();
            boolean var4 = param0.readBoolean();
            return new ClientboundMoveEntityPacket.Pos(var0, var1, var2, var3, var4);
        }

        @Override
        public void write(FriendlyByteBuf param0) {
            param0.writeVarInt(this.entityId);
            param0.writeShort(this.xa);
            param0.writeShort(this.ya);
            param0.writeShort(this.za);
            param0.writeBoolean(this.onGround);
        }
    }

    public static class PosRot extends ClientboundMoveEntityPacket {
        public PosRot(int param0, short param1, short param2, short param3, byte param4, byte param5, boolean param6) {
            super(param0, param1, param2, param3, param4, param5, param6, true, true);
        }

        public static ClientboundMoveEntityPacket.PosRot read(FriendlyByteBuf param0) {
            int var0 = param0.readVarInt();
            short var1 = param0.readShort();
            short var2 = param0.readShort();
            short var3 = param0.readShort();
            byte var4 = param0.readByte();
            byte var5 = param0.readByte();
            boolean var6 = param0.readBoolean();
            return new ClientboundMoveEntityPacket.PosRot(var0, var1, var2, var3, var4, var5, var6);
        }

        @Override
        public void write(FriendlyByteBuf param0) {
            param0.writeVarInt(this.entityId);
            param0.writeShort(this.xa);
            param0.writeShort(this.ya);
            param0.writeShort(this.za);
            param0.writeByte(this.yRot);
            param0.writeByte(this.xRot);
            param0.writeBoolean(this.onGround);
        }
    }

    public static class Rot extends ClientboundMoveEntityPacket {
        public Rot(int param0, byte param1, byte param2, boolean param3) {
            super(param0, (short)0, (short)0, (short)0, param1, param2, param3, true, false);
        }

        public static ClientboundMoveEntityPacket.Rot read(FriendlyByteBuf param0) {
            int var0 = param0.readVarInt();
            byte var1 = param0.readByte();
            byte var2 = param0.readByte();
            boolean var3 = param0.readBoolean();
            return new ClientboundMoveEntityPacket.Rot(var0, var1, var2, var3);
        }

        @Override
        public void write(FriendlyByteBuf param0) {
            param0.writeVarInt(this.entityId);
            param0.writeByte(this.yRot);
            param0.writeByte(this.xRot);
            param0.writeBoolean(this.onGround);
        }
    }
}
