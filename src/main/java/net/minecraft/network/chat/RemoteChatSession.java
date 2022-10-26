package net.minecraft.network.chat;

import com.mojang.authlib.GameProfile;
import java.time.Duration;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record RemoteChatSession(UUID sessionId, ProfilePublicKey profilePublicKey) {
    public SignedMessageValidator createMessageValidator() {
        return new SignedMessageValidator.KeyBased(this.profilePublicKey.createSignatureValidator());
    }

    public SignedMessageChain.Decoder createMessageDecoder(UUID param0) {
        return new SignedMessageChain(param0, this.sessionId).decoder(this.profilePublicKey);
    }

    public RemoteChatSession.Data asData() {
        return new RemoteChatSession.Data(this.sessionId, this.profilePublicKey.data());
    }

    public static record Data(UUID sessionId, ProfilePublicKey.Data profilePublicKey) {
        public static RemoteChatSession.Data read(FriendlyByteBuf param0) {
            return new RemoteChatSession.Data(param0.readUUID(), new ProfilePublicKey.Data(param0));
        }

        public static void write(FriendlyByteBuf param0, RemoteChatSession.Data param1) {
            param0.writeUUID(param1.sessionId);
            param1.profilePublicKey.write(param0);
        }

        public RemoteChatSession validate(GameProfile param0, SignatureValidator param1, Duration param2) throws ProfilePublicKey.ValidationException {
            return new RemoteChatSession(this.sessionId, ProfilePublicKey.createValidated(param1, param0.getId(), this.profilePublicKey, param2));
        }
    }
}
