package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;

public record StatePropertiesPredicate<S>(List<StatePropertiesPredicate.PropertyMatcher> properties) {
    private static final Codec<List<StatePropertiesPredicate.PropertyMatcher>> PROPERTIES_CODEC = Codec.unboundedMap(
            Codec.STRING, StatePropertiesPredicate.ValueMatcher.CODEC
        )
        .xmap(
            param0 -> param0.entrySet()
                    .stream()
                    .map(
                        param0x -> new StatePropertiesPredicate.PropertyMatcher(
                                (String)param0x.getKey(), (StatePropertiesPredicate.ValueMatcher)param0x.getValue()
                            )
                    )
                    .toList(),
            param0 -> param0.stream()
                    .collect(Collectors.toMap(StatePropertiesPredicate.PropertyMatcher::name, StatePropertiesPredicate.PropertyMatcher::valueMatcher))
        );
    public static final Codec<StatePropertiesPredicate> CODEC = PROPERTIES_CODEC.xmap(StatePropertiesPredicate::new, StatePropertiesPredicate::properties);

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

    public Optional<String> checkState(StateDefinition<?, ?> param0) {
        for(StatePropertiesPredicate.PropertyMatcher var0 : this.properties) {
            Optional<String> var1 = var0.checkState(param0);
            if (var1.isPresent()) {
                return var1;
            }
        }

        return Optional.empty();
    }

    public void checkState(StateDefinition<?, ?> param0, Consumer<String> param1) {
        this.properties.forEach(param2 -> param2.checkState(param0).ifPresent(param1));
    }

    public static Optional<StatePropertiesPredicate> fromJson(@Nullable JsonElement param0) {
        return param0 != null && !param0.isJsonNull()
            ? Optional.of(Util.getOrThrow(CODEC.parse(JsonOps.INSTANCE, param0), JsonParseException::new))
            : Optional.empty();
    }

    public JsonElement serializeToJson() {
        return Util.getOrThrow(CODEC.encodeStart(JsonOps.INSTANCE, this), IllegalStateException::new);
    }

    public static class Builder {
        private final ImmutableList.Builder<StatePropertiesPredicate.PropertyMatcher> matchers = ImmutableList.builder();

        private Builder() {
        }

        public static StatePropertiesPredicate.Builder properties() {
            return new StatePropertiesPredicate.Builder();
        }

        public StatePropertiesPredicate.Builder hasProperty(Property<?> param0, String param1) {
            this.matchers.add(new StatePropertiesPredicate.PropertyMatcher(param0.getName(), new StatePropertiesPredicate.ExactMatcher(param1)));
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

        public Optional<StatePropertiesPredicate> build() {
            ImmutableList<StatePropertiesPredicate.PropertyMatcher> var0 = this.matchers.build();
            return var0.isEmpty() ? Optional.empty() : Optional.of(new StatePropertiesPredicate(var0));
        }
    }

    static record ExactMatcher<T>(String value) implements StatePropertiesPredicate.ValueMatcher {
        public static final Codec<StatePropertiesPredicate.ExactMatcher> CODEC = Codec.STRING
            .xmap(StatePropertiesPredicate.ExactMatcher::new, StatePropertiesPredicate.ExactMatcher::value);

        @Override
        public <T extends Comparable<T>> boolean match(StateHolder<?, ?> param0, Property<T> param1) {
            T var0 = param0.getValue(param1);
            Optional<T> var1 = param1.getValue(this.value);
            return var1.isPresent() && var0.compareTo(var1.get()) == 0;
        }
    }

    static record PropertyMatcher<S>(String name, StatePropertiesPredicate.ValueMatcher valueMatcher) {
        public <S extends StateHolder<?, S>> boolean match(StateDefinition<?, S> param0, S param1) {
            Property<?> var0 = param0.getProperty(this.name);
            return var0 != null && this.valueMatcher.match(param1, var0);
        }

        public Optional<String> checkState(StateDefinition<?, ?> param0) {
            Property<?> var0 = param0.getProperty(this.name);
            return var0 != null ? Optional.empty() : Optional.of(this.name);
        }
    }

    static record RangedMatcher<T>(Optional<String> minValue, Optional<String> maxValue) implements StatePropertiesPredicate.ValueMatcher {
        public static final Codec<StatePropertiesPredicate.RangedMatcher> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.strictOptionalField(Codec.STRING, "min").forGetter(StatePropertiesPredicate.RangedMatcher::minValue),
                        ExtraCodecs.strictOptionalField(Codec.STRING, "max").forGetter(StatePropertiesPredicate.RangedMatcher::maxValue)
                    )
                    .apply(param0, StatePropertiesPredicate.RangedMatcher::new)
        );

        @Override
        public <T extends Comparable<T>> boolean match(StateHolder<?, ?> param0, Property<T> param1) {
            T var0 = param0.getValue(param1);
            if (this.minValue.isPresent()) {
                Optional<T> var1 = param1.getValue(this.minValue.get());
                if (var1.isEmpty() || var0.compareTo(var1.get()) < 0) {
                    return false;
                }
            }

            if (this.maxValue.isPresent()) {
                Optional<T> var2 = param1.getValue(this.maxValue.get());
                if (var2.isEmpty() || var0.compareTo(var2.get()) > 0) {
                    return false;
                }
            }

            return true;
        }
    }

    interface ValueMatcher {
        Codec<StatePropertiesPredicate.ValueMatcher> CODEC = Codec.either(
                StatePropertiesPredicate.ExactMatcher.CODEC, StatePropertiesPredicate.RangedMatcher.CODEC
            )
            .xmap(param0 -> param0.map(param0x -> param0x, param0x -> param0x), param0 -> {
                if (param0 instanceof StatePropertiesPredicate.ExactMatcher var0) {
                    return Either.left(var0);
                } else if (param0 instanceof StatePropertiesPredicate.RangedMatcher var1) {
                    return Either.right(var1);
                } else {
                    throw new UnsupportedOperationException();
                }
            });

        <T extends Comparable<T>> boolean match(StateHolder<?, ?> var1, Property<T> var2);
    }
}
