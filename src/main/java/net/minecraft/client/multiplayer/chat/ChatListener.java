package net.minecraft.client.multiplayer.chat;

import com.google.common.collect.Queues;
import com.mojang.authlib.GameProfile;
import java.time.Instant;
import java.util.Collection;
import java.util.Deque;
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
    private final Deque<ChatListener.Message> delayedMessageQueue = Queues.newArrayDeque();
    private long messageDelay;
    private long previousMessageTime;

    public ChatListener(Minecraft param0) {
        this.minecraft = param0;
    }

    public void tick() {
        if (this.messageDelay != 0L) {
            if (Util.getMillis() >= this.previousMessageTime + this.messageDelay) {
                ChatListener.Message var0 = this.delayedMessageQueue.poll();

                while(var0 != null && !var0.accept()) {
                    var0 = this.delayedMessageQueue.poll();
                }
            }

        }
    }

    public void setMessageDelay(double param0) {
        long var0 = (long)(param0 * 1000.0);
        if (var0 == 0L && this.messageDelay > 0L) {
            this.delayedMessageQueue.forEach(ChatListener.Message::accept);
            this.delayedMessageQueue.clear();
        }

        this.messageDelay = var0;
    }

    public void acceptNextDelayedMessage() {
        this.delayedMessageQueue.remove().accept();
    }

    public Collection<?> delayedMessageQueue() {
        return this.delayedMessageQueue;
    }

    private boolean willDelayMessages() {
        return this.messageDelay > 0L && Util.getMillis() < this.previousMessageTime + this.messageDelay;
    }

    public void handleChatMessage(ChatType param0, PlayerChatMessage param1, ChatSender param2) {
        boolean var0 = this.minecraft.options.onlyShowSecureChat().get();
        PlayerChatMessage var1 = var0 ? param1.removeUnsignedContent() : param1;
        Component var2 = param0.chat().decorate(var1.serverContent(), param2);
        if (param2.isPlayer()) {
            PlayerInfo var3 = this.resolveSenderPlayer(param2);
            ChatTrustLevel var4 = this.evaluateTrustLevel(param2, param1, var2, var3);
            if (var0 && var4.isNotSecure()) {
                return;
            }

            if (this.willDelayMessages()) {
                this.delayedMessageQueue.add(() -> this.processPlayerChatMessage(param0, param2, param1, var2, var3, var4));
                return;
            }

            this.processPlayerChatMessage(param0, param2, param1, var2, var3, var4);
        } else {
            if (this.willDelayMessages()) {
                this.delayedMessageQueue.add(() -> this.processNonPlayerChatMessage(param0, param2, var1, var2));
                return;
            }

            this.processNonPlayerChatMessage(param0, param2, var1, var2);
        }

    }

    private boolean processPlayerChatMessage(
        ChatType param0, ChatSender param1, PlayerChatMessage param2, Component param3, @Nullable PlayerInfo param4, ChatTrustLevel param5
    ) {
        if (this.minecraft.isBlocked(param1.profileId())) {
            return false;
        } else {
            GuiMessageTag var0 = param5.createTag(param2);
            this.minecraft.gui.getChat().addMessage(param3, var0);
            this.narrateChatMessage(param0, param2, param1);
            this.logPlayerMessage(param2, param1, param4, param5);
            this.previousMessageTime = Util.getMillis();
            return true;
        }
    }

    private boolean processNonPlayerChatMessage(ChatType param0, ChatSender param1, PlayerChatMessage param2, Component param3) {
        this.minecraft.gui.getChat().addMessage(param3, GuiMessageTag.system());
        this.narrateChatMessage(param0, param2, param1);
        this.logSystemMessage(param3, param2.signature().timeStamp());
        this.previousMessageTime = Util.getMillis();
        return true;
    }

    private void narrateChatMessage(ChatType param0, PlayerChatMessage param1, ChatSender param2) {
        this.minecraft.getNarrator().sayChatNow(() -> param0.narration().decorate(param1.serverContent(), param2));
    }

    private ChatTrustLevel evaluateTrustLevel(ChatSender param0, PlayerChatMessage param1, Component param2, @Nullable PlayerInfo param3) {
        return this.isSenderLocalPlayer(param0) ? ChatTrustLevel.SECURE : ChatTrustLevel.evaluate(param1, param2, param3);
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
                this.minecraft.gui.getChat().addMessage(param0, GuiMessageTag.system());
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

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    interface Message {
        boolean accept();
    }
}
