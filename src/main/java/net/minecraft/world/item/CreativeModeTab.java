package net.minecraft.world.item;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
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
    @Nullable
    private CreativeModeTab.ItemDisplayParameters cachedParameters;
    private boolean searchTreeDirty;
    @Nullable
    private Consumer<List<ItemStack>> searchTreeRebuilder;

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

    protected abstract void generateDisplayItems(FeatureFlagSet var1, CreativeModeTab.Output var2, boolean var3);

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

    private ItemStackLinkedSet lazyBuildDisplayItems(FeatureFlagSet param0, boolean param1, boolean param2) {
        CreativeModeTab.ItemDisplayParameters var0 = new CreativeModeTab.ItemDisplayParameters(param0, param2);
        boolean var1 = this.displayItems == null || this.displayItemsSearchTab == null || !Objects.equals(this.cachedParameters, var0);
        if (var1) {
            CreativeModeTab.ItemDisplayBuilder var2 = new CreativeModeTab.ItemDisplayBuilder(this, param0);
            this.generateDisplayItems(param0, var2, param2);
            this.displayItems = var2.getTabContents();
            this.displayItemsSearchTab = var2.getSearchTabContents();
            this.cachedParameters = var0;
        }

        if (this.searchTreeRebuilder != null && (var1 || this.searchTreeDirty)) {
            this.searchTreeRebuilder.accept(Lists.newArrayList(this.displayItemsSearchTab));
            this.markSearchTreeRebuilt();
        }

        return param1 ? this.displayItemsSearchTab : this.displayItems;
    }

    public ItemStackLinkedSet getDisplayItems(FeatureFlagSet param0, boolean param1) {
        return this.lazyBuildDisplayItems(param0, false, param1);
    }

    public ItemStackLinkedSet getSearchTabDisplayItems(FeatureFlagSet param0, boolean param1) {
        return this.lazyBuildDisplayItems(param0, true, param1);
    }

    public boolean contains(FeatureFlagSet param0, ItemStack param1, boolean param2) {
        return this.getSearchTabDisplayItems(param0, param2).contains(param1);
    }

    public void setSearchTreeRebuilder(Consumer<List<ItemStack>> param0) {
        this.searchTreeRebuilder = param0;
    }

    public void invalidateSearchTree() {
        this.searchTreeDirty = true;
    }

    private void markSearchTreeRebuilt() {
        this.searchTreeDirty = false;
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

    static record ItemDisplayParameters(FeatureFlagSet enabledFeatures, boolean hasPermissions) {
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
