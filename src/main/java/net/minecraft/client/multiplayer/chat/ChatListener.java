package net.minecraft.client.multiplayer.chat;

import com.mojang.authlib.GameProfile;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.util.StringDecomposer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class ChatListener {
    private final Minecraft minecraft;

    public ChatListener(Minecraft param0) {
        this.minecraft = param0;
    }

    public void handleChatMessage(ChatType param0, PlayerChatMessage param1, ChatSender param2) {
        if (!this.minecraft.isBlocked(param2.profileId())) {
            boolean var0 = this.minecraft.options.onlyShowSecureChat().get();
            PlayerChatMessage var1 = var0 ? param1.removeUnsignedContent() : param1;
            Component var2 = param0.chat().decorate(var1.serverContent(), param2);
            PlayerInfo var3 = this.resolveSenderPlayer(param2);
            ChatTrustLevel var4 = this.evaluateTrustLevel(param2, var1, var2, var3);
            if (!var4.isNotSecure() || !var0) {
                GuiMessageTag var5 = var4.createTag(var1);
                this.minecraft.gui.getChat().enqueueMessage(var2, var5);
                this.minecraft.getNarrator().sayChatNow(() -> param0.narration().decorate(var1.serverContent(), param2));
                if (param2.isPlayer()) {
                    this.logPlayerMessage(param1, param2, var3, var4);
                } else {
                    this.logSystemMessage(var2, param1.signature().timeStamp());
                }

            }
        }
    }

    private ChatTrustLevel evaluateTrustLevel(ChatSender param0, PlayerChatMessage param1, Component param2, @Nullable PlayerInfo param3) {
        if (param0.isPlayer()) {
            return this.isSenderLocalPlayer(param0) ? ChatTrustLevel.SECURE : ChatTrustLevel.evaluate(param1, param2, param3);
        } else {
            return ChatTrustLevel.UNKNOWN;
        }
    }

    private void logPlayerMessage(PlayerChatMessage param0, ChatSender param1, @Nullable PlayerInfo param2, ChatTrustLevel param3) {
        GameProfile var0;
        if (param2 != null) {
            var0 = param2.getProfile();
        } else {
            var0 = new GameProfile(param1.profileId(), param1.name().getString());
        }

        ChatLog var2 = this.minecraft.getReportingContext().chatLog();
        var2.push(LoggedChat.player(var0, param1.name(), param0, param3));
    }

    @Nullable
    private PlayerInfo resolveSenderPlayer(ChatSender param0) {
        ClientPacketListener var0 = this.minecraft.getConnection();
        return var0 != null ? var0.getPlayerInfo(param0.profileId()) : null;
    }

    public void handleSystemMessage(Component param0, boolean param1) {
        if (!this.minecraft.options.hideMatchedNames().get() || !this.minecraft.isBlocked(this.guessChatUUID(param0))) {
            if (param1) {
                this.minecraft.gui.setOverlayMessage(param0, false);
            } else {
                this.minecraft.gui.getChat().addMessage(param0);
                this.logSystemMessage(param0, Instant.now());
            }

            this.minecraft.getNarrator().sayNow(param0);
        }
    }

    private UUID guessChatUUID(Component param0) {
        String var0 = StringDecomposer.getPlainText(param0);
        String var1 = StringUtils.substringBetween(var0, "<", ">");
        return var1 == null ? Util.NIL_UUID : this.minecraft.getPlayerSocialManager().getDiscoveredUUID(var1);
    }

    private void logSystemMessage(Component param0, Instant param1) {
        ChatLog var0 = this.minecraft.getReportingContext().chatLog();
        var0.push(LoggedChat.system(param0, param1));
    }

    private boolean isSenderLocalPlayer(ChatSender param0) {
        if (this.minecraft.isLocalServer() && this.minecraft.player != null) {
            UUID var0 = this.minecraft.player.getGameProfile().getId();
            return var0.equals(param0.profileId());
        } else {
            return false;
        }
    }
}
