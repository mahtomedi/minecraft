package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;

public class StatePropertiesPredicate {
    public static final StatePropertiesPredicate ANY = new StatePropertiesPredicate(ImmutableList.of());
    private final List<StatePropertiesPredicate.PropertyMatcher> properties;

    private static StatePropertiesPredicate.PropertyMatcher fromJson(String param0, JsonElement param1) {
        if (param1.isJsonPrimitive()) {
            String var0 = param1.getAsString();
            return new StatePropertiesPredicate.ExactPropertyMatcher(param0, var0);
        } else {
            JsonObject var1 = GsonHelper.convertToJsonObject(param1, "value");
            String var2 = var1.has("min") ? getStringOrNull(var1.get("min")) : null;
            String var3 = var1.has("max") ? getStringOrNull(var1.get("max")) : null;
            return (StatePropertiesPredicate.PropertyMatcher)(var2 != null && var2.equals(var3)
                ? new StatePropertiesPredicate.ExactPropertyMatcher(param0, var2)
                : new StatePropertiesPredicate.RangedPropertyMatcher(param0, var2, var3));
        }
    }

    @Nullable
    private static String getStringOrNull(JsonElement param0) {
        return param0.isJsonNull() ? null : param0.getAsString();
    }

    StatePropertiesPredicate(List<StatePropertiesPredicate.PropertyMatcher> param0) {
        this.properties = ImmutableList.copyOf(param0);
    }

    public <S extends StateHolder<?, S>> boolean matches(StateDefinition<?, S> param0, S param1) {
        for(StatePropertiesPredicate.PropertyMatcher var0 : this.properties) {
            if (!var0.match(param0, param1)) {
                return false;
            }
        }

        return true;
    }

    public boolean matches(BlockState param0) {
        return this.matches(param0.getBlock().getStateDefinition(), param0);
    }

    public boolean matches(FluidState param0) {
        return this.matches(param0.getType().getStateDefinition(), param0);
    }

    public void checkState(StateDefinition<?, ?> param0, Consumer<String> param1) {
        this.properties.forEach(param2 -> param2.checkState(param0, param1));
    }

    public static StatePropertiesPredicate fromJson(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "properties");
            List<StatePropertiesPredicate.PropertyMatcher> var1 = Lists.newArrayList();

            for(Entry<String, JsonElement> var2 : var0.entrySet()) {
                var1.add(fromJson(var2.getKey(), var2.getValue()));
            }

            return new StatePropertiesPredicate(var1);
        } else {
            return ANY;
        }
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject var0 = new JsonObject();
            if (!this.properties.isEmpty()) {
                this.properties.forEach(param1 -> var0.add(param1.getName(), param1.toJson()));
            }

            return var0;
        }
    }

    public static class Builder {
        private final List<StatePropertiesPredicate.PropertyMatcher> matchers = Lists.newArrayList();

        private Builder() {
        }

        public static StatePropertiesPredicate.Builder properties() {
            return new StatePropertiesPredicate.Builder();
        }

        public StatePropertiesPredicate.Builder hasProperty(Property<?> param0, String param1) {
            this.matchers.add(new StatePropertiesPredicate.ExactPropertyMatcher(param0.getName(), param1));
            return this;
        }

        public StatePropertiesPredicate.Builder hasProperty(Property<Integer> param0, int param1) {
            return this.hasProperty(param0, Integer.toString(param1));
        }

        public StatePropertiesPredicate.Builder hasProperty(Property<Boolean> param0, boolean param1) {
            return this.hasProperty(param0, Boolean.toString(param1));
        }

        public <T extends Comparable<T> & StringRepresentable> StatePropertiesPredicate.Builder hasProperty(Property<T> param0, T param1) {
            return this.hasProperty(param0, param1.getSerializedName());
        }

        public StatePropertiesPredicate build() {
            return new StatePropertiesPredicate(this.matchers);
        }
    }

    static class ExactPropertyMatcher extends StatePropertiesPredicate.PropertyMatcher {
        private final String value;

        public ExactPropertyMatcher(String param0, String param1) {
            super(param0);
            this.value = param1;
        }

        @Override
        protected <T extends Comparable<T>> boolean match(StateHolder<?, ?> param0, Property<T> param1) {
            T var0 = param0.getValue(param1);
            Optional<T> var1 = param1.getValue(this.value);
            return var1.isPresent() && var0.compareTo(var1.get()) == 0;
        }

        @Override
        public JsonElement toJson() {
            return new JsonPrimitive(this.value);
        }
    }

    abstract static class PropertyMatcher {
        private final String name;

        public PropertyMatcher(String param0) {
            this.name = param0;
        }

        public <S extends StateHolder<?, S>> boolean match(StateDefinition<?, S> param0, S param1) {
            Property<?> var0 = param0.getProperty(this.name);
            return var0 == null ? false : this.match(param1, var0);
        }

        protected abstract <T extends Comparable<T>> boolean match(StateHolder<?, ?> var1, Property<T> var2);

        public abstract JsonElement toJson();

        public String getName() {
            return this.name;
        }

        public void checkState(StateDefinition<?, ?> param0, Consumer<String> param1) {
            Property<?> var0 = param0.getProperty(this.name);
            if (var0 == null) {
                param1.accept(this.name);
            }

        }
    }

    static class RangedPropertyMatcher extends StatePropertiesPredicate.PropertyMatcher {
        @Nullable
        private final String minValue;
        @Nullable
        private final String maxValue;

        public RangedPropertyMatcher(String param0, @Nullable String param1, @Nullable String param2) {
            super(param0);
            this.minValue = param1;
            this.maxValue = param2;
        }

        @Override
        protected <T extends Comparable<T>> boolean match(StateHolder<?, ?> param0, Property<T> param1) {
            T var0 = param0.getValue(param1);
            if (this.minValue != null) {
                Optional<T> var1 = param1.getValue(this.minValue);
                if (!var1.isPresent() || var0.compareTo(var1.get()) < 0) {
                    return false;
                }
            }

            if (this.maxValue != null) {
                Optional<T> var2 = param1.getValue(this.maxValue);
                if (!var2.isPresent() || var0.compareTo(var2.get()) > 0) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public JsonElement toJson() {
            JsonObject var0 = new JsonObject();
            if (this.minValue != null) {
                var0.addProperty("min", this.minValue);
            }

            if (this.maxValue != null) {
                var0.addProperty("max", this.maxValue);
            }

            return var0;
        }
    }
}
