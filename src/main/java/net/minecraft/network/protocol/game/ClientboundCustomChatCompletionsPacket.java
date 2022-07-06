package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundCustomChatCompletionsPacket(ClientboundCustomChatCompletionsPacket.Action action, List<String> entries)
    implements Packet<ClientGamePacketListener> {
    public ClientboundCustomChatCompletionsPacket(FriendlyByteBuf param0) {
        this(param0.readEnum(ClientboundCustomChatCompletionsPacket.Action.class), param0.readList(FriendlyByteBuf::readUtf));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeEnum(this.action);
        param0.writeCollection(this.entries, FriendlyByteBuf::writeUtf);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleCustomChatCompletions(this);
    }

    public static enum Action {
        ADD,
        REMOVE,
        SET;
    }
}
