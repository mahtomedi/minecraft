package net.minecraft.util;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RewindableStream<T> {
    final List<T> cache = Lists.newArrayList();
    final Spliterator<T> source;

    public RewindableStream(Stream<T> param0) {
        this.source = param0.spliterator();
    }

    public Stream<T> getStream() {
        return StreamSupport.stream(new AbstractSpliterator<T>(Long.MAX_VALUE, 0) {
            private int index;

            @Override
            public boolean tryAdvance(Consumer<? super T> param0) {
                while(this.index >= RewindableStream.this.cache.size()) {
                    if (!RewindableStream.this.source.tryAdvance(RewindableStream.this.cache::add)) {
                        return false;
                    }
                }

                param0.accept(RewindableStream.this.cache.get(this.index++));
                return true;
            }
        }, false);
    }
}
