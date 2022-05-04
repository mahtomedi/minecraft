package net.minecraft.client.searchtree;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import java.util.Comparator;
import java.util.Iterator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MergingUniqueIterator<T> extends AbstractIterator<T> {
    private final PeekingIterator<T> firstIterator;
    private final PeekingIterator<T> secondIterator;
    private final Comparator<T> comparator;

    public MergingUniqueIterator(Iterator<T> param0, Iterator<T> param1, Comparator<T> param2) {
        this.firstIterator = Iterators.peekingIterator(param0);
        this.secondIterator = Iterators.peekingIterator(param1);
        this.comparator = param2;
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
            int var2 = this.comparator.compare(this.firstIterator.peek(), this.secondIterator.peek());
            if (var2 == 0) {
                this.secondIterator.next();
            }

            return (T)(var2 <= 0 ? this.firstIterator.next() : this.secondIterator.next());
        }
    }
}
