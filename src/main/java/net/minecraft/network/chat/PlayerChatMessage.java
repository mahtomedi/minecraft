package net.minecraft.network.chat;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record PlayerChatMessage(
    SignedMessageHeader signedHeader, MessageSignature headerSignature, SignedMessageBody signedBody, Optional<Component> unsignedContent
) {
    public static final Duration MESSAGE_EXPIRES_AFTER_SERVER = Duration.ofMinutes(5L);
    public static final Duration MESSAGE_EXPIRES_AFTER_CLIENT = MESSAGE_EXPIRES_AFTER_SERVER.plus(Duration.ofMinutes(2L));

    public PlayerChatMessage(FriendlyByteBuf param0) {
        this(new SignedMessageHeader(param0), new MessageSignature(param0), new SignedMessageBody(param0), param0.readOptional(FriendlyByteBuf::readComponent));
    }

    public static PlayerChatMessage system(ChatMessageContent param0) {
        MessageSigner var0 = MessageSigner.system();
        SignedMessageBody var1 = new SignedMessageBody(param0, var0.timeStamp(), var0.salt(), LastSeenMessages.EMPTY);
        SignedMessageHeader var2 = new SignedMessageHeader(null, var0.profileId());
        return new PlayerChatMessage(var2, MessageSignature.EMPTY, var1, Optional.empty());
    }

    public void write(FriendlyByteBuf param0) {
        this.signedHeader.write(param0);
        this.headerSignature.write(param0);
        this.signedBody.write(param0);
        param0.writeOptional(this.unsignedContent, FriendlyByteBuf::writeComponent);
    }

    public FilteredText<PlayerChatMessage> withFilteredText(FilteredText<ChatMessageContent> param0) {
        return param0.rebuildIfNeeded(
            this,
            param0x -> this.signedContent().equals(param0x)
                    ? this
                    : new PlayerChatMessage(this.signedHeader, this.headerSignature, this.signedBody.withContent(param0x), this.unsignedContent)
        );
    }

    public PlayerChatMessage withUnsignedContent(Component param0) {
        Optional<Component> var0 = !this.signedContent().decorated().equals(param0) ? Optional.of(param0) : Optional.empty();
        return new PlayerChatMessage(this.signedHeader, this.headerSignature, this.signedBody, var0);
    }

    public PlayerChatMessage removeUnsignedContent() {
        return this.unsignedContent.isPresent() ? new PlayerChatMessage(this.signedHeader, this.headerSignature, this.signedBody, Optional.empty()) : this;
    }

    public boolean verify(SignatureValidator param0) {
        return this.headerSignature.verify(param0, this.signedHeader, this.signedBody);
    }

    public boolean verify(ProfilePublicKey param0) {
        SignatureValidator var0 = param0.createSignatureValidator();
        return this.verify(var0);
    }

    public boolean verify(ChatSender param0) {
        ProfilePublicKey var0 = param0.profilePublicKey();
        return var0 != null && this.verify(var0);
    }

    public ChatMessageContent signedContent() {
        return this.signedBody.content();
    }

    public Component serverContent() {
        return this.unsignedContent().orElse(this.signedContent().decorated());
    }

    public Instant timeStamp() {
        return this.signedBody.timeStamp();
    }

    public long salt() {
        return this.signedBody.salt();
    }

    public boolean hasExpiredServer(Instant param0) {
        return param0.isAfter(this.timeStamp().plus(MESSAGE_EXPIRES_AFTER_SERVER));
    }

    public boolean hasExpiredClient(Instant param0) {
        return param0.isAfter(this.timeStamp().plus(MESSAGE_EXPIRES_AFTER_CLIENT));
    }

    public MessageSigner signer() {
        return new MessageSigner(this.signedHeader.sender(), this.timeStamp(), this.salt());
    }

    @Nullable
    public LastSeenMessages.Entry toLastSeenEntry() {
        MessageSigner var0 = this.signer();
        return !this.headerSignature.isEmpty() && !var0.isSystem() ? new LastSeenMessages.Entry(var0.profileId(), this.headerSignature) : null;
    }

    public boolean hasSignatureFrom(ServerPlayer param0) {
        return !this.headerSignature.isEmpty() && this.signedHeader.sender().equals(param0.getUUID());
    }
}
