package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.SuggestionContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractCommandBlockEditScreen extends Screen {
    protected EditBox commandEdit;
    protected EditBox previousEdit;
    protected Button doneButton;
    protected Button cancelButton;
    protected Button outputButton;
    protected boolean trackOutput;
    protected final List<String> commandUsage = Lists.newArrayList();
    protected int commandUsagePosition;
    protected int commandUsageWidth;
    protected ParseResults<SharedSuggestionProvider> currentParse;
    protected CompletableFuture<Suggestions> pendingSuggestions;
    protected AbstractCommandBlockEditScreen.SuggestionsList suggestions;
    private boolean keepSuggestions;

    public AbstractCommandBlockEditScreen() {
        super(NarratorChatListener.NO_TITLE);
    }

    @Override
    public void tick() {
        this.commandEdit.tick();
    }

    abstract BaseCommandBlock getCommandBlock();

    abstract int getPreviousY();

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.doneButton = this.addButton(
            new Button(this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20, I18n.get("gui.done"), param0 -> this.onDone())
        );
        this.cancelButton = this.addButton(
            new Button(this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20, I18n.get("gui.cancel"), param0 -> this.onClose())
        );
        this.outputButton = this.addButton(new Button(this.width / 2 + 150 - 20, this.getPreviousY(), 20, 20, "O", param0 -> {
            BaseCommandBlock var0 = this.getCommandBlock();
            var0.setTrackOutput(!var0.isTrackOutput());
            this.updateCommandOutput();
        }));
        this.commandEdit = new EditBox(this.font, this.width / 2 - 150, 50, 300, 20, I18n.get("advMode.command"));
        this.commandEdit.setMaxLength(32500);
        this.commandEdit.setFormatter(this::formatChat);
        this.commandEdit.setResponder(this::onEdited);
        this.children.add(this.commandEdit);
        this.previousEdit = new EditBox(this.font, this.width / 2 - 150, this.getPreviousY(), 276, 20, I18n.get("advMode.previousOutput"));
        this.previousEdit.setMaxLength(32500);
        this.previousEdit.setEditable(false);
        this.previousEdit.setValue("-");
        this.children.add(this.previousEdit);
        this.setInitialFocus(this.commandEdit);
        this.commandEdit.setFocus(true);
        this.updateCommandInfo();
    }

    @Override
    public void resize(Minecraft param0, int param1, int param2) {
        String var0 = this.commandEdit.getValue();
        this.init(param0, param1, param2);
        this.setChatLine(var0);
        this.updateCommandInfo();
    }

    protected void updateCommandOutput() {
        if (this.getCommandBlock().isTrackOutput()) {
            this.outputButton.setMessage("O");
            this.previousEdit.setValue(this.getCommandBlock().getLastOutput().getString());
        } else {
            this.outputButton.setMessage("X");
            this.previousEdit.setValue("-");
        }

    }

    protected void onDone() {
        BaseCommandBlock var0 = this.getCommandBlock();
        this.populateAndSendPacket(var0);
        if (!var0.isTrackOutput()) {
            var0.setLastOutput(null);
        }

        this.minecraft.setScreen(null);
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    protected abstract void populateAndSendPacket(BaseCommandBlock var1);

    @Override
    public void onClose() {
        this.getCommandBlock().setTrackOutput(this.trackOutput);
        this.minecraft.setScreen(null);
    }

    private void onEdited(String param0) {
        this.updateCommandInfo();
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (this.suggestions != null && this.suggestions.keyPressed(param0, param1, param2)) {
            return true;
        } else if (this.getFocused() == this.commandEdit && param0 == 258) {
            this.showSuggestions();
            return true;
        } else if (super.keyPressed(param0, param1, param2)) {
            return true;
        } else if (param0 != 257 && param0 != 335) {
            if (param0 == 258 && this.getFocused() == this.commandEdit) {
                this.showSuggestions();
            }

            return false;
        } else {
            this.onDone();
            return true;
        }
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2) {
        return this.suggestions != null && this.suggestions.mouseScrolled(Mth.clamp(param2, -1.0, 1.0)) ? true : super.mouseScrolled(param0, param1, param2);
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        return this.suggestions != null && this.suggestions.mouseClicked((int)param0, (int)param1, param2) ? true : super.mouseClicked(param0, param1, param2);
    }

    protected void updateCommandInfo() {
        String var0 = this.commandEdit.getValue();
        if (this.currentParse != null && !this.currentParse.getReader().getString().equals(var0)) {
            this.currentParse = null;
        }

        if (!this.keepSuggestions) {
            this.commandEdit.setSuggestion(null);
            this.suggestions = null;
        }

        this.commandUsage.clear();
        CommandDispatcher<SharedSuggestionProvider> var1 = this.minecraft.player.connection.getCommands();
        StringReader var2 = new StringReader(var0);
        if (var2.canRead() && var2.peek() == '/') {
            var2.skip();
        }

        int var3 = var2.getCursor();
        if (this.currentParse == null) {
            this.currentParse = var1.parse(var2, this.minecraft.player.connection.getSuggestionsProvider());
        }

        int var4 = this.commandEdit.getCursorPosition();
        if (var4 >= var3 && (this.suggestions == null || !this.keepSuggestions)) {
            this.pendingSuggestions = var1.getCompletionSuggestions(this.currentParse, var4);
            this.pendingSuggestions.thenRun(() -> {
                if (this.pendingSuggestions.isDone()) {
                    this.updateUsageInfo();
                }
            });
        }

    }

    private void updateUsageInfo() {
        if (this.pendingSuggestions.join().isEmpty()
            && !this.currentParse.getExceptions().isEmpty()
            && this.commandEdit.getCursorPosition() == this.commandEdit.getValue().length()) {
            int var0 = 0;

            for(Entry<CommandNode<SharedSuggestionProvider>, CommandSyntaxException> var1 : this.currentParse.getExceptions().entrySet()) {
                CommandSyntaxException var2 = var1.getValue();
                if (var2.getType() == CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect()) {
                    ++var0;
                } else {
                    this.commandUsage.add(var2.getMessage());
                }
            }

            if (var0 > 0) {
                this.commandUsage.add(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create().getMessage());
            }
        }

        this.commandUsagePosition = 0;
        this.commandUsageWidth = this.width;
        if (this.commandUsage.isEmpty()) {
            this.fillNodeUsage(ChatFormatting.GRAY);
        }

        this.suggestions = null;
        if (this.minecraft.options.autoSuggestions) {
            this.showSuggestions();
        }

    }

    private String formatChat(String param0, int param1) {
        return this.currentParse != null ? ChatScreen.formatText(this.currentParse, param0, param1) : param0;
    }

    private void fillNodeUsage(ChatFormatting param0) {
        CommandContextBuilder<SharedSuggestionProvider> var0 = this.currentParse.getContext();
        SuggestionContext<SharedSuggestionProvider> var1 = var0.findSuggestionContext(this.commandEdit.getCursorPosition());
        Map<CommandNode<SharedSuggestionProvider>, String> var2 = this.minecraft
            .player
            .connection
            .getCommands()
            .getSmartUsage(var1.parent, this.minecraft.player.connection.getSuggestionsProvider());
        List<String> var3 = Lists.newArrayList();
        int var4 = 0;

        for(Entry<CommandNode<SharedSuggestionProvider>, String> var5 : var2.entrySet()) {
            if (!(var5.getKey() instanceof LiteralCommandNode)) {
                var3.add(param0 + (String)var5.getValue());
                var4 = Math.max(var4, this.font.width(var5.getValue()));
            }
        }

        if (!var3.isEmpty()) {
            this.commandUsage.addAll(var3);
            this.commandUsagePosition = Mth.clamp(
                this.commandEdit.getScreenX(var1.startPos), 0, this.commandEdit.getScreenX(0) + this.commandEdit.getInnerWidth() - var4
            );
            this.commandUsageWidth = var4;
        }

    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.drawCenteredString(this.font, I18n.get("advMode.setCommand"), this.width / 2, 20, 16777215);
        this.drawString(this.font, I18n.get("advMode.command"), this.width / 2 - 150, 40, 10526880);
        this.commandEdit.render(param0, param1, param2);
        int var0 = 75;
        if (!this.previousEdit.getValue().isEmpty()) {
            var0 += 5 * 9 + 1 + this.getPreviousY() - 135;
            this.drawString(this.font, I18n.get("advMode.previousOutput"), this.width / 2 - 150, var0 + 4, 10526880);
            this.previousEdit.render(param0, param1, param2);
        }

        super.render(param0, param1, param2);
        if (this.suggestions != null) {
            this.suggestions.render(param0, param1);
        } else {
            var0 = 0;

            for(String var1 : this.commandUsage) {
                fill(this.commandUsagePosition - 1, 72 + 12 * var0, this.commandUsagePosition + this.commandUsageWidth + 1, 84 + 12 * var0, Integer.MIN_VALUE);
                this.font.drawShadow(var1, (float)this.commandUsagePosition, (float)(74 + 12 * var0), -1);
                ++var0;
            }
        }

    }

    public void showSuggestions() {
        if (this.pendingSuggestions != null && this.pendingSuggestions.isDone()) {
            Suggestions var0 = this.pendingSuggestions.join();
            if (!var0.isEmpty()) {
                int var1 = 0;

                for(Suggestion var2 : var0.getList()) {
                    var1 = Math.max(var1, this.font.width(var2.getText()));
                }

                int var3 = Mth.clamp(
                    this.commandEdit.getScreenX(var0.getRange().getStart()), 0, this.commandEdit.getScreenX(0) + this.commandEdit.getInnerWidth() - var1
                );
                this.suggestions = new AbstractCommandBlockEditScreen.SuggestionsList(var3, 72, var1, var0);
            }
        }

    }

    protected void setChatLine(String param0) {
        this.commandEdit.setValue(param0);
    }

    @Nullable
    private static String calculateSuggestionSuffix(String param0, String param1) {
        return param1.startsWith(param0) ? param1.substring(param0.length()) : null;
    }

    @OnlyIn(Dist.CLIENT)
    class SuggestionsList {
        private final Rect2i rect;
        private final Suggestions suggestions;
        private final String originalContents;
        private int offset;
        private int current;
        private Vec2 lastMouse = Vec2.ZERO;
        private boolean tabCycles;

        private SuggestionsList(int param0, int param1, int param2, Suggestions param3) {
            this.rect = new Rect2i(param0 - 1, param1, param2 + 1, Math.min(param3.getList().size(), 7) * 12);
            this.suggestions = param3;
            this.originalContents = AbstractCommandBlockEditScreen.this.commandEdit.getValue();
            this.select(0);
        }

        public void render(int param0, int param1) {
            int var0 = Math.min(this.suggestions.getList().size(), 7);
            int var1 = Integer.MIN_VALUE;
            int var2 = -5592406;
            boolean var3 = this.offset > 0;
            boolean var4 = this.suggestions.getList().size() > this.offset + var0;
            boolean var5 = var3 || var4;
            boolean var6 = this.lastMouse.x != (float)param0 || this.lastMouse.y != (float)param1;
            if (var6) {
                this.lastMouse = new Vec2((float)param0, (float)param1);
            }

            if (var5) {
                GuiComponent.fill(this.rect.getX(), this.rect.getY() - 1, this.rect.getX() + this.rect.getWidth(), this.rect.getY(), Integer.MIN_VALUE);
                GuiComponent.fill(
                    this.rect.getX(),
                    this.rect.getY() + this.rect.getHeight(),
                    this.rect.getX() + this.rect.getWidth(),
                    this.rect.getY() + this.rect.getHeight() + 1,
                    Integer.MIN_VALUE
                );
                if (var3) {
                    for(int var7 = 0; var7 < this.rect.getWidth(); ++var7) {
                        if (var7 % 2 == 0) {
                            GuiComponent.fill(this.rect.getX() + var7, this.rect.getY() - 1, this.rect.getX() + var7 + 1, this.rect.getY(), -1);
                        }
                    }
                }

                if (var4) {
                    for(int var8 = 0; var8 < this.rect.getWidth(); ++var8) {
                        if (var8 % 2 == 0) {
                            GuiComponent.fill(
                                this.rect.getX() + var8,
                                this.rect.getY() + this.rect.getHeight(),
                                this.rect.getX() + var8 + 1,
                                this.rect.getY() + this.rect.getHeight() + 1,
                                -1
                            );
                        }
                    }
                }
            }

            boolean var9 = false;

            for(int var10 = 0; var10 < var0; ++var10) {
                Suggestion var11 = this.suggestions.getList().get(var10 + this.offset);
                GuiComponent.fill(
                    this.rect.getX(),
                    this.rect.getY() + 12 * var10,
                    this.rect.getX() + this.rect.getWidth(),
                    this.rect.getY() + 12 * var10 + 12,
                    Integer.MIN_VALUE
                );
                if (param0 > this.rect.getX()
                    && param0 < this.rect.getX() + this.rect.getWidth()
                    && param1 > this.rect.getY() + 12 * var10
                    && param1 < this.rect.getY() + 12 * var10 + 12) {
                    if (var6) {
                        this.select(var10 + this.offset);
                    }

                    var9 = true;
                }

                AbstractCommandBlockEditScreen.this.font
                    .drawShadow(
                        var11.getText(),
                        (float)(this.rect.getX() + 1),
                        (float)(this.rect.getY() + 2 + 12 * var10),
                        var10 + this.offset == this.current ? -256 : -5592406
                    );
            }

            if (var9) {
                Message var12 = this.suggestions.getList().get(this.current).getTooltip();
                if (var12 != null) {
                    AbstractCommandBlockEditScreen.this.renderTooltip(ComponentUtils.fromMessage(var12).getColoredString(), param0, param1);
                }
            }

        }

        public boolean mouseClicked(int param0, int param1, int param2) {
            if (!this.rect.contains(param0, param1)) {
                return false;
            } else {
                int var0 = (param1 - this.rect.getY()) / 12 + this.offset;
                if (var0 >= 0 && var0 < this.suggestions.getList().size()) {
                    this.select(var0);
                    this.useSuggestion();
                }

                return true;
            }
        }

        public boolean mouseScrolled(double param0) {
            int var0 = (int)(
                AbstractCommandBlockEditScreen.this.minecraft.mouseHandler.xpos()
                    * (double)AbstractCommandBlockEditScreen.this.minecraft.getWindow().getGuiScaledWidth()
                    / (double)AbstractCommandBlockEditScreen.this.minecraft.getWindow().getScreenWidth()
            );
            int var1 = (int)(
                AbstractCommandBlockEditScreen.this.minecraft.mouseHandler.ypos()
                    * (double)AbstractCommandBlockEditScreen.this.minecraft.getWindow().getGuiScaledHeight()
                    / (double)AbstractCommandBlockEditScreen.this.minecraft.getWindow().getScreenHeight()
            );
            if (this.rect.contains(var0, var1)) {
                this.offset = Mth.clamp((int)((double)this.offset - param0), 0, Math.max(this.suggestions.getList().size() - 7, 0));
                return true;
            } else {
                return false;
            }
        }

        public boolean keyPressed(int param0, int param1, int param2) {
            if (param0 == 265) {
                this.cycle(-1);
                this.tabCycles = false;
                return true;
            } else if (param0 == 264) {
                this.cycle(1);
                this.tabCycles = false;
                return true;
            } else if (param0 == 258) {
                if (this.tabCycles) {
                    this.cycle(Screen.hasShiftDown() ? -1 : 1);
                }

                this.useSuggestion();
                return true;
            } else if (param0 == 256) {
                this.hide();
                return true;
            } else {
                return false;
            }
        }

        public void cycle(int param0) {
            this.select(this.current + param0);
            int var0 = this.offset;
            int var1 = this.offset + 7 - 1;
            if (this.current < var0) {
                this.offset = Mth.clamp(this.current, 0, Math.max(this.suggestions.getList().size() - 7, 0));
            } else if (this.current > var1) {
                this.offset = Mth.clamp(this.current - 7, 0, Math.max(this.suggestions.getList().size() - 7, 0));
            }

        }

        public void select(int param0) {
            this.current = param0;
            if (this.current < 0) {
                this.current += this.suggestions.getList().size();
            }

            if (this.current >= this.suggestions.getList().size()) {
                this.current -= this.suggestions.getList().size();
            }

            Suggestion var0 = this.suggestions.getList().get(this.current);
            AbstractCommandBlockEditScreen.this.commandEdit
                .setSuggestion(
                    AbstractCommandBlockEditScreen.calculateSuggestionSuffix(
                        AbstractCommandBlockEditScreen.this.commandEdit.getValue(), var0.apply(this.originalContents)
                    )
                );
        }

        public void useSuggestion() {
            Suggestion var0 = this.suggestions.getList().get(this.current);
            AbstractCommandBlockEditScreen.this.keepSuggestions = true;
            AbstractCommandBlockEditScreen.this.setChatLine(var0.apply(this.originalContents));
            int var1 = var0.getRange().getStart() + var0.getText().length();
            AbstractCommandBlockEditScreen.this.commandEdit.setCursorPosition(var1);
            AbstractCommandBlockEditScreen.this.commandEdit.setHighlightPos(var1);
            this.select(this.current);
            AbstractCommandBlockEditScreen.this.keepSuggestions = false;
            this.tabCycles = true;
        }

        public void hide() {
            AbstractCommandBlockEditScreen.this.suggestions = null;
        }
    }
}
