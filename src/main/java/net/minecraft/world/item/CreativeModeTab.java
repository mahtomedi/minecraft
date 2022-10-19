package net.minecraft.world.item;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.ItemLike;

public abstract class CreativeModeTab {
    private final int id;
    private final Component displayName;
    private String backgroundSuffix = "items.png";
    private boolean canScroll = true;
    private boolean showTitle = true;
    private ItemStack iconItemStack;
    @Nullable
    private ItemStackLinkedSet displayItems;
    @Nullable
    private ItemStackLinkedSet displayItemsSearchTab;

    public CreativeModeTab(int param0, Component param1) {
        this.id = param0;
        this.displayName = param1;
        this.iconItemStack = ItemStack.EMPTY;
    }

    public int getId() {
        return this.id;
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public ItemStack getIconItem() {
        if (this.iconItemStack.isEmpty()) {
            this.iconItemStack = this.makeIcon();
        }

        return this.iconItemStack;
    }

    public abstract ItemStack makeIcon();

    protected abstract void generateDisplayItems(FeatureFlagSet var1, CreativeModeTab.Output var2);

    public String getBackgroundSuffix() {
        return this.backgroundSuffix;
    }

    public CreativeModeTab setBackgroundSuffix(String param0) {
        this.backgroundSuffix = param0;
        return this;
    }

    public boolean showTitle() {
        return this.showTitle;
    }

    public CreativeModeTab hideTitle() {
        this.showTitle = false;
        return this;
    }

    public boolean canScroll() {
        return this.canScroll;
    }

    public CreativeModeTab hideScroll() {
        this.canScroll = false;
        return this;
    }

    public int getColumn() {
        return this.id % 6;
    }

    public boolean isTopRow() {
        return this.id < 6;
    }

    public boolean isAlignedRight() {
        return this.getColumn() == 5;
    }

    private ItemStackLinkedSet lazyBuildDisplayItems(FeatureFlagSet param0, boolean param1) {
        if (this.displayItems == null || this.displayItemsSearchTab == null) {
            CreativeModeTab.ItemDisplayBuilder var0 = new CreativeModeTab.ItemDisplayBuilder(this, param0);
            this.generateDisplayItems(param0, var0);
            this.displayItems = var0.getTabContents();
            this.displayItemsSearchTab = var0.getSearchTabContents();
        }

        return param1 ? this.displayItemsSearchTab : this.displayItems;
    }

    public ItemStackLinkedSet getDisplayItems(FeatureFlagSet param0) {
        return this.lazyBuildDisplayItems(param0, false);
    }

    public ItemStackLinkedSet getSearchTabDisplayItems(FeatureFlagSet param0) {
        return this.lazyBuildDisplayItems(param0, true);
    }

    public boolean contains(FeatureFlagSet param0, ItemStack param1) {
        return this.getSearchTabDisplayItems(param0).contains(param1);
    }

    public void invalidateDisplayListCache() {
        this.displayItems = null;
        this.displayItemsSearchTab = null;
    }

    static class ItemDisplayBuilder implements CreativeModeTab.Output {
        private final ItemStackLinkedSet tabContents = new ItemStackLinkedSet();
        private final ItemStackLinkedSet searchTabContents = new ItemStackLinkedSet();
        private final CreativeModeTab tab;
        private final FeatureFlagSet featureFlagSet;

        public ItemDisplayBuilder(CreativeModeTab param0, FeatureFlagSet param1) {
            this.tab = param0;
            this.featureFlagSet = param1;
        }

        @Override
        public void accept(ItemStack param0, CreativeModeTab.TabVisibility param1) {
            boolean var0 = this.tabContents.contains(param0) && param1 != CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY;
            if (var0) {
                throw new IllegalStateException(
                    "Accidentally adding the same item stack twice "
                        + param0.getDisplayName().getString()
                        + " to a Creative Mode Tab: "
                        + this.tab.getDisplayName().getString()
                );
            } else {
                if (param0.getItem().isEnabled(this.featureFlagSet)) {
                    switch(param1) {
                        case PARENT_AND_SEARCH_TABS:
                            this.tabContents.add(param0);
                            this.searchTabContents.add(param0);
                            break;
                        case PARENT_TAB_ONLY:
                            this.tabContents.add(param0);
                            break;
                        case SEARCH_TAB_ONLY:
                            this.searchTabContents.add(param0);
                    }
                }

            }
        }

        public ItemStackLinkedSet getTabContents() {
            return this.tabContents;
        }

        public ItemStackLinkedSet getSearchTabContents() {
            return this.searchTabContents;
        }
    }

    protected interface Output {
        void accept(ItemStack var1, CreativeModeTab.TabVisibility var2);

        default void accept(ItemStack param0) {
            this.accept(param0, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        default void accept(ItemLike param0, CreativeModeTab.TabVisibility param1) {
            this.accept(new ItemStack(param0), param1);
        }

        default void accept(ItemLike param0) {
            this.accept(new ItemStack(param0), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        default void acceptAll(Collection<ItemStack> param0, CreativeModeTab.TabVisibility param1) {
            param0.forEach(param1x -> this.accept(param1x, param1));
        }

        default void acceptAll(Collection<ItemStack> param0) {
            this.acceptAll(param0, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    protected static enum TabVisibility {
        PARENT_AND_SEARCH_TABS,
        PARENT_TAB_ONLY,
        SEARCH_TAB_ONLY;
    }
}
