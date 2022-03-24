package net.minecraft.client.gui.font;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextFieldHelper {
    private final Supplier<String> getMessageFn;
    private final Consumer<String> setMessageFn;
    private final Supplier<String> getClipboardFn;
    private final Consumer<String> setClipboardFn;
    private final Predicate<String> stringValidator;
    private int cursorPos;
    private int selectionPos;

    public TextFieldHelper(Supplier<String> param0, Consumer<String> param1, Supplier<String> param2, Consumer<String> param3, Predicate<String> param4) {
        this.getMessageFn = param0;
        this.setMessageFn = param1;
        this.getClipboardFn = param2;
        this.setClipboardFn = param3;
        this.stringValidator = param4;
        this.setCursorToEnd();
    }

    public static Supplier<String> createClipboardGetter(Minecraft param0) {
        return () -> getClipboardContents(param0);
    }

    public static String getClipboardContents(Minecraft param0) {
        return ChatFormatting.stripFormatting(param0.keyboardHandler.getClipboard().replaceAll("\\r", ""));
    }

    public static Consumer<String> createClipboardSetter(Minecraft param0) {
        return param1 -> setClipboardContents(param0, param1);
    }

    public static void setClipboardContents(Minecraft param0, String param1) {
        param0.keyboardHandler.setClipboard(param1);
    }

    public boolean charTyped(char param0) {
        if (SharedConstants.isAllowedChatCharacter(param0)) {
            this.insertText(this.getMessageFn.get(), Character.toString(param0));
        }

        return true;
    }

    public boolean keyPressed(int param0) {
        if (Screen.isSelectAll(param0)) {
            this.selectAll();
            return true;
        } else if (Screen.isCopy(param0)) {
            this.copy();
            return true;
        } else if (Screen.isPaste(param0)) {
            this.paste();
            return true;
        } else if (Screen.isCut(param0)) {
            this.cut();
            return true;
        } else {
            TextFieldHelper.CursorStep var0 = Screen.hasControlDown() ? TextFieldHelper.CursorStep.WORD : TextFieldHelper.CursorStep.CHARACTER;
            if (param0 == 259) {
                this.removeFromCursor(-1, var0);
                return true;
            } else {
                if (param0 == 261) {
                    this.removeFromCursor(1, var0);
                } else {
                    if (param0 == 263) {
                        this.moveBy(-1, Screen.hasShiftDown(), var0);
                        return true;
                    }

                    if (param0 == 262) {
                        this.moveBy(1, Screen.hasShiftDown(), var0);
                        return true;
                    }

                    if (param0 == 268) {
                        this.setCursorToStart(Screen.hasShiftDown());
                        return true;
                    }

                    if (param0 == 269) {
                        this.setCursorToEnd(Screen.hasShiftDown());
                        return true;
                    }
                }

                return false;
            }
        }
    }

    private int clampToMsgLength(int param0) {
        return Mth.clamp(param0, 0, this.getMessageFn.get().length());
    }

    private void insertText(String param0, String param1) {
        if (this.selectionPos != this.cursorPos) {
            param0 = this.deleteSelection(param0);
        }

        this.cursorPos = Mth.clamp(this.cursorPos, 0, param0.length());
        String var0 = new StringBuilder(param0).insert(this.cursorPos, param1).toString();
        if (this.stringValidator.test(var0)) {
            this.setMessageFn.accept(var0);
            this.selectionPos = this.cursorPos = Math.min(var0.length(), this.cursorPos + param1.length());
        }

    }

    public void insertText(String param0) {
        this.insertText(this.getMessageFn.get(), param0);
    }

    private void resetSelectionIfNeeded(boolean param0) {
        if (!param0) {
            this.selectionPos = this.cursorPos;
        }

    }

    public void moveBy(int param0, boolean param1, TextFieldHelper.CursorStep param2) {
        switch(param2) {
            case CHARACTER:
                this.moveByChars(param0, param1);
                break;
            case WORD:
                this.moveByWords(param0, param1);
        }

    }

    public void moveByChars(int param0) {
        this.moveByChars(param0, false);
    }

    public void moveByChars(int param0, boolean param1) {
        this.cursorPos = Util.offsetByCodepoints(this.getMessageFn.get(), this.cursorPos, param0);
        this.resetSelectionIfNeeded(param1);
    }

    public void moveByWords(int param0) {
        this.moveByWords(param0, false);
    }

    public void moveByWords(int param0, boolean param1) {
        this.cursorPos = StringSplitter.getWordPosition(this.getMessageFn.get(), param0, this.cursorPos, true);
        this.resetSelectionIfNeeded(param1);
    }

    public void removeFromCursor(int param0, TextFieldHelper.CursorStep param1) {
        switch(param1) {
            case CHARACTER:
                this.removeCharsFromCursor(param0);
                break;
            case WORD:
                this.removeWordsFromCursor(param0);
        }

    }

    public void removeWordsFromCursor(int param0) {
        int var0 = StringSplitter.getWordPosition(this.getMessageFn.get(), param0, this.cursorPos, true);
        this.removeCharsFromCursor(var0 - this.cursorPos);
    }

    public void removeCharsFromCursor(int param0) {
        String var0 = this.getMessageFn.get();
        if (!var0.isEmpty()) {
            String var1;
            if (this.selectionPos != this.cursorPos) {
                var1 = this.deleteSelection(var0);
            } else {
                int var2 = Util.offsetByCodepoints(var0, this.cursorPos, param0);
                int var3 = Math.min(var2, this.cursorPos);
                int var4 = Math.max(var2, this.cursorPos);
                var1 = new StringBuilder(var0).delete(var3, var4).toString();
                if (param0 < 0) {
                    this.selectionPos = this.cursorPos = var3;
                }
            }

            this.setMessageFn.accept(var1);
        }

    }

    public void cut() {
        String var0 = this.getMessageFn.get();
        this.setClipboardFn.accept(this.getSelected(var0));
        this.setMessageFn.accept(this.deleteSelection(var0));
    }

    public void paste() {
        this.insertText(this.getMessageFn.get(), this.getClipboardFn.get());
        this.selectionPos = this.cursorPos;
    }

    public void copy() {
        this.setClipboardFn.accept(this.getSelected(this.getMessageFn.get()));
    }

    public void selectAll() {
        this.selectionPos = 0;
        this.cursorPos = this.getMessageFn.get().length();
    }

    private String getSelected(String param0) {
        int var0 = Math.min(this.cursorPos, this.selectionPos);
        int var1 = Math.max(this.cursorPos, this.selectionPos);
        return param0.substring(var0, var1);
    }

    private String deleteSelection(String param0) {
        if (this.selectionPos == this.cursorPos) {
            return param0;
        } else {
            int var0 = Math.min(this.cursorPos, this.selectionPos);
            int var1 = Math.max(this.cursorPos, this.selectionPos);
            String var2 = param0.substring(0, var0) + param0.substring(var1);
            this.selectionPos = this.cursorPos = var0;
            return var2;
        }
    }

    public void setCursorToStart() {
        this.setCursorToStart(false);
    }

    public void setCursorToStart(boolean param0) {
        this.cursorPos = 0;
        this.resetSelectionIfNeeded(param0);
    }

    public void setCursorToEnd() {
        this.setCursorToEnd(false);
    }

    public void setCursorToEnd(boolean param0) {
        this.cursorPos = this.getMessageFn.get().length();
        this.resetSelectionIfNeeded(param0);
    }

    public int getCursorPos() {
        return this.cursorPos;
    }

    public void setCursorPos(int param0) {
        this.setCursorPos(param0, true);
    }

    public void setCursorPos(int param0, boolean param1) {
        this.cursorPos = this.clampToMsgLength(param0);
        this.resetSelectionIfNeeded(param1);
    }

    public int getSelectionPos() {
        return this.selectionPos;
    }

    public void setSelectionPos(int param0) {
        this.selectionPos = this.clampToMsgLength(param0);
    }

    public void setSelectionRange(int param0, int param1) {
        int var0 = this.getMessageFn.get().length();
        this.cursorPos = Mth.clamp(param0, 0, var0);
        this.selectionPos = Mth.clamp(param1, 0, var0);
    }

    public boolean isSelecting() {
        return this.cursorPos != this.selectionPos;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum CursorStep {
        CHARACTER,
        WORD;
    }
}
