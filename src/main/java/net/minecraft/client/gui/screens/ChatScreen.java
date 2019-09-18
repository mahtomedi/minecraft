package net.minecraft.client.gui.screens;

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
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatScreen extends Screen {
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");
    private String historyBuffer = "";
    private int historyPos = -1;
    protected EditBox input;
    private String initial = "";
    protected final List<String> commandUsage = Lists.newArrayList();
    protected int commandUsagePosition;
    protected int commandUsageWidth;
    private ParseResults<SharedSuggestionProvider> currentParse;
    private CompletableFuture<Suggestions> pendingSuggestions;
    private ChatScreen.SuggestionsList suggestions;
    private boolean hasEdits;
    private boolean keepSuggestions;

    public ChatScreen(String param0) {
        super(NarratorChatListener.NO_TITLE);
        this.initial = param0;
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.historyPos = this.minecraft.gui.getChat().getRecentChat().size();
        this.input = new EditBox(this.font, 4, this.height - 12, this.width - 4, 12, I18n.get("chat.editBox"));
        this.input.setMaxLength(256);
        this.input.setBordered(false);
        this.input.setValue(this.initial);
        this.input.setFormatter(this::formatChat);
        this.input.setResponder(this::onEdited);
        this.children.add(this.input);
        this.updateCommandInfo();
        this.setInitialFocus(this.input);
    }

    @Override
    public void resize(Minecraft param0, int param1, int param2) {
        String var0 = this.input.getValue();
        this.init(param0, param1, param2);
        this.setChatLine(var0);
        this.updateCommandInfo();
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
        this.hasEdits = !var0.equals(this.initial);
        this.updateCommandInfo();
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (this.suggestions != null && this.suggestions.keyPressed(param0, param1, param2)) {
            return true;
        } else {
            if (param0 == 258) {
                this.hasEdits = true;
                this.showSuggestions();
            }

            if (super.keyPressed(param0, param1, param2)) {
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
    }

    public void showSuggestions() {
        if (this.pendingSuggestions != null && this.pendingSuggestions.isDone()) {
            int var0 = 0;
            Suggestions var1 = this.pendingSuggestions.join();
            if (!var1.getList().isEmpty()) {
                for(Suggestion var2 : var1.getList()) {
                    var0 = Math.max(var0, this.font.width(var2.getText()));
                }

                int var3 = Mth.clamp(this.input.getScreenX(var1.getRange().getStart()), 0, this.width - var0);
                this.suggestions = new ChatScreen.SuggestionsList(var3, this.height - 12, var0, var1);
            }
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

    private void updateCommandInfo() {
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
            CommandDispatcher<SharedSuggestionProvider> var2 = this.minecraft.player.connection.getCommands();
            if (this.currentParse == null) {
                this.currentParse = var2.parse(var1, this.minecraft.player.connection.getSuggestionsProvider());
            }

            int var3 = this.input.getCursorPosition();
            if (var3 >= 1 && (this.suggestions == null || !this.keepSuggestions)) {
                this.pendingSuggestions = var2.getCompletionSuggestions(this.currentParse, var3);
                this.pendingSuggestions.thenRun(() -> {
                    if (this.pendingSuggestions.isDone()) {
                        this.updateUsageInfo();
                    }
                });
            }
        } else {
            int var5 = getLastWordIndex(var0);
            Collection<String> var6 = this.minecraft.player.connection.getSuggestionsProvider().getOnlinePlayerNames();
            this.pendingSuggestions = SharedSuggestionProvider.suggest(var6, new SuggestionsBuilder(var0, var5));
        }

    }

    private void updateUsageInfo() {
        if (this.pendingSuggestions.join().isEmpty()
            && !this.currentParse.getExceptions().isEmpty()
            && this.input.getCursorPosition() == this.input.getValue().length()) {
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
        if (this.hasEdits && this.minecraft.options.autoSuggestions) {
            this.showSuggestions();
        }

    }

    private String formatChat(String param0, int param1) {
        return this.currentParse != null ? formatText(this.currentParse, param0, param1) : param0;
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

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2) {
        if (param2 > 1.0) {
            param2 = 1.0;
        }

        if (param2 < -1.0) {
            param2 = -1.0;
        }

        if (this.suggestions != null && this.suggestions.mouseScrolled(param2)) {
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
        if (this.suggestions != null && this.suggestions.mouseClicked((int)param0, (int)param1, param2)) {
            return true;
        } else {
            if (param2 == 0) {
                Component var0 = this.minecraft.gui.getChat().getClickedComponentAt(param0, param1);
                if (var0 != null && this.handleComponentClicked(var0)) {
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
                this.suggestions = null;
                this.historyPos = var0;
                this.hasEdits = false;
            }
        }
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.setFocused(this.input);
        this.input.setFocus(true);
        fill(2, this.height - 14, this.width - 2, this.height - 2, this.minecraft.options.getBackgroundColor(Integer.MIN_VALUE));
        this.input.render(param0, param1, param2);
        if (this.suggestions != null) {
            this.suggestions.render(param0, param1);
        } else {
            int var0 = 0;

            for(String var1 : this.commandUsage) {
                fill(
                    this.commandUsagePosition - 1,
                    this.height - 14 - 13 - 12 * var0,
                    this.commandUsagePosition + this.commandUsageWidth + 1,
                    this.height - 2 - 13 - 12 * var0,
                    -16777216
                );
                this.font.drawShadow(var1, (float)this.commandUsagePosition, (float)(this.height - 14 - 13 + 2 - 12 * var0), -1);
                ++var0;
            }
        }

        Component var2 = this.minecraft.gui.getChat().getClickedComponentAt((double)param0, (double)param1);
        if (var2 != null && var2.getStyle().getHoverEvent() != null) {
            this.renderComponentHoverEffect(var2, param0, param1);
        }

        super.render(param0, param1, param2);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
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
            this.commandUsagePosition = Mth.clamp(this.input.getScreenX(var1.startPos), 0, this.width - var4);
            this.commandUsageWidth = var4;
        }

    }

    @Nullable
    private static String calculateSuggestionSuffix(String param0, String param1) {
        return param1.startsWith(param0) ? param1.substring(param0.length()) : null;
    }

    private void setChatLine(String param0) {
        this.input.setValue(param0);
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
            this.rect = new Rect2i(param0 - 1, param1 - 3 - Math.min(param3.getList().size(), 10) * 12, param2 + 1, Math.min(param3.getList().size(), 10) * 12);
            this.suggestions = param3;
            this.originalContents = ChatScreen.this.input.getValue();
            this.select(0);
        }

        public void render(int param0, int param1) {
            int var0 = Math.min(this.suggestions.getList().size(), 10);
            int var1 = -5592406;
            boolean var2 = this.offset > 0;
            boolean var3 = this.suggestions.getList().size() > this.offset + var0;
            boolean var4 = var2 || var3;
            boolean var5 = this.lastMouse.x != (float)param0 || this.lastMouse.y != (float)param1;
            if (var5) {
                this.lastMouse = new Vec2((float)param0, (float)param1);
            }

            if (var4) {
                GuiComponent.fill(this.rect.getX(), this.rect.getY() - 1, this.rect.getX() + this.rect.getWidth(), this.rect.getY(), -805306368);
                GuiComponent.fill(
                    this.rect.getX(),
                    this.rect.getY() + this.rect.getHeight(),
                    this.rect.getX() + this.rect.getWidth(),
                    this.rect.getY() + this.rect.getHeight() + 1,
                    -805306368
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
                    this.rect.getX(), this.rect.getY() + 12 * var9, this.rect.getX() + this.rect.getWidth(), this.rect.getY() + 12 * var9 + 12, -805306368
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

                ChatScreen.this.font
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
                    ChatScreen.this.renderTooltip(ComponentUtils.fromMessage(var11).getColoredString(), param0, param1);
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
                ChatScreen.this.minecraft.mouseHandler.xpos()
                    * (double)ChatScreen.this.minecraft.getWindow().getGuiScaledWidth()
                    / (double)ChatScreen.this.minecraft.getWindow().getScreenWidth()
            );
            int var1 = (int)(
                ChatScreen.this.minecraft.mouseHandler.ypos()
                    * (double)ChatScreen.this.minecraft.getWindow().getGuiScaledHeight()
                    / (double)ChatScreen.this.minecraft.getWindow().getScreenHeight()
            );
            if (this.rect.contains(var0, var1)) {
                this.offset = Mth.clamp((int)((double)this.offset - param0), 0, Math.max(this.suggestions.getList().size() - 10, 0));
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
            int var1 = this.offset + 10 - 1;
            if (this.current < var0) {
                this.offset = Mth.clamp(this.current, 0, Math.max(this.suggestions.getList().size() - 10, 0));
            } else if (this.current > var1) {
                this.offset = Mth.clamp(this.current + 1 - 10, 0, Math.max(this.suggestions.getList().size() - 10, 0));
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
            ChatScreen.this.input.setSuggestion(ChatScreen.calculateSuggestionSuffix(ChatScreen.this.input.getValue(), var0.apply(this.originalContents)));
        }

        public void useSuggestion() {
            Suggestion var0 = this.suggestions.getList().get(this.current);
            ChatScreen.this.keepSuggestions = true;
            ChatScreen.this.setChatLine(var0.apply(this.originalContents));
            int var1 = var0.getRange().getStart() + var0.getText().length();
            ChatScreen.this.input.setCursorPosition(var1);
            ChatScreen.this.input.setHighlightPos(var1);
            this.select(this.current);
            ChatScreen.this.keepSuggestions = false;
            this.tabCycles = true;
        }

        public void hide() {
            ChatScreen.this.suggestions = null;
        }
    }
}
