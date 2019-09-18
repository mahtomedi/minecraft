package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ChatComponent extends GuiComponent {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft minecraft;
    private final List<String> recentChat = Lists.newArrayList();
    private final List<GuiMessage> allMessages = Lists.newArrayList();
    private final List<GuiMessage> trimmedMessages = Lists.newArrayList();
    private int chatScrollbarPos;
    private boolean newMessageSinceScroll;

    public ChatComponent(Minecraft param0) {
        this.minecraft = param0;
    }

    public void render(int param0) {
        if (this.minecraft.options.chatVisibility != ChatVisiblity.HIDDEN) {
            int var0 = this.getLinesPerPage();
            int var1 = this.trimmedMessages.size();
            if (var1 > 0) {
                boolean var2 = false;
                if (this.isChatFocused()) {
                    var2 = true;
                }

                double var3 = this.getScale();
                int var4 = Mth.ceil((double)this.getWidth() / var3);
                RenderSystem.pushMatrix();
                RenderSystem.translatef(2.0F, 8.0F, 0.0F);
                RenderSystem.scaled(var3, var3, 1.0);
                double var5 = this.minecraft.options.chatOpacity * 0.9F + 0.1F;
                double var6 = this.minecraft.options.textBackgroundOpacity;
                int var7 = 0;

                for(int var8 = 0; var8 + this.chatScrollbarPos < this.trimmedMessages.size() && var8 < var0; ++var8) {
                    GuiMessage var9 = this.trimmedMessages.get(var8 + this.chatScrollbarPos);
                    if (var9 != null) {
                        int var10 = param0 - var9.getAddedTime();
                        if (var10 < 200 || var2) {
                            double var11 = var2 ? 1.0 : getTimeFactor(var10);
                            int var12 = (int)(255.0 * var11 * var5);
                            int var13 = (int)(255.0 * var11 * var6);
                            ++var7;
                            if (var12 > 3) {
                                int var14 = 0;
                                int var15 = -var8 * 9;
                                fill(-2, var15 - 9, 0 + var4 + 4, var15, var13 << 24);
                                String var16 = var9.getMessage().getColoredString();
                                RenderSystem.enableBlend();
                                this.minecraft.font.drawShadow(var16, 0.0F, (float)(var15 - 8), 16777215 + (var12 << 24));
                                RenderSystem.disableAlphaTest();
                                RenderSystem.disableBlend();
                            }
                        }
                    }
                }

                if (var2) {
                    int var17 = 9;
                    RenderSystem.translatef(-3.0F, 0.0F, 0.0F);
                    int var18 = var1 * var17 + var1;
                    int var19 = var7 * var17 + var7;
                    int var20 = this.chatScrollbarPos * var19 / var1;
                    int var21 = var19 * var19 / var18;
                    if (var18 != var19) {
                        int var22 = var20 > 0 ? 170 : 96;
                        int var23 = this.newMessageSinceScroll ? 13382451 : 3355562;
                        fill(0, -var20, 2, -var20 - var21, var23 + (var22 << 24));
                        fill(2, -var20, 1, -var20 - var21, 13421772 + (var22 << 24));
                    }
                }

                RenderSystem.popMatrix();
            }
        }
    }

    private static double getTimeFactor(int param0) {
        double var0 = (double)param0 / 200.0;
        var0 = 1.0 - var0;
        var0 *= 10.0;
        var0 = Mth.clamp(var0, 0.0, 1.0);
        return var0 * var0;
    }

    public void clearMessages(boolean param0) {
        this.trimmedMessages.clear();
        this.allMessages.clear();
        if (param0) {
            this.recentChat.clear();
        }

    }

    public void addMessage(Component param0) {
        this.addMessage(param0, 0);
    }

    public void addMessage(Component param0, int param1) {
        this.addMessage(param0, param1, this.minecraft.gui.getGuiTicks(), false);
        LOGGER.info("[CHAT] {}", param0.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
    }

    private void addMessage(Component param0, int param1, int param2, boolean param3) {
        if (param1 != 0) {
            this.removeById(param1);
        }

        int var0 = Mth.floor((double)this.getWidth() / this.getScale());
        List<Component> var1 = ComponentRenderUtils.wrapComponents(param0, var0, this.minecraft.font, false, false);
        boolean var2 = this.isChatFocused();

        for(Component var3 : var1) {
            if (var2 && this.chatScrollbarPos > 0) {
                this.newMessageSinceScroll = true;
                this.scrollChat(1.0);
            }

            this.trimmedMessages.add(0, new GuiMessage(param2, var3, param1));
        }

        while(this.trimmedMessages.size() > 100) {
            this.trimmedMessages.remove(this.trimmedMessages.size() - 1);
        }

        if (!param3) {
            this.allMessages.add(0, new GuiMessage(param2, param0, param1));

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

    public void scrollChat(double param0) {
        this.chatScrollbarPos = (int)((double)this.chatScrollbarPos + param0);
        int var0 = this.trimmedMessages.size();
        if (this.chatScrollbarPos > var0 - this.getLinesPerPage()) {
            this.chatScrollbarPos = var0 - this.getLinesPerPage();
        }

        if (this.chatScrollbarPos <= 0) {
            this.chatScrollbarPos = 0;
            this.newMessageSinceScroll = false;
        }

    }

    @Nullable
    public Component getClickedComponentAt(double param0, double param1) {
        if (!this.isChatFocused()) {
            return null;
        } else {
            double var0 = this.getScale();
            double var1 = param0 - 2.0;
            double var2 = (double)this.minecraft.getWindow().getGuiScaledHeight() - param1 - 40.0;
            var1 = (double)Mth.floor(var1 / var0);
            var2 = (double)Mth.floor(var2 / var0);
            if (!(var1 < 0.0) && !(var2 < 0.0)) {
                int var3 = Math.min(this.getLinesPerPage(), this.trimmedMessages.size());
                if (var1 <= (double)Mth.floor((double)this.getWidth() / this.getScale()) && var2 < (double)(9 * var3 + var3)) {
                    int var4 = (int)(var2 / 9.0 + (double)this.chatScrollbarPos);
                    if (var4 >= 0 && var4 < this.trimmedMessages.size()) {
                        GuiMessage var5 = this.trimmedMessages.get(var4);
                        int var6 = 0;

                        for(Component var7 : var5.getMessage()) {
                            if (var7 instanceof TextComponent) {
                                var6 += this.minecraft.font.width(ComponentRenderUtils.stripColor(((TextComponent)var7).getText(), false));
                                if ((double)var6 > var1) {
                                    return var7;
                                }
                            }
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
    }

    public boolean isChatFocused() {
        return this.minecraft.screen instanceof ChatScreen;
    }

    public void removeById(int param0) {
        Iterator<GuiMessage> var0 = this.trimmedMessages.iterator();

        while(var0.hasNext()) {
            GuiMessage var1 = var0.next();
            if (var1.getId() == param0) {
                var0.remove();
            }
        }

        var0 = this.allMessages.iterator();

        while(var0.hasNext()) {
            GuiMessage var2 = var0.next();
            if (var2.getId() == param0) {
                var0.remove();
                break;
            }
        }

    }

    public int getWidth() {
        return getWidth(this.minecraft.options.chatWidth);
    }

    public int getHeight() {
        return getHeight(this.isChatFocused() ? this.minecraft.options.chatHeightFocused : this.minecraft.options.chatHeightUnfocused);
    }

    public double getScale() {
        return this.minecraft.options.chatScale;
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

    public int getLinesPerPage() {
        return this.getHeight() / 9;
    }
}
