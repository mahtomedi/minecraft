package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class ServerboundPlayerCommandPacket implements Packet<ServerGamePacketListener> {
    private final int id;
    private final ServerboundPlayerCommandPacket.Action action;
    private final int data;

    public ServerboundPlayerCommandPacket(Entity param0, ServerboundPlayerCommandPacket.Action param1) {
        this(param0, param1, 0);
    }

    public ServerboundPlayerCommandPacket(Entity param0, ServerboundPlayerCommandPacket.Action param1, int param2) {
        this.id = param0.getId();
        this.action = param1;
        this.data = param2;
    }

    public ServerboundPlayerCommandPacket(FriendlyByteBuf param0) {
        this.id = param0.readVarInt();
        this.action = param0.readEnum(ServerboundPlayerCommandPacket.Action.class);
        this.data = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.id);
        param0.writeEnum(this.action);
        param0.writeVarInt(this.data);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handlePlayerCommand(this);
    }

    public int getId() {
        return this.id;
    }

    public ServerboundPlayerCommandPacket.Action getAction() {
        return this.action;
    }

    public int getData() {
        return this.data;
    }

    public static enum Action {
        PRESS_SHIFT_KEY,
        RELEASE_SHIFT_KEY,
        STOP_SLEEPING,
        START_SPRINTING,
        STOP_SPRINTING,
        START_RIDING_JUMP,
        STOP_RIDING_JUMP,
        OPEN_INVENTORY,
        START_FALL_FLYING;
    }
}
