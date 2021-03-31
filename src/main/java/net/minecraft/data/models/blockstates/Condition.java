package net.minecraft.data.models.blockstates;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public interface Condition extends Supplier<JsonElement> {
    void validate(StateDefinition<?, ?> var1);

    static Condition.TerminalCondition condition() {
        return new Condition.TerminalCondition();
    }

    static Condition and(Condition... param0) {
        return new Condition.CompositeCondition(Condition.Operation.AND, Arrays.asList(param0));
    }

    static Condition or(Condition... param0) {
        return new Condition.CompositeCondition(Condition.Operation.OR, Arrays.asList(param0));
    }

    public static class CompositeCondition implements Condition {
        private final Condition.Operation operation;
        private final List<Condition> subconditions;

        private CompositeCondition(Condition.Operation param0, List<Condition> param1) {
            this.operation = param0;
            this.subconditions = param1;
        }

        @Override
        public void validate(StateDefinition<?, ?> param0) {
            this.subconditions.forEach(param1 -> param1.validate(param0));
        }

        public JsonElement get() {
            JsonArray var0 = new JsonArray();
            this.subconditions.stream().map(Supplier::get).forEach(var0::add);
            JsonObject var1 = new JsonObject();
            var1.add(this.operation.id, var0);
            return var1;
        }
    }

    public static enum Operation {
        AND("AND"),
        OR("OR");

        private final String id;

        private Operation(String param0) {
            this.id = param0;
        }
    }

    public static class TerminalCondition implements Condition {
        private final Map<Property<?>, String> terms = Maps.newHashMap();

        private static <T extends Comparable<T>> String joinValues(Property<T> param0, Stream<T> param1) {
            return param1.<String>map(param0::getName).collect(Collectors.joining("|"));
        }

        private static <T extends Comparable<T>> String getTerm(Property<T> param0, T param1, T[] param2) {
            return joinValues(param0, Stream.concat(Stream.of(param1), Stream.of(param2)));
        }

        private <T extends Comparable<T>> void putValue(Property<T> param0, String param1) {
            String var0 = this.terms.put(param0, param1);
            if (var0 != null) {
                throw new IllegalStateException("Tried to replace " + param0 + " value from " + var0 + " to " + param1);
            }
        }

        public final <T extends Comparable<T>> Condition.TerminalCondition term(Property<T> param0, T param1) {
            this.putValue(param0, param0.getName(param1));
            return this;
        }

        @SafeVarargs
        public final <T extends Comparable<T>> Condition.TerminalCondition term(Property<T> param0, T param1, T... param2) {
            this.putValue(param0, getTerm(param0, param1, param2));
            return this;
        }

        public final <T extends Comparable<T>> Condition.TerminalCondition negatedTerm(Property<T> param0, T param1) {
            this.putValue(param0, "!" + param0.getName(param1));
            return this;
        }

        @SafeVarargs
        public final <T extends Comparable<T>> Condition.TerminalCondition negatedTerm(Property<T> param0, T param1, T... param2) {
            this.putValue(param0, "!" + getTerm(param0, param1, param2));
            return this;
        }

        public JsonElement get() {
            JsonObject var0 = new JsonObject();
            this.terms.forEach((param1, param2) -> var0.addProperty(param1.getName(), param2));
            return var0;
        }

        @Override
        public void validate(StateDefinition<?, ?> param0) {
            List<Property<?>> var0 = this.terms.keySet().stream().filter(param1 -> param0.getProperty(param1.getName()) != param1).collect(Collectors.toList());
            if (!var0.isEmpty()) {
                throw new IllegalStateException("Properties " + var0 + " are missing from " + param0);
            }
        }
    }
}
