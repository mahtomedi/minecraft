package net.minecraft.client.gui.components;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CommandSuggestions {
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");
    private final Minecraft minecraft;
    private final Screen screen;
    private final EditBox input;
    private final Font font;
    private final boolean requireSlash;
    private final boolean onlyShowIfCursorPastError;
    private final int lineStartOffset;
    private final int suggestionLineLimit;
    private final boolean anchorToBottom;
    private final int fillColor;
    private final List<String> commandUsage = Lists.newArrayList();
    private int commandUsagePosition;
    private int commandUsageWidth;
    private ParseResults<SharedSuggestionProvider> currentParse;
    private CompletableFuture<Suggestions> pendingSuggestions;
    private CommandSuggestions.SuggestionsList suggestions;
    private boolean allowSuggestions;
    private boolean keepSuggestions;

    public CommandSuggestions(
        Minecraft param0, Screen param1, EditBox param2, Font param3, boolean param4, boolean param5, int param6, int param7, boolean param8, int param9
    ) {
        this.minecraft = param0;
        this.screen = param1;
        this.input = param2;
        this.font = param3;
        this.requireSlash = param4;
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
            this.showSuggestions();
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

    public void showSuggestions() {
        if (this.pendingSuggestions != null && this.pendingSuggestions.isDone()) {
            Suggestions var0 = this.pendingSuggestions.join();
            if (!var0.isEmpty()) {
                int var1 = 0;

                for(Suggestion var2 : var0.getList()) {
                    var1 = Math.max(var1, this.font.width(var2.getText()));
                }

                int var3 = Mth.clamp(this.input.getScreenX(var0.getRange().getStart()), 0, this.input.getScreenX(0) + this.input.getInnerWidth() - var1);
                int var4 = this.anchorToBottom ? this.screen.height - 12 : 72;
                this.suggestions = new CommandSuggestions.SuggestionsList(var3, var4, var1, var0);
            }
        }

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
        if (var1.canRead() && var1.peek() == '/') {
            var1.skip();
        } else if (this.requireSlash) {
            return;
        }

        CommandDispatcher<SharedSuggestionProvider> var2 = this.minecraft.player.connection.getCommands();
        if (this.currentParse == null) {
            this.currentParse = var2.parse(var1, this.minecraft.player.connection.getSuggestionsProvider());
        }

        int var3 = this.onlyShowIfCursorPastError ? var1.getCursor() : 1;
        int var4 = this.input.getCursorPosition();
        if (var4 < var3 || this.suggestions != null && this.keepSuggestions) {
            int var6 = getLastWordIndex(var0);
            Collection<String> var7 = this.minecraft.player.connection.getSuggestionsProvider().getOnlinePlayerNames();
            this.pendingSuggestions = SharedSuggestionProvider.suggest(var7, new SuggestionsBuilder(var0, var6));
        } else {
            this.pendingSuggestions = var2.getCompletionSuggestions(this.currentParse, var4);
            this.pendingSuggestions.thenRun(() -> {
                if (this.pendingSuggestions.isDone()) {
                    this.updateUsageInfo();
                }
            });
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

    public void updateUsageInfo() {
        if (this.input.getCursorPosition() == this.input.getValue().length()) {
            if (this.pendingSuggestions.join().isEmpty() && !this.currentParse.getExceptions().isEmpty()) {
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
            } else if (this.currentParse.getReader().canRead()) {
                this.commandUsage.add(Commands.getParseException(this.currentParse).getMessage());
            }
        }

        this.commandUsagePosition = 0;
        this.commandUsageWidth = this.screen.width;
        if (this.commandUsage.isEmpty()) {
            this.fillNodeUsage(ChatFormatting.GRAY);
        }

        this.suggestions = null;
        if (this.allowSuggestions && this.minecraft.options.autoSuggestions) {
            this.showSuggestions();
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
            this.commandUsagePosition = Mth.clamp(this.input.getScreenX(var1.startPos), 0, this.input.getScreenX(0) + this.input.getInnerWidth() - var4);
            this.commandUsageWidth = var4;
        }

    }

    private String formatChat(String param0x, int param1x) {
        return this.currentParse != null ? formatText(this.currentParse, param0x, param1x) : param0x;
    }

    @Nullable
    private static String calculateSuggestionSuffix(String param0, String param1) {
        return param1.startsWith(param0) ? param1.substring(param0.length()) : null;
    }

    public static String formatText(ParseResults<SharedSuggestionProvider> param0, String param1, int param2) {
        ChatFormatting[] var0 = new ChatFormatting[]{
            ChatFormatting.AQUA, ChatFormatting.YELLOW, ChatFormatting.GREEN, ChatFormatting.LIGHT_PURPLE, ChatFormatting.GOLD
        };
        String var1 = ChatFormatting.GRAY.toString();
        StringBuilder var2 = new StringBuilder(var1);
        int var3 = 0;
        int var4 = -1;
        CommandContextBuilder<SharedSuggestionProvider> var5 = param0.getContext().getLastChild();

        for(ParsedArgument<SharedSuggestionProvider, ?> var6 : var5.getArguments().values()) {
            if (++var4 >= var0.length) {
                var4 = 0;
            }

            int var7 = Math.max(var6.getRange().getStart() - param2, 0);
            if (var7 >= param1.length()) {
                break;
            }

            int var8 = Math.min(var6.getRange().getEnd() - param2, param1.length());
            if (var8 > 0) {
                var2.append((CharSequence)param1, var3, var7);
                var2.append(var0[var4]);
                var2.append((CharSequence)param1, var7, var8);
                var2.append(var1);
                var3 = var8;
            }
        }

        if (param0.getReader().canRead()) {
            int var9 = Math.max(param0.getReader().getCursor() - param2, 0);
            if (var9 < param1.length()) {
                int var10 = Math.min(var9 + param0.getReader().getRemainingLength(), param1.length());
                var2.append((CharSequence)param1, var3, var9);
                var2.append(ChatFormatting.RED);
                var2.append((CharSequence)param1, var9, var10);
                var3 = var10;
            }
        }

        var2.append((CharSequence)param1, var3, param1.length());
        return var2.toString();
    }

    public void render(int param0, int param1) {
        if (this.suggestions != null) {
            this.suggestions.render(param0, param1);
        } else {
            int var0 = 0;

            for(String var1 : this.commandUsage) {
                int var2 = this.anchorToBottom ? this.screen.height - 14 - 13 - 12 * var0 : 72 + 12 * var0;
                GuiComponent.fill(this.commandUsagePosition - 1, var2, this.commandUsagePosition + this.commandUsageWidth + 1, var2 + 12, this.fillColor);
                this.font.drawShadow(var1, (float)this.commandUsagePosition, (float)(var2 + 2), -1);
                ++var0;
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    public class SuggestionsList {
        private final Rect2i rect;
        private final Suggestions suggestions;
        private final String originalContents;
        private int offset;
        private int current;
        private Vec2 lastMouse = Vec2.ZERO;
        private boolean tabCycles;

        private SuggestionsList(int param1, int param2, int param3, Suggestions param4) {
            int var0 = param1 - 1;
            int var1 = CommandSuggestions.this.anchorToBottom
                ? param2 - 3 - Math.min(param4.getList().size(), CommandSuggestions.this.suggestionLineLimit) * 12
                : param2;
            this.rect = new Rect2i(var0, var1, param3 + 1, Math.min(param4.getList().size(), CommandSuggestions.this.suggestionLineLimit) * 12);
            this.suggestions = param4;
            this.originalContents = CommandSuggestions.this.input.getValue();
            this.select(0);
        }

        public void render(int param0, int param1) {
            int var0 = Math.min(this.suggestions.getList().size(), CommandSuggestions.this.suggestionLineLimit);
            int var1 = -5592406;
            boolean var2 = this.offset > 0;
            boolean var3 = this.suggestions.getList().size() > this.offset + var0;
            boolean var4 = var2 || var3;
            boolean var5 = this.lastMouse.x != (float)param0 || this.lastMouse.y != (float)param1;
            if (var5) {
                this.lastMouse = new Vec2((float)param0, (float)param1);
            }

            if (var4) {
                GuiComponent.fill(
                    this.rect.getX(), this.rect.getY() - 1, this.rect.getX() + this.rect.getWidth(), this.rect.getY(), CommandSuggestions.this.fillColor
                );
                GuiComponent.fill(
                    this.rect.getX(),
                    this.rect.getY() + this.rect.getHeight(),
                    this.rect.getX() + this.rect.getWidth(),
                    this.rect.getY() + this.rect.getHeight() + 1,
                    CommandSuggestions.this.fillColor
                );
                if (var2) {
                    for(int var6 = 0; var6 < this.rect.getWidth(); ++var6) {
                        if (var6 % 2 == 0) {
                            GuiComponent.fill(this.rect.getX() + var6, this.rect.getY() - 1, this.rect.getX() + var6 + 1, this.rect.getY(), -1);
                        }
                    }
                }

                if (var3) {
                    for(int var7 = 0; var7 < this.rect.getWidth(); ++var7) {
                        if (var7 % 2 == 0) {
                            GuiComponent.fill(
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
                Suggestion var10 = this.suggestions.getList().get(var9 + this.offset);
                GuiComponent.fill(
                    this.rect.getX(),
                    this.rect.getY() + 12 * var9,
                    this.rect.getX() + this.rect.getWidth(),
                    this.rect.getY() + 12 * var9 + 12,
                    CommandSuggestions.this.fillColor
                );
                if (param0 > this.rect.getX()
                    && param0 < this.rect.getX() + this.rect.getWidth()
                    && param1 > this.rect.getY() + 12 * var9
                    && param1 < this.rect.getY() + 12 * var9 + 12) {
                    if (var5) {
                        this.select(var9 + this.offset);
                    }

                    var8 = true;
                }

                CommandSuggestions.this.font
                    .drawShadow(
                        var10.getText(),
                        (float)(this.rect.getX() + 1),
                        (float)(this.rect.getY() + 2 + 12 * var9),
                        var9 + this.offset == this.current ? -256 : -5592406
                    );
            }

            if (var8) {
                Message var11 = this.suggestions.getList().get(this.current).getTooltip();
                if (var11 != null) {
                    CommandSuggestions.this.screen.renderTooltip(ComponentUtils.fromMessage(var11).getColoredString(), param0, param1);
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
                    (int)((double)this.offset - param0), 0, Math.max(this.suggestions.getList().size() - CommandSuggestions.this.suggestionLineLimit, 0)
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
                this.hide();
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
                this.offset = Mth.clamp(this.current, 0, Math.max(this.suggestions.getList().size() - CommandSuggestions.this.suggestionLineLimit, 0));
            } else if (this.current > var1) {
                this.offset = Mth.clamp(
                    this.current + CommandSuggestions.this.lineStartOffset - CommandSuggestions.this.suggestionLineLimit,
                    0,
                    Math.max(this.suggestions.getList().size() - CommandSuggestions.this.suggestionLineLimit, 0)
                );
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
            CommandSuggestions.this.input
                .setSuggestion(CommandSuggestions.calculateSuggestionSuffix(CommandSuggestions.this.input.getValue(), var0.apply(this.originalContents)));
        }

        public void useSuggestion() {
            Suggestion var0 = this.suggestions.getList().get(this.current);
            CommandSuggestions.this.keepSuggestions = true;
            CommandSuggestions.this.input.setValue(var0.apply(this.originalContents));
            int var1 = var0.getRange().getStart() + var0.getText().length();
            CommandSuggestions.this.input.setCursorPosition(var1);
            CommandSuggestions.this.input.setHighlightPos(var1);
            this.select(this.current);
            CommandSuggestions.this.keepSuggestions = false;
            this.tabCycles = true;
        }

        public void hide() {
            CommandSuggestions.this.suggestions = null;
        }
    }
}
