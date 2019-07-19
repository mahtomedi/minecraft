package net.minecraft.client.searchtree;

import com.google.common.collect.Maps;
import java.util.Map;
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
    private final Map<SearchRegistry.Key<?>, MutableSearchTree<?>> searchTrees = Maps.newHashMap();

    @Override
    public void onResourceManagerReload(ResourceManager param0) {
        for(MutableSearchTree<?> var0 : this.searchTrees.values()) {
            var0.refresh();
        }

    }

    public <T> void register(SearchRegistry.Key<T> param0, MutableSearchTree<T> param1) {
        this.searchTrees.put(param0, param1);
    }

    public <T> MutableSearchTree<T> getTree(SearchRegistry.Key<T> param0) {
        return (MutableSearchTree<T>)this.searchTrees.get(param0);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Key<T> {
    }
}
