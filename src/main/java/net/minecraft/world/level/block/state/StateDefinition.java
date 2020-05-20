package net.minecraft.world.level.block.state;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.commons.lang3.mutable.MutableObject;

public class StateDefinition<O, S extends StateHolder<O, S>> {
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
    private final O owner;
    private final ImmutableSortedMap<String, Property<?>> propertiesByName;
    private final ImmutableList<S> states;

    protected StateDefinition(Function<O, S> param0, O param1, StateDefinition.Factory<O, S> param2, Map<String, Property<?>> param3) {
        this.owner = param1;
        this.propertiesByName = ImmutableSortedMap.copyOf(param3);
        MapCodec<S> var0 = new StateDefinition.PropertiesCodec<>(this.propertiesByName, () -> param0.apply(param1));
        Map<Map<Property<?>, Comparable<?>>, S> var1 = Maps.newLinkedHashMap();
        List<S> var2 = Lists.newArrayList();
        Stream<List<Pair<Property<?>, Comparable<?>>>> var3 = Stream.of(Collections.emptyList());

        for(Property<?> var4 : this.propertiesByName.values()) {
            var3 = var3.flatMap(param1x -> var4.getPossibleValues().stream().map(param2x -> {
                    List<Pair<Property<?>, Comparable<?>>> var0x = Lists.newArrayList(param1x);
                    var0x.add(Pair.of(var4, param2x));
                    return var0x;
                }));
        }

        var3.forEach(param5 -> {
            ImmutableMap<Property<?>, Comparable<?>> var0x = param5.stream().collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));
            S var1x = param2.create(param1, var0x, var0);
            var1.put(var0x, var1x);
            var2.add(var1x);
        });

        for(S var5 : var2) {
            var5.populateNeighbours(var1);
        }

        this.states = ImmutableList.copyOf(var2);
    }

    public ImmutableList<S> getPossibleStates() {
        return this.states;
    }

    public S any() {
        return this.states.get(0);
    }

    public O getOwner() {
        return this.owner;
    }

    public Collection<Property<?>> getProperties() {
        return this.propertiesByName.values();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("block", this.owner)
            .add("properties", this.propertiesByName.values().stream().map(Property::getName).collect(Collectors.toList()))
            .toString();
    }

    @Nullable
    public Property<?> getProperty(String param0) {
        return this.propertiesByName.get(param0);
    }

    public static class Builder<O, S extends StateHolder<O, S>> {
        private final O owner;
        private final Map<String, Property<?>> properties = Maps.newHashMap();

        public Builder(O param0) {
            this.owner = param0;
        }

        public StateDefinition.Builder<O, S> add(Property<?>... param0) {
            for(Property<?> var0 : param0) {
                this.validateProperty(var0);
                this.properties.put(var0.getName(), var0);
            }

            return this;
        }

        private <T extends Comparable<T>> void validateProperty(Property<T> param0) {
            String var0 = param0.getName();
            if (!StateDefinition.NAME_PATTERN.matcher(var0).matches()) {
                throw new IllegalArgumentException(this.owner + " has invalidly named property: " + var0);
            } else {
                Collection<T> var1 = param0.getPossibleValues();
                if (var1.size() <= 1) {
                    throw new IllegalArgumentException(this.owner + " attempted use property " + var0 + " with <= 1 possible values");
                } else {
                    for(T var2 : var1) {
                        String var3 = param0.getName(var2);
                        if (!StateDefinition.NAME_PATTERN.matcher(var3).matches()) {
                            throw new IllegalArgumentException(this.owner + " has property: " + var0 + " with invalidly named value: " + var3);
                        }
                    }

                    if (this.properties.containsKey(var0)) {
                        throw new IllegalArgumentException(this.owner + " has duplicate property: " + var0);
                    }
                }
            }
        }

        public StateDefinition<O, S> create(Function<O, S> param0, StateDefinition.Factory<O, S> param1) {
            return new StateDefinition<>(param0, this.owner, param1, this.properties);
        }
    }

    public interface Factory<O, S> {
        S create(O var1, ImmutableMap<Property<?>, Comparable<?>> var2, MapCodec<S> var3);
    }

    static class PropertiesCodec<S extends StateHolder<?, S>> extends MapCodec<S> {
        private final Map<String, Property<?>> propertiesByName;
        private final Supplier<S> defaultState;

        public PropertiesCodec(Map<String, Property<?>> param0, Supplier<S> param1) {
            this.propertiesByName = param0;
            this.defaultState = param1;
        }

        public <T> RecordBuilder<T> encode(S param0, DynamicOps<T> param1, RecordBuilder<T> param2) {
            param0.getValues().forEach((param2x, param3) -> param2.add(param2x.getName(), param1.createString(getName(param2x, param3))));
            return param2;
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> param0) {
            return this.propertiesByName.keySet().stream().map(param0::createString);
        }

        @Override
        public <T> DataResult<S> decode(DynamicOps<T> param0, MapLike<T> param1) {
            MutableObject<DataResult<S>> var0 = new MutableObject<>(DataResult.success(this.defaultState.get()));
            param1.entries().forEach(param2 -> {
                DataResult<Property<?>> var0x = param0.getStringValue(param2.getFirst()).map(this.propertiesByName::get);
                T var1x = param2.getSecond();
                var0.setValue(var0.getValue().flatMap(param3 -> var0x.flatMap(param3x -> param3x.parseValue(param0, (S)param3, (T)var1x))));
            });
            return var0.getValue();
        }

        private static <T extends Comparable<T>> String getName(Property<T> param0, Comparable<?> param1) {
            return param0.getName((T)param1);
        }

        @Override
        public String toString() {
            return "PropertiesCodec";
        }
    }
}
