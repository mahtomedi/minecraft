package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;

public record ClientboundPlayerChatPacket(PlayerChatMessage message, ChatType.BoundNetwork chatType) implements Packet<ClientGamePacketListener> {
    public ClientboundPlayerChatPacket(FriendlyByteBuf param0) {
        this(new PlayerChatMessage(param0), new ChatType.BoundNetwork(param0));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        this.message.write(param0);
        this.chatType.write(param0);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handlePlayerChat(this);
    }

    @Override
    public boolean isSkippable() {
        return true;
    }

    public Optional<ChatType.Bound> resolveChatType(RegistryAccess param0) {
        return this.chatType.resolve(param0);
    }
}
