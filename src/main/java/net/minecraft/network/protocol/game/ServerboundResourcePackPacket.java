package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundResourcePackPacket implements Packet<ServerGamePacketListener> {
    private ServerboundResourcePackPacket.Action action;

    public ServerboundResourcePackPacket() {
    }

    public ServerboundResourcePackPacket(ServerboundResourcePackPacket.Action param0) {
        this.action = param0;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.action = param0.readEnum(ServerboundResourcePackPacket.Action.class);
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeEnum(this.action);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleResourcePackResponse(this);
    }

    public static enum Action {
        SUCCESSFULLY_LOADED,
        DECLINED,
        FAILED_DOWNLOAD,
        ACCEPTED;
    }
}
