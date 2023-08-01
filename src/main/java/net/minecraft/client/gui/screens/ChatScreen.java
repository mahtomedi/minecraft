package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class ChatScreen extends Screen {
    public static final double MOUSE_SCROLL_SPEED = 7.0;
    private static final Component USAGE_TEXT = Component.translatable("chat_screen.usage");
    private static final int TOOLTIP_MAX_WIDTH = 210;
    private String historyBuffer = "";
    private int historyPos = -1;
    protected EditBox input;
    private String initial;
    CommandSuggestions commandSuggestions;

    public ChatScreen(String param0) {
        super(Component.translatable("chat_screen.title"));
        this.initial = param0;
    }

    @Override
    protected void init() {
        this.historyPos = this.minecraft.gui.getChat().getRecentChat().size();
        this.input = new EditBox(this.minecraft.fontFilterFishy, 4, this.height - 12, this.width - 4, 12, Component.translatable("chat.editBox")) {
            @Override
            protected MutableComponent createNarrationMessage() {
                return super.createNarrationMessage().append(ChatScreen.this.commandSuggestions.getNarrationMessage());
            }
        };
        this.input.setMaxLength(256);
        this.input.setBordered(false);
        this.input.setValue(this.initial);
        this.input.setResponder(this::onEdited);
        this.input.setCanLoseFocus(false);
        this.addWidget(this.input);
        this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.input, this.font, false, false, 1, 10, true, -805306368);
        this.commandSuggestions.updateCommandInfo();
        this.setInitialFocus(this.input);
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
        this.minecraft.gui.getChat().resetChatScroll();
    }

    private void onEdited(String param0) {
        String var0 = this.input.getValue();
        this.commandSuggestions.setAllowSuggestions(!var0.equals(this.initial));
        this.commandSuggestions.updateCommandInfo();
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
    public boolean mouseScrolled(double param0, double param1, double param2, double param3) {
        param3 = Mth.clamp(param3, -1.0, 1.0);
        if (this.commandSuggestions.mouseScrolled(param3)) {
            return true;
        } else {
            if (!hasShiftDown()) {
                param3 *= 7.0;
            }

            this.minecraft.gui.getChat().scrollChat((int)param3);
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
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        param0.fill(2, this.height - 14, this.width - 2, this.height - 2, this.minecraft.options.getBackgroundColor(Integer.MIN_VALUE));
        this.input.render(param0, param1, param2, param3);
        super.render(param0, param1, param2, param3);
        this.commandSuggestions.render(param0, param1, param2);
        GuiMessageTag var0 = this.minecraft.gui.getChat().getMessageTagAt((double)param1, (double)param2);
        if (var0 != null && var0.text() != null) {
            param0.renderTooltip(this.font, this.font.split(var0.text(), 210), param1, param2);
        } else {
            Style var1 = this.getComponentStyleAt((double)param1, (double)param2);
            if (var1 != null && var1.getHoverEvent() != null) {
                param0.renderComponentHoverEffect(this.font, var1, param1, param2);
            }
        }

    }

    @Override
    public void renderBackground(GuiGraphics param0, int param1, int param2, float param3) {
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

    @Nullable
    private Style getComponentStyleAt(double param0, double param1) {
        return this.minecraft.gui.getChat().getClickedComponentStyleAt(param0, param1);
    }

    public boolean handleChatInput(String param0, boolean param1) {
        param0 = this.normalizeChatMessage(param0);
        if (param0.isEmpty()) {
            return true;
        } else {
            if (param1) {
                this.minecraft.gui.getChat().addRecentChat(param0);
            }

            if (param0.startsWith("/")) {
                this.minecraft.player.connection.sendCommand(param0.substring(1));
            } else {
                this.minecraft.player.connection.sendChat(param0);
            }

            return true;
        }
    }

    public String normalizeChatMessage(String param0) {
        return StringUtil.trimChatMessage(StringUtils.normalizeSpace(param0.trim()));
    }
}
