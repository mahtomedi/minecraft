package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

@OnlyIn(Dist.CLIENT)
public class BookEditScreen extends Screen {
    private static final int TEXT_WIDTH = 114;
    private static final int TEXT_HEIGHT = 128;
    private static final int IMAGE_WIDTH = 192;
    private static final int IMAGE_HEIGHT = 192;
    private static final Component EDIT_TITLE_LABEL = Component.translatable("book.editTitle");
    private static final Component FINALIZE_WARNING_LABEL = Component.translatable("book.finalizeWarning");
    private static final FormattedCharSequence BLACK_CURSOR = FormattedCharSequence.forward("_", Style.EMPTY.withColor(ChatFormatting.BLACK));
    private static final FormattedCharSequence GRAY_CURSOR = FormattedCharSequence.forward("_", Style.EMPTY.withColor(ChatFormatting.GRAY));
    private final Player owner;
    private final ItemStack book;
    private boolean isModified;
    private boolean isSigning;
    private int frameTick;
    private int currentPage;
    private final List<String> pages = Lists.newArrayList();
    private String title = "";
    private final TextFieldHelper pageEdit = new TextFieldHelper(
        this::getCurrentPageText,
        this::setCurrentPageText,
        this::getClipboard,
        this::setClipboard,
        param0x -> param0x.length() < 1024 && this.font.wordWrapHeight(param0x, 114) <= 128
    );
    private final TextFieldHelper titleEdit = new TextFieldHelper(
        () -> this.title, param0x -> this.title = param0x, this::getClipboard, this::setClipboard, param0x -> param0x.length() < 16
    );
    private long lastClickTime;
    private int lastIndex = -1;
    private PageButton forwardButton;
    private PageButton backButton;
    private Button doneButton;
    private Button signButton;
    private Button finalizeButton;
    private Button cancelButton;
    private final InteractionHand hand;
    @Nullable
    private BookEditScreen.DisplayCache displayCache = BookEditScreen.DisplayCache.EMPTY;
    private Component pageMsg = CommonComponents.EMPTY;
    private final Component ownerText;

    public BookEditScreen(Player param0, ItemStack param1, InteractionHand param2) {
        super(GameNarrator.NO_TITLE);
        this.owner = param0;
        this.book = param1;
        this.hand = param2;
        CompoundTag var0 = param1.getTag();
        if (var0 != null) {
            BookViewScreen.loadPages(var0, this.pages::add);
        }

        if (this.pages.isEmpty()) {
            this.pages.add("");
        }

        this.ownerText = Component.translatable("book.byAuthor", param0.getName()).withStyle(ChatFormatting.DARK_GRAY);
    }

    private void setClipboard(String param0x) {
        if (this.minecraft != null) {
            TextFieldHelper.setClipboardContents(this.minecraft, param0x);
        }

    }

    private String getClipboard() {
        return this.minecraft != null ? TextFieldHelper.getClipboardContents(this.minecraft) : "";
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
        this.clearDisplayCache();
        this.signButton = this.addRenderableWidget(Button.builder(Component.translatable("book.signButton"), param0 -> {
            this.isSigning = true;
            this.updateButtonVisibility();
        }).bounds(this.width / 2 - 100, 196, 98, 20).build());
        this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, param0 -> {
            this.minecraft.setScreen(null);
            this.saveChanges(false);
        }).bounds(this.width / 2 + 2, 196, 98, 20).build());
        this.finalizeButton = this.addRenderableWidget(Button.builder(Component.translatable("book.finalizeButton"), param0 -> {
            if (this.isSigning) {
                this.saveChanges(true);
                this.minecraft.setScreen(null);
            }

        }).bounds(this.width / 2 - 100, 196, 98, 20).build());
        this.cancelButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, param0 -> {
            if (this.isSigning) {
                this.isSigning = false;
            }

            this.updateButtonVisibility();
        }).bounds(this.width / 2 + 2, 196, 98, 20).build());
        int var0 = (this.width - 192) / 2;
        int var1 = 2;
        this.forwardButton = this.addRenderableWidget(new PageButton(var0 + 116, 159, true, param0 -> this.pageForward(), true));
        this.backButton = this.addRenderableWidget(new PageButton(var0 + 43, 159, false, param0 -> this.pageBack(), true));
        this.updateButtonVisibility();
    }

    private void pageBack() {
        if (this.currentPage > 0) {
            --this.currentPage;
        }

        this.updateButtonVisibility();
        this.clearDisplayCacheAfterPageChange();
    }

    private void pageForward() {
        if (this.currentPage < this.getNumPages() - 1) {
            ++this.currentPage;
        } else {
            this.appendPageToBook();
            if (this.currentPage < this.getNumPages() - 1) {
                ++this.currentPage;
            }
        }

        this.updateButtonVisibility();
        this.clearDisplayCacheAfterPageChange();
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
            this.updateLocalCopy(param0);
            int var0 = this.hand == InteractionHand.MAIN_HAND ? this.owner.getInventory().selected : 40;
            this.minecraft.getConnection().send(new ServerboundEditBookPacket(var0, this.pages, param0 ? Optional.of(this.title.trim()) : Optional.empty()));
        }
    }

    private void updateLocalCopy(boolean param0) {
        ListTag var0 = new ListTag();
        this.pages.stream().map(StringTag::valueOf).forEach(var0::add);
        if (!this.pages.isEmpty()) {
            this.book.addTagElement("pages", var0);
        }

        if (param0) {
            this.book.addTagElement("author", StringTag.valueOf(this.owner.getGameProfile().getName()));
            this.book.addTagElement("title", StringTag.valueOf(this.title.trim()));
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
        } else if (this.isSigning) {
            return this.titleKeyPressed(param0, param1, param2);
        } else {
            boolean var0 = this.bookKeyPressed(param0, param1, param2);
            if (var0) {
                this.clearDisplayCache();
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean charTyped(char param0, int param1) {
        if (super.charTyped(param0, param1)) {
            return true;
        } else if (this.isSigning) {
            boolean var0 = this.titleEdit.charTyped(param0);
            if (var0) {
                this.updateButtonVisibility();
                this.isModified = true;
                return true;
            } else {
                return false;
            }
        } else if (SharedConstants.isAllowedChatCharacter(param0)) {
            this.pageEdit.insertText(Character.toString(param0));
            this.clearDisplayCache();
            return true;
        } else {
            return false;
        }
    }

    private boolean bookKeyPressed(int param0, int param1, int param2) {
        if (Screen.isSelectAll(param0)) {
            this.pageEdit.selectAll();
            return true;
        } else if (Screen.isCopy(param0)) {
            this.pageEdit.copy();
            return true;
        } else if (Screen.isPaste(param0)) {
            this.pageEdit.paste();
            return true;
        } else if (Screen.isCut(param0)) {
            this.pageEdit.cut();
            return true;
        } else {
            TextFieldHelper.CursorStep var0 = Screen.hasControlDown() ? TextFieldHelper.CursorStep.WORD : TextFieldHelper.CursorStep.CHARACTER;
            switch(param0) {
                case 257:
                case 335:
                    this.pageEdit.insertText("\n");
                    return true;
                case 259:
                    this.pageEdit.removeFromCursor(-1, var0);
                    return true;
                case 261:
                    this.pageEdit.removeFromCursor(1, var0);
                    return true;
                case 262:
                    this.pageEdit.moveBy(1, Screen.hasShiftDown(), var0);
                    return true;
                case 263:
                    this.pageEdit.moveBy(-1, Screen.hasShiftDown(), var0);
                    return true;
                case 264:
                    this.keyDown();
                    return true;
                case 265:
                    this.keyUp();
                    return true;
                case 266:
                    this.backButton.onPress();
                    return true;
                case 267:
                    this.forwardButton.onPress();
                    return true;
                case 268:
                    this.keyHome();
                    return true;
                case 269:
                    this.keyEnd();
                    return true;
                default:
                    return false;
            }
        }
    }

    private void keyUp() {
        this.changeLine(-1);
    }

    private void keyDown() {
        this.changeLine(1);
    }

    private void changeLine(int param0) {
        int var0 = this.pageEdit.getCursorPos();
        int var1 = this.getDisplayCache().changeLine(var0, param0);
        this.pageEdit.setCursorPos(var1, Screen.hasShiftDown());
    }

    private void keyHome() {
        if (Screen.hasControlDown()) {
            this.pageEdit.setCursorToStart(Screen.hasShiftDown());
        } else {
            int var0 = this.pageEdit.getCursorPos();
            int var1 = this.getDisplayCache().findLineStart(var0);
            this.pageEdit.setCursorPos(var1, Screen.hasShiftDown());
        }

    }

    private void keyEnd() {
        if (Screen.hasControlDown()) {
            this.pageEdit.setCursorToEnd(Screen.hasShiftDown());
        } else {
            BookEditScreen.DisplayCache var0 = this.getDisplayCache();
            int var1 = this.pageEdit.getCursorPos();
            int var2 = var0.findLineEnd(var1);
            this.pageEdit.setCursorPos(var2, Screen.hasShiftDown());
        }

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
                this.titleEdit.removeCharsFromCursor(-1);
                this.updateButtonVisibility();
                this.isModified = true;
                return true;
            default:
                return false;
        }
    }

    private String getCurrentPageText() {
        return this.currentPage >= 0 && this.currentPage < this.pages.size() ? this.pages.get(this.currentPage) : "";
    }

    private void setCurrentPageText(String param0x) {
        if (this.currentPage >= 0 && this.currentPage < this.pages.size()) {
            this.pages.set(this.currentPage, param0x);
            this.isModified = true;
            this.clearDisplayCache();
        }

    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.setFocused(null);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BookViewScreen.BOOK_LOCATION);
        int var0 = (this.width - 192) / 2;
        int var1 = 2;
        this.blit(param0, var0, 2, 0, 0, 192, 192);
        if (this.isSigning) {
            boolean var2 = this.frameTick / 6 % 2 == 0;
            FormattedCharSequence var3 = FormattedCharSequence.composite(
                FormattedCharSequence.forward(this.title, Style.EMPTY), var2 ? BLACK_CURSOR : GRAY_CURSOR
            );
            int var4 = this.font.width(EDIT_TITLE_LABEL);
            this.font.draw(param0, EDIT_TITLE_LABEL, (float)(var0 + 36 + (114 - var4) / 2), 34.0F, 0);
            int var5 = this.font.width(var3);
            this.font.draw(param0, var3, (float)(var0 + 36 + (114 - var5) / 2), 50.0F, 0);
            int var6 = this.font.width(this.ownerText);
            this.font.draw(param0, this.ownerText, (float)(var0 + 36 + (114 - var6) / 2), 60.0F, 0);
            this.font.drawWordWrap(FINALIZE_WARNING_LABEL, var0 + 36, 82, 114, 0);
        } else {
            int var7 = this.font.width(this.pageMsg);
            this.font.draw(param0, this.pageMsg, (float)(var0 - var7 + 192 - 44), 18.0F, 0);
            BookEditScreen.DisplayCache var8 = this.getDisplayCache();

            for(BookEditScreen.LineInfo var9 : var8.lines) {
                this.font.draw(param0, var9.asComponent, (float)var9.x, (float)var9.y, -16777216);
            }

            this.renderHighlight(var8.selection);
            this.renderCursor(param0, var8.cursor, var8.cursorAtEnd);
        }

        super.render(param0, param1, param2, param3);
    }

    private void renderCursor(PoseStack param0, BookEditScreen.Pos2i param1, boolean param2) {
        if (this.frameTick / 6 % 2 == 0) {
            param1 = this.convertLocalToScreen(param1);
            if (!param2) {
                GuiComponent.fill(param0, param1.x, param1.y - 1, param1.x + 1, param1.y + 9, -16777216);
            } else {
                this.font.draw(param0, "_", (float)param1.x, (float)param1.y, 0);
            }
        }

    }

    private void renderHighlight(Rect2i[] param0) {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 255.0F, 255.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        var1.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        for(Rect2i var2 : param0) {
            int var3 = var2.getX();
            int var4 = var2.getY();
            int var5 = var3 + var2.getWidth();
            int var6 = var4 + var2.getHeight();
            var1.vertex((double)var3, (double)var6, 0.0).endVertex();
            var1.vertex((double)var5, (double)var6, 0.0).endVertex();
            var1.vertex((double)var5, (double)var4, 0.0).endVertex();
            var1.vertex((double)var3, (double)var4, 0.0).endVertex();
        }

        var0.end();
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }

    private BookEditScreen.Pos2i convertScreenToLocal(BookEditScreen.Pos2i param0) {
        return new BookEditScreen.Pos2i(param0.x - (this.width - 192) / 2 - 36, param0.y - 32);
    }

    private BookEditScreen.Pos2i convertLocalToScreen(BookEditScreen.Pos2i param0) {
        return new BookEditScreen.Pos2i(param0.x + (this.width - 192) / 2 + 36, param0.y + 32);
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (super.mouseClicked(param0, param1, param2)) {
            return true;
        } else {
            if (param2 == 0) {
                long var0 = Util.getMillis();
                BookEditScreen.DisplayCache var1 = this.getDisplayCache();
                int var2 = var1.getIndexAtPosition(this.font, this.convertScreenToLocal(new BookEditScreen.Pos2i((int)param0, (int)param1)));
                if (var2 >= 0) {
                    if (var2 != this.lastIndex || var0 - this.lastClickTime >= 250L) {
                        this.pageEdit.setCursorPos(var2, Screen.hasShiftDown());
                    } else if (!this.pageEdit.isSelecting()) {
                        this.selectWord(var2);
                    } else {
                        this.pageEdit.selectAll();
                    }

                    this.clearDisplayCache();
                }

                this.lastIndex = var2;
                this.lastClickTime = var0;
            }

            return true;
        }
    }

    private void selectWord(int param0) {
        String var0 = this.getCurrentPageText();
        this.pageEdit.setSelectionRange(StringSplitter.getWordPosition(var0, -1, param0, false), StringSplitter.getWordPosition(var0, 1, param0, false));
    }

    @Override
    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        if (super.mouseDragged(param0, param1, param2, param3, param4)) {
            return true;
        } else {
            if (param2 == 0) {
                BookEditScreen.DisplayCache var0 = this.getDisplayCache();
                int var1 = var0.getIndexAtPosition(this.font, this.convertScreenToLocal(new BookEditScreen.Pos2i((int)param0, (int)param1)));
                this.pageEdit.setCursorPos(var1, true);
                this.clearDisplayCache();
            }

            return true;
        }
    }

    private BookEditScreen.DisplayCache getDisplayCache() {
        if (this.displayCache == null) {
            this.displayCache = this.rebuildDisplayCache();
            this.pageMsg = Component.translatable("book.pageIndicator", this.currentPage + 1, this.getNumPages());
        }

        return this.displayCache;
    }

    private void clearDisplayCache() {
        this.displayCache = null;
    }

    private void clearDisplayCacheAfterPageChange() {
        this.pageEdit.setCursorToEnd();
        this.clearDisplayCache();
    }

    private BookEditScreen.DisplayCache rebuildDisplayCache() {
        String var0 = this.getCurrentPageText();
        if (var0.isEmpty()) {
            return BookEditScreen.DisplayCache.EMPTY;
        } else {
            int var1 = this.pageEdit.getCursorPos();
            int var2 = this.pageEdit.getSelectionPos();
            IntList var3 = new IntArrayList();
            List<BookEditScreen.LineInfo> var4 = Lists.newArrayList();
            MutableInt var5 = new MutableInt();
            MutableBoolean var6 = new MutableBoolean();
            StringSplitter var7 = this.font.getSplitter();
            var7.splitLines(var0, 114, Style.EMPTY, true, (param5, param6, param7) -> {
                int var0x = var5.getAndIncrement();
                String var1x = var0.substring(param6, param7);
                var6.setValue(var1x.endsWith("\n"));
                String var2x = StringUtils.stripEnd(var1x, " \n");
                int var3x = var0x * 9;
                BookEditScreen.Pos2i var4x = this.convertLocalToScreen(new BookEditScreen.Pos2i(0, var3x));
                var3.add(param6);
                var4.add(new BookEditScreen.LineInfo(param5, var2x, var4x.x, var4x.y));
            });
            int[] var8 = var3.toIntArray();
            boolean var9 = var1 == var0.length();
            BookEditScreen.Pos2i var10;
            if (var9 && var6.isTrue()) {
                var10 = new BookEditScreen.Pos2i(0, var4.size() * 9);
            } else {
                int var11 = findLineFromPos(var8, var1);
                int var12 = this.font.width(var0.substring(var8[var11], var1));
                var10 = new BookEditScreen.Pos2i(var12, var11 * 9);
            }

            List<Rect2i> var14 = Lists.newArrayList();
            if (var1 != var2) {
                int var15 = Math.min(var1, var2);
                int var16 = Math.max(var1, var2);
                int var17 = findLineFromPos(var8, var15);
                int var18 = findLineFromPos(var8, var16);
                if (var17 == var18) {
                    int var19 = var17 * 9;
                    int var20 = var8[var17];
                    var14.add(this.createPartialLineSelection(var0, var7, var15, var16, var19, var20));
                } else {
                    int var21 = var17 + 1 > var8.length ? var0.length() : var8[var17 + 1];
                    var14.add(this.createPartialLineSelection(var0, var7, var15, var21, var17 * 9, var8[var17]));

                    for(int var22 = var17 + 1; var22 < var18; ++var22) {
                        int var23 = var22 * 9;
                        String var24 = var0.substring(var8[var22], var8[var22 + 1]);
                        int var25 = (int)var7.stringWidth(var24);
                        var14.add(this.createSelection(new BookEditScreen.Pos2i(0, var23), new BookEditScreen.Pos2i(var25, var23 + 9)));
                    }

                    var14.add(this.createPartialLineSelection(var0, var7, var8[var18], var16, var18 * 9, var8[var18]));
                }
            }

            return new BookEditScreen.DisplayCache(var0, var10, var9, var8, var4.toArray(new BookEditScreen.LineInfo[0]), var14.toArray(new Rect2i[0]));
        }
    }

    static int findLineFromPos(int[] param0, int param1) {
        int var0 = Arrays.binarySearch(param0, param1);
        return var0 < 0 ? -(var0 + 2) : var0;
    }

    private Rect2i createPartialLineSelection(String param0, StringSplitter param1, int param2, int param3, int param4, int param5) {
        String var0 = param0.substring(param5, param2);
        String var1 = param0.substring(param5, param3);
        BookEditScreen.Pos2i var2 = new BookEditScreen.Pos2i((int)param1.stringWidth(var0), param4);
        BookEditScreen.Pos2i var3 = new BookEditScreen.Pos2i((int)param1.stringWidth(var1), param4 + 9);
        return this.createSelection(var2, var3);
    }

    private Rect2i createSelection(BookEditScreen.Pos2i param0, BookEditScreen.Pos2i param1) {
        BookEditScreen.Pos2i var0 = this.convertLocalToScreen(param0);
        BookEditScreen.Pos2i var1 = this.convertLocalToScreen(param1);
        int var2 = Math.min(var0.x, var1.x);
        int var3 = Math.max(var0.x, var1.x);
        int var4 = Math.min(var0.y, var1.y);
        int var5 = Math.max(var0.y, var1.y);
        return new Rect2i(var2, var4, var3 - var2, var5 - var4);
    }

    @OnlyIn(Dist.CLIENT)
    static class DisplayCache {
        static final BookEditScreen.DisplayCache EMPTY = new BookEditScreen.DisplayCache(
            "",
            new BookEditScreen.Pos2i(0, 0),
            true,
            new int[]{0},
            new BookEditScreen.LineInfo[]{new BookEditScreen.LineInfo(Style.EMPTY, "", 0, 0)},
            new Rect2i[0]
        );
        private final String fullText;
        final BookEditScreen.Pos2i cursor;
        final boolean cursorAtEnd;
        private final int[] lineStarts;
        final BookEditScreen.LineInfo[] lines;
        final Rect2i[] selection;

        public DisplayCache(String param0, BookEditScreen.Pos2i param1, boolean param2, int[] param3, BookEditScreen.LineInfo[] param4, Rect2i[] param5) {
            this.fullText = param0;
            this.cursor = param1;
            this.cursorAtEnd = param2;
            this.lineStarts = param3;
            this.lines = param4;
            this.selection = param5;
        }

        public int getIndexAtPosition(Font param0, BookEditScreen.Pos2i param1) {
            int var0 = param1.y / 9;
            if (var0 < 0) {
                return 0;
            } else if (var0 >= this.lines.length) {
                return this.fullText.length();
            } else {
                BookEditScreen.LineInfo var1 = this.lines[var0];
                return this.lineStarts[var0] + param0.getSplitter().plainIndexAtWidth(var1.contents, param1.x, var1.style);
            }
        }

        public int changeLine(int param0, int param1) {
            int var0 = BookEditScreen.findLineFromPos(this.lineStarts, param0);
            int var1 = var0 + param1;
            int var4;
            if (0 <= var1 && var1 < this.lineStarts.length) {
                int var2 = param0 - this.lineStarts[var0];
                int var3 = this.lines[var1].contents.length();
                var4 = this.lineStarts[var1] + Math.min(var2, var3);
            } else {
                var4 = param0;
            }

            return var4;
        }

        public int findLineStart(int param0) {
            int var0 = BookEditScreen.findLineFromPos(this.lineStarts, param0);
            return this.lineStarts[var0];
        }

        public int findLineEnd(int param0) {
            int var0 = BookEditScreen.findLineFromPos(this.lineStarts, param0);
            return this.lineStarts[var0] + this.lines[var0].contents.length();
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class LineInfo {
        final Style style;
        final String contents;
        final Component asComponent;
        final int x;
        final int y;

        public LineInfo(Style param0, String param1, int param2, int param3) {
            this.style = param0;
            this.contents = param1;
            this.x = param2;
            this.y = param3;
            this.asComponent = Component.literal(param1).setStyle(param0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class Pos2i {
        public final int x;
        public final int y;

        Pos2i(int param0, int param1) {
            this.x = param0;
            this.y = param1;
        }
    }
}
