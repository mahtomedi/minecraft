package net.minecraft.client.multiplayer.chat;

import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageHeader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface LoggedChatMessageLink extends LoggedChatEvent {
    static LoggedChatMessageLink.Header header(SignedMessageHeader param0, MessageSignature param1, byte[] param2) {
        return new LoggedChatMessageLink.Header(param0, param1, param2);
    }

    SignedMessageHeader header();

    MessageSignature headerSignature();

    byte[] bodyDigest();

    @OnlyIn(Dist.CLIENT)
    public static record Header(SignedMessageHeader header, MessageSignature headerSignature, byte[] bodyDigest) implements LoggedChatMessageLink {
    }
}
