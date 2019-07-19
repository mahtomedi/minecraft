package net.minecraft.world.level.block.state;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.MapFiller;
import net.minecraft.world.level.block.state.properties.Property;

public class StateDefinition<O, S extends StateHolder<S>> {
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
    private final O owner;
    private final ImmutableSortedMap<String, Property<?>> propertiesByName;
    private final ImmutableList<S> states;

    protected <A extends AbstractStateHolder<O, S>> StateDefinition(O param0, StateDefinition.Factory<O, S, A> param1, Map<String, Property<?>> param2) {
        this.owner = param0;
        this.propertiesByName = ImmutableSortedMap.copyOf(param2);
        Map<Map<Property<?>, Comparable<?>>, A> var0 = Maps.newLinkedHashMap();
        List<A> var1 = Lists.newArrayList();
        Stream<List<Comparable<?>>> var2 = Stream.of(Collections.emptyList());

        for(Property<?> var3 : this.propertiesByName.values()) {
            var2 = var2.flatMap(param1x -> var3.getPossibleValues().stream().map(param1xx -> {
                    List<Comparable<?>> var0x = Lists.newArrayList(param1x);
                    var0x.add(param1xx);
                    return var0x;
                }));
        }

        var2.forEach(param4 -> {
            Map<Property<?>, Comparable<?>> var0x = MapFiller.linkedHashMapFrom(this.propertiesByName.values(), param4);
            A var1x = param1.create(param0, ImmutableMap.copyOf(var0x));
            var0.put(var0x, var1x);
            var1.add(var1x);
        });

        for(A var4 : var1) {
            var4.populateNeighbours(var0);
        }

        this.states = ImmutableList.copyOf(var1);
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

    public static class Builder<O, S extends StateHolder<S>> {
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

        public <A extends AbstractStateHolder<O, S>> StateDefinition<O, S> create(StateDefinition.Factory<O, S, A> param0) {
            return new StateDefinition<>(this.owner, param0, this.properties);
        }
    }

    public interface Factory<O, S extends StateHolder<S>, A extends AbstractStateHolder<O, S>> {
        A create(O var1, ImmutableMap<Property<?>, Comparable<?>> var2);
    }
}
