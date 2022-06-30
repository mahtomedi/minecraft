package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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
    private final Minecraft minecraft;
    private final List<String> recentChat = Lists.newArrayList();
    private final List<GuiMessage> allMessages = Lists.newArrayList();
    private final List<GuiMessage.Line> trimmedMessages = Lists.newArrayList();
    private final Deque<ChatComponent.QueuedMessage> chatQueue = Queues.newArrayDeque();
    private int chatScrollbarPos;
    private boolean newMessageSinceScroll;
    private long lastMessage;

    public ChatComponent(Minecraft param0) {
        this.minecraft = param0;
    }

    public void render(PoseStack param0, int param1) {
        if (!this.isChatHidden()) {
            this.processPendingMessages();
            int var0 = this.getLinesPerPage();
            int var1 = this.trimmedMessages.size();
            if (var1 > 0) {
                boolean var2 = this.isChatFocused();
                float var3 = (float)this.getScale();
                int var4 = Mth.ceil((float)this.getWidth() / var3);
                param0.pushPose();
                param0.translate(4.0, 8.0, 0.0);
                param0.scale(var3, var3, 1.0F);
                double var5 = this.minecraft.options.chatOpacity().get() * 0.9F + 0.1F;
                double var6 = this.minecraft.options.textBackgroundOpacity().get();
                double var7 = this.minecraft.options.chatLineSpacing().get();
                double var8 = 9.0 * (var7 + 1.0);
                double var9 = -8.0 * (var7 + 1.0) + 4.0 * var7;
                int var10 = 0;

                for(int var11 = 0; var11 + this.chatScrollbarPos < this.trimmedMessages.size() && var11 < var0; ++var11) {
                    GuiMessage.Line var12 = this.trimmedMessages.get(var11 + this.chatScrollbarPos);
                    if (var12 != null) {
                        int var13 = param1 - var12.addedTime();
                        if (var13 < 200 || var2) {
                            double var14 = var2 ? 1.0 : getTimeFactor(var13);
                            int var15 = (int)(255.0 * var14 * var5);
                            int var16 = (int)(255.0 * var14 * var6);
                            ++var10;
                            if (var15 > 3) {
                                int var17 = 0;
                                double var18 = (double)(-var11) * var8;
                                int var19 = (int)(var18 + var9);
                                param0.pushPose();
                                param0.translate(0.0, 0.0, 50.0);
                                fill(param0, -4, (int)(var18 - var8), 0 + var4 + 4, (int)var18, var16 << 24);
                                GuiMessageTag var20 = var12.tag();
                                if (var20 != null) {
                                    int var21 = var20.indicatorColor() | var15 << 24;
                                    fill(param0, -4, (int)(var18 - var8), -2, (int)var18, var21);
                                    if (var2 && var12.endOfEntry() && var20.icon() != null) {
                                        int var22 = this.getTagIconLeft(var12);
                                        int var23 = var19 + 9;
                                        this.drawTagIcon(param0, var22, var23, var20.icon());
                                    }
                                }

                                RenderSystem.enableBlend();
                                param0.translate(0.0, 0.0, 50.0);
                                this.minecraft.font.drawShadow(param0, var12.content(), 0.0F, (float)var19, 16777215 + (var15 << 24));
                                RenderSystem.disableBlend();
                                param0.popPose();
                            }
                        }
                    }
                }

                if (!this.chatQueue.isEmpty()) {
                    int var24 = (int)(128.0 * var5);
                    int var25 = (int)(255.0 * var6);
                    param0.pushPose();
                    param0.translate(0.0, 0.0, 50.0);
                    fill(param0, -2, 0, var4 + 4, 9, var25 << 24);
                    RenderSystem.enableBlend();
                    param0.translate(0.0, 0.0, 50.0);
                    this.minecraft.font.drawShadow(param0, Component.translatable("chat.queue", this.chatQueue.size()), 0.0F, 1.0F, 16777215 + (var24 << 24));
                    param0.popPose();
                    RenderSystem.disableBlend();
                }

                if (var2) {
                    int var26 = 9;
                    int var27 = var1 * var26;
                    int var28 = var10 * var26;
                    int var29 = this.chatScrollbarPos * var28 / var1;
                    int var30 = var28 * var28 / var27;
                    if (var27 != var28) {
                        int var31 = var29 > 0 ? 170 : 96;
                        int var32 = this.newMessageSinceScroll ? 13382451 : 3355562;
                        param0.translate(-4.0, 0.0, 0.0);
                        fill(param0, 0, -var29, 2, -var29 - var30, var32 + (var31 << 24));
                        fill(param0, 2, -var29, 1, -var29 - var30, 13421772 + (var31 << 24));
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
        this.chatQueue.clear();
        this.trimmedMessages.clear();
        this.allMessages.clear();
        if (param0) {
            this.recentChat.clear();
        }

    }

    public void addMessage(Component param0) {
        this.addMessage(param0, null);
    }

    public void addMessage(Component param0, @Nullable GuiMessageTag param1) {
        this.addMessage(param0, this.minecraft.gui.getGuiTicks(), param1, false);
        String var0 = param0.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
        String var1 = Util.mapNullable(param1, GuiMessageTag::logTag);
        if (var1 != null) {
            LOGGER.info("[{}] [CHAT] {}", var1, var0);
        } else {
            LOGGER.info("[CHAT] {}", var0);
        }

    }

    private void addMessage(Component param0, int param1, @Nullable GuiMessageTag param2, boolean param3) {
        int var0 = Mth.floor((double)this.getWidth() / this.getScale());
        if (param2 != null && param2.icon() != null) {
            var0 -= param2.icon().width + 4 + 2;
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
            this.trimmedMessages.add(0, new GuiMessage.Line(param1, var4, param2, var5));
        }

        while(this.trimmedMessages.size() > 100) {
            this.trimmedMessages.remove(this.trimmedMessages.size() - 1);
        }

        if (!param3) {
            this.allMessages.add(0, new GuiMessage(param1, param0, param2));

            while(this.allMessages.size() > 100) {
                this.allMessages.remove(this.allMessages.size() - 1);
            }
        }

    }

    public void rescaleChat() {
        this.trimmedMessages.clear();
        this.resetChatScroll();

        for(int var0 = this.allMessages.size() - 1; var0 >= 0; --var0) {
            GuiMessage var1 = this.allMessages.get(var0);
            this.addMessage(var1.content(), var1.addedTime(), var1.tag(), true);
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
        if (this.isChatFocused() && !this.minecraft.options.hideGui && !this.isChatHidden() && !this.chatQueue.isEmpty()) {
            double var0 = param0 - 2.0;
            double var1 = (double)this.minecraft.getWindow().getGuiScaledHeight() - param1 - 40.0;
            if (var0 <= (double)Mth.floor((double)this.getWidth() / this.getScale()) && var1 < 0.0 && var1 > (double)Mth.floor(-9.0 * this.getScale())) {
                ChatComponent.QueuedMessage var2 = this.chatQueue.remove();
                this.addMessage(var2.message(), var2.tag());
                this.lastMessage = System.currentTimeMillis();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Nullable
    public Style getClickedComponentStyleAt(double param0, double param1) {
        double var0 = this.screenToChatX(param0);
        if (!(var0 < 0.0) && !(var0 > (double)Mth.floor((double)this.getWidth() / this.getScale()))) {
            double var1 = this.screenToChatY(param1);
            int var2 = this.getMessageIndexAt(var1);
            if (var2 >= 0 && var2 < this.trimmedMessages.size()) {
                GuiMessage.Line var3 = this.trimmedMessages.get(var2);
                return this.minecraft.font.getSplitter().componentStyleAtWidth(var3.content(), Mth.floor(var0));
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Nullable
    public GuiMessageTag getMessageTagAt(double param0, double param1) {
        double var0 = this.screenToChatX(param0);
        double var1 = this.screenToChatY(param1);
        int var2 = this.getMessageIndexAt(var1);
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
        return (param0 - 4.0) / this.getScale();
    }

    private double screenToChatY(double param0) {
        double var0 = (double)this.minecraft.getWindow().getGuiScaledHeight() - param0 - 40.0;
        return var0 / (this.getScale() * (this.minecraft.options.chatLineSpacing().get() + 1.0));
    }

    private int getMessageIndexAt(double param0) {
        if (this.isChatFocused() && !this.minecraft.options.hideGui && !this.isChatHidden()) {
            int var0 = Math.min(this.getLinesPerPage(), this.trimmedMessages.size());
            if (param0 >= 0.0 && param0 < (double)(9 * var0 + var0)) {
                int var1 = Mth.floor(param0 / 9.0 + (double)this.chatScrollbarPos);
                if (var1 >= 0 && var1 < this.trimmedMessages.size()) {
                    return var1;
                }
            }

            return -1;
        } else {
            return -1;
        }
    }

    @Nullable
    public ChatScreen getFocusedChat() {
        Screen var2 = this.minecraft.screen;
        return var2 instanceof ChatScreen ? (ChatScreen)var2 : null;
    }

    private boolean isChatFocused() {
        return this.getFocusedChat() != null;
    }

    public int getWidth() {
        return getWidth(this.minecraft.options.chatWidth().get());
    }

    public int getHeight() {
        return getHeight(
            this.isChatFocused() ? this.minecraft.options.chatHeightFocused().get() : this.minecraft.options.chatHeightUnfocused().get()
                / (this.minecraft.options.chatLineSpacing().get() + 1.0)
        );
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
        return this.getHeight() / 9;
    }

    private long getChatRateMillis() {
        return (long)(this.minecraft.options.chatDelay().get() * 1000.0);
    }

    private void processPendingMessages() {
        if (!this.chatQueue.isEmpty()) {
            long var0 = System.currentTimeMillis();
            if (var0 - this.lastMessage >= this.getChatRateMillis()) {
                ChatComponent.QueuedMessage var1 = this.chatQueue.remove();
                this.addMessage(var1.message(), var1.tag());
                this.lastMessage = var0;
            }

        }
    }

    public void enqueueMessage(Component param0) {
        this.enqueueMessage(param0, null);
    }

    public void enqueueMessage(Component param0, @Nullable GuiMessageTag param1) {
        if (this.minecraft.options.chatDelay().get() <= 0.0) {
            this.addMessage(param0, param1);
        } else {
            long var0 = System.currentTimeMillis();
            if (var0 - this.lastMessage >= this.getChatRateMillis()) {
                this.addMessage(param0);
                this.lastMessage = var0;
            } else {
                this.chatQueue.add(new ChatComponent.QueuedMessage(param0, param1));
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    static record QueuedMessage(Component message, @Nullable GuiMessageTag tag) {
    }
}
