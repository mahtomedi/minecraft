package net.minecraft.core;

import com.mojang.datafixers.util.Either;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public interface Holder<T> {
    T value();

    boolean isBound();

    boolean is(ResourceLocation var1);

    boolean is(ResourceKey<T> var1);

    boolean is(Predicate<ResourceKey<T>> var1);

    boolean is(TagKey<T> var1);

    Stream<TagKey<T>> tags();

    Either<ResourceKey<T>, T> unwrap();

    Optional<ResourceKey<T>> unwrapKey();

    Holder.Kind kind();

    boolean isValidInRegistry(Registry<T> var1);

    static <T> Holder<T> direct(T param0) {
        return new Holder.Direct<>(param0);
    }

    static <T> Holder<T> hackyErase(Holder<? extends T> param0) {
        return param0;
    }

    public static record Direct<T>(T value) implements Holder<T> {
        @Override
        public boolean isBound() {
            return true;
        }

        @Override
        public boolean is(ResourceLocation param0) {
            return false;
        }

        @Override
        public boolean is(ResourceKey<T> param0) {
            return false;
        }

        @Override
        public boolean is(TagKey<T> param0) {
            return false;
        }

        @Override
        public boolean is(Predicate<ResourceKey<T>> param0) {
            return false;
        }

        @Override
        public Either<ResourceKey<T>, T> unwrap() {
            return Either.right(this.value);
        }

        @Override
        public Optional<ResourceKey<T>> unwrapKey() {
            return Optional.empty();
        }

        @Override
        public Holder.Kind kind() {
            return Holder.Kind.DIRECT;
        }

        @Override
        public String toString() {
            return "Direct{" + this.value + "}";
        }

        @Override
        public boolean isValidInRegistry(Registry<T> param0) {
            return true;
        }

        @Override
        public Stream<TagKey<T>> tags() {
            return Stream.of();
        }
    }

    public static enum Kind {
        REFERENCE,
        DIRECT;
    }

    public static class Reference<T> implements Holder<T> {
        private final Registry<T> registry;
        private Set<TagKey<T>> tags = Set.of();
        private final Holder.Reference.Type type;
        @Nullable
        private ResourceKey<T> key;
        @Nullable
        private T value;

        private Reference(Holder.Reference.Type param0, Registry<T> param1, @Nullable ResourceKey<T> param2, @Nullable T param3) {
            this.registry = param1;
            this.type = param0;
            this.key = param2;
            this.value = param3;
        }

        public static <T> Holder.Reference<T> createStandAlone(Registry<T> param0, ResourceKey<T> param1) {
            return new Holder.Reference<>(Holder.Reference.Type.STAND_ALONE, param0, param1, (T)null);
        }

        @Deprecated
        public static <T> Holder.Reference<T> createIntrusive(Registry<T> param0, @Nullable T param1) {
            return new Holder.Reference<>(Holder.Reference.Type.INTRUSIVE, param0, null, param1);
        }

        public ResourceKey<T> key() {
            if (this.key == null) {
                throw new IllegalStateException("Trying to access unbound value '" + this.value + "' from registry " + this.registry);
            } else {
                return this.key;
            }
        }

        @Override
        public T value() {
            if (this.value == null) {
                throw new IllegalStateException("Trying to access unbound value '" + this.key + "' from registry " + this.registry);
            } else {
                return this.value;
            }
        }

        @Override
        public boolean is(ResourceLocation param0) {
            return this.key().location().equals(param0);
        }

        @Override
        public boolean is(ResourceKey<T> param0) {
            return this.key() == param0;
        }

        @Override
        public boolean is(TagKey<T> param0) {
            return this.tags.contains(param0);
        }

        @Override
        public boolean is(Predicate<ResourceKey<T>> param0) {
            return param0.test(this.key());
        }

        @Override
        public boolean isValidInRegistry(Registry<T> param0) {
            return this.registry == param0;
        }

        @Override
        public Either<ResourceKey<T>, T> unwrap() {
            return Either.left(this.key());
        }

        @Override
        public Optional<ResourceKey<T>> unwrapKey() {
            return Optional.of(this.key());
        }

        @Override
        public Holder.Kind kind() {
            return Holder.Kind.REFERENCE;
        }

        @Override
        public boolean isBound() {
            return this.key != null && this.value != null;
        }

        void bind(ResourceKey<T> param0, T param1) {
            if (this.key != null && param0 != this.key) {
                throw new IllegalStateException("Can't change holder key: existing=" + this.key + ", new=" + param0);
            } else if (this.type == Holder.Reference.Type.INTRUSIVE && this.value != param1) {
                throw new IllegalStateException("Can't change holder " + param0 + " value: existing=" + this.value + ", new=" + param1);
            } else {
                this.key = param0;
                this.value = param1;
            }
        }

        void bindTags(Collection<TagKey<T>> param0) {
            this.tags = Set.copyOf(param0);
        }

        @Override
        public Stream<TagKey<T>> tags() {
            return this.tags.stream();
        }

        @Override
        public String toString() {
            return "Reference{" + this.key + "=" + this.value + "}";
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (!(param0 instanceof Holder.Reference)) {
                return false;
            } else {
                Holder.Reference var0 = (Holder.Reference)param0;
                if (this.key != null && var0.key != null && this.value != null && var0.value != null) {
                    return this.registry.key().equals(var0.registry.key())
                        && this.type == var0.type
                        && this.key.equals(var0.key)
                        && this.value.equals(var0.value);
                } else {
                    return false;
                }
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.registry.key(), this.type, this.key, this.value);
        }

        static enum Type {
            STAND_ALONE,
            INTRUSIVE;
        }
    }
}
