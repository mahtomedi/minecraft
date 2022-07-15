package net.minecraft.client.multiplayer.chat;

import com.google.common.collect.Queues;
import com.mojang.authlib.GameProfile;
import java.time.Instant;
import java.util.Deque;
import java.util.UUID;
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

    public long queueSize() {
        return this.delayedMessageQueue.stream().filter(ChatListener.Message::isVisible).count();
    }

    public void clearQueue() {
        this.delayedMessageQueue.forEach(param0 -> {
            param0.remove();
            param0.accept();
        });
        this.delayedMessageQueue.clear();
    }

    public boolean removeFromDelayedMessageQueue(MessageSignature param0) {
        for(ChatListener.Message var0 : this.delayedMessageQueue) {
            if (var0.removeIfSignatureMatches(param0)) {
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

    public void handleChatMessage(final PlayerChatMessage param0, final ChatType.Bound param1) {
        final boolean var0 = this.minecraft.options.onlyShowSecureChat().get();
        final PlayerChatMessage var1 = var0 ? param0.removeUnsignedContent() : param0;
        final Component var2 = param1.decorate(var1.serverContent());
        MessageSigner var3 = param0.signer();
        if (!var3.isSystem()) {
            final PlayerInfo var4 = this.resolveSenderPlayer(var3.profileId());
            final Instant var5 = Instant.now();
            this.handleMessage(new ChatListener.Message() {
                private boolean removed;

                @Override
                public boolean accept() {
                    if (this.removed) {
                        byte[] var0 = param0.signedBody().hash().asBytes();
                        ChatListener.this.processPlayerChatHeader(param0.signedHeader(), param0.headerSignature(), var0);
                        return false;
                    } else {
                        return ChatListener.this.processPlayerChatMessage(param1, param0, var2, var4, var0, var5);
                    }
                }

                @Override
                public boolean removeIfSignatureMatches(MessageSignature param0x) {
                    if (param0.headerSignature().equals(param0)) {
                        this.removed = true;
                        return true;
                    } else {
                        return false;
                    }
                }

                @Override
                public void remove() {
                    this.removed = true;
                }

                @Override
                public boolean isVisible() {
                    return !this.removed;
                }
            });
        } else {
            this.handleMessage(new ChatListener.Message() {
                @Override
                public boolean accept() {
                    return ChatListener.this.processNonPlayerChatMessage(param1, var1, var2);
                }

                @Override
                public boolean isVisible() {
                    return true;
                }
            });
        }

    }

    public void handleChatHeader(SignedMessageHeader param0, MessageSignature param1, byte[] param2) {
        this.handleMessage(() -> this.processPlayerChatHeader(param0, param1, param2));
    }

    boolean processPlayerChatMessage(
        ChatType.Bound param0, PlayerChatMessage param1, Component param2, @Nullable PlayerInfo param3, boolean param4, Instant param5
    ) {
        boolean var0 = this.showMessageToPlayer(param0, param1, param2, param3, param4, param5);
        ClientPacketListener var1 = this.minecraft.getConnection();
        if (var1 != null) {
            var1.markMessageAsProcessed(param1, var0);
        }

        return var0;
    }

    private boolean showMessageToPlayer(
        ChatType.Bound param0, PlayerChatMessage param1, Component param2, @Nullable PlayerInfo param3, boolean param4, Instant param5
    ) {
        ChatTrustLevel var0 = this.evaluateTrustLevel(param1, param2, param3, param5);
        if (param4 && var0.isNotSecure()) {
            return false;
        } else if (this.minecraft.isBlocked(param1.signer().profileId())) {
            return false;
        } else {
            GuiMessageTag var1 = var0.createTag(param1);
            this.minecraft.gui.getChat().addMessage(param2, param1.headerSignature(), var1);
            this.narrateChatMessage(param0, param1);
            this.logPlayerMessage(param1, param0, param3, var0);
            this.previousMessageTime = Util.getMillis();
            return true;
        }
    }

    boolean processNonPlayerChatMessage(ChatType.Bound param0, PlayerChatMessage param1, Component param2) {
        this.minecraft.gui.getChat().addMessage(param2, GuiMessageTag.system());
        this.narrateChatMessage(param0, param1);
        this.logSystemMessage(param2, param1.timeStamp());
        this.previousMessageTime = Util.getMillis();
        return true;
    }

    boolean processPlayerChatHeader(SignedMessageHeader param0, MessageSignature param1, byte[] param2) {
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

    private ChatTrustLevel evaluateTrustLevel(PlayerChatMessage param0, Component param1, @Nullable PlayerInfo param2, Instant param3) {
        return this.isSenderLocalPlayer(param0.signer().profileId()) ? ChatTrustLevel.SECURE : ChatTrustLevel.evaluate(param0, param1, param2, param3);
    }

    private void logPlayerMessage(PlayerChatMessage param0, ChatType.Bound param1, @Nullable PlayerInfo param2, ChatTrustLevel param3) {
        GameProfile var0;
        if (param2 != null) {
            var0 = param2.getProfile();
        } else {
            var0 = new GameProfile(param0.signer().profileId(), param1.name().getString());
        }

        ChatLog var2 = this.minecraft.getReportingContext().chatLog();
        var2.push(LoggedChatMessage.player(var0, param1.name(), param0, param3));
    }

    private void logSystemMessage(Component param0, Instant param1) {
        ChatLog var0 = this.minecraft.getReportingContext().chatLog();
        var0.push(LoggedChatMessage.system(param0, param1));
    }

    private void logPlayerHeader(SignedMessageHeader param0, MessageSignature param1, byte[] param2) {
        ChatLog var0 = this.minecraft.getReportingContext().chatLog();
        var0.push(LoggedChatMessageLink.header(param0, param1, param2));
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

    private boolean isSenderLocalPlayer(UUID param0) {
        if (this.minecraft.isLocalServer() && this.minecraft.player != null) {
            UUID var0 = this.minecraft.player.getGameProfile().getId();
            return var0.equals(param0);
        } else {
            return false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    interface Message {
        default boolean removeIfSignatureMatches(MessageSignature param0) {
            return false;
        }

        default void remove() {
        }

        boolean accept();

        default boolean isVisible() {
            return false;
        }
    }
}
