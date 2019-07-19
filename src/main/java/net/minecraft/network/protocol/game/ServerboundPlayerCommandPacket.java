package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundPlayerCommandPacket implements Packet<ServerGamePacketListener> {
    private int id;
    private ServerboundPlayerCommandPacket.Action action;
    private int data;

    public ServerboundPlayerCommandPacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundPlayerCommandPacket(Entity param0, ServerboundPlayerCommandPacket.Action param1) {
        this(param0, param1, 0);
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundPlayerCommandPacket(Entity param0, ServerboundPlayerCommandPacket.Action param1, int param2) {
        this.id = param0.getId();
        this.action = param1;
        this.data = param2;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.id = param0.readVarInt();
        this.action = param0.readEnum(ServerboundPlayerCommandPacket.Action.class);
        this.data = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.id);
        param0.writeEnum(this.action);
        param0.writeVarInt(this.data);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handlePlayerCommand(this);
    }

    public ServerboundPlayerCommandPacket.Action getAction() {
        return this.action;
    }

    public int getData() {
        return this.data;
    }

    public static enum Action {
        START_SNEAKING,
        STOP_SNEAKING,
        STOP_SLEEPING,
        START_SPRINTING,
        STOP_SPRINTING,
        START_RIDING_JUMP,
        STOP_RIDING_JUMP,
        OPEN_INVENTORY,
        START_FALL_FLYING;
    }
}
