package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundChatPacket implements Packet<ServerGamePacketListener> {
    private static final int MAX_MESSAGE_LENGTH = 256;
    private final String message;

    public ServerboundChatPacket(String param0) {
        if (param0.length() > 256) {
            param0 = param0.substring(0, 256);
        }

        this.message = param0;
    }

    public ServerboundChatPacket(FriendlyByteBuf param0) {
        this.message = param0.readUtf(256);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.message);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleChat(this);
    }

    public String getMessage() {
        return this.message;
    }
}
