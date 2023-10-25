package net.minecraft.util;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Queues;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Comparator;
import java.util.Deque;
import java.util.Optional;
import java.util.Map.Entry;
import javax.annotation.Nullable;

public final class SequencedPriorityIterator<T> extends AbstractIterator<T> {
    private final Int2ObjectMap<Deque<T>> valuesByPriority = new Int2ObjectOpenHashMap<>();

    public void add(T param0, int param1) {
        this.valuesByPriority.computeIfAbsent(param1, param0x -> Queues.newArrayDeque()).addLast(param0);
    }

    @Nullable
    @Override
    protected T computeNext() {
        Optional<Deque<T>> var0 = this.valuesByPriority
            .int2ObjectEntrySet()
            .stream()
            .filter(param0 -> !param0.getValue().isEmpty())
            .max(Comparator.comparingInt(Entry::getKey))
            .map(Entry::getValue);
        return var0.map(Deque::removeFirst).orElseGet(() -> (T)this.endOfData());
    }
}
