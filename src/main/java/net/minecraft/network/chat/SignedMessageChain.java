package net.minecraft.network.chat;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.util.SignatureUpdater;
import net.minecraft.util.Signer;

public class SignedMessageChain {
    @Nullable
    private MessageSignature previousSignature;

    private SignedMessageChain.Link pack(Signer param0, MessageSigner param1, ChatMessageContent param2, LastSeenMessages param3) {
        MessageSignature var0 = pack(param0, param1, this.previousSignature, param2, param3);
        this.previousSignature = var0;
        return new SignedMessageChain.Link(var0);
    }

    private static MessageSignature pack(
        Signer param0, MessageSigner param1, @Nullable MessageSignature param2, ChatMessageContent param3, LastSeenMessages param4
    ) {
        SignedMessageHeader var0 = new SignedMessageHeader(param2, param1.profileId());
        SignedMessageBody var1 = new SignedMessageBody(param3, param1.timeStamp(), param1.salt(), param4);
        byte[] var2 = var1.hash().asBytes();
        return new MessageSignature(param0.sign((SignatureUpdater)(param2x -> var0.updateSignature(param2x, var2))));
    }

    private PlayerChatMessage unpack(SignedMessageChain.Link param0, MessageSigner param1, ChatMessageContent param2, LastSeenMessages param3) {
        PlayerChatMessage var0 = unpack(param0, this.previousSignature, param1, param2, param3);
        this.previousSignature = param0.signature;
        return var0;
    }

    private static PlayerChatMessage unpack(
        SignedMessageChain.Link param0, @Nullable MessageSignature param1, MessageSigner param2, ChatMessageContent param3, LastSeenMessages param4
    ) {
        SignedMessageHeader var0 = new SignedMessageHeader(param1, param2.profileId());
        SignedMessageBody var1 = new SignedMessageBody(param3, param2.timeStamp(), param2.salt(), param4);
        return new PlayerChatMessage(var0, param0.signature, var1, Optional.empty(), FilterMask.PASS_THROUGH);
    }

    public SignedMessageChain.Decoder decoder() {
        return this::unpack;
    }

    public SignedMessageChain.Encoder encoder() {
        return this::pack;
    }

    @FunctionalInterface
    public interface Decoder {
        SignedMessageChain.Decoder UNSIGNED = (param0, param1, param2, param3) -> PlayerChatMessage.unsigned(param1, param2);

        PlayerChatMessage unpack(SignedMessageChain.Link var1, MessageSigner var2, ChatMessageContent var3, LastSeenMessages var4);
    }

    @FunctionalInterface
    public interface Encoder {
        SignedMessageChain.Link pack(Signer var1, MessageSigner var2, ChatMessageContent var3, LastSeenMessages var4);
    }

    public static record Link(MessageSignature signature) {
    }
}
