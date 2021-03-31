package net.minecraft.util.random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public interface WeightedEntry {
    Weight getWeight();

    static <T> WeightedEntry.Wrapper<T> wrap(T param0, int param1) {
        return new WeightedEntry.Wrapper<>(param0, Weight.of(param1));
    }

    public static class IntrusiveBase implements WeightedEntry {
        private final Weight weight;

        public IntrusiveBase(int param0) {
            this.weight = Weight.of(param0);
        }

        public IntrusiveBase(Weight param0) {
            this.weight = param0;
        }

        @Override
        public Weight getWeight() {
            return this.weight;
        }
    }

    public static class Wrapper<T> implements WeightedEntry {
        private final T data;
        private final Weight weight;

        private Wrapper(T param0, Weight param1) {
            this.data = param0;
            this.weight = param1;
        }

        public T getData() {
            return this.data;
        }

        @Override
        public Weight getWeight() {
            return this.weight;
        }

        public static <E> Codec<WeightedEntry.Wrapper<E>> codec(Codec<E> param0) {
            return RecordCodecBuilder.create(
                param1 -> param1.group(
                            param0.fieldOf("data").forGetter(WeightedEntry.Wrapper::getData),
                            Weight.CODEC.fieldOf("weight").forGetter(WeightedEntry.Wrapper::getWeight)
                        )
                        .apply(param1, WeightedEntry.Wrapper::new)
            );
        }
    }
}
