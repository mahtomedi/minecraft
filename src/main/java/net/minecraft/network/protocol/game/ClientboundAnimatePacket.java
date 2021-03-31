package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class ClientboundAnimatePacket implements Packet<ClientGamePacketListener> {
    public static final int SWING_MAIN_HAND = 0;
    public static final int HURT = 1;
    public static final int WAKE_UP = 2;
    public static final int SWING_OFF_HAND = 3;
    public static final int CRITICAL_HIT = 4;
    public static final int MAGIC_CRITICAL_HIT = 5;
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

    public int getId() {
        return this.id;
    }

    public int getAction() {
        return this.action;
    }
}
