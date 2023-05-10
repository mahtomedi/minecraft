package net.minecraft.client.gui.components;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EditBox extends AbstractWidget implements Renderable {
    public static final int BACKWARDS = -1;
    public static final int FORWARDS = 1;
    private static final int CURSOR_INSERT_WIDTH = 1;
    private static final int CURSOR_INSERT_COLOR = -3092272;
    private static final String CURSOR_APPEND_CHARACTER = "_";
    public static final int DEFAULT_TEXT_COLOR = 14737632;
    private static final int BORDER_COLOR_FOCUSED = -1;
    private static final int BORDER_COLOR = -6250336;
    private static final int BACKGROUND_COLOR = -16777216;
    private final Font font;
    private String value = "";
    private int maxLength = 32;
    private int frame;
    private boolean bordered = true;
    private boolean canLoseFocus = true;
    private boolean isEditable = true;
    private boolean shiftPressed;
    private int displayPos;
    private int cursorPos;
    private int highlightPos;
    private int textColor = 14737632;
    private int textColorUneditable = 7368816;
    @Nullable
    private String suggestion;
    @Nullable
    private Consumer<String> responder;
    private Predicate<String> filter = Objects::nonNull;
    private BiFunction<String, Integer, FormattedCharSequence> formatter = (param0x, param1x) -> FormattedCharSequence.forward(param0x, Style.EMPTY);
    @Nullable
    private Component hint;

    public EditBox(Font param0, int param1, int param2, int param3, int param4, Component param5) {
        this(param0, param1, param2, param3, param4, null, param5);
    }

    public EditBox(Font param0, int param1, int param2, int param3, int param4, @Nullable EditBox param5, Component param6) {
        super(param1, param2, param3, param4, param6);
        this.font = param0;
        if (param5 != null) {
            this.setValue(param5.getValue());
        }

    }

    public void setResponder(Consumer<String> param0) {
        this.responder = param0;
    }

    public void setFormatter(BiFunction<String, Integer, FormattedCharSequence> param0) {
        this.formatter = param0;
    }

    public void tick() {
        ++this.frame;
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        Component var0 = this.getMessage();
        return Component.translatable("gui.narrate.editBox", var0, this.value);
    }

    public void setValue(String param0) {
        if (this.filter.test(param0)) {
            if (param0.length() > this.maxLength) {
                this.value = param0.substring(0, this.maxLength);
            } else {
                this.value = param0;
            }

            this.moveCursorToEnd();
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(param0);
        }
    }

    public String getValue() {
        return this.value;
    }

    public String getHighlighted() {
        int var0 = Math.min(this.cursorPos, this.highlightPos);
        int var1 = Math.max(this.cursorPos, this.highlightPos);
        return this.value.substring(var0, var1);
    }

    public void setFilter(Predicate<String> param0) {
        this.filter = param0;
    }

    public void insertText(String param0) {
        int var0 = Math.min(this.cursorPos, this.highlightPos);
        int var1 = Math.max(this.cursorPos, this.highlightPos);
        int var2 = this.maxLength - this.value.length() - (var0 - var1);
        String var3 = SharedConstants.filterText(param0);
        int var4 = var3.length();
        if (var2 < var4) {
            var3 = var3.substring(0, var2);
            var4 = var2;
        }

        String var5 = new StringBuilder(this.value).replace(var0, var1, var3).toString();
        if (this.filter.test(var5)) {
            this.value = var5;
            this.setCursorPosition(var0 + var4);
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(this.value);
        }
    }

    private void onValueChange(String param0) {
        if (this.responder != null) {
            this.responder.accept(param0);
        }

    }

    private void deleteText(int param0) {
        if (Screen.hasControlDown()) {
            this.deleteWords(param0);
        } else {
            this.deleteChars(param0);
        }

    }

    public void deleteWords(int param0) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                this.deleteChars(this.getWordPosition(param0) - this.cursorPos);
            }
        }
    }

    public void deleteChars(int param0) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                int var0 = this.getCursorPos(param0);
                int var1 = Math.min(var0, this.cursorPos);
                int var2 = Math.max(var0, this.cursorPos);
                if (var1 != var2) {
                    String var3 = new StringBuilder(this.value).delete(var1, var2).toString();
                    if (this.filter.test(var3)) {
                        this.value = var3;
                        this.moveCursorTo(var1);
                    }
                }
            }
        }
    }

    public int getWordPosition(int param0) {
        return this.getWordPosition(param0, this.getCursorPosition());
    }

    private int getWordPosition(int param0, int param1) {
        return this.getWordPosition(param0, param1, true);
    }

    private int getWordPosition(int param0, int param1, boolean param2) {
        int var0 = param1;
        boolean var1 = param0 < 0;
        int var2 = Math.abs(param0);

        for(int var3 = 0; var3 < var2; ++var3) {
            if (!var1) {
                int var4 = this.value.length();
                var0 = this.value.indexOf(32, var0);
                if (var0 == -1) {
                    var0 = var4;
                } else {
                    while(param2 && var0 < var4 && this.value.charAt(var0) == ' ') {
                        ++var0;
                    }
                }
            } else {
                while(param2 && var0 > 0 && this.value.charAt(var0 - 1) == ' ') {
                    --var0;
                }

                while(var0 > 0 && this.value.charAt(var0 - 1) != ' ') {
                    --var0;
                }
            }
        }

        return var0;
    }

    public void moveCursor(int param0) {
        this.moveCursorTo(this.getCursorPos(param0));
    }

    private int getCursorPos(int param0) {
        return Util.offsetByCodepoints(this.value, this.cursorPos, param0);
    }

    public void moveCursorTo(int param0) {
        this.setCursorPosition(param0);
        if (!this.shiftPressed) {
            this.setHighlightPos(this.cursorPos);
        }

        this.onValueChange(this.value);
    }

    public void setCursorPosition(int param0) {
        this.cursorPos = Mth.clamp(param0, 0, this.value.length());
    }

    public void moveCursorToStart() {
        this.moveCursorTo(0);
    }

    public void moveCursorToEnd() {
        this.moveCursorTo(this.value.length());
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (!this.canConsumeInput()) {
            return false;
        } else {
            this.shiftPressed = Screen.hasShiftDown();
            if (Screen.isSelectAll(param0)) {
                this.moveCursorToEnd();
                this.setHighlightPos(0);
                return true;
            } else if (Screen.isCopy(param0)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                return true;
            } else if (Screen.isPaste(param0)) {
                if (this.isEditable) {
                    this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
                }

                return true;
            } else if (Screen.isCut(param0)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                if (this.isEditable) {
                    this.insertText("");
                }

                return true;
            } else {
                switch(param0) {
                    case 259:
                        if (this.isEditable) {
                            this.shiftPressed = false;
                            this.deleteText(-1);
                            this.shiftPressed = Screen.hasShiftDown();
                        }

                        return true;
                    case 260:
                    case 264:
                    case 265:
                    case 266:
                    case 267:
                    default:
                        return false;
                    case 261:
                        if (this.isEditable) {
                            this.shiftPressed = false;
                            this.deleteText(1);
                            this.shiftPressed = Screen.hasShiftDown();
                        }

                        return true;
                    case 262:
                        if (Screen.hasControlDown()) {
                            this.moveCursorTo(this.getWordPosition(1));
                        } else {
                            this.moveCursor(1);
                        }

                        return true;
                    case 263:
                        if (Screen.hasControlDown()) {
                            this.moveCursorTo(this.getWordPosition(-1));
                        } else {
                            this.moveCursor(-1);
                        }

                        return true;
                    case 268:
                        this.moveCursorToStart();
                        return true;
                    case 269:
                        this.moveCursorToEnd();
                        return true;
                }
            }
        }
    }

    public boolean canConsumeInput() {
        return this.isVisible() && this.isFocused() && this.isEditable();
    }

    @Override
    public boolean charTyped(char param0, int param1) {
        if (!this.canConsumeInput()) {
            return false;
        } else if (SharedConstants.isAllowedChatCharacter(param0)) {
            if (this.isEditable) {
                this.insertText(Character.toString(param0));
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onClick(double param0, double param1) {
        int var0 = Mth.floor(param0) - this.getX();
        if (this.bordered) {
            var0 -= 4;
        }

        String var1 = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
        this.moveCursorTo(this.font.plainSubstrByWidth(var1, var0).length() + this.displayPos);
    }

    @Override
    public void playDownSound(SoundManager param0) {
    }

    @Override
    public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
        if (this.isVisible()) {
            if (this.isBordered()) {
                int var0 = this.isFocused() ? -1 : -6250336;
                param0.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, var0);
                param0.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, -16777216);
            }

            int var1 = this.isEditable ? this.textColor : this.textColorUneditable;
            int var2 = this.cursorPos - this.displayPos;
            int var3 = this.highlightPos - this.displayPos;
            String var4 = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
            boolean var5 = var2 >= 0 && var2 <= var4.length();
            boolean var6 = this.isFocused() && this.frame / 6 % 2 == 0 && var5;
            int var7 = this.bordered ? this.getX() + 4 : this.getX();
            int var8 = this.bordered ? this.getY() + (this.height - 8) / 2 : this.getY();
            int var9 = var7;
            if (var3 > var4.length()) {
                var3 = var4.length();
            }

            if (!var4.isEmpty()) {
                String var10 = var5 ? var4.substring(0, var2) : var4;
                var9 = param0.drawString(this.font, this.formatter.apply(var10, this.displayPos), var7, var8, var1);
            }

            boolean var11 = this.cursorPos < this.value.length() || this.value.length() >= this.getMaxLength();
            int var12 = var9;
            if (!var5) {
                var12 = var2 > 0 ? var7 + this.width : var7;
            } else if (var11) {
                var12 = var9 - 1;
                --var9;
            }

            if (!var4.isEmpty() && var5 && var2 < var4.length()) {
                param0.drawString(this.font, this.formatter.apply(var4.substring(var2), this.cursorPos), var9, var8, var1);
            }

            if (this.hint != null && var4.isEmpty() && !this.isFocused()) {
                param0.drawString(this.font, this.hint, var9, var8, var1);
            }

            if (!var11 && this.suggestion != null) {
                param0.drawString(this.font, this.suggestion, var12 - 1, var8, -8355712);
            }

            if (var6) {
                if (var11) {
                    param0.fill(var12, var8 - 1, var12 + 1, var8 + 1 + 9, -3092272);
                } else {
                    param0.drawString(this.font, "_", var12, var8, var1);
                }
            }

            if (var3 != var2) {
                int var13 = var7 + this.font.width(var4.substring(0, var3));
                this.renderHighlight(param0, var12, var8 - 1, var13 - 1, var8 + 1 + 9);
            }

        }
    }

    private void renderHighlight(GuiGraphics param0, int param1, int param2, int param3, int param4) {
        if (param1 < param3) {
            int var0 = param1;
            param1 = param3;
            param3 = var0;
        }

        if (param2 < param4) {
            int var1 = param2;
            param2 = param4;
            param4 = var1;
        }

        if (param3 > this.getX() + this.width) {
            param3 = this.getX() + this.width;
        }

        if (param1 > this.getX() + this.width) {
            param1 = this.getX() + this.width;
        }

        param0.fill(RenderType.guiTextHighlight(), param1, param2, param3, param4, -16776961);
    }

    public void setMaxLength(int param0) {
        this.maxLength = param0;
        if (this.value.length() > param0) {
            this.value = this.value.substring(0, param0);
            this.onValueChange(this.value);
        }

    }

    private int getMaxLength() {
        return this.maxLength;
    }

    public int getCursorPosition() {
        return this.cursorPos;
    }

    private boolean isBordered() {
        return this.bordered;
    }

    public void setBordered(boolean param0) {
        this.bordered = param0;
    }

    public void setTextColor(int param0) {
        this.textColor = param0;
    }

    public void setTextColorUneditable(int param0) {
        this.textColorUneditable = param0;
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent param0) {
        return this.visible && this.isEditable ? super.nextFocusPath(param0) : null;
    }

    @Override
    public boolean isMouseOver(double param0, double param1) {
        return this.visible
            && param0 >= (double)this.getX()
            && param0 < (double)(this.getX() + this.width)
            && param1 >= (double)this.getY()
            && param1 < (double)(this.getY() + this.height);
    }

    @Override
    public void setFocused(boolean param0) {
        if (this.canLoseFocus || param0) {
            super.setFocused(param0);
            if (param0) {
                this.frame = 0;
            }

        }
    }

    private boolean isEditable() {
        return this.isEditable;
    }

    public void setEditable(boolean param0) {
        this.isEditable = param0;
    }

    public int getInnerWidth() {
        return this.isBordered() ? this.width - 8 : this.width;
    }

    public void setHighlightPos(int param0) {
        int var0 = this.value.length();
        this.highlightPos = Mth.clamp(param0, 0, var0);
        if (this.font != null) {
            if (this.displayPos > var0) {
                this.displayPos = var0;
            }

            int var1 = this.getInnerWidth();
            String var2 = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), var1);
            int var3 = var2.length() + this.displayPos;
            if (this.highlightPos == this.displayPos) {
                this.displayPos -= this.font.plainSubstrByWidth(this.value, var1, true).length();
            }

            if (this.highlightPos > var3) {
                this.displayPos += this.highlightPos - var3;
            } else if (this.highlightPos <= this.displayPos) {
                this.displayPos -= this.displayPos - this.highlightPos;
            }

            this.displayPos = Mth.clamp(this.displayPos, 0, var0);
        }

    }

    public void setCanLoseFocus(boolean param0) {
        this.canLoseFocus = param0;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean param0) {
        this.visible = param0;
    }

    public void setSuggestion(@Nullable String param0) {
        this.suggestion = param0;
    }

    public int getScreenX(int param0) {
        return param0 > this.value.length() ? this.getX() : this.getX() + this.font.width(this.value.substring(0, param0));
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput param0) {
        param0.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
    }

    public void setHint(Component param0) {
        this.hint = param0;
    }
}
