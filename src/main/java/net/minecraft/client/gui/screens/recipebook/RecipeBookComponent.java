package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.Language;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.network.protocol.game.ServerboundRecipeBookUpdatePacket;
import net.minecraft.recipebook.PlaceRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RecipeBookComponent extends GuiComponent implements Widget, GuiEventListener, RecipeShownListener, PlaceRecipe<Ingredient> {
    protected static final ResourceLocation RECIPE_BOOK_LOCATION = new ResourceLocation("textures/gui/recipe_book.png");
    private int xOffset;
    private int width;
    private int height;
    protected final GhostRecipe ghostRecipe = new GhostRecipe();
    private final List<RecipeBookTabButton> tabButtons = Lists.newArrayList();
    private RecipeBookTabButton selectedTab;
    protected StateSwitchingButton filterButton;
    protected RecipeBookMenu<?> menu;
    protected Minecraft minecraft;
    private EditBox searchBox;
    private String lastSearch = "";
    protected ClientRecipeBook book;
    protected final RecipeBookPage recipeBookPage = new RecipeBookPage();
    protected final StackedContents stackedContents = new StackedContents();
    private int timesInventoryChanged;
    private boolean ignoreTextInput;

    public void init(int param0, int param1, Minecraft param2, boolean param3, RecipeBookMenu<?> param4) {
        this.minecraft = param2;
        this.width = param0;
        this.height = param1;
        this.menu = param4;
        param2.player.containerMenu = param4;
        this.book = param2.player.getRecipeBook();
        this.timesInventoryChanged = param2.player.inventory.getTimesChanged();
        if (this.isVisible()) {
            this.initVisuals(param3);
        }

        param2.keyboardHandler.setSendRepeatsToGui(true);
    }

    public void initVisuals(boolean param0) {
        this.xOffset = param0 ? 0 : 86;
        int var0 = (this.width - 147) / 2 - this.xOffset;
        int var1 = (this.height - 166) / 2;
        this.stackedContents.clear();
        this.minecraft.player.inventory.fillStackedContents(this.stackedContents);
        this.menu.fillCraftSlotsStackedContents(this.stackedContents);
        String var2 = this.searchBox != null ? this.searchBox.getValue() : "";
        this.searchBox = new EditBox(this.minecraft.font, var0 + 25, var1 + 14, 80, 9 + 5, I18n.get("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setBordered(false);
        this.searchBox.setVisible(true);
        this.searchBox.setTextColor(16777215);
        this.searchBox.setValue(var2);
        this.recipeBookPage.init(this.minecraft, var0, var1);
        this.recipeBookPage.addListener(this);
        this.filterButton = new StateSwitchingButton(var0 + 110, var1 + 12, 26, 16, this.book.isFilteringCraftable(this.menu));
        this.initFilterButtonTextures();
        this.tabButtons.clear();

        for(RecipeBookCategories var3 : ClientRecipeBook.getCategories(this.menu)) {
            this.tabButtons.add(new RecipeBookTabButton(var3));
        }

        if (this.selectedTab != null) {
            this.selectedTab = this.tabButtons
                .stream()
                .filter(param0x -> param0x.getCategory().equals(this.selectedTab.getCategory()))
                .findFirst()
                .orElse(null);
        }

        if (this.selectedTab == null) {
            this.selectedTab = this.tabButtons.get(0);
        }

        this.selectedTab.setStateTriggered(true);
        this.updateCollections(false);
        this.updateTabs();
    }

    @Override
    public boolean changeFocus(boolean param0) {
        return false;
    }

    protected void initFilterButtonTextures() {
        this.filterButton.initTextureValues(152, 41, 28, 18, RECIPE_BOOK_LOCATION);
    }

    public void removed() {
        this.searchBox = null;
        this.selectedTab = null;
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    public int updateScreenPosition(boolean param0, int param1, int param2) {
        int var0;
        if (this.isVisible() && !param0) {
            var0 = 177 + (param1 - param2 - 200) / 2;
        } else {
            var0 = (param1 - param2) / 2;
        }

        return var0;
    }

    public void toggleVisibility() {
        this.setVisible(!this.isVisible());
    }

    public boolean isVisible() {
        return this.book.isGuiOpen();
    }

    protected void setVisible(boolean param0) {
        this.book.setGuiOpen(param0);
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

        if (this.book.isFilteringCraftable(this.menu)) {
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
            if (var5 == RecipeBookCategories.SEARCH || var5 == RecipeBookCategories.FURNACE_SEARCH) {
                var4.visible = true;
                var4.setPosition(var0, var1 + 27 * var3++);
            } else if (var4.updateVisibility(this.book)) {
                var4.setPosition(var0, var1 + 27 * var3++);
                var4.startAnimation(this.minecraft);
            }
        }

    }

    public void tick() {
        if (this.isVisible()) {
            if (this.timesInventoryChanged != this.minecraft.player.inventory.getTimesChanged()) {
                this.updateStackedContents();
                this.timesInventoryChanged = this.minecraft.player.inventory.getTimesChanged();
            }

        }
    }

    private void updateStackedContents() {
        this.stackedContents.clear();
        this.minecraft.player.inventory.fillStackedContents(this.stackedContents);
        this.menu.fillCraftSlotsStackedContents(this.stackedContents);
        this.updateCollections(false);
    }

    @Override
    public void render(int param0, int param1, float param2) {
        if (this.isVisible()) {
            Lighting.turnOnGui();
            GlStateManager.disableLighting();
            GlStateManager.pushMatrix();
            GlStateManager.translatef(0.0F, 0.0F, 100.0F);
            this.minecraft.getTextureManager().bind(RECIPE_BOOK_LOCATION);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            int var0 = (this.width - 147) / 2 - this.xOffset;
            int var1 = (this.height - 166) / 2;
            this.blit(var0, var1, 1, 1, 147, 166);
            this.searchBox.render(param0, param1, param2);
            Lighting.turnOff();

            for(RecipeBookTabButton var2 : this.tabButtons) {
                var2.render(param0, param1, param2);
            }

            this.filterButton.render(param0, param1, param2);
            this.recipeBookPage.render(var0, var1, param0, param1, param2);
            GlStateManager.popMatrix();
        }
    }

    public void renderTooltip(int param0, int param1, int param2, int param3) {
        if (this.isVisible()) {
            this.recipeBookPage.renderTooltip(param2, param3);
            if (this.filterButton.isHovered()) {
                String var0 = this.getFilterButtonTooltip();
                if (this.minecraft.screen != null) {
                    this.minecraft.screen.renderTooltip(var0, param2, param3);
                }
            }

            this.renderGhostRecipeTooltip(param0, param1, param2, param3);
        }
    }

    protected String getFilterButtonTooltip() {
        return I18n.get(this.filterButton.isStateTriggered() ? "gui.recipebook.toggleRecipes.craftable" : "gui.recipebook.toggleRecipes.all");
    }

    private void renderGhostRecipeTooltip(int param0, int param1, int param2, int param3) {
        ItemStack var0 = null;

        for(int var1 = 0; var1 < this.ghostRecipe.size(); ++var1) {
            GhostRecipe.GhostIngredient var2 = this.ghostRecipe.get(var1);
            int var3 = var2.getX() + param0;
            int var4 = var2.getY() + param1;
            if (param2 >= var3 && param3 >= var4 && param2 < var3 + 16 && param3 < var4 + 16) {
                var0 = var2.getItem();
            }
        }

        if (var0 != null && this.minecraft.screen != null) {
            this.minecraft.screen.renderTooltip(this.minecraft.screen.getTooltipFromItem(var0), param2, param3);
        }

    }

    public void renderGhostRecipe(int param0, int param1, boolean param2, float param3) {
        this.ghostRecipe.render(this.minecraft, param0, param1, param2, param3);
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
                return true;
            } else if (this.filterButton.mouseClicked(param0, param1, param2)) {
                boolean var2 = this.updateFiltering();
                this.filterButton.setStateTriggered(var2);
                this.sendUpdateSettings();
                this.updateCollections(false);
                return true;
            } else {
                for(RecipeBookTabButton var3 : this.tabButtons) {
                    if (var3.mouseClicked(param0, param1, param2)) {
                        if (this.selectedTab != var3) {
                            this.selectedTab.setStateTriggered(false);
                            this.selectedTab = var3;
                            this.selectedTab.setStateTriggered(true);
                            this.updateCollections(true);
                        }

                        return true;
                    }
                }

                return false;
            }
        } else {
            return false;
        }
    }

    protected boolean updateFiltering() {
        boolean var0 = !this.book.isFilteringCraftable();
        this.book.setFilteringCraftable(var0);
        return var0;
    }

    public boolean hasClickedOutside(double param0, double param1, int param2, int param3, int param4, int param5, int param6) {
        if (!this.isVisible()) {
            return true;
        } else {
            boolean var0 = param0 < (double)param2 || param1 < (double)param3 || param0 >= (double)(param2 + param4) || param1 >= (double)(param3 + param5);
            boolean var1 = (double)(param2 - 147) < param0 && param0 < (double)param2 && (double)param3 < param1 && param1 < (double)(param3 + param5);
            return var0 && !var1 && !this.selectedTab.isHovered();
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
            this.searchBox.setFocus(true);
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
            Language var1 = var0.getLanguage("en_pt");
            if (var0.getSelected().compareTo(var1) == 0) {
                return;
            }

            var0.setSelected(var1);
            this.minecraft.options.languageCode = var1.getCode();
            this.minecraft.reloadResourcePacks();
            this.minecraft.font.setBidirectional(var0.isBidirectional());
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
        ItemStack var0 = param0.getResultItem();
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
            this.minecraft
                .getConnection()
                .send(
                    new ServerboundRecipeBookUpdatePacket(
                        this.book.isGuiOpen(),
                        this.book.isFilteringCraftable(),
                        this.book.isFurnaceGuiOpen(),
                        this.book.isFurnaceFilteringCraftable(),
                        this.book.isBlastingFurnaceGuiOpen(),
                        this.book.isBlastingFurnaceFilteringCraftable()
                    )
                );
        }

    }
}
