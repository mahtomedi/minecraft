package net.minecraft.client.searchtree;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;
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
public class ReloadableSearchTree<T> extends ReloadableIdSearchTree<T> {
    protected SuffixArray<T> tree = new SuffixArray<>();
    private final Function<T, Stream<String>> filler;

    public ReloadableSearchTree(Function<T, Stream<String>> param0, Function<T, Stream<ResourceLocation>> param1) {
        super(param1);
        this.filler = param0;
    }

    @Override
    public void refresh() {
        this.tree = new SuffixArray<>();
        super.refresh();
        this.tree.generate();
    }

    @Override
    protected void index(T param0) {
        super.index(param0);
        this.filler.apply(param0).forEach(param1 -> this.tree.add(param0, param1.toLowerCase(Locale.ROOT)));
    }

    @Override
    public List<T> search(String param0) {
        int var0 = param0.indexOf(58);
        if (var0 < 0) {
            return this.tree.search(param0);
        } else {
            List<T> var1 = this.namespaceTree.search(param0.substring(0, var0).trim());
            String var2 = param0.substring(var0 + 1).trim();
            List<T> var3 = this.pathTree.search(var2);
            List<T> var4 = this.tree.search(var2);
            return Lists.newArrayList(
                new ReloadableIdSearchTree.IntersectionIterator<>(
                    var1.iterator(),
                    new ReloadableSearchTree.MergingUniqueIterator<>(var3.iterator(), var4.iterator(), this::comparePosition),
                    this::comparePosition
                )
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class MergingUniqueIterator<T> extends AbstractIterator<T> {
        private final PeekingIterator<T> firstIterator;
        private final PeekingIterator<T> secondIterator;
        private final Comparator<T> orderT;

        public MergingUniqueIterator(Iterator<T> param0, Iterator<T> param1, Comparator<T> param2) {
            this.firstIterator = Iterators.peekingIterator(param0);
            this.secondIterator = Iterators.peekingIterator(param1);
            this.orderT = param2;
        }

        @Override
        protected T computeNext() {
            boolean var0 = !this.firstIterator.hasNext();
            boolean var1 = !this.secondIterator.hasNext();
            if (var0 && var1) {
                return this.endOfData();
            } else if (var0) {
                return this.secondIterator.next();
            } else if (var1) {
                return this.firstIterator.next();
            } else {
                int var2 = this.orderT.compare(this.firstIterator.peek(), this.secondIterator.peek());
                if (var2 == 0) {
                    this.secondIterator.next();
                }

                return (T)(var2 <= 0 ? this.firstIterator.next() : this.secondIterator.next());
            }
        }
    }
}
