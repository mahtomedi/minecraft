package net.minecraft.client.multiplayer.chat;

import com.google.common.collect.Queues;
import com.mojang.authlib.GameProfile;
import java.time.Instant;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSigner;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageHeader;
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

    public boolean removeFromDelayedMessageQueue(MessageSignature param0) {
        Iterator<ChatListener.Message> var0 = this.delayedMessageQueue.iterator();

        while(var0.hasNext()) {
            if (var0.next().getHeaderSignature().equals(param0)) {
                var0.remove();
                return true;
            }
        }

        return false;
    }

    private boolean willDelayMessages() {
        return this.messageDelay > 0L && Util.getMillis() < this.previousMessageTime + this.messageDelay;
    }

    private void handleMessage(ChatListener.Message param0) {
        if (this.willDelayMessages()) {
            this.delayedMessageQueue.add(param0);
        } else {
            param0.accept();
        }

    }

    public void handleChatMessage(PlayerChatMessage param0, ChatType.Bound param1) {
        boolean var0 = this.minecraft.options.onlyShowSecureChat().get();
        PlayerChatMessage var1 = var0 ? param0.removeUnsignedContent() : param0;
        Component var2 = param1.decorate(var1.serverContent());
        MessageSigner var3 = param0.signer();
        if (!var3.isSystem()) {
            PlayerInfo var4 = this.resolveSenderPlayer(var3.profileId());
            ChatTrustLevel var5 = this.evaluateTrustLevel(param0, var2, var4);
            if (var0 && var5.isNotSecure()) {
                return;
            }

            this.handleMessage(new ChatListener.Message(param0.headerSignature(), () -> this.processPlayerChatMessage(param1, param0, var2, var4, var5)));
        } else {
            this.handleMessage(new ChatListener.Message(param0.headerSignature(), () -> this.processNonPlayerChatMessage(param1, var1, var2)));
        }

    }

    public void handleChatHeader(SignedMessageHeader param0, MessageSignature param1, byte[] param2) {
        this.handleMessage(new ChatListener.Message(param1, () -> this.processPlayerChatHeader(param0, param1, param2)));
    }

    private boolean processPlayerChatMessage(
        ChatType.Bound param0, PlayerChatMessage param1, Component param2, @Nullable PlayerInfo param3, ChatTrustLevel param4
    ) {
        if (this.minecraft.isBlocked(param1.signer().profileId())) {
            return false;
        } else {
            GuiMessageTag var0 = param4.createTag(param1);
            this.minecraft.gui.getChat().addMessage(param2, param1.headerSignature(), var0);
            this.narrateChatMessage(param0, param1);
            this.logPlayerMessage(param1, param0, param3, param4);
            this.previousMessageTime = Util.getMillis();
            return true;
        }
    }

    private boolean processNonPlayerChatMessage(ChatType.Bound param0, PlayerChatMessage param1, Component param2) {
        this.minecraft.gui.getChat().addMessage(param2, GuiMessageTag.system());
        this.narrateChatMessage(param0, param1);
        this.logSystemMessage(param2, param1.timeStamp());
        this.previousMessageTime = Util.getMillis();
        return true;
    }

    private boolean processPlayerChatHeader(SignedMessageHeader param0, MessageSignature param1, byte[] param2) {
        PlayerInfo var0 = this.resolveSenderPlayer(param0.sender());
        if (var0 != null) {
            var0.getMessageValidator().validateHeader(param0, param1, param2);
        }

        this.logPlayerHeader(param0, param1, param2);
        return false;
    }

    private void narrateChatMessage(ChatType.Bound param0, PlayerChatMessage param1) {
        this.minecraft.getNarrator().sayChatNow(() -> param0.decorateNarration(param1.serverContent()));
    }

    private ChatTrustLevel evaluateTrustLevel(PlayerChatMessage param0, Component param1, @Nullable PlayerInfo param2) {
        return this.isSenderLocalPlayer(param0.signer().profileId()) ? ChatTrustLevel.SECURE : ChatTrustLevel.evaluate(param0, param1, param2);
    }

    private void logPlayerMessage(PlayerChatMessage param0, ChatType.Bound param1, @Nullable PlayerInfo param2, ChatTrustLevel param3) {
        GameProfile var0;
        if (param2 != null) {
            var0 = param2.getProfile();
        } else {
            var0 = new GameProfile(param0.signer().profileId(), param1.name().getString());
        }

        ChatLog var2 = this.minecraft.getReportingContext().chatLog();
        var2.push(LoggedChat.player(var0, param1.name(), param0, param3));
    }

    private void logPlayerHeader(SignedMessageHeader param0, MessageSignature param1, byte[] param2) {
    }

    @Nullable
    private PlayerInfo resolveSenderPlayer(UUID param0) {
        ClientPacketListener var0 = this.minecraft.getConnection();
        return var0 != null ? var0.getPlayerInfo(param0) : null;
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

    private boolean isSenderLocalPlayer(UUID param0) {
        if (this.minecraft.isLocalServer() && this.minecraft.player != null) {
            UUID var0 = this.minecraft.player.getGameProfile().getId();
            return var0.equals(param0);
        } else {
            return false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record Message(MessageSignature headerSignature, BooleanSupplier processMessage) {
        MessageSignature getHeaderSignature() {
            return this.headerSignature;
        }

        public boolean accept() {
            return this.processMessage.getAsBoolean();
        }
    }
}
