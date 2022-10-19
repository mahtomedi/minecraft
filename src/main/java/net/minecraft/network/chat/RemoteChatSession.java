package net.minecraft.network.chat;

import com.mojang.authlib.GameProfile;
import java.time.Duration;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record RemoteChatSession(UUID sessionId, @Nullable ProfilePublicKey profilePublicKey) {
    public static final RemoteChatSession UNVERIFIED = new RemoteChatSession(Util.NIL_UUID, null);

    public SignedMessageValidator createMessageValidator() {
        return (SignedMessageValidator)(this.profilePublicKey != null
            ? new SignedMessageValidator.KeyBased(this.profilePublicKey.createSignatureValidator())
            : SignedMessageValidator.ACCEPT_UNSIGNED);
    }

    public SignedMessageChain.Decoder createMessageDecoder(UUID param0) {
        return this.profilePublicKey != null
            ? new SignedMessageChain(param0, this.sessionId).decoder(this.profilePublicKey)
            : SignedMessageChain.Decoder.unsigned(param0);
    }

    public RemoteChatSession.Data asData() {
        return new RemoteChatSession.Data(this.sessionId, Util.mapNullable(this.profilePublicKey, ProfilePublicKey::data));
    }

    public boolean verifiable() {
        return this.profilePublicKey != null;
    }

    public static record Data(UUID sessionId, @Nullable ProfilePublicKey.Data profilePublicKey) {
        public static final RemoteChatSession.Data UNVERIFIED = RemoteChatSession.UNVERIFIED.asData();

        public static RemoteChatSession.Data read(FriendlyByteBuf param0) {
            return new RemoteChatSession.Data(param0.readUUID(), param0.readNullable(ProfilePublicKey.Data::new));
        }

        public static void write(FriendlyByteBuf param0, RemoteChatSession.Data param1) {
            param0.writeUUID(param1.sessionId);
            param0.writeNullable(param1.profilePublicKey, (param0x, param1x) -> param1x.write(param0x));
        }

        public RemoteChatSession validate(GameProfile param0, SignatureValidator param1, Duration param2) throws ProfilePublicKey.ValidationException {
            return this.profilePublicKey == null
                ? RemoteChatSession.UNVERIFIED
                : new RemoteChatSession(this.sessionId, ProfilePublicKey.createValidated(param1, param0.getId(), this.profilePublicKey, param2));
        }
    }
}
