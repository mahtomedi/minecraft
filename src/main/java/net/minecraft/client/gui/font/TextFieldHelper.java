package net.minecraft.client.gui.font;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextFieldHelper {
    private final Minecraft minecraft;
    private final Font font;
    private final Supplier<String> getMessageFn;
    private final Consumer<String> setMessageFn;
    private final int maxWidth;
    private int cursorPos;
    private int selectionPos;

    public TextFieldHelper(Minecraft param0, Supplier<String> param1, Consumer<String> param2, int param3) {
        this.minecraft = param0;
        this.font = param0.font;
        this.getMessageFn = param1;
        this.setMessageFn = param2;
        this.maxWidth = param3;
        this.setEnd();
    }

    public boolean charTyped(char param0) {
        if (SharedConstants.isAllowedChatCharacter(param0)) {
            this.insertText(Character.toString(param0));
        }

        return true;
    }

    private void insertText(String param0) {
        if (this.selectionPos != this.cursorPos) {
            this.deleteSelection();
        }

        String var0 = this.getMessageFn.get();
        this.cursorPos = Mth.clamp(this.cursorPos, 0, var0.length());
        String var1 = new StringBuilder(var0).insert(this.cursorPos, param0).toString();
        if (this.font.width(var1) <= this.maxWidth) {
            this.setMessageFn.accept(var1);
            this.selectionPos = this.cursorPos = Math.min(var1.length(), this.cursorPos + param0.length());
        }

    }

    public boolean keyPressed(int param0) {
        String var0 = this.getMessageFn.get();
        if (Screen.isSelectAll(param0)) {
            this.selectionPos = 0;
            this.cursorPos = var0.length();
            return true;
        } else if (Screen.isCopy(param0)) {
            this.minecraft.keyboardHandler.setClipboard(this.getSelected());
            return true;
        } else if (Screen.isPaste(param0)) {
            this.insertText(SharedConstants.filterText(ChatFormatting.stripFormatting(this.minecraft.keyboardHandler.getClipboard().replaceAll("\\r", ""))));
            this.selectionPos = this.cursorPos;
            return true;
        } else if (Screen.isCut(param0)) {
            this.minecraft.keyboardHandler.setClipboard(this.getSelected());
            this.deleteSelection();
            return true;
        } else if (param0 == 259) {
            if (!var0.isEmpty()) {
                if (this.selectionPos != this.cursorPos) {
                    this.deleteSelection();
                } else if (this.cursorPos > 0) {
                    var0 = new StringBuilder(var0).deleteCharAt(Math.max(0, this.cursorPos - 1)).toString();
                    this.selectionPos = this.cursorPos = Math.max(0, this.cursorPos - 1);
                    this.setMessageFn.accept(var0);
                }
            }

            return true;
        } else if (param0 == 261) {
            if (!var0.isEmpty()) {
                if (this.selectionPos != this.cursorPos) {
                    this.deleteSelection();
                } else if (this.cursorPos < var0.length()) {
                    var0 = new StringBuilder(var0).deleteCharAt(Math.max(0, this.cursorPos)).toString();
                    this.setMessageFn.accept(var0);
                }
            }

            return true;
        } else if (param0 == 263) {
            int var1 = this.font.isBidirectional() ? 1 : -1;
            if (Screen.hasControlDown()) {
                this.cursorPos = this.font.getWordPosition(var0, var1, this.cursorPos, true);
            } else {
                this.cursorPos = Math.max(0, Math.min(var0.length(), this.cursorPos + var1));
            }

            if (!Screen.hasShiftDown()) {
                this.selectionPos = this.cursorPos;
            }

            return true;
        } else if (param0 == 262) {
            int var2 = this.font.isBidirectional() ? -1 : 1;
            if (Screen.hasControlDown()) {
                this.cursorPos = this.font.getWordPosition(var0, var2, this.cursorPos, true);
            } else {
                this.cursorPos = Math.max(0, Math.min(var0.length(), this.cursorPos + var2));
            }

            if (!Screen.hasShiftDown()) {
                this.selectionPos = this.cursorPos;
            }

            return true;
        } else if (param0 == 268) {
            this.cursorPos = 0;
            if (!Screen.hasShiftDown()) {
                this.selectionPos = this.cursorPos;
            }

            return true;
        } else if (param0 == 269) {
            this.cursorPos = this.getMessageFn.get().length();
            if (!Screen.hasShiftDown()) {
                this.selectionPos = this.cursorPos;
            }

            return true;
        } else {
            return false;
        }
    }

    private String getSelected() {
        String var0 = this.getMessageFn.get();
        int var1 = Math.min(this.cursorPos, this.selectionPos);
        int var2 = Math.max(this.cursorPos, this.selectionPos);
        return var0.substring(var1, var2);
    }

    private void deleteSelection() {
        if (this.selectionPos != this.cursorPos) {
            String var0 = this.getMessageFn.get();
            int var1 = Math.min(this.cursorPos, this.selectionPos);
            int var2 = Math.max(this.cursorPos, this.selectionPos);
            String var3 = var0.substring(0, var1) + var0.substring(var2);
            this.cursorPos = var1;
            this.selectionPos = this.cursorPos;
            this.setMessageFn.accept(var3);
        }
    }

    public void setEnd() {
        this.selectionPos = this.cursorPos = this.getMessageFn.get().length();
    }

    public int getCursorPos() {
        return this.cursorPos;
    }

    public int getSelectionPos() {
        return this.selectionPos;
    }
}
