package net.minecraft.data.models.blockstates;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.world.level.block.state.properties.Property;

public final class Selector {
    private static final Selector EMPTY = new Selector(ImmutableList.of());
    private static final Comparator<Property.Value<?>> COMPARE_BY_NAME = Comparator.comparing(param0 -> param0.property().getName());
    private final List<Property.Value<?>> values;

    public Selector extend(Property.Value<?> param0) {
        return new Selector(ImmutableList.<Property.Value<?>>builder().addAll(this.values).add(param0).build());
    }

    public Selector extend(Selector param0) {
        return new Selector(ImmutableList.<Property.Value<?>>builder().addAll(this.values).addAll(param0.values).build());
    }

    private Selector(List<Property.Value<?>> param0) {
        this.values = param0;
    }

    public static Selector empty() {
        return EMPTY;
    }

    public static Selector of(Property.Value<?>... param0) {
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
        return this.values.stream().sorted(COMPARE_BY_NAME).map(Property.Value::toString).collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return this.getKey();
    }
}
