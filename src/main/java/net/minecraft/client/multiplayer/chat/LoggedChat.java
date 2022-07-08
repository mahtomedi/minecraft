package net.minecraft.client.multiplayer.chat;

import com.mojang.authlib.GameProfile;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface LoggedChat {
    static LoggedChat player(GameProfile param0, Component param1, PlayerChatMessage param2, ChatTrustLevel param3) {
        return new LoggedChat.Player(param0, param1, param2, param3);
    }

    static LoggedChat system(Component param0, Instant param1) {
        return new LoggedChat.System(param0, param1);
    }

    Component toContentComponent();

    default Component toNarrationComponent() {
        return this.toContentComponent();
    }

    boolean canReport(UUID var1);

    @OnlyIn(Dist.CLIENT)
    public static record Player(GameProfile profile, Component displayName, PlayerChatMessage message, ChatTrustLevel trustLevel) implements LoggedChat {
        private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

        @Override
        public Component toContentComponent() {
            return this.message.serverContent();
        }

        @Override
        public Component toNarrationComponent() {
            Component var0 = this.message.serverContent();
            Component var1 = this.getTimeComponent();
            return Component.translatable("gui.chatSelection.message.narrate", this.displayName, var0, var1);
        }

        public Component toHeadingComponent() {
            Component var0 = this.getTimeComponent();
            return Component.translatable("gui.chatSelection.heading", this.displayName, var0);
        }

        private Component getTimeComponent() {
            LocalDateTime var0 = LocalDateTime.ofInstant(this.message.timeStamp(), ZoneOffset.systemDefault());
            return Component.literal(var0.format(TIME_FORMATTER)).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
        }

        @Override
        public boolean canReport(UUID param0) {
            return this.profileId().equals(param0);
        }

        public UUID profileId() {
            return this.profile.getId();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record System(Component message, Instant timeStamp) implements LoggedChat {
        @Override
        public Component toContentComponent() {
            return this.message;
        }

        @Override
        public boolean canReport(UUID param0) {
            return false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record WithId(int id, LoggedChat message) {
    }
}
