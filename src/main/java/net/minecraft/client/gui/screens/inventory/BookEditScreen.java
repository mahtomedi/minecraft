package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import java.util.ListIterator;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BookEditScreen extends Screen {
    private final Player owner;
    private final ItemStack book;
    private boolean isModified;
    private boolean isSigning;
    private int frameTick;
    private int currentPage;
    private final List<String> pages = Lists.newArrayList();
    private String title = "";
    private int cursorPos;
    private int selectionPos;
    private long lastClickTime;
    private int lastIndex = -1;
    private PageButton forwardButton;
    private PageButton backButton;
    private Button doneButton;
    private Button signButton;
    private Button finalizeButton;
    private Button cancelButton;
    private final InteractionHand hand;

    public BookEditScreen(Player param0, ItemStack param1, InteractionHand param2) {
        super(NarratorChatListener.NO_TITLE);
        this.owner = param0;
        this.book = param1;
        this.hand = param2;
        CompoundTag var0 = param1.getTag();
        if (var0 != null) {
            ListTag var1 = var0.getList("pages", 8).copy();

            for(int var2 = 0; var2 < var1.size(); ++var2) {
                this.pages.add(var1.getString(var2));
            }
        }

        if (this.pages.isEmpty()) {
            this.pages.add("");
        }

    }

    private int getNumPages() {
        return this.pages.size();
    }

    @Override
    public void tick() {
        super.tick();
        ++this.frameTick;
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.signButton = this.addButton(new Button(this.width / 2 - 100, 196, 98, 20, I18n.get("book.signButton"), param0 -> {
            this.isSigning = true;
            this.updateButtonVisibility();
        }));
        this.doneButton = this.addButton(new Button(this.width / 2 + 2, 196, 98, 20, I18n.get("gui.done"), param0 -> {
            this.minecraft.setScreen(null);
            this.saveChanges(false);
        }));
        this.finalizeButton = this.addButton(new Button(this.width / 2 - 100, 196, 98, 20, I18n.get("book.finalizeButton"), param0 -> {
            if (this.isSigning) {
                this.saveChanges(true);
                this.minecraft.setScreen(null);
            }

        }));
        this.cancelButton = this.addButton(new Button(this.width / 2 + 2, 196, 98, 20, I18n.get("gui.cancel"), param0 -> {
            if (this.isSigning) {
                this.isSigning = false;
            }

            this.updateButtonVisibility();
        }));
        int var0 = (this.width - 192) / 2;
        int var1 = 2;
        this.forwardButton = this.addButton(new PageButton(var0 + 116, 159, true, param0 -> this.pageForward(), true));
        this.backButton = this.addButton(new PageButton(var0 + 43, 159, false, param0 -> this.pageBack(), true));
        this.updateButtonVisibility();
    }

    private String filterText(String param0) {
        StringBuilder var0 = new StringBuilder();

        for(char var1 : param0.toCharArray()) {
            if (var1 != 167 && var1 != 127) {
                var0.append(var1);
            }
        }

        return var0.toString();
    }

    private void pageBack() {
        if (this.currentPage > 0) {
            --this.currentPage;
            this.cursorPos = 0;
            this.selectionPos = this.cursorPos;
        }

        this.updateButtonVisibility();
    }

    private void pageForward() {
        if (this.currentPage < this.getNumPages() - 1) {
            ++this.currentPage;
            this.cursorPos = 0;
            this.selectionPos = this.cursorPos;
        } else {
            this.appendPageToBook();
            if (this.currentPage < this.getNumPages() - 1) {
                ++this.currentPage;
            }

            this.cursorPos = 0;
            this.selectionPos = this.cursorPos;
        }

        this.updateButtonVisibility();
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private void updateButtonVisibility() {
        this.backButton.visible = !this.isSigning && this.currentPage > 0;
        this.forwardButton.visible = !this.isSigning;
        this.doneButton.visible = !this.isSigning;
        this.signButton.visible = !this.isSigning;
        this.cancelButton.visible = this.isSigning;
        this.finalizeButton.visible = this.isSigning;
        this.finalizeButton.active = !this.title.trim().isEmpty();
    }

    private void eraseEmptyTrailingPages() {
        ListIterator<String> var0 = this.pages.listIterator(this.pages.size());

        while(var0.hasPrevious() && var0.previous().isEmpty()) {
            var0.remove();
        }

    }

    private void saveChanges(boolean param0) {
        if (this.isModified) {
            this.eraseEmptyTrailingPages();
            ListTag var0 = new ListTag();
            this.pages.stream().map(StringTag::new).forEach(var0::add);
            if (!this.pages.isEmpty()) {
                this.book.addTagElement("pages", var0);
            }

            if (param0) {
                this.book.addTagElement("author", new StringTag(this.owner.getGameProfile().getName()));
                this.book.addTagElement("title", new StringTag(this.title.trim()));
            }

            this.minecraft.getConnection().send(new ServerboundEditBookPacket(this.book, param0, this.hand));
        }
    }

    private void appendPageToBook() {
        if (this.getNumPages() < 100) {
            this.pages.add("");
            this.isModified = true;
        }
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (super.keyPressed(param0, param1, param2)) {
            return true;
        } else {
            return this.isSigning ? this.titleKeyPressed(param0, param1, param2) : this.bookKeyPressed(param0, param1, param2);
        }
    }

    @Override
    public boolean charTyped(char param0, int param1) {
        if (super.charTyped(param0, param1)) {
            return true;
        } else if (this.isSigning) {
            if (this.title.length() < 16 && SharedConstants.isAllowedChatCharacter(param0)) {
                this.title = this.title + Character.toString(param0);
                this.updateButtonVisibility();
                this.isModified = true;
                return true;
            } else {
                return false;
            }
        } else if (SharedConstants.isAllowedChatCharacter(param0)) {
            this.insertText(Character.toString(param0));
            return true;
        } else {
            return false;
        }
    }

    private boolean bookKeyPressed(int param0, int param1, int param2) {
        String var0 = this.getCurrentPageText();
        if (Screen.isSelectAll(param0)) {
            this.selectionPos = 0;
            this.cursorPos = var0.length();
            return true;
        } else if (Screen.isCopy(param0)) {
            this.minecraft.keyboardHandler.setClipboard(this.getSelected());
            return true;
        } else if (Screen.isPaste(param0)) {
            this.insertText(this.filterText(ChatFormatting.stripFormatting(this.minecraft.keyboardHandler.getClipboard().replaceAll("\\r", ""))));
            this.selectionPos = this.cursorPos;
            return true;
        } else if (Screen.isCut(param0)) {
            this.minecraft.keyboardHandler.setClipboard(this.getSelected());
            this.deleteSelection();
            return true;
        } else {
            switch(param0) {
                case 257:
                case 335:
                    this.insertText("\n");
                    return true;
                case 259:
                    this.keyBackspace(var0);
                    return true;
                case 261:
                    this.keyDelete(var0);
                    return true;
                case 262:
                    this.keyRight(var0);
                    return true;
                case 263:
                    this.keyLeft(var0);
                    return true;
                case 264:
                    this.keyDown(var0);
                    return true;
                case 265:
                    this.keyUp(var0);
                    return true;
                case 266:
                    this.backButton.onPress();
                    return true;
                case 267:
                    this.forwardButton.onPress();
                    return true;
                case 268:
                    this.keyHome(var0);
                    return true;
                case 269:
                    this.keyEnd(var0);
                    return true;
                default:
                    return false;
            }
        }
    }

    private void keyBackspace(String param0) {
        if (!param0.isEmpty()) {
            if (this.selectionPos != this.cursorPos) {
                this.deleteSelection();
            } else if (this.cursorPos > 0) {
                String var0 = new StringBuilder(param0).deleteCharAt(Math.max(0, this.cursorPos - 1)).toString();
                this.setCurrentPageText(var0);
                this.cursorPos = Math.max(0, this.cursorPos - 1);
                this.selectionPos = this.cursorPos;
            }
        }

    }

    private void keyDelete(String param0) {
        if (!param0.isEmpty()) {
            if (this.selectionPos != this.cursorPos) {
                this.deleteSelection();
            } else if (this.cursorPos < param0.length()) {
                String var0 = new StringBuilder(param0).deleteCharAt(Math.max(0, this.cursorPos)).toString();
                this.setCurrentPageText(var0);
            }
        }

    }

    private void keyLeft(String param0) {
        int var0 = this.font.isBidirectional() ? 1 : -1;
        if (Screen.hasControlDown()) {
            this.cursorPos = this.font.getWordPosition(param0, var0, this.cursorPos, true);
        } else {
            this.cursorPos = Math.max(0, this.cursorPos + var0);
        }

        if (!Screen.hasShiftDown()) {
            this.selectionPos = this.cursorPos;
        }

    }

    private void keyRight(String param0) {
        int var0 = this.font.isBidirectional() ? -1 : 1;
        if (Screen.hasControlDown()) {
            this.cursorPos = this.font.getWordPosition(param0, var0, this.cursorPos, true);
        } else {
            this.cursorPos = Math.min(param0.length(), this.cursorPos + var0);
        }

        if (!Screen.hasShiftDown()) {
            this.selectionPos = this.cursorPos;
        }

    }

    private void keyUp(String param0) {
        if (!param0.isEmpty()) {
            BookEditScreen.Pos2i var0 = this.getPositionAtIndex(param0, this.cursorPos);
            if (var0.y == 0) {
                this.cursorPos = 0;
                if (!Screen.hasShiftDown()) {
                    this.selectionPos = this.cursorPos;
                }
            } else {
                int var1 = this.getIndexAtPosition(param0, new BookEditScreen.Pos2i(var0.x + this.getWidthAt(param0, this.cursorPos) / 3, var0.y - 9));
                if (var1 >= 0) {
                    this.cursorPos = var1;
                    if (!Screen.hasShiftDown()) {
                        this.selectionPos = this.cursorPos;
                    }
                }
            }
        }

    }

    private void keyDown(String param0) {
        if (!param0.isEmpty()) {
            BookEditScreen.Pos2i var0 = this.getPositionAtIndex(param0, this.cursorPos);
            int var1 = this.font.wordWrapHeight(param0 + "" + ChatFormatting.BLACK + "_", 114);
            if (var0.y + 9 == var1) {
                this.cursorPos = param0.length();
                if (!Screen.hasShiftDown()) {
                    this.selectionPos = this.cursorPos;
                }
            } else {
                int var2 = this.getIndexAtPosition(param0, new BookEditScreen.Pos2i(var0.x + this.getWidthAt(param0, this.cursorPos) / 3, var0.y + 9));
                if (var2 >= 0) {
                    this.cursorPos = var2;
                    if (!Screen.hasShiftDown()) {
                        this.selectionPos = this.cursorPos;
                    }
                }
            }
        }

    }

    private void keyHome(String param0) {
        this.cursorPos = this.getIndexAtPosition(param0, new BookEditScreen.Pos2i(0, this.getPositionAtIndex(param0, this.cursorPos).y));
        if (!Screen.hasShiftDown()) {
            this.selectionPos = this.cursorPos;
        }

    }

    private void keyEnd(String param0) {
        this.cursorPos = this.getIndexAtPosition(param0, new BookEditScreen.Pos2i(113, this.getPositionAtIndex(param0, this.cursorPos).y));
        if (!Screen.hasShiftDown()) {
            this.selectionPos = this.cursorPos;
        }

    }

    private void deleteSelection() {
        if (this.selectionPos != this.cursorPos) {
            String var0 = this.getCurrentPageText();
            int var1 = Math.min(this.cursorPos, this.selectionPos);
            int var2 = Math.max(this.cursorPos, this.selectionPos);
            String var3 = var0.substring(0, var1) + var0.substring(var2);
            this.cursorPos = var1;
            this.selectionPos = this.cursorPos;
            this.setCurrentPageText(var3);
        }
    }

    private int getWidthAt(String param0, int param1) {
        return (int)this.font.charWidth(param0.charAt(Mth.clamp(param1, 0, param0.length() - 1)));
    }

    private boolean titleKeyPressed(int param0, int param1, int param2) {
        switch(param0) {
            case 257:
            case 335:
                if (!this.title.isEmpty()) {
                    this.saveChanges(true);
                    this.minecraft.setScreen(null);
                }

                return true;
            case 259:
                if (!this.title.isEmpty()) {
                    this.title = this.title.substring(0, this.title.length() - 1);
                    this.updateButtonVisibility();
                }

                return true;
            default:
                return false;
        }
    }

    private String getCurrentPageText() {
        return this.currentPage >= 0 && this.currentPage < this.pages.size() ? this.pages.get(this.currentPage) : "";
    }

    private void setCurrentPageText(String param0) {
        if (this.currentPage >= 0 && this.currentPage < this.pages.size()) {
            this.pages.set(this.currentPage, param0);
            this.isModified = true;
        }

    }

    private void insertText(String param0) {
        if (this.selectionPos != this.cursorPos) {
            this.deleteSelection();
        }

        String var0 = this.getCurrentPageText();
        this.cursorPos = Mth.clamp(this.cursorPos, 0, var0.length());
        String var1 = new StringBuilder(var0).insert(this.cursorPos, param0).toString();
        int var2 = this.font.wordWrapHeight(var1 + "" + ChatFormatting.BLACK + "_", 114);
        if (var2 <= 128 && var1.length() < 1024) {
            this.setCurrentPageText(var1);
            this.selectionPos = this.cursorPos = Math.min(this.getCurrentPageText().length(), this.cursorPos + param0.length());
        }

    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.setFocused(null);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(BookViewScreen.BOOK_LOCATION);
        int var0 = (this.width - 192) / 2;
        int var1 = 2;
        this.blit(var0, 2, 0, 0, 192, 192);
        if (this.isSigning) {
            String var2 = this.title;
            if (this.frameTick / 6 % 2 == 0) {
                var2 = var2 + "" + ChatFormatting.BLACK + "_";
            } else {
                var2 = var2 + "" + ChatFormatting.GRAY + "_";
            }

            String var3 = I18n.get("book.editTitle");
            int var4 = this.strWidth(var3);
            this.font.draw(var3, (float)(var0 + 36 + (114 - var4) / 2), 34.0F, 0);
            int var5 = this.strWidth(var2);
            this.font.draw(var2, (float)(var0 + 36 + (114 - var5) / 2), 50.0F, 0);
            String var6 = I18n.get("book.byAuthor", this.owner.getName().getString());
            int var7 = this.strWidth(var6);
            this.font.draw(ChatFormatting.DARK_GRAY + var6, (float)(var0 + 36 + (114 - var7) / 2), 60.0F, 0);
            String var8 = I18n.get("book.finalizeWarning");
            this.font.drawWordWrap(var8, var0 + 36, 82, 114, 0);
        } else {
            String var9 = I18n.get("book.pageIndicator", this.currentPage + 1, this.getNumPages());
            String var10 = this.getCurrentPageText();
            int var11 = this.strWidth(var9);
            this.font.draw(var9, (float)(var0 - var11 + 192 - 44), 18.0F, 0);
            this.font.drawWordWrap(var10, var0 + 36, 32, 114, 0);
            this.renderSelection(var10);
            if (this.frameTick / 6 % 2 == 0) {
                BookEditScreen.Pos2i var12 = this.getPositionAtIndex(var10, this.cursorPos);
                if (this.font.isBidirectional()) {
                    this.handleBidi(var12);
                    var12.x = var12.x - 4;
                }

                this.convertLocalToScreen(var12);
                if (this.cursorPos < var10.length()) {
                    GuiComponent.fill(var12.x, var12.y - 1, var12.x + 1, var12.y + 9, -16777216);
                } else {
                    this.font.draw("_", (float)var12.x, (float)var12.y, 0);
                }
            }
        }

        super.render(param0, param1, param2);
    }

    private int strWidth(String param0) {
        return this.font.width(this.font.isBidirectional() ? this.font.bidirectionalShaping(param0) : param0);
    }

    private int strIndexAtWidth(String param0, int param1) {
        return this.font.indexAtWidth(param0, param1);
    }

    private String getSelected() {
        String var0 = this.getCurrentPageText();
        int var1 = Math.min(this.cursorPos, this.selectionPos);
        int var2 = Math.max(this.cursorPos, this.selectionPos);
        return var0.substring(var1, var2);
    }

    private void renderSelection(String param0) {
        if (this.selectionPos != this.cursorPos) {
            int var0 = Math.min(this.cursorPos, this.selectionPos);
            int var1 = Math.max(this.cursorPos, this.selectionPos);
            String var2 = param0.substring(var0, var1);
            int var3 = this.font.getWordPosition(param0, 1, var1, true);
            String var4 = param0.substring(var0, var3);
            BookEditScreen.Pos2i var5 = this.getPositionAtIndex(param0, var0);
            BookEditScreen.Pos2i var6 = new BookEditScreen.Pos2i(var5.x, var5.y + 9);

            while(!var2.isEmpty()) {
                int var7 = this.strIndexAtWidth(var4, 114 - var5.x);
                if (var2.length() <= var7) {
                    var6.x = var5.x + this.strWidth(var2);
                    this.renderHighlight(var5, var6);
                    break;
                }

                var7 = Math.min(var7, var2.length() - 1);
                String var8 = var2.substring(0, var7);
                char var9 = var2.charAt(var7);
                boolean var10 = var9 == ' ' || var9 == '\n';
                var2 = ChatFormatting.getLastColors(var8) + var2.substring(var7 + (var10 ? 1 : 0));
                var4 = ChatFormatting.getLastColors(var8) + var4.substring(var7 + (var10 ? 1 : 0));
                var6.x = var5.x + this.strWidth(var8 + " ");
                this.renderHighlight(var5, var6);
                var5.x = 0;
                var5.y = var5.y + 9;
                var6.y = var6.y + 9;
            }

        }
    }

    private void renderHighlight(BookEditScreen.Pos2i param0, BookEditScreen.Pos2i param1) {
        BookEditScreen.Pos2i var0 = new BookEditScreen.Pos2i(param0.x, param0.y);
        BookEditScreen.Pos2i var1 = new BookEditScreen.Pos2i(param1.x, param1.y);
        if (this.font.isBidirectional()) {
            this.handleBidi(var0);
            this.handleBidi(var1);
            int var2 = var1.x;
            var1.x = var0.x;
            var0.x = var2;
        }

        this.convertLocalToScreen(var0);
        this.convertLocalToScreen(var1);
        Tesselator var3 = Tesselator.getInstance();
        BufferBuilder var4 = var3.getBuilder();
        GlStateManager.color4f(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture();
        GlStateManager.enableColorLogicOp();
        GlStateManager.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        var4.begin(7, DefaultVertexFormat.POSITION);
        var4.vertex((double)var0.x, (double)var1.y, 0.0).endVertex();
        var4.vertex((double)var1.x, (double)var1.y, 0.0).endVertex();
        var4.vertex((double)var1.x, (double)var0.y, 0.0).endVertex();
        var4.vertex((double)var0.x, (double)var0.y, 0.0).endVertex();
        var3.end();
        GlStateManager.disableColorLogicOp();
        GlStateManager.enableTexture();
    }

    private BookEditScreen.Pos2i getPositionAtIndex(String param0, int param1) {
        BookEditScreen.Pos2i var0 = new BookEditScreen.Pos2i();
        int var1 = 0;
        int var2 = 0;

        for(String var3 = param0; !var3.isEmpty(); var2 = var1) {
            int var4 = this.strIndexAtWidth(var3, 114);
            if (var3.length() <= var4) {
                String var5 = var3.substring(0, Math.min(Math.max(param1 - var2, 0), var3.length()));
                var0.x = var0.x + this.strWidth(var5);
                break;
            }

            String var6 = var3.substring(0, var4);
            char var7 = var3.charAt(var4);
            boolean var8 = var7 == ' ' || var7 == '\n';
            var3 = ChatFormatting.getLastColors(var6) + var3.substring(var4 + (var8 ? 1 : 0));
            var1 += var6.length() + (var8 ? 1 : 0);
            if (var1 - 1 >= param1) {
                String var9 = var6.substring(0, Math.min(Math.max(param1 - var2, 0), var6.length()));
                var0.x = var0.x + this.strWidth(var9);
                break;
            }

            var0.y = var0.y + 9;
        }

        return var0;
    }

    private void handleBidi(BookEditScreen.Pos2i param0) {
        if (this.font.isBidirectional()) {
            param0.x = 114 - param0.x;
        }

    }

    private void convertScreenToLocal(BookEditScreen.Pos2i param0) {
        param0.x = param0.x - (this.width - 192) / 2 - 36;
        param0.y = param0.y - 32;
    }

    private void convertLocalToScreen(BookEditScreen.Pos2i param0) {
        param0.x = param0.x + (this.width - 192) / 2 + 36;
        param0.y = param0.y + 32;
    }

    private int indexInLine(String param0, int param1) {
        if (param1 < 0) {
            return 0;
        } else {
            float var0 = 0.0F;
            boolean var1 = false;
            String var2 = param0 + " ";

            for(int var3 = 0; var3 < var2.length(); ++var3) {
                char var4 = var2.charAt(var3);
                float var5 = this.font.charWidth(var4);
                if (var4 == 167 && var3 < var2.length() - 1) {
                    var4 = var2.charAt(++var3);
                    if (var4 == 'l' || var4 == 'L') {
                        var1 = true;
                    } else if (var4 == 'r' || var4 == 'R') {
                        var1 = false;
                    }

                    var5 = 0.0F;
                }

                float var6 = var0;
                var0 += var5;
                if (var1 && var5 > 0.0F) {
                    ++var0;
                }

                if ((float)param1 >= var6 && (float)param1 < var0) {
                    return var3;
                }
            }

            return (float)param1 >= var0 ? var2.length() - 1 : -1;
        }
    }

    private int getIndexAtPosition(String param0, BookEditScreen.Pos2i param1) {
        int var0 = 16 * 9;
        if (param1.y > var0) {
            return -1;
        } else {
            int var1 = Integer.MIN_VALUE;
            int var2 = 9;
            int var3 = 0;

            for(String var4 = param0; !var4.isEmpty() && var1 < var0; var2 += 9) {
                int var5 = this.strIndexAtWidth(var4, 114);
                if (var5 < var4.length()) {
                    String var6 = var4.substring(0, var5);
                    if (param1.y >= var1 && param1.y < var2) {
                        int var7 = this.indexInLine(var6, param1.x);
                        return var7 < 0 ? -1 : var3 + var7;
                    }

                    char var8 = var4.charAt(var5);
                    boolean var9 = var8 == ' ' || var8 == '\n';
                    var4 = ChatFormatting.getLastColors(var6) + var4.substring(var5 + (var9 ? 1 : 0));
                    var3 += var6.length() + (var9 ? 1 : 0);
                } else if (param1.y >= var1 && param1.y < var2) {
                    int var10 = this.indexInLine(var4, param1.x);
                    return var10 < 0 ? -1 : var3 + var10;
                }

                var1 = var2;
            }

            return param0.length();
        }
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (param2 == 0) {
            long var0 = Util.getMillis();
            String var1 = this.getCurrentPageText();
            if (!var1.isEmpty()) {
                BookEditScreen.Pos2i var2 = new BookEditScreen.Pos2i((int)param0, (int)param1);
                this.convertScreenToLocal(var2);
                this.handleBidi(var2);
                int var3 = this.getIndexAtPosition(var1, var2);
                if (var3 >= 0) {
                    if (var3 != this.lastIndex || var0 - this.lastClickTime >= 250L) {
                        this.cursorPos = var3;
                        if (!Screen.hasShiftDown()) {
                            this.selectionPos = this.cursorPos;
                        }
                    } else if (this.selectionPos == this.cursorPos) {
                        this.selectionPos = this.font.getWordPosition(var1, -1, var3, false);
                        this.cursorPos = this.font.getWordPosition(var1, 1, var3, false);
                    } else {
                        this.selectionPos = 0;
                        this.cursorPos = this.getCurrentPageText().length();
                    }
                }

                this.lastIndex = var3;
            }

            this.lastClickTime = var0;
        }

        return super.mouseClicked(param0, param1, param2);
    }

    @Override
    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        if (param2 == 0 && this.currentPage >= 0 && this.currentPage < this.pages.size()) {
            String var0 = this.pages.get(this.currentPage);
            BookEditScreen.Pos2i var1 = new BookEditScreen.Pos2i((int)param0, (int)param1);
            this.convertScreenToLocal(var1);
            this.handleBidi(var1);
            int var2 = this.getIndexAtPosition(var0, var1);
            if (var2 >= 0) {
                this.cursorPos = var2;
            }
        }

        return super.mouseDragged(param0, param1, param2, param3, param4);
    }

    @OnlyIn(Dist.CLIENT)
    class Pos2i {
        private int x;
        private int y;

        Pos2i() {
        }

        Pos2i(int param0, int param1) {
            this.x = param0;
            this.y = param1;
        }
    }
}
