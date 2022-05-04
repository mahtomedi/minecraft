package net.minecraft.network.protocol.game;

import java.util.Objects;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public record ClientboundSystemChatPacket(Component content, int typeId) implements Packet<ClientGamePacketListener> {
    public ClientboundSystemChatPacket(FriendlyByteBuf param0) {
        this(param0.readComponent(), param0.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeComponent(this.content);
        param0.writeVarInt(this.typeId);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSystemChat(this);
    }

    @Override
    public boolean isSkippable() {
        return true;
    }

    public ChatType resolveType(Registry<ChatType> param0) {
        return Objects.requireNonNull(param0.byId(this.typeId), "Invalid chat type");
    }
}
