package net.minecraft.network.chat;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.Signer;
import net.minecraft.world.entity.player.ProfileKeyPair;

public record LocalChatSession(UUID sessionId, @Nullable ProfileKeyPair keyPair) {
    public static LocalChatSession create(@Nullable ProfileKeyPair param0) {
        return new LocalChatSession(UUID.randomUUID(), param0);
    }

    public SignedMessageChain.Encoder createMessageEncoder(UUID param0) {
        Signer var0 = this.createSigner();
        return var0 != null ? new SignedMessageChain(param0, this.sessionId).encoder(var0) : SignedMessageChain.Encoder.UNSIGNED;
    }

    @Nullable
    public Signer createSigner() {
        return this.keyPair != null ? Signer.from(this.keyPair.privateKey(), "SHA256withRSA") : null;
    }

    public RemoteChatSession asRemote() {
        return new RemoteChatSession(this.sessionId, Util.mapNullable(this.keyPair, ProfileKeyPair::publicKey));
    }
}
