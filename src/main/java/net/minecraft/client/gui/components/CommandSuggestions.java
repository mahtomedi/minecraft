package net.minecraft.client.gui.components;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.SuggestionContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CommandSuggestions {
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");
    private static final Style UNPARSED_STYLE = Style.EMPTY.withColor(ChatFormatting.RED);
    private static final Style LITERAL_STYLE = Style.EMPTY.withColor(ChatFormatting.GRAY);
    private static final List<Style> ARGUMENT_STYLES = Stream.of(
            ChatFormatting.AQUA, ChatFormatting.YELLOW, ChatFormatting.GREEN, ChatFormatting.LIGHT_PURPLE, ChatFormatting.GOLD
        )
        .map(Style.EMPTY::withColor)
        .collect(ImmutableList.toImmutableList());
    final Minecraft minecraft;
    final Screen screen;
    final EditBox input;
    final Font font;
    private final boolean commandsOnly;
    private final boolean onlyShowIfCursorPastError;
    final int lineStartOffset;
    final int suggestionLineLimit;
    final boolean anchorToBottom;
    final int fillColor;
    private final List<FormattedCharSequence> commandUsage = Lists.newArrayList();
    private int commandUsagePosition;
    private int commandUsageWidth;
    @Nullable
    private ParseResults<SharedSuggestionProvider> currentParse;
    @Nullable
    private CompletableFuture<Suggestions> pendingSuggestions;
    @Nullable
    private CommandSuggestions.SuggestionsList suggestions;
    private boolean allowSuggestions;
    boolean keepSuggestions;

    public CommandSuggestions(
        Minecraft param0, Screen param1, EditBox param2, Font param3, boolean param4, boolean param5, int param6, int param7, boolean param8, int param9
    ) {
        this.minecraft = param0;
        this.screen = param1;
        this.input = param2;
        this.font = param3;
        this.commandsOnly = param4;
        this.onlyShowIfCursorPastError = param5;
        this.lineStartOffset = param6;
        this.suggestionLineLimit = param7;
        this.anchorToBottom = param8;
        this.fillColor = param9;
        param2.setFormatter(this::formatChat);
    }

    public void setAllowSuggestions(boolean param0) {
        this.allowSuggestions = param0;
        if (!param0) {
            this.suggestions = null;
        }

    }

    public boolean keyPressed(int param0, int param1, int param2) {
        if (this.suggestions != null && this.suggestions.keyPressed(param0, param1, param2)) {
            return true;
        } else if (this.screen.getFocused() == this.input && param0 == 258) {
            this.showSuggestions(true);
            return true;
        } else {
            return false;
        }
    }

    public boolean mouseScrolled(double param0) {
        return this.suggestions != null && this.suggestions.mouseScrolled(Mth.clamp(param0, -1.0, 1.0));
    }

    public boolean mouseClicked(double param0, double param1, int param2) {
        return this.suggestions != null && this.suggestions.mouseClicked((int)param0, (int)param1, param2);
    }

    public void showSuggestions(boolean param0) {
        if (this.pendingSuggestions != null && this.pendingSuggestions.isDone()) {
            Suggestions var0 = this.pendingSuggestions.join();
            if (!var0.isEmpty()) {
                int var1 = 0;

                for(Suggestion var2 : var0.getList()) {
                    var1 = Math.max(var1, this.font.width(var2.getText()));
                }

                int var3 = Mth.clamp(this.input.getScreenX(var0.getRange().getStart()), 0, this.input.getScreenX(0) + this.input.getInnerWidth() - var1);
                int var4 = this.anchorToBottom ? this.screen.height - 12 : 72;
                this.suggestions = new CommandSuggestions.SuggestionsList(var3, var4, var1, this.sortSuggestions(var0), param0);
            }
        }

    }

    public void hide() {
        this.suggestions = null;
    }

    private List<Suggestion> sortSuggestions(Suggestions param0) {
        String var0 = this.input.getValue().substring(0, this.input.getCursorPosition());
        int var1 = getLastWordIndex(var0);
        String var2 = var0.substring(var1).toLowerCase(Locale.ROOT);
        List<Suggestion> var3 = Lists.newArrayList();
        List<Suggestion> var4 = Lists.newArrayList();

        for(Suggestion var5 : param0.getList()) {
            if (!var5.getText().startsWith(var2) && !var5.getText().startsWith("minecraft:" + var2)) {
                var4.add(var5);
            } else {
                var3.add(var5);
            }
        }

        var3.addAll(var4);
        return var3;
    }

    public void updateCommandInfo() {
        String var0 = this.input.getValue();
        if (this.currentParse != null && !this.currentParse.getReader().getString().equals(var0)) {
            this.currentParse = null;
        }

        if (!this.keepSuggestions) {
            this.input.setSuggestion(null);
            this.suggestions = null;
        }

        this.commandUsage.clear();
        StringReader var1 = new StringReader(var0);
        boolean var2 = var1.canRead() && var1.peek() == '/';
        if (var2) {
            var1.skip();
        }

        boolean var3 = this.commandsOnly || var2;
        int var4 = this.input.getCursorPosition();
        if (var3) {
            CommandDispatcher<SharedSuggestionProvider> var5 = this.minecraft.player.connection.getCommands();
            if (this.currentParse == null) {
                this.currentParse = var5.parse(var1, this.minecraft.player.connection.getSuggestionsProvider());
            }

            int var6 = this.onlyShowIfCursorPastError ? var1.getCursor() : 1;
            if (var4 >= var6 && (this.suggestions == null || !this.keepSuggestions)) {
                this.pendingSuggestions = var5.getCompletionSuggestions(this.currentParse, var4);
                this.pendingSuggestions.thenRun(() -> {
                    if (this.pendingSuggestions.isDone()) {
                        this.updateUsageInfo();
                    }
                });
            }
        } else {
            String var7 = var0.substring(0, var4);
            int var8 = getLastWordIndex(var7);
            Collection<String> var9 = this.minecraft.player.connection.getSuggestionsProvider().getCustomTabSugggestions();
            this.pendingSuggestions = SharedSuggestionProvider.suggest(var9, new SuggestionsBuilder(var7, var8));
        }

    }

    private static int getLastWordIndex(String param0) {
        if (Strings.isNullOrEmpty(param0)) {
            return 0;
        } else {
            int var0 = 0;
            Matcher var1 = WHITESPACE_PATTERN.matcher(param0);

            while(var1.find()) {
                var0 = var1.end();
            }

            return var0;
        }
    }

    private static FormattedCharSequence getExceptionMessage(CommandSyntaxException param0) {
        Component var0 = ComponentUtils.fromMessage(param0.getRawMessage());
        String var1 = param0.getContext();
        return var1 == null
            ? var0.getVisualOrderText()
            : Component.translatable("command.context.parse_error", var0, param0.getCursor(), var1).getVisualOrderText();
    }

    private void updateUsageInfo() {
        if (this.input.getCursorPosition() == this.input.getValue().length()) {
            if (this.pendingSuggestions.join().isEmpty() && !this.currentParse.getExceptions().isEmpty()) {
                int var0 = 0;

                for(Entry<CommandNode<SharedSuggestionProvider>, CommandSyntaxException> var1 : this.currentParse.getExceptions().entrySet()) {
                    CommandSyntaxException var2 = var1.getValue();
                    if (var2.getType() == CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect()) {
                        ++var0;
                    } else {
                        this.commandUsage.add(getExceptionMessage(var2));
                    }
                }

                if (var0 > 0) {
                    this.commandUsage.add(getExceptionMessage(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create()));
                }
            } else if (this.currentParse.getReader().canRead()) {
                this.commandUsage.add(getExceptionMessage(Commands.getParseException(this.currentParse)));
            }
        }

        this.commandUsagePosition = 0;
        this.commandUsageWidth = this.screen.width;
        if (this.commandUsage.isEmpty()) {
            this.fillNodeUsage(ChatFormatting.GRAY);
        }

        this.suggestions = null;
        if (this.allowSuggestions && this.minecraft.options.autoSuggestions().get()) {
            this.showSuggestions(false);
        }

    }

    private void fillNodeUsage(ChatFormatting param0) {
        CommandContextBuilder<SharedSuggestionProvider> var0 = this.currentParse.getContext();
        SuggestionContext<SharedSuggestionProvider> var1 = var0.findSuggestionContext(this.input.getCursorPosition());
        Map<CommandNode<SharedSuggestionProvider>, String> var2 = this.minecraft
            .player
            .connection
            .getCommands()
            .getSmartUsage(var1.parent, this.minecraft.player.connection.getSuggestionsProvider());
        List<FormattedCharSequence> var3 = Lists.newArrayList();
        int var4 = 0;
        Style var5 = Style.EMPTY.withColor(param0);

        for(Entry<CommandNode<SharedSuggestionProvider>, String> var6 : var2.entrySet()) {
            if (!(var6.getKey() instanceof LiteralCommandNode)) {
                var3.add(FormattedCharSequence.forward(var6.getValue(), var5));
                var4 = Math.max(var4, this.font.width(var6.getValue()));
            }
        }

        if (!var3.isEmpty()) {
            this.commandUsage.addAll(var3);
            this.commandUsagePosition = Mth.clamp(this.input.getScreenX(var1.startPos), 0, this.input.getScreenX(0) + this.input.getInnerWidth() - var4);
            this.commandUsageWidth = var4;
        }

    }

    private FormattedCharSequence formatChat(String param0x, int param1x) {
        return this.currentParse != null ? formatText(this.currentParse, param0x, param1x) : FormattedCharSequence.forward(param0x, Style.EMPTY);
    }

    @Nullable
    static String calculateSuggestionSuffix(String param0, String param1) {
        return param1.startsWith(param0) ? param1.substring(param0.length()) : null;
    }

    private static FormattedCharSequence formatText(ParseResults<SharedSuggestionProvider> param0, String param1, int param2) {
        List<FormattedCharSequence> var0 = Lists.newArrayList();
        int var1 = 0;
        int var2 = -1;
        CommandContextBuilder<SharedSuggestionProvider> var3 = param0.getContext().getLastChild();

        for(ParsedArgument<SharedSuggestionProvider, ?> var4 : var3.getArguments().values()) {
            if (++var2 >= ARGUMENT_STYLES.size()) {
                var2 = 0;
            }

            int var5 = Math.max(var4.getRange().getStart() - param2, 0);
            if (var5 >= param1.length()) {
                break;
            }

            int var6 = Math.min(var4.getRange().getEnd() - param2, param1.length());
            if (var6 > 0) {
                var0.add(FormattedCharSequence.forward(param1.substring(var1, var5), LITERAL_STYLE));
                var0.add(FormattedCharSequence.forward(param1.substring(var5, var6), ARGUMENT_STYLES.get(var2)));
                var1 = var6;
            }
        }

        if (param0.getReader().canRead()) {
            int var7 = Math.max(param0.getReader().getCursor() - param2, 0);
            if (var7 < param1.length()) {
                int var8 = Math.min(var7 + param0.getReader().getRemainingLength(), param1.length());
                var0.add(FormattedCharSequence.forward(param1.substring(var1, var7), LITERAL_STYLE));
                var0.add(FormattedCharSequence.forward(param1.substring(var7, var8), UNPARSED_STYLE));
                var1 = var8;
            }
        }

        var0.add(FormattedCharSequence.forward(param1.substring(var1), LITERAL_STYLE));
        return FormattedCharSequence.composite(var0);
    }

    public void render(PoseStack param0, int param1, int param2) {
        if (!this.renderSuggestions(param0, param1, param2)) {
            this.renderUsage(param0);
        }

    }

    public boolean renderSuggestions(PoseStack param0, int param1, int param2) {
        if (this.suggestions != null) {
            this.suggestions.render(param0, param1, param2);
            return true;
        } else {
            return false;
        }
    }

    public void renderUsage(PoseStack param0) {
        int var0 = 0;

        for(FormattedCharSequence var1 : this.commandUsage) {
            int var2 = this.anchorToBottom ? this.screen.height - 14 - 13 - 12 * var0 : 72 + 12 * var0;
            GuiComponent.fill(param0, this.commandUsagePosition - 1, var2, this.commandUsagePosition + this.commandUsageWidth + 1, var2 + 12, this.fillColor);
            this.font.drawShadow(param0, var1, (float)this.commandUsagePosition, (float)(var2 + 2), -1);
            ++var0;
        }

    }

    public Component getNarrationMessage() {
        return (Component)(this.suggestions != null ? CommonComponents.NEW_LINE.copy().append(this.suggestions.getNarrationMessage()) : CommonComponents.EMPTY);
    }

    @OnlyIn(Dist.CLIENT)
    public class SuggestionsList {
        private final Rect2i rect;
        private final String originalContents;
        private final List<Suggestion> suggestionList;
        private int offset;
        private int current;
        private Vec2 lastMouse = Vec2.ZERO;
        private boolean tabCycles;
        private int lastNarratedEntry;

        SuggestionsList(int param1, int param2, int param3, List<Suggestion> param4, boolean param5) {
            int var0 = param1 - 1;
            int var1 = CommandSuggestions.this.anchorToBottom ? param2 - 3 - Math.min(param4.size(), CommandSuggestions.this.suggestionLineLimit) * 12 : param2;
            this.rect = new Rect2i(var0, var1, param3 + 1, Math.min(param4.size(), CommandSuggestions.this.suggestionLineLimit) * 12);
            this.originalContents = CommandSuggestions.this.input.getValue();
            this.lastNarratedEntry = param5 ? -1 : 0;
            this.suggestionList = param4;
            this.select(0);
        }

        public void render(PoseStack param0, int param1, int param2) {
            int var0 = Math.min(this.suggestionList.size(), CommandSuggestions.this.suggestionLineLimit);
            int var1 = -5592406;
            boolean var2 = this.offset > 0;
            boolean var3 = this.suggestionList.size() > this.offset + var0;
            boolean var4 = var2 || var3;
            boolean var5 = this.lastMouse.x != (float)param1 || this.lastMouse.y != (float)param2;
            if (var5) {
                this.lastMouse = new Vec2((float)param1, (float)param2);
            }

            if (var4) {
                GuiComponent.fill(
                    param0,
                    this.rect.getX(),
                    this.rect.getY() - 1,
                    this.rect.getX() + this.rect.getWidth(),
                    this.rect.getY(),
                    CommandSuggestions.this.fillColor
                );
                GuiComponent.fill(
                    param0,
                    this.rect.getX(),
                    this.rect.getY() + this.rect.getHeight(),
                    this.rect.getX() + this.rect.getWidth(),
                    this.rect.getY() + this.rect.getHeight() + 1,
                    CommandSuggestions.this.fillColor
                );
                if (var2) {
                    for(int var6 = 0; var6 < this.rect.getWidth(); ++var6) {
                        if (var6 % 2 == 0) {
                            GuiComponent.fill(param0, this.rect.getX() + var6, this.rect.getY() - 1, this.rect.getX() + var6 + 1, this.rect.getY(), -1);
                        }
                    }
                }

                if (var3) {
                    for(int var7 = 0; var7 < this.rect.getWidth(); ++var7) {
                        if (var7 % 2 == 0) {
                            GuiComponent.fill(
                                param0,
                                this.rect.getX() + var7,
                                this.rect.getY() + this.rect.getHeight(),
                                this.rect.getX() + var7 + 1,
                                this.rect.getY() + this.rect.getHeight() + 1,
                                -1
                            );
                        }
                    }
                }
            }

            boolean var8 = false;

            for(int var9 = 0; var9 < var0; ++var9) {
                Suggestion var10 = this.suggestionList.get(var9 + this.offset);
                GuiComponent.fill(
                    param0,
                    this.rect.getX(),
                    this.rect.getY() + 12 * var9,
                    this.rect.getX() + this.rect.getWidth(),
                    this.rect.getY() + 12 * var9 + 12,
                    CommandSuggestions.this.fillColor
                );
                if (param1 > this.rect.getX()
                    && param1 < this.rect.getX() + this.rect.getWidth()
                    && param2 > this.rect.getY() + 12 * var9
                    && param2 < this.rect.getY() + 12 * var9 + 12) {
                    if (var5) {
                        this.select(var9 + this.offset);
                    }

                    var8 = true;
                }

                CommandSuggestions.this.font
                    .drawShadow(
                        param0,
                        var10.getText(),
                        (float)(this.rect.getX() + 1),
                        (float)(this.rect.getY() + 2 + 12 * var9),
                        var9 + this.offset == this.current ? -256 : -5592406
                    );
            }

            if (var8) {
                Message var11 = this.suggestionList.get(this.current).getTooltip();
                if (var11 != null) {
                    CommandSuggestions.this.screen.renderTooltip(param0, ComponentUtils.fromMessage(var11), param1, param2);
                }
            }

        }

        public boolean mouseClicked(int param0, int param1, int param2) {
            if (!this.rect.contains(param0, param1)) {
                return false;
            } else {
                int var0 = (param1 - this.rect.getY()) / 12 + this.offset;
                if (var0 >= 0 && var0 < this.suggestionList.size()) {
                    this.select(var0);
                    this.useSuggestion();
                }

                return true;
            }
        }

        public boolean mouseScrolled(double param0) {
            int var0 = (int)(
                CommandSuggestions.this.minecraft.mouseHandler.xpos()
                    * (double)CommandSuggestions.this.minecraft.getWindow().getGuiScaledWidth()
                    / (double)CommandSuggestions.this.minecraft.getWindow().getScreenWidth()
            );
            int var1 = (int)(
                CommandSuggestions.this.minecraft.mouseHandler.ypos()
                    * (double)CommandSuggestions.this.minecraft.getWindow().getGuiScaledHeight()
                    / (double)CommandSuggestions.this.minecraft.getWindow().getScreenHeight()
            );
            if (this.rect.contains(var0, var1)) {
                this.offset = Mth.clamp(
                    (int)((double)this.offset - param0), 0, Math.max(this.suggestionList.size() - CommandSuggestions.this.suggestionLineLimit, 0)
                );
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
                CommandSuggestions.this.hide();
                return true;
            } else {
                return false;
            }
        }

        public void cycle(int param0) {
            this.select(this.current + param0);
            int var0 = this.offset;
            int var1 = this.offset + CommandSuggestions.this.suggestionLineLimit - 1;
            if (this.current < var0) {
                this.offset = Mth.clamp(this.current, 0, Math.max(this.suggestionList.size() - CommandSuggestions.this.suggestionLineLimit, 0));
            } else if (this.current > var1) {
                this.offset = Mth.clamp(
                    this.current + CommandSuggestions.this.lineStartOffset - CommandSuggestions.this.suggestionLineLimit,
                    0,
                    Math.max(this.suggestionList.size() - CommandSuggestions.this.suggestionLineLimit, 0)
                );
            }

        }

        public void select(int param0) {
            this.current = param0;
            if (this.current < 0) {
                this.current += this.suggestionList.size();
            }

            if (this.current >= this.suggestionList.size()) {
                this.current -= this.suggestionList.size();
            }

            Suggestion var0 = this.suggestionList.get(this.current);
            CommandSuggestions.this.input
                .setSuggestion(CommandSuggestions.calculateSuggestionSuffix(CommandSuggestions.this.input.getValue(), var0.apply(this.originalContents)));
            if (this.lastNarratedEntry != this.current) {
                CommandSuggestions.this.minecraft.getNarrator().sayNow(this.getNarrationMessage());
            }

        }

        public void useSuggestion() {
            Suggestion var0 = this.suggestionList.get(this.current);
            CommandSuggestions.this.keepSuggestions = true;
            CommandSuggestions.this.input.setValue(var0.apply(this.originalContents));
            int var1 = var0.getRange().getStart() + var0.getText().length();
            CommandSuggestions.this.input.setCursorPosition(var1);
            CommandSuggestions.this.input.setHighlightPos(var1);
            this.select(this.current);
            CommandSuggestions.this.keepSuggestions = false;
            this.tabCycles = true;
        }

        Component getNarrationMessage() {
            this.lastNarratedEntry = this.current;
            Suggestion var0 = this.suggestionList.get(this.current);
            Message var1 = var0.getTooltip();
            return var1 != null
                ? Component.translatable("narration.suggestion.tooltip", this.current + 1, this.suggestionList.size(), var0.getText(), var1)
                : Component.translatable("narration.suggestion", this.current + 1, this.suggestionList.size(), var0.getText());
        }
    }
}
