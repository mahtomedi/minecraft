package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.BookAccess;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BookViewScreen extends Screen {
    public static final ResourceLocation BOOK_LOCATION = new ResourceLocation("textures/gui/book.png");
    private BookAccess bookAccess;
    private int currentPage;
    private List<Component> cachedPageComponents = Collections.emptyList();
    private int cachedPage = -1;
    private PageButton forwardButton;
    private PageButton backButton;
    private final boolean playTurnSound;

    public BookViewScreen(BookAccess param0) {
        this(param0, true);
    }

    public BookViewScreen() {
        this(BookAccess.EMPTY_ACCESS, false);
    }

    private BookViewScreen(BookAccess param0, boolean param1) {
        super(NarratorChatListener.NO_TITLE);
        this.bookAccess = param0;
        this.playTurnSound = param1;
    }

    public void setBookAccess(BookAccess param0) {
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
        this.addButton(new Button(this.width / 2 - 100, 196, 200, 20, I18n.get("gui.done"), param0 -> this.minecraft.setScreen(null)));
    }

    protected void createPageControlButtons() {
        int var0 = (this.width - 192) / 2;
        int var1 = 2;
        this.forwardButton = this.addButton(new PageButton(var0 + 116, 159, true, param0 -> this.pageForward(), this.playTurnSound));
        this.backButton = this.addButton(new PageButton(var0 + 43, 159, false, param0 -> this.pageBack(), this.playTurnSound));
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
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(BOOK_LOCATION);
        int var0 = (this.width - 192) / 2;
        int var1 = 2;
        this.blit(var0, 2, 0, 0, 192, 192);
        String var2 = I18n.get("book.pageIndicator", this.currentPage + 1, Math.max(this.getNumPages(), 1));
        if (this.cachedPage != this.currentPage) {
            Component var3 = this.bookAccess.getPage(this.currentPage);
            this.cachedPageComponents = ComponentRenderUtils.wrapComponents(var3, 114, this.font, true, true);
        }

        this.cachedPage = this.currentPage;
        int var4 = this.strWidth(var2);
        this.font.draw(var2, (float)(var0 - var4 + 192 - 44), 18.0F, 0);
        int var5 = Math.min(128 / 9, this.cachedPageComponents.size());

        for(int var6 = 0; var6 < var5; ++var6) {
            Component var7 = this.cachedPageComponents.get(var6);
            this.font.draw(var7.getColoredString(), (float)(var0 + 36), (float)(32 + var6 * 9), 0);
        }

        Component var8 = this.getClickedComponentAt((double)param0, (double)param1);
        if (var8 != null) {
            this.renderComponentHoverEffect(var8, param0, param1);
        }

        super.render(param0, param1, param2);
    }

    private int strWidth(String param0) {
        return this.font.width(this.font.isBidirectional() ? this.font.bidirectionalShaping(param0) : param0);
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (param2 == 0) {
            Component var0 = this.getClickedComponentAt(param0, param1);
            if (var0 != null && this.handleComponentClicked(var0)) {
                return true;
            }
        }

        return super.mouseClicked(param0, param1, param2);
    }

    @Override
    public boolean handleComponentClicked(Component param0) {
        ClickEvent var0 = param0.getStyle().getClickEvent();
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
                this.minecraft.setScreen(null);
            }

            return var3;
        }
    }

    @Nullable
    public Component getClickedComponentAt(double param0, double param1) {
        if (this.cachedPageComponents == null) {
            return null;
        } else {
            int var0 = Mth.floor(param0 - (double)((this.width - 192) / 2) - 36.0);
            int var1 = Mth.floor(param1 - 2.0 - 30.0);
            if (var0 >= 0 && var1 >= 0) {
                int var2 = Math.min(128 / 9, this.cachedPageComponents.size());
                if (var0 <= 114 && var1 < 9 * var2 + var2) {
                    int var3 = var1 / 9;
                    if (var3 >= 0 && var3 < this.cachedPageComponents.size()) {
                        Component var4 = this.cachedPageComponents.get(var3);
                        int var5 = 0;

                        for(Component var6 : var4) {
                            if (var6 instanceof TextComponent) {
                                var5 += this.minecraft.font.width(var6.getColoredString());
                                if (var5 > var0) {
                                    return var6;
                                }
                            }
                        }
                    }

                    return null;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }
}
