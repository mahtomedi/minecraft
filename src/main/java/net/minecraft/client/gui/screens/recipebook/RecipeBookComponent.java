package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.recipebook.PlaceRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RecipeBookComponent implements PlaceRecipe<Ingredient>, Renderable, GuiEventListener, NarratableEntry, RecipeShownListener {
    public static final WidgetSprites RECIPE_BUTTON_SPRITES = new WidgetSprites(
        new ResourceLocation("recipe_book/button"), new ResourceLocation("recipe_book/button_highlighted")
    );
    private static final WidgetSprites FILTER_BUTTON_SPRITES = new WidgetSprites(
        new ResourceLocation("recipe_book/filter_enabled"),
        new ResourceLocation("recipe_book/filter_disabled"),
        new ResourceLocation("recipe_book/filter_enabled_highlighted"),
        new ResourceLocation("recipe_book/filter_disabled_highlighted")
    );
    protected static final ResourceLocation RECIPE_BOOK_LOCATION = new ResourceLocation("textures/gui/recipe_book.png");
    private static final Component SEARCH_HINT = Component.translatable("gui.recipebook.search_hint")
        .withStyle(ChatFormatting.ITALIC)
        .withStyle(ChatFormatting.GRAY);
    public static final int IMAGE_WIDTH = 147;
    public static final int IMAGE_HEIGHT = 166;
    private static final int OFFSET_X_POSITION = 86;
    private static final Component ONLY_CRAFTABLES_TOOLTIP = Component.translatable("gui.recipebook.toggleRecipes.craftable");
    private static final Component ALL_RECIPES_TOOLTIP = Component.translatable("gui.recipebook.toggleRecipes.all");
    private int xOffset;
    private int width;
    private int height;
    protected final GhostRecipe ghostRecipe = new GhostRecipe();
    private final List<RecipeBookTabButton> tabButtons = Lists.newArrayList();
    @Nullable
    private RecipeBookTabButton selectedTab;
    protected StateSwitchingButton filterButton;
    protected RecipeBookMenu<?> menu;
    protected Minecraft minecraft;
    @Nullable
    private EditBox searchBox;
    private String lastSearch = "";
    private ClientRecipeBook book;
    private final RecipeBookPage recipeBookPage = new RecipeBookPage();
    private final StackedContents stackedContents = new StackedContents();
    private int timesInventoryChanged;
    private boolean ignoreTextInput;
    private boolean visible;
    private boolean widthTooNarrow;

    public void init(int param0, int param1, Minecraft param2, boolean param3, RecipeBookMenu<?> param4) {
        this.minecraft = param2;
        this.width = param0;
        this.height = param1;
        this.menu = param4;
        this.widthTooNarrow = param3;
        param2.player.containerMenu = param4;
        this.book = param2.player.getRecipeBook();
        this.timesInventoryChanged = param2.player.getInventory().getTimesChanged();
        this.visible = this.isVisibleAccordingToBookData();
        if (this.visible) {
            this.initVisuals();
        }

    }

    public void initVisuals() {
        this.xOffset = this.widthTooNarrow ? 0 : 86;
        int var0 = (this.width - 147) / 2 - this.xOffset;
        int var1 = (this.height - 166) / 2;
        this.stackedContents.clear();
        this.minecraft.player.getInventory().fillStackedContents(this.stackedContents);
        this.menu.fillCraftSlotsStackedContents(this.stackedContents);
        String var2 = this.searchBox != null ? this.searchBox.getValue() : "";
        this.searchBox = new EditBox(this.minecraft.font, var0 + 25, var1 + 13, 81, 9 + 5, Component.translatable("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setVisible(true);
        this.searchBox.setTextColor(16777215);
        this.searchBox.setValue(var2);
        this.searchBox.setHint(SEARCH_HINT);
        this.recipeBookPage.init(this.minecraft, var0, var1);
        this.recipeBookPage.addListener(this);
        this.filterButton = new StateSwitchingButton(var0 + 110, var1 + 12, 26, 16, this.book.isFiltering(this.menu));
        this.updateFilterButtonTooltip();
        this.initFilterButtonTextures();
        this.tabButtons.clear();

        for(RecipeBookCategories var3 : RecipeBookCategories.getCategories(this.menu.getRecipeBookType())) {
            this.tabButtons.add(new RecipeBookTabButton(var3));
        }

        if (this.selectedTab != null) {
            this.selectedTab = this.tabButtons.stream().filter(param0 -> param0.getCategory().equals(this.selectedTab.getCategory())).findFirst().orElse(null);
        }

        if (this.selectedTab == null) {
            this.selectedTab = this.tabButtons.get(0);
        }

        this.selectedTab.setStateTriggered(true);
        this.updateCollections(false);
        this.updateTabs();
    }

    private void updateFilterButtonTooltip() {
        this.filterButton.setTooltip(this.filterButton.isStateTriggered() ? Tooltip.create(this.getRecipeFilterName()) : Tooltip.create(ALL_RECIPES_TOOLTIP));
    }

    protected void initFilterButtonTextures() {
        this.filterButton.initTextureValues(FILTER_BUTTON_SPRITES);
    }

    public int updateScreenPosition(int param0, int param1) {
        int var0;
        if (this.isVisible() && !this.widthTooNarrow) {
            var0 = 177 + (param0 - param1 - 200) / 2;
        } else {
            var0 = (param0 - param1) / 2;
        }

        return var0;
    }

    public void toggleVisibility() {
        this.setVisible(!this.isVisible());
    }

    public boolean isVisible() {
        return this.visible;
    }

    private boolean isVisibleAccordingToBookData() {
        return this.book.isOpen(this.menu.getRecipeBookType());
    }

    protected void setVisible(boolean param0) {
        if (param0) {
            this.initVisuals();
        }

        this.visible = param0;
        this.book.setOpen(this.menu.getRecipeBookType(), param0);
        if (!param0) {
            this.recipeBookPage.setInvisible();
        }

        this.sendUpdateSettings();
    }

    public void slotClicked(@Nullable Slot param0) {
        if (param0 != null && param0.index < this.menu.getSize()) {
            this.ghostRecipe.clear();
            if (this.isVisible()) {
                this.updateStackedContents();
            }
        }

    }

    private void updateCollections(boolean param0) {
        List<RecipeCollection> var0 = this.book.getCollection(this.selectedTab.getCategory());
        var0.forEach(param0x -> param0x.canCraft(this.stackedContents, this.menu.getGridWidth(), this.menu.getGridHeight(), this.book));
        List<RecipeCollection> var1 = Lists.newArrayList(var0);
        var1.removeIf(param0x -> !param0x.hasKnownRecipes());
        var1.removeIf(param0x -> !param0x.hasFitting());
        String var2 = this.searchBox.getValue();
        if (!var2.isEmpty()) {
            ObjectSet<RecipeCollection> var3 = new ObjectLinkedOpenHashSet<>(
                this.minecraft.getSearchTree(SearchRegistry.RECIPE_COLLECTIONS).search(var2.toLowerCase(Locale.ROOT))
            );
            var1.removeIf(param1 -> !var3.contains(param1));
        }

        if (this.book.isFiltering(this.menu)) {
            var1.removeIf(param0x -> !param0x.hasCraftable());
        }

        this.recipeBookPage.updateCollections(var1, param0);
    }

    private void updateTabs() {
        int var0 = (this.width - 147) / 2 - this.xOffset - 30;
        int var1 = (this.height - 166) / 2 + 3;
        int var2 = 27;
        int var3 = 0;

        for(RecipeBookTabButton var4 : this.tabButtons) {
            RecipeBookCategories var5 = var4.getCategory();
            if (var5 == RecipeBookCategories.CRAFTING_SEARCH || var5 == RecipeBookCategories.FURNACE_SEARCH) {
                var4.visible = true;
                var4.setPosition(var0, var1 + 27 * var3++);
            } else if (var4.updateVisibility(this.book)) {
                var4.setPosition(var0, var1 + 27 * var3++);
                var4.startAnimation(this.minecraft);
            }
        }

    }

    public void tick() {
        boolean var0 = this.isVisibleAccordingToBookData();
        if (this.isVisible() != var0) {
            this.setVisible(var0);
        }

        if (this.isVisible()) {
            if (this.timesInventoryChanged != this.minecraft.player.getInventory().getTimesChanged()) {
                this.updateStackedContents();
                this.timesInventoryChanged = this.minecraft.player.getInventory().getTimesChanged();
            }

        }
    }

    private void updateStackedContents() {
        this.stackedContents.clear();
        this.minecraft.player.getInventory().fillStackedContents(this.stackedContents);
        this.menu.fillCraftSlotsStackedContents(this.stackedContents);
        this.updateCollections(false);
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        if (this.isVisible()) {
            param0.pose().pushPose();
            param0.pose().translate(0.0F, 0.0F, 100.0F);
            int var0 = (this.width - 147) / 2 - this.xOffset;
            int var1 = (this.height - 166) / 2;
            param0.blit(RECIPE_BOOK_LOCATION, var0, var1, 1, 1, 147, 166);
            this.searchBox.render(param0, param1, param2, param3);

            for(RecipeBookTabButton var2 : this.tabButtons) {
                var2.render(param0, param1, param2, param3);
            }

            this.filterButton.render(param0, param1, param2, param3);
            this.recipeBookPage.render(param0, var0, var1, param1, param2, param3);
            param0.pose().popPose();
        }
    }

    public void renderTooltip(GuiGraphics param0, int param1, int param2, int param3, int param4) {
        if (this.isVisible()) {
            this.recipeBookPage.renderTooltip(param0, param3, param4);
            this.renderGhostRecipeTooltip(param0, param1, param2, param3, param4);
        }
    }

    protected Component getRecipeFilterName() {
        return ONLY_CRAFTABLES_TOOLTIP;
    }

    private void renderGhostRecipeTooltip(GuiGraphics param0, int param1, int param2, int param3, int param4) {
        ItemStack var0 = null;

        for(int var1 = 0; var1 < this.ghostRecipe.size(); ++var1) {
            GhostRecipe.GhostIngredient var2 = this.ghostRecipe.get(var1);
            int var3 = var2.getX() + param1;
            int var4 = var2.getY() + param2;
            if (param3 >= var3 && param4 >= var4 && param3 < var3 + 16 && param4 < var4 + 16) {
                var0 = var2.getItem();
            }
        }

        if (var0 != null && this.minecraft.screen != null) {
            param0.renderComponentTooltip(this.minecraft.font, Screen.getTooltipFromItem(this.minecraft, var0), param3, param4);
        }

    }

    public void renderGhostRecipe(GuiGraphics param0, int param1, int param2, boolean param3, float param4) {
        this.ghostRecipe.render(param0, this.minecraft, param1, param2, param3, param4);
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (this.isVisible() && !this.minecraft.player.isSpectator()) {
            if (this.recipeBookPage.mouseClicked(param0, param1, param2, (this.width - 147) / 2 - this.xOffset, (this.height - 166) / 2, 147, 166)) {
                Recipe<?> var0 = this.recipeBookPage.getLastClickedRecipe();
                RecipeCollection var1 = this.recipeBookPage.getLastClickedRecipeCollection();
                if (var0 != null && var1 != null) {
                    if (!var1.isCraftable(var0) && this.ghostRecipe.getRecipe() == var0) {
                        return false;
                    }

                    this.ghostRecipe.clear();
                    this.minecraft.gameMode.handlePlaceRecipe(this.minecraft.player.containerMenu.containerId, var0, Screen.hasShiftDown());
                    if (!this.isOffsetNextToMainGUI()) {
                        this.setVisible(false);
                    }
                }

                return true;
            } else if (this.searchBox.mouseClicked(param0, param1, param2)) {
                this.searchBox.setFocused(true);
                return true;
            } else {
                this.searchBox.setFocused(false);
                if (this.filterButton.mouseClicked(param0, param1, param2)) {
                    boolean var2 = this.toggleFiltering();
                    this.filterButton.setStateTriggered(var2);
                    this.updateFilterButtonTooltip();
                    this.sendUpdateSettings();
                    this.updateCollections(false);
                    return true;
                } else {
                    for(RecipeBookTabButton var3 : this.tabButtons) {
                        if (var3.mouseClicked(param0, param1, param2)) {
                            if (this.selectedTab != var3) {
                                if (this.selectedTab != null) {
                                    this.selectedTab.setStateTriggered(false);
                                }

                                this.selectedTab = var3;
                                this.selectedTab.setStateTriggered(true);
                                this.updateCollections(true);
                            }

                            return true;
                        }
                    }

                    return false;
                }
            }
        } else {
            return false;
        }
    }

    private boolean toggleFiltering() {
        RecipeBookType var0 = this.menu.getRecipeBookType();
        boolean var1 = !this.book.isFiltering(var0);
        this.book.setFiltering(var0, var1);
        return var1;
    }

    public boolean hasClickedOutside(double param0, double param1, int param2, int param3, int param4, int param5, int param6) {
        if (!this.isVisible()) {
            return true;
        } else {
            boolean var0 = param0 < (double)param2 || param1 < (double)param3 || param0 >= (double)(param2 + param4) || param1 >= (double)(param3 + param5);
            boolean var1 = (double)(param2 - 147) < param0 && param0 < (double)param2 && (double)param3 < param1 && param1 < (double)(param3 + param5);
            return var0 && !var1 && !this.selectedTab.isHoveredOrFocused();
        }
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        this.ignoreTextInput = false;
        if (!this.isVisible() || this.minecraft.player.isSpectator()) {
            return false;
        } else if (param0 == 256 && !this.isOffsetNextToMainGUI()) {
            this.setVisible(false);
            return true;
        } else if (this.searchBox.keyPressed(param0, param1, param2)) {
            this.checkSearchStringUpdate();
            return true;
        } else if (this.searchBox.isFocused() && this.searchBox.isVisible() && param0 != 256) {
            return true;
        } else if (this.minecraft.options.keyChat.matches(param0, param1) && !this.searchBox.isFocused()) {
            this.ignoreTextInput = true;
            this.searchBox.setFocused(true);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean keyReleased(int param0, int param1, int param2) {
        this.ignoreTextInput = false;
        return GuiEventListener.super.keyReleased(param0, param1, param2);
    }

    @Override
    public boolean charTyped(char param0, int param1) {
        if (this.ignoreTextInput) {
            return false;
        } else if (!this.isVisible() || this.minecraft.player.isSpectator()) {
            return false;
        } else if (this.searchBox.charTyped(param0, param1)) {
            this.checkSearchStringUpdate();
            return true;
        } else {
            return GuiEventListener.super.charTyped(param0, param1);
        }
    }

    @Override
    public boolean isMouseOver(double param0, double param1) {
        return false;
    }

    @Override
    public void setFocused(boolean param0) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    private void checkSearchStringUpdate() {
        String var0 = this.searchBox.getValue().toLowerCase(Locale.ROOT);
        this.pirateSpeechForThePeople(var0);
        if (!var0.equals(this.lastSearch)) {
            this.updateCollections(false);
            this.lastSearch = var0;
        }

    }

    private void pirateSpeechForThePeople(String param0) {
        if ("excitedze".equals(param0)) {
            LanguageManager var0 = this.minecraft.getLanguageManager();
            String var1 = "en_pt";
            LanguageInfo var2 = var0.getLanguage("en_pt");
            if (var2 == null || var0.getSelected().equals("en_pt")) {
                return;
            }

            var0.setSelected("en_pt");
            this.minecraft.options.languageCode = "en_pt";
            this.minecraft.reloadResourcePacks();
            this.minecraft.options.save();
        }

    }

    private boolean isOffsetNextToMainGUI() {
        return this.xOffset == 86;
    }

    public void recipesUpdated() {
        this.updateTabs();
        if (this.isVisible()) {
            this.updateCollections(false);
        }

    }

    @Override
    public void recipesShown(List<Recipe<?>> param0) {
        for(Recipe<?> var0 : param0) {
            this.minecraft.player.removeRecipeHighlight(var0);
        }

    }

    public void setupGhostRecipe(Recipe<?> param0, List<Slot> param1) {
        ItemStack var0 = param0.getResultItem(this.minecraft.level.registryAccess());
        this.ghostRecipe.setRecipe(param0);
        this.ghostRecipe.addIngredient(Ingredient.of(var0), param1.get(0).x, param1.get(0).y);
        this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), param0, param0.getIngredients().iterator(), 0);
    }

    @Override
    public void addItemToSlot(Iterator<Ingredient> param0, int param1, int param2, int param3, int param4) {
        Ingredient var0 = param0.next();
        if (!var0.isEmpty()) {
            Slot var1 = this.menu.slots.get(param1);
            this.ghostRecipe.addIngredient(var0, var1.x, var1.y);
        }

    }

    protected void sendUpdateSettings() {
        if (this.minecraft.getConnection() != null) {
            RecipeBookType var0 = this.menu.getRecipeBookType();
            boolean var1 = this.book.getBookSettings().isOpen(var0);
            boolean var2 = this.book.getBookSettings().isFiltering(var0);
            this.minecraft.getConnection().send(new ServerboundRecipeBookChangeSettingsPacket(var0, var1, var2));
        }

    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return this.visible ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput param0) {
        List<NarratableEntry> var0 = Lists.newArrayList();
        this.recipeBookPage.listButtons(param1 -> {
            if (param1.isActive()) {
                var0.add(param1);
            }

        });
        var0.add(this.searchBox);
        var0.add(this.filterButton);
        var0.addAll(this.tabButtons);
        Screen.NarratableSearchResult var1 = Screen.findNarratableWidget(var0, null);
        if (var1 != null) {
            var1.entry.updateNarration(param0.nest());
        }

    }
}
