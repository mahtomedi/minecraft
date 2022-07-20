package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.tree.CommandNode;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.chat.ChatPreviewAnimator;
import net.minecraft.client.gui.chat.ClientChatPreview;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.chat.ChatPreviewStatus;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PreviewableCommand;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class ChatScreen extends Screen {
    private static final int CHAT_SIGNING_PENDING_INDICATOR_COLOR = 15118153;
    private static final int CHAT_SIGNING_READY_INDICATOR_COLOR = 7844841;
    private static final int PREVIEW_HIGHLIGHT_COLOR = 10533887;
    public static final double MOUSE_SCROLL_SPEED = 7.0;
    private static final Component USAGE_TEXT = Component.translatable("chat_screen.usage");
    private static final int PREVIEW_MARGIN_SIDES = 2;
    private static final int PREVIEW_PADDING = 2;
    private static final int PREVIEW_MARGIN_BOTTOM = 15;
    private static final Component PREVIEW_WARNING_TITLE = Component.translatable("chatPreview.warning.toast.title");
    private static final Component PREVIEW_WARNING_TOAST = Component.translatable("chatPreview.warning.toast");
    private static final Component PREVIEW_INPUT_HINT = Component.translatable("chat.previewInput", Component.translatable("key.keyboard.enter"))
        .withStyle(ChatFormatting.DARK_GRAY);
    private static final int TOOLTIP_MAX_WIDTH = 260;
    private String historyBuffer = "";
    private int historyPos = -1;
    protected EditBox input;
    private String initial;
    CommandSuggestions commandSuggestions;
    private ClientChatPreview chatPreview;
    private ChatPreviewStatus chatPreviewStatus;
    private boolean previewNotRequired;
    private final ChatPreviewAnimator chatPreviewAnimator = new ChatPreviewAnimator();

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
        this.chatPreviewAnimator.reset(Util.getMillis());
        this.chatPreview = new ClientChatPreview(this.minecraft);
        this.updateChatPreview(this.input.getValue());
        ServerData var0 = this.minecraft.getCurrentServer();
        this.chatPreviewStatus = var0 != null && !var0.previewsChat() ? ChatPreviewStatus.OFF : this.minecraft.options.chatPreview().get();
        if (var0 != null && this.chatPreviewStatus != ChatPreviewStatus.OFF) {
            ServerData.ChatPreview var1 = var0.getChatPreview();
            if (var1 != null && var0.previewsChat() && var1.showToast()) {
                ServerList.saveSingleServer(var0);
                SystemToast var2 = SystemToast.multiline(
                    this.minecraft, SystemToast.SystemToastIds.CHAT_PREVIEW_WARNING, PREVIEW_WARNING_TITLE, PREVIEW_WARNING_TOAST
                );
                this.minecraft.getToasts().addToast(var2);
            }
        }

        if (this.chatPreviewStatus == ChatPreviewStatus.CONFIRM) {
            this.previewNotRequired = this.initial.startsWith("/") && !this.minecraft.player.commandHasSignableArguments(this.initial.substring(1));
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
        if (this.chatPreviewStatus == ChatPreviewStatus.LIVE) {
            this.updateChatPreview(var0x);
        } else if (this.chatPreviewStatus == ChatPreviewStatus.CONFIRM && !this.chatPreview.queryEquals(var0x)) {
            this.previewNotRequired = var0x.startsWith("/") && !this.minecraft.player.commandHasSignableArguments(var0x.substring(1));
            this.chatPreview.update("");
        }

    }

    private void updateChatPreview(String param0) {
        String var0 = this.normalizeChatMessage(param0);
        if (this.sendsChatPreviewRequests()) {
            this.requestPreview(var0);
        } else {
            this.chatPreview.disable();
        }

    }

    private void requestPreview(String param0) {
        if (param0.startsWith("/")) {
            this.requestCommandArgumentPreview(param0);
        } else {
            this.requestChatMessagePreview(param0);
        }

    }

    private void requestChatMessagePreview(String param0) {
        this.chatPreview.update(param0);
    }

    private void requestCommandArgumentPreview(String param0) {
        ParseResults<SharedSuggestionProvider> var0 = this.commandSuggestions.getCurrentContext();
        CommandNode<SharedSuggestionProvider> var1 = this.commandSuggestions.getNodeAt(this.input.getCursorPosition());
        if (var0 != null && var1 != null && PreviewableCommand.of(var0).isPreviewed(var1)) {
            this.chatPreview.update(param0);
        } else {
            this.chatPreview.disable();
        }

    }

    private boolean sendsChatPreviewRequests() {
        if (this.minecraft.player == null) {
            return false;
        } else if (this.minecraft.isLocalServer()) {
            return true;
        } else if (this.chatPreviewStatus == ChatPreviewStatus.OFF) {
            return false;
        } else {
            ServerData var0 = this.minecraft.getCurrentServer();
            return var0 != null && var0.previewsChat();
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
        } else if (param0 == 257 || param0 == 335) {
            if (this.handleChatInput(this.input.getValue(), true)) {
                this.minecraft.setScreen(null);
            }

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
                    this.initial = this.input.getValue();
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
        super.render(param0, param1, param2, param3);
        boolean var0 = this.minecraft.getProfileKeyPairManager().signer() != null;
        ChatPreviewAnimator.State var1 = this.chatPreviewAnimator.get(Util.getMillis(), this.getDisplayedPreviewText());
        if (var1.preview() != null) {
            this.renderChatPreview(param0, var1.preview(), var1.alpha(), var0);
            this.commandSuggestions.renderSuggestions(param0, param1, param2);
        } else {
            this.commandSuggestions.render(param0, param1, param2);
            if (var0) {
                param0.pushPose();
                fill(param0, 0, this.height - 14, 2, this.height - 2, -8932375);
                param0.popPose();
            }
        }

        Style var2 = this.getComponentStyleAt((double)param1, (double)param2);
        if (var2 != null && var2.getHoverEvent() != null) {
            this.renderComponentHoverEffect(param0, var2, param1, param2);
        } else {
            GuiMessageTag var3 = this.minecraft.gui.getChat().getMessageTagAt((double)param1, (double)param2);
            if (var3 != null && var3.text() != null) {
                this.renderTooltip(param0, this.font.split(var3.text(), 260), param1, param2);
            }
        }

    }

    @Nullable
    protected Component getDisplayedPreviewText() {
        String var0 = this.input.getValue();
        if (var0.isBlank()) {
            return null;
        } else {
            Component var1 = this.peekPreview();
            return this.chatPreviewStatus == ChatPreviewStatus.CONFIRM && !this.previewNotRequired
                ? Objects.requireNonNullElse(
                    var1, (Component)(this.chatPreview.queryEquals(var0) && !var0.startsWith("/") ? Component.literal(var0) : PREVIEW_INPUT_HINT)
                )
                : var1;
        }
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

    public void renderChatPreview(PoseStack param0, Component param1, float param2, boolean param3) {
        int var0 = (int)(255.0 * (this.minecraft.options.chatOpacity().get() * 0.9F + 0.1F) * (double)param2);
        int var1 = (int)((double)(this.chatPreview.hasScheduledRequest() ? 127 : 255) * this.minecraft.options.textBackgroundOpacity().get() * (double)param2);
        int var2 = this.chatPreviewWidth();
        List<FormattedCharSequence> var3 = this.splitChatPreview(param1);
        int var4 = this.chatPreviewHeight(var3);
        int var5 = this.chatPreviewTop(var4);
        RenderSystem.enableBlend();
        param0.pushPose();
        param0.translate((double)this.chatPreviewLeft(), (double)var5, 0.0);
        fill(param0, 0, 0, var2, var4, var1 << 24);
        if (var0 > 0) {
            param0.translate(2.0, 2.0, 0.0);

            for(int var6 = 0; var6 < var3.size(); ++var6) {
                FormattedCharSequence var7 = var3.get(var6);
                int var8 = var6 * 9;
                this.renderChatPreviewHighlights(param0, var7, var8, var0);
                this.font.drawShadow(param0, var7, 0.0F, (float)var8, var0 << 24 | 16777215);
            }
        }

        param0.popPose();
        RenderSystem.disableBlend();
        if (param3 && this.chatPreview.peek() != null) {
            int var9 = this.chatPreview.hasScheduledRequest() ? 15118153 : 7844841;
            int var10 = (int)(255.0F * param2);
            param0.pushPose();
            fill(param0, 0, var5, 2, this.chatPreviewBottom(), var10 << 24 | var9);
            param0.popPose();
        }

    }

    private void renderChatPreviewHighlights(PoseStack param0, FormattedCharSequence param1, int param2, int param3) {
        int var0 = param2 + 9;
        int var1 = param3 << 24 | 10533887;
        Predicate<Style> var2 = param0x -> param0x.getHoverEvent() != null || param0x.getClickEvent() != null;

        for(StringSplitter.Span var3 : this.font.getSplitter().findSpans(param1, var2)) {
            int var4 = Mth.floor(var3.left());
            int var5 = Mth.ceil(var3.right());
            fill(param0, var4, param2, var5, var0, var1);
        }

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
            Component var0 = this.peekPreview();
            if (var0 == null) {
                return null;
            } else {
                List<FormattedCharSequence> var1 = this.splitChatPreview(var0);
                int var2 = this.chatPreviewHeight(var1);
                if (!(param0 < (double)this.chatPreviewLeft())
                    && !(param0 > (double)this.chatPreviewRight())
                    && !(param1 < (double)this.chatPreviewTop(var2))
                    && !(param1 > (double)this.chatPreviewBottom())) {
                    int var3 = this.chatPreviewLeft() + 2;
                    int var4 = this.chatPreviewTop(var2) + 2;
                    int var5 = (Mth.floor(param1) - var4) / 9;
                    if (var5 >= 0 && var5 < var1.size()) {
                        FormattedCharSequence var6 = var1.get(var5);
                        return this.minecraft.font.getSplitter().componentStyleAtWidth(var6, (int)(param0 - (double)var3));
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }
    }

    @Nullable
    private Component peekPreview() {
        return Util.mapNullable(this.chatPreview.peek(), ClientChatPreview.Preview::response);
    }

    private List<FormattedCharSequence> splitChatPreview(Component param0) {
        return this.font.split(param0, this.chatPreviewWidth());
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

    public boolean handleChatInput(String param0, boolean param1) {
        param0 = this.normalizeChatMessage(param0);
        if (param0.isEmpty()) {
            return true;
        } else {
            if (this.chatPreviewStatus == ChatPreviewStatus.CONFIRM && !this.previewNotRequired) {
                this.commandSuggestions.hide();
                if (!this.chatPreview.queryEquals(param0)) {
                    this.updateChatPreview(param0);
                    return false;
                }
            }

            if (param1) {
                this.minecraft.gui.getChat().addRecentChat(param0);
            }

            Component var0 = Util.mapNullable(this.chatPreview.pull(param0), ClientChatPreview.Preview::response);
            if (param0.startsWith("/")) {
                this.minecraft.player.commandSigned(param0.substring(1), var0);
            } else {
                this.minecraft.player.chatSigned(param0, var0);
            }

            return true;
        }
    }

    public String normalizeChatMessage(String param0) {
        return StringUtils.normalizeSpace(param0.trim());
    }

    public ClientChatPreview getChatPreview() {
        return this.chatPreview;
    }
}
