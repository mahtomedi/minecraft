package net.minecraft.network.protocol.common;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundResourcePackPacket(UUID id, ServerboundResourcePackPacket.Action action) implements Packet<ServerCommonPacketListener> {
    public ServerboundResourcePackPacket(FriendlyByteBuf param0) {
        this(param0.readUUID(), param0.readEnum(ServerboundResourcePackPacket.Action.class));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUUID(this.id);
        param0.writeEnum(this.action);
    }

    public void handle(ServerCommonPacketListener param0) {
        param0.handleResourcePackResponse(this);
    }

    public static enum Action {
        SUCCESSFULLY_LOADED,
        DECLINED,
        FAILED_DOWNLOAD,
        ACCEPTED,
        INVALID_URL,
        FAILED_RELOAD,
        DISCARDED;
    }
}
