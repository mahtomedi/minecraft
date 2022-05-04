package net.minecraft.client.searchtree;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import java.util.Comparator;
import java.util.Iterator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IntersectionIterator<T> extends AbstractIterator<T> {
    private final PeekingIterator<T> firstIterator;
    private final PeekingIterator<T> secondIterator;
    private final Comparator<T> comparator;

    public IntersectionIterator(Iterator<T> param0, Iterator<T> param1, Comparator<T> param2) {
        this.firstIterator = Iterators.peekingIterator(param0);
        this.secondIterator = Iterators.peekingIterator(param1);
        this.comparator = param2;
    }

    @Override
    protected T computeNext() {
        while(this.firstIterator.hasNext() && this.secondIterator.hasNext()) {
            int var0 = this.comparator.compare(this.firstIterator.peek(), this.secondIterator.peek());
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
