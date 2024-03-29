package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RecipeBookPage {
    public static final int ITEMS_PER_PAGE = 20;
    private static final WidgetSprites PAGE_FORWARD_SPRITES = new WidgetSprites(
        new ResourceLocation("recipe_book/page_forward"), new ResourceLocation("recipe_book/page_forward_highlighted")
    );
    private static final WidgetSprites PAGE_BACKWARD_SPRITES = new WidgetSprites(
        new ResourceLocation("recipe_book/page_backward"), new ResourceLocation("recipe_book/page_backward_highlighted")
    );
    private final List<RecipeButton> buttons = Lists.newArrayListWithCapacity(20);
    @Nullable
    private RecipeButton hoveredButton;
    private final OverlayRecipeComponent overlay = new OverlayRecipeComponent();
    private Minecraft minecraft;
    private final List<RecipeShownListener> showListeners = Lists.newArrayList();
    private List<RecipeCollection> recipeCollections = ImmutableList.of();
    private StateSwitchingButton forwardButton;
    private StateSwitchingButton backButton;
    private int totalPages;
    private int currentPage;
    private RecipeBook recipeBook;
    @Nullable
    private RecipeHolder<?> lastClickedRecipe;
    @Nullable
    private RecipeCollection lastClickedRecipeCollection;

    public RecipeBookPage() {
        for(int var0 = 0; var0 < 20; ++var0) {
            this.buttons.add(new RecipeButton());
        }

    }

    public void init(Minecraft param0, int param1, int param2) {
        this.minecraft = param0;
        this.recipeBook = param0.player.getRecipeBook();

        for(int var0 = 0; var0 < this.buttons.size(); ++var0) {
            this.buttons.get(var0).setPosition(param1 + 11 + 25 * (var0 % 5), param2 + 31 + 25 * (var0 / 5));
        }

        this.forwardButton = new StateSwitchingButton(param1 + 93, param2 + 137, 12, 17, false);
        this.forwardButton.initTextureValues(PAGE_FORWARD_SPRITES);
        this.backButton = new StateSwitchingButton(param1 + 38, param2 + 137, 12, 17, true);
        this.backButton.initTextureValues(PAGE_BACKWARD_SPRITES);
    }

    public void addListener(RecipeBookComponent param0) {
        this.showListeners.remove(param0);
        this.showListeners.add(param0);
    }

    public void updateCollections(List<RecipeCollection> param0, boolean param1) {
        this.recipeCollections = param0;
        this.totalPages = (int)Math.ceil((double)param0.size() / 20.0);
        if (this.totalPages <= this.currentPage || param1) {
            this.currentPage = 0;
        }

        this.updateButtonsForPage();
    }

    private void updateButtonsForPage() {
        int var0 = 20 * this.currentPage;

        for(int var1 = 0; var1 < this.buttons.size(); ++var1) {
            RecipeButton var2 = this.buttons.get(var1);
            if (var0 + var1 < this.recipeCollections.size()) {
                RecipeCollection var3 = this.recipeCollections.get(var0 + var1);
                var2.init(var3, this);
                var2.visible = true;
            } else {
                var2.visible = false;
            }
        }

        this.updateArrowButtons();
    }

    private void updateArrowButtons() {
        this.forwardButton.visible = this.totalPages > 1 && this.currentPage < this.totalPages - 1;
        this.backButton.visible = this.totalPages > 1 && this.currentPage > 0;
    }

    public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, float param5) {
        if (this.totalPages > 1) {
            Component var0 = Component.translatable("gui.recipebook.page", this.currentPage + 1, this.totalPages);
            int var1 = this.minecraft.font.width(var0);
            param0.drawString(this.minecraft.font, var0, param1 - var1 / 2 + 73, param2 + 141, -1, false);
        }

        this.hoveredButton = null;

        for(RecipeButton var2 : this.buttons) {
            var2.render(param0, param3, param4, param5);
            if (var2.visible && var2.isHoveredOrFocused()) {
                this.hoveredButton = var2;
            }
        }

        this.backButton.render(param0, param3, param4, param5);
        this.forwardButton.render(param0, param3, param4, param5);
        this.overlay.render(param0, param3, param4, param5);
    }

    public void renderTooltip(GuiGraphics param0, int param1, int param2) {
        if (this.minecraft.screen != null && this.hoveredButton != null && !this.overlay.isVisible()) {
            param0.renderComponentTooltip(this.minecraft.font, this.hoveredButton.getTooltipText(), param1, param2);
        }

    }

    @Nullable
    public RecipeHolder<?> getLastClickedRecipe() {
        return this.lastClickedRecipe;
    }

    @Nullable
    public RecipeCollection getLastClickedRecipeCollection() {
        return this.lastClickedRecipeCollection;
    }

    public void setInvisible() {
        this.overlay.setVisible(false);
    }

    public boolean mouseClicked(double param0, double param1, int param2, int param3, int param4, int param5, int param6) {
        this.lastClickedRecipe = null;
        this.lastClickedRecipeCollection = null;
        if (this.overlay.isVisible()) {
            if (this.overlay.mouseClicked(param0, param1, param2)) {
                this.lastClickedRecipe = this.overlay.getLastRecipeClicked();
                this.lastClickedRecipeCollection = this.overlay.getRecipeCollection();
            } else {
                this.overlay.setVisible(false);
            }

            return true;
        } else if (this.forwardButton.mouseClicked(param0, param1, param2)) {
            ++this.currentPage;
            this.updateButtonsForPage();
            return true;
        } else if (this.backButton.mouseClicked(param0, param1, param2)) {
            --this.currentPage;
            this.updateButtonsForPage();
            return true;
        } else {
            for(RecipeButton var0 : this.buttons) {
                if (var0.mouseClicked(param0, param1, param2)) {
                    if (param2 == 0) {
                        this.lastClickedRecipe = var0.getRecipe();
                        this.lastClickedRecipeCollection = var0.getCollection();
                    } else if (param2 == 1 && !this.overlay.isVisible() && !var0.isOnlyOption()) {
                        this.overlay
                            .init(
                                this.minecraft,
                                var0.getCollection(),
                                var0.getX(),
                                var0.getY(),
                                param3 + param5 / 2,
                                param4 + 13 + param6 / 2,
                                (float)var0.getWidth()
                            );
                    }

                    return true;
                }
            }

            return false;
        }
    }

    public void recipesShown(List<RecipeHolder<?>> param0) {
        for(RecipeShownListener var0 : this.showListeners) {
            var0.recipesShown(param0);
        }

    }

    public Minecraft getMinecraft() {
        return this.minecraft;
    }

    public RecipeBook getRecipeBook() {
        return this.recipeBook;
    }

    protected void listButtons(Consumer<AbstractWidget> param0) {
        param0.accept(this.forwardButton);
        param0.accept(this.backButton);
        this.buttons.forEach(param0);
    }
}
