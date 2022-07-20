package net.minecraft.client.multiplayer.chat;

import java.time.Instant;
import javax.annotation.Nullable;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageValidator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum ChatTrustLevel {
    SECURE,
    MODIFIED,
    NOT_SECURE,
    BROKEN_CHAIN;

    public static ChatTrustLevel evaluate(PlayerChatMessage param0, Component param1, @Nullable PlayerInfo param2, Instant param3) {
        if (param2 != null && !param0.hasExpiredClient(param3)) {
            SignedMessageValidator.State var0 = param2.getMessageValidator().validateMessage(param0);
            if (var0 == SignedMessageValidator.State.BROKEN_CHAIN) {
                return BROKEN_CHAIN;
            } else if (var0 == SignedMessageValidator.State.NOT_SECURE) {
                return NOT_SECURE;
            } else if (param0.unsignedContent().isPresent()) {
                return MODIFIED;
            } else {
                return !param1.contains(param0.signedContent().decorated()) ? MODIFIED : SECURE;
            }
        } else {
            return NOT_SECURE;
        }
    }

    public boolean isNotSecure() {
        return this == NOT_SECURE || this == BROKEN_CHAIN;
    }

    @Nullable
    public GuiMessageTag createTag(PlayerChatMessage param0) {
        return switch(this) {
            case MODIFIED -> GuiMessageTag.chatModified(param0.signedContent().plain());
            case NOT_SECURE -> GuiMessageTag.chatNotSecure();
            default -> null;
        };
    }
}
