package net.minecraft.client.searchtree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SearchRegistry implements ResourceManagerReloadListener {
    public static final SearchRegistry.Key<ItemStack> CREATIVE_NAMES = new SearchRegistry.Key<>();
    public static final SearchRegistry.Key<ItemStack> CREATIVE_TAGS = new SearchRegistry.Key<>();
    public static final SearchRegistry.Key<RecipeCollection> RECIPE_COLLECTIONS = new SearchRegistry.Key<>();
    private final Map<SearchRegistry.Key<?>, SearchRegistry.TreeEntry<?>> searchTrees = new HashMap<>();

    @Override
    public void onResourceManagerReload(ResourceManager param0) {
        for(SearchRegistry.TreeEntry<?> var0 : this.searchTrees.values()) {
            var0.refresh();
        }

    }

    public <T> void register(SearchRegistry.Key<T> param0, SearchRegistry.TreeBuilderSupplier<T> param1) {
        this.searchTrees.put(param0, new SearchRegistry.TreeEntry<>(param1));
    }

    private <T> SearchRegistry.TreeEntry<T> getSupplier(SearchRegistry.Key<T> param0) {
        SearchRegistry.TreeEntry<T> var0 = (SearchRegistry.TreeEntry)this.searchTrees.get(param0);
        if (var0 == null) {
            throw new IllegalStateException("Tree builder not registered");
        } else {
            return var0;
        }
    }

    public <T> void populate(SearchRegistry.Key<T> param0, List<T> param1) {
        this.getSupplier(param0).populate(param1);
    }

    public <T> SearchTree<T> getTree(SearchRegistry.Key<T> param0) {
        return this.getSupplier(param0).tree;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Key<T> {
    }

    @OnlyIn(Dist.CLIENT)
    public interface TreeBuilderSupplier<T> extends Function<List<T>, RefreshableSearchTree<T>> {
    }

    @OnlyIn(Dist.CLIENT)
    static class TreeEntry<T> {
        private final SearchRegistry.TreeBuilderSupplier<T> factory;
        RefreshableSearchTree<T> tree = RefreshableSearchTree.empty();

        TreeEntry(SearchRegistry.TreeBuilderSupplier<T> param0) {
            this.factory = param0;
        }

        void populate(List<T> param0) {
            this.tree = this.factory.apply((T)param0);
            this.tree.refresh();
        }

        void refresh() {
            this.tree.refresh();
        }
    }
}
