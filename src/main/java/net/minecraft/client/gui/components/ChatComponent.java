package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ChatComponent extends GuiComponent {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_CHAT_HISTORY = 100;
    private static final int MESSAGE_NOT_FOUND = -1;
    private static final int MESSAGE_INDENT = 4;
    private static final int MESSAGE_TAG_MARGIN_LEFT = 4;
    private static final int BOTTOM_MARGIN = 40;
    private static final int TIME_BEFORE_MESSAGE_DELETION = 60;
    private static final Component DELETED_CHAT_MESSAGE = Component.translatable("chat.deleted_marker").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    private final Minecraft minecraft;
    private final List<String> recentChat = Lists.newArrayList();
    private final List<GuiMessage> allMessages = Lists.newArrayList();
    private final List<GuiMessage.Line> trimmedMessages = Lists.newArrayList();
    private int chatScrollbarPos;
    private boolean newMessageSinceScroll;
    private final List<ChatComponent.DelayedMessageDeletion> messageDeletionQueue = new ArrayList<>();

    public ChatComponent(Minecraft param0) {
        this.minecraft = param0;
    }

    public void tick() {
        if (!this.messageDeletionQueue.isEmpty()) {
            this.processMessageDeletionQueue();
        }

    }

    public void render(PoseStack param0, int param1, int param2, int param3) {
        if (!this.isChatHidden()) {
            int var0 = this.getLinesPerPage();
            int var1 = this.trimmedMessages.size();
            if (var1 > 0) {
                boolean var2 = this.isChatFocused();
                float var3 = (float)this.getScale();
                int var4 = Mth.ceil((float)this.getWidth() / var3);
                int var5 = this.minecraft.getWindow().getGuiScaledHeight();
                param0.pushPose();
                param0.scale(var3, var3, 1.0F);
                param0.translate(4.0F, 0.0F, 0.0F);
                int var6 = Mth.floor((float)(var5 - 40) / var3);
                int var7 = this.getMessageEndIndexAt(this.screenToChatX((double)param2), this.screenToChatY((double)param3));
                double var8 = this.minecraft.options.chatOpacity().get() * 0.9F + 0.1F;
                double var9 = this.minecraft.options.textBackgroundOpacity().get();
                double var10 = this.minecraft.options.chatLineSpacing().get();
                int var11 = this.getLineHeight();
                int var12 = (int)Math.round(-8.0 * (var10 + 1.0) + 4.0 * var10);
                int var13 = 0;

                for(int var14 = 0; var14 + this.chatScrollbarPos < this.trimmedMessages.size() && var14 < var0; ++var14) {
                    int var15 = var14 + this.chatScrollbarPos;
                    GuiMessage.Line var16 = this.trimmedMessages.get(var15);
                    if (var16 != null) {
                        int var17 = param1 - var16.addedTime();
                        if (var17 < 200 || var2) {
                            double var18 = var2 ? 1.0 : getTimeFactor(var17);
                            int var19 = (int)(255.0 * var18 * var8);
                            int var20 = (int)(255.0 * var18 * var9);
                            ++var13;
                            if (var19 > 3) {
                                int var21 = 0;
                                int var22 = var6 - var14 * var11;
                                int var23 = var22 + var12;
                                param0.pushPose();
                                param0.translate(0.0F, 0.0F, 50.0F);
                                fill(param0, -4, var22 - var11, 0 + var4 + 4 + 4, var22, var20 << 24);
                                GuiMessageTag var24 = var16.tag();
                                if (var24 != null) {
                                    int var25 = var24.indicatorColor() | var19 << 24;
                                    fill(param0, -4, var22 - var11, -2, var22, var25);
                                    if (var15 == var7 && var24.icon() != null) {
                                        int var26 = this.getTagIconLeft(var16);
                                        int var27 = var23 + 9;
                                        this.drawTagIcon(param0, var26, var27, var24.icon());
                                    }
                                }

                                param0.translate(0.0F, 0.0F, 50.0F);
                                this.minecraft.font.drawShadow(param0, var16.content(), 0.0F, (float)var23, 16777215 + (var19 << 24));
                                param0.popPose();
                            }
                        }
                    }
                }

                long var28 = this.minecraft.getChatListener().queueSize();
                if (var28 > 0L) {
                    int var29 = (int)(128.0 * var8);
                    int var30 = (int)(255.0 * var9);
                    param0.pushPose();
                    param0.translate(0.0F, (float)var6, 50.0F);
                    fill(param0, -2, 0, var4 + 4, 9, var30 << 24);
                    param0.translate(0.0F, 0.0F, 50.0F);
                    this.minecraft.font.drawShadow(param0, Component.translatable("chat.queue", var28), 0.0F, 1.0F, 16777215 + (var29 << 24));
                    param0.popPose();
                }

                if (var2) {
                    int var31 = this.getLineHeight();
                    int var32 = var1 * var31;
                    int var33 = var13 * var31;
                    int var34 = this.chatScrollbarPos * var33 / var1 - var6;
                    int var35 = var33 * var33 / var32;
                    if (var32 != var33) {
                        int var36 = var34 > 0 ? 170 : 96;
                        int var37 = this.newMessageSinceScroll ? 13382451 : 3355562;
                        int var38 = var4 + 4;
                        fill(param0, var38, -var34, var38 + 2, -var34 - var35, var37 + (var36 << 24));
                        fill(param0, var38 + 2, -var34, var38 + 1, -var34 - var35, 13421772 + (var36 << 24));
                    }
                }

                param0.popPose();
            }
        }
    }

    private void drawTagIcon(PoseStack param0, int param1, int param2, GuiMessageTag.Icon param3) {
        int var0 = param2 - param3.height - 1;
        param3.draw(param0, param1, var0);
    }

    private int getTagIconLeft(GuiMessage.Line param0) {
        return this.minecraft.font.width(param0.content()) + 4;
    }

    private boolean isChatHidden() {
        return this.minecraft.options.chatVisibility().get() == ChatVisiblity.HIDDEN;
    }

    private static double getTimeFactor(int param0) {
        double var0 = (double)param0 / 200.0;
        var0 = 1.0 - var0;
        var0 *= 10.0;
        var0 = Mth.clamp(var0, 0.0, 1.0);
        return var0 * var0;
    }

    public void clearMessages(boolean param0) {
        this.minecraft.getChatListener().clearQueue();
        this.messageDeletionQueue.clear();
        this.trimmedMessages.clear();
        this.allMessages.clear();
        if (param0) {
            this.recentChat.clear();
        }

    }

    public void addMessage(Component param0) {
        this.addMessage(param0, null, this.minecraft.isSingleplayer() ? GuiMessageTag.systemSinglePlayer() : GuiMessageTag.system());
    }

    public void addMessage(Component param0, @Nullable MessageSignature param1, @Nullable GuiMessageTag param2) {
        this.logChatMessage(param0, param2);
        this.addMessage(param0, param1, this.minecraft.gui.getGuiTicks(), param2, false);
    }

    private void logChatMessage(Component param0, @Nullable GuiMessageTag param1) {
        String var0 = param0.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
        String var1 = Optionull.map(param1, GuiMessageTag::logTag);
        if (var1 != null) {
            LOGGER.info("[{}] [CHAT] {}", var1, var0);
        } else {
            LOGGER.info("[CHAT] {}", var0);
        }

    }

    private void addMessage(Component param0, @Nullable MessageSignature param1, int param2, @Nullable GuiMessageTag param3, boolean param4) {
        int var0 = Mth.floor((double)this.getWidth() / this.getScale());
        if (param3 != null && param3.icon() != null) {
            var0 -= param3.icon().width + 4 + 2;
        }

        List<FormattedCharSequence> var1 = ComponentRenderUtils.wrapComponents(param0, var0, this.minecraft.font);
        boolean var2 = this.isChatFocused();

        for(int var3 = 0; var3 < var1.size(); ++var3) {
            FormattedCharSequence var4 = var1.get(var3);
            if (var2 && this.chatScrollbarPos > 0) {
                this.newMessageSinceScroll = true;
                this.scrollChat(1);
            }

            boolean var5 = var3 == var1.size() - 1;
            this.trimmedMessages.add(0, new GuiMessage.Line(param2, var4, param3, var5));
        }

        while(this.trimmedMessages.size() > 100) {
            this.trimmedMessages.remove(this.trimmedMessages.size() - 1);
        }

        if (!param4) {
            this.allMessages.add(0, new GuiMessage(param2, param0, param1, param3));

            while(this.allMessages.size() > 100) {
                this.allMessages.remove(this.allMessages.size() - 1);
            }
        }

    }

    private void processMessageDeletionQueue() {
        int var0 = this.minecraft.gui.getGuiTicks();
        this.messageDeletionQueue.removeIf(param1 -> {
            if (var0 >= param1.deletableAfter()) {
                return this.deleteMessageOrDelay(param1.signature()) == null;
            } else {
                return false;
            }
        });
    }

    public void deleteMessage(MessageSignature param0) {
        ChatComponent.DelayedMessageDeletion var0 = this.deleteMessageOrDelay(param0);
        if (var0 != null) {
            this.messageDeletionQueue.add(var0);
        }

    }

    @Nullable
    private ChatComponent.DelayedMessageDeletion deleteMessageOrDelay(MessageSignature param0) {
        int var0 = this.minecraft.gui.getGuiTicks();
        ListIterator<GuiMessage> var1 = this.allMessages.listIterator();

        while(var1.hasNext()) {
            GuiMessage var2 = var1.next();
            if (param0.equals(var2.signature())) {
                int var3 = var2.addedTime() + 60;
                if (var0 >= var3) {
                    var1.set(this.createDeletedMarker(var2));
                    this.refreshTrimmedMessage();
                    return null;
                }

                return new ChatComponent.DelayedMessageDeletion(param0, var3);
            }
        }

        return null;
    }

    private GuiMessage createDeletedMarker(GuiMessage param0) {
        return new GuiMessage(param0.addedTime(), DELETED_CHAT_MESSAGE, null, GuiMessageTag.system());
    }

    public void rescaleChat() {
        this.resetChatScroll();
        this.refreshTrimmedMessage();
    }

    private void refreshTrimmedMessage() {
        this.trimmedMessages.clear();

        for(int var0 = this.allMessages.size() - 1; var0 >= 0; --var0) {
            GuiMessage var1 = this.allMessages.get(var0);
            this.addMessage(var1.content(), var1.signature(), var1.addedTime(), var1.tag(), true);
        }

    }

    public List<String> getRecentChat() {
        return this.recentChat;
    }

    public void addRecentChat(String param0) {
        if (this.recentChat.isEmpty() || !this.recentChat.get(this.recentChat.size() - 1).equals(param0)) {
            this.recentChat.add(param0);
        }

    }

    public void resetChatScroll() {
        this.chatScrollbarPos = 0;
        this.newMessageSinceScroll = false;
    }

    public void scrollChat(int param0) {
        this.chatScrollbarPos += param0;
        int var0 = this.trimmedMessages.size();
        if (this.chatScrollbarPos > var0 - this.getLinesPerPage()) {
            this.chatScrollbarPos = var0 - this.getLinesPerPage();
        }

        if (this.chatScrollbarPos <= 0) {
            this.chatScrollbarPos = 0;
            this.newMessageSinceScroll = false;
        }

    }

    public boolean handleChatQueueClicked(double param0, double param1) {
        if (this.isChatFocused() && !this.minecraft.options.hideGui && !this.isChatHidden()) {
            ChatListener var0 = this.minecraft.getChatListener();
            if (var0.queueSize() == 0L) {
                return false;
            } else {
                double var1 = param0 - 2.0;
                double var2 = (double)this.minecraft.getWindow().getGuiScaledHeight() - param1 - 40.0;
                if (var1 <= (double)Mth.floor((double)this.getWidth() / this.getScale()) && var2 < 0.0 && var2 > (double)Mth.floor(-9.0 * this.getScale())) {
                    var0.acceptNextDelayedMessage();
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    @Nullable
    public Style getClickedComponentStyleAt(double param0, double param1) {
        double var0 = this.screenToChatX(param0);
        double var1 = this.screenToChatY(param1);
        int var2 = this.getMessageLineIndexAt(var0, var1);
        if (var2 >= 0 && var2 < this.trimmedMessages.size()) {
            GuiMessage.Line var3 = this.trimmedMessages.get(var2);
            return this.minecraft.font.getSplitter().componentStyleAtWidth(var3.content(), Mth.floor(var0));
        } else {
            return null;
        }
    }

    @Nullable
    public GuiMessageTag getMessageTagAt(double param0, double param1) {
        double var0 = this.screenToChatX(param0);
        double var1 = this.screenToChatY(param1);
        int var2 = this.getMessageEndIndexAt(var0, var1);
        if (var2 >= 0 && var2 < this.trimmedMessages.size()) {
            GuiMessage.Line var3 = this.trimmedMessages.get(var2);
            GuiMessageTag var4 = var3.tag();
            if (var4 != null && this.hasSelectedMessageTag(var0, var3, var4)) {
                return var4;
            }
        }

        return null;
    }

    private boolean hasSelectedMessageTag(double param0, GuiMessage.Line param1, GuiMessageTag param2) {
        if (param0 < 0.0) {
            return true;
        } else {
            GuiMessageTag.Icon var0 = param2.icon();
            if (var0 == null) {
                return false;
            } else {
                int var1 = this.getTagIconLeft(param1);
                int var2 = var1 + var0.width;
                return param0 >= (double)var1 && param0 <= (double)var2;
            }
        }
    }

    private double screenToChatX(double param0) {
        return param0 / this.getScale() - 4.0;
    }

    private double screenToChatY(double param0) {
        double var0 = (double)this.minecraft.getWindow().getGuiScaledHeight() - param0 - 40.0;
        return var0 / (this.getScale() * (double)this.getLineHeight());
    }

    private int getMessageEndIndexAt(double param0, double param1) {
        int var0 = this.getMessageLineIndexAt(param0, param1);
        if (var0 == -1) {
            return -1;
        } else {
            while(var0 >= 0) {
                if (this.trimmedMessages.get(var0).endOfEntry()) {
                    return var0;
                }

                --var0;
            }

            return var0;
        }
    }

    private int getMessageLineIndexAt(double param0, double param1) {
        if (this.isChatFocused() && !this.minecraft.options.hideGui && !this.isChatHidden()) {
            if (!(param0 < -4.0) && !(param0 > (double)Mth.floor((double)this.getWidth() / this.getScale()))) {
                int var0 = Math.min(this.getLinesPerPage(), this.trimmedMessages.size());
                if (param1 >= 0.0 && param1 < (double)var0) {
                    int var1 = Mth.floor(param1 + (double)this.chatScrollbarPos);
                    if (var1 >= 0 && var1 < this.trimmedMessages.size()) {
                        return var1;
                    }
                }

                return -1;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    private boolean isChatFocused() {
        return this.minecraft.screen instanceof ChatScreen;
    }

    public int getWidth() {
        return getWidth(this.minecraft.options.chatWidth().get());
    }

    public int getHeight() {
        return getHeight(this.isChatFocused() ? this.minecraft.options.chatHeightFocused().get() : this.minecraft.options.chatHeightUnfocused().get());
    }

    public double getScale() {
        return this.minecraft.options.chatScale().get();
    }

    public static int getWidth(double param0) {
        int var0 = 320;
        int var1 = 40;
        return Mth.floor(param0 * 280.0 + 40.0);
    }

    public static int getHeight(double param0) {
        int var0 = 180;
        int var1 = 20;
        return Mth.floor(param0 * 160.0 + 20.0);
    }

    public static double defaultUnfocusedPct() {
        int var0 = 180;
        int var1 = 20;
        return 70.0 / (double)(getHeight(1.0) - 20);
    }

    public int getLinesPerPage() {
        return this.getHeight() / this.getLineHeight();
    }

    private int getLineHeight() {
        return (int)(9.0 * (this.minecraft.options.chatLineSpacing().get() + 1.0));
    }

    @OnlyIn(Dist.CLIENT)
    static record DelayedMessageDeletion(MessageSignature signature, int deletableAfter) {
    }
}
