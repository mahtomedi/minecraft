package net.minecraft.data.models.blockstates;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class Selector {
    private static final Selector EMPTY = new Selector(ImmutableList.of());
    private static final Comparator<PropertyValue<?>> COMPARE_BY_NAME = Comparator.comparing(param0 -> param0.getProperty().getName());
    private final List<PropertyValue<?>> values;

    public Selector extend(PropertyValue<?> param0) {
        return new Selector(ImmutableList.<PropertyValue<?>>builder().addAll(this.values).add(param0).build());
    }

    public Selector extend(Selector param0) {
        return new Selector(ImmutableList.<PropertyValue<?>>builder().addAll(this.values).addAll(param0.values).build());
    }

    private Selector(List<PropertyValue<?>> param0) {
        this.values = param0;
    }

    public static Selector empty() {
        return EMPTY;
    }

    public static Selector of(PropertyValue<?>... param0) {
        return new Selector(ImmutableList.copyOf(param0));
    }

    @Override
    public boolean equals(Object param0) {
        return this == param0 || param0 instanceof Selector && this.values.equals(((Selector)param0).values);
    }

    @Override
    public int hashCode() {
        return this.values.hashCode();
    }

    public String getKey() {
        return this.values.stream().sorted(COMPARE_BY_NAME).map(PropertyValue::toString).collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return this.getKey();
    }
}
