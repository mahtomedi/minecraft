package net.minecraft.client.searchtree;

import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FullTextSearchTree<T> extends IdSearchTree<T> {
    private final List<T> contents;
    private final Function<T, Stream<String>> filler;
    private PlainTextSearchTree<T> plainTextSearchTree = PlainTextSearchTree.empty();

    public FullTextSearchTree(Function<T, Stream<String>> param0, Function<T, Stream<ResourceLocation>> param1, List<T> param2) {
        super(param1, param2);
        this.contents = param2;
        this.filler = param0;
    }

    @Override
    public void refresh() {
        super.refresh();
        this.plainTextSearchTree = PlainTextSearchTree.create(this.contents, this.filler);
    }

    @Override
    protected List<T> searchPlainText(String param0) {
        return this.plainTextSearchTree.search(param0);
    }

    @Override
    protected List<T> searchResourceLocation(String param0, String param1) {
        List<T> var0 = this.resourceLocationSearchTree.searchNamespace(param0);
        List<T> var1 = this.resourceLocationSearchTree.searchPath(param1);
        List<T> var2 = this.plainTextSearchTree.search(param1);
        Iterator<T> var3 = new MergingUniqueIterator<>(var1.iterator(), var2.iterator(), this.additionOrder);
        return ImmutableList.copyOf(new IntersectionIterator<>(var0.iterator(), var3, this.additionOrder));
    }
}
