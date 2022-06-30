package net.minecraft.network.protocol.game;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;

public record ClientboundPlayerChatPacket(
    Component signedContent, Optional<Component> unsignedContent, int typeId, ChatSender sender, Instant timeStamp, Crypt.SaltSignaturePair saltSignature
) implements Packet<ClientGamePacketListener> {
    public ClientboundPlayerChatPacket(FriendlyByteBuf param0) {
        this(
            param0.readComponent(),
            param0.readOptional(FriendlyByteBuf::readComponent),
            param0.readVarInt(),
            new ChatSender(param0),
            param0.readInstant(),
            new Crypt.SaltSignaturePair(param0)
        );
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeComponent(this.signedContent);
        param0.writeOptional(this.unsignedContent, FriendlyByteBuf::writeComponent);
        param0.writeVarInt(this.typeId);
        this.sender.write(param0);
        param0.writeInstant(this.timeStamp);
        Crypt.SaltSignaturePair.write(param0, this.saltSignature);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handlePlayerChat(this);
    }

    @Override
    public boolean isSkippable() {
        return true;
    }

    public PlayerChatMessage getMessage() {
        MessageSignature var0 = new MessageSignature(this.sender.profileId(), this.timeStamp, this.saltSignature);
        return new PlayerChatMessage(this.signedContent, var0, this.unsignedContent);
    }

    public ChatType resolveType(Registry<ChatType> param0) {
        return Objects.requireNonNull(param0.byId(this.typeId), "Invalid chat type");
    }
}
