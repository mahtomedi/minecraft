package net.minecraft.network.protocol.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundResourcePackPacket implements Packet<ServerCommonPacketListener> {
    private final ServerboundResourcePackPacket.Action action;

    public ServerboundResourcePackPacket(ServerboundResourcePackPacket.Action param0) {
        this.action = param0;
    }

    public ServerboundResourcePackPacket(FriendlyByteBuf param0) {
        this.action = param0.readEnum(ServerboundResourcePackPacket.Action.class);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeEnum(this.action);
    }

    public void handle(ServerCommonPacketListener param0) {
        param0.handleResourcePackResponse(this);
    }

    public ServerboundResourcePackPacket.Action getAction() {
        return this.action;
    }

    public static enum Action {
        SUCCESSFULLY_LOADED,
        DECLINED,
        FAILED_DOWNLOAD,
        ACCEPTED;
    }
}
