package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.ClientChatPreview;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class ChatScreen extends Screen {
    public static final double MOUSE_SCROLL_SPEED = 7.0;
    private static final Component USAGE_TEXT = Component.translatable("chat_screen.usage");
    private static final int PREVIEW_MARGIN_SIDES = 2;
    private static final int PREVIEW_PADDING = 2;
    private static final int PREVIEW_MARGIN_BOTTOM = 15;
    private static final Component PREVIEW_WARNING_TITLE = Component.translatable("chatPreview.warning.toast.title");
    private static final Component PREVIEW_WARNING_TOAST = Component.translatable("chatPreview.warning.toast");
    private static final Component PREVIEW_HINT = Component.translatable("chat.preview").withStyle(ChatFormatting.DARK_GRAY);
    private String historyBuffer = "";
    private int historyPos = -1;
    protected EditBox input;
    private final String initial;
    CommandSuggestions commandSuggestions;
    private ClientChatPreview chatPreview;

    public ChatScreen(String param0) {
        super(Component.translatable("chat_screen.title"));
        this.initial = param0;
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.historyPos = this.minecraft.gui.getChat().getRecentChat().size();
        this.input = new EditBox(this.font, 4, this.height - 12, this.width - 4, 12, Component.translatable("chat.editBox")) {
            @Override
            protected MutableComponent createNarrationMessage() {
                return super.createNarrationMessage().append(ChatScreen.this.commandSuggestions.getNarrationMessage());
            }
        };
        this.input.setMaxLength(256);
        this.input.setBordered(false);
        this.input.setValue(this.initial);
        this.input.setResponder(this::onEdited);
        this.addWidget(this.input);
        this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.input, this.font, false, false, 1, 10, true, -805306368);
        this.commandSuggestions.updateCommandInfo();
        this.setInitialFocus(this.input);
        this.chatPreview = new ClientChatPreview(this.minecraft);
        this.updateChatPreview(this.input.getValue());
        ServerData var0 = this.minecraft.getCurrentServer();
        if (var0 != null) {
            ServerData.ChatPreview var1 = var0.getChatPreview();
            if (var1 != null && var1.showToast()) {
                ServerList.saveSingleServer(var0);
                SystemToast var2 = SystemToast.multiline(
                    this.minecraft, SystemToast.SystemToastIds.CHAT_PREVIEW_WARNING, PREVIEW_WARNING_TITLE, PREVIEW_WARNING_TOAST
                );
                this.minecraft.getToasts().addToast(var2);
            }
        }

    }

    @Override
    public void resize(Minecraft param0, int param1, int param2) {
        String var0 = this.input.getValue();
        this.init(param0, param1, param2);
        this.setChatLine(var0);
        this.commandSuggestions.updateCommandInfo();
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        this.minecraft.gui.getChat().resetChatScroll();
    }

    @Override
    public void tick() {
        this.input.tick();
        this.chatPreview.tick();
    }

    private void onEdited(String param0) {
        String var0x = this.input.getValue();
        this.commandSuggestions.setAllowSuggestions(!var0x.equals(this.initial));
        this.commandSuggestions.updateCommandInfo();
        this.updateChatPreview(var0x);
    }

    private void updateChatPreview(String param0) {
        String var0 = this.normalizeChatMessage(param0);
        if (this.sendsChatPreviewRequests() && !var0.startsWith("/")) {
            this.chatPreview.request(var0);
        } else {
            this.chatPreview.clear();
        }

    }

    private boolean sendsChatPreviewRequests() {
        if (this.minecraft.player == null) {
            return false;
        } else if (!this.minecraft.options.chatPreview().get()) {
            return false;
        } else {
            ServerData var0 = this.minecraft.getCurrentServer();
            return var0 != null && var0.previewsChat();
        }
    }

    @Override
    public boolean keyReleased(int param0, int param1, int param2) {
        if (super.keyReleased(param0, param1, param2)) {
            return true;
        } else if (param0 != 257 && param0 != 335) {
            return false;
        } else {
            this.handleChatInput(this.input.getValue(), true);
            this.minecraft.setScreen(null);
            return true;
        }
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (this.commandSuggestions.keyPressed(param0, param1, param2)) {
            return true;
        } else if (super.keyPressed(param0, param1, param2)) {
            return true;
        } else if (param0 == 256) {
            this.minecraft.setScreen(null);
            return true;
        } else if (param0 == 265) {
            this.moveInHistory(-1);
            return true;
        } else if (param0 == 264) {
            this.moveInHistory(1);
            return true;
        } else if (param0 == 266) {
            this.minecraft.gui.getChat().scrollChat(this.minecraft.gui.getChat().getLinesPerPage() - 1);
            return true;
        } else if (param0 == 267) {
            this.minecraft.gui.getChat().scrollChat(-this.minecraft.gui.getChat().getLinesPerPage() + 1);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2) {
        param2 = Mth.clamp(param2, -1.0, 1.0);
        if (this.commandSuggestions.mouseScrolled(param2)) {
            return true;
        } else {
            if (!hasShiftDown()) {
                param2 *= 7.0;
            }

            this.minecraft.gui.getChat().scrollChat((int)param2);
            return true;
        }
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (this.commandSuggestions.mouseClicked((double)((int)param0), (double)((int)param1), param2)) {
            return true;
        } else {
            if (param2 == 0) {
                ChatComponent var0 = this.minecraft.gui.getChat();
                if (var0.handleChatQueueClicked(param0, param1)) {
                    return true;
                }

                Style var1 = this.getComponentStyleAt(param0, param1);
                if (var1 != null && this.handleComponentClicked(var1)) {
                    return true;
                }
            }

            return this.input.mouseClicked(param0, param1, param2) ? true : super.mouseClicked(param0, param1, param2);
        }
    }

    @Override
    protected void insertText(String param0, boolean param1) {
        if (param1) {
            this.input.setValue(param0);
        } else {
            this.input.insertText(param0);
        }

    }

    public void moveInHistory(int param0) {
        int var0 = this.historyPos + param0;
        int var1 = this.minecraft.gui.getChat().getRecentChat().size();
        var0 = Mth.clamp(var0, 0, var1);
        if (var0 != this.historyPos) {
            if (var0 == var1) {
                this.historyPos = var1;
                this.input.setValue(this.historyBuffer);
            } else {
                if (this.historyPos == var1) {
                    this.historyBuffer = this.input.getValue();
                }

                this.input.setValue(this.minecraft.gui.getChat().getRecentChat().get(var0));
                this.commandSuggestions.setAllowSuggestions(false);
                this.historyPos = var0;
            }
        }
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.setFocused(this.input);
        this.input.setFocus(true);
        fill(param0, 2, this.height - 14, this.width - 2, this.height - 2, this.minecraft.options.getBackgroundColor(Integer.MIN_VALUE));
        this.input.render(param0, param1, param2, param3);
        if (this.chatPreview.isActive()) {
            this.renderChatPreview(param0);
        }

        this.commandSuggestions.render(param0, param1, param2);
        Style var0 = this.getComponentStyleAt((double)param1, (double)param2);
        if (var0 != null && var0.getHoverEvent() != null) {
            this.renderComponentHoverEffect(param0, var0, param1, param2);
        }

        super.render(param0, param1, param2, param3);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void setChatLine(String param0) {
        this.input.setValue(param0);
    }

    @Override
    protected void updateNarrationState(NarrationElementOutput param0) {
        param0.add(NarratedElementType.TITLE, this.getTitle());
        param0.add(NarratedElementType.USAGE, USAGE_TEXT);
        String var0 = this.input.getValue();
        if (!var0.isEmpty()) {
            param0.nest().add(NarratedElementType.TITLE, (Component)Component.translatable("chat_screen.message", var0));
        }

    }

    public void renderChatPreview(PoseStack param0) {
        int var0 = (int)(255.0 * (this.minecraft.options.chatOpacity().get() * 0.9F + 0.1F));
        int var1 = (int)(255.0 * this.minecraft.options.textBackgroundOpacity().get());
        int var2 = this.chatPreviewWidth();
        List<FormattedCharSequence> var3 = this.peekChatPreview();
        int var4 = this.chatPreviewHeight(var3);
        RenderSystem.enableBlend();
        param0.pushPose();
        param0.translate((double)this.chatPreviewLeft(), (double)this.chatPreviewTop(var4), 0.0);
        fill(param0, 0, 0, var2, var4, var1 << 24);
        param0.translate(2.0, 2.0, 0.0);

        for(int var5 = 0; var5 < var3.size(); ++var5) {
            FormattedCharSequence var6 = var3.get(var5);
            this.minecraft.font.drawShadow(param0, var6, 0.0F, (float)(var5 * 9), var0 << 24 | 16777215);
        }

        param0.popPose();
        RenderSystem.disableBlend();
    }

    @Nullable
    private Style getComponentStyleAt(double param0, double param1) {
        Style var0 = this.minecraft.gui.getChat().getClickedComponentStyleAt(param0, param1);
        if (var0 == null) {
            var0 = this.getChatPreviewStyleAt(param0, param1);
        }

        return var0;
    }

    @Nullable
    private Style getChatPreviewStyleAt(double param0, double param1) {
        if (this.minecraft.options.hideGui) {
            return null;
        } else {
            List<FormattedCharSequence> var0 = this.peekChatPreview();
            int var1 = this.chatPreviewHeight(var0);
            if (!(param0 < (double)this.chatPreviewLeft())
                && !(param0 > (double)this.chatPreviewRight())
                && !(param1 < (double)this.chatPreviewTop(var1))
                && !(param1 > (double)this.chatPreviewBottom())) {
                int var2 = this.chatPreviewLeft() + 2;
                int var3 = this.chatPreviewTop(var1) + 2;
                int var4 = (Mth.floor(param1) - var3) / 9;
                if (var4 >= 0 && var4 < var0.size()) {
                    FormattedCharSequence var5 = var0.get(var4);
                    return this.minecraft.font.getSplitter().componentStyleAtWidth(var5, (int)(param0 - (double)var2));
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    private List<FormattedCharSequence> peekChatPreview() {
        Component var0 = this.chatPreview.peek();
        return var0 != null ? this.font.split(var0, this.chatPreviewWidth()) : List.of(PREVIEW_HINT.getVisualOrderText());
    }

    private int chatPreviewWidth() {
        return this.minecraft.screen.width - 4;
    }

    private int chatPreviewHeight(List<FormattedCharSequence> param0) {
        return Math.max(param0.size(), 1) * 9 + 4;
    }

    private int chatPreviewBottom() {
        return this.minecraft.screen.height - 15;
    }

    private int chatPreviewTop(int param0) {
        return this.chatPreviewBottom() - param0;
    }

    private int chatPreviewLeft() {
        return 2;
    }

    private int chatPreviewRight() {
        return this.minecraft.screen.width - 2;
    }

    public void handleChatInput(String param0, boolean param1) {
        param0 = this.normalizeChatMessage(param0);
        if (!param0.isEmpty()) {
            if (param1) {
                this.minecraft.gui.getChat().addRecentChat(param0);
            }

            Component var0 = this.chatPreview.pull(param0);
            if (param0.startsWith("/")) {
                this.minecraft.player.command(param0.substring(1), var0);
            } else {
                this.minecraft.player.chat(param0, var0);
            }

        }
    }

    public String normalizeChatMessage(String param0) {
        return StringUtils.normalizeSpace(param0.trim());
    }

    public ClientChatPreview getChatPreview() {
        return this.chatPreview;
    }
}
