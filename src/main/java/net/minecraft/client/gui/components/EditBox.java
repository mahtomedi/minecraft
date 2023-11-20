package net.minecraft.client.gui.components;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EditBox extends AbstractWidget implements Renderable {
    private static final WidgetSprites SPRITES = new WidgetSprites(
        new ResourceLocation("widget/text_field"), new ResourceLocation("widget/text_field_highlighted")
    );
    public static final int BACKWARDS = -1;
    public static final int FORWARDS = 1;
    private static final int CURSOR_INSERT_WIDTH = 1;
    private static final int CURSOR_INSERT_COLOR = -3092272;
    private static final String CURSOR_APPEND_CHARACTER = "_";
    public static final int DEFAULT_TEXT_COLOR = 14737632;
    private static final int CURSOR_BLINK_INTERVAL_MS = 300;
    private final Font font;
    private String value = "";
    private int maxLength = 32;
    private boolean bordered = true;
    private boolean canLoseFocus = true;
    private boolean isEditable = true;
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
    private long focusedTime = Util.getMillis();

    public EditBox(Font param0, int param1, int param2, Component param3) {
        this(param0, 0, 0, param1, param2, param3);
    }

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

            this.moveCursorToEnd(false);
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
        if (var2 > 0) {
            String var3 = SharedConstants.filterText(param0);
            int var4 = var3.length();
            if (var2 < var4) {
                if (Character.isHighSurrogate(var3.charAt(var2 - 1))) {
                    --var2;
                }

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
                this.deleteCharsToPos(this.getWordPosition(param0));
            }
        }
    }

    public void deleteChars(int param0) {
        this.deleteCharsToPos(this.getCursorPos(param0));
    }

    public void deleteCharsToPos(int param0) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                int var0 = Math.min(param0, this.cursorPos);
                int var1 = Math.max(param0, this.cursorPos);
                if (var0 != var1) {
                    String var2 = new StringBuilder(this.value).delete(var0, var1).toString();
                    if (this.filter.test(var2)) {
                        this.value = var2;
                        this.moveCursorTo(var0, false);
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

    public void moveCursor(int param0, boolean param1) {
        this.moveCursorTo(this.getCursorPos(param0), param1);
    }

    private int getCursorPos(int param0) {
        return Util.offsetByCodepoints(this.value, this.cursorPos, param0);
    }

    public void moveCursorTo(int param0, boolean param1) {
        this.setCursorPosition(param0);
        if (!param1) {
            this.setHighlightPos(this.cursorPos);
        }

        this.onValueChange(this.value);
    }

    public void setCursorPosition(int param0) {
        this.cursorPos = Mth.clamp(param0, 0, this.value.length());
        this.scrollTo(this.cursorPos);
    }

    public void moveCursorToStart(boolean param0) {
        this.moveCursorTo(0, param0);
    }

    public void moveCursorToEnd(boolean param0) {
        this.moveCursorTo(this.value.length(), param0);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (this.isActive() && this.isFocused()) {
            switch(param0) {
                case 259:
                    if (this.isEditable) {
                        this.deleteText(-1);
                    }

                    return true;
                case 260:
                case 264:
                case 265:
                case 266:
                case 267:
                default:
                    if (Screen.isSelectAll(param0)) {
                        this.moveCursorToEnd(false);
                        this.setHighlightPos(0);
                        return true;
                    } else if (Screen.isCopy(param0)) {
                        Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                        return true;
                    } else if (Screen.isPaste(param0)) {
                        if (this.isEditable()) {
                            this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
                        }

                        return true;
                    } else {
                        if (Screen.isCut(param0)) {
                            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                            if (this.isEditable()) {
                                this.insertText("");
                            }

                            return true;
                        }

                        return false;
                    }
                case 261:
                    if (this.isEditable) {
                        this.deleteText(1);
                    }

                    return true;
                case 262:
                    if (Screen.hasControlDown()) {
                        this.moveCursorTo(this.getWordPosition(1), Screen.hasShiftDown());
                    } else {
                        this.moveCursor(1, Screen.hasShiftDown());
                    }

                    return true;
                case 263:
                    if (Screen.hasControlDown()) {
                        this.moveCursorTo(this.getWordPosition(-1), Screen.hasShiftDown());
                    } else {
                        this.moveCursor(-1, Screen.hasShiftDown());
                    }

                    return true;
                case 268:
                    this.moveCursorToStart(Screen.hasShiftDown());
                    return true;
                case 269:
                    this.moveCursorToEnd(Screen.hasShiftDown());
                    return true;
            }
        } else {
            return false;
        }
    }

    public boolean canConsumeInput() {
        return this.isActive() && this.isFocused() && this.isEditable();
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
        this.moveCursorTo(this.font.plainSubstrByWidth(var1, var0).length() + this.displayPos, Screen.hasShiftDown());
    }

    @Override
    public void playDownSound(SoundManager param0) {
    }

    @Override
    public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
        if (this.isVisible()) {
            if (this.isBordered()) {
                ResourceLocation var0 = SPRITES.get(this.isActive(), this.isFocused());
                param0.blitSprite(var0, this.getX(), this.getY(), this.getWidth(), this.getHeight());
            }

            int var1 = this.isEditable ? this.textColor : this.textColorUneditable;
            int var2 = this.cursorPos - this.displayPos;
            String var3 = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
            boolean var4 = var2 >= 0 && var2 <= var3.length();
            boolean var5 = this.isFocused() && (Util.getMillis() - this.focusedTime) / 300L % 2L == 0L && var4;
            int var6 = this.bordered ? this.getX() + 4 : this.getX();
            int var7 = this.bordered ? this.getY() + (this.height - 8) / 2 : this.getY();
            int var8 = var6;
            int var9 = Mth.clamp(this.highlightPos - this.displayPos, 0, var3.length());
            if (!var3.isEmpty()) {
                String var10 = var4 ? var3.substring(0, var2) : var3;
                var8 = param0.drawString(this.font, this.formatter.apply(var10, this.displayPos), var6, var7, var1);
            }

            boolean var11 = this.cursorPos < this.value.length() || this.value.length() >= this.getMaxLength();
            int var12 = var8;
            if (!var4) {
                var12 = var2 > 0 ? var6 + this.width : var6;
            } else if (var11) {
                var12 = var8 - 1;
                --var8;
            }

            if (!var3.isEmpty() && var4 && var2 < var3.length()) {
                param0.drawString(this.font, this.formatter.apply(var3.substring(var2), this.cursorPos), var8, var7, var1);
            }

            if (this.hint != null && var3.isEmpty() && !this.isFocused()) {
                param0.drawString(this.font, this.hint, var8, var7, var1);
            }

            if (!var11 && this.suggestion != null) {
                param0.drawString(this.font, this.suggestion, var12 - 1, var7, -8355712);
            }

            if (var5) {
                if (var11) {
                    param0.fill(RenderType.guiOverlay(), var12, var7 - 1, var12 + 1, var7 + 1 + 9, -3092272);
                } else {
                    param0.drawString(this.font, "_", var12, var7, var1);
                }
            }

            if (var9 != var2) {
                int var13 = var6 + this.font.width(var3.substring(0, var9));
                this.renderHighlight(param0, var12, var7 - 1, var13 - 1, var7 + 1 + 9);
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

    public boolean isBordered() {
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

    @Override
    public void setFocused(boolean param0) {
        if (this.canLoseFocus || param0) {
            super.setFocused(param0);
            if (param0) {
                this.focusedTime = Util.getMillis();
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
        this.highlightPos = Mth.clamp(param0, 0, this.value.length());
        this.scrollTo(this.highlightPos);
    }

    private void scrollTo(int param0) {
        if (this.font != null) {
            this.displayPos = Math.min(this.displayPos, this.value.length());
            int var0 = this.getInnerWidth();
            String var1 = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), var0);
            int var2 = var1.length() + this.displayPos;
            if (param0 == this.displayPos) {
                this.displayPos -= this.font.plainSubstrByWidth(this.value, var0, true).length();
            }

            if (param0 > var2) {
                this.displayPos += param0 - var2;
            } else if (param0 <= this.displayPos) {
                this.displayPos -= this.displayPos - param0;
            }

            this.displayPos = Mth.clamp(this.displayPos, 0, this.value.length());
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
