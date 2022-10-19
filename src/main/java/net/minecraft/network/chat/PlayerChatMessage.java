package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import java.security.SignatureException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.SignatureUpdater;
import net.minecraft.util.SignatureValidator;

public record PlayerChatMessage(
    SignedMessageLink link, @Nullable MessageSignature signature, SignedMessageBody signedBody, @Nullable Component unsignedContent, FilterMask filterMask
) {
    private static final UUID SYSTEM_SENDER = Util.NIL_UUID;
    public static final Duration MESSAGE_EXPIRES_AFTER_SERVER = Duration.ofMinutes(5L);
    public static final Duration MESSAGE_EXPIRES_AFTER_CLIENT = MESSAGE_EXPIRES_AFTER_SERVER.plus(Duration.ofMinutes(2L));

    public static PlayerChatMessage system(String param0) {
        return unsigned(SYSTEM_SENDER, param0);
    }

    public static PlayerChatMessage unsigned(UUID param0, String param1) {
        SignedMessageBody var0 = SignedMessageBody.unsigned(param1);
        SignedMessageLink var1 = SignedMessageLink.unsigned(param0);
        return new PlayerChatMessage(var1, null, var0, null, FilterMask.PASS_THROUGH);
    }

    public PlayerChatMessage withUnsignedContent(Component param0) {
        Component var0 = !param0.equals(Component.literal(this.signedContent())) ? param0 : null;
        return new PlayerChatMessage(this.link, this.signature, this.signedBody, var0, this.filterMask);
    }

    public PlayerChatMessage removeUnsignedContent() {
        return this.unsignedContent != null ? new PlayerChatMessage(this.link, this.signature, this.signedBody, null, this.filterMask) : this;
    }

    public PlayerChatMessage filter(FilterMask param0) {
        return this.filterMask.equals(param0) ? this : new PlayerChatMessage(this.link, this.signature, this.signedBody, this.unsignedContent, param0);
    }

    public PlayerChatMessage filter(boolean param0) {
        return this.filter(param0 ? this.filterMask : FilterMask.PASS_THROUGH);
    }

    public static void updateSignature(SignatureUpdater.Output param0, SignedMessageLink param1, SignedMessageBody param2) throws SignatureException {
        param0.update(Ints.toByteArray(1));
        param1.updateSignature(param0);
        param2.updateSignature(param0);
    }

    public boolean verify(SignatureValidator param0) {
        return this.signature != null && this.signature.verify(param0, param0x -> updateSignature(param0x, this.link, this.signedBody));
    }

    public String signedContent() {
        return this.signedBody.content();
    }

    public Component decoratedContent() {
        return Objects.requireNonNullElseGet(this.unsignedContent, () -> Component.literal(this.signedContent()));
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

    public UUID sender() {
        return this.link.sender();
    }

    public boolean isSystem() {
        return this.sender().equals(SYSTEM_SENDER);
    }

    public boolean hasSignature() {
        return this.signature != null;
    }

    public boolean hasSignatureFrom(UUID param0) {
        return this.hasSignature() && this.link.sender().equals(param0);
    }

    public boolean isFullyFiltered() {
        return this.filterMask.isFullyFiltered();
    }
}
