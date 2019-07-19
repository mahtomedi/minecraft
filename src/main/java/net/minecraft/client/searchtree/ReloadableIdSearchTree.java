package net.minecraft.client.searchtree;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ReloadableIdSearchTree<T> implements MutableSearchTree<T> {
    protected SuffixArray<T> namespaceTree = new SuffixArray<>();
    protected SuffixArray<T> pathTree = new SuffixArray<>();
    private final Function<T, Stream<ResourceLocation>> idGetter;
    private final List<T> contents = Lists.newArrayList();
    private final Object2IntMap<T> orderT = new Object2IntOpenHashMap<>();

    public ReloadableIdSearchTree(Function<T, Stream<ResourceLocation>> param0) {
        this.idGetter = param0;
    }

    @Override
    public void refresh() {
        this.namespaceTree = new SuffixArray<>();
        this.pathTree = new SuffixArray<>();

        for(T var0 : this.contents) {
            this.index(var0);
        }

        this.namespaceTree.generate();
        this.pathTree.generate();
    }

    @Override
    public void add(T param0) {
        this.orderT.put(param0, this.contents.size());
        this.contents.add(param0);
        this.index(param0);
    }

    @Override
    public void clear() {
        this.contents.clear();
        this.orderT.clear();
    }

    protected void index(T param0) {
        this.idGetter.apply(param0).forEach(param1 -> {
            this.namespaceTree.add(param0, param1.getNamespace().toLowerCase(Locale.ROOT));
            this.pathTree.add(param0, param1.getPath().toLowerCase(Locale.ROOT));
        });
    }

    protected int comparePosition(T param0, T param1) {
        return Integer.compare(this.orderT.getInt(param0), this.orderT.getInt(param1));
    }

    @Override
    public List<T> search(String param0) {
        int var0 = param0.indexOf(58);
        if (var0 == -1) {
            return this.pathTree.search(param0);
        } else {
            List<T> var1 = this.namespaceTree.search(param0.substring(0, var0).trim());
            String var2 = param0.substring(var0 + 1).trim();
            List<T> var3 = this.pathTree.search(var2);
            return Lists.newArrayList(new ReloadableIdSearchTree.IntersectionIterator<>(var1.iterator(), var3.iterator(), this::comparePosition));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class IntersectionIterator<T> extends AbstractIterator<T> {
        private final PeekingIterator<T> firstIterator;
        private final PeekingIterator<T> secondIterator;
        private final Comparator<T> orderT;

        public IntersectionIterator(Iterator<T> param0, Iterator<T> param1, Comparator<T> param2) {
            this.firstIterator = Iterators.peekingIterator(param0);
            this.secondIterator = Iterators.peekingIterator(param1);
            this.orderT = param2;
        }

        @Override
        protected T computeNext() {
            while(this.firstIterator.hasNext() && this.secondIterator.hasNext()) {
                int var0 = this.orderT.compare(this.firstIterator.peek(), this.secondIterator.peek());
                if (var0 == 0) {
                    this.secondIterator.next();
                    return this.firstIterator.next();
                }

                if (var0 < 0) {
                    this.firstIterator.next();
                } else {
                    this.secondIterator.next();
                }
            }

            return this.endOfData();
        }
    }
}
