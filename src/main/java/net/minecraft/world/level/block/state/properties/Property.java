package net.minecraft.world.level.block.state.properties;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.StateHolder;

public abstract class Property<T extends Comparable<T>> {
    private final Class<T> clazz;
    private final String name;
    @Nullable
    private Integer hashCode;
    private final Codec<T> codec = Codec.STRING
        .comapFlatMap(
            param0x -> this.getValue(param0x)
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unable to read property: " + this + " with value: " + param0x)),
            this::getName
        );
    private final Codec<Property.Value<T>> valueCodec = this.codec.xmap(this::value, Property.Value::value);

    protected Property(String param0, Class<T> param1) {
        this.clazz = param1;
        this.name = param0;
    }

    public Property.Value<T> value(T param0x) {
        return new Property.Value<>(this, param0x);
    }

    public Property.Value<T> value(StateHolder<?, ?> param0) {
        return new Property.Value<>(this, param0.getValue(this));
    }

    public Stream<Property.Value<T>> getAllValues() {
        return this.getPossibleValues().stream().map(this::value);
    }

    public Codec<T> codec() {
        return this.codec;
    }

    public Codec<Property.Value<T>> valueCodec() {
        return this.valueCodec;
    }

    public String getName() {
        return this.name;
    }

    public Class<T> getValueClass() {
        return this.clazz;
    }

    public abstract Collection<T> getPossibleValues();

    public abstract String getName(T var1);

    public abstract Optional<T> getValue(String var1);

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", this.name).add("clazz", this.clazz).add("values", this.getPossibleValues()).toString();
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof Property)) {
            return false;
        } else {
            Property<?> var0 = (Property)param0;
            return this.clazz.equals(var0.clazz) && this.name.equals(var0.name);
        }
    }

    @Override
    public final int hashCode() {
        if (this.hashCode == null) {
            this.hashCode = this.generateHashCode();
        }

        return this.hashCode;
    }

    public int generateHashCode() {
        return 31 * this.clazz.hashCode() + this.name.hashCode();
    }

    public <U, S extends StateHolder<?, S>> DataResult<S> parseValue(DynamicOps<U> param0, S param1, U param2) {
        DataResult<T> var0 = this.codec.parse(param0, param2);
        return var0.<S>map(param1x -> param1.setValue(this, param1x)).setPartial(param1);
    }

    public static record Value<T extends Comparable<T>>(Property<T> property, T value) {
        public Value(Property<T> param0, T param1) {
            if (!param0.getPossibleValues().contains(param1)) {
                throw new IllegalArgumentException("Value " + param1 + " does not belong to property " + param0);
            } else {
                this.property = param0;
                this.value = param1;
            }
        }

        public String toString() {
            return this.property.getName() + "=" + this.property.getName(this.value);
        }
    }
}
