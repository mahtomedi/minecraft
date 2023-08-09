package net.minecraft.core;

import com.mojang.datafixers.util.Either;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.VisibleForTesting;

public interface HolderSet<T> extends Iterable<Holder<T>> {
    Stream<Holder<T>> stream();

    int size();

    Either<TagKey<T>, List<Holder<T>>> unwrap();

    Optional<Holder<T>> getRandomElement(RandomSource var1);

    Holder<T> get(int var1);

    boolean contains(Holder<T> var1);

    boolean canSerializeIn(HolderOwner<T> var1);

    Optional<TagKey<T>> unwrapKey();

    @Deprecated
    @VisibleForTesting
    static <T> HolderSet.Named<T> emptyNamed(HolderOwner<T> param0, TagKey<T> param1) {
        return new HolderSet.Named<>(param0, param1);
    }

    @SafeVarargs
    static <T> HolderSet.Direct<T> direct(Holder<T>... param0) {
        return new HolderSet.Direct<>(List.of(param0));
    }

    static <T> HolderSet.Direct<T> direct(List<? extends Holder<T>> param0) {
        return new HolderSet.Direct<>(List.copyOf(param0));
    }

    @SafeVarargs
    static <E, T> HolderSet.Direct<T> direct(Function<E, Holder<T>> param0, E... param1) {
        return direct(Stream.of(param1).map(param0).toList());
    }

    static <E, T> HolderSet.Direct<T> direct(Function<E, Holder<T>> param0, Collection<E> param1) {
        return direct(param1.stream().map(param0).toList());
    }

    public static class Direct<T> extends HolderSet.ListBacked<T> {
        private final List<Holder<T>> contents;
        @Nullable
        private Set<Holder<T>> contentsSet;

        Direct(List<Holder<T>> param0) {
            this.contents = param0;
        }

        @Override
        protected List<Holder<T>> contents() {
            return this.contents;
        }

        @Override
        public Either<TagKey<T>, List<Holder<T>>> unwrap() {
            return Either.right(this.contents);
        }

        @Override
        public Optional<TagKey<T>> unwrapKey() {
            return Optional.empty();
        }

        @Override
        public boolean contains(Holder<T> param0) {
            if (this.contentsSet == null) {
                this.contentsSet = Set.copyOf(this.contents);
            }

            return this.contentsSet.contains(param0);
        }

        @Override
        public String toString() {
            return "DirectSet[" + this.contents + "]";
        }
    }

    public abstract static class ListBacked<T> implements HolderSet<T> {
        protected abstract List<Holder<T>> contents();

        @Override
        public int size() {
            return this.contents().size();
        }

        @Override
        public Spliterator<Holder<T>> spliterator() {
            return this.contents().spliterator();
        }

        @Override
        public Iterator<Holder<T>> iterator() {
            return this.contents().iterator();
        }

        @Override
        public Stream<Holder<T>> stream() {
            return this.contents().stream();
        }

        @Override
        public Optional<Holder<T>> getRandomElement(RandomSource param0) {
            return Util.getRandomSafe(this.contents(), param0);
        }

        @Override
        public Holder<T> get(int param0) {
            return this.contents().get(param0);
        }

        @Override
        public boolean canSerializeIn(HolderOwner<T> param0) {
            return true;
        }
    }

    public static class Named<T> extends HolderSet.ListBacked<T> {
        private final HolderOwner<T> owner;
        private final TagKey<T> key;
        private List<Holder<T>> contents = List.of();

        Named(HolderOwner<T> param0, TagKey<T> param1) {
            this.owner = param0;
            this.key = param1;
        }

        void bind(List<Holder<T>> param0) {
            this.contents = List.copyOf(param0);
        }

        public TagKey<T> key() {
            return this.key;
        }

        @Override
        protected List<Holder<T>> contents() {
            return this.contents;
        }

        @Override
        public Either<TagKey<T>, List<Holder<T>>> unwrap() {
            return Either.left(this.key);
        }

        @Override
        public Optional<TagKey<T>> unwrapKey() {
            return Optional.of(this.key);
        }

        @Override
        public boolean contains(Holder<T> param0) {
            return param0.is(this.key);
        }

        @Override
        public String toString() {
            return "NamedSet(" + this.key + ")[" + this.contents + "]";
        }

        @Override
        public boolean canSerializeIn(HolderOwner<T> param0) {
            return this.owner.canSerializeIn(param0);
        }
    }
}
