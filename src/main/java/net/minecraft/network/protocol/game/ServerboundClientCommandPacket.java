package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundClientCommandPacket implements Packet<ServerGamePacketListener> {
    private final ServerboundClientCommandPacket.Action action;

    public ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action param0) {
        this.action = param0;
    }

    public ServerboundClientCommandPacket(FriendlyByteBuf param0) {
        this.action = param0.readEnum(ServerboundClientCommandPacket.Action.class);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeEnum(this.action);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleClientCommand(this);
    }

    public ServerboundClientCommandPacket.Action getAction() {
        return this.action;
    }

    public static enum Action {
        PERFORM_RESPAWN,
        REQUEST_STATS;
    }
}
