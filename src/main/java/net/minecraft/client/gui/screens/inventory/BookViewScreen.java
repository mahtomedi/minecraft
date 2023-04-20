package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BookViewScreen extends Screen {
    public static final int PAGE_INDICATOR_TEXT_Y_OFFSET = 16;
    public static final int PAGE_TEXT_X_OFFSET = 36;
    public static final int PAGE_TEXT_Y_OFFSET = 30;
    public static final BookViewScreen.BookAccess EMPTY_ACCESS = new BookViewScreen.BookAccess() {
        @Override
        public int getPageCount() {
            return 0;
        }

        @Override
        public FormattedText getPageRaw(int param0) {
            return FormattedText.EMPTY;
        }
    };
    public static final ResourceLocation BOOK_LOCATION = new ResourceLocation("textures/gui/book.png");
    protected static final int TEXT_WIDTH = 114;
    protected static final int TEXT_HEIGHT = 128;
    protected static final int IMAGE_WIDTH = 192;
    protected static final int IMAGE_HEIGHT = 192;
    private BookViewScreen.BookAccess bookAccess;
    private int currentPage;
    private List<FormattedCharSequence> cachedPageComponents = Collections.emptyList();
    private int cachedPage = -1;
    private Component pageMsg = CommonComponents.EMPTY;
    private PageButton forwardButton;
    private PageButton backButton;
    private final boolean playTurnSound;

    public BookViewScreen(BookViewScreen.BookAccess param0) {
        this(param0, true);
    }

    public BookViewScreen() {
        this(EMPTY_ACCESS, false);
    }

    private BookViewScreen(BookViewScreen.BookAccess param0, boolean param1) {
        super(GameNarrator.NO_TITLE);
        this.bookAccess = param0;
        this.playTurnSound = param1;
    }

    public void setBookAccess(BookViewScreen.BookAccess param0) {
        this.bookAccess = param0;
        this.currentPage = Mth.clamp(this.currentPage, 0, param0.getPageCount());
        this.updateButtonVisibility();
        this.cachedPage = -1;
    }

    public boolean setPage(int param0) {
        int var0 = Mth.clamp(param0, 0, this.bookAccess.getPageCount() - 1);
        if (var0 != this.currentPage) {
            this.currentPage = var0;
            this.updateButtonVisibility();
            this.cachedPage = -1;
            return true;
        } else {
            return false;
        }
    }

    protected boolean forcePage(int param0) {
        return this.setPage(param0);
    }

    @Override
    protected void init() {
        this.createMenuControls();
        this.createPageControlButtons();
    }

    protected void createMenuControls() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, param0 -> this.onClose()).bounds(this.width / 2 - 100, 196, 200, 20).build());
    }

    protected void createPageControlButtons() {
        int var0 = (this.width - 192) / 2;
        int var1 = 2;
        this.forwardButton = this.addRenderableWidget(new PageButton(var0 + 116, 159, true, param0 -> this.pageForward(), this.playTurnSound));
        this.backButton = this.addRenderableWidget(new PageButton(var0 + 43, 159, false, param0 -> this.pageBack(), this.playTurnSound));
        this.updateButtonVisibility();
    }

    private int getNumPages() {
        return this.bookAccess.getPageCount();
    }

    protected void pageBack() {
        if (this.currentPage > 0) {
            --this.currentPage;
        }

        this.updateButtonVisibility();
    }

    protected void pageForward() {
        if (this.currentPage < this.getNumPages() - 1) {
            ++this.currentPage;
        }

        this.updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        this.forwardButton.visible = this.currentPage < this.getNumPages() - 1;
        this.backButton.visible = this.currentPage > 0;
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (super.keyPressed(param0, param1, param2)) {
            return true;
        } else {
            switch(param0) {
                case 266:
                    this.backButton.onPress();
                    return true;
                case 267:
                    this.forwardButton.onPress();
                    return true;
                default:
                    return false;
            }
        }
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        int var0 = (this.width - 192) / 2;
        int var1 = 2;
        param0.blit(BOOK_LOCATION, var0, 2, 0, 0, 192, 192);
        if (this.cachedPage != this.currentPage) {
            FormattedText var2 = this.bookAccess.getPage(this.currentPage);
            this.cachedPageComponents = this.font.split(var2, 114);
            this.pageMsg = Component.translatable("book.pageIndicator", this.currentPage + 1, Math.max(this.getNumPages(), 1));
        }

        this.cachedPage = this.currentPage;
        int var3 = this.font.width(this.pageMsg);
        param0.drawString(this.font, this.pageMsg, var0 - var3 + 192 - 44, 18, 0, false);
        int var4 = Math.min(128 / 9, this.cachedPageComponents.size());

        for(int var5 = 0; var5 < var4; ++var5) {
            FormattedCharSequence var6 = this.cachedPageComponents.get(var5);
            param0.drawString(this.font, var6, var0 + 36, 32 + var5 * 9, 0, false);
        }

        Style var7 = this.getClickedComponentStyleAt((double)param1, (double)param2);
        if (var7 != null) {
            param0.renderComponentHoverEffect(this.font, var7, param1, param2);
        }

        super.render(param0, param1, param2, param3);
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (param2 == 0) {
            Style var0 = this.getClickedComponentStyleAt(param0, param1);
            if (var0 != null && this.handleComponentClicked(var0)) {
                return true;
            }
        }

        return super.mouseClicked(param0, param1, param2);
    }

    @Override
    public boolean handleComponentClicked(Style param0) {
        ClickEvent var0 = param0.getClickEvent();
        if (var0 == null) {
            return false;
        } else if (var0.getAction() == ClickEvent.Action.CHANGE_PAGE) {
            String var1 = var0.getValue();

            try {
                int var2 = Integer.parseInt(var1) - 1;
                return this.forcePage(var2);
            } catch (Exception var5) {
                return false;
            }
        } else {
            boolean var3 = super.handleComponentClicked(param0);
            if (var3 && var0.getAction() == ClickEvent.Action.RUN_COMMAND) {
                this.closeScreen();
            }

            return var3;
        }
    }

    protected void closeScreen() {
        this.minecraft.setScreen(null);
    }

    @Nullable
    public Style getClickedComponentStyleAt(double param0, double param1) {
        if (this.cachedPageComponents.isEmpty()) {
            return null;
        } else {
            int var0 = Mth.floor(param0 - (double)((this.width - 192) / 2) - 36.0);
            int var1 = Mth.floor(param1 - 2.0 - 30.0);
            if (var0 >= 0 && var1 >= 0) {
                int var2 = Math.min(128 / 9, this.cachedPageComponents.size());
                if (var0 <= 114 && var1 < 9 * var2 + var2) {
                    int var3 = var1 / 9;
                    if (var3 >= 0 && var3 < this.cachedPageComponents.size()) {
                        FormattedCharSequence var4 = this.cachedPageComponents.get(var3);
                        return this.minecraft.font.getSplitter().componentStyleAtWidth(var4, var0);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    static List<String> loadPages(CompoundTag param0) {
        Builder<String> var0 = ImmutableList.builder();
        loadPages(param0, var0::add);
        return var0.build();
    }

    public static void loadPages(CompoundTag param0, Consumer<String> param1) {
        ListTag var0 = param0.getList("pages", 8).copy();
        IntFunction<String> var2;
        if (Minecraft.getInstance().isTextFilteringEnabled() && param0.contains("filtered_pages", 10)) {
            CompoundTag var1 = param0.getCompound("filtered_pages");
            var2 = param2 -> {
                String var0x = String.valueOf(param2);
                return var1.contains(var0x) ? var1.getString(var0x) : var0.getString(param2);
            };
        } else {
            var2 = var0::getString;
        }

        for(int var4 = 0; var4 < var0.size(); ++var4) {
            param1.accept(var2.apply(var4));
        }

    }

    @OnlyIn(Dist.CLIENT)
    public interface BookAccess {
        int getPageCount();

        FormattedText getPageRaw(int var1);

        default FormattedText getPage(int param0) {
            return param0 >= 0 && param0 < this.getPageCount() ? this.getPageRaw(param0) : FormattedText.EMPTY;
        }

        static BookViewScreen.BookAccess fromItem(ItemStack param0) {
            if (param0.is(Items.WRITTEN_BOOK)) {
                return new BookViewScreen.WrittenBookAccess(param0);
            } else {
                return (BookViewScreen.BookAccess)(param0.is(Items.WRITABLE_BOOK) ? new BookViewScreen.WritableBookAccess(param0) : BookViewScreen.EMPTY_ACCESS);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class WritableBookAccess implements BookViewScreen.BookAccess {
        private final List<String> pages;

        public WritableBookAccess(ItemStack param0) {
            this.pages = readPages(param0);
        }

        private static List<String> readPages(ItemStack param0) {
            CompoundTag var0 = param0.getTag();
            return (List<String>)(var0 != null ? BookViewScreen.loadPages(var0) : ImmutableList.of());
        }

        @Override
        public int getPageCount() {
            return this.pages.size();
        }

        @Override
        public FormattedText getPageRaw(int param0) {
            return FormattedText.of(this.pages.get(param0));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class WrittenBookAccess implements BookViewScreen.BookAccess {
        private final List<String> pages;

        public WrittenBookAccess(ItemStack param0) {
            this.pages = readPages(param0);
        }

        private static List<String> readPages(ItemStack param0) {
            CompoundTag var0 = param0.getTag();
            return (List<String>)(var0 != null && WrittenBookItem.makeSureTagIsValid(var0)
                ? BookViewScreen.loadPages(var0)
                : ImmutableList.of(Component.Serializer.toJson(Component.translatable("book.invalid.tag").withStyle(ChatFormatting.DARK_RED))));
        }

        @Override
        public int getPageCount() {
            return this.pages.size();
        }

        @Override
        public FormattedText getPageRaw(int param0) {
            String var0 = this.pages.get(param0);

            try {
                FormattedText var1 = Component.Serializer.fromJson(var0);
                if (var1 != null) {
                    return var1;
                }
            } catch (Exception var4) {
            }

            return FormattedText.of(var0);
        }
    }
}
