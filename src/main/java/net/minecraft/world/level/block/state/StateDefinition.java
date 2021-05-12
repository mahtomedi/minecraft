package net.minecraft.world.level.block.state;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.properties.Property;

public class StateDefinition<O, S extends StateHolder<O, S>> {
    static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
    private final O owner;
    private final ImmutableSortedMap<String, Property<?>> propertiesByName;
    private final ImmutableList<S> states;

    protected StateDefinition(Function<O, S> param0, O param1, StateDefinition.Factory<O, S> param2, Map<String, Property<?>> param3) {
        this.owner = param1;
        this.propertiesByName = ImmutableSortedMap.copyOf(param3);
        Supplier<S> var0 = () -> param0.apply(param1);
        MapCodec<S> var1 = MapCodec.of(Encoder.empty(), Decoder.unit(var0));

        for(Entry<String, Property<?>> var2 : this.propertiesByName.entrySet()) {
            var1 = appendPropertyCodec(var1, var0, var2.getKey(), var2.getValue());
        }

        MapCodec<S> var3 = var1;
        Map<Map<Property<?>, Comparable<?>>, S> var4 = Maps.newLinkedHashMap();
        List<S> var5 = Lists.newArrayList();
        Stream<List<Pair<Property<?>, Comparable<?>>>> var6 = Stream.of(Collections.emptyList());

        for(Property<?> var7 : this.propertiesByName.values()) {
            var6 = var6.flatMap(param1x -> var7.getPossibleValues().stream().map(param2x -> {
                    List<Pair<Property<?>, Comparable<?>>> var0x = Lists.newArrayList(param1x);
                    var0x.add(Pair.of(var7, param2x));
                    return var0x;
                }));
        }

        var6.forEach(param5 -> {
            ImmutableMap<Property<?>, Comparable<?>> var0x = param5.stream().collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));
            S var1x = param2.create(param1, var0x, var3);
            var4.put(var0x, var1x);
            var5.add(var1x);
        });

        for(S var8 : var5) {
            var8.populateNeighbours(var4);
        }

        this.states = ImmutableList.copyOf(var5);
    }

    private static <S extends StateHolder<?, S>, T extends Comparable<T>> MapCodec<S> appendPropertyCodec(
        MapCodec<S> param0, Supplier<S> param1, String param2, Property<T> param3
    ) {
        return Codec.mapPair(param0, param3.valueCodec().fieldOf(param2).setPartial(() -> param3.value(param1.get())))
            .xmap(param1x -> param1x.getFirst().setValue(param3, param1x.getSecond().value()), param1x -> Pair.of(param1x, param3.value(param1x)));
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
}
