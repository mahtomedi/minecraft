package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatScreen extends Screen {
    public static final int MOUSE_SCROLL_SPEED = 7;
    private String historyBuffer = "";
    private int historyPos = -1;
    protected EditBox input;
    private String initial = "";
    CommandSuggestions commandSuggestions;

    public ChatScreen(String param0) {
        super(NarratorChatListener.NO_TITLE);
        this.initial = param0;
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.historyPos = this.minecraft.gui.getChat().getRecentChat().size();
        this.input = new EditBox(this.font, 4, this.height - 12, this.width - 4, 12, new TranslatableComponent("chat.editBox")) {
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
            String var0 = this.input.getValue().trim();
            if (!var0.isEmpty()) {
                this.sendMessage(var0);
            }

            this.minecraft.setScreen(null);
            return true;
        } else if (param0 == 265) {
            this.moveInHistory(-1);
            return true;
        } else if (param0 == 264) {
            this.moveInHistory(1);
            return true;
        } else if (param0 == 266) {
            this.minecraft.gui.getChat().scrollChat((double)(this.minecraft.gui.getChat().getLinesPerPage() - 1));
            return true;
        } else if (param0 == 267) {
            this.minecraft.gui.getChat().scrollChat((double)(-this.minecraft.gui.getChat().getLinesPerPage() + 1));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2) {
        if (param2 > 1.0) {
            param2 = 1.0;
        }

        if (param2 < -1.0) {
            param2 = -1.0;
        }

        if (this.commandSuggestions.mouseScrolled(param2)) {
            return true;
        } else {
            if (!hasShiftDown()) {
                param2 *= 7.0;
            }

            this.minecraft.gui.getChat().scrollChat(param2);
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

                Style var1 = var0.getClickedComponentStyleAt(param0, param1);
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
        this.commandSuggestions.render(param0, param1, param2);
        Style var0 = this.minecraft.gui.getChat().getClickedComponentStyleAt((double)param1, (double)param2);
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
}
