package net.minecraft.client.multiplayer.chat;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface LoggedChatMessage extends LoggedChatEvent {
    static LoggedChatMessage.Player player(GameProfile param0, PlayerChatMessage param1, ChatTrustLevel param2) {
        return new LoggedChatMessage.Player(param0, param1, param2);
    }

    static LoggedChatMessage.System system(Component param0, Instant param1) {
        return new LoggedChatMessage.System(param0, param1);
    }

    Component toContentComponent();

    default Component toNarrationComponent() {
        return this.toContentComponent();
    }

    boolean canReport(UUID var1);

    @OnlyIn(Dist.CLIENT)
    public static record Player(GameProfile profile, PlayerChatMessage message, ChatTrustLevel trustLevel) implements LoggedChatMessage {
        public static final Codec<LoggedChatMessage.Player> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.GAME_PROFILE.fieldOf("profile").forGetter(LoggedChatMessage.Player::profile),
                        PlayerChatMessage.MAP_CODEC.forGetter(LoggedChatMessage.Player::message),
                        ChatTrustLevel.CODEC.optionalFieldOf("trust_level", ChatTrustLevel.SECURE).forGetter(LoggedChatMessage.Player::trustLevel)
                    )
                    .apply(param0, LoggedChatMessage.Player::new)
        );
        private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

        @Override
        public Component toContentComponent() {
            if (!this.message.filterMask().isEmpty()) {
                Component var0 = this.message.filterMask().applyWithFormatting(this.message.signedContent());
                return (Component)(var0 != null ? var0 : Component.empty());
            } else {
                return this.message.decoratedContent();
            }
        }

        @Override
        public Component toNarrationComponent() {
            Component var0 = this.toContentComponent();
            Component var1 = this.getTimeComponent();
            return Component.translatable("gui.chatSelection.message.narrate", this.profile.getName(), var0, var1);
        }

        public Component toHeadingComponent() {
            Component var0 = this.getTimeComponent();
            return Component.translatable("gui.chatSelection.heading", this.profile.getName(), var0);
        }

        private Component getTimeComponent() {
            LocalDateTime var0 = LocalDateTime.ofInstant(this.message.timeStamp(), ZoneOffset.systemDefault());
            return Component.literal(var0.format(TIME_FORMATTER)).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
        }

        @Override
        public boolean canReport(UUID param0) {
            return this.message.hasSignatureFrom(param0);
        }

        public UUID profileId() {
            return this.profile.getId();
        }

        @Override
        public LoggedChatEvent.Type type() {
            return LoggedChatEvent.Type.PLAYER;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record System(Component message, Instant timeStamp) implements LoggedChatMessage {
        public static final Codec<LoggedChatMessage.System> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ComponentSerialization.CODEC.fieldOf("message").forGetter(LoggedChatMessage.System::message),
                        ExtraCodecs.INSTANT_ISO8601.fieldOf("time_stamp").forGetter(LoggedChatMessage.System::timeStamp)
                    )
                    .apply(param0, LoggedChatMessage.System::new)
        );

        @Override
        public Component toContentComponent() {
            return this.message;
        }

        @Override
        public boolean canReport(UUID param0) {
            return false;
        }

        @Override
        public LoggedChatEvent.Type type() {
            return LoggedChatEvent.Type.SYSTEM;
        }
    }
}
