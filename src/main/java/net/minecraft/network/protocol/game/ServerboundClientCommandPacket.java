package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundClientCommandPacket implements Packet<ServerGamePacketListener> {
    private ServerboundClientCommandPacket.Action action;

    public ServerboundClientCommandPacket() {
    }

    public ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action param0) {
        this.action = param0;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.action = param0.readEnum(ServerboundClientCommandPacket.Action.class);
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
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
