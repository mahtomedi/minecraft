package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class WeightedList<U> {
    protected final List<WeightedList.WeightedEntry<U>> entries;
    private final Random random = new Random();

    public WeightedList() {
        this(Lists.newArrayList());
    }

    private WeightedList(List<WeightedList.WeightedEntry<U>> param0) {
        this.entries = Lists.newArrayList(param0);
    }

    public static <U> Codec<WeightedList<U>> codec(Codec<U> param0) {
        return WeightedList.WeightedEntry.<U>codec(param0).listOf().xmap(WeightedList::new, param0x -> param0x.entries);
    }

    public WeightedList<U> add(U param0, int param1) {
        this.entries.add(new WeightedList.WeightedEntry<>(param0, param1));
        return this;
    }

    public WeightedList<U> shuffle() {
        return this.shuffle(this.random);
    }

    public WeightedList<U> shuffle(Random param0) {
        this.entries.forEach(param1 -> param1.setRandom(param0.nextFloat()));
        this.entries.sort(Comparator.comparingDouble(param0x -> param0x.getRandWeight()));
        return this;
    }

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    public Stream<U> stream() {
        return this.entries.stream().map(WeightedList.WeightedEntry::getData);
    }

    public U getOne(Random param0) {
        return this.shuffle(param0).stream().findFirst().orElseThrow(RuntimeException::new);
    }

    @Override
    public String toString() {
        return "WeightedList[" + this.entries + "]";
    }

    public static class WeightedEntry<T> {
        private final T data;
        private final int weight;
        private double randWeight;

        private WeightedEntry(T param0, int param1) {
            this.weight = param1;
            this.data = param0;
        }

        private double getRandWeight() {
            return this.randWeight;
        }

        private void setRandom(float param0) {
            this.randWeight = -Math.pow((double)param0, (double)(1.0F / (float)this.weight));
        }

        public T getData() {
            return this.data;
        }

        @Override
        public String toString() {
            return "" + this.weight + ":" + this.data;
        }

        public static <E> Codec<WeightedList.WeightedEntry<E>> codec(final Codec<E> param0) {
            return new Codec<WeightedList.WeightedEntry<E>>() {
                @Override
                public <T> DataResult<Pair<WeightedList.WeightedEntry<E>, T>> decode(DynamicOps<T> param0x, T param1) {
                    Dynamic<T> var0 = new Dynamic<>(param0, param1);
                    return var0.get("data")
                        .flatMap(param0::parse)
                        .map(param1x -> new WeightedList.WeightedEntry(param1x, var0.get("weight").asInt(1)))
                        .map(param1x -> Pair.of(param1x, param0.empty()));
                }

                public <T> DataResult<T> encode(WeightedList.WeightedEntry<E> param0x, DynamicOps<T> param1, T param2) {
                    return param1.mapBuilder()
                        .add("weight", param1.createInt(param0.weight))
                        .add("data", param0.encodeStart(param1, param0.data))
                        .build(param2);
                }
            };
        }
    }
}
