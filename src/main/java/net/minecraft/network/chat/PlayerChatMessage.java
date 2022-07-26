package net.minecraft.network.chat;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record PlayerChatMessage(
    SignedMessageHeader signedHeader,
    MessageSignature headerSignature,
    SignedMessageBody signedBody,
    Optional<Component> unsignedContent,
    FilterMask filterMask
) {
    public static final Duration MESSAGE_EXPIRES_AFTER_SERVER = Duration.ofMinutes(5L);
    public static final Duration MESSAGE_EXPIRES_AFTER_CLIENT = MESSAGE_EXPIRES_AFTER_SERVER.plus(Duration.ofMinutes(2L));

    public PlayerChatMessage(FriendlyByteBuf param0) {
        this(
            new SignedMessageHeader(param0),
            new MessageSignature(param0),
            new SignedMessageBody(param0),
            param0.readOptional(FriendlyByteBuf::readComponent),
            FilterMask.read(param0)
        );
    }

    public static PlayerChatMessage system(ChatMessageContent param0) {
        return unsigned(MessageSigner.system(), param0);
    }

    public static PlayerChatMessage unsigned(MessageSigner param0, ChatMessageContent param1) {
        SignedMessageBody var0 = new SignedMessageBody(param1, param0.timeStamp(), param0.salt(), LastSeenMessages.EMPTY);
        SignedMessageHeader var1 = new SignedMessageHeader(null, param0.profileId());
        return new PlayerChatMessage(var1, MessageSignature.EMPTY, var0, Optional.empty(), FilterMask.PASS_THROUGH);
    }

    public void write(FriendlyByteBuf param0) {
        this.signedHeader.write(param0);
        this.headerSignature.write(param0);
        this.signedBody.write(param0);
        param0.writeOptional(this.unsignedContent, FriendlyByteBuf::writeComponent);
        FilterMask.write(param0, this.filterMask);
    }

    public PlayerChatMessage withUnsignedContent(Component param0) {
        Optional<Component> var0 = !this.signedContent().decorated().equals(param0) ? Optional.of(param0) : Optional.empty();
        return new PlayerChatMessage(this.signedHeader, this.headerSignature, this.signedBody, var0, this.filterMask);
    }

    public PlayerChatMessage removeUnsignedContent() {
        return this.unsignedContent.isPresent()
            ? new PlayerChatMessage(this.signedHeader, this.headerSignature, this.signedBody, Optional.empty(), this.filterMask)
            : this;
    }

    public PlayerChatMessage filter(FilterMask param0) {
        return this.filterMask.equals(param0)
            ? this
            : new PlayerChatMessage(this.signedHeader, this.headerSignature, this.signedBody, this.unsignedContent, param0);
    }

    public PlayerChatMessage filter(boolean param0) {
        return this.filter(param0 ? this.filterMask : FilterMask.PASS_THROUGH);
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

    public boolean hasSignatureFrom(UUID param0) {
        return !this.headerSignature.isEmpty() && this.signedHeader.sender().equals(param0);
    }

    public boolean isFullyFiltered() {
        return this.filterMask.isFullyFiltered();
    }
}
