package net.minecraft.client.multiplayer.chat;

import java.time.Instant;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum ChatTrustLevel {
    SECURE,
    MODIFIED,
    NOT_SECURE;

    public static ChatTrustLevel evaluate(PlayerChatMessage param0, Component param1, Instant param2) {
        if (!param0.hasSignature() || param0.hasExpiredClient(param2)) {
            return NOT_SECURE;
        } else {
            return isModified(param0, param1) ? MODIFIED : SECURE;
        }
    }

    private static boolean isModified(PlayerChatMessage param0, Component param1) {
        if (!param1.getString().contains(param0.signedContent())) {
            return true;
        } else {
            Component var0 = param0.unsignedContent();
            return var0 == null ? false : containsModifiedStyle(var0);
        }
    }

    private static boolean containsModifiedStyle(Component param0) {
        return param0.<Boolean>visit((param0x, param1) -> isModifiedStyle(param0x) ? Optional.of(true) : Optional.empty(), Style.EMPTY).orElse(false);
    }

    private static boolean isModifiedStyle(Style param0) {
        return !param0.getFont().equals(Style.DEFAULT_FONT);
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
