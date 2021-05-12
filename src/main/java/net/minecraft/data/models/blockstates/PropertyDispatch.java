package net.minecraft.data.models.blockstates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.world.level.block.state.properties.Property;

public abstract class PropertyDispatch {
    private final Map<Selector, List<Variant>> values = Maps.newHashMap();

    protected void putValue(Selector param0, List<Variant> param1) {
        List<Variant> var0 = this.values.put(param0, param1);
        if (var0 != null) {
            throw new IllegalStateException("Value " + param0 + " is already defined");
        }
    }

    Map<Selector, List<Variant>> getEntries() {
        this.verifyComplete();
        return ImmutableMap.copyOf(this.values);
    }

    private void verifyComplete() {
        List<Property<?>> var0 = this.getDefinedProperties();
        Stream<Selector> var1 = Stream.of(Selector.empty());

        for(Property<?> var2 : var0) {
            var1 = var1.flatMap(param1 -> var2.getAllValues().map(param1::extend));
        }

        List<Selector> var3 = var1.filter(param0 -> !this.values.containsKey(param0)).collect(Collectors.toList());
        if (!var3.isEmpty()) {
            throw new IllegalStateException("Missing definition for properties: " + var3);
        }
    }

    abstract List<Property<?>> getDefinedProperties();

    public static <T1 extends Comparable<T1>> PropertyDispatch.C1<T1> property(Property<T1> param0) {
        return new PropertyDispatch.C1<>(param0);
    }

    public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>> PropertyDispatch.C2<T1, T2> properties(Property<T1> param0, Property<T2> param1) {
        return new PropertyDispatch.C2<>(param0, param1);
    }

    public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>> PropertyDispatch.C3<T1, T2, T3> properties(
        Property<T1> param0, Property<T2> param1, Property<T3> param2
    ) {
        return new PropertyDispatch.C3<>(param0, param1, param2);
    }

    public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>> PropertyDispatch.C4<T1, T2, T3, T4> properties(
        Property<T1> param0, Property<T2> param1, Property<T3> param2, Property<T4> param3
    ) {
        return new PropertyDispatch.C4<>(param0, param1, param2, param3);
    }

    public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>, T5 extends Comparable<T5>> PropertyDispatch.C5<T1, T2, T3, T4, T5> properties(
        Property<T1> param0, Property<T2> param1, Property<T3> param2, Property<T4> param3, Property<T5> param4
    ) {
        return new PropertyDispatch.C5<>(param0, param1, param2, param3, param4);
    }

    public static class C1<T1 extends Comparable<T1>> extends PropertyDispatch {
        private final Property<T1> property1;

        C1(Property<T1> param0) {
            this.property1 = param0;
        }

        @Override
        public List<Property<?>> getDefinedProperties() {
            return ImmutableList.of(this.property1);
        }

        public PropertyDispatch.C1<T1> select(T1 param0, List<Variant> param1) {
            Selector var0 = Selector.of(this.property1.value(param0));
            this.putValue(var0, param1);
            return this;
        }

        public PropertyDispatch.C1<T1> select(T1 param0, Variant param1) {
            return this.select(param0, Collections.singletonList(param1));
        }

        public PropertyDispatch generate(Function<T1, Variant> param0) {
            this.property1.getPossibleValues().forEach(param1 -> this.select(param1, param0.apply(param1)));
            return this;
        }

        public PropertyDispatch generateList(Function<T1, List<Variant>> param0) {
            this.property1.getPossibleValues().forEach(param1 -> this.select(param1, param0.apply(param1)));
            return this;
        }
    }

    public static class C2<T1 extends Comparable<T1>, T2 extends Comparable<T2>> extends PropertyDispatch {
        private final Property<T1> property1;
        private final Property<T2> property2;

        C2(Property<T1> param0, Property<T2> param1) {
            this.property1 = param0;
            this.property2 = param1;
        }

        @Override
        public List<Property<?>> getDefinedProperties() {
            return ImmutableList.of(this.property1, this.property2);
        }

        public PropertyDispatch.C2<T1, T2> select(T1 param0, T2 param1, List<Variant> param2) {
            Selector var0 = Selector.of(this.property1.value(param0), this.property2.value(param1));
            this.putValue(var0, param2);
            return this;
        }

        public PropertyDispatch.C2<T1, T2> select(T1 param0, T2 param1, Variant param2) {
            return this.select(param0, param1, Collections.singletonList(param2));
        }

        public PropertyDispatch generate(BiFunction<T1, T2, Variant> param0) {
            this.property1
                .getPossibleValues()
                .forEach(param1 -> this.property2.getPossibleValues().forEach(param2 -> this.select((T1)param1, param2, param0.apply((T1)param1, param2))));
            return this;
        }

        public PropertyDispatch generateList(BiFunction<T1, T2, List<Variant>> param0) {
            this.property1
                .getPossibleValues()
                .forEach(param1 -> this.property2.getPossibleValues().forEach(param2 -> this.select((T1)param1, param2, param0.apply((T1)param1, param2))));
            return this;
        }
    }

    public static class C3<T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>> extends PropertyDispatch {
        private final Property<T1> property1;
        private final Property<T2> property2;
        private final Property<T3> property3;

        C3(Property<T1> param0, Property<T2> param1, Property<T3> param2) {
            this.property1 = param0;
            this.property2 = param1;
            this.property3 = param2;
        }

        @Override
        public List<Property<?>> getDefinedProperties() {
            return ImmutableList.of(this.property1, this.property2, this.property3);
        }

        public PropertyDispatch.C3<T1, T2, T3> select(T1 param0, T2 param1, T3 param2, List<Variant> param3) {
            Selector var0 = Selector.of(this.property1.value(param0), this.property2.value(param1), this.property3.value(param2));
            this.putValue(var0, param3);
            return this;
        }

        public PropertyDispatch.C3<T1, T2, T3> select(T1 param0, T2 param1, T3 param2, Variant param3) {
            return this.select(param0, param1, param2, Collections.singletonList(param3));
        }

        public PropertyDispatch generate(PropertyDispatch.TriFunction<T1, T2, T3, Variant> param0) {
            this.property1
                .getPossibleValues()
                .forEach(
                    param1 -> this.property2
                            .getPossibleValues()
                            .forEach(
                                param2 -> this.property3
                                        .getPossibleValues()
                                        .forEach(param3 -> this.select((T1)param1, (T2)param2, param3, param0.apply((T1)param1, (T2)param2, param3)))
                            )
                );
            return this;
        }

        public PropertyDispatch generateList(PropertyDispatch.TriFunction<T1, T2, T3, List<Variant>> param0) {
            this.property1
                .getPossibleValues()
                .forEach(
                    param1 -> this.property2
                            .getPossibleValues()
                            .forEach(
                                param2 -> this.property3
                                        .getPossibleValues()
                                        .forEach(param3 -> this.select((T1)param1, (T2)param2, param3, param0.apply((T1)param1, (T2)param2, param3)))
                            )
                );
            return this;
        }
    }

    public static class C4<T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>> extends PropertyDispatch {
        private final Property<T1> property1;
        private final Property<T2> property2;
        private final Property<T3> property3;
        private final Property<T4> property4;

        C4(Property<T1> param0, Property<T2> param1, Property<T3> param2, Property<T4> param3) {
            this.property1 = param0;
            this.property2 = param1;
            this.property3 = param2;
            this.property4 = param3;
        }

        @Override
        public List<Property<?>> getDefinedProperties() {
            return ImmutableList.of(this.property1, this.property2, this.property3, this.property4);
        }

        public PropertyDispatch.C4<T1, T2, T3, T4> select(T1 param0, T2 param1, T3 param2, T4 param3, List<Variant> param4) {
            Selector var0 = Selector.of(this.property1.value(param0), this.property2.value(param1), this.property3.value(param2), this.property4.value(param3));
            this.putValue(var0, param4);
            return this;
        }

        public PropertyDispatch.C4<T1, T2, T3, T4> select(T1 param0, T2 param1, T3 param2, T4 param3, Variant param4) {
            return this.select(param0, param1, param2, param3, Collections.singletonList(param4));
        }

        public PropertyDispatch generate(PropertyDispatch.QuadFunction<T1, T2, T3, T4, Variant> param0) {
            this.property1
                .getPossibleValues()
                .forEach(
                    param1 -> this.property2
                            .getPossibleValues()
                            .forEach(
                                param2 -> this.property3
                                        .getPossibleValues()
                                        .forEach(
                                            param3 -> this.property4
                                                    .getPossibleValues()
                                                    .forEach(
                                                        param4 -> this.select(
                                                                (T1)param1,
                                                                (T2)param2,
                                                                (T3)param3,
                                                                param4,
                                                                param0.apply((T1)param1, (T2)param2, (T3)param3, param4)
                                                            )
                                                    )
                                        )
                            )
                );
            return this;
        }

        public PropertyDispatch generateList(PropertyDispatch.QuadFunction<T1, T2, T3, T4, List<Variant>> param0) {
            this.property1
                .getPossibleValues()
                .forEach(
                    param1 -> this.property2
                            .getPossibleValues()
                            .forEach(
                                param2 -> this.property3
                                        .getPossibleValues()
                                        .forEach(
                                            param3 -> this.property4
                                                    .getPossibleValues()
                                                    .forEach(
                                                        param4 -> this.select(
                                                                (T1)param1,
                                                                (T2)param2,
                                                                (T3)param3,
                                                                param4,
                                                                param0.apply((T1)param1, (T2)param2, (T3)param3, param4)
                                                            )
                                                    )
                                        )
                            )
                );
            return this;
        }
    }

    public static class C5<T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>, T5 extends Comparable<T5>>
        extends PropertyDispatch {
        private final Property<T1> property1;
        private final Property<T2> property2;
        private final Property<T3> property3;
        private final Property<T4> property4;
        private final Property<T5> property5;

        C5(Property<T1> param0, Property<T2> param1, Property<T3> param2, Property<T4> param3, Property<T5> param4) {
            this.property1 = param0;
            this.property2 = param1;
            this.property3 = param2;
            this.property4 = param3;
            this.property5 = param4;
        }

        @Override
        public List<Property<?>> getDefinedProperties() {
            return ImmutableList.of(this.property1, this.property2, this.property3, this.property4, this.property5);
        }

        public PropertyDispatch.C5<T1, T2, T3, T4, T5> select(T1 param0, T2 param1, T3 param2, T4 param3, T5 param4, List<Variant> param5) {
            Selector var0 = Selector.of(
                this.property1.value(param0),
                this.property2.value(param1),
                this.property3.value(param2),
                this.property4.value(param3),
                this.property5.value(param4)
            );
            this.putValue(var0, param5);
            return this;
        }

        public PropertyDispatch.C5<T1, T2, T3, T4, T5> select(T1 param0, T2 param1, T3 param2, T4 param3, T5 param4, Variant param5) {
            return this.select(param0, param1, param2, param3, param4, Collections.singletonList(param5));
        }

        public PropertyDispatch generate(PropertyDispatch.PentaFunction<T1, T2, T3, T4, T5, Variant> param0) {
            this.property1
                .getPossibleValues()
                .forEach(
                    param1 -> this.property2
                            .getPossibleValues()
                            .forEach(
                                param2 -> this.property3
                                        .getPossibleValues()
                                        .forEach(
                                            param3 -> this.property4
                                                    .getPossibleValues()
                                                    .forEach(
                                                        param4 -> this.property5
                                                                .getPossibleValues()
                                                                .forEach(
                                                                    param5 -> this.select(
                                                                            (T1)param1,
                                                                            (T2)param2,
                                                                            (T3)param3,
                                                                            (T4)param4,
                                                                            param5,
                                                                            param0.apply((T1)param1, (T2)param2, (T3)param3, (T4)param4, param5)
                                                                        )
                                                                )
                                                    )
                                        )
                            )
                );
            return this;
        }

        public PropertyDispatch generateList(PropertyDispatch.PentaFunction<T1, T2, T3, T4, T5, List<Variant>> param0) {
            this.property1
                .getPossibleValues()
                .forEach(
                    param1 -> this.property2
                            .getPossibleValues()
                            .forEach(
                                param2 -> this.property3
                                        .getPossibleValues()
                                        .forEach(
                                            param3 -> this.property4
                                                    .getPossibleValues()
                                                    .forEach(
                                                        param4 -> this.property5
                                                                .getPossibleValues()
                                                                .forEach(
                                                                    param5 -> this.select(
                                                                            (T1)param1,
                                                                            (T2)param2,
                                                                            (T3)param3,
                                                                            (T4)param4,
                                                                            param5,
                                                                            param0.apply((T1)param1, (T2)param2, (T3)param3, (T4)param4, param5)
                                                                        )
                                                                )
                                                    )
                                        )
                            )
                );
            return this;
        }
    }

    @FunctionalInterface
    public interface PentaFunction<P1, P2, P3, P4, P5, R> {
        R apply(P1 var1, P2 var2, P3 var3, P4 var4, P5 var5);
    }

    @FunctionalInterface
    public interface QuadFunction<P1, P2, P3, P4, R> {
        R apply(P1 var1, P2 var2, P3 var3, P4 var4);
    }

    @FunctionalInterface
    public interface TriFunction<P1, P2, P3, R> {
        R apply(P1 var1, P2 var2, P3 var3);
    }
}
