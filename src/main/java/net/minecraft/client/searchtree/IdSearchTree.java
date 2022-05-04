package net.minecraft.client.searchtree;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IdSearchTree<T> implements RefreshableSearchTree<T> {
    protected final Comparator<T> additionOrder;
    protected final ResourceLocationSearchTree<T> resourceLocationSearchTree;

    public IdSearchTree(Function<T, Stream<ResourceLocation>> param0, List<T> param1) {
        ToIntFunction<T> var0 = Util.createIndexLookup(param1);
        this.additionOrder = Comparator.comparingInt(var0);
        this.resourceLocationSearchTree = ResourceLocationSearchTree.create(param1, param0);
    }

    @Override
    public List<T> search(String param0) {
        int var0 = param0.indexOf(58);
        return var0 == -1 ? this.searchPlainText(param0) : this.searchResourceLocation(param0.substring(0, var0).trim(), param0.substring(var0 + 1).trim());
    }

    protected List<T> searchPlainText(String param0) {
        return this.resourceLocationSearchTree.searchPath(param0);
    }

    protected List<T> searchResourceLocation(String param0, String param1) {
        List<T> var0 = this.resourceLocationSearchTree.searchNamespace(param0);
        List<T> var1 = this.resourceLocationSearchTree.searchPath(param1);
        return ImmutableList.copyOf(new IntersectionIterator<>(var0.iterator(), var1.iterator(), this.additionOrder));
    }
}
