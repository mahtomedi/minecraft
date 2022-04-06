package net.minecraft.util.random;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.RandomSource;

public class WeightedRandomList<E extends WeightedEntry> {
    private final int totalWeight;
    private final ImmutableList<E> items;

    WeightedRandomList(List<? extends E> param0) {
        this.items = ImmutableList.copyOf(param0);
        this.totalWeight = WeightedRandom.getTotalWeight(param0);
    }

    public static <E extends WeightedEntry> WeightedRandomList<E> create() {
        return new WeightedRandomList<>(ImmutableList.of());
    }

    @SafeVarargs
    public static <E extends WeightedEntry> WeightedRandomList<E> create(E... param0) {
        return new WeightedRandomList<>(ImmutableList.copyOf(param0));
    }

    public static <E extends WeightedEntry> WeightedRandomList<E> create(List<E> param0) {
        return new WeightedRandomList<>(param0);
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public Optional<E> getRandom(RandomSource param0) {
        if (this.totalWeight == 0) {
            return Optional.empty();
        } else {
            int var0 = param0.nextInt(this.totalWeight);
            return WeightedRandom.getWeightedItem(this.items, var0);
        }
    }

    public List<E> unwrap() {
        return this.items;
    }

    public static <E extends WeightedEntry> Codec<WeightedRandomList<E>> codec(Codec<E> param0) {
        return param0.listOf().xmap(WeightedRandomList::create, WeightedRandomList::unwrap);
    }
}
