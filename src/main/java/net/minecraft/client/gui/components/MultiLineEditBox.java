package net.minecraft.client.gui.components;

import java.util.function.Consumer;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MultiLineEditBox extends AbstractScrollWidget {
    private static final int CURSOR_INSERT_WIDTH = 1;
    private static final int CURSOR_INSERT_COLOR = -3092272;
    private static final String CURSOR_APPEND_CHARACTER = "_";
    private static final int TEXT_COLOR = -2039584;
    private static final int PLACEHOLDER_TEXT_COLOR = -857677600;
    private static final int CURSOR_BLINK_INTERVAL_MS = 300;
    private final Font font;
    private final Component placeholder;
    private final MultilineTextField textField;
    private long focusedTime = Util.getMillis();

    public MultiLineEditBox(Font param0, int param1, int param2, int param3, int param4, Component param5, Component param6) {
        super(param1, param2, param3, param4, param6);
        this.font = param0;
        this.placeholder = param5;
        this.textField = new MultilineTextField(param0, param3 - this.totalInnerPadding());
        this.textField.setCursorListener(this::scrollToCursor);
    }

    public void setCharacterLimit(int param0) {
        this.textField.setCharacterLimit(param0);
    }

    public void setValueListener(Consumer<String> param0) {
        this.textField.setValueListener(param0);
    }

    public void setValue(String param0) {
        this.textField.setValue(param0);
    }

    public String getValue() {
        return this.textField.value();
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput param0) {
        param0.add(NarratedElementType.TITLE, (Component)Component.translatable("gui.narrate.editBox", this.getMessage(), this.getValue()));
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (this.withinContentAreaPoint(param0, param1) && param2 == 0) {
            this.textField.setSelecting(Screen.hasShiftDown());
            this.seekCursorScreen(param0, param1);
            return true;
        } else {
            return super.mouseClicked(param0, param1, param2);
        }
    }

    @Override
    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        if (super.mouseDragged(param0, param1, param2, param3, param4)) {
            return true;
        } else if (this.withinContentAreaPoint(param0, param1) && param2 == 0) {
            this.textField.setSelecting(true);
            this.seekCursorScreen(param0, param1);
            this.textField.setSelecting(Screen.hasShiftDown());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        return this.textField.keyPressed(param0);
    }

    @Override
    public boolean charTyped(char param0, int param1) {
        if (this.visible && this.isFocused() && SharedConstants.isAllowedChatCharacter(param0)) {
            this.textField.insertText(Character.toString(param0));
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void renderContents(GuiGraphics param0, int param1, int param2, float param3) {
        String var0 = this.textField.value();
        if (var0.isEmpty() && !this.isFocused()) {
            param0.drawWordWrap(
                this.font,
                this.placeholder,
                this.getX() + this.innerPadding(),
                this.getY() + this.innerPadding(),
                this.width - this.totalInnerPadding(),
                -857677600
            );
        } else {
            int var1 = this.textField.cursor();
            boolean var2 = this.isFocused() && (Util.getMillis() - this.focusedTime) / 300L % 2L == 0L;
            boolean var3 = var1 < var0.length();
            int var4 = 0;
            int var5 = 0;
            int var6 = this.getY() + this.innerPadding();

            for(MultilineTextField.StringView var7 : this.textField.iterateLines()) {
                boolean var8 = this.withinContentAreaTopBottom(var6, var6 + 9);
                if (var2 && var3 && var1 >= var7.beginIndex() && var1 <= var7.endIndex()) {
                    if (var8) {
                        var4 = param0.drawString(this.font, var0.substring(var7.beginIndex(), var1), this.getX() + this.innerPadding(), var6, -2039584) - 1;
                        param0.fill(var4, var6 - 1, var4 + 1, var6 + 1 + 9, -3092272);
                        param0.drawString(this.font, var0.substring(var1, var7.endIndex()), var4, var6, -2039584);
                    }
                } else {
                    if (var8) {
                        var4 = param0.drawString(
                                this.font, var0.substring(var7.beginIndex(), var7.endIndex()), this.getX() + this.innerPadding(), var6, -2039584
                            )
                            - 1;
                    }

                    var5 = var6;
                }

                var6 += 9;
            }

            if (var2 && !var3 && this.withinContentAreaTopBottom(var5, var5 + 9)) {
                param0.drawString(this.font, "_", var4, var5, -3092272);
            }

            if (this.textField.hasSelection()) {
                MultilineTextField.StringView var9 = this.textField.getSelected();
                int var10 = this.getX() + this.innerPadding();
                var6 = this.getY() + this.innerPadding();

                for(MultilineTextField.StringView var11 : this.textField.iterateLines()) {
                    if (var9.beginIndex() > var11.endIndex()) {
                        var6 += 9;
                    } else {
                        if (var11.beginIndex() > var9.endIndex()) {
                            break;
                        }

                        if (this.withinContentAreaTopBottom(var6, var6 + 9)) {
                            int var12 = this.font.width(var0.substring(var11.beginIndex(), Math.max(var9.beginIndex(), var11.beginIndex())));
                            int var13;
                            if (var9.endIndex() > var11.endIndex()) {
                                var13 = this.width - this.innerPadding();
                            } else {
                                var13 = this.font.width(var0.substring(var11.beginIndex(), var9.endIndex()));
                            }

                            this.renderHighlight(param0, var10 + var12, var6, var10 + var13, var6 + 9);
                        }

                        var6 += 9;
                    }
                }
            }

        }
    }

    @Override
    protected void renderDecorations(GuiGraphics param0) {
        super.renderDecorations(param0);
        if (this.textField.hasCharacterLimit()) {
            int var0 = this.textField.characterLimit();
            Component var1 = Component.translatable("gui.multiLineEditBox.character_limit", this.textField.value().length(), var0);
            param0.drawString(this.font, var1, this.getX() + this.width - this.font.width(var1), this.getY() + this.height + 4, 10526880);
        }

    }

    @Override
    public int getInnerHeight() {
        return 9 * this.textField.getLineCount();
    }

    @Override
    protected boolean scrollbarVisible() {
        return (double)this.textField.getLineCount() > this.getDisplayableLineCount();
    }

    @Override
    protected double scrollRate() {
        return 9.0 / 2.0;
    }

    private void renderHighlight(GuiGraphics param0, int param1, int param2, int param3, int param4) {
        param0.fill(RenderType.guiTextHighlight(), param1, param2, param3, param4, -16776961);
    }

    private void scrollToCursor() {
        double var0 = this.scrollAmount();
        MultilineTextField.StringView var1 = this.textField.getLineView((int)(var0 / 9.0));
        if (this.textField.cursor() <= var1.beginIndex()) {
            var0 = (double)(this.textField.getLineAtCursor() * 9);
        } else {
            MultilineTextField.StringView var2 = this.textField.getLineView((int)((var0 + (double)this.height) / 9.0) - 1);
            if (this.textField.cursor() > var2.endIndex()) {
                var0 = (double)(this.textField.getLineAtCursor() * 9 - this.height + 9 + this.totalInnerPadding());
            }
        }

        this.setScrollAmount(var0);
    }

    private double getDisplayableLineCount() {
        return (double)(this.height - this.totalInnerPadding()) / 9.0;
    }

    private void seekCursorScreen(double param0, double param1) {
        double var0 = param0 - (double)this.getX() - (double)this.innerPadding();
        double var1 = param1 - (double)this.getY() - (double)this.innerPadding() + this.scrollAmount();
        this.textField.seekCursorToPoint(var0, var1);
    }

    @Override
    public void setFocused(boolean param0) {
        super.setFocused(param0);
        if (param0) {
            this.focusedTime = Util.getMillis();
        }

    }
}
