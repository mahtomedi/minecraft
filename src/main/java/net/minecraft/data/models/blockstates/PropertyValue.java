package net.minecraft.data.models.blockstates;

import java.util.stream.Stream;
import net.minecraft.world.level.block.state.properties.Property;

public final class PropertyValue<T extends Comparable<T>> {
    private final Property<T> property;
    private final T value;

    public PropertyValue(Property<T> param0, T param1) {
        if (!param0.getPossibleValues().contains(param1)) {
            throw new IllegalArgumentException("Value " + param1 + " does not belong to property " + param0);
        } else {
            this.property = param0;
            this.value = param1;
        }
    }

    public Property<T> getProperty() {
        return this.property;
    }

    @Override
    public String toString() {
        return this.property.getName() + "=" + this.property.getName(this.value);
    }

    public static <T extends Comparable<T>> Stream<PropertyValue<T>> getAll(Property<T> param0) {
        return param0.getPossibleValues().stream().map(param1 -> new PropertyValue<>(param0, param1));
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof PropertyValue)) {
            return false;
        } else {
            PropertyValue<?> var0 = (PropertyValue)param0;
            return this.property == var0.property && this.value.equals(var0.value);
        }
    }

    @Override
    public int hashCode() {
        int var0 = this.property.hashCode();
        return 31 * var0 + this.value.hashCode();
    }
}
