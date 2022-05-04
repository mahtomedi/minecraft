package net.minecraft.network.protocol.game;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;

public record ClientboundPlayerChatPacket(Component content, int typeId, ChatSender sender, Instant timeStamp, Crypt.SaltSignaturePair saltSignature)
    implements Packet<ClientGamePacketListener> {
    private static final Duration MESSAGE_EXPIRES_AFTER = ServerboundChatPacket.MESSAGE_EXPIRES_AFTER.plus(Duration.ofMinutes(2L));

    public ClientboundPlayerChatPacket(FriendlyByteBuf param0) {
        this(param0.readComponent(), param0.readVarInt(), new ChatSender(param0), Instant.ofEpochSecond(param0.readLong()), new Crypt.SaltSignaturePair(param0));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeComponent(this.content);
        param0.writeVarInt(this.typeId);
        this.sender.write(param0);
        param0.writeLong(this.timeStamp.getEpochSecond());
        this.saltSignature.write(param0);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handlePlayerChat(this);
    }

    @Override
    public boolean isSkippable() {
        return true;
    }

    public SignedMessage getSignedMessage() {
        MessageSignature var0 = new MessageSignature(this.sender.uuid(), this.timeStamp, this.saltSignature);
        return new SignedMessage(this.content, var0);
    }

    private Instant getExpiresAt() {
        return this.timeStamp.plus(MESSAGE_EXPIRES_AFTER);
    }

    public boolean hasExpired(Instant param0) {
        return param0.isAfter(this.getExpiresAt());
    }

    public ChatType resolveType(Registry<ChatType> param0) {
        return Objects.requireNonNull(param0.byId(this.typeId), "Invalid chat type");
    }
}
