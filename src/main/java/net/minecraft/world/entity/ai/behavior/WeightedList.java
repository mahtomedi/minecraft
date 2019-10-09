package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

public class WeightedList<U> {
    protected final List<WeightedList<U>.WeightedEntry<? extends U>> entries = Lists.newArrayList();
    private final Random random;

    public WeightedList(Random param0) {
        this.random = param0;
    }

    public WeightedList() {
        this(new Random());
    }

    public <T> WeightedList(Dynamic<T> param0, Function<Dynamic<T>, U> param1) {
        this();
        param0.asStream().forEach(param1x -> param1x.get("data").map(param2 -> {
                U var0 = param1.apply(param2);
                int var1x = param1x.get("weight").asInt(1);
                return this.add(var0, var1x);
            }));
    }

    public <T> T serialize(DynamicOps<T> param0, Function<U, Dynamic<T>> param1) {
        return param0.createList(
            this.streamEntries()
                .map(
                    param2 -> param0.createMap(
                            ImmutableMap.<T, T>builder()
                                .put(param0.createString("data"), param1.apply(param2.getData()).getValue())
                                .put(param0.createString("weight"), param0.createInt(param2.getWeight()))
                                .build()
                        )
                )
        );
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

    public Stream<? extends U> stream() {
        return this.entries.stream().map(WeightedList.WeightedEntry::getData);
    }

    public Stream<WeightedList<U>.WeightedEntry<? extends U>> streamEntries() {
        return this.entries.stream();
    }

    public U getOne(Random param0) {
        return this.shuffle(param0).stream().findFirst().orElseThrow(RuntimeException::new);
    }

    @Override
    public String toString() {
        return "WeightedList[" + this.entries + "]";
    }

    public class WeightedEntry<T> {
        private final T data;
        private final int weight;
        private double randWeight;

        private WeightedEntry(T param1, int param2) {
            this.weight = param2;
            this.data = param1;
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

        public int getWeight() {
            return this.weight;
        }

        @Override
        public String toString() {
            return "" + this.weight + ":" + this.data;
        }
    }
}
