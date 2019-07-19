package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class WeightedList<U> {
    private final List<WeightedList<U>.WeightedEntry<? extends U>> entries = Lists.newArrayList();
    private final Random random;

    public WeightedList() {
        this(new Random());
    }

    public WeightedList(Random param0) {
        this.random = param0;
    }

    public void add(U param0, int param1) {
        this.entries.add(new WeightedList.WeightedEntry<>(param0, param1));
    }

    public void shuffle() {
        this.entries.forEach(param0 -> param0.setRandom(this.random.nextFloat()));
        this.entries.sort(Comparator.comparingDouble(WeightedList.WeightedEntry::getRandWeight));
    }

    public Stream<? extends U> stream() {
        return this.entries.stream().map(WeightedList.WeightedEntry::getData);
    }

    @Override
    public String toString() {
        return "WeightedList[" + this.entries + "]";
    }

    class WeightedEntry<T> {
        private final T data;
        private final int weight;
        private double randWeight;

        private WeightedEntry(T param0, int param1) {
            this.weight = param1;
            this.data = param0;
        }

        public double getRandWeight() {
            return this.randWeight;
        }

        public void setRandom(float param0) {
            this.randWeight = -Math.pow((double)param0, (double)(1.0F / (float)this.weight));
        }

        public T getData() {
            return this.data;
        }

        @Override
        public String toString() {
            return "" + this.weight + ":" + this.data;
        }
    }
}
