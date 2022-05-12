package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.GuiMessage;
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
    private final Minecraft minecraft;
    private final List<String> recentChat = Lists.newArrayList();
    private final List<GuiMessage<Component>> allMessages = Lists.newArrayList();
    private final List<GuiMessage<FormattedCharSequence>> trimmedMessages = Lists.newArrayList();
    private final Deque<Component> chatQueue = Queues.newArrayDeque();
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
                    GuiMessage<FormattedCharSequence> var12 = this.trimmedMessages.get(var11 + this.chatScrollbarPos);
                    if (var12 != null) {
                        int var13 = param1 - var12.getAddedTime();
                        if (var13 < 200 || var2) {
                            double var14 = var2 ? 1.0 : getTimeFactor(var13);
                            int var15 = (int)(255.0 * var14 * var5);
                            int var16 = (int)(255.0 * var14 * var6);
                            ++var10;
                            if (var15 > 3) {
                                int var17 = 0;
                                double var18 = (double)(-var11) * var8;
                                param0.pushPose();
                                param0.translate(0.0, 0.0, 50.0);
                                fill(param0, -4, (int)(var18 - var8), 0 + var4 + 4, (int)var18, var16 << 24);
                                RenderSystem.enableBlend();
                                param0.translate(0.0, 0.0, 50.0);
                                this.minecraft.font.drawShadow(param0, var12.getMessage(), 0.0F, (float)((int)(var18 + var9)), 16777215 + (var15 << 24));
                                RenderSystem.disableBlend();
                                param0.popPose();
                            }
                        }
                    }
                }

                if (!this.chatQueue.isEmpty()) {
                    int var19 = (int)(128.0 * var5);
                    int var20 = (int)(255.0 * var6);
                    param0.pushPose();
                    param0.translate(0.0, 0.0, 50.0);
                    fill(param0, -2, 0, var4 + 4, 9, var20 << 24);
                    RenderSystem.enableBlend();
                    param0.translate(0.0, 0.0, 50.0);
                    this.minecraft.font.drawShadow(param0, Component.translatable("chat.queue", this.chatQueue.size()), 0.0F, 1.0F, 16777215 + (var19 << 24));
                    param0.popPose();
                    RenderSystem.disableBlend();
                }

                if (var2) {
                    int var21 = 9;
                    int var22 = var1 * var21;
                    int var23 = var10 * var21;
                    int var24 = this.chatScrollbarPos * var23 / var1;
                    int var25 = var23 * var23 / var22;
                    if (var22 != var23) {
                        int var26 = var24 > 0 ? 170 : 96;
                        int var27 = this.newMessageSinceScroll ? 13382451 : 3355562;
                        param0.translate(-4.0, 0.0, 0.0);
                        fill(param0, 0, -var24, 2, -var24 - var25, var27 + (var26 << 24));
                        fill(param0, 2, -var24, 1, -var24 - var25, 13421772 + (var26 << 24));
                    }
                }

                param0.popPose();
            }
        }
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
        this.addMessage(param0, 0);
    }

    private void addMessage(Component param0, int param1) {
        this.addMessage(param0, param1, this.minecraft.gui.getGuiTicks(), false);
        LOGGER.info("[CHAT] {}", param0.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
    }

    private void addMessage(Component param0, int param1, int param2, boolean param3) {
        if (param1 != 0) {
            this.removeById(param1);
        }

        int var0 = Mth.floor((double)this.getWidth() / this.getScale());
        List<FormattedCharSequence> var1 = ComponentRenderUtils.wrapComponents(param0, var0, this.minecraft.font);
        boolean var2 = this.isChatFocused();

        for(FormattedCharSequence var3 : var1) {
            if (var2 && this.chatScrollbarPos > 0) {
                this.newMessageSinceScroll = true;
                this.scrollChat(1);
            }

            this.trimmedMessages.add(0, new GuiMessage<>(param2, var3, param1));
        }

        while(this.trimmedMessages.size() > 100) {
            this.trimmedMessages.remove(this.trimmedMessages.size() - 1);
        }

        if (!param3) {
            this.allMessages.add(0, new GuiMessage<>(param2, param0, param1));

            while(this.allMessages.size() > 100) {
                this.allMessages.remove(this.allMessages.size() - 1);
            }
        }

    }

    public void rescaleChat() {
        this.trimmedMessages.clear();
        this.resetChatScroll();

        for(int var0 = this.allMessages.size() - 1; var0 >= 0; --var0) {
            GuiMessage<Component> var1 = this.allMessages.get(var0);
            this.addMessage(var1.getMessage(), var1.getId(), var1.getAddedTime(), true);
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
                this.addMessage(this.chatQueue.remove());
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
        if (this.isChatFocused() && !this.minecraft.options.hideGui && !this.isChatHidden()) {
            double var0 = param0 - 2.0;
            double var1 = (double)this.minecraft.getWindow().getGuiScaledHeight() - param1 - 40.0;
            var0 = (double)Mth.floor(var0 / this.getScale());
            var1 = (double)Mth.floor(var1 / (this.getScale() * (this.minecraft.options.chatLineSpacing().get() + 1.0)));
            if (!(var0 < 0.0) && !(var1 < 0.0)) {
                int var2 = Math.min(this.getLinesPerPage(), this.trimmedMessages.size());
                if (var0 <= (double)Mth.floor((double)this.getWidth() / this.getScale()) && var1 < (double)(9 * var2 + var2)) {
                    int var3 = (int)(var1 / 9.0 + (double)this.chatScrollbarPos);
                    if (var3 >= 0 && var3 < this.trimmedMessages.size()) {
                        GuiMessage<FormattedCharSequence> var4 = this.trimmedMessages.get(var3);
                        return this.minecraft.font.getSplitter().componentStyleAtWidth(var4.getMessage(), (int)var0);
                    }
                }

                return null;
            } else {
                return null;
            }
        } else {
            return null;
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

    private void removeById(int param0) {
        this.trimmedMessages.removeIf(param1 -> param1.getId() == param0);
        this.allMessages.removeIf(param1 -> param1.getId() == param0);
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
                this.addMessage(this.chatQueue.remove());
                this.lastMessage = var0;
            }

        }
    }

    public void enqueueMessage(Component param0) {
        if (this.minecraft.options.chatDelay().get() <= 0.0) {
            this.addMessage(param0);
        } else {
            long var0 = System.currentTimeMillis();
            if (var0 - this.lastMessage >= this.getChatRateMillis()) {
                this.addMessage(param0);
                this.lastMessage = var0;
            } else {
                this.chatQueue.add(param0);
            }
        }

    }
}
