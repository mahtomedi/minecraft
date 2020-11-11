package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.HotbarManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreativeModeInventoryScreen extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
    private static final ResourceLocation CREATIVE_TABS_LOCATION = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
    private static final SimpleContainer CONTAINER = new SimpleContainer(45);
    private static final Component TRASH_SLOT_TOOLTIP = new TranslatableComponent("inventory.binSlot");
    private static int selectedTab = CreativeModeTab.TAB_BUILDING_BLOCKS.getId();
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
    private final Map<ResourceLocation, Tag<Item>> visibleTags = Maps.newTreeMap();

    public CreativeModeInventoryScreen(Player param0) {
        super(new CreativeModeInventoryScreen.ItemPickerMenu(param0), param0.getInventory(), TextComponent.EMPTY);
        param0.containerMenu = this.menu;
        this.passEvents = true;
        this.imageHeight = 136;
        this.imageWidth = 195;
    }

    @Override
    public void tick() {
        if (!this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
        } else if (this.searchBox != null) {
            this.searchBox.tick();
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
        if (param0 == null && selectedTab != CreativeModeTab.TAB_INVENTORY.getId() && param3 != ClickType.QUICK_CRAFT) {
            Inventory var15 = this.minecraft.player.getInventory();
            if (!var15.getCarried().isEmpty() && this.hasClickedOutside) {
                if (param2 == 0) {
                    this.minecraft.player.drop(var15.getCarried(), true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop(var15.getCarried());
                    var15.setCarried(ItemStack.EMPTY);
                }

                if (param2 == 1) {
                    ItemStack var16 = var15.getCarried().split(1);
                    this.minecraft.player.drop(var16, true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop(var16);
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
            } else if (selectedTab == CreativeModeTab.TAB_INVENTORY.getId()) {
                if (param0 == this.destroyItemSlot) {
                    this.minecraft.player.getInventory().setCarried(ItemStack.EMPTY);
                } else if (param3 == ClickType.THROW && param0 != null && param0.hasItem()) {
                    ItemStack var2 = param0.remove(param2 == 0 ? 1 : param0.getItem().getMaxStackSize());
                    ItemStack var3 = param0.getItem();
                    this.minecraft.player.drop(var2, true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop(var2);
                    this.minecraft.gameMode.handleCreativeModeItemAdd(var3, ((CreativeModeInventoryScreen.SlotWrapper)param0).target.index);
                } else if (param3 == ClickType.THROW && !this.minecraft.player.getInventory().getCarried().isEmpty()) {
                    this.minecraft.player.drop(this.minecraft.player.getInventory().getCarried(), true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop(this.minecraft.player.getInventory().getCarried());
                    this.minecraft.player.getInventory().setCarried(ItemStack.EMPTY);
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
                Inventory var4 = this.minecraft.player.getInventory();
                ItemStack var5 = var4.getCarried();
                ItemStack var6 = param0.getItem();
                if (param3 == ClickType.SWAP) {
                    if (!var6.isEmpty()) {
                        ItemStack var7 = var6.copy();
                        var7.setCount(var7.getMaxStackSize());
                        this.minecraft.player.getInventory().setItem(param2, var7);
                        this.minecraft.player.inventoryMenu.broadcastChanges();
                    }

                    return;
                }

                if (param3 == ClickType.CLONE) {
                    if (var4.getCarried().isEmpty() && param0.hasItem()) {
                        ItemStack var8 = param0.getItem().copy();
                        var8.setCount(var8.getMaxStackSize());
                        var4.setCarried(var8);
                    }

                    return;
                }

                if (param3 == ClickType.THROW) {
                    if (!var6.isEmpty()) {
                        ItemStack var9 = var6.copy();
                        var9.setCount(param2 == 0 ? 1 : var9.getMaxStackSize());
                        this.minecraft.player.drop(var9, true);
                        this.minecraft.gameMode.handleCreativeModeItemDrop(var9);
                    }

                    return;
                }

                if (!var5.isEmpty() && !var6.isEmpty() && var5.sameItem(var6) && ItemStack.tagMatches(var5, var6)) {
                    if (param2 == 0) {
                        if (var0) {
                            var5.setCount(var5.getMaxStackSize());
                        } else if (var5.getCount() < var5.getMaxStackSize()) {
                            var5.grow(1);
                        }
                    } else {
                        var5.shrink(1);
                    }
                } else if (!var6.isEmpty() && var5.isEmpty()) {
                    var4.setCarried(var6.copy());
                    var5 = var4.getCarried();
                    if (var0) {
                        var5.setCount(var5.getMaxStackSize());
                    }
                } else if (param2 == 0) {
                    var4.setCarried(ItemStack.EMPTY);
                } else {
                    var4.getCarried().shrink(1);
                }
            } else if (this.menu != null) {
                ItemStack var10 = param0 == null ? ItemStack.EMPTY : this.menu.getSlot(param0.index).getItem();
                this.menu.clicked(param0 == null ? param1 : param0.index, param2, param3, this.minecraft.player);
                if (AbstractContainerMenu.getQuickcraftHeader(param2) == 2) {
                    for(int var11 = 0; var11 < 9; ++var11) {
                        this.minecraft.gameMode.handleCreativeModeItemAdd(this.menu.getSlot(45 + var11).getItem(), 36 + var11);
                    }
                } else if (param0 != null) {
                    ItemStack var12 = this.menu.getSlot(param0.index).getItem();
                    this.minecraft.gameMode.handleCreativeModeItemAdd(var12, param0.index - this.menu.slots.size() + 9 + 36);
                    int var13 = 45 + param2;
                    if (param3 == ClickType.SWAP) {
                        this.minecraft.gameMode.handleCreativeModeItemAdd(var10, var13 - this.menu.slots.size() + 9 + 36);
                    } else if (param3 == ClickType.THROW && !var10.isEmpty()) {
                        ItemStack var14 = var10.copy();
                        var14.setCount(param2 == 0 ? 1 : var14.getMaxStackSize());
                        this.minecraft.player.drop(var14, true);
                        this.minecraft.gameMode.handleCreativeModeItemDrop(var14);
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
    protected void checkEffectRendering() {
        int var0 = this.leftPos;
        super.checkEffectRendering();
        if (this.searchBox != null && this.leftPos != var0) {
            this.searchBox.setX(this.leftPos + 82);
        }

    }

    @Override
    protected void init() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            super.init();
            this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
            this.searchBox = new EditBox(this.font, this.leftPos + 82, this.topPos + 6, 80, 9, new TranslatableComponent("itemGroup.search"));
            this.searchBox.setMaxLength(50);
            this.searchBox.setBordered(false);
            this.searchBox.setVisible(false);
            this.searchBox.setTextColor(16777215);
            this.children.add(this.searchBox);
            int var0 = selectedTab;
            selectedTab = -1;
            this.selectTab(CreativeModeTab.TABS[var0]);
            this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
            this.listener = new CreativeInventoryListener(this.minecraft);
            this.minecraft.player.inventoryMenu.addSlotListener(this.listener);
        } else {
            this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
        }

    }

    @Override
    public void resize(Minecraft param0, int param1, int param2) {
        String var0 = this.searchBox.getValue();
        this.init(param0, param1, param2);
        this.searchBox.setValue(var0);
        if (!this.searchBox.getValue().isEmpty()) {
            this.refreshSearchResults();
        }

    }

    @Override
    public void removed() {
        super.removed();
        if (this.minecraft.player != null && this.minecraft.player.getInventory() != null) {
            this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
        }

        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean charTyped(char param0, int param1) {
        if (this.ignoreTextInput) {
            return false;
        } else if (selectedTab != CreativeModeTab.TAB_SEARCH.getId()) {
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
        if (selectedTab != CreativeModeTab.TAB_SEARCH.getId()) {
            if (this.minecraft.options.keyChat.matches(param0, param1)) {
                this.ignoreTextInput = true;
                this.selectTab(CreativeModeTab.TAB_SEARCH);
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
            for(Item var1 : Registry.ITEM) {
                var1.fillItemCategory(CreativeModeTab.TAB_SEARCH, this.menu.items);
            }
        } else {
            SearchTree<ItemStack> var2;
            if (var0.startsWith("#")) {
                var0 = var0.substring(1);
                var2 = this.minecraft.getSearchTree(SearchRegistry.CREATIVE_TAGS);
                this.updateVisibleTags(var0);
            } else {
                var2 = this.minecraft.getSearchTree(SearchRegistry.CREATIVE_NAMES);
            }

            this.menu.items.addAll(var2.search(var0.toLowerCase(Locale.ROOT)));
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

        TagCollection<Item> var5 = ItemTags.getAllTags();
        var5.getAvailableTags().stream().filter(var1).forEach(param1 -> {
        });
    }

    @Override
    protected void renderLabels(PoseStack param0, int param1, int param2) {
        CreativeModeTab var0 = CreativeModeTab.TABS[selectedTab];
        if (var0.showTitle()) {
            RenderSystem.disableBlend();
            this.font.draw(param0, var0.getDisplayName(), 8.0F, 6.0F, 4210752);
        }

    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (param2 == 0) {
            double var0 = param0 - (double)this.leftPos;
            double var1 = param1 - (double)this.topPos;

            for(CreativeModeTab var2 : CreativeModeTab.TABS) {
                if (this.checkTabClicked(var2, var0, var1)) {
                    return true;
                }
            }

            if (selectedTab != CreativeModeTab.TAB_INVENTORY.getId() && this.insideScrollbar(param0, param1)) {
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

            for(CreativeModeTab var2 : CreativeModeTab.TABS) {
                if (this.checkTabClicked(var2, var0, var1)) {
                    this.selectTab(var2);
                    return true;
                }
            }
        }

        return super.mouseReleased(param0, param1, param2);
    }

    private boolean canScroll() {
        return selectedTab != CreativeModeTab.TAB_INVENTORY.getId() && CreativeModeTab.TABS[selectedTab].canScroll() && this.menu.canScroll();
    }

    private void selectTab(CreativeModeTab param0) {
        int var0 = selectedTab;
        selectedTab = param0.getId();
        this.quickCraftSlots.clear();
        this.menu.items.clear();
        if (param0 == CreativeModeTab.TAB_HOTBAR) {
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
                            var5.setHoverName(new TranslatableComponent("inventory.hotbarInfo", var7, var6));
                            this.menu.items.add(var5);
                        } else {
                            this.menu.items.add(ItemStack.EMPTY);
                        }
                    }
                } else {
                    this.menu.items.addAll(var3);
                }
            }
        } else if (param0 != CreativeModeTab.TAB_SEARCH) {
            param0.fillItemList(this.menu.items);
        }

        if (param0 == CreativeModeTab.TAB_INVENTORY) {
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
        } else if (var0 == CreativeModeTab.TAB_INVENTORY.getId()) {
            this.menu.slots.clear();
            this.menu.slots.addAll(this.originalSlots);
            this.originalSlots = null;
        }

        if (this.searchBox != null) {
            if (param0 == CreativeModeTab.TAB_SEARCH) {
                this.searchBox.setVisible(true);
                this.searchBox.setCanLoseFocus(false);
                this.searchBox.setFocus(true);
                if (var0 != param0.getId()) {
                    this.searchBox.setValue("");
                }

                this.refreshSearchResults();
            } else {
                this.searchBox.setVisible(false);
                this.searchBox.setCanLoseFocus(true);
                this.searchBox.setFocus(false);
                this.searchBox.setValue("");
            }
        }

        this.scrollOffs = 0.0F;
        this.menu.scrollTo(0.0F);
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2) {
        if (!this.canScroll()) {
            return false;
        } else {
            int var0 = (this.menu.items.size() + 9 - 1) / 9 - 5;
            this.scrollOffs = (float)((double)this.scrollOffs - param2 / (double)var0);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
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
        this.hasClickedOutside = var0 && !this.checkTabClicked(CreativeModeTab.TABS[selectedTab], param0, param1);
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
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        super.render(param0, param1, param2, param3);

        for(CreativeModeTab var0 : CreativeModeTab.TABS) {
            if (this.checkTabHovering(param0, var0, param1, param2)) {
                break;
            }
        }

        if (this.destroyItemSlot != null
            && selectedTab == CreativeModeTab.TAB_INVENTORY.getId()
            && this.isHovering(this.destroyItemSlot.x, this.destroyItemSlot.y, 16, 16, (double)param1, (double)param2)) {
            this.renderTooltip(param0, TRASH_SLOT_TOOLTIP, param1, param2);
        }

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderTooltip(param0, param1, param2);
    }

    @Override
    protected void renderTooltip(PoseStack param0, ItemStack param1, int param2, int param3) {
        if (selectedTab == CreativeModeTab.TAB_SEARCH.getId()) {
            List<Component> var0 = param1.getTooltipLines(
                this.minecraft.player, this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL
            );
            List<Component> var1 = Lists.newArrayList(var0);
            Item var2 = param1.getItem();
            CreativeModeTab var3 = var2.getItemCategory();
            if (var3 == null && param1.is(Items.ENCHANTED_BOOK)) {
                Map<Enchantment, Integer> var4 = EnchantmentHelper.getEnchantments(param1);
                if (var4.size() == 1) {
                    Enchantment var5 = var4.keySet().iterator().next();

                    for(CreativeModeTab var6 : CreativeModeTab.TABS) {
                        if (var6.hasEnchantmentCategory(var5.category)) {
                            var3 = var6;
                            break;
                        }
                    }
                }
            }

            this.visibleTags.forEach((param2x, param3x) -> {
                if (param1.is(param3x)) {
                    var1.add(1, new TextComponent("#" + param2x).withStyle(ChatFormatting.DARK_PURPLE));
                }

            });
            if (var3 != null) {
                var1.add(1, var3.getDisplayName().copy().withStyle(ChatFormatting.BLUE));
            }

            this.renderTooltip(param0, var1, param1.getTooltipImage(), param2, param3);
        } else {
            super.renderTooltip(param0, param1, param2, param3);
        }

    }

    @Override
    protected void renderBg(PoseStack param0, float param1, int param2, int param3) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        CreativeModeTab var0 = CreativeModeTab.TABS[selectedTab];

        for(CreativeModeTab var1 : CreativeModeTab.TABS) {
            this.minecraft.getTextureManager().bind(CREATIVE_TABS_LOCATION);
            if (var1.getId() != selectedTab) {
                this.renderTabButton(param0, var1);
            }
        }

        this.minecraft.getTextureManager().bind(new ResourceLocation("textures/gui/container/creative_inventory/tab_" + var0.getBackgroundSuffix()));
        this.blit(param0, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        this.searchBox.render(param0, param2, param3, param1);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int var2 = this.leftPos + 175;
        int var3 = this.topPos + 18;
        int var4 = var3 + 112;
        this.minecraft.getTextureManager().bind(CREATIVE_TABS_LOCATION);
        if (var0.canScroll()) {
            this.blit(param0, var2, var3 + (int)((float)(var4 - var3 - 17) * this.scrollOffs), 232 + (this.canScroll() ? 0 : 12), 0, 12, 15);
        }

        this.renderTabButton(param0, var0);
        if (var0 == CreativeModeTab.TAB_INVENTORY) {
            InventoryScreen.renderEntityInInventory(
                this.leftPos + 88, this.topPos + 45, 20, (float)(this.leftPos + 88 - param2), (float)(this.topPos + 45 - 30 - param3), this.minecraft.player
            );
        }

    }

    protected boolean checkTabClicked(CreativeModeTab param0, double param1, double param2) {
        int var0 = param0.getColumn();
        int var1 = 28 * var0;
        int var2 = 0;
        if (param0.isAlignedRight()) {
            var1 = this.imageWidth - 28 * (6 - var0) + 2;
        } else if (var0 > 0) {
            var1 += var0;
        }

        if (param0.isTopRow()) {
            var2 -= 32;
        } else {
            var2 += this.imageHeight;
        }

        return param1 >= (double)var1 && param1 <= (double)(var1 + 28) && param2 >= (double)var2 && param2 <= (double)(var2 + 32);
    }

    protected boolean checkTabHovering(PoseStack param0, CreativeModeTab param1, int param2, int param3) {
        int var0 = param1.getColumn();
        int var1 = 28 * var0;
        int var2 = 0;
        if (param1.isAlignedRight()) {
            var1 = this.imageWidth - 28 * (6 - var0) + 2;
        } else if (var0 > 0) {
            var1 += var0;
        }

        if (param1.isTopRow()) {
            var2 -= 32;
        } else {
            var2 += this.imageHeight;
        }

        if (this.isHovering(var1 + 3, var2 + 3, 23, 27, (double)param2, (double)param3)) {
            this.renderTooltip(param0, param1.getDisplayName(), param2, param3);
            return true;
        } else {
            return false;
        }
    }

    protected void renderTabButton(PoseStack param0, CreativeModeTab param1) {
        boolean var0 = param1.getId() == selectedTab;
        boolean var1 = param1.isTopRow();
        int var2 = param1.getColumn();
        int var3 = var2 * 28;
        int var4 = 0;
        int var5 = this.leftPos + 28 * var2;
        int var6 = this.topPos;
        int var7 = 32;
        if (var0) {
            var4 += 32;
        }

        if (param1.isAlignedRight()) {
            var5 = this.leftPos + this.imageWidth - 28 * (6 - var2);
        } else if (var2 > 0) {
            var5 += var2;
        }

        if (var1) {
            var6 -= 28;
        } else {
            var4 += 64;
            var6 += this.imageHeight - 4;
        }

        this.blit(param0, var5, var6, var3, var4, 28, 32);
        this.itemRenderer.blitOffset = 100.0F;
        var5 += 6;
        var6 += 8 + (var1 ? 1 : -1);
        RenderSystem.enableRescaleNormal();
        ItemStack var8 = param1.getIconItem();
        this.itemRenderer.renderAndDecorateItem(var8, var5, var6);
        this.itemRenderer.renderGuiItemDecorations(this.font, var8, var5, var6);
        this.itemRenderer.blitOffset = 0.0F;
    }

    public int getSelectedTab() {
        return selectedTab;
    }

    public static void handleHotbarLoadOrSave(Minecraft param0, int param1, boolean param2, boolean param3) {
        LocalPlayer var0 = param0.player;
        HotbarManager var1 = param0.getHotbarManager();
        Hotbar var2 = var1.get(param1);
        if (param2) {
            for(int var3 = 0; var3 < Inventory.getSelectionSize(); ++var3) {
                ItemStack var4 = var2.get(var3).copy();
                var0.getInventory().setItem(var3, var4);
                param0.gameMode.handleCreativeModeItemAdd(var4, 36 + var3);
            }

            var0.inventoryMenu.broadcastChanges();
        } else if (param3) {
            for(int var5 = 0; var5 < Inventory.getSelectionSize(); ++var5) {
                var2.set(var5, var0.getInventory().getItem(var5).copy());
            }

            Component var6 = param0.options.keyHotbarSlots[param1].getTranslatedKeyMessage();
            Component var7 = param0.options.keyLoadHotbarActivator.getTranslatedKeyMessage();
            param0.gui.setOverlayMessage(new TranslatableComponent("inventory.hotbarSaved", var7, var6), false);
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
            if (super.mayPickup(param0) && this.hasItem()) {
                return this.getItem().getTagElement("CustomCreativeLock") == null;
            } else {
                return !this.hasItem();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ItemPickerMenu extends AbstractContainerMenu {
        public final NonNullList<ItemStack> items = NonNullList.create();

        public ItemPickerMenu(Player param0) {
            super(null, 0);
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

        public void scrollTo(float param0) {
            int var0 = (this.items.size() + 9 - 1) / 9 - 5;
            int var1 = (int)((double)(param0 * (float)var0) + 0.5);
            if (var1 < 0) {
                var1 = 0;
            }

            for(int var2 = 0; var2 < 5; ++var2) {
                for(int var3 = 0; var3 < 9; ++var3) {
                    int var4 = var3 + (var2 + var1) * 9;
                    if (var4 >= 0 && var4 < this.items.size()) {
                        CreativeModeInventoryScreen.CONTAINER.setItem(var3 + var2 * 9, this.items.get(var4));
                    } else {
                        CreativeModeInventoryScreen.CONTAINER.setItem(var3 + var2 * 9, ItemStack.EMPTY);
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
                    var0.set(ItemStack.EMPTY);
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
    }

    @OnlyIn(Dist.CLIENT)
    static class SlotWrapper extends Slot {
        private final Slot target;

        public SlotWrapper(Slot param0, int param1, int param2, int param3) {
            super(param0.container, param1, param2, param3);
            this.target = param0;
        }

        @Override
        public ItemStack onTake(Player param0, ItemStack param1) {
            return this.target.onTake(param0, param1);
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
