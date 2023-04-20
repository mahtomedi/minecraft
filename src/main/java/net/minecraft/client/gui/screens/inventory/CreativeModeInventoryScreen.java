package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.HotbarManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreativeModeInventoryScreen extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
    private static final ResourceLocation CREATIVE_TABS_LOCATION = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
    private static final String GUI_CREATIVE_TAB_PREFIX = "textures/gui/container/creative_inventory/tab_";
    private static final String CUSTOM_SLOT_LOCK = "CustomCreativeLock";
    private static final int NUM_ROWS = 5;
    private static final int NUM_COLS = 9;
    private static final int TAB_WIDTH = 26;
    private static final int TAB_HEIGHT = 32;
    private static final int SCROLLER_WIDTH = 12;
    private static final int SCROLLER_HEIGHT = 15;
    static final SimpleContainer CONTAINER = new SimpleContainer(45);
    private static final Component TRASH_SLOT_TOOLTIP = Component.translatable("inventory.binSlot");
    private static final int TEXT_COLOR = 16777215;
    private static CreativeModeTab selectedTab = CreativeModeTabs.getDefaultTab();
    private float scrollOffs;
    private boolean scrolling;
    private EditBox searchBox;
    @Nullable
    private List<Slot> originalSlots;
    @Nullable
    private Slot destroyItemSlot;
    private CreativeInventoryListener listener;
    private boolean ignoreTextInput;
    private boolean hasClickedOutside;
    private final Set<TagKey<Item>> visibleTags = new HashSet<>();
    private final boolean displayOperatorCreativeTab;

    public CreativeModeInventoryScreen(Player param0, FeatureFlagSet param1, boolean param2) {
        super(new CreativeModeInventoryScreen.ItemPickerMenu(param0), param0.getInventory(), CommonComponents.EMPTY);
        param0.containerMenu = this.menu;
        this.imageHeight = 136;
        this.imageWidth = 195;
        this.displayOperatorCreativeTab = param2;
        CreativeModeTabs.tryRebuildTabContents(param1, this.hasPermissions(param0), param0.level.registryAccess());
    }

    private boolean hasPermissions(Player param0) {
        return param0.canUseGameMasterBlocks() && this.displayOperatorCreativeTab;
    }

    private void tryRefreshInvalidatedTabs(FeatureFlagSet param0, boolean param1, HolderLookup.Provider param2) {
        if (CreativeModeTabs.tryRebuildTabContents(param0, param1, param2)) {
            for(CreativeModeTab var0 : CreativeModeTabs.allTabs()) {
                Collection<ItemStack> var1 = var0.getDisplayItems();
                if (var0 == selectedTab) {
                    if (var0.getType() == CreativeModeTab.Type.CATEGORY && var1.isEmpty()) {
                        this.selectTab(CreativeModeTabs.getDefaultTab());
                    } else {
                        this.refreshCurrentTabContents(var1);
                    }
                }
            }
        }

    }

    private void refreshCurrentTabContents(Collection<ItemStack> param0) {
        int var0 = this.menu.getRowIndexForScroll(this.scrollOffs);
        this.menu.items.clear();
        if (selectedTab.getType() == CreativeModeTab.Type.SEARCH) {
            this.refreshSearchResults();
        } else {
            this.menu.items.addAll(param0);
        }

        this.scrollOffs = this.menu.getScrollForRowIndex(var0);
        this.menu.scrollTo(this.scrollOffs);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (this.minecraft != null) {
            if (this.minecraft.player != null) {
                this.tryRefreshInvalidatedTabs(
                    this.minecraft.player.connection.enabledFeatures(),
                    this.hasPermissions(this.minecraft.player),
                    this.minecraft.player.level.registryAccess()
                );
            }

            if (!this.minecraft.gameMode.hasInfiniteItems()) {
                this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
            } else {
                this.searchBox.tick();
            }

        }
    }

    @Override
    protected void slotClicked(@Nullable Slot param0, int param1, int param2, ClickType param3) {
        if (this.isCreativeSlot(param0)) {
            this.searchBox.moveCursorToEnd();
            this.searchBox.setHighlightPos(0);
        }

        boolean var0 = param3 == ClickType.QUICK_MOVE;
        param3 = param1 == -999 && param3 == ClickType.PICKUP ? ClickType.THROW : param3;
        if (param0 == null && selectedTab.getType() != CreativeModeTab.Type.INVENTORY && param3 != ClickType.QUICK_CRAFT) {
            if (!this.menu.getCarried().isEmpty() && this.hasClickedOutside) {
                if (param2 == 0) {
                    this.minecraft.player.drop(this.menu.getCarried(), true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop(this.menu.getCarried());
                    this.menu.setCarried(ItemStack.EMPTY);
                }

                if (param2 == 1) {
                    ItemStack var14 = this.menu.getCarried().split(1);
                    this.minecraft.player.drop(var14, true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop(var14);
                }
            }
        } else {
            if (param0 != null && !param0.mayPickup(this.minecraft.player)) {
                return;
            }

            if (param0 == this.destroyItemSlot && var0) {
                for(int var1 = 0; var1 < this.minecraft.player.inventoryMenu.getItems().size(); ++var1) {
                    this.minecraft.gameMode.handleCreativeModeItemAdd(ItemStack.EMPTY, var1);
                }
            } else if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
                if (param0 == this.destroyItemSlot) {
                    this.menu.setCarried(ItemStack.EMPTY);
                } else if (param3 == ClickType.THROW && param0 != null && param0.hasItem()) {
                    ItemStack var2 = param0.remove(param2 == 0 ? 1 : param0.getItem().getMaxStackSize());
                    ItemStack var3 = param0.getItem();
                    this.minecraft.player.drop(var2, true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop(var2);
                    this.minecraft.gameMode.handleCreativeModeItemAdd(var3, ((CreativeModeInventoryScreen.SlotWrapper)param0).target.index);
                } else if (param3 == ClickType.THROW && !this.menu.getCarried().isEmpty()) {
                    this.minecraft.player.drop(this.menu.getCarried(), true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop(this.menu.getCarried());
                    this.menu.setCarried(ItemStack.EMPTY);
                } else {
                    this.minecraft
                        .player
                        .inventoryMenu
                        .clicked(
                            param0 == null ? param1 : ((CreativeModeInventoryScreen.SlotWrapper)param0).target.index, param2, param3, this.minecraft.player
                        );
                    this.minecraft.player.inventoryMenu.broadcastChanges();
                }
            } else if (param3 != ClickType.QUICK_CRAFT && param0.container == CONTAINER) {
                ItemStack var4 = this.menu.getCarried();
                ItemStack var5 = param0.getItem();
                if (param3 == ClickType.SWAP) {
                    if (!var5.isEmpty()) {
                        this.minecraft.player.getInventory().setItem(param2, var5.copyWithCount(var5.getMaxStackSize()));
                        this.minecraft.player.inventoryMenu.broadcastChanges();
                    }

                    return;
                }

                if (param3 == ClickType.CLONE) {
                    if (this.menu.getCarried().isEmpty() && param0.hasItem()) {
                        ItemStack var6 = param0.getItem();
                        this.menu.setCarried(var6.copyWithCount(var6.getMaxStackSize()));
                    }

                    return;
                }

                if (param3 == ClickType.THROW) {
                    if (!var5.isEmpty()) {
                        ItemStack var7 = var5.copyWithCount(param2 == 0 ? 1 : var5.getMaxStackSize());
                        this.minecraft.player.drop(var7, true);
                        this.minecraft.gameMode.handleCreativeModeItemDrop(var7);
                    }

                    return;
                }

                if (!var4.isEmpty() && !var5.isEmpty() && var4.sameItem(var5) && ItemStack.tagMatches(var4, var5)) {
                    if (param2 == 0) {
                        if (var0) {
                            var4.setCount(var4.getMaxStackSize());
                        } else if (var4.getCount() < var4.getMaxStackSize()) {
                            var4.grow(1);
                        }
                    } else {
                        var4.shrink(1);
                    }
                } else if (!var5.isEmpty() && var4.isEmpty()) {
                    int var8 = var0 ? var5.getMaxStackSize() : var5.getCount();
                    this.menu.setCarried(var5.copyWithCount(var8));
                } else if (param2 == 0) {
                    this.menu.setCarried(ItemStack.EMPTY);
                } else if (!this.menu.getCarried().isEmpty()) {
                    this.menu.getCarried().shrink(1);
                }
            } else if (this.menu != null) {
                ItemStack var9 = param0 == null ? ItemStack.EMPTY : this.menu.getSlot(param0.index).getItem();
                this.menu.clicked(param0 == null ? param1 : param0.index, param2, param3, this.minecraft.player);
                if (AbstractContainerMenu.getQuickcraftHeader(param2) == 2) {
                    for(int var10 = 0; var10 < 9; ++var10) {
                        this.minecraft.gameMode.handleCreativeModeItemAdd(this.menu.getSlot(45 + var10).getItem(), 36 + var10);
                    }
                } else if (param0 != null) {
                    ItemStack var11 = this.menu.getSlot(param0.index).getItem();
                    this.minecraft.gameMode.handleCreativeModeItemAdd(var11, param0.index - this.menu.slots.size() + 9 + 36);
                    int var12 = 45 + param2;
                    if (param3 == ClickType.SWAP) {
                        this.minecraft.gameMode.handleCreativeModeItemAdd(var9, var12 - this.menu.slots.size() + 9 + 36);
                    } else if (param3 == ClickType.THROW && !var9.isEmpty()) {
                        ItemStack var13 = var9.copyWithCount(param2 == 0 ? 1 : var9.getMaxStackSize());
                        this.minecraft.player.drop(var13, true);
                        this.minecraft.gameMode.handleCreativeModeItemDrop(var13);
                    }

                    this.minecraft.player.inventoryMenu.broadcastChanges();
                }
            }
        }

    }

    private boolean isCreativeSlot(@Nullable Slot param0) {
        return param0 != null && param0.container == CONTAINER;
    }

    @Override
    protected void init() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            super.init();
            this.searchBox = new EditBox(this.font, this.leftPos + 82, this.topPos + 6, 80, 9, Component.translatable("itemGroup.search"));
            this.searchBox.setMaxLength(50);
            this.searchBox.setBordered(false);
            this.searchBox.setVisible(false);
            this.searchBox.setTextColor(16777215);
            this.addWidget(this.searchBox);
            CreativeModeTab var0 = selectedTab;
            selectedTab = CreativeModeTabs.getDefaultTab();
            this.selectTab(var0);
            this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
            this.listener = new CreativeInventoryListener(this.minecraft);
            this.minecraft.player.inventoryMenu.addSlotListener(this.listener);
            if (!selectedTab.shouldDisplay()) {
                this.selectTab(CreativeModeTabs.getDefaultTab());
            }
        } else {
            this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
        }

    }

    @Override
    public void resize(Minecraft param0, int param1, int param2) {
        int var0 = this.menu.getRowIndexForScroll(this.scrollOffs);
        String var1 = this.searchBox.getValue();
        this.init(param0, param1, param2);
        this.searchBox.setValue(var1);
        if (!this.searchBox.getValue().isEmpty()) {
            this.refreshSearchResults();
        }

        this.scrollOffs = this.menu.getScrollForRowIndex(var0);
        this.menu.scrollTo(this.scrollOffs);
    }

    @Override
    public void removed() {
        super.removed();
        if (this.minecraft.player != null && this.minecraft.player.getInventory() != null) {
            this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
        }

    }

    @Override
    public boolean charTyped(char param0, int param1) {
        if (this.ignoreTextInput) {
            return false;
        } else if (selectedTab.getType() != CreativeModeTab.Type.SEARCH) {
            return false;
        } else {
            String var0 = this.searchBox.getValue();
            if (this.searchBox.charTyped(param0, param1)) {
                if (!Objects.equals(var0, this.searchBox.getValue())) {
                    this.refreshSearchResults();
                }

                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        this.ignoreTextInput = false;
        if (selectedTab.getType() != CreativeModeTab.Type.SEARCH) {
            if (this.minecraft.options.keyChat.matches(param0, param1)) {
                this.ignoreTextInput = true;
                this.selectTab(CreativeModeTabs.searchTab());
                return true;
            } else {
                return super.keyPressed(param0, param1, param2);
            }
        } else {
            boolean var0 = !this.isCreativeSlot(this.hoveredSlot) || this.hoveredSlot.hasItem();
            boolean var1 = InputConstants.getKey(param0, param1).getNumericKeyValue().isPresent();
            if (var0 && var1 && this.checkHotbarKeyPressed(param0, param1)) {
                this.ignoreTextInput = true;
                return true;
            } else {
                String var2 = this.searchBox.getValue();
                if (this.searchBox.keyPressed(param0, param1, param2)) {
                    if (!Objects.equals(var2, this.searchBox.getValue())) {
                        this.refreshSearchResults();
                    }

                    return true;
                } else {
                    return this.searchBox.isFocused() && this.searchBox.isVisible() && param0 != 256 ? true : super.keyPressed(param0, param1, param2);
                }
            }
        }
    }

    @Override
    public boolean keyReleased(int param0, int param1, int param2) {
        this.ignoreTextInput = false;
        return super.keyReleased(param0, param1, param2);
    }

    private void refreshSearchResults() {
        this.menu.items.clear();
        this.visibleTags.clear();
        String var0 = this.searchBox.getValue();
        if (var0.isEmpty()) {
            this.menu.items.addAll(selectedTab.getDisplayItems());
        } else {
            SearchTree<ItemStack> var1;
            if (var0.startsWith("#")) {
                var0 = var0.substring(1);
                var1 = this.minecraft.getSearchTree(SearchRegistry.CREATIVE_TAGS);
                this.updateVisibleTags(var0);
            } else {
                var1 = this.minecraft.getSearchTree(SearchRegistry.CREATIVE_NAMES);
            }

            this.menu.items.addAll(var1.search(var0.toLowerCase(Locale.ROOT)));
        }

        this.scrollOffs = 0.0F;
        this.menu.scrollTo(0.0F);
    }

    private void updateVisibleTags(String param0) {
        int var0 = param0.indexOf(58);
        Predicate<ResourceLocation> var1;
        if (var0 == -1) {
            var1 = param1 -> param1.getPath().contains(param0);
        } else {
            String var2 = param0.substring(0, var0).trim();
            String var3 = param0.substring(var0 + 1).trim();
            var1 = param2 -> param2.getNamespace().contains(var2) && param2.getPath().contains(var3);
        }

        BuiltInRegistries.ITEM.getTagNames().filter(param1 -> var1.test(param1.location())).forEach(this.visibleTags::add);
    }

    @Override
    protected void renderLabels(GuiGraphics param0, int param1, int param2) {
        if (selectedTab.showTitle()) {
            param0.drawString(this.font, selectedTab.getDisplayName(), 8, 6, 4210752, false);
        }

    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (param2 == 0) {
            double var0 = param0 - (double)this.leftPos;
            double var1 = param1 - (double)this.topPos;

            for(CreativeModeTab var2 : CreativeModeTabs.tabs()) {
                if (this.checkTabClicked(var2, var0, var1)) {
                    return true;
                }
            }

            if (selectedTab.getType() != CreativeModeTab.Type.INVENTORY && this.insideScrollbar(param0, param1)) {
                this.scrolling = this.canScroll();
                return true;
            }
        }

        return super.mouseClicked(param0, param1, param2);
    }

    @Override
    public boolean mouseReleased(double param0, double param1, int param2) {
        if (param2 == 0) {
            double var0 = param0 - (double)this.leftPos;
            double var1 = param1 - (double)this.topPos;
            this.scrolling = false;

            for(CreativeModeTab var2 : CreativeModeTabs.tabs()) {
                if (this.checkTabClicked(var2, var0, var1)) {
                    this.selectTab(var2);
                    return true;
                }
            }
        }

        return super.mouseReleased(param0, param1, param2);
    }

    private boolean canScroll() {
        return selectedTab.canScroll() && this.menu.canScroll();
    }

    private void selectTab(CreativeModeTab param0) {
        CreativeModeTab var0 = selectedTab;
        selectedTab = param0;
        this.quickCraftSlots.clear();
        this.menu.items.clear();
        this.clearDraggingState();
        if (selectedTab.getType() == CreativeModeTab.Type.HOTBAR) {
            HotbarManager var1 = this.minecraft.getHotbarManager();

            for(int var2 = 0; var2 < 9; ++var2) {
                Hotbar var3 = var1.get(var2);
                if (var3.isEmpty()) {
                    for(int var4 = 0; var4 < 9; ++var4) {
                        if (var4 == var2) {
                            ItemStack var5 = new ItemStack(Items.PAPER);
                            var5.getOrCreateTagElement("CustomCreativeLock");
                            Component var6 = this.minecraft.options.keyHotbarSlots[var2].getTranslatedKeyMessage();
                            Component var7 = this.minecraft.options.keySaveHotbarActivator.getTranslatedKeyMessage();
                            var5.setHoverName(Component.translatable("inventory.hotbarInfo", var7, var6));
                            this.menu.items.add(var5);
                        } else {
                            this.menu.items.add(ItemStack.EMPTY);
                        }
                    }
                } else {
                    this.menu.items.addAll(var3);
                }
            }
        } else if (selectedTab.getType() == CreativeModeTab.Type.CATEGORY) {
            this.menu.items.addAll(selectedTab.getDisplayItems());
        }

        if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
            AbstractContainerMenu var8 = this.minecraft.player.inventoryMenu;
            if (this.originalSlots == null) {
                this.originalSlots = ImmutableList.copyOf(this.menu.slots);
            }

            this.menu.slots.clear();

            for(int var9 = 0; var9 < var8.slots.size(); ++var9) {
                int var13;
                int var14;
                if (var9 >= 5 && var9 < 9) {
                    int var10 = var9 - 5;
                    int var11 = var10 / 2;
                    int var12 = var10 % 2;
                    var13 = 54 + var11 * 54;
                    var14 = 6 + var12 * 27;
                } else if (var9 >= 0 && var9 < 5) {
                    var13 = -2000;
                    var14 = -2000;
                } else if (var9 == 45) {
                    var13 = 35;
                    var14 = 20;
                } else {
                    int var19 = var9 - 9;
                    int var20 = var19 % 9;
                    int var21 = var19 / 9;
                    var13 = 9 + var20 * 18;
                    if (var9 >= 36) {
                        var14 = 112;
                    } else {
                        var14 = 54 + var21 * 18;
                    }
                }

                Slot var25 = new CreativeModeInventoryScreen.SlotWrapper(var8.slots.get(var9), var9, var13, var14);
                this.menu.slots.add(var25);
            }

            this.destroyItemSlot = new Slot(CONTAINER, 0, 173, 112);
            this.menu.slots.add(this.destroyItemSlot);
        } else if (var0.getType() == CreativeModeTab.Type.INVENTORY) {
            this.menu.slots.clear();
            this.menu.slots.addAll(this.originalSlots);
            this.originalSlots = null;
        }

        if (selectedTab.getType() == CreativeModeTab.Type.SEARCH) {
            this.searchBox.setVisible(true);
            this.searchBox.setCanLoseFocus(false);
            this.searchBox.setFocused(true);
            if (var0 != param0) {
                this.searchBox.setValue("");
            }

            this.refreshSearchResults();
        } else {
            this.searchBox.setVisible(false);
            this.searchBox.setCanLoseFocus(true);
            this.searchBox.setFocused(false);
            this.searchBox.setValue("");
        }

        this.scrollOffs = 0.0F;
        this.menu.scrollTo(0.0F);
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2) {
        if (!this.canScroll()) {
            return false;
        } else {
            this.scrollOffs = this.menu.subtractInputFromScroll(this.scrollOffs, param2);
            this.menu.scrollTo(this.scrollOffs);
            return true;
        }
    }

    @Override
    protected boolean hasClickedOutside(double param0, double param1, int param2, int param3, int param4) {
        boolean var0 = param0 < (double)param2
            || param1 < (double)param3
            || param0 >= (double)(param2 + this.imageWidth)
            || param1 >= (double)(param3 + this.imageHeight);
        this.hasClickedOutside = var0 && !this.checkTabClicked(selectedTab, param0, param1);
        return this.hasClickedOutside;
    }

    protected boolean insideScrollbar(double param0, double param1) {
        int var0 = this.leftPos;
        int var1 = this.topPos;
        int var2 = var0 + 175;
        int var3 = var1 + 18;
        int var4 = var2 + 14;
        int var5 = var3 + 112;
        return param0 >= (double)var2 && param1 >= (double)var3 && param0 < (double)var4 && param1 < (double)var5;
    }

    @Override
    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        if (this.scrolling) {
            int var0 = this.topPos + 18;
            int var1 = var0 + 112;
            this.scrollOffs = ((float)param1 - (float)var0 - 7.5F) / ((float)(var1 - var0) - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.menu.scrollTo(this.scrollOffs);
            return true;
        } else {
            return super.mouseDragged(param0, param1, param2, param3, param4);
        }
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        super.render(param0, param1, param2, param3);

        for(CreativeModeTab var0 : CreativeModeTabs.tabs()) {
            if (this.checkTabHovering(param0, var0, param1, param2)) {
                break;
            }
        }

        if (this.destroyItemSlot != null
            && selectedTab.getType() == CreativeModeTab.Type.INVENTORY
            && this.isHovering(this.destroyItemSlot.x, this.destroyItemSlot.y, 16, 16, (double)param1, (double)param2)) {
            param0.renderTooltip(this.font, TRASH_SLOT_TOOLTIP, param1, param2);
        }

        this.renderTooltip(param0, param1, param2);
    }

    @Override
    public List<Component> getTooltipFromContainerItem(ItemStack param0) {
        boolean var0 = this.hoveredSlot != null && this.hoveredSlot instanceof CreativeModeInventoryScreen.CustomCreativeSlot;
        boolean var1 = selectedTab.getType() == CreativeModeTab.Type.CATEGORY;
        boolean var2 = selectedTab.getType() == CreativeModeTab.Type.SEARCH;
        TooltipFlag.Default var3 = this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
        TooltipFlag var4 = var0 ? var3.asCreative() : var3;
        List<Component> var5 = param0.getTooltipLines(this.minecraft.player, var4);
        if (var1 && var0) {
            return var5;
        } else {
            List<Component> var6 = Lists.newArrayList(var5);
            if (var2 && var0) {
                this.visibleTags.forEach(param2 -> {
                    if (param0.is(param2)) {
                        var6.add(1, Component.literal("#" + param2.location()).withStyle(ChatFormatting.DARK_PURPLE));
                    }

                });
            }

            int var7 = 1;

            for(CreativeModeTab var8 : CreativeModeTabs.tabs()) {
                if (var8.getType() != CreativeModeTab.Type.SEARCH && var8.contains(param0)) {
                    var6.add(var7++, var8.getDisplayName().copy().withStyle(ChatFormatting.BLUE));
                }
            }

            return var6;
        }
    }

    @Override
    protected void renderBg(GuiGraphics param0, float param1, int param2, int param3) {
        for(CreativeModeTab var0 : CreativeModeTabs.tabs()) {
            if (var0 != selectedTab) {
                this.renderTabButton(param0, var0);
            }
        }

        param0.blit(
            new ResourceLocation("textures/gui/container/creative_inventory/tab_" + selectedTab.getBackgroundSuffix()),
            this.leftPos,
            this.topPos,
            0,
            0,
            this.imageWidth,
            this.imageHeight
        );
        this.searchBox.render(param0, param2, param3, param1);
        int var1 = this.leftPos + 175;
        int var2 = this.topPos + 18;
        int var3 = var2 + 112;
        if (selectedTab.canScroll()) {
            param0.blit(CREATIVE_TABS_LOCATION, var1, var2 + (int)((float)(var3 - var2 - 17) * this.scrollOffs), 232 + (this.canScroll() ? 0 : 12), 0, 12, 15);
        }

        this.renderTabButton(param0, selectedTab);
        if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                param0,
                this.leftPos + 88,
                this.topPos + 45,
                20,
                (float)(this.leftPos + 88 - param2),
                (float)(this.topPos + 45 - 30 - param3),
                this.minecraft.player
            );
        }

    }

    private int getTabX(CreativeModeTab param0) {
        int var0 = param0.column();
        int var1 = 27;
        int var2 = 27 * var0;
        if (param0.isAlignedRight()) {
            var2 = this.imageWidth - 27 * (7 - var0) + 1;
        }

        return var2;
    }

    private int getTabY(CreativeModeTab param0) {
        int var0 = 0;
        if (param0.row() == CreativeModeTab.Row.TOP) {
            var0 -= 32;
        } else {
            var0 += this.imageHeight;
        }

        return var0;
    }

    protected boolean checkTabClicked(CreativeModeTab param0, double param1, double param2) {
        int var0 = this.getTabX(param0);
        int var1 = this.getTabY(param0);
        return param1 >= (double)var0 && param1 <= (double)(var0 + 26) && param2 >= (double)var1 && param2 <= (double)(var1 + 32);
    }

    protected boolean checkTabHovering(GuiGraphics param0, CreativeModeTab param1, int param2, int param3) {
        int var0 = this.getTabX(param1);
        int var1 = this.getTabY(param1);
        if (this.isHovering(var0 + 3, var1 + 3, 21, 27, (double)param2, (double)param3)) {
            param0.renderTooltip(this.font, param1.getDisplayName(), param2, param3);
            return true;
        } else {
            return false;
        }
    }

    protected void renderTabButton(GuiGraphics param0, CreativeModeTab param1) {
        boolean var0 = param1 == selectedTab;
        boolean var1 = param1.row() == CreativeModeTab.Row.TOP;
        int var2 = param1.column();
        int var3 = var2 * 26;
        int var4 = 0;
        int var5 = this.leftPos + this.getTabX(param1);
        int var6 = this.topPos;
        int var7 = 32;
        if (var0) {
            var4 += 32;
        }

        if (var1) {
            var6 -= 28;
        } else {
            var4 += 64;
            var6 += this.imageHeight - 4;
        }

        param0.blit(CREATIVE_TABS_LOCATION, var5, var6, var3, var4, 26, 32);
        param0.pose().pushPose();
        param0.pose().translate(0.0F, 0.0F, 100.0F);
        var5 += 5;
        var6 += 8 + (var1 ? 1 : -1);
        ItemStack var8 = param1.getIconItem();
        param0.renderItem(var8, var5, var6);
        param0.renderItemDecorations(this.font, var8, var5, var6);
        param0.pose().popPose();
    }

    public boolean isInventoryOpen() {
        return selectedTab.getType() == CreativeModeTab.Type.INVENTORY;
    }

    public static void handleHotbarLoadOrSave(Minecraft param0, int param1, boolean param2, boolean param3) {
        LocalPlayer var0 = param0.player;
        HotbarManager var1 = param0.getHotbarManager();
        Hotbar var2 = var1.get(param1);
        if (param2) {
            for(int var3 = 0; var3 < Inventory.getSelectionSize(); ++var3) {
                ItemStack var4 = var2.get(var3);
                ItemStack var5 = var4.isItemEnabled(var0.level.enabledFeatures()) ? var4.copy() : ItemStack.EMPTY;
                var0.getInventory().setItem(var3, var5);
                param0.gameMode.handleCreativeModeItemAdd(var5, 36 + var3);
            }

            var0.inventoryMenu.broadcastChanges();
        } else if (param3) {
            for(int var6 = 0; var6 < Inventory.getSelectionSize(); ++var6) {
                var2.set(var6, var0.getInventory().getItem(var6).copy());
            }

            Component var7 = param0.options.keyHotbarSlots[param1].getTranslatedKeyMessage();
            Component var8 = param0.options.keyLoadHotbarActivator.getTranslatedKeyMessage();
            Component var9 = Component.translatable("inventory.hotbarSaved", var8, var7);
            param0.gui.setOverlayMessage(var9, false);
            param0.getNarrator().sayNow(var9);
            var1.save();
        }

    }

    @OnlyIn(Dist.CLIENT)
    static class CustomCreativeSlot extends Slot {
        public CustomCreativeSlot(Container param0, int param1, int param2, int param3) {
            super(param0, param1, param2, param3);
        }

        @Override
        public boolean mayPickup(Player param0) {
            ItemStack var0 = this.getItem();
            if (super.mayPickup(param0) && !var0.isEmpty()) {
                return var0.isItemEnabled(param0.level.enabledFeatures()) && var0.getTagElement("CustomCreativeLock") == null;
            } else {
                return var0.isEmpty();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ItemPickerMenu extends AbstractContainerMenu {
        public final NonNullList<ItemStack> items = NonNullList.create();
        private final AbstractContainerMenu inventoryMenu;

        public ItemPickerMenu(Player param0) {
            super(null, 0);
            this.inventoryMenu = param0.inventoryMenu;
            Inventory var0 = param0.getInventory();

            for(int var1 = 0; var1 < 5; ++var1) {
                for(int var2 = 0; var2 < 9; ++var2) {
                    this.addSlot(
                        new CreativeModeInventoryScreen.CustomCreativeSlot(
                            CreativeModeInventoryScreen.CONTAINER, var1 * 9 + var2, 9 + var2 * 18, 18 + var1 * 18
                        )
                    );
                }
            }

            for(int var3 = 0; var3 < 9; ++var3) {
                this.addSlot(new Slot(var0, var3, 9 + var3 * 18, 112));
            }

            this.scrollTo(0.0F);
        }

        @Override
        public boolean stillValid(Player param0) {
            return true;
        }

        protected int calculateRowCount() {
            return Mth.positiveCeilDiv(this.items.size(), 9) - 5;
        }

        protected int getRowIndexForScroll(float param0) {
            return Math.max((int)((double)(param0 * (float)this.calculateRowCount()) + 0.5), 0);
        }

        protected float getScrollForRowIndex(int param0) {
            return Mth.clamp((float)param0 / (float)this.calculateRowCount(), 0.0F, 1.0F);
        }

        protected float subtractInputFromScroll(float param0, double param1) {
            return Mth.clamp(param0 - (float)(param1 / (double)this.calculateRowCount()), 0.0F, 1.0F);
        }

        public void scrollTo(float param0) {
            int var0 = this.getRowIndexForScroll(param0);

            for(int var1 = 0; var1 < 5; ++var1) {
                for(int var2 = 0; var2 < 9; ++var2) {
                    int var3 = var2 + (var1 + var0) * 9;
                    if (var3 >= 0 && var3 < this.items.size()) {
                        CreativeModeInventoryScreen.CONTAINER.setItem(var2 + var1 * 9, this.items.get(var3));
                    } else {
                        CreativeModeInventoryScreen.CONTAINER.setItem(var2 + var1 * 9, ItemStack.EMPTY);
                    }
                }
            }

        }

        public boolean canScroll() {
            return this.items.size() > 45;
        }

        @Override
        public ItemStack quickMoveStack(Player param0, int param1) {
            if (param1 >= this.slots.size() - 9 && param1 < this.slots.size()) {
                Slot var0 = this.slots.get(param1);
                if (var0 != null && var0.hasItem()) {
                    var0.setByPlayer(ItemStack.EMPTY);
                }
            }

            return ItemStack.EMPTY;
        }

        @Override
        public boolean canTakeItemForPickAll(ItemStack param0, Slot param1) {
            return param1.container != CreativeModeInventoryScreen.CONTAINER;
        }

        @Override
        public boolean canDragTo(Slot param0) {
            return param0.container != CreativeModeInventoryScreen.CONTAINER;
        }

        @Override
        public ItemStack getCarried() {
            return this.inventoryMenu.getCarried();
        }

        @Override
        public void setCarried(ItemStack param0) {
            this.inventoryMenu.setCarried(param0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class SlotWrapper extends Slot {
        final Slot target;

        public SlotWrapper(Slot param0, int param1, int param2, int param3) {
            super(param0.container, param1, param2, param3);
            this.target = param0;
        }

        @Override
        public void onTake(Player param0, ItemStack param1) {
            this.target.onTake(param0, param1);
        }

        @Override
        public boolean mayPlace(ItemStack param0) {
            return this.target.mayPlace(param0);
        }

        @Override
        public ItemStack getItem() {
            return this.target.getItem();
        }

        @Override
        public boolean hasItem() {
            return this.target.hasItem();
        }

        @Override
        public void setByPlayer(ItemStack param0) {
            this.target.setByPlayer(param0);
        }

        @Override
        public void set(ItemStack param0) {
            this.target.set(param0);
        }

        @Override
        public void setChanged() {
            this.target.setChanged();
        }

        @Override
        public int getMaxStackSize() {
            return this.target.getMaxStackSize();
        }

        @Override
        public int getMaxStackSize(ItemStack param0) {
            return this.target.getMaxStackSize(param0);
        }

        @Nullable
        @Override
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return this.target.getNoItemIcon();
        }

        @Override
        public ItemStack remove(int param0) {
            return this.target.remove(param0);
        }

        @Override
        public boolean isActive() {
            return this.target.isActive();
        }

        @Override
        public boolean mayPickup(Player param0) {
            return this.target.mayPickup(param0);
        }
    }
}
