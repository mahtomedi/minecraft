package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.RandomSource;

public class ShufflingList<U> implements Iterable<U> {
    protected final List<ShufflingList.WeightedEntry<U>> entries;
    private final RandomSource random = RandomSource.create();

    public ShufflingList() {
        this.entries = Lists.newArrayList();
    }

    private ShufflingList(List<ShufflingList.WeightedEntry<U>> param0) {
        this.entries = Lists.newArrayList(param0);
    }

    public static <U> Codec<ShufflingList<U>> codec(Codec<U> param0) {
        return ShufflingList.WeightedEntry.<U>codec(param0).listOf().xmap(ShufflingList::new, param0x -> param0x.entries);
    }

    public ShufflingList<U> add(U param0, int param1) {
        this.entries.add(new ShufflingList.WeightedEntry<>(param0, param1));
        return this;
    }

    public ShufflingList<U> shuffle() {
        this.entries.forEach(param0 -> param0.setRandom(this.random.nextFloat()));
        this.entries.sort(Comparator.comparingDouble(ShufflingList.WeightedEntry::getRandWeight));
        return this;
    }

    public Stream<U> stream() {
        return this.entries.stream().map(ShufflingList.WeightedEntry::getData);
    }

    @Override
    public Iterator<U> iterator() {
        return Iterators.transform(this.entries.iterator(), ShufflingList.WeightedEntry::getData);
    }

    @Override
    public String toString() {
        return "ShufflingList[" + this.entries + "]";
    }

    public static class WeightedEntry<T> {
        final T data;
        final int weight;
        private double randWeight;

        WeightedEntry(T param0, int param1) {
            this.weight = param1;
            this.data = param0;
        }

        private double getRandWeight() {
            return this.randWeight;
        }

        void setRandom(float param0) {
            this.randWeight = -Math.pow((double)param0, (double)(1.0F / (float)this.weight));
        }

        public T getData() {
            return this.data;
        }

        public int getWeight() {
            return this.weight;
        }

        @Override
        public String toString() {
            return this.weight + ":" + this.data;
        }

        public static <E> Codec<ShufflingList.WeightedEntry<E>> codec(final Codec<E> param0) {
            return new Codec<ShufflingList.WeightedEntry<E>>() {
                @Override
                public <T> DataResult<Pair<ShufflingList.WeightedEntry<E>, T>> decode(DynamicOps<T> param0x, T param1) {
                    Dynamic<T> var0 = new Dynamic<>(param0, param1);
                    return var0.get("data")
                        .flatMap(param0::parse)
                        .map(param1x -> new ShufflingList.WeightedEntry<>(param1x, var0.get("weight").asInt(1)))
                        .map(param1x -> Pair.of((ShufflingList.WeightedEntry<E>)param1x, param0.empty()));
                }

                public <T> DataResult<T> encode(ShufflingList.WeightedEntry<E> param0x, DynamicOps<T> param1, T param2) {
                    return param1.mapBuilder()
                        .add("weight", param1.createInt(param0.weight))
                        .add("data", param0.encodeStart(param1, param0.data))
                        .build(param2);
                }
            };
        }
    }
}
