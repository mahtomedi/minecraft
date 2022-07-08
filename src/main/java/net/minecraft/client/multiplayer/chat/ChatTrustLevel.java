package net.minecraft.client.multiplayer.chat;

import java.time.Instant;
import javax.annotation.Nullable;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum ChatTrustLevel {
    SECURE,
    MODIFIED,
    NOT_SECURE;

    public static ChatTrustLevel evaluate(PlayerChatMessage param0, Component param1, @Nullable PlayerInfo param2) {
        if (param0.hasExpiredClient(Instant.now())) {
            return NOT_SECURE;
        } else if (param2 == null || !param2.getMessageValidator().validateMessage(param0)) {
            return NOT_SECURE;
        } else if (param0.unsignedContent().isPresent()) {
            return MODIFIED;
        } else {
            return !param1.contains(param0.signedContent()) ? MODIFIED : SECURE;
        }
    }

    public boolean isNotSecure() {
        return this == NOT_SECURE;
    }

    @Nullable
    public GuiMessageTag createTag(PlayerChatMessage param0) {
        return switch(this) {
            case MODIFIED -> GuiMessageTag.chatModified(param0.signedContent());
            case NOT_SECURE -> GuiMessageTag.chatNotSecure();
            default -> null;
        };
    }
}
