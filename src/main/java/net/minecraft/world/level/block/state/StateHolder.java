package net.minecraft.world.level.block.state;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.properties.Property;

public abstract class StateHolder<O, S> {
    public static final String NAME_TAG = "Name";
    public static final String PROPERTIES_TAG = "Properties";
    private static final Function<Entry<Property<?>, Comparable<?>>, String> PROPERTY_ENTRY_TO_STRING_FUNCTION = new Function<Entry<Property<?>, Comparable<?>>, String>(
        
    ) {
        public String apply(@Nullable Entry<Property<?>, Comparable<?>> param0) {
            if (param0 == null) {
                return "<NULL>";
            } else {
                Property<?> var0 = param0.getKey();
                return var0.getName() + "=" + this.getName(var0, param0.getValue());
            }
        }

        private <T extends Comparable<T>> String getName(Property<T> param0, Comparable<?> param1) {
            return param0.getName((T)param1);
        }
    };
    protected final O owner;
    private final ImmutableMap<Property<?>, Comparable<?>> values;
    private Table<Property<?>, Comparable<?>, S> neighbours;
    protected final MapCodec<S> propertiesCodec;

    protected StateHolder(O param0, ImmutableMap<Property<?>, Comparable<?>> param1, MapCodec<S> param2) {
        this.owner = param0;
        this.values = param1;
        this.propertiesCodec = param2;
    }

    public <T extends Comparable<T>> S cycle(Property<T> param0) {
        return this.setValue(param0, findNextInCollection(param0.getPossibleValues(), this.getValue(param0)));
    }

    protected static <T> T findNextInCollection(Collection<T> param0, T param1) {
        Iterator<T> var0 = param0.iterator();

        while(var0.hasNext()) {
            if (var0.next().equals(param1)) {
                if (var0.hasNext()) {
                    return var0.next();
                }

                return param0.iterator().next();
            }
        }

        return var0.next();
    }

    @Override
    public String toString() {
        StringBuilder var0 = new StringBuilder();
        var0.append(this.owner);
        if (!this.getValues().isEmpty()) {
            var0.append('[');
            var0.append(this.getValues().entrySet().stream().map(PROPERTY_ENTRY_TO_STRING_FUNCTION).collect(Collectors.joining(",")));
            var0.append(']');
        }

        return var0.toString();
    }

    public Collection<Property<?>> getProperties() {
        return Collections.unmodifiableCollection(this.values.keySet());
    }

    public <T extends Comparable<T>> boolean hasProperty(Property<T> param0) {
        return this.values.containsKey(param0);
    }

    public <T extends Comparable<T>> T getValue(Property<T> param0) {
        Comparable<?> var0 = this.values.get(param0);
        if (var0 == null) {
            throw new IllegalArgumentException("Cannot get property " + param0 + " as it does not exist in " + this.owner);
        } else {
            return param0.getValueClass().cast(var0);
        }
    }

    public <T extends Comparable<T>> Optional<T> getOptionalValue(Property<T> param0) {
        Comparable<?> var0 = this.values.get(param0);
        return var0 == null ? Optional.empty() : Optional.of(param0.getValueClass().cast(var0));
    }

    public <T extends Comparable<T>, V extends T> S setValue(Property<T> param0, V param1) {
        Comparable<?> var0 = this.values.get(param0);
        if (var0 == null) {
            throw new IllegalArgumentException("Cannot set property " + param0 + " as it does not exist in " + this.owner);
        } else if (var0 == param1) {
            return (S)this;
        } else {
            S var1 = this.neighbours.get(param0, param1);
            if (var1 == null) {
                throw new IllegalArgumentException("Cannot set property " + param0 + " to " + param1 + " on " + this.owner + ", it is not an allowed value");
            } else {
                return var1;
            }
        }
    }

    public void populateNeighbours(Map<Map<Property<?>, Comparable<?>>, S> param0) {
        if (this.neighbours != null) {
            throw new IllegalStateException();
        } else {
            Table<Property<?>, Comparable<?>, S> var0 = HashBasedTable.create();

            for(Entry<Property<?>, Comparable<?>> var1 : this.values.entrySet()) {
                Property<?> var2 = var1.getKey();

                for(Comparable<?> var3 : var2.getPossibleValues()) {
                    if (var3 != var1.getValue()) {
                        var0.put(var2, var3, param0.get(this.makeNeighbourValues(var2, var3)));
                    }
                }
            }

            this.neighbours = (Table<Property<?>, Comparable<?>, S>)(var0.isEmpty() ? var0 : ArrayTable.create(var0));
        }
    }

    private Map<Property<?>, Comparable<?>> makeNeighbourValues(Property<?> param0, Comparable<?> param1) {
        Map<Property<?>, Comparable<?>> var0 = Maps.newHashMap(this.values);
        var0.put(param0, param1);
        return var0;
    }

    public ImmutableMap<Property<?>, Comparable<?>> getValues() {
        return this.values;
    }

    protected static <O, S extends StateHolder<O, S>> Codec<S> codec(Codec<O> param0, Function<O, S> param1) {
        return param0.dispatch("Name", param0x -> param0x.owner, param1x -> {
            S var0x = param1.apply(param1x);
            return var0x.getValues().isEmpty() ? Codec.unit((S)var0x) : var0x.propertiesCodec.fieldOf("Properties").codec();
        });
    }
}
