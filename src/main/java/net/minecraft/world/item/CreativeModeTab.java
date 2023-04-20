package net.minecraft.world.item;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.ItemLike;

public class CreativeModeTab {
    private final Component displayName;
    String backgroundSuffix = "items.png";
    boolean canScroll = true;
    boolean showTitle = true;
    boolean alignedRight = false;
    private final CreativeModeTab.Row row;
    private final int column;
    private final CreativeModeTab.Type type;
    @Nullable
    private ItemStack iconItemStack;
    private Collection<ItemStack> displayItems = ItemStackLinkedSet.createTypeAndTagSet();
    private Set<ItemStack> displayItemsSearchTab = ItemStackLinkedSet.createTypeAndTagSet();
    @Nullable
    private Consumer<List<ItemStack>> searchTreeBuilder;
    private final Supplier<ItemStack> iconGenerator;
    private final CreativeModeTab.DisplayItemsGenerator displayItemsGenerator;

    CreativeModeTab(
        CreativeModeTab.Row param0,
        int param1,
        CreativeModeTab.Type param2,
        Component param3,
        Supplier<ItemStack> param4,
        CreativeModeTab.DisplayItemsGenerator param5
    ) {
        this.row = param0;
        this.column = param1;
        this.displayName = param3;
        this.iconGenerator = param4;
        this.displayItemsGenerator = param5;
        this.type = param2;
    }

    public static CreativeModeTab.Builder builder(CreativeModeTab.Row param0, int param1) {
        return new CreativeModeTab.Builder(param0, param1);
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public ItemStack getIconItem() {
        if (this.iconItemStack == null) {
            this.iconItemStack = this.iconGenerator.get();
        }

        return this.iconItemStack;
    }

    public String getBackgroundSuffix() {
        return this.backgroundSuffix;
    }

    public boolean showTitle() {
        return this.showTitle;
    }

    public boolean canScroll() {
        return this.canScroll;
    }

    public int column() {
        return this.column;
    }

    public CreativeModeTab.Row row() {
        return this.row;
    }

    public boolean hasAnyItems() {
        return !this.displayItems.isEmpty();
    }

    public boolean shouldDisplay() {
        return this.type != CreativeModeTab.Type.CATEGORY || this.hasAnyItems();
    }

    public boolean isAlignedRight() {
        return this.alignedRight;
    }

    public CreativeModeTab.Type getType() {
        return this.type;
    }

    public void buildContents(CreativeModeTab.ItemDisplayParameters param0) {
        CreativeModeTab.ItemDisplayBuilder var0 = new CreativeModeTab.ItemDisplayBuilder(this, param0.enabledFeatures);
        ResourceKey<CreativeModeTab> var1 = BuiltInRegistries.CREATIVE_MODE_TAB
            .getResourceKey(this)
            .orElseThrow(() -> new IllegalStateException("Unregistered creative tab: " + this));
        this.displayItemsGenerator.accept(param0, var0);
        this.displayItems = var0.tabContents;
        this.displayItemsSearchTab = var0.searchTabContents;
        this.rebuildSearchTree();
    }

    public Collection<ItemStack> getDisplayItems() {
        return this.displayItems;
    }

    public Collection<ItemStack> getSearchTabDisplayItems() {
        return this.displayItemsSearchTab;
    }

    public boolean contains(ItemStack param0) {
        return this.displayItemsSearchTab.contains(param0);
    }

    public void setSearchTreeBuilder(Consumer<List<ItemStack>> param0) {
        this.searchTreeBuilder = param0;
    }

    public void rebuildSearchTree() {
        if (this.searchTreeBuilder != null) {
            this.searchTreeBuilder.accept(Lists.newArrayList(this.displayItemsSearchTab));
        }

    }

    public static class Builder {
        private static final CreativeModeTab.DisplayItemsGenerator EMPTY_GENERATOR = (param0, param1) -> {
        };
        private final CreativeModeTab.Row row;
        private final int column;
        private Component displayName = Component.empty();
        private Supplier<ItemStack> iconGenerator = () -> ItemStack.EMPTY;
        private CreativeModeTab.DisplayItemsGenerator displayItemsGenerator = EMPTY_GENERATOR;
        private boolean canScroll = true;
        private boolean showTitle = true;
        private boolean alignedRight = false;
        private CreativeModeTab.Type type = CreativeModeTab.Type.CATEGORY;
        private String backgroundSuffix = "items.png";

        public Builder(CreativeModeTab.Row param0, int param1) {
            this.row = param0;
            this.column = param1;
        }

        public CreativeModeTab.Builder title(Component param0) {
            this.displayName = param0;
            return this;
        }

        public CreativeModeTab.Builder icon(Supplier<ItemStack> param0) {
            this.iconGenerator = param0;
            return this;
        }

        public CreativeModeTab.Builder displayItems(CreativeModeTab.DisplayItemsGenerator param0) {
            this.displayItemsGenerator = param0;
            return this;
        }

        public CreativeModeTab.Builder alignedRight() {
            this.alignedRight = true;
            return this;
        }

        public CreativeModeTab.Builder hideTitle() {
            this.showTitle = false;
            return this;
        }

        public CreativeModeTab.Builder noScrollBar() {
            this.canScroll = false;
            return this;
        }

        protected CreativeModeTab.Builder type(CreativeModeTab.Type param0) {
            this.type = param0;
            return this;
        }

        public CreativeModeTab.Builder backgroundSuffix(String param0) {
            this.backgroundSuffix = param0;
            return this;
        }

        public CreativeModeTab build() {
            if ((this.type == CreativeModeTab.Type.HOTBAR || this.type == CreativeModeTab.Type.INVENTORY) && this.displayItemsGenerator != EMPTY_GENERATOR) {
                throw new IllegalStateException("Special tabs can't have display items");
            } else {
                CreativeModeTab var0 = new CreativeModeTab(this.row, this.column, this.type, this.displayName, this.iconGenerator, this.displayItemsGenerator);
                var0.alignedRight = this.alignedRight;
                var0.showTitle = this.showTitle;
                var0.canScroll = this.canScroll;
                var0.backgroundSuffix = this.backgroundSuffix;
                return var0;
            }
        }
    }

    @FunctionalInterface
    public interface DisplayItemsGenerator {
        void accept(CreativeModeTab.ItemDisplayParameters var1, CreativeModeTab.Output var2);
    }

    static class ItemDisplayBuilder implements CreativeModeTab.Output {
        public final Collection<ItemStack> tabContents = ItemStackLinkedSet.createTypeAndTagSet();
        public final Set<ItemStack> searchTabContents = ItemStackLinkedSet.createTypeAndTagSet();
        private final CreativeModeTab tab;
        private final FeatureFlagSet featureFlagSet;

        public ItemDisplayBuilder(CreativeModeTab param0, FeatureFlagSet param1) {
            this.tab = param0;
            this.featureFlagSet = param1;
        }

        @Override
        public void accept(ItemStack param0, CreativeModeTab.TabVisibility param1) {
            if (param0.getCount() != 1) {
                throw new IllegalArgumentException("Stack size must be exactly 1");
            } else {
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
        }
    }

    public static record ItemDisplayParameters(FeatureFlagSet enabledFeatures, boolean hasPermissions, HolderLookup.Provider holders) {
        public boolean needsUpdate(FeatureFlagSet param0, boolean param1, HolderLookup.Provider param2) {
            return !this.enabledFeatures.equals(param0) || this.hasPermissions != param1 || this.holders != param2;
        }
    }

    public interface Output {
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

    public static enum Row {
        TOP,
        BOTTOM;
    }

    protected static enum TabVisibility {
        PARENT_AND_SEARCH_TABS,
        PARENT_TAB_ONLY,
        SEARCH_TAB_ONLY;
    }

    public static enum Type {
        CATEGORY,
        INVENTORY,
        HOTBAR,
        SEARCH;
    }
}
